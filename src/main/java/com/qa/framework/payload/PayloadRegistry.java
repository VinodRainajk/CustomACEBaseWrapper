package com.qa.framework.payload;

import com.qa.framework.exceptions.WrapperException;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads merged {@code *_payload.yml} and resolves {@code operations.<id>} entries.
 */
public final class PayloadRegistry {

    private static final String FEATURES_PAYLOAD_PREFIX = "features/";
    private static final String FEATURE_PAYLOAD_BASE = "payloads/features/";
    private static final String CONFIG_BASE = "config/";

    private static final ThreadLocal<String> activeFeature = new ThreadLocal<>();
    private static final ThreadLocal<Map<String, Object>> cachedDoc = new ThreadLocal<>();
    private static final ThreadLocal<String> cachedFeatureKey = new ThreadLocal<>();

    private PayloadRegistry() {
    }

    public static void setActiveFeature(String featureBaseName) {
        activeFeature.set(featureBaseName);
        cachedDoc.remove();
        cachedFeatureKey.remove();
        FeaturePayloadLoader.setActiveFeature(featureBaseName);
    }

    public static void clear() {
        activeFeature.remove();
        cachedDoc.remove();
        cachedFeatureKey.remove();
        FeaturePayloadLoader.clear();
    }

    public static String getActiveFeature() {
        return activeFeature.get();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getDocument() {
        String feature = activeFeature.get();
        if (feature == null || feature.isEmpty()) {
            return Map.of();
        }
        if (feature.equals(cachedFeatureKey.get()) && cachedDoc.get() != null) {
            return cachedDoc.get();
        }
        Map<String, Object> merged = new HashMap<>();
        deepMergeInPlace(merged, loadYamlMap(FEATURE_PAYLOAD_BASE + feature + "_payload.yml"));
        deepMergeInPlace(merged, loadYamlMap(FEATURES_PAYLOAD_PREFIX + feature + "_payload.yml"));
        String profile = com.qa.framework.config.UnifiedConfigLoader.getProfile();
        deepMergeInPlace(merged, loadYamlMap(CONFIG_BASE + profile + "/" + feature + "_payload.yml"));
        cachedFeatureKey.set(feature);
        cachedDoc.set(merged);
        return merged;
    }

    @SuppressWarnings("unchecked")
    public static PayloadOperation getOperation(String operationId) {
        if (operationId == null || operationId.isBlank()) {
            throw new WrapperException("Payload operation id is required");
        }
        Map<String, Object> doc = getDocument();
        Object opsObj = doc.get("operations");
        if (!(opsObj instanceof Map)) {
            throw new WrapperException("No 'operations' section in payload YAML for feature "
                    + activeFeature.get() + " (operation=" + operationId + ")");
        }
        Map<String, Object> operations = (Map<String, Object>) opsObj;
        Object entry = operations.get(operationId);
        if (!(entry instanceof Map)) {
            throw new WrapperException("Operation not found: " + operationId
                    + " (feature=" + activeFeature.get() + ")");
        }
        Map<String, Object> map = (Map<String, Object>) entry;
        Object typeObj = map.get("type");
        Object fileObj = map.get("file");
        if (typeObj == null || fileObj == null) {
            throw new WrapperException("Operation " + operationId + " must define 'type' and 'file'");
        }
        return new PayloadOperation(
                operationId,
                PayloadOperationType.fromString(String.valueOf(typeObj)),
                String.valueOf(fileObj).trim());
    }

    public static String readClasspathText(String classpathPath) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try (InputStream is = cl.getResourceAsStream(classpathPath)) {
            if (is == null) {
                throw new WrapperException("Classpath resource not found: " + classpathPath);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8).trim();
        } catch (WrapperException e) {
            throw e;
        } catch (Exception e) {
            throw new WrapperException("Failed to read: " + classpathPath, e);
        }
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
            return null;
        }
    }

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
}
