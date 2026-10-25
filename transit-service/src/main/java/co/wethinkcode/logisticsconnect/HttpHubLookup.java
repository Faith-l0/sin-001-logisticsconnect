package co.wethinkcode.logisticsconnect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class HttpHubLookup implements HubLookup {

    private final String baseUrl;
    private final ObjectMapper mapper = new ObjectMapper();

    public HttpHubLookup(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public Optional<HubInfo> find(String hubId) {
        String encoded = URLEncoder.encode(hubId, StandardCharsets.UTF_8);
        HttpResponse<String> response = HttpJson.get(baseUrl + "/hubs/" + encoded, "hub-service");
        if (response.statusCode() == 404) {
            return Optional.empty();
        }
        if (response.statusCode() != 200) {
            throw new DependencyUnavailableException("hub-service answered " + response.statusCode());
        }
        try {
            JsonNode hub = mapper.readTree(response.body());
            JsonNode active = hub.get("active");
            // An unknown (null) active flag is treated as active.
            boolean isActive = active == null || active.isNull() || active.asBoolean();
            return Optional.of(new HubInfo(hub.get("hubId").asText(), isActive));
        } catch (JsonProcessingException e) {
            throw new DependencyUnavailableException("hub-service sent unreadable data", e);
        }
    }
}
