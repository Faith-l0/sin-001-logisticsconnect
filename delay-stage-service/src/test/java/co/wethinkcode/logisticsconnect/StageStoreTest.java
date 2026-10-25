package co.wethinkcode.logisticsconnect;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class StageStoreTest {

    private final StageStore store = new StageStore();

    @Test
    void unknownHubDefaultsToStageZero() {
        assertEquals(0, store.get("H-500"));
    }

    @Test
    void setThenGet() {
        store.set("H-500", 3);
        assertEquals(3, store.get("H-500"));
    }

    @Test
    void hubsAreIndependent() {
        store.set("H-500", 3);
        assertEquals(0, store.get("H-501"));
    }

    @Test
    void acceptsBoundaryStages() {
        store.set("H-500", 0);
        store.set("H-500", 8);
        assertEquals(8, store.get("H-500"));
    }

    @Test
    void rejectsStageBelowRange() {
        assertThrows(IllegalArgumentException.class, () -> store.set("H-500", -1));
    }

    @Test
    void rejectsStageAboveRange() {
        assertThrows(IllegalArgumentException.class, () -> store.set("H-500", 9));
    }

    @Test
    void rejectedStageLeavesOldValue() {
        store.set("H-500", 2);
        assertThrows(IllegalArgumentException.class, () -> store.set("H-500", 99));
        assertEquals(2, store.get("H-500"));
    }
}
