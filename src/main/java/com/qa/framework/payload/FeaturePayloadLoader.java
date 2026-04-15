package com.qa.framework.payload;

import com.qa.framework.config.UnifiedConfigLoader;
import com.qa.framework.exceptions.WrapperException;
import com.qa.framework.utils.DynamicValueUtils;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;

/**
 * Loads per-feature payload YAML: SQL queries, API paths, and JSON bodies in one file.
 * <p>
 * Base files (merged in order; later layers override earlier):
 * <ol>
 *   <li>{@code payloads/features/{featureName}_payload.yml} — shared defaults</li>
 *   <li>{@code features/{featureName}_payload.yml} — co-located overrides (same stem as the feature file)</li>
 *   <li>{@code config/{profile}/{featureName}_payload.yml} — profile overlay (same {@code profile} as DB YAML)</li>
 * </ol>
 * <p>
 * Values may be:
 * <ul>
 *   <li>Plain string — inline SQL, JSON, or path fragment</li>
 *   <li>Map {@code inline: "..."} — explicit multiline text</li>
 *   <li>Map {@code file: "relative/path.sql"} — load from classpath under {@code sql/} or {@code api/}
 *       (root inferred from key: {@code queries.*}/{@code sql.*} → {@code sql/}, {@code bodies.*}/{@code api.*} → {@code api/}),
 *       or set {@code root: sql} / {@code root: api}. If {@code file} starts with {@code sql/} or {@code api/}, it is used as-is.</li>
 * </ul>
 * <p>
 * In Gherkin, pass {@code {queries.my_key}} as the sole content of a step string to resolve via this loader
 * (see {@link #resolveBracedPayloadOrLiteral(String)}).
 */
public final class FeaturePayloadLoader {

