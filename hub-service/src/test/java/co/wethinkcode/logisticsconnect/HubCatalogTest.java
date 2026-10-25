package co.wethinkcode.logisticsconnect;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class HubCatalogTest {

    private static HubRecord hub(String id, String province, String center, List<String> aliases) {
        return new HubRecord(id, province, center, true, aliases);
    }

    private final List<HubRecord> hubs = List.of(
            hub("H-500", "Gauteng", "Johannesburg Central", List.of("H-504", "H-510")),
            hub("H-502", "Gauteng", "Pretoria North", List.of()),
            hub("H-501", "Western Cape", "Cape Town Port", List.of("H-505")),
            hub("H-509", null, "Mystery Hub", List.of()));

    @Test
    void provincesAreDistinctSortedAndSkipUnknown() {
        assertEquals(List.of("Gauteng", "Western Cape"), HubCatalog.provinces(hubs));
    }

    @Test
    void provincesOfEmptyListIsEmpty() {
        assertEquals(List.of(), HubCatalog.provinces(List.of()));
    }

    @Test
    void sortingCentersAreSortedForAProvince() {
        assertEquals(List.of("Johannesburg Central", "Pretoria North"),
                HubCatalog.sortingCenters(hubs, "Gauteng"));
    }

    @Test
    void sortingCenterLookupIgnoresCaseAndPadding() {
        assertEquals(List.of("Cape Town Port"), HubCatalog.sortingCenters(hubs, "  western cape "));
    }

    @Test
    void sortingCentersOfUnknownProvinceIsEmpty() {
        assertEquals(List.of(), HubCatalog.sortingCenters(hubs, "Narnia"));
    }

    @Test
    void findByCanonicalId() {
        Optional<HubRecord> found = HubCatalog.findById(hubs, "H-502");
        assertTrue(found.isPresent());
        assertEquals("Pretoria North", found.get().sortingCenter());
    }

    @Test
    void findByAliasReturnsCanonicalHub() {
        Optional<HubRecord> found = HubCatalog.findById(hubs, "h-504 ");
        assertTrue(found.isPresent());
        assertEquals("H-500", found.get().hubId());
    }

    @Test
    void findUnknownIdIsEmpty() {
        assertTrue(HubCatalog.findById(hubs, "H-999").isEmpty());
    }

    @Test
    void findNullIdIsEmpty() {
        assertTrue(HubCatalog.findById(hubs, null).isEmpty());
    }
}
