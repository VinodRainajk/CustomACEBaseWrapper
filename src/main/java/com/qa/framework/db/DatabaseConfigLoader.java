package com.qa.framework.db;

import com.qa.framework.config.UnifiedConfigLoader;
import com.qa.framework.exceptions.WrapperException;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads database config from unified YAML:
 * config/{profile}/master-config.yaml + optional config/{profile}/{feature}-config.yaml
 * where DB settings live under the top-level "db" section.
 * Profile is passed via -Dprofile=dev (Option B: profile as folder).
 * Config is resolved by name (e.g. "mysql", "oracle").
 */
public class DatabaseConfigLoader {

    public static String getProfile() {
        return UnifiedConfigLoader.getProfile();
    }

    /**
     * Resolve config for the given config name.
     * All config lives inside profile folder: config/{profile}/
     * Resolution: config/{profile}/master-config.yaml + config/{profile}/{feature}-config.yaml + sections
     *
     * @param configName   e.g. "mysql", "oracle"
     * @param featureName  base name of feature file (e.g. "cross-db")
     * @param scenarioName scenario title (for section override)
     * @return map with url, username, password, driver, type, timeout
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> resolveConfig(String configName, String featureName, String scenarioName) {
        Map<String, Object> mergedRoot = UnifiedConfigLoader.loadMergedConfig(featureName);
        Object dbObj = mergedRoot.get("db");
        if (!(dbObj instanceof Map)) {
            throw new WrapperException("Unified config must contain top-level 'db' section");
        }
        Map<String, Object> dbRoot = (Map<String, Object>) dbObj;
        Map<String, Object> baseConfig = (Map<String, Object>) dbRoot.get(configName);
        if (baseConfig == null) {
            throw new WrapperException("DB config '" + configName + "' not found under db section");
        }

        Map<String, Object> merged = new HashMap<>(baseConfig);
        Object sectionsObj = mergedRoot.get("sections");
        if (sectionsObj instanceof Map && scenarioName != null && !scenarioName.isBlank()) {
            Map<String, Object> sections = (Map<String, Object>) sectionsObj;
            Object scenarioObj = sections.get(scenarioName);
            if (scenarioObj instanceof Map) {
                Map<String, Object> scenarioMap = (Map<String, Object>) scenarioObj;
                Object scenarioDbObj = scenarioMap.get("db");
                if (scenarioDbObj instanceof Map) {
                    Map<String, Object> scenarioDb = (Map<String, Object>) scenarioDbObj;
                    Object scenarioDbConfig = scenarioDb.get(configName);
                    if (scenarioDbConfig instanceof Map) {
                        mergeInto(merged, (Map<String, Object>) scenarioDbConfig);
                    }
                }
            }
        }

        return merged;
    }

    /**
     * Extract feature name from feature file URI (e.g. "file:.../cross-db.feature" -> "cross-db").
     */
    public static String extractFeatureName(String featureUri) {
        if (featureUri == null) return "default";
        String name = featureUri;
        int lastSlash = name.lastIndexOf('/');
        if (lastSlash >= 0) name = name.substring(lastSlash + 1);
        int dot = name.lastIndexOf('.');
        if (dot > 0) name = name.substring(0, dot);
        return name.isEmpty() ? "default" : name;
    }

    private static void mergeInto(Map<String, Object> target, Map<String, Object> source) {
        if (source == null) return;
        for (Map.Entry<String, Object> e : source.entrySet()) {
            if (e.getValue() != null) {
                target.put(e.getKey(), e.getValue());
            }
        }
    }

    /**
     * Build DatabaseConnection from resolved YAML config map.
     */
    public static DatabaseConnection createConnectionFromResolvedConfig(Map<String, Object> config) {
        String url = getString(config, "url");
        String username = getString(config, "username");
        String password = getString(config, "password");
        String driver = getString(config, "driver");

        if (url == null || username == null || password == null) {
            throw new WrapperException("Database config must have url, username, and password");
        }

        return new DatabaseConnection(url, username, password, driver != null ? driver : "");
    }

    private static String getString(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v != null ? String.valueOf(v) : null;
    }
}
