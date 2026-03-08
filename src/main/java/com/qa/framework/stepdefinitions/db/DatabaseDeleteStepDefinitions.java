package com.qa.framework.stepdefinitions.db;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Step definitions for DELETE operations.
 */
public class DatabaseDeleteStepDefinitions {

    private DatabaseStepContext ctx() {
        return DatabaseStepContext.getInstance();
    }

    @When("I execute the delete query {string}")
    public void iExecuteTheDeleteQuery(String query) {
        try {
            int count = ctx().getCurrentConnection().executeUpdate(query);
            ctx().setDeleteCount(count);
            ctx().setUpdateCount(count);
            ctx().setLastException(null);
        } catch (Exception e) {
            ctx().setLastException(e);
        }
    }

    @When("I execute the prepared delete {string} with parameters:")
    public void iExecuteThePreparedDeleteWithParameters(String query, List<String> parameters) {
        try {
            int count = ctx().getCurrentConnection().executePreparedUpdate(query, parameters.toArray());
            ctx().setDeleteCount(count);
            ctx().setUpdateCount(count);
            ctx().setLastException(null);
        } catch (Exception e) {
            ctx().setLastException(e);
        }
    }

    @When("I truncate the table {string}")
    public void iTruncateTheTable(String tableName) {
        try {
            int count = ctx().getCurrentConnection().executeUpdate("TRUNCATE TABLE " + tableName);
            ctx().setDeleteCount(count);
            ctx().setUpdateCount(count);
            ctx().setLastException(null);
        } catch (Exception e) {
            ctx().setLastException(e);
        }
    }

    @Then("the delete should affect {int} row(s)")
    public void theDeleteShouldAffectRows(int expectedCount) {
        assertEquals(expectedCount, ctx().getDeleteCount(),
                "Expected " + expectedCount + " rows deleted but got " + ctx().getDeleteCount());
    }

    @Then("the delete should execute successfully")
    public void theDeleteShouldExecuteSuccessfully() {
        assertNull(ctx().getLastException(), "Delete should execute without exception");
    }
}
