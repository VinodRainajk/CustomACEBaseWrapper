package com.qa.framework.stepdefinitions.db;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Step definitions for database functions.
 * Functions are typically called via SELECT, e.g. SELECT my_function(?) FROM DUAL
 */
public class DatabaseFunctionStepDefinitions {

    private DatabaseStepContext ctx() {
        return DatabaseStepContext.getInstance();
    }

    @When("I call the function using query {string}")
    public void iCallTheFunctionUsingQuery(String query) {
        try {
            ctx().setQueryResults(ctx().getCurrentConnection().executeQuery(query));
            ctx().setLastException(null);
        } catch (Exception e) {
            ctx().setLastException(e);
        }
    }

    @When("I call the function {string} with parameters {string}")
    public void iCallTheFunctionWithParameters(String functionCall, String paramList) {
        try {
            String query = "SELECT " + functionCall + " as result FROM DUAL";
            ctx().setQueryResults(ctx().getCurrentConnection().executeQuery(query));
            ctx().setLastException(null);
        } catch (Exception e) {
            ctx().setLastException(e);
        }
    }

    @Then("the function should return value {string}")
    public void theFunctionShouldReturnValue(String expectedValue) {
        assertNotNull(ctx().getQueryResults(), "Query results should not be null");
        assertFalse(ctx().getQueryResults().isEmpty(), "Query results should not be empty");
        Object result = ctx().getQueryResults().get(0).get("result");
        if (result == null) {
            result = ctx().getQueryResults().get(0).get("RESULT");
        }
        assertEquals(expectedValue, String.valueOf(result), "Function return value mismatch");
    }
}
