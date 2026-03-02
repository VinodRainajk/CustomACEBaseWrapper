package com.qa.framework.config;

import com.qa.framework.exceptions.WrapperException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Database configuration loader that reads properties from profile-specific configuration files.
 */
public class DatabaseConfig {
    
    private Properties properties;
    private String profile;
    
    private static final String CONFIG_PATH_TEMPLATE = "config/%s-config.properties";
    
    public DatabaseConfig(String profile) {
        this.profile = profile;
        this.properties = new Properties();
        loadConfiguration();
    }
    
    /**
     * Load configuration from the profile-specific properties file.
     */
    private void loadConfiguration() {
        String configPath = String.format(CONFIG_PATH_TEMPLATE, profile.toLowerCase());
        
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(configPath)) {
            if (inputStream == null) {
                throw new WrapperException("Configuration file not found: " + configPath);
            }
            properties.load(inputStream);
        } catch (IOException e) {
            throw new WrapperException("Failed to load configuration from: " + configPath, e);
        }
    }
    
    /**
     * Get the database type (mysql, postgresql, sqlserver, oracle).
     */
    public String getDatabaseType() {
        return getProperty("db.type");
    }
    
    /**
     * Get the JDBC driver class name.
     */
    public String getDriver() {
        return getProperty("db.driver");
    }
    
    /**
     * Get the database URL.
     */
    public String getUrl() {
        return getProperty("db.url");
    }
    
    /**
     * Get the database host.
     */
    public String getHost() {
        return getProperty("db.host");
    }
    
    /**
     * Get the database port.
     */
    public String getPort() {
        return getProperty("db.port");
    }
    
    /**
     * Get the database name.
     */
    public String getDatabase() {
        return getProperty("db.database");
    }
    
    /**
     * Get the database username.
     */
    public String getUsername() {
        return getProperty("db.username");
    }
    
    /**
     * Get the database password.
     */
    public String getPassword() {
        return getProperty("db.password");
    }
    
    /**
     * Get connection timeout in milliseconds.
     */
    public int getConnectionTimeout() {
        String timeout = getProperty("db.connection.timeout", "30000");
        return Integer.parseInt(timeout);
    }
    
    /**
     * Get connection pool size.
     */
    public int getConnectionPoolSize() {
        String poolSize = getProperty("db.connection.pool.size", "10");
        return Integer.parseInt(poolSize);
    }
    
    /**
     * Check if SSL is enabled.
     */
    public boolean isSslEnabled() {
        String ssl = getProperty("db.ssl.enabled", "false");
        return Boolean.parseBoolean(ssl);
    }
    
    /**
     * Get a property value by key.
     */
    public String getProperty(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            throw new WrapperException("Property not found: " + key + " in profile: " + profile);
        }
        return value;
    }
    
    /**
     * Get a property value by key with a default value.
     */
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    /**
     * Get all properties.
     */
    public Properties getProperties() {
        return new Properties(properties);
    }
    
    /**
     * Get the active profile name.
     */
    public String getProfile() {
        return profile;
    }
    
    /**
     * Build a complete JDBC URL from individual components if needed.
     */
    public String buildUrl() {
        String existingUrl = properties.getProperty("db.url");
        if (existingUrl != null && !existingUrl.isEmpty()) {
            return existingUrl;
        }
        
        String dbType = getDatabaseType();
        String host = getHost();
        String port = getPort();
        String database = getDatabase();
        
        switch (dbType.toLowerCase()) {
            case "mysql":
                return String.format("jdbc:mysql://%s:%s/%s", host, port, database);
            case "postgresql":
                return String.format("jdbc:postgresql://%s:%s/%s", host, port, database);
            case "sqlserver":
                return String.format("jdbc:sqlserver://%s:%s;databaseName=%s", host, port, database);
            case "oracle":
                String sid = getProperty("db.sid", database);
                return String.format("jdbc:oracle:thin:@%s:%s:%s", host, port, sid);
            default:
                throw new WrapperException("Unsupported database type: " + dbType);
        }
    }
}
