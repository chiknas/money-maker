package services.httpclients;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import services.httpclients.kraken.deserializers.BalanceDeserializer;
import services.httpclients.kraken.deserializers.OrderInfoResultDeserializer;
import services.httpclients.kraken.deserializers.RecentTradesDeserializer;
import services.httpclients.kraken.response.balance.BalanceResult;
import services.httpclients.kraken.response.orderinfo.OrderInfoResult;
import services.httpclients.kraken.response.trades.Trades;

import java.net.http.HttpClient;

public class HttpClientModule extends AbstractModule {

    @Provides
    public HttpClient provideHttpClient() {
        return HttpClient.newHttpClient();
    }

    @Provides
    public Gson provideGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();

        // custom serializers/deserializers
        gsonBuilder.registerTypeAdapter(Trades.class, new RecentTradesDeserializer());
        gsonBuilder.registerTypeAdapter(BalanceResult.class, new BalanceDeserializer());
        gsonBuilder.registerTypeAdapter(OrderInfoResult.class, new OrderInfoResultDeserializer());

        return gsonBuilder.create();
    }

}
