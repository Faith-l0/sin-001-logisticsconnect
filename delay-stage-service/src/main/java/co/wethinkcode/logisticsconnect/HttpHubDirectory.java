package co.wethinkcode.logisticsconnect;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

/** Asks hub-service whether a hub exists and what its canonical ID is. */
public class HttpHubDirectory implements HubDirectory {

    private final String baseUrl;
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .build();
    private final ObjectMapper mapper = new ObjectMapper();

    public HttpHubDirectory(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public Optional<String> canonicalId(String hubId) {
        String encoded = URLEncoder.encode(hubId, StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/hubs/" + encoded))
                .timeout(Duration.ofSeconds(3))
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 404) {
                return Optional.empty();
            }
            if (response.statusCode() != 200) {
                throw new HubServiceUnavailableException("hub-service answered " + response.statusCode());
            }
            JsonNode hub = mapper.readTree(response.body());
            return Optional.of(hub.get("hubId").asText());
        } catch (IOException e) {
            throw new HubServiceUnavailableException("hub-service unreachable", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new HubServiceUnavailableException("interrupted while contacting hub-service", e);
        }
    }
}
