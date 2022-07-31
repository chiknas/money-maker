import com.google.inject.Guice;
import com.google.inject.Injector;
import httpclients.HttpClientModule;
import httpclients.kraken.KrakenClient;
import httpclients.kraken.KrakenModule;
import httpclients.kraken.response.trades.TradeDetails;
import httpclients.kraken.response.trades.TradesResponse;
import lombok.extern.slf4j.Slf4j;
import services.strategies.MovingAverageCrossoverStrategy;
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
        String assetCode = "XBTUSD";
        String assetDetailCode = "XXBTZUSD";

        Injector injector = Guice.createInjector(new HttpClientModule(), new KrakenModule());
        KrakenClient krakenClient = injector.getInstance(KrakenClient.class);
        MovingAverageCrossoverStrategy movingAverageCrossoverStrategy = injector.getInstance(MovingAverageCrossoverStrategy.class);

        Timeframe<BigDecimal> timeframe = new Timeframe<>(timeframeSize);

        // initialize timeframe with previous trades
        Optional<TradesResponse> tradesResponse = krakenClient.getHistoricData(assetCode);
        tradesResponse.ifPresent(response -> {
            List<TradeDetails> tradeDetails = response.getResult().getTradeDetails(assetDetailCode);
            tradeDetails.stream().sorted(Comparator.comparing(TradeDetails::getTime))
                    .collect(Collectors.toList())
                    .forEach(detail -> {
                        Tick<BigDecimal> ticker = new Tick<>(detail.getTime(), detail.getPrice());
                        timeframe.addTick(ticker);
                    });
        });

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            BigDecimal currentPrice = krakenClient.getTickerInfo(assetCode).get().getResult().get(assetDetailCode).getCurrentPrice();
            boolean isPriceChanged = timeframe.getTicks().isEmpty() || !timeframe.getTicks().getLast().getValue().equals(currentPrice);
            if (isPriceChanged) {
                timeframe.addTick(currentPrice);

                if (timeframe.isFull()) {
                    movingAverageCrossoverStrategy.strategy(50, 100).apply(timeframe).ifPresent(strat -> {
                        log.info("    Current price: " + currentPrice);
                        log.info(strat.toString());
                    });
                }
            }
        }, 2, 60, TimeUnit.SECONDS);
    }
}
