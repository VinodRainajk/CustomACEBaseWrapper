package com.qa.framework.payload;


import com.acebase.context.TestContext;
import com.acebase.steps.Steps;
import io.cucumber.java.en.Given;

/**
 * Sets scenario variables for {@code $var:name} placeholders.
 */
public class VariableStepDefinitions extends Steps {

    public VariableStepDefinitions(TestContext<?> testContext) {
        super(testContext);
    }


    @Given("variable {string} is {string}")
    public void variableIs(String name, String value) {
        ScenarioVariableStore.set(name, value);
    }
}
