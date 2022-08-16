package services.trades;

import com.google.inject.Inject;
import database.daos.TradeDao;
import database.entities.TradeEntity;
import database.entities.TradeOrderEntity;
import database.entities.TradeOrderStatus;
import lombok.extern.slf4j.Slf4j;
import services.httpclients.kraken.KrakenClient;
import services.strategies.tradingstrategies.TradingStrategy;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service responsible to execute and manage trades in the system.
 */
@Slf4j
public class TradeService {

    private final TradeDao transactionsDao;
    private final KrakenClient client;

    @Inject
    public TradeService(TradeDao transactionsDao, KrakenClient client) {
        this.transactionsDao = transactionsDao;
        this.client = client;
    }

    public void save(TradeEntity trade) {
        transactionsDao.save(trade);
    }

    public List<TradeEntity> getOpenTradesByStrategy(String strategyName) {
        return transactionsDao.findOpenTradesByStrategy(strategyName);

    }

    public Optional<TradeEntity> getById(BigInteger id) {
        return transactionsDao.findById(id);
    }

    /**
     * Opens a new trade. A new trade opens with a new entry order. A new order will have a status of pending and no
     * price info at first. When the order is executed we can update the db entry.
     * This will happen next time we check our pending orders in the API.
     */
    public void openTrade(String assetCode, TradingStrategy.TradingSignal tradingSignal, TradingStrategy tradingStrategy) {
        BigDecimal volume = BigDecimal.TEN;
        UUID orderReference = UUID.randomUUID();

        client.postMarketOrder(assetCode, BigDecimal.TEN, tradingSignal).ifPresentOrElse(
                response -> Optional.ofNullable(response.getResult().getTxid())
                        // get the first transaction as there should only be 1.
                        .flatMap(transactionList -> transactionList.stream().findFirst())
                        .ifPresentOrElse(orderTransactionId -> {
                            TradeOrderEntity entryOrder = new TradeOrderEntity();
                            entryOrder.setOrderReference(orderReference);
                            entryOrder.setOrderTransaction(orderTransactionId);
                            entryOrder.setType(tradingSignal);
                            entryOrder.setPrice(null);
                            entryOrder.setVolume(volume);
                            entryOrder.setTime(LocalDateTime.now());
                            entryOrder.setStatus(TradeOrderStatus.PENDING);
                            entryOrder.setAssetCode(assetCode);
                            entryOrder.setCost(null);

                            TradeEntity trade = new TradeEntity();
                            trade.setEntryStrategy(tradingStrategy.name());
                            trade.setPeriodLength(tradingStrategy.periodLength());
                            trade.setEntryOrder(entryOrder);

                            transactionsDao.save(trade);
                        }, () -> log.error("Order transaction id was not returned by the api which means the order was not successful.")),
                () -> log.error("Trade was not opened because the api request was not successful.")
        );
    }
}
