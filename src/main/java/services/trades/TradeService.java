package services.trades;

import com.google.inject.Inject;
import database.daos.TradeDao;
import database.entities.TradeEntity;
import database.entities.TradeOrderEntity;
import database.entities.TradeOrderStatus;
import database.entities.TradeOrderType;
import lombok.extern.slf4j.Slf4j;
import properties.PropertiesService;
import properties.TradeProperties;
import services.httpclients.kraken.KrakenClient;
import services.httpclients.kraken.response.addorder.AddOrderResult;
import services.httpclients.kraken.response.balance.BalanceResponse;
import services.strategies.tradingstrategies.TradingStrategy;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Service responsible to execute and manage trades in the system.
 */
@Slf4j
public class TradeService {

    private final TradeDao tradeDao;
    private final KrakenClient client;
    private final TradeProperties properties;

    @Inject
    public TradeService(TradeDao tradeDao, KrakenClient client, PropertiesService propertiesService) {
        this.tradeDao = tradeDao;
        this.client = client;
        this.properties = propertiesService.loadProperties(TradeProperties.class).orElseThrow();
    }

    public void save(TradeEntity trade) {
        tradeDao.save(trade);
    }

    public List<TradeEntity> getOpenTradesByStrategy(String strategyName) {
        return tradeDao.findOpenTradesByStrategy(strategyName);

    }

    public Optional<TradeEntity> getById(BigInteger id) {
        return tradeDao.findById(id);
    }

    // Generates a unique 32bit integer to be used as order reference. int will always be 32bit.
    protected int generateTradeReference() {
        return ThreadLocalRandom.current().nextInt(1000000000, Integer.MAX_VALUE);
    }

    /**
     * Will update the trade with a profit if there is an executed exit order.
     */
    public Optional<BigDecimal> calculateTradeProfit(TradeEntity trade) {
        return trade.getExitOrder().map(exitOrder -> {
            if (TradeOrderStatus.EXECUTED.equals(exitOrder.getStatus())) {
                BigDecimal entryCost = trade.getEntryOrder().getCost();
                BigDecimal exitCost = exitOrder.getCost();

                return exitCost.subtract(entryCost);
            }
            return null;
        });
    }

    /**
     * Opens a new trade. A new trade opens with a new entry order. A new order will have a status of pending and no
     * price info at first. When the order is executed we can update the db entry.
     * This will happen next time we check our pending orders in the API.
     */
    public void openTrade(BigDecimal price, TradingStrategy.TradingSignal tradingSignal, TradingStrategy tradingStrategy) {
        int orderReference = generateTradeReference();

        BigDecimal volume = properties.usesLeverage()
                ? getOpenTradeVolumeWithLeverage(price)
                : getOpenTradeVolume(price, tradingSignal);

        // post the order
        client.postMarketOrder(orderReference, volume, tradingSignal)
                // log the order in the db if successful
                .ifPresentOrElse(
                        response -> Optional.ofNullable(response.getResult()).map(AddOrderResult::getTxid)
                                // get the first transaction as there should only be 1.
                                .flatMap(transactionList -> transactionList.stream().findFirst())
                                .ifPresentOrElse(orderTransactionId -> {
                                    TradeOrderEntity entryOrder = new TradeOrderEntity();
                                    entryOrder.setOrderReference(orderReference);
                                    entryOrder.setOrderTransaction(orderTransactionId);
                                    entryOrder.setTradingSignal(tradingSignal);
                                    entryOrder.setVolume(volume);
                                    entryOrder.setTime(LocalDateTime.now());
                                    entryOrder.setStatus(TradeOrderStatus.PENDING);
                                    entryOrder.setAssetCode(properties.getAssetCode());
                                    entryOrder.setType(TradeOrderType.ENTRY);
                                    entryOrder.setCost(null);
                                    entryOrder.setPrice(null);
                                    entryOrder.setVolumeExec(null);

                                    TradeEntity trade = new TradeEntity();
                                    trade.setEntryStrategy(tradingStrategy.name());
                                    trade.setExitStrategy(tradingStrategy.exitStrategyName());
                                    trade.setPeriodLength(tradingStrategy.periodLength());
                                    trade.addOrder(entryOrder);

                                    tradeDao.save(trade);
                                }, () -> log.error("Order transaction id was not returned by the api which means the order was not successful.")),
                        () -> log.error("Trade was not opened because the api request was not successful.")
                );
    }

