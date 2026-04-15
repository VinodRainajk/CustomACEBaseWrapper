package com.qa.framework.stepdefinitions.db;

import com.qa.framework.db.DatabaseConfigLoader;
import com.qa.framework.db.DatabaseManager;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

/**
 * Hooks for database step definitions - setup and teardown.
 * Sets feature and scenario name for config resolution (master + feature + section).
 */
public class DatabaseHooks {

    @Before
    public void setUp(Scenario scenario) {
        DatabaseStepContext ctx = DatabaseStepContext.getInstance();
        ctx.setDbManager(DatabaseManager.getInstance());
        String featureUri = scenario.getUri() != null ? scenario.getUri().toString() : null;
        ctx.setFeatureName(DatabaseConfigLoader.extractFeatureName(featureUri));
        ctx.setScenarioName(scenario.getName());
    }

    @After
    public void tearDown() {
        DatabaseManager dbManager = DatabaseStepContext.getInstance().getDbManager();
        if (dbManager != null) {
            dbManager.closeAllConnections();
        }
        DatabaseStepContext.reset();
    }
}
