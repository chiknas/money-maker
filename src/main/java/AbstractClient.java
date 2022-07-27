import com.google.gson.Gson;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

public abstract class AbstractClient {

    private final HttpClient httpClient;

    public AbstractClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    abstract String getURI();

    public Optional<HttpRequest> getRequest(String path) {
        try {
            return Optional.of(HttpRequest.newBuilder()
                    .uri(URI.create(String.format(getURI(), path)))
                    .GET()
                    .build());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public <T> Optional<T> send(HttpRequest request, Class<T> responseType) {
        try {
            String body = httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
            return Optional.of(new Gson().fromJson(body, responseType));
        } catch (IOException | InterruptedException e) {
            return Optional.empty();
        }
    }

}
