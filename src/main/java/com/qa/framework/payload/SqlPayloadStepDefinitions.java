package com.qa.framework.payload;


import com.acebase.context.TestContext;
import com.acebase.steps.Steps;
import io.cucumber.java.en.When;

/**
 * Runs SQL from {@code operations.<id>} with {@code type: SQL} on a named database connection.
 */
public class SqlPayloadStepDefinitions extends Steps {

    public SqlPayloadStepDefinitions(TestContext<?> testContext) {
        super(testContext);
    }


    @When("I run SQL payload {string} on {string}")
    public void iRunSqlPayloadOn(String operationId, String connectionName) {
        SqlPayloadExecutor.execute(operationId, connectionName);
    }
}
