package co.wethinkcode.logisticsconnect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

/** The JSON body published to the package-status topic when a hub's stage changes. */
public final class StageMessage {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private StageMessage() {
    }

    public static String toJson(String hubId, int stage) {
        try {
            return MAPPER.writeValueAsString(Map.of("hubId", hubId, "stage", stage));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("could not serialise stage message", e);
        }
    }
}
