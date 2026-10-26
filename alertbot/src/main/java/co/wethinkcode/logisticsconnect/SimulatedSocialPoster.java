package co.wethinkcode.logisticsconnect;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Simulated social media post: logged and kept in a list, no real network call. */
public class SimulatedSocialPoster implements AlertPoster {

    private static final Logger log = LoggerFactory.getLogger(SimulatedSocialPoster.class);

    private final List<String> posts = new CopyOnWriteArrayList<>();

    @Override
    public void post(String text) {
        posts.add(text);
        log.info("[SIMULATED SOCIAL POST] {}", text);
    }

    public List<String> posts() {
        return List.copyOf(posts);
    }
}
