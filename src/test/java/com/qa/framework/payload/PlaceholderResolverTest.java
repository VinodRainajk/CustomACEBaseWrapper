package com.qa.framework.payload;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlaceholderResolverTest {

    @BeforeEach
    void setUp() {
        PayloadRegistry.setActiveFeature("users-api");
        ScenarioVariableStore.set("title", "Hello");
    }

    @AfterEach
    void tearDown() {
        PayloadRegistry.clear();
        ScenarioVariableStore.clear();
    }

    @Test
    void resolvesConfigAndVarInSameString() {
        String resolved = PlaceholderResolver.resolveText(
                "$config:api.application.url$config:api.paths.users");
        assertTrue(resolved.startsWith("https://"));
        assertTrue(resolved.contains("/users"));
    }

    @Test
    void resolvesVarPlaceholder() {
        assertEquals("Hello", PlaceholderResolver.resolveText("$var:title"));
    }
}
