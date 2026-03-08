package com.qa.framework.stepdefinitions.db;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Step definitions for Oracle-style packages.
 * Procedures in packages are called as: {call package_name.procedure_name(?, ?)}
 */
public class DatabasePackageStepDefinitions {

    private DatabaseStepContext ctx() {
        return DatabaseStepContext.getInstance();
    }

    @When("I execute the packaged procedure {string}")
    public void iExecuteThePackagedProcedure(String packageProcedureCall) {
        try {
            String callable = packageProcedureCall.contains("{call") ? packageProcedureCall : "{call " + packageProcedureCall + "}";
            ctx().setQueryResults(ctx().getCurrentConnection().executeCallable(callable));
            ctx().setLastException(null);
        } catch (Exception e) {
            ctx().setLastException(e);
        }
    }

    @When("I execute the packaged procedure {string} with parameters:")
    public void iExecuteThePackagedProcedureWithParameters(String packageProcedureCall, List<String> parameters) {
        try {
            String callable = packageProcedureCall.contains("{call") ? packageProcedureCall : "{call " + packageProcedureCall + "}";
            ctx().setQueryResults(ctx().getCurrentConnection().executeCallable(callable, parameters.toArray()));
            ctx().setLastException(null);
        } catch (Exception e) {
            ctx().setLastException(e);
        }
    }

    @Then("the packaged procedure should execute successfully")
    public void thePackagedProcedureShouldExecuteSuccessfully() {
        assertNull(ctx().getLastException(), "Packaged procedure should execute without exception");
    }
}
