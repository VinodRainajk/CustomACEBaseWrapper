package com.qa.framework.utils;

import com.qa.framework.exceptions.WrapperException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Utility for comparing SQL query results with CSV samples.
 * <p>
 * CSV rules:
 * - first row is header
 * - comma separated values (simple parser, no escaped commas/quotes support)
 * - blank values and "null" are normalized to empty string
 */
public final class CsvResultComparator {

    private CsvResultComparator() {
    }

    public static final class ComparisonResult {
        private final boolean matches;
        private final String message;

        private ComparisonResult(boolean matches, String message) {
            this.matches = matches;
            this.message = message;
        }

        public boolean matches() {
            return matches;
        }

        public String message() {
            return message;
        }
    }

    public static final class MismatchRow {
        private final String rowType;
        private final String rowIdentifier;
        private final String columnName;
        private final String expectedValue;
        private final String actualValue;
        private final String remarks;

        public MismatchRow(String rowType, String rowIdentifier, String columnName, String expectedValue, String actualValue, String remarks) {
            this.rowType = rowType;
            this.rowIdentifier = rowIdentifier;
            this.columnName = columnName;
            this.expectedValue = expectedValue;
            this.actualValue = actualValue;
            this.remarks = remarks;
        }

        public String getRowType() {
            return rowType;
        }

        public String getRowIdentifier() {
            return rowIdentifier;
        }

        public String getColumnName() {
            return columnName;
        }

        public String getExpectedValue() {
            return expectedValue;
        }

        public String getActualValue() {
            return actualValue;
        }

        public String getRemarks() {
            return remarks;
        }
    }

    public static final class DetailedComparisonResult {
        private final boolean matches;
        private final String message;
        private final List<MismatchRow> mismatches;

        public DetailedComparisonResult(boolean matches, String message, List<MismatchRow> mismatches) {
            this.matches = matches;
            this.message = message;
            this.mismatches = mismatches;
        }

        public boolean matches() {
            return matches;
        }

        public String message() {
            return message;
        }

        public List<MismatchRow> mismatches() {
            return mismatches;
        }
    }

    public static ComparisonResult compareOrdered(
            List<Map<String, Object>> actualRows,
            List<Map<String, String>> expectedRows
    ) {
        if (actualRows == null) {
            return new ComparisonResult(false, "Actual query results are null.");
        }
        if (actualRows.size() != expectedRows.size()) {
            return new ComparisonResult(false,
                    "Row count mismatch (ordered): expected " + expectedRows.size() + " but got " + actualRows.size());
        }
        for (int i = 0; i < expectedRows.size(); i++) {
            ComparisonResult rowResult = compareRow(actualRows.get(i), expectedRows.get(i), i);
            if (!rowResult.matches()) {
                return rowResult;
            }
        }
        return new ComparisonResult(true, "Ordered comparison matched.");
    }

