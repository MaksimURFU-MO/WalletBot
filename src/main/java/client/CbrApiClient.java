

package client;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class CbrApiClient implements RateSource {

    public static final URI DEFAULT_URI = URI.create("https://www.cbr-xml-daily.ru/daily_json.js");
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final HttpClient httpClient;
    private final URI uri;

    public CbrApiClient() {
        this(HttpClient.newBuilder().connectTimeout(TIMEOUT).build(), DEFAULT_URI);
    }

    public CbrApiClient(HttpClient httpClient, URI uri) {
        this.httpClient = httpClient;
        this.uri = uri;
    }

    @Override
    public String fetch() throws IOException {
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(TIMEOUT)
                .GET()
                .build();
        try {
            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                throw new IOException("ЦБ вернул статус " + response.statusCode());
            }
            return response.body();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Запрос к ЦБ прерван", e);
        }
    }
}