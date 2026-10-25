package co.wethinkcode.logisticsconnect;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Latest known delay stage per hub, fed by messages. Unknown hubs are at stage 0 (no delay). */
public class StageCache implements StageLookup {

    private final Map<String, Integer> stages = new ConcurrentHashMap<>();

    @Override
    public int stageFor(String hubId) {
        return stages.getOrDefault(hubId, 0);
    }

    public void update(StageUpdate update) {
        stages.put(update.hubId(), update.stage());
    }
}
