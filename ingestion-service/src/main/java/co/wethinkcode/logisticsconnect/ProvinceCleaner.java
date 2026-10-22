package co.wethinkcode.logisticsconnect;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Maps any spelling of a South African province to its canonical name.
 * Comparison ignores case, spaces and hyphens, so "Kwa-Zulu Natal" and
 * "KwaZulu Natal" both match "KwaZulu-Natal".
 */
public final class ProvinceCleaner {

    private static final List<String> PROVINCES = List.of(
            "Eastern Cape", "Free State", "Gauteng", "KwaZulu-Natal", "Limpopo",
            "Mpumalanga", "North West", "Northern Cape", "Western Cape");

    private static final Map<String, String> BY_KEY = PROVINCES.stream()
            .collect(Collectors.toMap(ProvinceCleaner::key, Function.identity()));

    private ProvinceCleaner() {
    }

    /** Returns the canonical province name, or null if blank or not recognised. */
    public static String cleanProvince(String raw) {
        if (raw == null) {
            return null;
        }
        return BY_KEY.get(key(raw));
    }

    private static String key(String text) {
        return text.toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
    }
}
