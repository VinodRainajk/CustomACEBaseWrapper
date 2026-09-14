package com.qa.framework.payload;


import com.acebase.context.TestContext;
import com.acebase.steps.Steps;
import com.qa.framework.stepdefinitions.api.APIStepContext;
import io.cucumber.java.en.When;

/**
 * Sends a curl-like API payload defined under {@code operations.<id>} with {@code type: API}.
 */
public class ApiPayloadStepDefinitions extends Steps {

    public ApiPayloadStepDefinitions(TestContext<?> testContext) {
        super(testContext);
    }


    private APIStepContext ctx() {
        return APIStepContext.getInstance();
    }

    @When("I send API payload {string}")
    public void iSendApiPayload(String operationId) {
        try {
            ApiPayloadExecutor.execute(operationId);
        } catch (Exception e) {
            ctx().setLastException(e);
        }
    }
}
