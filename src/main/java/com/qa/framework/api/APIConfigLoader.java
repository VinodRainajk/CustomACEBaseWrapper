package com.qa.framework.api;

import com.qa.framework.exceptions.WrapperException;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads API config: master-api.yaml + optional {feature}-config-api.yaml (mirrors DB: master_database.yml + {feature}-database.yml).
 * Profile is passed via -Dprofile=dev (Option B: profile as folder).
 * <p>
 * YAML is deep-merged into a map so dotted keys like {@code application.url} and {@code paths.users} resolve correctly.
 */
public class APIConfigLoader {

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
     * Load merged config for the given feature.
     * All config lives inside profile folder: config/{profile}/
     *
     * @param featureName base name of feature file without extension (e.g. "user-api")
     * @return merged config from config/{profile}/master-api.yaml + config/{profile}/{feature}-config-api.yaml
     */
    public static APIConfig loadConfig(String featureName) {
        String profile = getProfile();
        String profileBase = CONFIG_BASE + profile + "/";
        Map<String, Object> masterMap = loadYamlMap(profileBase + "master-api.yaml");
        if (masterMap == null) {
            masterMap = new HashMap<>();
        }
        Map<String, Object> featureMap = loadYamlMap(profileBase + featureName + "-config-api.yaml");
        Map<String, Object> merged = deepMerge(masterMap, featureMap != null ? featureMap : new HashMap<>());
        APIConfig config = mapToConfig(merged);
        config.setMergedRoot(merged);
        return config;
    }

    /**
     * Resolve a dotted path in merged API YAML to a scalar string (e.g. {@code paths.users}, {@code application.url}).
     *
     * @throws WrapperException if config is missing, key not found, or value is not a scalar
     */
    public static String requireScalar(APIConfig config, String dottedPath) {
        if (config == null || config.getMergedRoot() == null) {
            throw new WrapperException("API config not loaded; cannot resolve: " + dottedPath);
        }
        Object v = navigate(config.getMergedRoot(), dottedPath);
        if (v == null) {
            throw new WrapperException("API config key not found: " + dottedPath);
        }
        if (v instanceof Map || v instanceof List) {
            throw new WrapperException("API config key " + dottedPath + " is not a scalar");
        }
        return String.valueOf(v).trim();
    }

    @SuppressWarnings("unchecked")
    private static Object navigate(Map<String, Object> root, String dottedPath) {
        if (root == null || dottedPath == null || dottedPath.isEmpty()) {
            return null;
        }
        String[] parts = dottedPath.split("\\.");
        Object cur = root;
        for (String part : parts) {
            if (!(cur instanceof Map)) {
                return null;
            }
            Map<String, Object> map = (Map<String, Object>) cur;
            cur = map.get(part);
            if (cur == null) {
                return null;
            }
        }
        return cur;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> loadYamlMap(String classpathPath) {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(classpathPath)) {
            if (is == null) {
                return null;
            }
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

    @SuppressWarnings("unchecked")
    private static Map<String, Object> deepMerge(Map<String, Object> base, Map<String, Object> override) {
        if (base == null) {
            base = new HashMap<>();
        }
        if (override == null || override.isEmpty()) {
            return new HashMap<>(base);
        }
        Map<String, Object> out = new HashMap<>(base);
        for (Map.Entry<String, Object> e : override.entrySet()) {
            if (e.getValue() == null) {
                continue;
            }
            Object existing = out.get(e.getKey());
            if (existing instanceof Map && e.getValue() instanceof Map) {
                out.put(e.getKey(), deepMerge((Map<String, Object>) existing, (Map<String, Object>) e.getValue()));
            } else {
                out.put(e.getKey(), e.getValue());
            }
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private static APIConfig mapToConfig(Map<String, Object> map) {
        APIConfig config = new APIConfig();
        if (map == null) {
            return config;
        }
        if (map.containsKey("application")) {
            config.setApplication(mapToApplication((Map<String, Object>) map.get("application")));
        }
        if (map.containsKey("auth")) {
            config.setAuth(mapToAuth((Map<String, Object>) map.get("auth")));
        }
        return config;
    }

    private static APIConfig.Application mapToApplication(Map<String, Object> m) {
        APIConfig.Application app = new APIConfig.Application();
        if (m != null) {
            if (m.containsKey("url")) {
                app.setUrl(String.valueOf(m.get("url")));
            }
            if (m.containsKey("timeout")) {
                Object t = m.get("timeout");
                if (t instanceof Number) {
                    app.setTimeout(((Number) t).intValue());
                }
            }
        }
        return app;
    }

    private static APIConfig.Auth mapToAuth(Map<String, Object> m) {
        APIConfig.Auth auth = new APIConfig.Auth();
        if (m != null) {
            if (m.containsKey("type")) {
                auth.setType(String.valueOf(m.get("type")));
            }
            if (m.containsKey("token")) {
                auth.setToken(String.valueOf(m.get("token")));
            }
            if (m.containsKey("apiKey")) {
                auth.setApiKey(String.valueOf(m.get("apiKey")));
            }
        }
        return auth;
    }

    /**
     * Extract feature name from feature file URI (e.g. "file:.../user-api.feature" -> "user-api").
     */
    public static String extractFeatureName(String featureUri) {
        if (featureUri == null) {
            return "default";
        }
        String name = featureUri;
        int lastSlash = name.lastIndexOf('/');
        if (lastSlash >= 0) {
            name = name.substring(lastSlash + 1);
        }
        int dot = name.lastIndexOf('.');
        if (dot > 0) {
            name = name.substring(0, dot);
        }
        return name.isEmpty() ? "default" : name;
    }
}
