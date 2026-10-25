package co.wethinkcode.logisticsconnect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/** Stage 2 version: asks delay-stage-service synchronously. Stage 3 replaces this with the queue. */
public class HttpStageLookup implements StageLookup {

    private final String baseUrl;
    private final ObjectMapper mapper = new ObjectMapper();

    public HttpStageLookup(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public int stageFor(String hubId) {
        String encoded = URLEncoder.encode(hubId, StandardCharsets.UTF_8);
        HttpResponse<String> response = HttpJson.get(baseUrl + "/stage/" + encoded, "delay-stage-service");
        if (response.statusCode() != 200) {
            throw new DependencyUnavailableException("delay-stage-service answered " + response.statusCode());
        }
        try {
            return mapper.readTree(response.body()).get("stage").asInt();
        } catch (JsonProcessingException e) {
            throw new DependencyUnavailableException("delay-stage-service sent unreadable data", e);
        }
    }
}
