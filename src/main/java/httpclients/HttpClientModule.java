package httpclients;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import httpclients.kraken.deserializers.RecentTradesDeserializer;
import httpclients.kraken.response.trades.Trades;

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

        return gsonBuilder.create();
    }

}
