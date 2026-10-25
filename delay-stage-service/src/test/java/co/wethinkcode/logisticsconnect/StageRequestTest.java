package co.wethinkcode.logisticsconnect;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class StageRequestTest {

    @Test
    void parsesAValidStage() {
        assertEquals(3, StageRequest.parseStage("{\"stage\": 3}"));
    }

    @Test
    void parsesZero() {
        assertEquals(0, StageRequest.parseStage("{\"stage\":0}"));
    }

    @Test
    void rejectsMissingField() {
        assertThrows(IllegalArgumentException.class, () -> StageRequest.parseStage("{}"));
    }

    @Test
    void rejectsStageGivenAsString() {
        assertThrows(IllegalArgumentException.class, () -> StageRequest.parseStage("{\"stage\":\"3\"}"));
    }

    @Test
    void rejectsDecimalStage() {
        assertThrows(IllegalArgumentException.class, () -> StageRequest.parseStage("{\"stage\":3.5}"));
    }

    @Test
    void rejectsInvalidJson() {
        assertThrows(IllegalArgumentException.class, () -> StageRequest.parseStage("not json"));
    }

    @Test
    void rejectsEmptyBody() {
        assertThrows(IllegalArgumentException.class, () -> StageRequest.parseStage(""));
    }

    @Test
    void rejectsJsonArray() {
        assertThrows(IllegalArgumentException.class, () -> StageRequest.parseStage("[3]"));
    }
}
