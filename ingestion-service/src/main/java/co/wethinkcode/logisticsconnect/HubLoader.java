package co.wethinkcode.logisticsconnect;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Reads the raw hubs CSV and runs every field through the cleaners.
 * Rows come back one-for-one: nothing is merged or dropped here, apart from
 * rows with no usable ID. Deduplication is a separate step.
 */
public final class HubLoader {

    private HubLoader() {
    }

    public static List<HubRecord> loadFromResource(String resourceName) {
        try (InputStream in = HubLoader.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (in == null) {
                throw new IllegalStateException("Resource not found: " + resourceName);
            }
            return load(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + resourceName, e);
        }
    }

    public static List<HubRecord> load(Reader reader) {
        try (CSVReader csv = new CSVReader(reader)) {
            String[] header = csv.readNext();
            if (header == null) {
                return List.of();
            }
            Map<String, Integer> columns = new HashMap<>();
            for (int i = 0; i < header.length; i++) {
                // Header names are padded too (" Province "), and a file can start with a BOM.
                String name = header[i].replace("\uFEFF", "").strip().toLowerCase(Locale.ROOT);
                columns.put(name, i);
            }

            List<HubRecord> records = new ArrayList<>();
            String[] row;
            while ((row = csv.readNext()) != null) {
                HubRecord record = toRecord(row, columns);
                if (record.hubId() != null) {
                    records.add(record);
                }
            }
            return records;
        } catch (IOException | CsvValidationException e) {
            throw new IllegalStateException("Could not parse CSV", e);
        }
    }

    private static HubRecord toRecord(String[] row, Map<String, Integer> columns) {
        return new HubRecord(
                HubCleaner.cleanId(cell(row, columns, "hub_id")),
                ProvinceCleaner.cleanProvince(cell(row, columns, "province")),
                HubCleaner.cleanName(cell(row, columns, "sorting_center")),
                ActiveFlagCleaner.parseActive(cell(row, columns, "active")),
                List.of());
    }

    private static String cell(String[] row, Map<String, Integer> columns, String name) {
        Integer index = columns.get(name);
        return (index == null || index >= row.length) ? null : row[index];
    }
}
