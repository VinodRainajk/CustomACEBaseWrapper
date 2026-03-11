package com.qa.framework.runners;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

/**
 * Base Test Runner - Routes to appropriate step definitions based on tags.
 * This is the main interceptor/router for all test types.
 * Uses @SelectClasspathResource for features (do not set cucumber.features - it overrides JUnit selectors).
 *
 * Supports multiple test types:
 * - @DB - Database tests (routes to com.qa.framework.stepdefinitions.db)
 * - @UI - UI tests (routes to your UI library step definitions)
 * - @API - API tests (routes to your API library step definitions)
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(
    key = GLUE_PROPERTY_NAME, 
    value = "com.qa.framework.stepdefinitions.db," +
            "com.qa.framework.stepdefinitions.ui," +
            "com.qa.framework.stepdefinitions.api"
)
@ConfigurationParameter(
    key = PLUGIN_PROPERTY_NAME, 
    value = "pretty, " +
            "html:target/cucumber-reports/cucumber.html, " +
            "json:target/cucumber-reports/cucumber.json, " +
            "junit:target/cucumber-reports/cucumber.xml"
)
public class BaseTestRunner {
    // This is an empty class - annotations do all the work
    // Cucumber will automatically discover step definitions from all glue paths
}
