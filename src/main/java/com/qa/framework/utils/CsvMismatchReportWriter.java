package com.qa.framework.utils;

import com.qa.framework.exceptions.WrapperException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Writes CSV mismatch rows into an export artifact.
 */
public final class CsvMismatchReportWriter {

    private CsvMismatchReportWriter() {
    }

    public static void write(String outputPath, List<CsvResultComparator.MismatchRow> mismatches) {
        Path path = Paths.get(outputPath);
        try {
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            List<String> lines = new ArrayList<>();
            lines.add("row_type,row_identifier,column_name,expected_value,actual_value,remarks");
            for (CsvResultComparator.MismatchRow mismatch : mismatches) {
                lines.add(String.join(",",
                        csv(mismatch.getRowType()),
                        csv(mismatch.getRowIdentifier()),
                        csv(mismatch.getColumnName()),
                        csv(mismatch.getExpectedValue()),
                        csv(mismatch.getActualValue()),
                        csv(mismatch.getRemarks())
                ));
            }
            Files.write(path, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new WrapperException("Failed to write mismatch CSV report: " + outputPath, e);
        }
    }

    private static String csv(String value) {
        String safe = value == null ? "" : value;
        String escaped = safe.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }
}
