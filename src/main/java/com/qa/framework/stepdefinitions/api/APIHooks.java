package com.qa.framework.stepdefinitions.api;

import com.qa.framework.api.APIConfig;
import com.qa.framework.api.APIConfigLoader;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

/**
 * Hooks for API step definitions - setup and teardown.
 * Loads config (master + optional feature override) based on feature file name.
 */
public class APIHooks {

    @Before("@API")
    public void setUp(Scenario scenario) {
        String featureUri = scenario.getUri() != null ? scenario.getUri().toString() : null;
        String featureName = APIConfigLoader.extractFeatureName(featureUri);
        APIConfig config = APIConfigLoader.loadConfig(featureName);
        APIStepContext ctx = APIStepContext.getInstance();
        ctx.setConfig(config);
        if (config != null && config.getApplication() != null && config.getApplication().getUrl() != null) {
            String url = config.getApplication().getUrl();
            ctx.setBaseUrl(url.endsWith("/") ? url.substring(0, url.length() - 1) : url);
        }
    }

    @After("@API")
    public void tearDown() {
        APIStepContext.reset();
    }
}
