package client;

import com.sun.net.httpserver.HttpServer;
import model.Currency;
import model.Rate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.CurrencyException;
import service.CurrencyException.Reason;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты клиента на локальном HTTP-сервере из JDK: интернет и настоящий ЦБ не нужны.
 */
class CbrApiClientTest {

    private static final String SAMPLE_JSON = """
            {
              "Date": "2026-11-16T11:30:00+03:00",
              "PreviousDate": "2026-11-15T11:30:00+03:00",
              "PreviousURL": "//www.cbr-xml-daily.ru/archive/2026/11/15/daily_json.js",
              "Timestamp": "2026-11-16T18:00:00+03:00",
              "Valute": {
                "USD": {
                  "ID": "R01235",
                  "NumCode": "840",
                  "CharCode": "USD",
                  "Nominal": 1,
                  "Name": "Доллар США",
                  "Value": 90.5,
                  "Previous": 90.0
                }
              }
            }
            """;

    private HttpServer server;
    private final AtomicReference<String> lastMethod = new AtomicReference<>();
    private final AtomicReference<String> lastPath = new AtomicReference<>();

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    private void respond(int status, String body, String contentType) {
        server.createContext("/daily_json.js", exchange -> {
            lastMethod.set(exchange.getRequestMethod());
            lastPath.set(exchange.getRequestURI().getPath());
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            if (contentType != null) {
                exchange.getResponseHeaders().set("Content-Type", contentType);
            }
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(bytes);
            }
        });
    }

    private CbrApiClient client() {
        URI uri = URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/daily_json.js");
        return new CbrApiClient(HttpClient.newHttpClient(), uri);
    }

    @Test
    void returnsParsedRatesOfSuccessfulResponse() throws CurrencyException {
        respond(200, SAMPLE_JSON, "application/json");

        Map<Currency, Rate> rates = client().fetch();

        assertTrue(rates.containsKey(Currency.USD));
        Rate usd = rates.get(Currency.USD);
        assertEquals(1, usd.nominal());
        assertEquals(new BigDecimal("90.5"), usd.value());
    }

    @Test
    void decodesCyrillicAsUtf8() throws CurrencyException {
        respond(200, SAMPLE_JSON, null);

        Map<Currency, Rate> rates = client().fetch();

        assertEquals("Доллар США", rates.get(Currency.USD).name());
    }

    @Test
    void sendsGetRequestToConfiguredPath() throws CurrencyException {
        respond(200, SAMPLE_JSON, null);

        client().fetch();

        assertEquals("GET", lastMethod.get());
        assertEquals("/daily_json.js", lastPath.get());
    }

    @Test
    void notFoundIsReportedAsCurrencyExceptionWithStatus() {
        respond(404, "нет такой страницы", null);

        CurrencyException e = assertThrows(CurrencyException.class, () -> client().fetch());

        assertEquals(Reason.SOURCE_UNAVAILABLE, e.getReason());
        assertTrue(e.getMessage().contains("404"));
    }

    @Test
    void serverErrorIsReportedAsCurrencyExceptionWithStatus() {
        respond(500, "ошибка", null);

        CurrencyException e = assertThrows(CurrencyException.class, () -> client().fetch());

        assertEquals(Reason.SOURCE_UNAVAILABLE, e.getReason());
        assertTrue(e.getMessage().contains("500"));
    }

    @Test
    void redirectIsNotFollowedAndIsAnError() {
        server.createContext("/daily_json.js", exchange -> {
            exchange.getResponseHeaders().set("Location", "http://127.0.0.1:1/");
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
        });

        CurrencyException e = assertThrows(CurrencyException.class, () -> client().fetch());

        assertEquals(Reason.SOURCE_UNAVAILABLE, e.getReason());
        assertTrue(e.getMessage().contains("302"));
    }

    @Test
    void unreachableServerIsCurrencyException() throws IOException {
        int freePort;
        try (ServerSocket socket = new ServerSocket(0)) {
            freePort = socket.getLocalPort();
        }
        URI uri = URI.create("http://127.0.0.1:" + freePort + "/daily_json.js");
        CbrApiClient unreachable = new CbrApiClient(HttpClient.newHttpClient(), uri);

        CurrencyException e = assertThrows(CurrencyException.class, unreachable::fetch);
        assertEquals(Reason.SOURCE_UNAVAILABLE, e.getReason());
    }

    @Test
    void interruptedThreadGetsCurrencyExceptionAndKeepsInterruptFlag() {
        respond(200, SAMPLE_JSON, null);
        Thread.currentThread().interrupt();
        try {
            CurrencyException e = assertThrows(CurrencyException.class, () -> client().fetch());
            assertEquals(Reason.SOURCE_UNAVAILABLE, e.getReason());
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    void invalidJsonFromServerThrowsCurrencyException() {
        respond(200, "не json вообще", "application/json");

        assertThrows(CurrencyException.class, () -> client().fetch());
    }

    @Test
    void defaultAddressIsCbrMirror() {
        assertEquals("https://www.cbr-xml-daily.ru/daily_json.js", CbrApiClient.DEFAULT_URI.toString());
    }
}