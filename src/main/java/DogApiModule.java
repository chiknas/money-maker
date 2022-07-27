import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import java.net.http.HttpClient;

public class DogApiModule extends AbstractModule {

    @Provides
    public HttpClient provideHttpClient() {
        return HttpClient.newHttpClient();
    }

}
