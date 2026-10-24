package co.wethinkcode.logisticsconnect;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Merges rows that describe the same real hub.
 *
 * Rules:
 *  1. A blank province is filled in only when every other row with the same
 *     sorting center agrees on one province. Otherwise it stays null.
 *  2. Rows with the same sorting center and province are one hub.
 *  3. The lowest hub ID is canonical and the other IDs become aliases.
 *  4. The active flag is a majority vote, ignoring unknowns. On a tie the
 *     lowest ID's value wins.
 */
public final class HubDeduplicator {

    private static final Comparator<HubRecord> BY_ID = Comparator
            .comparingLong((HubRecord r) -> numberIn(r.hubId()))
            .thenComparing(HubRecord::hubId);

    private HubDeduplicator() {
    }

    public static List<HubRecord> deduplicate(List<HubRecord> records) {
        List<HubRecord> sorted = new ArrayList<>(fillMissingProvinces(records));
        sorted.sort(BY_ID);

        Map<String, List<HubRecord>> groups = new LinkedHashMap<>();
        for (HubRecord record : sorted) {
            groups.computeIfAbsent(groupKey(record), k -> new ArrayList<>()).add(record);
        }
        return groups.values().stream().map(HubDeduplicator::merge).toList();
    }

    private static List<HubRecord> fillMissingProvinces(List<HubRecord> records) {
        Map<String, Set<String>> provincesByCenter = new HashMap<>();
        for (HubRecord r : records) {
            if (r.sortingCenter() != null && r.province() != null) {
                provincesByCenter.computeIfAbsent(r.sortingCenter(), k -> new HashSet<>())
                        .add(r.province());
            }
        }
        return records.stream().map(r -> {
            if (r.province() == null && r.sortingCenter() != null) {
                Set<String> known = provincesByCenter.getOrDefault(r.sortingCenter(), Set.of());
                if (known.size() == 1) {
                    return new HubRecord(r.hubId(), known.iterator().next(),
                            r.sortingCenter(), r.active(), r.aliases());
                }
            }
            return r;
        }).toList();
    }

    private static String groupKey(HubRecord r) {
        if (r.sortingCenter() == null) {
            return "id:" + r.hubId();
        }
        return r.sortingCenter().toLowerCase(Locale.ROOT) + "|" + r.province();
    }

    private static HubRecord merge(List<HubRecord> group) {
        HubRecord canonical = group.get(0); // lowest ID, because the list is sorted
        List<String> aliases = group.stream().skip(1).map(HubRecord::hubId).toList();
        return new HubRecord(canonical.hubId(), canonical.province(),
                canonical.sortingCenter(), resolveActive(group), aliases);
    }

    private static Boolean resolveActive(List<HubRecord> group) {
        long yes = group.stream().filter(r -> Boolean.TRUE.equals(r.active())).count();
        long no = group.stream().filter(r -> Boolean.FALSE.equals(r.active())).count();
        if (yes > no) {
            return Boolean.TRUE;
        }
        if (no > yes) {
            return Boolean.FALSE;
        }
        // Tie (or nothing known): the lowest ID with a known value decides.
        return group.stream().map(HubRecord::active).filter(Objects::nonNull).findFirst().orElse(null);
    }

    private static long numberIn(String id) {
        String digits = id.replaceAll("\\D", "");
        return digits.isEmpty() ? Long.MAX_VALUE : Long.parseLong(digits);
    }
}
