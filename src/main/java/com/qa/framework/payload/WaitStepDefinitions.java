package com.qa.framework.payload;


import com.acebase.context.TestContext;
import com.acebase.steps.Steps;
import com.qa.framework.utils.PollingUtils;
import io.cucumber.java.en.Then;

/**
 * Generic wait steps available to both API and DB glue.
 */
public class WaitStepDefinitions extends Steps {

    public WaitStepDefinitions(TestContext<?> testContext) {
        super(testContext);
    }


    @Then("I wait for {int} seconds before next step")
    public void iWaitForSecondsBeforeNextStep(int seconds) {
        PollingUtils.sleepSeconds(seconds);
    }
}
