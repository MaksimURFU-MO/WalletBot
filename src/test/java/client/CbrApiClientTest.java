package client;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты клиента на локальном HTTP-сервере из JDK: интернет и настоящий ЦБ не нужны.
 */
class CbrApiClientTest {

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
    void returnsBodyOfSuccessfulResponse() throws IOException {
        respond(200, "{\"Valute\": {}}", "application/json");

        assertEquals("{\"Valute\": {}}", client().fetch());
    }

    @Test
    void decodesCyrillicAsUtf8() throws IOException {
        respond(200, "Доллар США", null);

        assertEquals("Доллар США", client().fetch());
    }

    @Test
    void sendsGetRequestToConfiguredPath() throws IOException {
        respond(200, "ok", null);

        client().fetch();

        assertEquals("GET", lastMethod.get());
        assertEquals("/daily_json.js", lastPath.get());
    }

    @Test
    void notFoundIsReportedAsIOExceptionWithStatus() {
        respond(404, "нет такой страницы", null);

        IOException e = assertThrows(IOException.class, () -> client().fetch());

        assertTrue(e.getMessage().contains("404"));
    }

    @Test
    void serverErrorIsReportedAsIOExceptionWithStatus() {
        respond(500, "ошибка", null);

        IOException e = assertThrows(IOException.class, () -> client().fetch());

        assertTrue(e.getMessage().contains("500"));
    }

    @Test
    void redirectIsNotFollowedAndIsAnError() {
        server.createContext("/daily_json.js", exchange -> {
            exchange.getResponseHeaders().set("Location", "http://127.0.0.1:1/");
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
        });

        IOException e = assertThrows(IOException.class, () -> client().fetch());

        assertTrue(e.getMessage().contains("302"));
    }

    @Test
    void unreachableServerIsIOException() throws IOException {
        int freePort;
        try (ServerSocket socket = new ServerSocket(0)) {
            freePort = socket.getLocalPort();
        }
        URI uri = URI.create("http://127.0.0.1:" + freePort + "/daily_json.js");
        CbrApiClient unreachable = new CbrApiClient(HttpClient.newHttpClient(), uri);

        assertThrows(IOException.class, unreachable::fetch);
    }

    @Test
    void interruptedThreadGetsIOExceptionAndKeepsInterruptFlag() {
        respond(200, "ok", null);
        Thread.currentThread().interrupt();
        try {
            assertThrows(IOException.class, () -> client().fetch());
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted(); // сбросить флаг, чтобы не сломать другие тесты
        }
    }

    @Test
    void defaultAddressIsCbrMirror() {
        assertEquals("https://www.cbr-xml-daily.ru/daily_json.js", CbrApiClient.DEFAULT_URI.toString());
    }
}
