package co.wethinkcode.logisticsconnect;

import java.util.Locale;
import java.util.Set;

/**
 * Turns the many spellings of the "active" flag into true, false or null.
 * null means "unknown": a placeholder, a blank, or anything unrecognised.
 * Unknown values are never guessed.
 */
public final class ActiveFlagCleaner {

    private static final Set<String> TRUE_VALUES = Set.of("y", "yes", "true", "1");
    private static final Set<String> FALSE_VALUES = Set.of("n", "no", "false", "0");

    private ActiveFlagCleaner() {
    }

    public static Boolean parseActive(String raw) {
        if (raw == null) {
            return null;
        }
        String value = raw.strip().toLowerCase(Locale.ROOT);
        if (TRUE_VALUES.contains(value)) {
            return Boolean.TRUE;
        }
        if (FALSE_VALUES.contains(value)) {
            return Boolean.FALSE;
        }
        return null;
    }
}
