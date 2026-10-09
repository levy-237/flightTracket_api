package levan.flightdetector.service;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClient;

import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class FlightServiceTimeoutTests {
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void timesOutOnStalledHeadersOrBodyAndCanFetchAgain(boolean sendHeaders) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        var executor = Executors.newCachedThreadPool();
        CountDownLatch releaseStalledRequest = new CountDownLatch(1);
        AtomicInteger requests = new AtomicInteger();
        byte[] body = "{\"ac\":[]}".getBytes(StandardCharsets.UTF_8);
        server.setExecutor(executor);
        server.createContext("/v2/point/48.1103/16.5697/175", exchange -> {
            try (exchange) {
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                if (requests.incrementAndGet() == 1) {
                    if (sendHeaders) {
                        exchange.sendResponseHeaders(200, body.length);
                        exchange.getResponseBody().write(body, 0, 1);
                        exchange.getResponseBody().flush();
                    }
                    try {
                        releaseStalledRequest.await(10, TimeUnit.SECONDS);
                    } catch (InterruptedException exception) {
                        Thread.currentThread().interrupt();
                    }
                    return;
                }
                exchange.sendResponseHeaders(200, body.length);
                exchange.getResponseBody().write(body);
            }
        });
        server.start();
        try {
            FlightService service = new FlightService(RestClient.builder(),
                    "http://127.0.0.1:" + server.getAddress().getPort(), 1000, 250);

            assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
                var failure = assertThrows(RestClientException.class,
                        () -> service.getNearbyFlights(48.1103, 16.5697, 175));
                Throwable rootCause = failure;
                while (rootCause.getCause() != null) {
                    rootCause = rootCause.getCause();
                }
                assertInstanceOf(SocketTimeoutException.class, rootCause);
                assertTrue(service.getNearbyFlights(48.1103, 16.5697, 175).ac().isEmpty());
            });
        } finally {
            releaseStalledRequest.countDown();
            server.stop(0);
            executor.shutdownNow();
        }
    }
}
