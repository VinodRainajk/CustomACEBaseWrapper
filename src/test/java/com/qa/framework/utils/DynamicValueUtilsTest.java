package com.qa.framework.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DynamicValueUtilsTest {

    @AfterEach
    void tearDown() {
        DynamicValueUtils.clearScenarioVariables();
    }

    @Test
    void resolvesAndStoresVariableForReuse() {
        String first = DynamicValueUtils.resolveTokens("${auto.uuid->vars.id}");
        String second = DynamicValueUtils.resolveTokens("${vars.id}");

        assertFalse(first.isBlank());
        assertTrue(first.equals(second), "Stored var should match reused var value");
    }

    @Test
    void resolvesAutoIntInRange() {
        String value = DynamicValueUtils.resolveTokens("${auto.int:10:20}");
        int parsed = Integer.parseInt(value);
        assertTrue(parsed >= 10 && parsed <= 20, "auto.int should stay inside range");
    }
}