    /**
     * It will calculate the amount/volume of coins to long/short based on the specified price, the account at risk percentage and
     * the total account balance.
     * ex. (accountAtRisk * totalAccountBalance) / currentPrice = volume
     */
    protected BigDecimal getOpenTradeVolumeWithLeverage(BigDecimal price) {
        return client.getAccountBalance()
                .map(accountBalanceResponse -> properties.getAccountRisk()
                        .multiply(accountBalanceResponse.getAccountBalance().divide(price, 10, RoundingMode.HALF_EVEN)))
                .orElseThrow(() -> new IllegalStateException("Api query to get account balances failed. Trade cancelled."));
    }

    protected BigDecimal getOpenTradeVolume(BigDecimal price, TradingStrategy.TradingSignal tradingSignal) {
        return client.getAssetsBalance().map(balanceResponse ->
                        TradingStrategy.TradingSignal.BUY.equals(tradingSignal)
                                ? getBuyCryptoVolume(balanceResponse, price)
                                : getSellCryptoVolume(balanceResponse))
                .orElseThrow(() -> new IllegalStateException("Api query to get account balances failed. Trade cancelled."));
    }

    /**
     * Calculates the amount/volume of crypto to buy using cash. The calculation is based on the capital at risk percentage.
     * Cash is first transformed to the volume of crypto based on the current price.
     */
    protected BigDecimal getBuyCryptoVolume(BalanceResponse balanceResponse, BigDecimal price) {
        String assetCode = properties.getBuyAssetCode();
        BigDecimal cashBalance = balanceResponse.getResult().getAssetBalance(assetCode);
        BigDecimal assetBalance = cashBalance.divide(price, 10, RoundingMode.HALF_EVEN);
        return properties.getAccountRisk().multiply(assetBalance);
    }

    /**
     * Calculates the amount/volume of crypto to sell based on the capital at risk config.
     */
    protected BigDecimal getSellCryptoVolume(BalanceResponse balanceResponse) {
        String assetCode = properties.getSellAssetCode();
        BigDecimal assetBalance = balanceResponse.getResult().getAssetBalance(assetCode);
        return properties.getAccountRisk().multiply(assetBalance);
    }

    /**
     * Will post an order to complete the trade and hopefully make some money.
     * It will try to sell/buy the same volume of coin we sold/bought when we entered this trade.
     */
    public void closeTrade(BigDecimal price, TradeEntity trade) {
        int orderReference = generateTradeReference();

        // close the trade by executing the opposite transaction
        TradingStrategy.TradingSignal tradingSignal = TradingStrategy.TradingSignal.BUY.equals(trade.getEntryOrder().getTradingSignal())
                ? TradingStrategy.TradingSignal.SELL : TradingStrategy.TradingSignal.BUY;

        // since we are closing a trade we want to exit with the same amount we entered if possible.
        // other trades might have changed the available cash/coins so check if we have the exec volume in our balance before we send the order.
        BigDecimal volumeBalance = client.getAssetsBalance().map(balanceResponse ->
                        TradingStrategy.TradingSignal.BUY.equals(tradingSignal)
                                ? balanceResponse.getResult().getAssetBalance(properties.getBuyAssetCode()).divide(price, 10, RoundingMode.HALF_EVEN)
                                : balanceResponse.getResult().getAssetBalance(properties.getSellAssetCode()))
                .orElseThrow(() -> new IllegalStateException("Api query to get account balances failed. Trade cancelled."));
        BigDecimal volumeExec = trade.getEntryOrder().getVolumeExec();
        // if we use leverage we always want to close a trade with the entry order executed volume
        BigDecimal volume = !properties.usesLeverage() && volumeExec.compareTo(volumeBalance) > 0 ? volumeBalance : volumeExec;

        client.postMarketOrder(orderReference, volume, tradingSignal)
                .ifPresentOrElse(
                        response -> Optional.ofNullable(response.getResult().getTxid())
                                // get the first transaction as there should only be 1.
                                .flatMap(transactionList -> transactionList.stream().findFirst())
                                .ifPresentOrElse(orderTransactionId -> {
                                    TradeOrderEntity exitOrder = new TradeOrderEntity();
                                    exitOrder.setOrderReference(orderReference);
                                    exitOrder.setOrderTransaction(orderTransactionId);
                                    exitOrder.setTradingSignal(tradingSignal);
                                    exitOrder.setVolume(volume);
                                    exitOrder.setTime(LocalDateTime.now());
                                    exitOrder.setStatus(TradeOrderStatus.PENDING);
                                    exitOrder.setAssetCode(properties.getAssetCode());
                                    exitOrder.setType(TradeOrderType.EXIT);
                                    exitOrder.setCost(null);
                                    exitOrder.setPrice(null);
                                    exitOrder.setVolumeExec(null);

                                    trade.addOrder(exitOrder);

                                    tradeDao.save(trade);
                                }, () -> log.error("Order transaction id was not returned by the api which means the order was not successful.")),
                        () -> log.error("Trade was not closed because the api request was not successful.")
                );
    }
}
