package com.qa.framework.runners;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.FILTER_TAGS_PROPERTY_NAME;

/**
 * Database Test Runner - Executes only @DB tagged scenarios.
 * Routes to database step definitions.
 * Uses @SelectClasspathResource for features (do not set cucumber.features - it overrides JUnit selectors).
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.qa.framework.stepdefinitions.db")
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "@DB")
@ConfigurationParameter(
    key = PLUGIN_PROPERTY_NAME, 
    value = "pretty, " +
            "html:target/cucumber-reports/db-tests.html, " +
            "json:target/cucumber-reports/db-tests.json, " +
            "junit:target/cucumber-reports/db-tests.xml"
)
public class DBTestRunner {
}
