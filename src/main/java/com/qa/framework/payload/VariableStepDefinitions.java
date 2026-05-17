package com.qa.framework.payload;

import io.cucumber.java.en.Given;

/**
 * Sets scenario variables for {@code $var:name} placeholders.
 */
public class VariableStepDefinitions {

    @Given("variable {string} is {string}")
    public void variableIs(String name, String value) {
        ScenarioVariableStore.set(name, value);
    }
}
