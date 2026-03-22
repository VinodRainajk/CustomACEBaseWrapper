package com.qa.framework.stepdefinitions.db;

import com.qa.framework.db.PendingStatementExecutor;
import com.qa.framework.payload.FeaturePayloadLoader;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Step definitions for DELETE operations.
 */
public class DatabaseDeleteStepDefinitions {

    private DatabaseStepContext ctx() {
        return DatabaseStepContext.getInstance();
    }

    /** Uses SQL set via {@code When I set the SQL statement from feature payload "..."}. */
    @When("I execute the delete")
    public void iExecuteTheDeleteUsingPendingStatement() {
        PendingStatementExecutor.executePendingStatement(ctx(), "delete", null);
    }

    @When("I execute the delete query from feature payload {string}")
    public void iExecuteTheDeleteQueryFromFeaturePayload(String payloadKey) {
        iExecuteTheDeleteQuery(FeaturePayloadLoader.getString(payloadKey));
    }

    @When("I execute the delete query {string}")
    public void iExecuteTheDeleteQuery(String query) {
        try {
            int count = ctx().getCurrentConnection().executeUpdate(query);
            ctx().setDeleteCount(count);
            ctx().setUpdateCount(count);
            ctx().setLastException(null);
        } catch (Exception e) {
            ctx().setLastException(e);
        }
    }

    @When("I execute the prepared delete {string} with parameters:")
    public void iExecuteThePreparedDeleteWithParameters(String query, List<String> parameters) {
        try {
            int count = ctx().getCurrentConnection().executePreparedUpdate(query, parameters.toArray());
            ctx().setDeleteCount(count);
            ctx().setUpdateCount(count);
            ctx().setLastException(null);
        } catch (Exception e) {
            ctx().setLastException(e);
        }
    }

    @When("I truncate the table {string}")
    public void iTruncateTheTable(String tableName) {
        try {
            int count = ctx().getCurrentConnection().executeUpdate("TRUNCATE TABLE " + tableName);
            ctx().setDeleteCount(count);
            ctx().setUpdateCount(count);
            ctx().setLastException(null);
        } catch (Exception e) {
            ctx().setLastException(e);
        }
    }

    @Then("the delete should affect {int} row(s)")
    public void theDeleteShouldAffectRows(int expectedCount) {
        assertEquals(expectedCount, ctx().getDeleteCount(),
                "Expected " + expectedCount + " rows deleted but got " + ctx().getDeleteCount());
    }

    @Then("the delete should execute successfully")
    public void theDeleteShouldExecuteSuccessfully() {
        assertNull(ctx().getLastException(), "Delete should execute without exception");
    }

    @Then("the delete should succeed")
    public void theDeleteShouldSucceed() {
        theDeleteShouldExecuteSuccessfully();
    }
}
