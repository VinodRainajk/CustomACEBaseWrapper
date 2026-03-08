package com.qa.framework.stepdefinitions.api;

import io.cucumber.java.en.Then;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Step definitions for response status code assertions.
 */
public class APIResponseStatusStepDefinitions {

    private APIStepContext ctx() {
        return APIStepContext.getInstance();
    }

    @Then("the response status code should be {int}")
    public void theResponseStatusCodeShouldBe(int expectedStatus) {
        assertNotNull(ctx().getLastResponse(), "Response should not be null");
        assertEquals(expectedStatus, ctx().getLastResponse().getStatusCode(),
                "Status code mismatch");
    }

    @Then("the response status code should be success")
    public void theResponseStatusCodeShouldBeSuccess() {
        assertNotNull(ctx().getLastResponse(), "Response should not be null");
        int code = ctx().getLastResponse().getStatusCode();
        assertTrue(code >= 200 && code < 300, "Expected 2xx but got " + code);
    }

    @Then("the response status code should be client error")
    public void theResponseStatusCodeShouldBeClientError() {
        assertNotNull(ctx().getLastResponse(), "Response should not be null");
        int code = ctx().getLastResponse().getStatusCode();
        assertTrue(code >= 400 && code < 500, "Expected 4xx but got " + code);
    }

    @Then("the response status code should be server error")
    public void theResponseStatusCodeShouldBeServerError() {
        assertNotNull(ctx().getLastResponse(), "Response should not be null");
        int code = ctx().getLastResponse().getStatusCode();
        assertTrue(code >= 500 && code < 600, "Expected 5xx but got " + code);
    }
}
