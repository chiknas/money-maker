import com.google.inject.Guice;
import com.google.inject.Injector;
import database.DatabaseModule;
import httpclients.HttpClientModule;
import httpclients.kraken.KrakenClient;
import httpclients.kraken.KrakenModule;
import httpclients.kraken.response.trades.TradeDetails;
import httpclients.kraken.response.trades.TradesResponse;
import lombok.extern.slf4j.Slf4j;
import services.BannerService;
import services.strategies.GoldenCrossStrategy;
import services.trades.TradeService;
import valueobjects.timeframe.Tick;
import valueobjects.timeframe.Timeframe;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class MoneyMakerApplication {

    public static void main(String[] args) {

        int timeframeSize = 200;
        String assetCode = "XBTGBP";
        String assetDetailCode = "XXBTZGBP";

        new BannerService().printBanner();

        Injector injector = Guice.createInjector(new HttpClientModule(), new KrakenModule(), new DatabaseModule());
        KrakenClient krakenClient = injector.getInstance(KrakenClient.class);
        TradeService tradeService = injector.getInstance(TradeService.class);
        GoldenCrossStrategy goldenCrossStrategy = injector.getInstance(GoldenCrossStrategy.class);

        Timeframe<BigDecimal> timeframe = new Timeframe<>(timeframeSize);

        // initialize timeframe with previous trades
        Optional<TradesResponse> tradesResponse = krakenClient.getHistoricData(assetCode, goldenCrossStrategy.periodLength());
        tradesResponse.ifPresent(response -> {
            List<TradeDetails> tradeDetails = response.getResult().getTradeDetails(assetDetailCode);
            tradeDetails.stream().sorted(Comparator.comparing(TradeDetails::getTime))
                    .collect(Collectors.toList())
                    .forEach(detail -> {
                        Tick<BigDecimal> ticker = new Tick<>(detail.getTime(), detail.getPrice());
                        timeframe.addTick(ticker);
                    });
        });
        log.info("Initialized trading timeframe.");

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> krakenClient.getTickerInfo(assetCode).ifPresent(tickerPairResponse -> {
            BigDecimal currentPrice = tickerPairResponse.getResult().get(assetDetailCode).getCurrentPrice();
            boolean isPriceChanged = timeframe.getTicks().isEmpty() || !timeframe.getTicks().getLast().getValue().equals(currentPrice);
            if (isPriceChanged) {
                timeframe.addTick(currentPrice);

                if (timeframe.isFull()) {
                    goldenCrossStrategy.strategy().apply(timeframe)
                            .ifPresent(strat -> tradeService.trade(assetCode, timeframe.getTicks().getLast(), strat));
                }
            }
        }), 2, goldenCrossStrategy.periodLength().getSeconds(), TimeUnit.SECONDS);
        log.info("Trading session started! Looking for a good ticker to trade: " + assetCode);
    }
}
