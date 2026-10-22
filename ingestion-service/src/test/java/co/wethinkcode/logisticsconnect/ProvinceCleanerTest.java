package co.wethinkcode.logisticsconnect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ProvinceCleanerTest {

    @Test
    void fixesCasing() {
        assertEquals("Gauteng", ProvinceCleaner.cleanProvince("gauteng"));
        assertEquals("Eastern Cape", ProvinceCleaner.cleanProvince("eastern cape"));
        assertEquals("Western Cape", ProvinceCleaner.cleanProvince("western cape"));
    }

    @Test
    void trimsPadding() {
        assertEquals("Gauteng", ProvinceCleaner.cleanProvince(" Gauteng "));
        assertEquals("Western Cape", ProvinceCleaner.cleanProvince("Western Cape "));
    }

    @Test
    void kwaZuluNatalVariantsAllMapToOneName() {
        assertEquals("KwaZulu-Natal", ProvinceCleaner.cleanProvince("Kwa-Zulu Natal"));
        assertEquals("KwaZulu-Natal", ProvinceCleaner.cleanProvince("KwaZulu Natal"));
        assertEquals("KwaZulu-Natal", ProvinceCleaner.cleanProvince("KwaZulu-Natal"));
    }

    @Test
    void northWestKeepsItsSpace() {
        assertEquals("North West", ProvinceCleaner.cleanProvince("North West"));
    }

    @Test
    void unknownOrBlankBecomesNull() {
        assertNull(ProvinceCleaner.cleanProvince("Narnia"));
        assertNull(ProvinceCleaner.cleanProvince(""));
        assertNull(ProvinceCleaner.cleanProvince("   "));
        assertNull(ProvinceCleaner.cleanProvince(null));
    }
}
