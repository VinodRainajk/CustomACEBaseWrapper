package com.qa.framework.stepdefinitions.db;

import com.qa.framework.db.DatabaseConfigLoader;
import com.qa.framework.db.DatabaseConnection;
import com.qa.framework.db.DatabaseConnectionFactory;
import com.qa.framework.db.DatabaseManager;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Step definitions for database connection management.
 * Supports YAML config (master + feature + section) via config name in steps.
 */
public class DatabaseConnectionStepDefinitions {

    private DatabaseStepContext ctx() {
        return DatabaseStepContext.getInstance();
    }

    @Given("I connect to database {string}")
    public void iConnectToDatabase(String configName) {
        iConnectToDatabaseAs(configName, configName);
    }

    @Given("I connect to database {string} as {string}")
    public void iConnectToDatabaseAs(String configName, String connectionName) {
        Map<String, Object> config = DatabaseConfigLoader.resolveConfig(
                configName,
                ctx().getFeatureName(),
                ctx().getScenarioName()
        );
        DatabaseConnection connection = DatabaseConfigLoader.createConnectionFromResolvedConfig(config);
        connection.connect();
        ctx().getDbManager().addConnection(connectionName, connection);
        ctx().setCurrentConnection(connection);
        assertNotNull(ctx().getCurrentConnection(), "Database connection should be created for config: " + configName);
        assertTrue(ctx().getCurrentConnection().isConnected(), "Should be connected to database");
    }

    @Given("I have a database connection named {string} with URL {string} username {string} and password {string}")
    public void iHaveADatabaseConnection(String connectionName, String url, String username, String password) {
        ctx().getDbManager().addConnection(connectionName, url, username, password);
        ctx().setCurrentConnection(ctx().getDbManager().getConnection(connectionName));
        assertNotNull(ctx().getCurrentConnection(), "Database connection should be created");
    }

    @Given("I have a database connection named {string} using profile {string}")
    public void iHaveADatabaseConnectionUsingProfile(String connectionName, String profile) {
        DatabaseConnection connection = DatabaseConnectionFactory.createConnectionFromProfile(profile);
        ctx().getDbManager().addConnection(connectionName, connection);
        ctx().setCurrentConnection(connection);
        assertNotNull(ctx().getCurrentConnection(), "Database connection should be created from profile: " + profile);
    }

    @Given("I set the active database profile to {string}")
    public void iSetTheActiveDatabaseProfileTo(String profile) {
        ctx().getConfigManager().setActiveProfile(profile);
        assertEquals(profile, ctx().getConfigManager().getActiveProfile(), "Active profile should be set to: " + profile);
    }

    @Given("I connect to the database {string}")
    public void iConnectToTheDatabase(String connectionName) {
        ctx().setCurrentConnection(ctx().getDbManager().getConnection(connectionName));
        assertNotNull(ctx().getCurrentConnection(), "Database connection '" + connectionName + "' should exist");
        ctx().getCurrentConnection().connect();
        assertTrue(ctx().getCurrentConnection().isConnected(), "Should be connected to database");
    }

    @Given("I connect to database using the active profile")
    public void iConnectToTheDatabaseUsingActiveProfile() {
        ctx().setCurrentConnection(DatabaseConnectionFactory.createAndConnect());
        assertNotNull(ctx().getCurrentConnection(), "Database connection should be created from active profile");
        assertTrue(ctx().getCurrentConnection().isConnected(), "Should be connected to database");
    }

    @When("I disconnect from the database")
    public void iDisconnectFromTheDatabase() {
        if (ctx().getCurrentConnection() != null) {
            ctx().getCurrentConnection().disconnect();
        }
    }

    @Then("I should be connected to the database")
    public void iShouldBeConnectedToTheDatabase() {
        assertNotNull(ctx().getCurrentConnection(), "Database connection should exist");
        assertTrue(ctx().getCurrentConnection().isConnected(), "Should be connected to database");
    }

    @Then("I should not be connected to the database")
    public void iShouldNotBeConnectedToTheDatabase() {
        if (ctx().getCurrentConnection() != null) {
            assertFalse(ctx().getCurrentConnection().isConnected(), "Should not be connected to database");
        }
    }
}
