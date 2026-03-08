package com.qa.framework.stepdefinitions.db;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Step definitions for schema/metadata verification.
 */
public class DatabaseSchemaStepDefinitions {

    private DatabaseStepContext ctx() {
        return DatabaseStepContext.getInstance();
    }

    @When("I execute the query to count rows in table {string}")
    public void iExecuteTheQueryToCountRowsInTable(String tableName) {
        try {
            ctx().setQueryResults(ctx().getCurrentConnection().executeQuery("SELECT COUNT(*) as row_count FROM " + tableName));
            ctx().setLastException(null);
        } catch (Exception e) {
            ctx().setLastException(e);
        }
    }

    @Then("the table {string} should have {int} row(s)")
    public void theTableShouldHaveRows(String tableName, int expectedCount) {
        assertNotNull(ctx().getQueryResults(), "Query results should not be null");
        assertFalse(ctx().getQueryResults().isEmpty(), "Query results should not be empty");
        Map<String, Object> firstRow = ctx().getQueryResults().get(0);
        Object countObj = firstRow.get("row_count");
        if (countObj == null) {
            countObj = firstRow.get("ROW_COUNT");
        }
        assertNotNull(countObj, "row_count column should exist");
        int actualCount = ((Number) countObj).intValue();
        assertEquals(expectedCount, actualCount, "Table " + tableName + " should have " + expectedCount + " rows");
    }
}
