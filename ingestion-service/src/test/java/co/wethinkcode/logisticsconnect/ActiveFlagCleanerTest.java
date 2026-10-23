package co.wethinkcode.logisticsconnect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ActiveFlagCleanerTest {

    @Test
    void trueSpellings() {
        for (String raw : new String[] {"Y", "yes", "YES", "true", "TRUE", "1"}) {
            assertEquals(Boolean.TRUE, ActiveFlagCleaner.parseActive(raw), raw);
        }
    }

    @Test
    void falseSpellings() {
        for (String raw : new String[] {"N", "no", "NO", "false", "FALSE", "0"}) {
            assertEquals(Boolean.FALSE, ActiveFlagCleaner.parseActive(raw), raw);
        }
    }

    @Test
    void placeholdersBecomeNullNotFalse() {
        for (String raw : new String[] {"unknown", "N/A", "n/a", "TBD", "-", "NaN"}) {
            assertNull(ActiveFlagCleaner.parseActive(raw), raw);
        }
    }

    @Test
    void blankOrNullBecomesNull() {
        assertNull(ActiveFlagCleaner.parseActive(""));
        assertNull(ActiveFlagCleaner.parseActive("   "));
        assertNull(ActiveFlagCleaner.parseActive(null));
    }

    @Test
    void paddingIsIgnored() {
        assertEquals(Boolean.TRUE, ActiveFlagCleaner.parseActive(" Y "));
        assertEquals(Boolean.FALSE, ActiveFlagCleaner.parseActive("false "));
    }
}
