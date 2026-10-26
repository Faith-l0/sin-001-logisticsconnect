package co.wethinkcode.logisticsconnect;

import static org.junit.jupiter.api.Assertions.*;

import io.javalin.Javalin;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class AlertBotAppTest {

    private final SimulatedSocialPoster poster = new SimulatedSocialPoster();
    private Javalin app;

    @AfterEach
    void stop() {
        if (app != null) {
            app.stop();
        }
    }

    private HttpResponse<String> get(String path) throws Exception {
        app = AlertBotApp.createApp(poster).start(0);
        HttpRequest request = HttpRequest.newBuilder(
                URI.create("http://localhost:" + app.port() + path)).GET().build();
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void healthIsOk() throws Exception {
        HttpResponse<String> r = get("/health");
        assertEquals(200, r.statusCode());
        assertEquals("OK", r.body());
    }

    @Test
    void alertsStartEmpty() throws Exception {
        assertEquals("[]", get("/alerts").body());
    }

    @Test
    void postedAlertsAreListed() throws Exception {
        poster.post("Delay alert: hub H-500 is now at delay stage 7.");
        HttpResponse<String> r = get("/alerts");
        assertEquals(200, r.statusCode());
        assertTrue(r.body().contains("H-500"));
    }
}
