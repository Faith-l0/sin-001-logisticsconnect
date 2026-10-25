package co.wethinkcode.logisticsconnect;

import co.wethinkcode.logisticsconnect.mq.MqConfig;
import io.javalin.Javalin;
import io.javalin.http.NotFoundResponse;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DelayStageServiceApp {

    static final String HUB_SERVICE_URL = "http://localhost:7051";
    private static final Logger log = LoggerFactory.getLogger(DelayStageServiceApp.class);

    public static void main(String[] args) {
        createApp(new HttpHubDirectory(HUB_SERVICE_URL),
                new StageStore(),
                new ActiveMqStagePublisher(MqConfig.BROKER_URL, MqConfig.TOPIC)).start(7052);
    }

    /** For tests that do not care about publishing. */
    static Javalin createApp(HubDirectory directory, StageStore store) {
        return createApp(directory, store, (hubId, stage) -> { });
    }

    /** Builds the app without starting it, so tests can pick their own port. */
    static Javalin createApp(HubDirectory directory, StageStore store, StageChangePublisher publisher) {
        Javalin app = Javalin.create();

        app.get("/health", ctx -> ctx.result("OK"));

        app.get("/stage/{hubId}", ctx -> {
            String hubId = resolve(directory, ctx.pathParam("hubId"));
            ctx.json(Map.of("hubId", hubId, "stage", store.get(hubId)));
        });

        app.post("/stage/{hubId}", ctx -> {
            String hubId = resolve(directory, ctx.pathParam("hubId"));
            int stage = StageRequest.parseStage(ctx.body());
            int previous = store.get(hubId);
            store.set(hubId, stage);

            boolean changed = stage != previous;
            boolean published = false;
            if (changed) {
                try {
                    publisher.publish(hubId, stage);
                    published = true;
                } catch (PublishFailedException e) {
                    // The stage is saved either way; report the failed announcement instead of failing.
                    log.warn("Stage for {} saved but not published: {}", hubId, e.getMessage());
                }
            }
            ctx.json(Map.of("hubId", hubId, "stage", stage, "changed", changed, "published", published));
        });

        app.exception(IllegalArgumentException.class,
                (e, ctx) -> ctx.status(400).json(Map.of("error", e.getMessage())));
        app.exception(HubServiceUnavailableException.class,
                (e, ctx) -> ctx.status(503).json(Map.of("error", e.getMessage())));

        return app;
    }

    private static String resolve(HubDirectory directory, String rawId) {
        String id = rawId.strip().toUpperCase(Locale.ROOT);
        return directory.canonicalId(id)
                .orElseThrow(() -> new NotFoundResponse("Unknown hub: " + id));
    }
}
