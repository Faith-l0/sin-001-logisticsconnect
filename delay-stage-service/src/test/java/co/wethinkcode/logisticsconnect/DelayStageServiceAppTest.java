package co.wethinkcode.logisticsconnect;

import static org.junit.jupiter.api.Assertions.*;

import io.javalin.Javalin;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class DelayStageServiceAppTest {

    // H-504 is an alias of H-500; H-999 does not exist.
    private final HubDirectory directory = id -> switch (id) {
        case "H-500", "H-504" -> Optional.of("H-500");
        case "H-501" -> Optional.of("H-501");
        default -> Optional.empty();
    };

    private final StageStore store = new StageStore();
    private Javalin app;

    @AfterEach
    void stop() {
        if (app != null) {
            app.stop();
        }
    }

    private HttpResponse<String> call(HubDirectory dir, String method, String path, String body)
            throws Exception {
        if (app == null) {
            app = DelayStageServiceApp.createApp(dir, store).start(0);
        }
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + app.port() + path))
                .header("Content-Type", "application/json")
                .method(method, HttpRequest.BodyPublishers.ofString(body))
                .build();
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void healthIsOk() throws Exception {
        HttpResponse<String> r = call(directory, "GET", "/health", "");
        assertEquals(200, r.statusCode());
        assertEquals("OK", r.body());
    }

    @Test
    void newHubIsAtStageZero() throws Exception {
        HttpResponse<String> r = call(directory, "GET", "/stage/H-500", "");
        assertEquals(200, r.statusCode());
        assertTrue(r.body().contains("\"stage\":0"));
    }

    @Test
    void postThenGetReturnsNewStage() throws Exception {
        assertEquals(200, call(directory, "POST", "/stage/H-500", "{\"stage\":3}").statusCode());
        HttpResponse<String> r = call(directory, "GET", "/stage/H-500", "");
        assertTrue(r.body().contains("\"stage\":3"));
    }

    @Test
    void aliasAndCanonicalIdShareOneStage() throws Exception {
        call(directory, "POST", "/stage/h-504", "{\"stage\":5}");
        HttpResponse<String> r = call(directory, "GET", "/stage/H-500", "");
        assertTrue(r.body().contains("\"stage\":5"));
        assertTrue(r.body().contains("\"hubId\":\"H-500\""));
    }

    @Test
    void stageOutOfRangeIs400() throws Exception {
        assertEquals(400, call(directory, "POST", "/stage/H-500", "{\"stage\":9}").statusCode());
    }

    @Test
    void malformedBodyIs400() throws Exception {
        assertEquals(400, call(directory, "POST", "/stage/H-500", "nope").statusCode());
    }

    @Test
    void stageAsStringIs400() throws Exception {
        assertEquals(400, call(directory, "POST", "/stage/H-500", "{\"stage\":\"3\"}").statusCode());
    }

    @Test
    void unknownHubIs404() throws Exception {
        assertEquals(404, call(directory, "GET", "/stage/H-999", "").statusCode());
        assertEquals(404, call(directory, "POST", "/stage/H-999", "{\"stage\":1}").statusCode());
    }

    @Test
    void hubServiceDownIs503() throws Exception {
        HubDirectory down = id -> {
            throw new HubServiceUnavailableException("hub-service unreachable");
        };
        HttpResponse<String> r = call(down, "GET", "/stage/H-500", "");
        assertEquals(503, r.statusCode());
        assertTrue(r.body().contains("unreachable"));
    }
}
