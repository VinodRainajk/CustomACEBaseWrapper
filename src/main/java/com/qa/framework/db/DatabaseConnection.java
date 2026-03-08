package com.qa.framework.db;

import com.qa.framework.exceptions.WrapperException;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Database connection wrapper for managing database operations.
 */
public class DatabaseConnection {
    
    private Connection connection;
    private String url;
    private String username;
    private String password;
    private String driver;
    
    public DatabaseConnection(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }
    
    public DatabaseConnection(String url, String username, String password, String driver) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.driver = driver;
    }
    
    /**
     * Establish database connection.
     */
    public void connect() {
        try {
            if (driver != null && !driver.isEmpty()) {
                Class.forName(driver);
            }
            
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url, username, password);
            }
        } catch (ClassNotFoundException e) {
            throw new WrapperException("Database driver not found: " + driver, e);
        } catch (SQLException e) {
            throw new WrapperException("Failed to connect to database: " + e.getMessage(), e);
        }
    }
    
    /**
     * Close database connection.
     */
    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new WrapperException("Failed to close database connection: " + e.getMessage(), e);
        }
    }
    
    /**
     * Execute a SELECT query and return results.
     * 
     * @param query the SQL query to execute
     * @return list of maps containing column name and value pairs
     */
    public List<Map<String, Object>> executeQuery(String query) {
        List<Map<String, Object>> results = new ArrayList<>();
        
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            while (resultSet.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object value = resultSet.getObject(i);
                    row.put(columnName, value);
                }
                results.add(row);
            }
        } catch (SQLException e) {
            throw new WrapperException("Failed to execute query: " + e.getMessage(), e);
        }
        
        return results;
    }
    
    /**
     * Execute an UPDATE, INSERT, or DELETE query.
     * 
     * @param query the SQL query to execute
     * @return the number of rows affected
     */
    public int executeUpdate(String query) {
        try (Statement statement = connection.createStatement()) {
            return statement.executeUpdate(query);
        } catch (SQLException e) {
            throw new WrapperException("Failed to execute update: " + e.getMessage(), e);
        }
    }
    
    /**
     * Execute a prepared UPDATE, INSERT, or DELETE.
     *
     * @param query the SQL with placeholders
     * @param parameters the parameters to bind
     * @return number of rows affected
     */
    public int executePreparedUpdate(String query, Object... parameters) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            for (int i = 0; i < parameters.length; i++) {
                preparedStatement.setObject(i + 1, parameters[i]);
            }
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new WrapperException("Failed to execute prepared update: " + e.getMessage(), e);
        }
    }

    /**
     * Execute a callable statement (stored procedure).
     *
     * @param callableSql the callable SQL, e.g. "{call procedure_name(?, ?)}"
     * @param parameters input parameters
     * @return list of result sets if procedure returns result sets, empty list otherwise
     */
    public List<Map<String, Object>> executeCallable(String callableSql, Object... parameters) {
        List<Map<String, Object>> results = new ArrayList<>();
        try (CallableStatement callableStatement = connection.prepareCall(callableSql)) {
            for (int i = 0; i < parameters.length; i++) {
                callableStatement.setObject(i + 1, parameters[i]);
            }
            boolean hasResultSet = callableStatement.execute();
            while (hasResultSet) {
                try (ResultSet resultSet = callableStatement.getResultSet()) {
                    results.addAll(extractResultSet(resultSet));
                }
                hasResultSet = callableStatement.getMoreResults();
            }
        } catch (SQLException e) {
            throw new WrapperException("Failed to execute callable: " + e.getMessage(), e);
        }
        return results;
    }

    private List<Map<String, Object>> extractResultSet(ResultSet resultSet) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        while (resultSet.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                row.put(metaData.getColumnName(i), resultSet.getObject(i));
            }
            rows.add(row);
        }
        return rows;
    }

    /**
     * Execute a prepared statement query.
     * 
     * @param query the SQL query with placeholders
     * @param parameters the parameters to bind
     * @return list of maps containing column name and value pairs
     */
    public List<Map<String, Object>> executePreparedQuery(String query, Object... parameters) {
        List<Map<String, Object>> results = new ArrayList<>();
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            
            for (int i = 0; i < parameters.length; i++) {
                preparedStatement.setObject(i + 1, parameters[i]);
            }
            
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();
                
                while (resultSet.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        Object value = resultSet.getObject(i);
                        row.put(columnName, value);
                    }
                    results.add(row);
                }
            }
        } catch (SQLException e) {
            throw new WrapperException("Failed to execute prepared query: " + e.getMessage(), e);
        }
        
        return results;
    }
    
    /**
     * Check if connection is active.
     * 
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
    
    /**
     * Get the underlying connection object.
     * 
     * @return the connection object
     */
    public Connection getConnection() {
        return connection;
    }
}
