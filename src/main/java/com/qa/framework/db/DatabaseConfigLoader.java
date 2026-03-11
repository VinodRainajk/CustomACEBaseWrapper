package com.qa.framework.db;

import com.qa.framework.exceptions.WrapperException;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.LoaderOptions;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads database config from YAML: master_database.yml + optional profile folder + feature override + section override.
 * Profile is passed via -Dprofile=dev (Option B: profile as folder).
 * Config is resolved by name (e.g. "mysql", "oracle").
 */
public class DatabaseConfigLoader {

    private static final String CONFIG_BASE = "config/";
    private static final String PROFILE_PROPERTY = "profile";
    private static final String DEFAULT_PROFILE = "local";

    /**
     * Get the active profile from system property (-Dprofile=dev).
     * Defaults to "local" when not set.
     */
    public static String getProfile() {
        String p = System.getProperty(PROFILE_PROPERTY);
        return (p != null && !p.isEmpty()) ? p : DEFAULT_PROFILE;
    }

    /**
     * Resolve config for the given config name.
     * All config lives inside profile folder: config/{profile}/
     * Resolution: config/{profile}/master_database.yml + config/{profile}/{feature}-database.yml + sections
     *
     * @param configName   e.g. "mysql", "oracle"
     * @param featureName  base name of feature file (e.g. "cross-db")
     * @param scenarioName scenario title (for section override)
     * @return map with url, username, password, driver, type, timeout
     */
    public static Map<String, Object> resolveConfig(String configName, String featureName, String scenarioName) {
        String profile = getProfile();
        String profileBase = CONFIG_BASE + profile + "/";
        String masterPath = profileBase + "master_database.yml";

        Map<String, Object> master = loadYamlMap(masterPath);
        if (master == null) {
            throw new WrapperException("master_database.yml not found at " + masterPath + " (profile=" + profile + ")");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> baseConfig = (Map<String, Object>) master.get(configName);
        if (baseConfig == null) {
            throw new WrapperException("Config '" + configName + "' not found in " + masterPath);
        }

        Map<String, Object> merged = new HashMap<>(baseConfig);

        String featureConfigPath = profileBase + featureName + "-database.yml";
        Map<String, Object> featureYaml = loadYamlMap(featureConfigPath);
        if (featureYaml != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> featureConfig = (Map<String, Object>) featureYaml.get(configName);
            if (featureConfig != null) {
                mergeInto(merged, featureConfig);
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> sections = (Map<String, Object>) featureYaml.get("sections");
            if (sections != null && scenarioName != null && !scenarioName.isEmpty()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> sectionConfigs = (Map<String, Object>) sections.get(scenarioName);
                if (sectionConfigs != null) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> sectionConfig = (Map<String, Object>) sectionConfigs.get(configName);
                    if (sectionConfig != null) {
                        mergeInto(merged, sectionConfig);
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

    @SuppressWarnings("unchecked")
    private static Map<String, Object> loadYamlMap(String path) {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path)) {
            if (is == null) return null;
            Yaml yaml = new Yaml(new LoaderOptions());
            Object data = yaml.load(new java.io.InputStreamReader(is, StandardCharsets.UTF_8));
            if (data instanceof Map) {
                return (Map<String, Object>) data;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private static void mergeInto(Map<String, Object> target, Map<String, Object> source) {
        if (source == null) return;
        for (Map.Entry<String, Object> e : source.entrySet()) {
            if (e.getValue() != null) {
                target.put(e.getKey(), e.getValue());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> mergeMaps(Map<String, Object> base, Map<String, Object> override) {
        Map<String, Object> result = new HashMap<>(base);
        if (override == null) return result;
        for (Map.Entry<String, Object> e : override.entrySet()) {
            if (e.getValue() == null) continue;
            if (e.getValue() instanceof Map && result.get(e.getKey()) instanceof Map) {
                result.put(e.getKey(), mergeMaps((Map<String, Object>) result.get(e.getKey()), (Map<String, Object>) e.getValue()));
            } else {
                result.put(e.getKey(), e.getValue());
            }
        }
        return result;
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
