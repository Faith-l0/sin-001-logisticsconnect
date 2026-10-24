package co.wethinkcode.logisticsconnect;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class HubLoaderTest {

    private final List<HubRecord> records = HubLoader.loadFromResource("hubs-global.csv");

    @Test
    void loadsAllEighteenRows() {
        assertEquals(18, records.size());
    }

    @Test
    void cleansEachField() {
        HubRecord hub = byId("H-501");
        assertEquals("Western Cape", hub.province());
        assertEquals("Cape Town Port", hub.sortingCenter());
        assertEquals(Boolean.TRUE, hub.active());
    }

    @Test
    void paddedAndLowerCaseIdsAreFixed() {
        byId("H-502");
        byId("H-512");
    }

    @Test
    void blankProvinceIsNull() {
        assertNull(byId("H-508").province());
    }

    @Test
    void unknownActiveIsNull() {
        assertNull(byId("H-511").active());
        assertNull(byId("H-517").active());
    }

    @Test
    void kwaZuluNatalSpellingsAgree() {
        assertEquals("KwaZulu-Natal", byId("H-503").province());
        assertEquals("KwaZulu-Natal", byId("H-506").province());
        assertEquals("KwaZulu-Natal", byId("H-516").province());
    }

    private HubRecord byId(String id) {
        return records.stream()
                .filter(r -> r.hubId().equals(id))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No hub " + id));
    }
}
