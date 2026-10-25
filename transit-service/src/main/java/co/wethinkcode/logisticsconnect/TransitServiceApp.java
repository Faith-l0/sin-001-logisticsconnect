package co.wethinkcode.logisticsconnect;

import io.javalin.Javalin;
import io.javalin.http.ConflictResponse;
import io.javalin.http.NotFoundResponse;
import java.util.Locale;
import java.util.Map;

public class TransitServiceApp {

    static final String HUB_SERVICE_URL = "http://localhost:7051";
    static final String STAGE_SERVICE_URL = "http://localhost:7052";

    public static void main(String[] args) {
        createApp(new HttpHubLookup(HUB_SERVICE_URL), new HttpStageLookup(STAGE_SERVICE_URL)).start(7053);
    }

    /** Builds the app without starting it, so tests can pick their own port. */
    static Javalin createApp(HubLookup hubs, StageLookup stages) {
        Javalin app = Javalin.create();

        app.get("/health", ctx -> ctx.result("OK"));

        app.get("/eta/{hubId}", ctx -> {
            String id = ctx.pathParam("hubId").strip().toUpperCase(Locale.ROOT);
            HubInfo hub = hubs.find(id).orElseThrow(() -> new NotFoundResponse("Unknown hub: " + id));
            if (!hub.active()) {
                throw new ConflictResponse("Hub " + hub.hubId() + " is inactive, no ETA available");
            }
            int stage = stages.stageFor(hub.hubId());
            EtaWindow window = EtaCalculator.window(stage);
            ctx.json(Map.of(
                    "hubId", hub.hubId(),
                    "stage", stage,
                    "minHours", window.minHours(),
                    "maxHours", window.maxHours()));
        });

        // Partial failure: say which dependency is down instead of crashing.
        app.exception(DependencyUnavailableException.class,
                (e, ctx) -> ctx.status(503).json(Map.of("error", e.getMessage())));

        return app;
    }
}
