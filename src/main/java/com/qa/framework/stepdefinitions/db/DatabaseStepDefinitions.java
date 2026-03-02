package com.qa.framework.stepdefinitions.db;

import com.qa.framework.config.ConfigurationManager;
import com.qa.framework.config.DatabaseConfig;
import com.qa.framework.db.DatabaseConnection;
import com.qa.framework.db.DatabaseConnectionFactory;
import com.qa.framework.db.DatabaseManager;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Step definitions for database operations in Cucumber tests.
 * This class is part of the library JAR and will be discovered by Cucumber
 * when imported as a dependency.
 */
public class DatabaseStepDefinitions {
    
    private DatabaseManager dbManager;
    private ConfigurationManager configManager;
    private DatabaseConnection currentConnection;
    private List<Map<String, Object>> queryResults;
    private int updateCount;
    private Exception lastException;
    
    @Before("@DB")
    public void setUp() {
        dbManager = DatabaseManager.getInstance();
        configManager = ConfigurationManager.getInstance();
    }
    
    @After("@DB")
    public void tearDown() {
        if (dbManager != null) {
            dbManager.closeAllConnections();
        }
    }
    
    @Given("I have a database connection named {string} with URL {string} username {string} and password {string}")
    public void iHaveADatabaseConnection(String connectionName, String url, String username, String password) {
        dbManager.addConnection(connectionName, url, username, password);
        currentConnection = dbManager.getConnection(connectionName);
        assertNotNull(currentConnection, "Database connection should be created");
    }
    
    @Given("I have a database connection named {string} using profile {string}")
    public void iHaveADatabaseConnectionUsingProfile(String connectionName, String profile) {
        DatabaseConnection connection = DatabaseConnectionFactory.createConnectionFromProfile(profile);
        dbManager.addConnection(connectionName, connection);
        currentConnection = connection;
        assertNotNull(currentConnection, "Database connection should be created from profile: " + profile);
    }
    
    @Given("I set the active database profile to {string}")
    public void iSetTheActiveDatabaseProfileTo(String profile) {
        configManager.setActiveProfile(profile);
        assertEquals(profile, configManager.getActiveProfile(), "Active profile should be set to: " + profile);
    }
    
    @Given("I connect to the database {string}")
    public void iConnectToTheDatabase(String connectionName) {
        currentConnection = dbManager.getConnection(connectionName);
        assertNotNull(currentConnection, "Database connection '" + connectionName + "' should exist");
        currentConnection.connect();
        assertTrue(currentConnection.isConnected(), "Should be connected to database");
    }
    
    @Given("I connect to database using the active profile")
    public void iConnectToTheDatabaseUsingActiveProfile() {
        currentConnection = DatabaseConnectionFactory.createAndConnect();
        assertNotNull(currentConnection, "Database connection should be created from active profile");
        assertTrue(currentConnection.isConnected(), "Should be connected to database");
    }
    
    @When("I execute the query {string}")
    public void iExecuteTheQuery(String query) {
        try {
            queryResults = currentConnection.executeQuery(query);
            lastException = null;
        } catch (Exception e) {
            lastException = e;
        }
    }
    
    @When("I execute the update query {string}")
    public void iExecuteTheUpdateQuery(String query) {
        try {
            updateCount = currentConnection.executeUpdate(query);
            lastException = null;
        } catch (Exception e) {
            lastException = e;
        }
    }
    
    @When("I execute the prepared query {string} with parameters:")
    public void iExecuteThePreparedQueryWithParameters(String query, List<String> parameters) {
        try {
            queryResults = currentConnection.executePreparedQuery(query, parameters.toArray());
            lastException = null;
        } catch (Exception e) {
            lastException = e;
        }
    }
    
    @Then("the query should return {int} row(s)")
    public void theQueryShouldReturnRows(int expectedRowCount) {
        assertNotNull(queryResults, "Query results should not be null");
        assertEquals(expectedRowCount, queryResults.size(), 
            "Expected " + expectedRowCount + " rows but got " + queryResults.size());
    }
    
    @Then("the query should return at least {int} row(s)")
    public void theQueryShouldReturnAtLeastRows(int minRowCount) {
        assertNotNull(queryResults, "Query results should not be null");
        assertTrue(queryResults.size() >= minRowCount, 
            "Expected at least " + minRowCount + " rows but got " + queryResults.size());
    }
    
    @Then("the first row should contain column {string} with value {string}")
    public void theFirstRowShouldContainColumnWithValue(String columnName, String expectedValue) {
        assertNotNull(queryResults, "Query results should not be null");
        assertFalse(queryResults.isEmpty(), "Query results should not be empty");
        
        Map<String, Object> firstRow = queryResults.get(0);
        assertTrue(firstRow.containsKey(columnName), "Column '" + columnName + "' should exist");
        assertEquals(expectedValue, String.valueOf(firstRow.get(columnName)), 
            "Column '" + columnName + "' value mismatch");
    }
    
