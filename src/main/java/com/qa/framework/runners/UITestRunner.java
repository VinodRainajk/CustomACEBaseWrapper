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
 * UI Test Runner - Executes only @UI tagged scenarios.
 * Routes to UI step definitions (from your UI library).
 * 
 * Note: Update GLUE_PROPERTY_NAME to point to your UI library's step definitions package.
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = FEATURES_PROPERTY_NAME, value = "src/test/resources/features")
@ConfigurationParameter(
    key = GLUE_PROPERTY_NAME, 
    value = "com.qa.framework.stepdefinitions.ui"  // Update this to your UI library package
)
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "@UI")
@ConfigurationParameter(
    key = PLUGIN_PROPERTY_NAME, 
    value = "pretty, " +
            "html:target/cucumber-reports/ui-tests.html, " +
            "json:target/cucumber-reports/ui-tests.json, " +
            "junit:target/cucumber-reports/ui-tests.xml"
)
public class UITestRunner {
}
