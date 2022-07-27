import com.google.inject.Inject;

import javax.annotation.Nullable;
import java.net.http.HttpClient;
import java.util.Optional;

public class DogFactsClient extends AbstractClient {

    @Inject
    public DogFactsClient(HttpClient httpClient) {
        super(httpClient);
    }

    @Override
    String getURI() {
        return "https://dog-api.kinduff.com%s";
    }

    public Optional<FactsResponse> getFacts(@Nullable Integer size) {
        return getRequest(String.format("/api/facts?number=%s", Optional.ofNullable(size).orElse(1)))
                .flatMap(request -> send(request, FactsResponse.class));
    }


}
