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

class TransitServiceAppTest {

    // H-504 is an alias of H-500, H-507 is inactive, H-999 does not exist.
    private final HubLookup hubs = id -> switch (id) {
        case "H-500", "H-504" -> Optional.of(new HubInfo("H-500", true));
        case "H-507" -> Optional.of(new HubInfo("H-507", false));
        default -> Optional.empty();
    };

    private final StageLookup stageTwo = id -> 2;

    private Javalin app;

    @AfterEach
    void stop() {
        if (app != null) {
            app.stop();
        }
    }

    private HttpResponse<String> get(HubLookup h, StageLookup s, String path) throws Exception {
        app = TransitServiceApp.createApp(h, s).start(0);
        HttpRequest request = HttpRequest.newBuilder(
                URI.create("http://localhost:" + app.port() + path)).GET().build();
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void healthIsOk() throws Exception {
        HttpResponse<String> r = get(hubs, stageTwo, "/health");
        assertEquals(200, r.statusCode());
        assertEquals("OK", r.body());
    }

    @Test
    void etaReflectsTheDelayStage() throws Exception {
        HttpResponse<String> r = get(hubs, stageTwo, "/eta/H-500");
        assertEquals(200, r.statusCode());
        assertTrue(r.body().contains("\"stage\":2"));
        assertTrue(r.body().contains("\"minHours\":48"));
        assertTrue(r.body().contains("\"maxHours\":72"));
    }

    @Test
    void aliasIsReportedAsCanonicalHub() throws Exception {
        HttpResponse<String> r = get(hubs, stageTwo, "/eta/h-504");
        assertEquals(200, r.statusCode());
        assertTrue(r.body().contains("\"hubId\":\"H-500\""));
    }

    @Test
    void unknownHubIs404() throws Exception {
        assertEquals(404, get(hubs, stageTwo, "/eta/H-999").statusCode());
    }

    @Test
    void inactiveHubIs409() throws Exception {
        assertEquals(409, get(hubs, stageTwo, "/eta/H-507").statusCode());
    }

    @Test
    void hubServiceDownIs503AndNamesIt() throws Exception {
        HubLookup down = id -> {
            throw new DependencyUnavailableException("hub-service unreachable");
        };
        HttpResponse<String> r = get(down, stageTwo, "/eta/H-500");
        assertEquals(503, r.statusCode());
        assertTrue(r.body().contains("hub-service"));
    }

    @Test
    void stageServiceDownIs503AndNamesIt() throws Exception {
        StageLookup down = id -> {
            throw new DependencyUnavailableException("delay-stage-service unreachable");
        };
        HttpResponse<String> r = get(hubs, down, "/eta/H-500");
        assertEquals(503, r.statusCode());
        assertTrue(r.body().contains("delay-stage-service"));
    }
}
