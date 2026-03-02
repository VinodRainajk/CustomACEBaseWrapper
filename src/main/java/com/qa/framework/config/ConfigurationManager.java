package com.qa.framework.config;

import com.qa.framework.exceptions.WrapperException;

import java.util.HashMap;
import java.util.Map;

/**
 * Singleton manager for handling database configurations across different profiles.
 */
public class ConfigurationManager {
    
    private static ConfigurationManager instance;
    private Map<String, DatabaseConfig> configurations;
    private String activeProfile;
    
    private static final String DEFAULT_PROFILE_PROPERTY = "db.profile";
    private static final String DEFAULT_PROFILE = "mysql";
    
    private ConfigurationManager() {
        configurations = new HashMap<>();
        activeProfile = System.getProperty(DEFAULT_PROFILE_PROPERTY, DEFAULT_PROFILE);
    }
    
    /**
     * Get the singleton instance of ConfigurationManager.
     */
    public static synchronized ConfigurationManager getInstance() {
        if (instance == null) {
            instance = new ConfigurationManager();
        }
        return instance;
    }
    
    /**
     * Load a database configuration for a specific profile.
     */
    public DatabaseConfig loadConfiguration(String profile) {
        if (!configurations.containsKey(profile)) {
            DatabaseConfig config = new DatabaseConfig(profile);
            configurations.put(profile, config);
        }
        return configurations.get(profile);
    }
    
    /**
     * Get the configuration for the active profile.
     */
    public DatabaseConfig getActiveConfiguration() {
        return loadConfiguration(activeProfile);
    }
    
    /**
     * Get a configuration by profile name.
     */
    public DatabaseConfig getConfiguration(String profile) {
        return loadConfiguration(profile);
    }
    
    /**
     * Set the active profile.
     */
    public void setActiveProfile(String profile) {
        this.activeProfile = profile;
    }
    
    /**
     * Get the active profile name.
     */
    public String getActiveProfile() {
        return activeProfile;
    }
    
    /**
     * Check if a configuration exists for a profile.
     */
    public boolean hasConfiguration(String profile) {
        return configurations.containsKey(profile);
    }
    
    /**
     * Clear all loaded configurations.
     */
    public void clearConfigurations() {
        configurations.clear();
    }
    
    /**
     * Reset the configuration manager (useful for testing).
     */
    public static synchronized void reset() {
        instance = null;
    }
}
