package com.qa.framework.db;

import com.qa.framework.config.ConfigurationManager;
import com.qa.framework.config.DatabaseConfig;

/**
 * Factory class for creating database connections from configuration profiles.
 */
public class DatabaseConnectionFactory {
    
    /**
     * Create a database connection using the active profile.
     * 
     * @return DatabaseConnection configured with active profile settings
     */
    public static DatabaseConnection createConnection() {
        ConfigurationManager configManager = ConfigurationManager.getInstance();
        DatabaseConfig config = configManager.getActiveConfiguration();
        return createConnection(config);
    }
    
    /**
     * Create a database connection using a specific profile.
     * 
     * @param profile the configuration profile name (mysql, postgresql, sqlserver, oracle)
     * @return DatabaseConnection configured with the specified profile settings
     */
    public static DatabaseConnection createConnectionFromProfile(String profile) {
        ConfigurationManager configManager = ConfigurationManager.getInstance();
        DatabaseConfig config = configManager.loadConfiguration(profile);
        return createConnection(config);
    }
    
    /**
     * Create a database connection from a DatabaseConfig object.
     * 
     * @param config the database configuration
     * @return DatabaseConnection configured with the provided settings
     */
    public static DatabaseConnection createConnection(DatabaseConfig config) {
        String url = config.getUrl();
        String username = config.getUsername();
        String password = config.getPassword();
        String driver = config.getDriver();
        
        return new DatabaseConnection(url, username, password, driver);
    }
    
    /**
     * Create and connect to a database using the active profile.
     * 
     * @return connected DatabaseConnection
     */
    public static DatabaseConnection createAndConnect() {
        DatabaseConnection connection = createConnection();
        connection.connect();
        return connection;
    }
    
    /**
     * Create and connect to a database using a specific profile.
     * 
     * @param profile the configuration profile name
     * @return connected DatabaseConnection
     */
    public static DatabaseConnection createAndConnectFromProfile(String profile) {
        DatabaseConnection connection = createConnectionFromProfile(profile);
        connection.connect();
        return connection;
    }
}
