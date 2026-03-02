package com.qa.framework.base;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

class BaseWrapperTest {
    
    private TestWrapper wrapper;
    
    @BeforeEach
    void setUp() {
        wrapper = new TestWrapper("TestWrapper");
    }
    
    @Test
    void testInitialization() {
        assertFalse(wrapper.isInitialized());
        wrapper.initialize();
        assertTrue(wrapper.isInitialized());
    }
    
    @Test
    void testCleanup() {
        wrapper.initialize();
        assertTrue(wrapper.isInitialized());
        wrapper.cleanup();
        assertFalse(wrapper.isInitialized());
    }
    
    @Test
    void testGetName() {
        assertEquals("TestWrapper", wrapper.getName());
    }
    
    @Test
    void testSetName() {
        wrapper.setName("NewName");
        assertEquals("NewName", wrapper.getName());
    }
    
    @Test
    void testMultipleInitializationCalls() {
        wrapper.initialize();
        int initCount = wrapper.getInitCount();
        wrapper.initialize();
        assertEquals(initCount, wrapper.getInitCount());
    }
    
    private static class TestWrapper extends BaseWrapper {
        private int initCount = 0;
        private int cleanupCount = 0;
        
        public TestWrapper(String name) {
            super(name);
        }
        
        @Override
        protected void doInitialize() {
            initCount++;
        }
        
        @Override
        protected void doCleanup() {
            cleanupCount++;
        }
        
        public int getInitCount() {
            return initCount;
        }
        
        public int getCleanupCount() {
            return cleanupCount;
        }
    }
}
