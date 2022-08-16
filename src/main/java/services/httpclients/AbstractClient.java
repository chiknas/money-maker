package services.httpclients;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;

@Slf4j
public abstract class AbstractClient {

    private final HttpClient httpClient;
    private final Gson gson;

    public AbstractClient(HttpClient httpClient, Gson gson) {
        this.httpClient = httpClient;
        this.gson = gson;
    }

    protected abstract String getURI();

    /**
     * Generates a bare bones request builder with all the common stuff all requests might need.
     */
    private HttpRequest.Builder getRequestBuilder(String path, String... headers) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(getURI() + path));
        if (!List.of(headers).isEmpty()) {
            builder.headers(headers);
        }
        return builder;
    }

    /**
     * Generates a GET request for the specified path.
     */
    public Optional<HttpRequest> getRequest(String path, String... headers) {
        try {
            return Optional.of(getRequestBuilder(path, headers)
                    .GET()
                    .build());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Generates a POST request for the specified path.
     */
    public Optional<HttpRequest> postRequest(String data, String path, String... headers) {
        try {
            return Optional.of(getRequestBuilder(path, headers)
                    .POST(HttpRequest.BodyPublishers.ofString(data))
                    .build());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Send a request and returns the body of the response as string.
     */
    public Optional<String> send(HttpRequest request) {
        return send(request, String.class);
    }

    /**
     * Sends the provided request and returns the response body mapped to the specified class
     */
    public <T> Optional<T> send(HttpRequest request, Class<T> responseType) {
        try {
            String body = httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
            return Optional.of(String.class.equals(responseType) ? responseType.cast(body) : gson.fromJson(body, responseType));
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage(), e);
            return Optional.empty();
        }
    }

}
