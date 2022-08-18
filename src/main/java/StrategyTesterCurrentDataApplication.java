import com.google.inject.Guice;
import com.google.inject.Injector;
import database.DatabaseModule;
import database.entities.TradeEntity;
import database.entities.TradeOrderEntity;
import database.entities.TradeOrderStatus;
import lombok.extern.slf4j.Slf4j;
import services.httpclients.HttpClientModule;
import services.httpclients.kraken.KrakenClient;
import services.httpclients.kraken.KrakenModule;
import services.httpclients.kraken.response.trades.TradeDetails;
import services.httpclients.kraken.response.trades.TradesResponse;
import services.strategies.TradingStrategiesModule;
import services.strategies.exitstrategies.ExitStrategy;
import services.strategies.exitstrategies.TrailingStopExitStrategy;
import services.strategies.tradingstrategies.ThreeEmaCrossoverStrategy;
import services.strategies.tradingstrategies.TradingStrategy;
import services.trades.TradeService;
import valueobjects.timeframe.Tick;
import valueobjects.timeframe.Timeframe;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Goes through the specified timeframe from historic data from the api and saves trades it would execute in a live env.
 */
@Slf4j
public class StrategyTesterCurrentDataApplication {

    public static void main(String[] args) {
        int timeframeSize = 250;
        String assetCode = "XBTGBP";
        String assetDetailCode = "XXBTZGBP";

        Injector injector = Guice.createInjector(new HttpClientModule(), new KrakenModule(), new DatabaseModule(), new TradingStrategiesModule());
        KrakenClient krakenClient = injector.getInstance(KrakenClient.class);
        TradeService tradeService = injector.getInstance(TradeService.class);

        // select a strategy here
        TradingStrategy strategy = injector.getInstance(ThreeEmaCrossoverStrategy.class);

        // select exit strategy here
        ExitStrategy exitStrategy = injector.getInstance(TrailingStopExitStrategy.class);

        Timeframe timeframe = new Timeframe(timeframeSize);

        // initialize timeframe with previous trades
        Optional<TradesResponse> tradesResponse = krakenClient.getHistoricData(strategy.periodLength());
        tradesResponse.ifPresent(response -> {
            List<TradeDetails> tradeDetails = response.getResult().getTradeDetails(assetDetailCode);
            tradeDetails.stream().sorted(Comparator.comparing(TradeDetails::getTime))
                    .collect(Collectors.toList())
                    .forEach(detail -> {
                        Tick ticker = new Tick(detail.getTime(), detail.getPrice());
                        timeframe.addTick(ticker);
                    });
        });
        log.info(strategy.name() + ": Initialized trading timeframe.");

        // skip as many items as your strategy needs based on its period
        int skipItems = 55;
        for (int i = skipItems; i < timeframeSize - 1; i++) {
            Timeframe subframe = timeframe.subframe(i);
            Tick currentTick = subframe.getTicks().getLast();

            // check open trades and close if exit strategy says so
            tradeService.getOpenTradesByStrategy(strategy.name()).forEach(trade -> {
                exitStrategy.strategy().apply(trade.getId(), subframe).ifPresent(closeTradeSignal -> {
                    log.info("Exiting the following order with these details!!");
                    log.info("    TradeId: " + trade.getId());
                    log.info("    ExitSignal: " + closeTradeSignal);
                    log.info("    ExitPrice: " + currentTick.getValue());
                    log.info("    ExitTime: " + currentTick.getTime().toString());

                    TradeOrderEntity exitOrder = new TradeOrderEntity();
                    exitOrder.setOrderReference(1234);
                    exitOrder.setTradingSignal(closeTradeSignal);
                    exitOrder.setPrice(currentTick.getValue());
                    exitOrder.setVolume(BigDecimal.TEN);
                    exitOrder.setTime(currentTick.getTime());
                    exitOrder.setStatus(TradeOrderStatus.PENDING);
                    exitOrder.setAssetCode(assetCode);
                    exitOrder.setCost(BigDecimal.ZERO);

                    trade.addOrder(exitOrder);

                    BigDecimal margin = trade.getEntryOrder().getTradingSignal().equals(TradingStrategy.TradingSignal.BUY)
                            ? currentTick.getValue().subtract(trade.getEntryOrder().getPrice())
                            : trade.getEntryOrder().getPrice().subtract(currentTick.getValue());
                    BigDecimal divisor = currentTick.getValue().add(trade.getEntryOrder().getPrice()).divide(BigDecimal.valueOf(2), 10, RoundingMode.HALF_EVEN);
                    trade.setProfit(margin.divide(divisor, 10, RoundingMode.HALF_EVEN).multiply(BigDecimal.valueOf(100)));

                    tradeService.save(trade);
                });
            });


            strategy.strategy().apply(subframe)
                    // react to the specified trading signal
                    .ifPresent(signal -> {

                        TradeOrderEntity entryOrder = new TradeOrderEntity();
                        entryOrder.setOrderReference(1234);
                        entryOrder.setTradingSignal(signal);
                        entryOrder.setPrice(currentTick.getValue());
                        entryOrder.setStatus(TradeOrderStatus.PENDING);
                        entryOrder.setVolume(BigDecimal.TEN);
                        entryOrder.setTime(currentTick.getTime());
                        entryOrder.setAssetCode(assetCode);
                        entryOrder.setCost(BigDecimal.ZERO);

                        TradeEntity trade = new TradeEntity();
                        trade.setEntryStrategy(strategy.name());
                        trade.setExitStrategy(exitStrategy.name());
                        trade.setPeriodLength(strategy.periodLength());
                        trade.addOrder(entryOrder);

                        tradeService.save(trade);
                    });
        }
    }
}
