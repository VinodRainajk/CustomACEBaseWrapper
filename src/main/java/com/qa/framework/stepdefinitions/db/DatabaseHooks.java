package com.qa.framework.stepdefinitions.db;

import com.qa.framework.config.ConfigurationManager;
import com.qa.framework.db.DatabaseManager;
import io.cucumber.java.After;
import io.cucumber.java.Before;

/**
 * Hooks for database step definitions - setup and teardown.
 */
public class DatabaseHooks {

    @Before("@DB")
    public void setUp() {
        DatabaseStepContext ctx = DatabaseStepContext.getInstance();
        ctx.setDbManager(DatabaseManager.getInstance());
        ctx.setConfigManager(ConfigurationManager.getInstance());
    }

    @After("@DB")
    public void tearDown() {
        DatabaseManager dbManager = DatabaseStepContext.getInstance().getDbManager();
        if (dbManager != null) {
            dbManager.closeAllConnections();
        }
        DatabaseStepContext.reset();
    }
}
