package com.qa.framework.stepdefinitions.db;

import com.qa.framework.db.DatabaseConnection;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Step definitions for SELECT (read) operations.
 */
public class DatabaseSelectStepDefinitions {

    private DatabaseStepContext ctx() {
        return DatabaseStepContext.getInstance();
    }

    @When("I execute the query {string}")
    public void iExecuteTheQuery(String query) {
        iExecuteTheQueryOn(query, null);
    }

    @When("I execute the query {string} on {string}")
    public void iExecuteTheQueryOn(String query, String connectionName) {
        try {
            DatabaseConnection conn = connectionName != null && !connectionName.isEmpty()
                    ? ctx().getDbManager().getConnection(connectionName)
                    : ctx().getCurrentConnection();
            assertNotNull(conn, "No connection found" + (connectionName != null ? " for: " + connectionName : ""));
            List<Map<String, Object>> results = conn.executeQuery(query);
            ctx().setQueryResults(results);
            ctx().setLastException(null);
            if (connectionName != null) {
                ctx().putResultsForConnection(connectionName, results);
                ctx().setCurrentConnection(conn);
            }
        } catch (Exception e) {
            ctx().setLastException(e);
        }
    }

    @When("I execute the prepared query {string} with parameters:")
    public void iExecuteThePreparedQueryWithParameters(String query, List<String> parameters) {
        try {
            ctx().setQueryResults(ctx().getCurrentConnection().executePreparedQuery(query, parameters.toArray()));
            ctx().setLastException(null);
        } catch (Exception e) {
            ctx().setLastException(e);
        }
    }

    @When("I execute the prepared query {string} with parameters {string}")
    public void iExecuteThePreparedQueryWithParameters(String query, String paramList) {
        String[] params = paramList.split(",\\s*");
        try {
            ctx().setQueryResults(ctx().getCurrentConnection().executePreparedQuery(query, (Object[]) params));
            ctx().setLastException(null);
        } catch (Exception e) {
            ctx().setLastException(e);
        }
    }

    @Then("the query should return {int} row(s)")
    public void theQueryShouldReturnRows(int expectedRowCount) {
        assertNotNull(ctx().getQueryResults(), "Query results should not be null");
        assertEquals(expectedRowCount, ctx().getQueryResults().size(),
                "Expected " + expectedRowCount + " rows but got " + ctx().getQueryResults().size());
    }

    @Then("the query should return at least {int} row(s)")
    public void theQueryShouldReturnAtLeastRows(int minRowCount) {
        assertNotNull(ctx().getQueryResults(), "Query results should not be null");
        assertTrue(ctx().getQueryResults().size() >= minRowCount,
                "Expected at least " + minRowCount + " rows but got " + ctx().getQueryResults().size());
    }

    @Then("the query should return at most {int} row(s)")
    public void theQueryShouldReturnAtMostRows(int maxRowCount) {
        assertNotNull(ctx().getQueryResults(), "Query results should not be null");
        assertTrue(ctx().getQueryResults().size() <= maxRowCount,
                "Expected at most " + maxRowCount + " rows but got " + ctx().getQueryResults().size());
    }

    @Then("the query should return no rows")
    public void theQueryShouldReturnNoRows() {
        assertNotNull(ctx().getQueryResults(), "Query results should not be null");
        assertTrue(ctx().getQueryResults().isEmpty(), "Expected 0 rows but got " + ctx().getQueryResults().size());
    }

    @Then("the first row should contain column {string} with value {string}")
    public void theFirstRowShouldContainColumnWithValue(String columnName, String expectedValue) {
        assertNotNull(ctx().getQueryResults(), "Query results should not be null");
        assertFalse(ctx().getQueryResults().isEmpty(), "Query results should not be empty");
        Map<String, Object> firstRow = ctx().getQueryResults().get(0);
        assertTrue(firstRow.containsKey(columnName), "Column '" + columnName + "' should exist");
        assertEquals(expectedValue, String.valueOf(firstRow.get(columnName)), "Column '" + columnName + "' value mismatch");
    }

    @Then("the first row should contain column {string} with value greater than {string}")
    public void theFirstRowShouldContainColumnWithValueGreaterThan(String columnName, String minValue) {
        assertNotNull(ctx().getQueryResults(), "Query results should not be null");
        assertFalse(ctx().getQueryResults().isEmpty(), "Query results should not be empty");
        Map<String, Object> firstRow = ctx().getQueryResults().get(0);
        assertTrue(firstRow.containsKey(columnName), "Column '" + columnName + "' should exist");
        Object value = firstRow.get(columnName);
        assertNotNull(value, "Column '" + columnName + "' value should not be null");
        long actualValue = Long.parseLong(String.valueOf(value));
        long expectedMinValue = Long.parseLong(minValue);
        assertTrue(actualValue > expectedMinValue,
                "Column '" + columnName + "' value (" + actualValue + ") should be greater than " + expectedMinValue);
    }

