package com.qa.framework.payload;

import com.qa.framework.exceptions.WrapperException;
import com.qa.framework.utils.DynamicValueUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Thread-local store for {@code $var:name} placeholders (set from Gherkin).
 */
public final class ScenarioVariableStore {

    private static final ThreadLocal<Map<String, String>> VARS =
            ThreadLocal.withInitial(HashMap::new);

    private ScenarioVariableStore() {
    }

    public static void set(String name, String value) {
        if (name == null || name.isBlank()) {
            throw new WrapperException("Variable name cannot be blank");
        }
        String key = name.trim();
        VARS.get().put(key, value == null ? "" : value);
        DynamicValueUtils.setScenarioVariable(key, value == null ? "" : value);
    }

    public static String get(String name) {
        if (name == null || name.isBlank()) {
            throw new WrapperException("Variable name cannot be blank");
        }
        String value = VARS.get().get(name.trim());
        if (value == null) {
            throw new WrapperException("Variable not set: $var:" + name.trim()
                    + ". Use: Given variable \"" + name.trim() + "\" is \"<value>\"");
        }
        return value;
    }

    public static boolean has(String name) {
        return name != null && VARS.get().containsKey(name.trim());
    }

    public static void clear() {
        VARS.remove();
        DynamicValueUtils.clearScenarioVariables();
    }
}
