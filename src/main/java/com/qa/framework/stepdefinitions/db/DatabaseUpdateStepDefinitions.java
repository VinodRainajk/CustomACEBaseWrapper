package com.qa.framework.stepdefinitions.db;

import com.qa.framework.db.PendingStatementExecutor;
import com.qa.framework.payload.FeaturePayloadLoader;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Step definitions for UPDATE operations.
 */
public class DatabaseUpdateStepDefinitions {

    private DatabaseStepContext ctx() {
        return DatabaseStepContext.getInstance();
    }

    /** Uses SQL set via {@code When I set the SQL statement from feature payload "..."}. */
    @When("I execute the update")
    public void iExecuteTheUpdateUsingPendingStatement() {
        PendingStatementExecutor.executePendingStatement(ctx(), "update", null);
    }

    @When("I execute the update query from feature payload {string}")
    public void iExecuteTheUpdateQueryFromFeaturePayload(String payloadKey) {
        iExecuteTheUpdateQuery(FeaturePayloadLoader.getString(payloadKey));
    }

    @When("I execute the update query {string}")
    public void iExecuteTheUpdateQuery(String query) {
        try {
            String resolved = FeaturePayloadLoader.resolveBracedPayloadOrLiteral(query);
            int count = ctx().getCurrentConnection().executeUpdate(resolved);
            ctx().setUpdateCount(count);
            ctx().setLastException(null);
        } catch (Exception e) {
            ctx().setLastException(e);
        }
    }

    @When("I execute the prepared update {string} with parameters:")
    public void iExecuteThePreparedUpdateWithParameters(String query, List<String> parameters) {
        try {
            int count = ctx().getCurrentConnection().executePreparedUpdate(query, parameters.toArray());
            ctx().setUpdateCount(count);
            ctx().setLastException(null);
        } catch (Exception e) {
            ctx().setLastException(e);
        }
    }

    @Then("the update should affect {int} row(s)")
    public void theUpdateShouldAffectRows(int expectedCount) {
        assertEquals(expectedCount, ctx().getUpdateCount(),
                "Expected " + expectedCount + " rows affected but got " + ctx().getUpdateCount());
    }

    @Then("the update should execute successfully")
    public void theUpdateShouldExecuteSuccessfully() {
        assertNull(ctx().getLastException(), "Update should execute without exception");
    }

    @Then("the update should succeed")
    public void theUpdateShouldSucceed() {
        theUpdateShouldExecuteSuccessfully();
    }
}
