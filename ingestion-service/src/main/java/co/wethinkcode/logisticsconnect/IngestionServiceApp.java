package co.wethinkcode.logisticsconnect;

import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

public class IngestionServiceApp {

    private static final Logger log = LoggerFactory.getLogger(IngestionServiceApp.class);
    private static final int PORT = 7050;

    public static void main(String[] args) {
        List<HubRecord> raw = HubLoader.loadFromResource("hubs-global.csv");
        List<HubRecord> hubs = HubDeduplicator.deduplicate(raw);
        log.info("Loaded {} rows, serving {} hubs after deduplication", raw.size(), hubs.size());

        createApp(hubs).start(PORT);
    }

    /** Builds the app without starting it, so tests can start it on any free port. */
    static Javalin createApp(List<HubRecord> hubs) {
        Javalin app = Javalin.create();
        app.get("/health", ctx -> ctx.result("OK"));
        app.get("/hubs", ctx -> ctx.json(hubs));
        return app;
    }
}
        // TODO: read and clean src/main/resources/hubs-global.csv (hubs, sorting centers, regional districts data —
        // trim whitespace, fix casing, normalize dates/booleans) and expose the
        // cleaned records here for the other services to consume.

