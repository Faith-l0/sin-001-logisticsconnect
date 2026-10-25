package co.wethinkcode.logisticsconnect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/** Parses the body of POST /stage/{hubId}, e.g. {"stage": 3}. */
public final class StageRequest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private StageRequest() {
    }

    public static int parseStage(String body) {
        JsonNode node;
        try {
            node = MAPPER.readTree(body);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Body must be valid JSON");
        }
        if (node == null || !node.isObject()) {
            throw new IllegalArgumentException("Body must be a JSON object like {\"stage\": 3}");
        }
        JsonNode stage = node.get("stage");
        if (stage == null || !stage.isInt()) {
            throw new IllegalArgumentException("\"stage\" must be a whole number");
        }
        return stage.intValue();
    }
}
