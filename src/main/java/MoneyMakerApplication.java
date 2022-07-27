import com.google.inject.Guice;
import com.google.inject.Injector;

public class MoneyMakerApplication {
    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new DogApiModule());
        DogFactsClient dogFactsClient = injector.getInstance(DogFactsClient.class);
        System.out.println(dogFactsClient.getFacts(5).map(FactsResponse::getFacts).orElseThrow());
    }
}
