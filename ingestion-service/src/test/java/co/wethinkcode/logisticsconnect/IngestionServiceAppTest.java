package co.wethinkcode.logisticsconnect;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IngestionServiceAppTest {

    private Javalin app;
    private final HttpClient client = HttpClient.newHttpClient();

    @BeforeEach
    void start() {
        app = IngestionServiceApp.createApp(
                HubDeduplicator.deduplicate(HubLoader.loadFromResource("hubs-global.csv")));
        app.start(0); // port 0 means "any free port"
    }

    @AfterEach
    void stop() {
        app.stop();
    }

    @Test
    void healthReturnsOk() throws Exception {
        HttpResponse<String> response = get("/health");
        assertEquals(200, response.statusCode());
        assertEquals("OK", response.body());
    }

    @Test
    void hubsReturnsTenCleanedHubsAsJson() throws Exception {
        HttpResponse<String> response = get("/hubs");
        assertEquals(200, response.statusCode());
        assertTrue(response.headers().firstValue("Content-Type").orElse("").contains("json"));

        JsonNode hubs = new ObjectMapper().readTree(response.body());
        assertEquals(10, hubs.size());

        JsonNode johannesburg = null;
        for (JsonNode hub : hubs) {
            if (hub.get("hubId").asText().equals("H-500")) {
                johannesburg = hub;
            }
        }
        assertEquals("Gauteng", johannesburg.get("province").asText());
        assertEquals("Johannesburg Central", johannesburg.get("sortingCenter").asText());
        assertTrue(johannesburg.get("active").asBoolean());
        assertEquals(3, johannesburg.get("aliases").size());
    }

    private HttpResponse<String> get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(
                URI.create("http://localhost:" + app.port() + path)).build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