    @Then("the first row should contain column {string} with value less than {string}")
    public void theFirstRowShouldContainColumnWithValueLessThan(String columnName, String maxValue) {
        assertNotNull(ctx().getQueryResults(), "Query results should not be null");
        assertFalse(ctx().getQueryResults().isEmpty(), "Query results should not be empty");
        Map<String, Object> firstRow = ctx().getQueryResults().get(0);
        assertTrue(firstRow.containsKey(columnName), "Column '" + columnName + "' should exist");
        Object value = firstRow.get(columnName);
        assertNotNull(value, "Column '" + columnName + "' value should not be null");
        long actualValue = Long.parseLong(String.valueOf(value));
        long expectedMaxValue = Long.parseLong(maxValue);
        assertTrue(actualValue < expectedMaxValue,
                "Column '" + columnName + "' value (" + actualValue + ") should be less than " + expectedMaxValue);
    }

    @Then("the first row should contain column {string} with value between {string} and {string}")
    public void theFirstRowShouldContainColumnWithValueBetween(String columnName, String minValue, String maxValue) {
        assertNotNull(ctx().getQueryResults(), "Query results should not be null");
        assertFalse(ctx().getQueryResults().isEmpty(), "Query results should not be empty");
        Map<String, Object> firstRow = ctx().getQueryResults().get(0);
        assertTrue(firstRow.containsKey(columnName), "Column '" + columnName + "' should exist");
        Object value = firstRow.get(columnName);
        assertNotNull(value, "Column '" + columnName + "' value should not be null");
        long actualValue = Long.parseLong(String.valueOf(value));
        long expectedMin = Long.parseLong(minValue);
        long expectedMax = Long.parseLong(maxValue);
        assertTrue(actualValue >= expectedMin && actualValue <= expectedMax,
                "Column '" + columnName + "' value (" + actualValue + ") should be between " + expectedMin + " and " + expectedMax);
    }

    @Then("the query should execute successfully")
    public void theQueryShouldExecuteSuccessfully() {
        assertNull(ctx().getLastException(), "Query should execute without exception");
        assertNotNull(ctx().getQueryResults(), "Query results should not be null");
    }

    @Then("the result set should contain a column {string}")
    public void theResultSetShouldContainAColumn(String columnName) {
        assertNotNull(ctx().getQueryResults(), "Query results should not be null");
        assertFalse(ctx().getQueryResults().isEmpty(), "Query results should not be empty");
        Map<String, Object> firstRow = ctx().getQueryResults().get(0);
        assertTrue(firstRow.containsKey(columnName), "Result set should contain column '" + columnName + "'");
    }

    @Then("all rows should have column {string} not null")
    public void allRowsShouldHaveColumnNotNull(String columnName) {
        assertNotNull(ctx().getQueryResults(), "Query results should not be null");
        assertFalse(ctx().getQueryResults().isEmpty(), "Query results should not be empty");
        for (Map<String, Object> row : ctx().getQueryResults()) {
            assertTrue(row.containsKey(columnName), "Column '" + columnName + "' should exist");
            assertNotNull(row.get(columnName), "Column '" + columnName + "' should not be null");
        }
    }

    @Then("the result row count from {string} should equal the result row count from {string}")
    public void theResultRowCountFromShouldEqualTheResultRowCountFrom(String conn1, String conn2) {
        List<Map<String, Object>> results1 = ctx().getResultsForConnection(conn1);
        List<Map<String, Object>> results2 = ctx().getResultsForConnection(conn2);
        assertNotNull(results1, "No results for connection: " + conn1);
        assertNotNull(results2, "No results for connection: " + conn2);
        assertEquals(results1.size(), results2.size(),
                "Row count mismatch: " + conn1 + " has " + results1.size() + ", " + conn2 + " has " + results2.size());
    }

    @Then("the query on {string} should return {int} row(s)")
    public void theQueryOnShouldReturnRows(String connectionName, int expectedRowCount) {
        List<Map<String, Object>> results = ctx().getResultsForConnection(connectionName);
        assertNotNull(results, "No results for connection: " + connectionName);
        assertEquals(expectedRowCount, results.size(),
                "Expected " + expectedRowCount + " rows on " + connectionName + " but got " + results.size());
    }

    @Then("row {int} should contain column {string} with value {string}")
    public void rowShouldContainColumnWithValue(int rowIndex, String columnName, String expectedValue) {
        assertNotNull(ctx().getQueryResults(), "Query results should not be null");
        assertTrue(ctx().getQueryResults().size() > rowIndex - 1, "Row " + rowIndex + " does not exist");
        Map<String, Object> row = ctx().getQueryResults().get(rowIndex - 1);
        assertTrue(row.containsKey(columnName), "Column '" + columnName + "' should exist");
        assertEquals(expectedValue, String.valueOf(row.get(columnName)), "Column value mismatch");
    }
}
