package co.wethinkcode.logisticsconnect;

import static org.junit.jupiter.api.Assertions.*;

import io.javalin.Javalin;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class HubServiceAppTest {

    private final List<HubRecord> hubs = List.of(
            new HubRecord("H-500", "Gauteng", "Johannesburg Central", true, List.of("H-504")),
            new HubRecord("H-501", "Western Cape", "Cape Town Port", false, List.of()));

    private Javalin app;

    @AfterEach
    void stop() {
        if (app != null) {
            app.stop();
        }
    }

    private HttpResponse<String> get(HubSource source, String path) throws Exception {
        app = HubServiceApp.createApp(source).start(0);
        HttpRequest request = HttpRequest.newBuilder(
                URI.create("http://localhost:" + app.port() + path)).GET().build();
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void healthIsOk() throws Exception {
        HttpResponse<String> r = get(() -> hubs, "/health");
        assertEquals(200, r.statusCode());
        assertEquals("OK", r.body());
    }

    @Test
    void provincesAreListed() throws Exception {
        HttpResponse<String> r = get(() -> hubs, "/provinces");
        assertEquals(200, r.statusCode());
        assertTrue(r.body().contains("Gauteng"));
        assertTrue(r.body().contains("Western Cape"));
    }

    @Test
    void sortingCentersForProvince() throws Exception {
        HttpResponse<String> r = get(() -> hubs, "/provinces/Western%20Cape/sorting-centers");
        assertEquals(200, r.statusCode());
        assertTrue(r.body().contains("Cape Town Port"));
    }

    @Test
    void unknownProvinceIs404() throws Exception {
        assertEquals(404, get(() -> hubs, "/provinces/Narnia/sorting-centers").statusCode());
    }

    @Test
    void aliasResolvesToCanonicalHub() throws Exception {
        HttpResponse<String> r = get(() -> hubs, "/hubs/H-504");
        assertEquals(200, r.statusCode());
        assertTrue(r.body().contains("\"hubId\":\"H-500\""));
    }

    @Test
    void unknownHubIs404() throws Exception {
        assertEquals(404, get(() -> hubs, "/hubs/H-999").statusCode());
    }

    @Test
    void upstreamDownIs503() throws Exception {
        HubSource down = () -> {
            throw new SourceUnavailableException("ingestion-service unreachable");
        };
        HttpResponse<String> r = get(down, "/provinces");
        assertEquals(503, r.statusCode());
        assertTrue(r.body().contains("unreachable"));
    }
}
