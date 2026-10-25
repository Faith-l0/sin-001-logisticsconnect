package co.wethinkcode.logisticsconnect;

import io.javalin.Javalin;
import io.javalin.http.NotFoundResponse;
import java.util.Locale;
import java.util.Map;

public class DelayStageServiceApp {

    static final String HUB_SERVICE_URL = "http://localhost:7051";

    public static void main(String[] args) {
        createApp(new HttpHubDirectory(HUB_SERVICE_URL), new StageStore()).start(7052);
    }

    /** Builds the app without starting it, so tests can pick their own port. */
    static Javalin createApp(HubDirectory directory, StageStore store) {
        Javalin app = Javalin.create();

        app.get("/health", ctx -> ctx.result("OK"));

        app.get("/stage/{hubId}", ctx -> {
            String hubId = resolve(directory, ctx.pathParam("hubId"));
            ctx.json(Map.of("hubId", hubId, "stage", store.get(hubId)));
        });

        app.post("/stage/{hubId}", ctx -> {
            String hubId = resolve(directory, ctx.pathParam("hubId"));
            int stage = StageRequest.parseStage(ctx.body());
            store.set(hubId, stage);
            ctx.json(Map.of("hubId", hubId, "stage", stage));
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
