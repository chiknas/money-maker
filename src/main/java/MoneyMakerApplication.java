import com.google.inject.Guice;
import com.google.inject.Injector;
import httpclients.HttpClientModule;
import httpclients.kraken.KrakenClient;
import httpclients.kraken.KrakenModule;
import trading.strategies.MovingAverageCrossoverStrategy;
import trading.timeframe.Timeframe;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MoneyMakerApplication {
    public static void main(String[] args) throws IOException, InterruptedException {
        new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();

        Injector injector = Guice.createInjector(new HttpClientModule(), new KrakenModule());
        KrakenClient krakenClient = injector.getInstance(KrakenClient.class);

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        Timeframe<BigDecimal> timeframe = new Timeframe<>(200);
        MovingAverageCrossoverStrategy movingAverageCrossoverStrategy = new MovingAverageCrossoverStrategy(50, 100);

        /**
         * Use this https://api.kraken.com/0/public/Trades?pair=XBTUSD
         * to initialize the timeframe before starting
         */

        scheduler.scheduleAtFixedRate(() -> {
            BigDecimal currentPrice = krakenClient.getTickerInfo("XBTUSD").get().getResult().get("XXBTZUSD").getCurrentPrice();
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