    @Then("the first row should contain column {string} with value greater than {string}")
    public void theFirstRowShouldContainColumnWithValueGreaterThan(String columnName, String minValue) {
        assertNotNull(queryResults, "Query results should not be null");
        assertFalse(queryResults.isEmpty(), "Query results should not be empty");
        
        Map<String, Object> firstRow = queryResults.get(0);
        assertTrue(firstRow.containsKey(columnName), "Column '" + columnName + "' should exist");
        
        Object value = firstRow.get(columnName);
        assertNotNull(value, "Column '" + columnName + "' value should not be null");
        
        // Convert both values to long for comparison
        long actualValue = Long.parseLong(String.valueOf(value));
        long expectedMinValue = Long.parseLong(minValue);
        
        assertTrue(actualValue > expectedMinValue, 
            "Column '" + columnName + "' value (" + actualValue + ") should be greater than " + expectedMinValue);
    }
    
    @Then("the first row should contain column {string} with value less than {string}")
    public void theFirstRowShouldContainColumnWithValueLessThan(String columnName, String maxValue) {
        assertNotNull(queryResults, "Query results should not be null");
        assertFalse(queryResults.isEmpty(), "Query results should not be empty");
        
        Map<String, Object> firstRow = queryResults.get(0);
        assertTrue(firstRow.containsKey(columnName), "Column '" + columnName + "' should exist");
        
        Object value = firstRow.get(columnName);
        assertNotNull(value, "Column '" + columnName + "' value should not be null");
        
        // Convert both values to long for comparison
        long actualValue = Long.parseLong(String.valueOf(value));
        long expectedMaxValue = Long.parseLong(maxValue);
        
        assertTrue(actualValue < expectedMaxValue, 
            "Column '" + columnName + "' value (" + actualValue + ") should be less than " + expectedMaxValue);
    }
    
    @Then("the first row should contain column {string} with value between {string} and {string}")
    public void theFirstRowShouldContainColumnWithValueBetween(String columnName, String minValue, String maxValue) {
        assertNotNull(queryResults, "Query results should not be null");
        assertFalse(queryResults.isEmpty(), "Query results should not be empty");
        
        Map<String, Object> firstRow = queryResults.get(0);
        assertTrue(firstRow.containsKey(columnName), "Column '" + columnName + "' should exist");
        
        Object value = firstRow.get(columnName);
        assertNotNull(value, "Column '" + columnName + "' value should not be null");
        
        // Convert all values to long for comparison
        long actualValue = Long.parseLong(String.valueOf(value));
        long expectedMinValue = Long.parseLong(minValue);
        long expectedMaxValue = Long.parseLong(maxValue);
        
        assertTrue(actualValue >= expectedMinValue && actualValue <= expectedMaxValue, 
            "Column '" + columnName + "' value (" + actualValue + ") should be between " + 
            expectedMinValue + " and " + expectedMaxValue);
    }
    
    @Then("the update should affect {int} row(s)")
    public void theUpdateShouldAffectRows(int expectedCount) {
        assertEquals(expectedCount, updateCount, 
            "Expected " + expectedCount + " rows affected but got " + updateCount);
    }
    
    @Then("the query should execute successfully")
    public void theQueryShouldExecuteSuccessfully() {
        assertNull(lastException, "Query should execute without exception");
        assertNotNull(queryResults, "Query results should not be null");
    }
    
    @Then("the query should fail with an error")
    public void theQueryShouldFailWithAnError() {
        assertNotNull(lastException, "Query should have thrown an exception");
    }
    
    @Then("I should be connected to the database")
    public void iShouldBeConnectedToTheDatabase() {
        assertNotNull(currentConnection, "Database connection should exist");
        assertTrue(currentConnection.isConnected(), "Should be connected to database");
    }
    
    @When("I disconnect from the database")
    public void iDisconnectFromTheDatabase() {
        if (currentConnection != null) {
            currentConnection.disconnect();
        }
    }
    
    @Then("I should not be connected to the database")
    public void iShouldNotBeConnectedToTheDatabase() {
        if (currentConnection != null) {
            assertFalse(currentConnection.isConnected(), "Should not be connected to database");
        }
    }
    
    @Then("the result set should contain a column {string}")
    public void theResultSetShouldContainAColumn(String columnName) {
        assertNotNull(queryResults, "Query results should not be null");
        assertFalse(queryResults.isEmpty(), "Query results should not be empty");
        
        Map<String, Object> firstRow = queryResults.get(0);
        assertTrue(firstRow.containsKey(columnName), 
            "Result set should contain column '" + columnName + "'");
    }
    
    @Then("all rows should have column {string} not null")
    public void allRowsShouldHaveColumnNotNull(String columnName) {
        assertNotNull(queryResults, "Query results should not be null");
        assertFalse(queryResults.isEmpty(), "Query results should not be empty");
        
        for (Map<String, Object> row : queryResults) {
            assertTrue(row.containsKey(columnName), "Column '" + columnName + "' should exist");
            assertNotNull(row.get(columnName), "Column '" + columnName + "' should not be null");
        }
    }
}
