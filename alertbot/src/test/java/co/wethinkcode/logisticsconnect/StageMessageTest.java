package co.wethinkcode.logisticsconnect;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class StageMessageTest {

    @Test
    void parsesAValidMessage() {
        StageUpdate update = StageMessage.parse("{\"hubId\":\"h-500\",\"stage\":7}");
        assertEquals("H-500", update.hubId());
        assertEquals(7, update.stage());
    }

    @Test
    void rejectsMalformedJson() {
        assertThrows(IllegalArgumentException.class, () -> StageMessage.parse("not json"));
    }

    @Test
    void rejectsMissingFields() {
        assertThrows(IllegalArgumentException.class, () -> StageMessage.parse("{\"stage\":3}"));
        assertThrows(IllegalArgumentException.class, () -> StageMessage.parse("{\"hubId\":\"H-500\"}"));
    }

    @Test
    void rejectsStageOutOfRange() {
        assertThrows(IllegalArgumentException.class,
                () -> StageMessage.parse("{\"hubId\":\"H-500\",\"stage\":9}"));
    }
}
