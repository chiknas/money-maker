import com.google.inject.Guice;
import com.google.inject.Injector;
import httpclients.HttpClientModule;
import httpclients.kraken.KrakenClient;
import httpclients.kraken.KrakenModule;

public class MoneyMakerApplication {
    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new HttpClientModule(), new KrakenModule());
        KrakenClient krakenClient = injector.getInstance(KrakenClient.class);
//        System.out.println(krakenClient.getTickerInfo("XBTGBP").get().getResult().get("XXBTZGBP").getAskArray());
        System.out.println(krakenClient.getBalance().get());
    }
}
