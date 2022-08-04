import com.google.inject.Guice;
import com.google.inject.Injector;
import database.DatabaseModule;
import entities.TradeTransaction;
import httpclients.HttpClientModule;
import httpclients.kraken.KrakenClient;
import httpclients.kraken.KrakenModule;
import httpclients.kraken.response.trades.TradeDetails;
import httpclients.kraken.response.trades.TradesResponse;
import lombok.extern.slf4j.Slf4j;
import services.strategies.ThreeEmaCrossoverStrategy;
import services.strategies.TradingStrategiesModule;
import services.strategies.TradingStrategy;
import services.trades.TradeService;
import valueobjects.timeframe.Tick;
import valueobjects.timeframe.Timeframe;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Goes through the specified timeframe from historic data from the api and saves trades it would execute in a live env.
 */
@Slf4j
public class StrategyTesterApplication {

    public static void main(String[] args) {
        int timeframeSize = 200;
        String assetCode = "XBTGBP";
        String assetDetailCode = "XXBTZGBP";

        Injector injector = Guice.createInjector(new HttpClientModule(), new KrakenModule(), new DatabaseModule(), new TradingStrategiesModule());
        KrakenClient krakenClient = injector.getInstance(KrakenClient.class);
        TradeService tradeService = injector.getInstance(TradeService.class);

        // select a strategy here
        TradingStrategy strategy = injector.getInstance(ThreeEmaCrossoverStrategy.class);

        Timeframe timeframe = new Timeframe(timeframeSize);

        // initialize timeframe with previous trades
        Optional<TradesResponse> tradesResponse = krakenClient.getHistoricData(assetCode, strategy.periodLength());
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
            strategy.strategy().apply(subframe)
                    // react to the specified trading signal
                    .ifPresent(signal -> {
                        TradeTransaction tradeTransaction = new TradeTransaction();
                        tradeTransaction.setType(signal);
                        tradeTransaction.setPrice(subframe.getTicks().getLast().getValue());
                        tradeTransaction.setStrategy(strategy.name());
                        tradeTransaction.setTime(subframe.getTicks().getLast().getTime());
                        tradeTransaction.setAssetCode(assetCode);
                        tradeTransaction.setCost(BigDecimal.ZERO);
                        tradeTransaction.setPeriodLength(strategy.periodLength());
                        tradeService.trade(tradeTransaction);
                    });
        }
    }
}
