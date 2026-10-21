package co.wethinkcode.logisticsconnect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class HubCleanerTest {

    @Test
    void cleanId_trimsPadding() {
        assertEquals("H-502", HubCleaner.cleanId("H-502 "));
    }

    @Test
    void cleanId_upperCases() {
        assertEquals("H-501", HubCleaner.cleanId("h-501"));
    }

    @Test
    void cleanId_blankOrNullBecomesNull() {
        assertNull(HubCleaner.cleanId("   "));
        assertNull(HubCleaner.cleanId(null));
    }

    @Test
    void cleanName_collapsesDoubleSpaces() {
        assertEquals("Cape Town Port", HubCleaner.cleanName("Cape Town  Port"));
    }

    @Test
    void cleanName_titleCases() {
        assertEquals("Johannesburg Central", HubCleaner.cleanName("johannesburg central"));
    }

    @Test
    void cleanName_trimsPadding() {
        assertEquals("Pretoria North", HubCleaner.cleanName("  Pretoria North "));
    }

    @Test
    void cleanName_blankOrNullBecomesNull() {
        assertNull(HubCleaner.cleanName(""));
        assertNull(HubCleaner.cleanName(null));
    }
}
