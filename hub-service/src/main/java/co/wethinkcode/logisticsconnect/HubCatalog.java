package co.wethinkcode.logisticsconnect;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/** Pure lookups over the cleaned hub list. No I/O, so it is easy to test. */
public final class HubCatalog {

    private HubCatalog() {
    }

    public static List<String> provinces(List<HubRecord> hubs) {
        return hubs.stream()
                .map(HubRecord::province)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();
    }

    public static List<String> sortingCenters(List<HubRecord> hubs, String province) {
        String wanted = key(province);
        return hubs.stream()
                .filter(h -> h.province() != null && key(h.province()).equals(wanted))
                .map(HubRecord::sortingCenter)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();
    }

    /** Matches the canonical ID or any alias, ignoring case and padding. */
    public static Optional<HubRecord> findById(List<HubRecord> hubs, String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        String wanted = id.strip().toUpperCase(Locale.ROOT);
        return hubs.stream()
                .filter(h -> wanted.equals(h.hubId())
                        || (h.aliases() != null && h.aliases().contains(wanted)))
                .findFirst();
    }

    private static String key(String s) {
        return s == null ? "" : s.strip().toLowerCase(Locale.ROOT);
    }
}
