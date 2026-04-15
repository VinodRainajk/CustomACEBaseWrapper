package com.qa.framework.payload;

import com.qa.framework.utils.PollingUtils;
import io.cucumber.java.en.Then;

/**
 * Generic wait steps available to both API and DB glue.
 */
public class WaitStepDefinitions {

    @Then("I wait for {int} seconds before next step")
    public void iWaitForSecondsBeforeNextStep(int seconds) {
        PollingUtils.sleepSeconds(seconds);
    }
}
