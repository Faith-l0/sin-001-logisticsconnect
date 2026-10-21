package co.wethinkcode.logisticsconnect;

import java.util.List;

/**
 * One cleaned hub. 'active' is a Boolean so it can be null, meaning unknown
 * (the CSV has placeholders like "unknown" and "N/A"). 'aliases' will hold the
 * IDs of duplicate rows that were merged into this record.
 */
public record HubRecord(
        String hubId,
        String province,
        String sortingCenter,
        Boolean active,
        List<String> aliases) {
}
