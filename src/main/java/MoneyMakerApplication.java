import com.google.inject.Guice;
import com.google.inject.Injector;
import httpclients.HttpClientModule;
import httpclients.kraken.KrakenClient;
import httpclients.kraken.KrakenModule;
import trading.timeframe.Tick;
import trading.timeframe.Timeframe;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MoneyMakerApplication {
    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new HttpClientModule(), new KrakenModule());
        KrakenClient krakenClient = injector.getInstance(KrakenClient.class);
//        System.out.println(krakenClient.getTickerInfo("XBTGBP").get().getResult().get("XXBTZGBP").getAskArray());
//        System.out.println(krakenClient.getBalance().get());

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        Timeframe<BigDecimal> timeframe = new Timeframe<>(5);
        scheduler.scheduleAtFixedRate(() -> {
            String s = krakenClient.getTickerInfo("XBTGBP").get().getResult().get("XXBTZGBP").getAskArray().get(0);
            LinkedList<Tick<BigDecimal>> timeFrame = timeframe.addTick(new BigDecimal(s));
            System.out.println(timeFrame.stream().map(tick -> tick.getTime() + "  " + tick.getValue()).collect(Collectors.toList()));
        }, 2, 2, TimeUnit.SECONDS);
    }
}
