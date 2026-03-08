package com.qa.framework.stepdefinitions.db;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Step definitions for UPDATE operations.
 */
public class DatabaseUpdateStepDefinitions {

    private DatabaseStepContext ctx() {
        return DatabaseStepContext.getInstance();
    }

    @When("I execute the update query {string}")
    public void iExecuteTheUpdateQuery(String query) {
        try {
            int count = ctx().getCurrentConnection().executeUpdate(query);
            ctx().setUpdateCount(count);
            ctx().setLastException(null);
        } catch (Exception e) {
            ctx().setLastException(e);
        }
    }

    @When("I execute the prepared update {string} with parameters:")
    public void iExecuteThePreparedUpdateWithParameters(String query, List<String> parameters) {
        try {
            int count = ctx().getCurrentConnection().executePreparedUpdate(query, parameters.toArray());
            ctx().setUpdateCount(count);
            ctx().setLastException(null);
        } catch (Exception e) {
            ctx().setLastException(e);
        }
    }

    @Then("the update should affect {int} row(s)")
    public void theUpdateShouldAffectRows(int expectedCount) {
        assertEquals(expectedCount, ctx().getUpdateCount(),
                "Expected " + expectedCount + " rows affected but got " + ctx().getUpdateCount());
    }

    @Then("the update should execute successfully")
    public void theUpdateShouldExecuteSuccessfully() {
        assertNull(ctx().getLastException(), "Update should execute without exception");
    }
}
