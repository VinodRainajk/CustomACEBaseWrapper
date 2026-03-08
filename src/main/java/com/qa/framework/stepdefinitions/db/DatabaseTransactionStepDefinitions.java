package com.qa.framework.stepdefinitions.db;

import io.cucumber.java.en.When;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Step definitions for transaction management.
 */
public class DatabaseTransactionStepDefinitions {

    private DatabaseStepContext ctx() {
        return DatabaseStepContext.getInstance();
    }

    @When("I begin a transaction")
    public void iBeginATransaction() {
        try {
            Connection conn = ctx().getCurrentConnection().getConnection();
            assertNotNull(conn, "Database connection should exist");
            conn.setAutoCommit(false);
        } catch (SQLException e) {
            fail("Failed to begin transaction: " + e.getMessage());
        }
    }

    @When("I commit the transaction")
    public void iCommitTheTransaction() {
        try {
            Connection conn = ctx().getCurrentConnection().getConnection();
            assertNotNull(conn, "Database connection should exist");
            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            fail("Failed to commit transaction: " + e.getMessage());
        }
    }

    @When("I rollback the transaction")
    public void iRollbackTheTransaction() {
        try {
            Connection conn = ctx().getCurrentConnection().getConnection();
            assertNotNull(conn, "Database connection should exist");
            conn.rollback();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            fail("Failed to rollback transaction: " + e.getMessage());
        }
    }
}
