package com.qa.framework.stepdefinitions.db;

import io.cucumber.java.en.Then;

import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Step definitions for data validation.
 */
public class DatabaseValidationStepDefinitions {

    private DatabaseStepContext ctx() {
        return DatabaseStepContext.getInstance();
    }

    @Then("the first row column {string} should match pattern {string}")
    public void theFirstRowColumnShouldMatchPattern(String columnName, String regex) {
        assertNotNull(ctx().getQueryResults(), "Query results should not be null");
        assertFalse(ctx().getQueryResults().isEmpty(), "Query results should not be empty");
        Map<String, Object> firstRow = ctx().getQueryResults().get(0);
        assertTrue(firstRow.containsKey(columnName), "Column '" + columnName + "' should exist");
        String value = String.valueOf(firstRow.get(columnName));
        assertTrue(Pattern.matches(regex, value), "Column '" + columnName + "' value '" + value + "' should match pattern " + regex);
    }

    @Then("the first row column {string} should be one of {string}")
    public void theFirstRowColumnShouldBeOneOf(String columnName, String allowedValues) {
        assertNotNull(ctx().getQueryResults(), "Query results should not be null");
        assertFalse(ctx().getQueryResults().isEmpty(), "Query results should not be empty");
        Map<String, Object> firstRow = ctx().getQueryResults().get(0);
        assertTrue(firstRow.containsKey(columnName), "Column '" + columnName + "' should exist");
        String actual = String.valueOf(firstRow.get(columnName));
        String[] allowed = allowedValues.split(",\\s*");
        boolean found = false;
        for (String a : allowed) {
            if (actual.equals(a.trim())) {
                found = true;
                break;
            }
        }
        assertTrue(found, "Column '" + columnName + "' value '" + actual + "' should be one of: " + allowedValues);
    }
}
