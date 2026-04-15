package com.qa.framework.config;

import com.qa.framework.api.APIConfig;
import com.qa.framework.api.APIConfigLoader;
import com.qa.framework.db.DatabaseConfigLoader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UnifiedConfigLoaderTest {

    @AfterEach
    void tearDown() {
        System.clearProperty("profile");
    }

    @Test
    void defaultsToLocalProfileWhenNotProvided() {
        assertEquals("local", UnifiedConfigLoader.getProfile());
    }

    @Test
    void loadsApiSectionFromUnifiedConfig() {
        System.setProperty("profile", "local");
        APIConfig config = APIConfigLoader.loadConfig("users-api");
        assertNotNull(config.getApplication());
        assertEquals("https://jsonplaceholder.typicode.com", config.getApplication().getUrl());
    }

    @Test
    void appliesFeatureSpecificDbOverrideFromUnifiedConfig() {
        System.setProperty("profile", "local");
        Map<String, Object> db = DatabaseConfigLoader.resolveConfig("mysql", "users-api", "any");
        assertEquals("users_feature_local_user", String.valueOf(db.get("username")));
    }
}
