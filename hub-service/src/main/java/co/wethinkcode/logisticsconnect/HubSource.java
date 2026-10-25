package co.wethinkcode.logisticsconnect;

import java.util.List;

/** Where hub-service gets its data. Swappable so tests need no network. */
@FunctionalInterface
public interface HubSource {
    List<HubRecord> fetchHubs() throws SourceUnavailableException;
}
