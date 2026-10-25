package co.wethinkcode.logisticsconnect;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class StageMessageTest {

    @Test
    void parsesAValidMessage() {
        StageUpdate update = StageMessage.parse("{\"hubId\":\"H-500\",\"stage\":3}");
        assertEquals("H-500", update.hubId());
        assertEquals(3, update.stage());
    }

    @Test
    void normalisesTheHubId() {
        assertEquals("H-500", StageMessage.parse("{\"hubId\":\" h-500 \",\"stage\":1}").hubId());
    }

    @Test
    void rejectsMalformedJson() {
        assertThrows(IllegalArgumentException.class, () -> StageMessage.parse("not json"));
    }

    @Test
    void rejectsMissingHubId() {
        assertThrows(IllegalArgumentException.class, () -> StageMessage.parse("{\"stage\":3}"));
    }

    @Test
    void rejectsMissingStage() {
        assertThrows(IllegalArgumentException.class, () -> StageMessage.parse("{\"hubId\":\"H-500\"}"));
    }

    @Test
    void rejectsStageOutOfRange() {
        assertThrows(IllegalArgumentException.class,
                () -> StageMessage.parse("{\"hubId\":\"H-500\",\"stage\":9}"));
    }
}
