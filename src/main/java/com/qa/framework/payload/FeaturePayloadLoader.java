package com.qa.framework.payload;

import com.qa.framework.db.DatabaseConfigLoader;
import com.qa.framework.exceptions.WrapperException;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;

/**
 * Loads per-feature payload YAML: SQL queries, API paths, and JSON bodies in one file.
 * <p>
 * Convention: {@code payloads/features/{featureName}_payload.yml}<br>
 * Optional profile override (merged on top): {@code config/{profile}/{featureName}_payload.yml}
 * <p>
 * Example {@code user-api_payload.yml} for feature {@code user-api.feature}:
 * <pre>
 * queries:
 *   count_cities: "SELECT COUNT(*) AS c FROM city"
 * paths:
 *   user_by_id: "/users/1"
 * bodies:
 *   create_user: |
 *     {"name":"Test"}
 * prepared:
 *   city_by_id:
 *     query: "SELECT * FROM city WHERE ID = ?"
 *     parameters: ["1"]
 * </pre>
 */
public final class FeaturePayloadLoader {

    private static final String FEATURE_PAYLOAD_BASE = "payloads/features/";
    private static final String CONFIG_BASE = "config/";

    private static final ThreadLocal<String> activeFeature = new ThreadLocal<>();
    private static final ThreadLocal<Map<String, Object>> cachedDoc = new ThreadLocal<>();
    private static final ThreadLocal<String> cachedFeatureKey = new ThreadLocal<>();

    private FeaturePayloadLoader() {
    }

    /**
     * Set current feature base name (without .feature), e.g. from Scenario URI.
     */
    public static void setActiveFeature(String featureBaseName) {
        activeFeature.set(featureBaseName);
        cachedDoc.remove();
        cachedFeatureKey.remove();
    }

    public static void clear() {
        activeFeature.remove();
        cachedDoc.remove();
        cachedFeatureKey.remove();
    }

    public static String getActiveFeature() {
        return activeFeature.get();
    }

    /**
     * Merged document for the active feature (base + profile override).
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> getDocument() {
        String feature = activeFeature.get();
        if (feature == null || feature.isEmpty()) {
            return Collections.emptyMap();
        }
        if (feature.equals(cachedFeatureKey.get()) && cachedDoc.get() != null) {
            return cachedDoc.get();
        }
        Map<String, Object> base = loadYamlMap(FEATURE_PAYLOAD_BASE + feature + "_payload.yml");
        if (base == null) {
            base = new HashMap<>();
        }
        String profile = DatabaseConfigLoader.getProfile();
        Map<String, Object> override = loadYamlMap(CONFIG_BASE + profile + "/" + feature + "_payload.yml");
        Map<String, Object> merged = deepMerge(base, override);
        cachedFeatureKey.set(feature);
        cachedDoc.set(merged);
        return merged;
    }

    /**
     * Resolve a dotted path (e.g. {@code queries.count_cities}) to a scalar string.
     */
    public static String getString(String dottedPath) {
        Object v = navigate(getDocument(), dottedPath);
        if (v == null) {
            throw new WrapperException("Feature payload key not found: " + dottedPath
                    + " (feature=" + activeFeature.get() + ", file=" + activeFeature.get() + "_payload.yml)");
        }
        if (v instanceof Map || v instanceof List) {
            throw new WrapperException("Feature payload key " + dottedPath + " is not a scalar (use prepared.* for maps)");
        }
        return String.valueOf(v).trim();
    }

    /**
     * Resolve a dotted path to a map (e.g. {@code prepared.city_by_id} with query + parameters).
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> getMap(String dottedPath) {
        Object v = navigate(getDocument(), dottedPath);
        if (v == null) {
            throw new WrapperException("Feature payload map not found: " + dottedPath);
        }
        if (!(v instanceof Map)) {
            throw new WrapperException("Feature payload key " + dottedPath + " is not a map");
        }
        return (Map<String, Object>) v;
    }

    public static String getPreparedQuery(String preparedKey) {
        Map<String, Object> m = getMap("prepared." + preparedKey);
        Object q = m.get("query");
        if (q == null) {
            throw new WrapperException("prepared." + preparedKey + " must contain 'query'");
        }
        return String.valueOf(q);
    }

    public static List<String> getPreparedParameters(String preparedKey) {
        Map<String, Object> m = getMap("prepared." + preparedKey);
        Object raw = m.get("parameters");
        if (raw == null) {
            return Collections.emptyList();
        }
        if (!(raw instanceof List)) {
            throw new WrapperException("prepared." + preparedKey + ".parameters must be a list");
        }
        List<?> list = (List<?>) raw;
        return list.stream().map(String::valueOf).collect(Collectors.toList());
    }

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
            @SuppressWarnings("unchecked")
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
}
