package com.qa.framework.db;

import java.util.Map;

/**
 * Factory class for creating database connections from unified YAML configuration.
 */
public class DatabaseConnectionFactory {

    private static final String DEFAULT_DB_CONFIG_NAME = "mysql";

    private DatabaseConnectionFactory() {
    }

    /**
     * Create a connection using db.{configName} from unified config.
     */
    public static DatabaseConnection createConnection(String configName, String featureName, String scenarioName) {
        Map<String, Object> config = DatabaseConfigLoader.resolveConfig(configName, featureName, scenarioName);
        return DatabaseConfigLoader.createConnectionFromResolvedConfig(config);
    }

    /**
     * Create a connection using default DB config name (mysql).
     */
    public static DatabaseConnection createConnection(String featureName, String scenarioName) {
        return createConnection(DEFAULT_DB_CONFIG_NAME, featureName, scenarioName);
    }

    /**
     * Create and connect using db.{configName} from unified config.
     */
    public static DatabaseConnection createAndConnect(String configName, String featureName, String scenarioName) {
        DatabaseConnection connection = createConnection(configName, featureName, scenarioName);
        connection.connect();
        return connection;
    }

    /**
     * Create and connect using default DB config name (mysql).
     */
    public static DatabaseConnection createAndConnect(String featureName, String scenarioName) {
        return createAndConnect(DEFAULT_DB_CONFIG_NAME, featureName, scenarioName);
    }
}
