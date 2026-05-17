package com.qa.framework.payload;

import com.qa.framework.exceptions.WrapperException;
import com.qa.framework.stepdefinitions.api.APIStepContext;
import io.cucumber.java.en.Then;
import io.restassured.path.json.JsonPath;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Stores values from the last API response into {@code $var:name} for later payloads.
 */
public class ResponseVariableStepDefinitions {

    private APIStepContext ctx() {
        return APIStepContext.getInstance();
    }

    @Then("I store response {string} as variable {string}")
    public void iStoreResponseAsVariable(String jsonPath, String variableName) {
        assertNotNull(ctx().getLastResponse(), "No API response available to extract from");
        JsonPath jp = ctx().getLastResponse().jsonPath();
        Object value = jp.get(jsonPath);
        if (value == null) {
            throw new WrapperException("Response path not found: " + jsonPath);
        }
        ScenarioVariableStore.set(variableName, String.valueOf(value));
    }
}
