package com.qa.framework.stepdefinitions.api;

import io.cucumber.java.en.Then;
import io.restassured.path.json.JsonPath;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Step definitions for response body assertions.
 */
public class APIResponseBodyStepDefinitions {

    private APIStepContext ctx() {
        return APIStepContext.getInstance();
    }

    @Then("the response body should contain {string}")
    public void theResponseBodyShouldContain(String expectedText) {
        assertNotNull(ctx().getLastResponse(), "Response should not be null");
        String body = ctx().getLastResponse().getBody().asString();
        assertTrue(body != null && body.contains(expectedText),
                "Response body should contain: " + expectedText);
    }

    @Then("the response JSON path {string} should exist")
    public void theResponseJsonPathShouldExist(String jsonPath) {
        assertNotNull(ctx().getLastResponse(), "Response should not be null");
        JsonPath jp = ctx().getLastResponse().jsonPath();
        Object value = jp.get(jsonPath);
        assertNotNull(value, "JSON path " + jsonPath + " should exist");
    }

    @Then("the response JSON path {string} should equal {string}")
    public void theResponseJsonPathShouldEqual(String jsonPath, String expectedValue) {
        assertNotNull(ctx().getLastResponse(), "Response should not be null");
        Object value = ctx().getLastResponse().jsonPath().get(jsonPath);
        assertEquals(expectedValue, String.valueOf(value), "JSON path " + jsonPath + " value mismatch");
    }

    @Then("the response JSON path {string} should equal {int}")
    public void theResponseJsonPathShouldEqualInt(String jsonPath, int expectedValue) {
        assertNotNull(ctx().getLastResponse(), "Response should not be null");
        Object value = ctx().getLastResponse().jsonPath().get(jsonPath);
        assertEquals(expectedValue, value instanceof Number ? ((Number) value).intValue() : Integer.parseInt(String.valueOf(value)),
                "JSON path " + jsonPath + " value mismatch");
    }
}
