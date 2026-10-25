package co.wethinkcode.logisticsconnect;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

/** Fetches cleaned hubs from ingestion-service over HTTP. */
public class HttpHubSource implements HubSource {

    private final String url;
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .build();
    private final ObjectMapper mapper = new ObjectMapper();

    public HttpHubSource(String url) {
        this.url = url;
    }

    @Override
    public List<HubRecord> fetchHubs() {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(3))
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new SourceUnavailableException(
                        "ingestion-service answered " + response.statusCode());
            }
            return mapper.readValue(response.body(), new TypeReference<List<HubRecord>>() { });
        } catch (IOException e) {
            throw new SourceUnavailableException("ingestion-service unreachable", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SourceUnavailableException("interrupted while contacting ingestion-service", e);
        }
    }
}
