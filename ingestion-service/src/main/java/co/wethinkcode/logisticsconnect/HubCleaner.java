package co.wethinkcode.logisticsconnect;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Pure cleaning functions for the raw hub data. No I/O and no web code,
 * so every rule can be unit tested on its own.
 */
public final class HubCleaner {

    private HubCleaner() {
    }

    /** "h-501 " becomes "H-501". Blank or null becomes null. */
    public static String cleanId(String raw) {
        String trimmed = trimToNull(raw);
        return trimmed == null ? null : trimmed.toUpperCase(Locale.ROOT);
    }

    /** "cape town  port" becomes "Cape Town Port". Blank or null becomes null. */
    public static String cleanName(String raw) {
        String trimmed = trimToNull(raw);
        if (trimmed == null) {
            return null;
        }
        return Arrays.stream(trimmed.split("\\s+"))
                .map(word -> word.substring(0, 1).toUpperCase(Locale.ROOT)
                        + word.substring(1).toLowerCase(Locale.ROOT))
                .collect(Collectors.joining(" "));
    }

    private static String trimToNull(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.strip();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
