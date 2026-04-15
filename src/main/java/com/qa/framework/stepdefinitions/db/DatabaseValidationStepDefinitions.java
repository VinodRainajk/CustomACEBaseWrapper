package com.qa.framework.stepdefinitions.db;

import com.qa.framework.payload.FeaturePayloadLoader;
import com.qa.framework.utils.CsvMismatchReportWriter;
import com.qa.framework.utils.CsvResultComparator;
import io.cucumber.java.en.Then;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Step definitions for data validation.
 */
public class DatabaseValidationStepDefinitions {

    private DatabaseStepContext ctx() {
        return DatabaseStepContext.getInstance();
    }

    @Then("the first row column {string} should match pattern {string}")
    public void theFirstRowColumnShouldMatchPattern(String columnName, String regex) {
        assertNotNull(ctx().getQueryResults(), "Query results should not be null");
        assertFalse(ctx().getQueryResults().isEmpty(), "Query results should not be empty");
        Map<String, Object> firstRow = ctx().getQueryResults().get(0);
        assertTrue(firstRow.containsKey(columnName), "Column '" + columnName + "' should exist");
        String value = String.valueOf(firstRow.get(columnName));
        assertTrue(Pattern.matches(regex, value), "Column '" + columnName + "' value '" + value + "' should match pattern " + regex);
    }

    @Then("the first row column {string} should be one of {string}")
    public void theFirstRowColumnShouldBeOneOf(String columnName, String allowedValues) {
        assertNotNull(ctx().getQueryResults(), "Query results should not be null");
        assertFalse(ctx().getQueryResults().isEmpty(), "Query results should not be empty");
        Map<String, Object> firstRow = ctx().getQueryResults().get(0);
        assertTrue(firstRow.containsKey(columnName), "Column '" + columnName + "' should exist");
        String actual = String.valueOf(firstRow.get(columnName));
        String[] allowed = allowedValues.split(",\\s*");
        boolean found = false;
        for (String a : allowed) {
            if (actual.equals(a.trim())) {
                found = true;
                break;
            }
        }
        assertTrue(found, "Column '" + columnName + "' value '" + actual + "' should be one of: " + allowedValues);
    }

    @Then("the query result should match CSV from feature payload {string} in order")
    public void theQueryResultShouldMatchCsvInOrder(String payloadKey) {
        CsvResultComparator.ComparisonResult result = compareWithCsv(payloadKey, true);
        assertTrue(result.matches(), result.message());
    }

    @Then("the query result should match CSV from feature payload {string} ignoring order")
    public void theQueryResultShouldMatchCsvIgnoringOrder(String payloadKey) {
        CsvResultComparator.ComparisonResult result = compareWithCsv(payloadKey, false);
        assertTrue(result.matches(), result.message());
    }

    @Then("the query result should not match CSV from feature payload {string} in order")
    public void theQueryResultShouldNotMatchCsvInOrder(String payloadKey) {
        CsvResultComparator.ComparisonResult result = compareWithCsv(payloadKey, true);
        assertFalse(result.matches(), "Expected mismatch but matched. " + result.message());
    }

    @Then("the query result should not match CSV from feature payload {string} ignoring order")
    public void theQueryResultShouldNotMatchCsvIgnoringOrder(String payloadKey) {
        CsvResultComparator.ComparisonResult result = compareWithCsv(payloadKey, false);
        assertFalse(result.matches(), "Expected mismatch but matched. " + result.message());
    }

    @Then("the query result should match CSV from feature payload {string}")
    public void theQueryResultShouldMatchCsvDeferred(String payloadKey) {
        CsvResultComparator.DetailedComparisonResult detailed = compareWithCsvDetailed(payloadKey);
        ctx().setLastCsvComparisonState(new DatabaseStepContext.CsvComparisonState(true, payloadKey, detailed));
    }

    @Then("the query result should not match CSV from feature payload {string}")
    public void theQueryResultShouldNotMatchCsvDeferred(String payloadKey) {
        CsvResultComparator.DetailedComparisonResult detailed = compareWithCsvDetailed(payloadKey);
        ctx().setLastCsvComparisonState(new DatabaseStepContext.CsvComparisonState(false, payloadKey, detailed));
    }

    @Then("I export CSV mismatches with remarks to {string}")
    public void iExportCsvMismatchesWithRemarksTo(String outputPath) {
        DatabaseStepContext.CsvComparisonState state = ctx().getLastCsvComparisonState();
        assertNotNull(state, "No CSV comparison state found. Run CSV comparison step before export.");

        CsvResultComparator.DetailedComparisonResult result = state.getResult();
        CsvMismatchReportWriter.write(outputPath, result.mismatches());

        if (state.isExpectedToMatch()) {
            assertTrue(result.matches(), "CSV comparison failed. Report exported to: " + outputPath + ". " + result.message());
        } else {
            assertFalse(result.matches(), "Expected mismatch but matched. Report exported to: " + outputPath + ". " + result.message());
        }
    }

    private CsvResultComparator.ComparisonResult compareWithCsv(String payloadKey, boolean ordered) {
        assertNotNull(ctx().getQueryResults(), "Query results should not be null");
        String csvPath = FeaturePayloadLoader.getFileClasspathPath(payloadKey);
        List<Map<String, String>> expectedRows = CsvResultComparator.loadCsvFromClasspath(csvPath);
        if (ordered) {
            return CsvResultComparator.compareOrdered(ctx().getQueryResults(), expectedRows);
        }
        return CsvResultComparator.compareIgnoringOrder(ctx().getQueryResults(), expectedRows);
    }

    private CsvResultComparator.DetailedComparisonResult compareWithCsvDetailed(String payloadKey) {
        assertNotNull(ctx().getQueryResults(), "Query results should not be null");
        String csvPath = FeaturePayloadLoader.getFileClasspathPath(payloadKey);
        List<Map<String, String>> expectedRows = CsvResultComparator.loadCsvFromClasspath(csvPath);
        return CsvResultComparator.compareIgnoringOrderDetailed(ctx().getQueryResults(), expectedRows);
    }
}
