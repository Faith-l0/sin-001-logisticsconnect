package co.wethinkcode.logisticsconnect;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HubDeduplicatorTest {

    private final List<HubRecord> hubs =
            HubDeduplicator.deduplicate(HubLoader.loadFromResource("hubs-global.csv"));

    @Test
    void eighteenRowsBecomeTenHubs() {
        assertEquals(10, hubs.size());
    }

    @Test
    void johannesburgCentralMergesFourRowsIntoTheLowestId() {
        HubRecord hub = find(hubs, "H-500");
        assertEquals(List.of("H-504", "H-510", "H-515"), hub.aliases());
        assertEquals(Boolean.TRUE, hub.active()); // three true against one false
    }

    @Test
    void blankProvinceIsFilledFromTheSameSortingCenter() {
        HubRecord hub = find(hubs, "H-502");
        assertEquals("Gauteng", hub.province());
        assertEquals(List.of("H-508"), hub.aliases());
    }

    @Test
    void aTieGoesToTheLowestId() {
        // H-502 is false and H-508 is true, so the lowest ID decides.
        assertEquals(Boolean.FALSE, find(hubs, "H-502").active());
    }

    @Test
    void mergedIdsNoLongerAppearAsSeparateHubs() {
        assertTrue(hubs.stream().noneMatch(h -> h.hubId().equals("H-504")));
    }

    @Test
    void kwaZuluNatalVariantsMergeIntoOneHub() {
        assertEquals(List.of("H-506", "H-516"), find(hubs, "H-503").aliases());
    }

    @Test
    void unknownActiveStaysUnknown() {
        assertNull(find(hubs, "H-511").active());
    }

    @Test
    void blankProvinceIsNotGuessedWhenTheCenterExistsInTwoProvinces() {
        List<HubRecord> input = List.of(
                new HubRecord("H-1", "Gauteng", "Newlands", true, List.of()),
                new HubRecord("H-2", "Western Cape", "Newlands", true, List.of()),
                new HubRecord("H-3", null, "Newlands", true, List.of()));
        List<HubRecord> result = HubDeduplicator.deduplicate(input);
        assertEquals(3, result.size());
        assertNull(find(result, "H-3").province());
    }

    private static HubRecord find(List<HubRecord> list, String id) {
        return list.stream()
                .filter(r -> r.hubId().equals(id))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No hub " + id));
    }
}
