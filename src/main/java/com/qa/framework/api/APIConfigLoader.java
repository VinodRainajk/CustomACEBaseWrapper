package com.qa.framework.api;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.LoaderOptions;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Loads API config: master-api.yaml + optional {feature}-config-api.yaml (mirrors DB: master_database.yml + {feature}-database.yml).
 * Profile is passed via -Dprofile=dev (Option B: profile as folder).
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
        String masterPath = profileBase + "master-api.yaml";

        APIConfig master = loadYaml(masterPath, APIConfig.class);
        if (master == null) {
            master = new APIConfig();
        }

        String featureConfigPath = profileBase + featureName + "-config-api.yaml";
        APIConfig featureConfig = loadYaml(featureConfigPath, APIConfig.class);
        if (featureConfig == null) {
            return master;
        }

        return merge(master, featureConfig);
    }

    @SuppressWarnings("unchecked")
    private static <T> T loadYaml(String path, Class<T> type) {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path)) {
            if (is == null) return null;
            Yaml yaml = new Yaml(new LoaderOptions());
            Object data = yaml.load(new java.io.InputStreamReader(is, StandardCharsets.UTF_8));
            if (data == null) return null;
            if (type.isInstance(data)) return type.cast(data);
            return mapToConfig((Map<String, Object>) data, type);
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T mapToConfig(Map<String, Object> map, Class<T> type) {
        if (type == APIConfig.class) {
            APIConfig config = new APIConfig();
            if (map.containsKey("application")) {
                config.setApplication(mapToApplication((Map<String, Object>) map.get("application")));
            }
            if (map.containsKey("auth")) {
                config.setAuth(mapToAuth((Map<String, Object>) map.get("auth")));
            }
            return (T) config;
        }
        return null;
    }

    private static APIConfig.Application mapToApplication(Map<String, Object> m) {
        APIConfig.Application app = new APIConfig.Application();
        if (m != null) {
            if (m.containsKey("url")) app.setUrl(String.valueOf(m.get("url")));
            if (m.containsKey("timeout")) app.setTimeout(((Number) m.get("timeout")).intValue());
        }
        return app;
    }

    private static APIConfig.Auth mapToAuth(Map<String, Object> m) {
        APIConfig.Auth auth = new APIConfig.Auth();
        if (m != null) {
            if (m.containsKey("type")) auth.setType(String.valueOf(m.get("type")));
            if (m.containsKey("token")) auth.setToken(String.valueOf(m.get("token")));
            if (m.containsKey("apiKey")) auth.setApiKey(String.valueOf(m.get("apiKey")));
        }
        return auth;
    }

    private static APIConfig merge(APIConfig master, APIConfig override) {
        APIConfig result = new APIConfig();
        result.setApplication(mergeApplication(master.getApplication(), override.getApplication()));
        result.setAuth(mergeAuth(master.getAuth(), override.getAuth()));
        return result;
    }

    private static APIConfig.Application mergeApplication(
            APIConfig.Application master, APIConfig.Application override) {
        APIConfig.Application r = new APIConfig.Application();
        r.setUrl(override != null && override.getUrl() != null ? override.getUrl()
                : (master != null ? master.getUrl() : null));
        r.setTimeout(override != null && override.getTimeout() != null ? override.getTimeout()
                : (master != null ? master.getTimeout() : null));
        return r;
    }

    private static APIConfig.Auth mergeAuth(APIConfig.Auth master, APIConfig.Auth override) {
        APIConfig.Auth r = new APIConfig.Auth();
        if (override != null) {
            r.setType(override.getType() != null ? override.getType() : (master != null ? master.getType() : null));
            r.setToken(override.getToken() != null ? override.getToken() : (master != null ? master.getToken() : null));
            r.setApiKey(override.getApiKey() != null ? override.getApiKey() : (master != null ? master.getApiKey() : null));
        } else if (master != null) {
            r.setType(master.getType());
            r.setToken(master.getToken());
            r.setApiKey(master.getApiKey());
        }
        return r;
    }

    /**
     * Extract feature name from feature file URI (e.g. "file:.../user-api.feature" -> "user-api").
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
}
