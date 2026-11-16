package client;

import model.Currency;
import model.Rate;
import service.CurrencyException;
import service.CurrencyException.Reason;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

public class CbrApiClient implements RateSource {

    public static final URI DEFAULT_URI = URI.create("https://www.cbr-xml-daily.ru/daily_json.js");
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final HttpClient httpClient;
    private final URI uri;
    private final RatesParser parser;

    public CbrApiClient() {
        this(
                HttpClient.newBuilder().connectTimeout(TIMEOUT).build(),
                DEFAULT_URI,
                new CbrJsonParser()
        );
    }

    public CbrApiClient(HttpClient httpClient, URI uri) {
        this(httpClient, uri, new CbrJsonParser());
    }

    public CbrApiClient(HttpClient httpClient, URI uri, RatesParser parser) {
        this.httpClient = httpClient;
        this.uri = uri;
        this.parser = parser;
    }

    @Override
    public Map<Currency, Rate> fetch() throws CurrencyException {
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(TIMEOUT)
                .GET()
                .build();
        try {
            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                throw new CurrencyException(
                        Reason.SOURCE_UNAVAILABLE,
                        "ЦБ вернул статус " + response.statusCode()
                );
            }
            return parser.parse(response.body());
        } catch (IOException e) {
            throw new CurrencyException(Reason.SOURCE_UNAVAILABLE, "Ошибка сети при обращении к ЦБ", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CurrencyException(Reason.SOURCE_UNAVAILABLE, "Запрос к ЦБ прерван", e);
        }
    }
}