import com.google.inject.Guice;
import com.google.inject.Injector;
import httpclients.HttpClientModule;
import httpclients.kraken.KrakenClient;
import httpclients.kraken.KrakenModule;
import httpclients.kraken.response.trades.TradeDetails;
import httpclients.kraken.response.trades.TradesResponse;
import trading.strategies.MovingAverageCrossoverStrategy;
import trading.timeframe.Tick;
import trading.timeframe.Timeframe;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MoneyMakerApplication {
    public static void main(String[] args) throws IOException, InterruptedException {
        int timeframeSize = 200;
        String assetCode = "XBTUSD";
        String assetDetailCode = "XXBTZUSD";

        Injector injector = Guice.createInjector(new HttpClientModule(), new KrakenModule());
        KrakenClient krakenClient = injector.getInstance(KrakenClient.class);

        Timeframe<BigDecimal> timeframe = new Timeframe<>(timeframeSize);

        // initialize timeframe with previous trades
        Optional<TradesResponse> tradesResponse = krakenClient.getRecentTrades(assetCode);
        tradesResponse.ifPresent(response -> {
            List<TradeDetails> tradeDetails = response.getResult().getTradeDetails(assetDetailCode);
            tradeDetails.subList(tradeDetails.size() - timeframeSize - 1, tradeDetails.size() - 1).stream()
                    .sorted(Comparator.comparing(TradeDetails::getTime))
                    .forEach(detail -> {
                        Tick<BigDecimal> ticker = new Tick<>(detail.getTime(), detail.getPrice());
                        timeframe.addTick(ticker);
                    });
        });


        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        MovingAverageCrossoverStrategy movingAverageCrossoverStrategy = new MovingAverageCrossoverStrategy(50, 100);
        scheduler.scheduleAtFixedRate(() -> {
            BigDecimal currentPrice = krakenClient.getTickerInfo(assetCode).get().getResult().get(assetDetailCode).getCurrentPrice();
            boolean isPriceChanged = timeframe.getTicks().isEmpty() || !timeframe.getTicks().getLast().getValue().equals(currentPrice);
            if (isPriceChanged) {
                timeframe.addTick(currentPrice);
                System.out.print("    Current price: " + currentPrice + "\n");

                if (timeframe.isFull()) {
                    System.out.println(movingAverageCrossoverStrategy.strategy().apply(timeframe));
                } else {
                    System.out.println("Preparing timeframe...");
                }
            }
        }, 2, 1, TimeUnit.SECONDS);
    }
}
