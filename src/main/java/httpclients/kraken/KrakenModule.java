package httpclients.kraken;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

public class KrakenModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(String.class)
                .annotatedWith(Names.named("ApiPublicKey"))
                .toInstance(System.getenv("ApiPublicKey"));

        bind(String.class)
                .annotatedWith(Names.named("ApiPrivateKey"))
                .toInstance(System.getenv("ApiPrivateKey"));
    }
}
