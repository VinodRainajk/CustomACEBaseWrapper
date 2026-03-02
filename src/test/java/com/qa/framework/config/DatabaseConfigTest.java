package com.qa.framework.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DatabaseConfig class.
 */
class DatabaseConfigTest {
    
    @Test
    void testLoadMySQLConfiguration() {
        DatabaseConfig config = new DatabaseConfig("mysql");
        
        assertEquals("mysql", config.getDatabaseType());
        assertEquals("com.mysql.cj.jdbc.Driver", config.getDriver());
        assertNotNull(config.getUrl());
        assertTrue(config.getUrl().contains("jdbc:mysql"));
        assertNotNull(config.getUsername());
        assertNotNull(config.getPassword());
    }
    
    @Test
    void testLoadPostgreSQLConfiguration() {
        DatabaseConfig config = new DatabaseConfig("postgresql");
        
        assertEquals("postgresql", config.getDatabaseType());
        assertEquals("org.postgresql.Driver", config.getDriver());
        assertNotNull(config.getUrl());
        assertTrue(config.getUrl().contains("jdbc:postgresql"));
    }
    
    @Test
    void testLoadSQLServerConfiguration() {
        DatabaseConfig config = new DatabaseConfig("sqlserver");
        
        assertEquals("sqlserver", config.getDatabaseType());
        assertEquals("com.microsoft.sqlserver.jdbc.SQLServerDriver", config.getDriver());
        assertNotNull(config.getUrl());
        assertTrue(config.getUrl().contains("jdbc:sqlserver"));
    }
    
    @Test
    void testLoadOracleConfiguration() {
        DatabaseConfig config = new DatabaseConfig("oracle");
        
        assertEquals("oracle", config.getDatabaseType());
        assertEquals("oracle.jdbc.driver.OracleDriver", config.getDriver());
        assertNotNull(config.getUrl());
        assertTrue(config.getUrl().contains("jdbc:oracle"));
    }
    
    @Test
    void testGetPropertyWithDefault() {
        DatabaseConfig config = new DatabaseConfig("mysql");
        
        String value = config.getProperty("non.existent.property", "default");
        assertEquals("default", value);
    }
    
    @Test
    void testGetConnectionTimeout() {
        DatabaseConfig config = new DatabaseConfig("mysql");
        
        int timeout = config.getConnectionTimeout();
        assertTrue(timeout > 0);
    }
    
    @Test
    void testGetConnectionPoolSize() {
        DatabaseConfig config = new DatabaseConfig("mysql");
        
        int poolSize = config.getConnectionPoolSize();
        assertTrue(poolSize > 0);
    }
    
    @Test
    void testGetProfile() {
        DatabaseConfig config = new DatabaseConfig("mysql");
        
        assertEquals("mysql", config.getProfile());
    }
}
