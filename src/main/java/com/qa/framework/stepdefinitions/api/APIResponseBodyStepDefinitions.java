package com.qa.framework.stepdefinitions.api;

import com.qa.framework.exceptions.WrapperException;
import com.qa.framework.utils.DynamicValueUtils;
import com.qa.framework.utils.PollingUtils;
import io.cucumber.java.en.Then;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import static org.junit.jupiter.api.Assertions.*;
import static io.restassured.RestAssured.given;

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
        String resolvedExpected = DynamicValueUtils.resolveTokens(expectedText);
        assertTrue(body != null && body.contains(resolvedExpected),
                "Response body should contain: " + resolvedExpected);
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
        String resolvedExpected = DynamicValueUtils.resolveTokens(expectedValue);
        assertEquals(resolvedExpected, String.valueOf(value), "JSON path " + jsonPath + " value mismatch");
    }

    @Then("the response JSON path {string} should equal {int}")
    public void theResponseJsonPathShouldEqualInt(String jsonPath, int expectedValue) {
        assertNotNull(ctx().getLastResponse(), "Response should not be null");
        Object value = ctx().getLastResponse().jsonPath().get(jsonPath);
        assertEquals(expectedValue, value instanceof Number ? ((Number) value).intValue() : Integer.parseInt(String.valueOf(value)),
                "JSON path " + jsonPath + " value mismatch");
    }

    @Then("I wait up to {int} seconds for the last API response to have {string} as {string}, checking every {int} milliseconds")
    public void iWaitUpToSecondsForTheLastApiResponseToHaveAsCheckingEveryMilliseconds(
            int timeoutSeconds,
            String jsonPath,
            String expectedValue,
            int intervalMillis
    ) {
        APIStepContext.LastApiRequest last = ctx().getLastApiRequest();
        if (last == null) {
            throw new WrapperException("No previous API request found for polling.");
        }
        if (!"GET".equalsIgnoreCase(last.getMethod())) {
            throw new WrapperException("Polling replay supports GET only. Last request method was " + last.getMethod()
                    + ". Use a GET endpoint for polling.");
        }
        String resolvedExpected = DynamicValueUtils.resolveTokens(expectedValue);

        PollingUtils.pollUntil(
                timeoutSeconds,
                intervalMillis,
                "last API response jsonPath [" + jsonPath + "] to equal [" + resolvedExpected + "]",
                () -> {
                    Response replay = given().when().get(buildUrl(last.getEndpoint()));
                    ctx().setLastResponse(replay);
                    Object value = replay.jsonPath().get(jsonPath);
                    String actual = String.valueOf(value);
                    boolean matched = resolvedExpected.equals(actual);
                    return PollingUtils.PollOutcome.of(matched, "jsonPath=" + jsonPath + ", actual=" + actual);
                }
        );
    }

    private String buildUrl(String endpoint) {
        String base = ctx().getBaseUrl();
        if (base == null) {
            base = "";
        }
        String path = endpoint.startsWith("/") ? endpoint : "/" + endpoint;
        return base + path;
    }
}
