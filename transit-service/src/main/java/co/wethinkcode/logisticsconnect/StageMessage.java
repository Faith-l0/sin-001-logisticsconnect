package co.wethinkcode.logisticsconnect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Locale;

/** Parses the JSON published by delay-stage-service, e.g. {"hubId":"H-500","stage":3}. */
public final class StageMessage {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private StageMessage() {
    }

    public static StageUpdate parse(String json) {
        if (json == null) {
            throw new IllegalArgumentException("message has no body");
        }
        JsonNode node;
        try {
            node = MAPPER.readTree(json);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("message is not valid JSON");
        }
        if (node == null || !node.isObject()) {
            throw new IllegalArgumentException("message must be a JSON object");
        }
        JsonNode hubId = node.get("hubId");
        JsonNode stage = node.get("stage");
        if (hubId == null || !hubId.isTextual() || hubId.asText().isBlank()) {
            throw new IllegalArgumentException("message is missing a hubId");
        }
        if (stage == null || !stage.isInt()) {
            throw new IllegalArgumentException("message is missing a whole-number stage");
        }
        int value = stage.intValue();
        if (value < 0 || value > EtaCalculator.MAX_STAGE) {
            throw new IllegalArgumentException("stage out of range: " + value);
        }
        return new StageUpdate(hubId.asText().strip().toUpperCase(Locale.ROOT), value);
    }
}
