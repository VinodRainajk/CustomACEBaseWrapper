package com.qa.framework.config;

import com.qa.framework.exceptions.WrapperException;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Unified YAML config loader.
 * <p>
 * Resolution order (later overrides earlier):
 * <ol>
 *   <li>config/{profile}/master-config.yaml</li>
 *   <li>config/{profile}/{feature}-config.yaml</li>
 * </ol>
 * <p>
 * Active profile is passed as {@code -Dprofile=dev}. Defaults to {@code local}.
 */
public final class UnifiedConfigLoader {

    public static final String CONFIG_BASE = "config/";
    public static final String PROFILE_PROPERTY = "profile";
    public static final String DEFAULT_PROFILE = "local";

    private UnifiedConfigLoader() {
    }

    public static String getProfile() {
        String profile = System.getProperty(PROFILE_PROPERTY);
        return (profile == null || profile.isBlank()) ? DEFAULT_PROFILE : profile.trim();
    }

    public static Map<String, Object> loadMergedConfig(String featureName) {
        String profile = getProfile();
        String profileBase = CONFIG_BASE + profile + "/";

        Map<String, Object> master = loadYamlMap(profileBase + "master-config.yaml");
        if (master == null) {
            throw new WrapperException("master-config.yaml not found at " + profileBase + " (profile=" + profile + ")");
        }

        Map<String, Object> feature = loadYamlMap(profileBase + featureName + "-config.yaml");
        return deepMerge(master, feature != null ? feature : new HashMap<>());
    }

    @SuppressWarnings("unchecked")
    public static Object navigate(Map<String, Object> root, String dottedPath) {
        if (root == null || dottedPath == null || dottedPath.isBlank()) {
            return null;
        }
        String[] parts = dottedPath.split("\\.");
        Object current = root;
        for (String part : parts) {
            if (!(current instanceof Map)) {
                return null;
            }
            Map<String, Object> map = (Map<String, Object>) current;
            current = map.get(part);
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> deepMerge(Map<String, Object> base, Map<String, Object> override) {
        if (base == null) {
            base = new HashMap<>();
        }
        if (override == null || override.isEmpty()) {
            return new HashMap<>(base);
        }
        Map<String, Object> merged = new HashMap<>(base);
        for (Map.Entry<String, Object> entry : override.entrySet()) {
            Object overrideValue = entry.getValue();
            if (overrideValue == null) {
                continue;
            }
            Object baseValue = merged.get(entry.getKey());
            if (baseValue instanceof Map && overrideValue instanceof Map) {
                merged.put(entry.getKey(), deepMerge((Map<String, Object>) baseValue, (Map<String, Object>) overrideValue));
            } else {
                merged.put(entry.getKey(), overrideValue);
            }
        }
        return merged;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> loadYamlMap(String classpathPath) {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(classpathPath)) {
            if (is == null) {
                return null;
            }
            Yaml yaml = new Yaml(new LoaderOptions());
            Object data = yaml.load(new InputStreamReader(is, StandardCharsets.UTF_8));
            if (data instanceof Map) {
                return (Map<String, Object>) data;
            }
            return null;
        } catch (Exception e) {
            throw new WrapperException("Failed to load YAML config: " + classpathPath, e);
        }
    }
}
