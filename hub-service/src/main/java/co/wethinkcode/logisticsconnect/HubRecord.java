package co.wethinkcode.logisticsconnect;

import java.util.List;

/** A cleaned hub, as served by ingestion-service's GET /hubs. */
public record HubRecord(String hubId, String province, String sortingCenter,
                        Boolean active, List<String> aliases) {
}
