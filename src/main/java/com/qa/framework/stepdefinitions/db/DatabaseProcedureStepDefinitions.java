package com.qa.framework.stepdefinitions.db;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Step definitions for stored procedures.
 */
public class DatabaseProcedureStepDefinitions {

    private DatabaseStepContext ctx() {
        return DatabaseStepContext.getInstance();
    }

    @When("I execute the stored procedure {string}")
    public void iExecuteTheStoredProcedure(String procedureCall) {
        try {
            ctx().setQueryResults(ctx().getCurrentConnection().executeCallable(procedureCall));
            ctx().setLastException(null);
        } catch (Exception e) {
            ctx().setLastException(e);
        }
    }

    @When("I execute the stored procedure {string} with parameters:")
    public void iExecuteTheStoredProcedureWithParameters(String procedureCall, List<String> parameters) {
        try {
            ctx().setQueryResults(ctx().getCurrentConnection().executeCallable(procedureCall, parameters.toArray()));
            ctx().setLastException(null);
        } catch (Exception e) {
            ctx().setLastException(e);
        }
    }

    @Then("the procedure should execute successfully")
    public void theProcedureShouldExecuteSuccessfully() {
        assertNull(ctx().getLastException(), "Procedure should execute without exception");
    }

    @Then("the procedure should return {int} row(s)")
    public void theProcedureShouldReturnRows(int expectedRowCount) {
        assertNotNull(ctx().getQueryResults(), "Procedure results should not be null");
        assertEquals(expectedRowCount, ctx().getQueryResults().size(),
                "Expected " + expectedRowCount + " rows from procedure but got " + ctx().getQueryResults().size());
    }
}
