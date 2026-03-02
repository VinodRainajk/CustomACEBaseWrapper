package com.qa.framework.runners;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.FEATURES_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.FILTER_TAGS_PROPERTY_NAME;

/**
 * API Test Runner - Executes only @API tagged scenarios.
 * Routes to API step definitions (from your API library).
 * 
 * Note: Update GLUE_PROPERTY_NAME to point to your API library's step definitions package.
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = FEATURES_PROPERTY_NAME, value = "src/test/resources/features")
@ConfigurationParameter(
    key = GLUE_PROPERTY_NAME, 
    value = "com.qa.framework.stepdefinitions.api"  // Update this to your API library package
)
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "@API")
@ConfigurationParameter(
    key = PLUGIN_PROPERTY_NAME, 
    value = "pretty, " +
            "html:target/cucumber-reports/api-tests.html, " +
            "json:target/cucumber-reports/api-tests.json, " +
            "junit:target/cucumber-reports/api-tests.xml"
)
public class APITestRunner {
}
