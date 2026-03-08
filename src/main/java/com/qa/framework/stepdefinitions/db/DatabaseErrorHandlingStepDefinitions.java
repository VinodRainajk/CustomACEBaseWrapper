package com.qa.framework.stepdefinitions.db;

import io.cucumber.java.en.Then;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Step definitions for error handling.
 */
public class DatabaseErrorHandlingStepDefinitions {

    private DatabaseStepContext ctx() {
        return DatabaseStepContext.getInstance();
    }

    @Then("the query should fail with an error")
    public void theQueryShouldFailWithAnError() {
        assertNotNull(ctx().getLastException(), "Query should have thrown an exception");
    }

    @Then("the query should fail with error message containing {string}")
    public void theQueryShouldFailWithErrorMessageContaining(String expectedMessage) {
        assertNotNull(ctx().getLastException(), "Query should have thrown an exception");
        String actualMessage = ctx().getLastException().getMessage();
        assertTrue(actualMessage != null && actualMessage.contains(expectedMessage));
    }

    @Then("the operation should fail with an error")
    public void theOperationShouldFailWithAnError() {
        assertNotNull(ctx().getLastException(), "Operation should have thrown an exception");
    }
}