    private static final String FEATURES_PAYLOAD_PREFIX = "features/";
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
     * Merged document for the active feature (co-located YAML, payloads folder, then profile override).
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
        Map<String, Object> merged = new HashMap<>();
        Map<String, Object> payloadsDir = loadYamlMap(FEATURE_PAYLOAD_BASE + feature + "_payload.yml");
        Map<String, Object> coLocated = loadYamlMap(FEATURES_PAYLOAD_PREFIX + feature + "_payload.yml");
        deepMergeInPlace(merged, payloadsDir);
        deepMergeInPlace(merged, coLocated);
        String profile = UnifiedConfigLoader.getProfile();
        Map<String, Object> override = loadYamlMap(CONFIG_BASE + profile + "/" + feature + "_payload.yml");
        deepMergeInPlace(merged, override);
        cachedFeatureKey.set(feature);
        cachedDoc.set(merged);
        return merged;
    }

    /**
     * If {@code raw} is exactly {@code {dotted.key}}, resolves via {@link #getString(String)}.
     * Otherwise returns {@code raw} unchanged (literal SQL or path text).
     */
    public static String resolveBracedPayloadOrLiteral(String raw) {
        if (raw == null) {
            return null;
        }
        String s = raw.trim();
        if (s.length() >= 2 && s.charAt(0) == '{' && s.charAt(s.length() - 1) == '}') {
            int secondBrace = s.indexOf('{', 1);
            if (secondBrace < 0) {
                String inner = s.substring(1, s.length() - 1).trim();
                if (!inner.isEmpty() && inner.indexOf('}') < 0) {
                    return DynamicValueUtils.resolveTokens(getString(inner));
                }
            }
        }
        return DynamicValueUtils.resolveTokens(raw);
    }

    /**
     * Resolve a dotted path (e.g. {@code queries.count_cities}) to text, including {@code file}/{@code inline} maps.
     */
    @SuppressWarnings("unchecked")
    public static String getString(String dottedPath) {
        Object v = navigate(getDocument(), dottedPath);
        if (v == null) {
            throw new WrapperException("Feature payload key not found: " + dottedPath
                    + " (feature=" + activeFeature.get() + ", try features/" + activeFeature.get()
                    + "_payload.yml or payloads/features/" + activeFeature.get() + "_payload.yml)");
        }
        if (v instanceof List) {
            throw new WrapperException("Feature payload key " + dottedPath + " is a list, not a scalar");
        }
        if (v instanceof Map) {
            return resolveLeafToString(dottedPath, (Map<String, Object>) v);
        }
        return DynamicValueUtils.resolveTokens(String.valueOf(v).trim());
    }

    /**
     * Resolve a dotted payload key that points to a file-backed value and return classpath resource path.
     * <p>
     * Example:
     * <pre>
     * expected:
     *   users_csv:
     *     file: users/list.csv
     *     root: csv
     * </pre>
     * returns {@code csv/users/list.csv}.
     */
    @SuppressWarnings("unchecked")
    public static String getFileClasspathPath(String dottedPath) {
        Object v = navigate(getDocument(), dottedPath);
        if (!(v instanceof Map)) {
            throw new WrapperException("Feature payload key " + dottedPath + " is not a file map");
        }
        Map<String, Object> map = (Map<String, Object>) v;
        Object fileObj = map.get("file");
        if (fileObj == null) {
            throw new WrapperException("Feature payload key " + dottedPath + " does not contain 'file'");
        }
        String file = String.valueOf(fileObj).trim().replace('\\', '/');
        validateClasspathRelativePath(file);
        String root = map.get("root") != null ? String.valueOf(map.get("root")).trim().toLowerCase() : null;
        if (root == null || root.isEmpty()) {
            if (dottedPath.startsWith("paths.") && !file.startsWith("sql/") && !file.startsWith("api/")
                    && !file.startsWith("etl/") && !file.startsWith("ui/") && !file.startsWith("csv/")) {
                throw new WrapperException("paths.* file payload entries must set 'root' (path=" + dottedPath + ")");
            }
            root = inferResourceRootForPath(dottedPath);
        }
        return resolveClasspathPath(root, file);
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

    @SuppressWarnings("unchecked")
    public static String getPreparedQuery(String preparedKey) {
        String dotted = "prepared." + preparedKey;
        Map<String, Object> m = getMap(dotted);
        Object q = m.get("query");
        if (q == null) {
            throw new WrapperException("prepared." + preparedKey + " must contain 'query'");
        }
        if (q instanceof Map) {
            return resolveLeafToString(dotted + ".query", (Map<String, Object>) q);
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
        return list.stream()
                .map(String::valueOf)
                .map(DynamicValueUtils::resolveTokens)
                .collect(Collectors.toList());
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

    /**
     * Merges {@code fragment} into {@code target} in place (deep merge).
     */
    @SuppressWarnings("unchecked")
    private static void deepMergeInPlace(Map<String, Object> target, Map<String, Object> fragment) {
        if (target == null || fragment == null || fragment.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> e : fragment.entrySet()) {
            if (e.getValue() == null) {
                continue;
            }
            Object existing = target.get(e.getKey());
            if (existing instanceof Map && e.getValue() instanceof Map) {
                deepMergeInPlace((Map<String, Object>) existing, (Map<String, Object>) e.getValue());
            } else {
                target.put(e.getKey(), e.getValue());
            }
        }
    }

    private static String resolveLeafToString(String dottedPath, Map<String, Object> map) {
        Object inline = map.get("inline");
        if (inline != null) {
            return DynamicValueUtils.resolveTokens(String.valueOf(inline));
        }
        Object fileObj = map.get("file");
        if (fileObj != null) {
            String file = String.valueOf(fileObj).trim().replace('\\', '/');
            validateClasspathRelativePath(file);
            String root = map.get("root") != null ? String.valueOf(map.get("root")).trim().toLowerCase() : null;
            if (root == null || root.isEmpty()) {
                if (dottedPath.startsWith("paths.") && !file.startsWith("sql/") && !file.startsWith("api/")
                        && !file.startsWith("etl/") && !file.startsWith("ui/")) {
                    throw new WrapperException("paths.* file payload entries must set 'root: sql' or 'root: api' (path="
                            + dottedPath + ")");
                }
                root = inferResourceRootForPath(dottedPath);
            }
            return DynamicValueUtils.resolveTokens(readClasspathText(resolveClasspathPath(root, file)));
        }
        throw new WrapperException("Feature payload map at " + dottedPath
                + " must include 'inline', 'file', or be a prepared block with 'query'/'parameters'");
    }

    private static String inferResourceRootForPath(String dottedPath) {
        if (dottedPath.startsWith("bodies.") || dottedPath.startsWith("api.")) {
            return "api";
        }
        return "sql";
    }

    /**
     * {@code root} is {@code sql} or {@code api}. {@code file} may already be {@code sql/...} or {@code api/...}.
     */
    private static String resolveClasspathPath(String root, String file) {
        if (file.startsWith("sql/") || file.startsWith("api/") || file.startsWith("etl/")
                || file.startsWith("ui/") || file.startsWith("csv/")) {
            return file;
        }
        String r = root.endsWith("/") ? root.substring(0, root.length() - 1) : root;
        return r + "/" + file;
    }

    private static void validateClasspathRelativePath(String path) {
        if (path.isEmpty()) {
            throw new WrapperException("Payload file path is empty");
        }
        if (path.contains("..")) {
            throw new WrapperException("Invalid payload file path (.. not allowed): " + path);
        }
        if (Paths.get(path).normalize().toString().contains("..")) {
            throw new WrapperException("Invalid payload file path: " + path);
        }
    }

    private static String readClasspathText(String classpathPath) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try (InputStream is = cl.getResourceAsStream(classpathPath)) {
            if (is == null) {
                throw new WrapperException("Classpath resource not found: " + classpathPath);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8).trim();
        } catch (WrapperException e) {
            throw e;
        } catch (Exception e) {
            throw new WrapperException("Failed to read classpath resource: " + classpathPath, e);
        }
    }
}
