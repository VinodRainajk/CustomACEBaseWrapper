package com.qa.framework.utils;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvResultComparatorTest {

    @Test
    void orderedComparisonDetectsOrderDifference() {
        List<Map<String, Object>> actual = List.of(
                Map.of("id", "1", "name", "A"),
                Map.of("id", "2", "name", "B")
        );
        List<Map<String, String>> expected = List.of(
                Map.of("id", "2", "name", "B"),
                Map.of("id", "1", "name", "A")
        );

        CsvResultComparator.ComparisonResult ordered = CsvResultComparator.compareOrdered(actual, expected);
        CsvResultComparator.ComparisonResult unordered = CsvResultComparator.compareIgnoringOrder(actual, expected);

        assertFalse(ordered.matches(), ordered.message());
        assertTrue(unordered.matches(), unordered.message());
    }
}
