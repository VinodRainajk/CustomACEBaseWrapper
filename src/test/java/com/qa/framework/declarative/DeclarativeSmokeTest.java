package com.qa.framework.declarative;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

/**
 * Runs features/declarative-demo.feature through a real Cucumber runtime.
 * <p>
 * The scenario has no Java step definitions at all, so it can only pass if the backend registered the
 * sentences from bundles/demo-bundles.feature and ran their recipes.
 * </p>
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/declarative-demo.feature")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.qa.framework.payload")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty")
public class DeclarativeSmokeTest {
}
