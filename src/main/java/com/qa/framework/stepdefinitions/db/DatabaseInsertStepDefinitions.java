package com.qa.framework.stepdefinitions.db;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Step definitions for INSERT operations.
 */
public class DatabaseInsertStepDefinitions {

    private DatabaseStepContext ctx() {
        return DatabaseStepContext.getInstance();
    }

    @When("I execute the insert query {string}")
    public void iExecuteTheInsertQuery(String query) {
        try {
            int count = ctx().getCurrentConnection().executeUpdate(query);
            ctx().setInsertCount(count);
            ctx().setUpdateCount(count);
            ctx().setLastException(null);
        } catch (Exception e) {
            ctx().setLastException(e);
        }
    }

    @When("I execute the prepared insert {string} with parameters:")
    public void iExecuteThePreparedInsertWithParameters(String query, List<String> parameters) {
        try {
            int count = ctx().getCurrentConnection().executePreparedUpdate(query, parameters.toArray());
            ctx().setInsertCount(count);
            ctx().setUpdateCount(count);
            ctx().setLastException(null);
        } catch (Exception e) {
            ctx().setLastException(e);
        }
    }

    @Then("the insert should affect {int} row(s)")
    public void theInsertShouldAffectRows(int expectedCount) {
        assertEquals(expectedCount, ctx().getInsertCount(),
                "Expected " + expectedCount + " rows inserted but got " + ctx().getInsertCount());
    }

    @Then("the insert should execute successfully")
    public void theInsertShouldExecuteSuccessfully() {
        assertNull(ctx().getLastException(), "Insert should execute without exception");
    }
}
