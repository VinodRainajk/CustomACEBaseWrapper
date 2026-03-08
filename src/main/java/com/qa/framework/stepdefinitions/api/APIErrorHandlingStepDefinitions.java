package com.qa.framework.stepdefinitions.api;

import io.cucumber.java.en.Then;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Step definitions for API error handling.
 */
public class APIErrorHandlingStepDefinitions {

    private APIStepContext ctx() {
        return APIStepContext.getInstance();
    }

    @Then("the API request should have failed")
    public void theApiRequestShouldHaveFailed() {
        assertNotNull(ctx().getLastException(), "Expected request to have thrown an exception");
    }
}
