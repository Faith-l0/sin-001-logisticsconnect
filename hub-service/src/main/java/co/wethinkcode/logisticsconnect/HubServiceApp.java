package co.wethinkcode.logisticsconnect;

import io.javalin.Javalin;
import java.util.List;
import java.util.Map;

public class HubServiceApp {

    static final String INGESTION_URL = "http://localhost:7050/hubs";

    public static void main(String[] args) {
        createApp(new HttpHubSource(INGESTION_URL)).start(7051);
    }

    /** Builds the app without starting it, so tests can pick their own port. */
    static Javalin createApp(HubSource source) {
        Javalin app = Javalin.create();

        app.get("/health", ctx -> ctx.result("OK"));

        app.get("/provinces", ctx -> ctx.json(HubCatalog.provinces(source.fetchHubs())));

        app.get("/provinces/{province}/sorting-centers", ctx -> {
            String province = ctx.pathParam("province");
            List<String> centers = HubCatalog.sortingCenters(source.fetchHubs(), province);
            if (centers.isEmpty()) {
                ctx.status(404).json(Map.of("error", "Unknown province: " + province));
            } else {
                ctx.json(centers);
            }
        });

        app.get("/hubs/{id}", ctx -> {
            String id = ctx.pathParam("id");
            HubCatalog.findById(source.fetchHubs(), id).ifPresentOrElse(
                    ctx::json,
                    () -> ctx.status(404).json(Map.of("error", "Unknown hub: " + id)));
        });

        // Partial failure: report it clearly instead of letting the service crash.
        app.exception(SourceUnavailableException.class,
                (e, ctx) -> ctx.status(503).json(Map.of("error", e.getMessage())));

        return app;
    }
}