    public static ComparisonResult compareIgnoringOrder(
            List<Map<String, Object>> actualRows,
            List<Map<String, String>> expectedRows
    ) {
        if (actualRows == null) {
            return new ComparisonResult(false, "Actual query results are null.");
        }
        if (actualRows.size() != expectedRows.size()) {
            return new ComparisonResult(false,
                    "Row count mismatch (ignoring order): expected " + expectedRows.size() + " but got " + actualRows.size());
        }

        Map<String, Integer> expectedCounts = toMultiset(expectedRows);
        Map<String, Integer> actualCounts = toMultisetFromActual(actualRows, expectedRows.isEmpty() ? List.of() : new ArrayList<>(expectedRows.get(0).keySet()));

        if (expectedCounts.equals(actualCounts)) {
            return new ComparisonResult(true, "Unordered comparison matched.");
        }

        String expectedDump = expectedCounts.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getValue() + " x " + e.getKey())
                .collect(Collectors.joining("; "));
        String actualDump = actualCounts.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getValue() + " x " + e.getKey())
                .collect(Collectors.joining("; "));
        return new ComparisonResult(false,
                "Unordered row content mismatch. Expected multiset: [" + expectedDump + "] but got: [" + actualDump + "]");
    }

    /**
     * Checks expected rows are contained in actual rows (order-independent).
     * Extra rows in actual are allowed.
     */
    public static ComparisonResult containsIgnoringOrder(
            List<Map<String, Object>> actualRows,
            List<Map<String, String>> expectedRows
    ) {
        if (actualRows == null) {
            return new ComparisonResult(false, "Actual query results are null.");
        }
        if (expectedRows == null || expectedRows.isEmpty()) {
            return new ComparisonResult(true, "Expected CSV rows are empty; contains check passes.");
        }

        List<String> headers = new ArrayList<>(expectedRows.get(0).keySet());
        Map<String, Integer> actualCounts = toMultisetFromActual(actualRows, headers);
        Map<String, Integer> expectedCounts = toMultiset(expectedRows);

        List<String> missing = new ArrayList<>();
        for (Map.Entry<String, Integer> e : expectedCounts.entrySet()) {
            int actualCount = actualCounts.getOrDefault(e.getKey(), 0);
            if (actualCount < e.getValue()) {
                missing.add("need " + e.getValue() + " x " + e.getKey() + " but found " + actualCount);
            }
        }
        if (missing.isEmpty()) {
            return new ComparisonResult(true, "Contains comparison matched.");
        }
        return new ComparisonResult(false, "Contains comparison failed. Missing rows: " + String.join("; ", missing));
    }

    /**
     * Unordered exact comparison with mismatch details for reporting.
     * Extra rows and missing rows are reported as canonical row values.
     */
    public static DetailedComparisonResult compareIgnoringOrderDetailed(
            List<Map<String, Object>> actualRows,
            List<Map<String, String>> expectedRows
    ) {
        if (actualRows == null) {
            return new DetailedComparisonResult(false, "Actual query results are null.",
                    List.of(new MismatchRow("system", "", "", "", "", "Actual query results are null")));
        }
        if (actualRows.size() != expectedRows.size()) {
            // keep evaluating to produce richer mismatch list
        }
        List<String> headers = expectedRows.isEmpty() ? List.of() : new ArrayList<>(expectedRows.get(0).keySet());
        Map<String, Integer> expectedCounts = toMultiset(expectedRows);
        Map<String, Integer> actualCounts = toMultisetFromActual(actualRows, headers);

        List<MismatchRow> mismatches = new ArrayList<>();
        for (Map.Entry<String, Integer> e : expectedCounts.entrySet()) {
            int actualCount = actualCounts.getOrDefault(e.getKey(), 0);
            if (actualCount < e.getValue()) {
                for (int i = 0; i < (e.getValue() - actualCount); i++) {
                    mismatches.add(new MismatchRow(
                            "missing_in_actual",
                            "",
                            "",
                            e.getKey(),
                            "",
                            "Expected row from CSV was not found in SQL result"
                    ));
                }
            }
        }
        for (Map.Entry<String, Integer> e : actualCounts.entrySet()) {
            int expectedCount = expectedCounts.getOrDefault(e.getKey(), 0);
            if (expectedCount < e.getValue()) {
                for (int i = 0; i < (e.getValue() - expectedCount); i++) {
                    mismatches.add(new MismatchRow(
                            "extra_in_actual",
                            "",
                            "",
                            "",
                            e.getKey(),
                            "Actual SQL row is not present in expected CSV"
                    ));
                }
            }
        }

        if (mismatches.isEmpty()) {
            return new DetailedComparisonResult(true, "Unordered comparison matched.", List.of());
        }
        return new DetailedComparisonResult(false, "Unordered comparison mismatch.", mismatches);
    }

    public static List<Map<String, String>> loadCsvFromClasspath(String classpathPath) {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(classpathPath)) {
            if (is == null) {
                throw new WrapperException("CSV resource not found on classpath: " + classpathPath);
            }
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                List<String> lines = br.lines()
                        .filter(line -> line != null && !line.trim().isEmpty())
                        .collect(Collectors.toList());
                if (lines.isEmpty()) {
                    return List.of();
                }
                List<String> headers = splitCsvLine(lines.get(0));
                List<Map<String, String>> rows = new ArrayList<>();
                for (int i = 1; i < lines.size(); i++) {
                    List<String> values = splitCsvLine(lines.get(i));
                    Map<String, String> row = new HashMap<>();
                    for (int c = 0; c < headers.size(); c++) {
                        String header = headers.get(c).trim();
                        String value = c < values.size() ? normalize(values.get(c)) : "";
                        row.put(header, value);
                    }
                    rows.add(row);
                }
                return rows;
            }
        } catch (WrapperException e) {
            throw e;
        } catch (Exception e) {
            throw new WrapperException("Failed to load/parse CSV: " + classpathPath, e);
        }
    }

    private static ComparisonResult compareRow(Map<String, Object> actual, Map<String, String> expected, int index) {
        for (Map.Entry<String, String> entry : expected.entrySet()) {
            String column = entry.getKey();
            String expectedValue = normalize(entry.getValue());
            if (!actual.containsKey(column)) {
                return new ComparisonResult(false, "Row " + (index + 1) + " missing column '" + column + "'");
            }
            String actualValue = normalize(actual.get(column));
            if (!Objects.equals(actualValue, expectedValue)) {
                return new ComparisonResult(false,
                        "Row " + (index + 1) + " column '" + column + "' mismatch: expected '" + expectedValue +
                                "' but got '" + actualValue + "'");
            }
        }
        return new ComparisonResult(true, "Row matched.");
    }

    private static Map<String, Integer> toMultiset(List<Map<String, String>> rows) {
        Map<String, Integer> map = new HashMap<>();
        for (Map<String, String> row : rows) {
            String key = canonical(row, new ArrayList<>(row.keySet()));
            map.put(key, map.getOrDefault(key, 0) + 1);
        }
        return map;
    }

    private static Map<String, Integer> toMultisetFromActual(List<Map<String, Object>> rows, List<String> headers) {
        Map<String, Integer> map = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Map<String, String> normalized = new HashMap<>();
            for (String h : headers) {
                normalized.put(h, normalize(row.get(h)));
            }
            String key = canonical(normalized, headers);
            map.put(key, map.getOrDefault(key, 0) + 1);
        }
        return map;
    }

    private static String canonical(Map<String, String> row, List<String> headers) {
        return headers.stream()
                .sorted()
                .map(h -> h + "=" + normalize(row.get(h)))
                .collect(Collectors.joining("|"));
    }

    private static List<String> splitCsvLine(String line) {
        String[] split = line.split(",", -1);
        List<String> values = new ArrayList<>(split.length);
        for (String part : split) {
            values.add(part.trim());
        }
        return values;
    }

    private static String normalize(Object value) {
        if (value == null) {
            return "";
        }
        String s = String.valueOf(value).trim();
        if ("null".equalsIgnoreCase(s)) {
            return "";
        }
        return s;
    }
}
