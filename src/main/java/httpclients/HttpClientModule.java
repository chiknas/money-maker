package httpclients;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import java.net.http.HttpClient;

public class HttpClientModule extends AbstractModule {

    @Provides
    public HttpClient provideHttpClient() {
        return HttpClient.newHttpClient();
    }

}
