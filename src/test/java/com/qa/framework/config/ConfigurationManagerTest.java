package com.qa.framework.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ConfigurationManager class.
 */
class ConfigurationManagerTest {
    
    private ConfigurationManager configManager;
    
    @BeforeEach
    void setUp() {
        ConfigurationManager.reset();
        configManager = ConfigurationManager.getInstance();
    }
    
    @AfterEach
    void tearDown() {
        configManager.clearConfigurations();
        ConfigurationManager.reset();
    }
    
    @Test
    void testGetInstance() {
        ConfigurationManager instance1 = ConfigurationManager.getInstance();
        ConfigurationManager instance2 = ConfigurationManager.getInstance();
        
        assertSame(instance1, instance2, "Should return the same singleton instance");
    }
    
    @Test
    void testLoadConfiguration() {
        DatabaseConfig config = configManager.loadConfiguration("mysql");
        
        assertNotNull(config);
        assertEquals("mysql", config.getProfile());
    }
    
    @Test
    void testGetActiveConfiguration() {
        configManager.setActiveProfile("postgresql");
        DatabaseConfig config = configManager.getActiveConfiguration();
        
        assertNotNull(config);
        assertEquals("postgresql", config.getProfile());
    }
    
    @Test
    void testSetActiveProfile() {
        configManager.setActiveProfile("oracle");
        
        assertEquals("oracle", configManager.getActiveProfile());
    }
    
    @Test
    void testGetConfiguration() {
        DatabaseConfig config = configManager.getConfiguration("sqlserver");
        
        assertNotNull(config);
        assertEquals("sqlserver", config.getProfile());
    }
    
    @Test
    void testHasConfiguration() {
        configManager.loadConfiguration("mysql");
        
        assertTrue(configManager.hasConfiguration("mysql"));
        assertFalse(configManager.hasConfiguration("nonexistent"));
    }
    
    @Test
    void testClearConfigurations() {
        configManager.loadConfiguration("mysql");
        configManager.loadConfiguration("postgresql");
        
        assertTrue(configManager.hasConfiguration("mysql"));
        assertTrue(configManager.hasConfiguration("postgresql"));
        
        configManager.clearConfigurations();
        
        assertFalse(configManager.hasConfiguration("mysql"));
        assertFalse(configManager.hasConfiguration("postgresql"));
    }
    
    @Test
    void testDefaultProfile() {
        String activeProfile = configManager.getActiveProfile();
        
        assertNotNull(activeProfile);
        assertEquals("mysql", activeProfile);
    }
}
