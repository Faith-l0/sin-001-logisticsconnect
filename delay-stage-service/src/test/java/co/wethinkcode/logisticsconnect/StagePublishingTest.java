package co.wethinkcode.logisticsconnect;

import static org.junit.jupiter.api.Assertions.*;

import io.javalin.Javalin;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class StagePublishingTest {

    // H-504 is an alias of H-500.
    private final HubDirectory directory = id -> Optional.of(id.equals("H-504") ? "H-500" : id);

    private final StageStore store = new StageStore();
    private final List<String> published = new ArrayList<>();
    private final StageChangePublisher recorder = (hubId, stage) -> published.add(hubId + ":" + stage);

    private Javalin app;

    @AfterEach
    void stop() {
        if (app != null) {
            app.stop();
        }
    }

    private HttpResponse<String> post(StageChangePublisher publisher, String hub, String body)
            throws Exception {
        if (app == null) {
            app = DelayStageServiceApp.createApp(directory, store, publisher).start(0);
        }
        HttpRequest request = HttpRequest.newBuilder(
                        URI.create("http://localhost:" + app.port() + "/stage/" + hub))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void publishesWhenStageChanges() throws Exception {
        post(recorder, "H-500", "{\"stage\":3}");
        assertEquals(List.of("H-500:3"), published);
    }

    @Test
    void aliasIsPublishedUnderTheCanonicalId() throws Exception {
        post(recorder, "h-504", "{\"stage\":2}");
        assertEquals(List.of("H-500:2"), published);
    }

    @Test
    void doesNotPublishWhenStageIsUnchanged() throws Exception {
        post(recorder, "H-500", "{\"stage\":3}");
        HttpResponse<String> second = post(recorder, "H-500", "{\"stage\":3}");
        assertEquals(1, published.size());
        assertTrue(second.body().contains("\"changed\":false"));
    }

    @Test
    void doesNotPublishAnInvalidStage() throws Exception {
        HttpResponse<String> r = post(recorder, "H-500", "{\"stage\":9}");
        assertEquals(400, r.statusCode());
        assertTrue(published.isEmpty());
    }

    @Test
    void brokerDownStillSavesTheStageAndSaysSo() throws Exception {
        StageChangePublisher down = (hubId, stage) -> {
            throw new PublishFailedException("broker unreachable");
        };
        HttpResponse<String> r = post(down, "H-500", "{\"stage\":3}");
        assertEquals(200, r.statusCode());
        assertTrue(r.body().contains("\"published\":false"));
        assertEquals(3, store.get("H-500"));
    }
}
