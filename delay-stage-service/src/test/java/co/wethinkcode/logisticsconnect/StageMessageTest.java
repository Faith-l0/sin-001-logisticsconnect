package co.wethinkcode.logisticsconnect;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class StageMessageTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void jsonCarriesHubIdAndStage() throws Exception {
        JsonNode node = mapper.readTree(StageMessage.toJson("H-500", 3));
        assertEquals("H-500", node.get("hubId").asText());
        assertEquals(3, node.get("stage").asInt());
    }

    @Test
    void jsonHasExactlyTwoFields() throws Exception {
        assertEquals(2, mapper.readTree(StageMessage.toJson("H-500", 3)).size());
    }
}
