package co.wethinkcode.logisticsconnect;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class StageCacheTest {

    private final StageCache cache = new StageCache();

    @Test
    void unknownHubDefaultsToStageZero() {
        assertEquals(0, cache.stageFor("H-500"));
    }

    @Test
    void updateIsVisible() {
        cache.update(new StageUpdate("H-500", 4));
        assertEquals(4, cache.stageFor("H-500"));
    }

    @Test
    void latestUpdateWins() {
        cache.update(new StageUpdate("H-500", 4));
        cache.update(new StageUpdate("H-500", 1));
        assertEquals(1, cache.stageFor("H-500"));
    }

    @Test
    void hubsAreIndependent() {
        cache.update(new StageUpdate("H-500", 4));
        assertEquals(0, cache.stageFor("H-501"));
    }
}
