package co.wethinkcode.logisticsconnect;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Remembers each hub's last stage and posts when the alert rule says so. */
public class AlertBot {

    private final AlertPoster poster;
    private final Map<String, Integer> lastStage = new ConcurrentHashMap<>();

    public AlertBot(AlertPoster poster) {
        this.poster = poster;
    }

    public void onUpdate(StageUpdate update) {
        // A hub we have never heard about starts at stage 0, matching the other services.
        int previous = lastStage.getOrDefault(update.hubId(), 0);
        lastStage.put(update.hubId(), update.stage());

        switch (AlertPolicy.decide(previous, update.stage())) {
            case ALERT -> poster.post("Delay alert: hub " + update.hubId()
                    + " is now at delay stage " + update.stage()
                    + ". Expect longer delivery times.");
            case RECOVERED -> poster.post("Update: hub " + update.hubId()
                    + " is down to delay stage " + update.stage()
                    + ". Delays are easing.");
            case NONE -> { }
        }
    }
}
