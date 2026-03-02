package com.qa.framework.db;

import java.util.HashMap;
import java.util.Map;

/**
 * Singleton manager for handling multiple database connections.
 */
public class DatabaseManager {
    
    private static DatabaseManager instance;
    private Map<String, DatabaseConnection> connections;
    
    private DatabaseManager() {
        connections = new HashMap<>();
    }
    
    /**
     * Get the singleton instance of DatabaseManager.
     * 
     * @return the DatabaseManager instance
     */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    /**
     * Add a database connection with a unique name.
     * 
     * @param name the connection name
     * @param url the database URL
     * @param username the database username
     * @param password the database password
     */
    public void addConnection(String name, String url, String username, String password) {
        DatabaseConnection connection = new DatabaseConnection(url, username, password);
        connections.put(name, connection);
    }
    
    /**
     * Add an existing database connection with a unique name.
     * 
     * @param name the connection name
     * @param connection the DatabaseConnection object
     */
    public void addConnection(String name, DatabaseConnection connection) {
        connections.put(name, connection);
    }
    
    /**
     * Get a database connection by name.
     * 
     * @param name the connection name
     * @return the DatabaseConnection object
     */
    public DatabaseConnection getConnection(String name) {
        return connections.get(name);
    }
    
    /**
     * Remove a database connection.
     * 
     * @param name the connection name
     */
    public void removeConnection(String name) {
        DatabaseConnection connection = connections.get(name);
        if (connection != null) {
            connection.disconnect();
            connections.remove(name);
        }
    }
    
    /**
     * Close all database connections.
     */
    public void closeAllConnections() {
        for (DatabaseConnection connection : connections.values()) {
            connection.disconnect();
        }
        connections.clear();
    }
    
    /**
     * Check if a connection exists.
     * 
     * @param name the connection name
     * @return true if connection exists, false otherwise
     */
    public boolean hasConnection(String name) {
        return connections.containsKey(name);
    }
}
