package com.qa.framework.base;

import java.util.HashMap;
import java.util.Map;

/**
 * A configurable wrapper that extends BaseWrapper with configuration management.
 */
public abstract class ConfigurableWrapper extends BaseWrapper {
    
    protected Map<String, Object> configuration;
    
    public ConfigurableWrapper(String name) {
        super(name);
        this.configuration = new HashMap<>();
    }
    
    /**
     * Set a configuration property.
     * 
     * @param key the configuration key
     * @param value the configuration value
     */
    public void setConfig(String key, Object value) {
        configuration.put(key, value);
    }
    
    /**
     * Get a configuration property.
     * 
     * @param key the configuration key
     * @return the configuration value, or null if not found
     */
    public Object getConfig(String key) {
        return configuration.get(key);
    }
    
    /**
     * Get a configuration property with a default value.
     * 
     * @param key the configuration key
     * @param defaultValue the default value to return if key not found
     * @return the configuration value, or defaultValue if not found
     */
    public Object getConfig(String key, Object defaultValue) {
        return configuration.getOrDefault(key, defaultValue);
    }
    
    /**
     * Check if a configuration key exists.
     * 
     * @param key the configuration key
     * @return true if the key exists, false otherwise
     */
    public boolean hasConfig(String key) {
        return configuration.containsKey(key);
    }
    
    /**
     * Remove a configuration property.
     * 
     * @param key the configuration key
     * @return the previous value associated with the key, or null
     */
    public Object removeConfig(String key) {
        return configuration.remove(key);
    }
    
    /**
     * Clear all configuration properties.
     */
    public void clearConfig() {
        configuration.clear();
    }
    
    /**
     * Get all configuration properties.
     * 
     * @return a copy of the configuration map
     */
    public Map<String, Object> getAllConfig() {
        return new HashMap<>(configuration);
    }
}
