package com.qa.framework.api;

import com.qa.framework.config.UnifiedConfigLoader;
import com.qa.framework.exceptions.WrapperException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads API config from unified YAML:
 * config/{profile}/master-config.yaml + optional config/{profile}/{feature}-config.yaml.
 * API settings are read from top-level "api" section.
 * Profile is passed via -Dprofile=dev (Option B: profile as folder).
 * <p>
 * YAML is deep-merged into a map so dotted keys like {@code application.url} and {@code paths.users} resolve correctly.
 */
public class APIConfigLoader {

    public static String getProfile() {
        return UnifiedConfigLoader.getProfile();
    }

    /**
     * Load merged config for the given feature.
     * All config lives inside profile folder: config/{profile}/
     *
     * @param featureName base name of feature file without extension (e.g. "user-api")
     * @return merged config from config/{profile}/master-config.yaml + config/{profile}/{feature}-config.yaml
     */
    @SuppressWarnings("unchecked")
    public static APIConfig loadConfig(String featureName) {
        Map<String, Object> unifiedRoot = UnifiedConfigLoader.loadMergedConfig(featureName);
        Object apiObj = unifiedRoot.get("api");
        if (!(apiObj instanceof Map)) {
            throw new WrapperException("Unified config must contain top-level 'api' section");
        }
        Map<String, Object> merged = new HashMap<>((Map<String, Object>) apiObj);
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
        Object v = UnifiedConfigLoader.navigate(config.getMergedRoot(), dottedPath);
        if (v == null) {
            throw new WrapperException("API config key not found: " + dottedPath);
        }
        if (v instanceof Map || v instanceof List) {
            throw new WrapperException("API config key " + dottedPath + " is not a scalar");
        }
        return String.valueOf(v).trim();
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
