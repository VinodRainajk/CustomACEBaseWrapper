package com.qa.framework.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StringUtilsTest {
    
    @Test
    void testIsEmpty() {
        assertTrue(StringUtils.isEmpty(null));
        assertTrue(StringUtils.isEmpty(""));
        assertFalse(StringUtils.isEmpty("test"));
        assertFalse(StringUtils.isEmpty(" "));
    }
    
    @Test
    void testIsNotEmpty() {
        assertFalse(StringUtils.isNotEmpty(null));
        assertFalse(StringUtils.isNotEmpty(""));
        assertTrue(StringUtils.isNotEmpty("test"));
        assertTrue(StringUtils.isNotEmpty(" "));
    }
    
    @Test
    void testIsBlank() {
        assertTrue(StringUtils.isBlank(null));
        assertTrue(StringUtils.isBlank(""));
        assertTrue(StringUtils.isBlank("   "));
        assertFalse(StringUtils.isBlank("test"));
    }
    
    @Test
    void testIsNotBlank() {
        assertFalse(StringUtils.isNotBlank(null));
        assertFalse(StringUtils.isNotBlank(""));
        assertFalse(StringUtils.isNotBlank("   "));
        assertTrue(StringUtils.isNotBlank("test"));
    }
    
    @Test
    void testDefaultIfEmpty() {
        assertEquals("default", StringUtils.defaultIfEmpty(null, "default"));
        assertEquals("default", StringUtils.defaultIfEmpty("", "default"));
        assertEquals("test", StringUtils.defaultIfEmpty("test", "default"));
    }
    
    @Test
    void testCapitalize() {
        assertNull(StringUtils.capitalize(null));
        assertEquals("", StringUtils.capitalize(""));
        assertEquals("Test", StringUtils.capitalize("test"));
        assertEquals("Test", StringUtils.capitalize("Test"));
    }
    
    @Test
    void testTruncate() {
        assertNull(StringUtils.truncate(null, 5));
        assertEquals("test", StringUtils.truncate("test", 10));
        assertEquals("test", StringUtils.truncate("test", 4));
        assertEquals("tes", StringUtils.truncate("test", 3));
    }
}
