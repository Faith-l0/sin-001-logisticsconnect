package co.wethinkcode.logisticsconnect;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** In-memory delay stage per hub. Stage 0 means no delay. */
public class StageStore {

    public static final int MIN_STAGE = 0;
    public static final int MAX_STAGE = 8;

    private final Map<String, Integer> stages = new ConcurrentHashMap<>();

    public int get(String hubId) {
        return stages.getOrDefault(hubId, MIN_STAGE);
    }

    public void set(String hubId, int stage) {
        if (stage < MIN_STAGE || stage > MAX_STAGE) {
            throw new IllegalArgumentException(
                    "Stage must be between " + MIN_STAGE + " and " + MAX_STAGE + ", got " + stage);
        }
        stages.put(hubId, stage);
    }
}
