package com.qa.framework.payload;

import com.qa.framework.config.UnifiedConfigLoader;
import com.qa.framework.exceptions.WrapperException;

import java.util.Map;

/**
 * Resolves {@code $config:dotted.path} from merged profile YAML
 * ({@code config/{profile}/master-config.yaml} + feature override).
 */
public final class ConfigValueSource {

    private ConfigValueSource() {
    }

    public static String resolve(String dottedPath) {
        if (dottedPath == null || dottedPath.isBlank()) {
            throw new WrapperException("$config: path cannot be blank");
        }
        String feature = PayloadRegistry.getActiveFeature();
        if (feature == null || feature.isEmpty()) {
            feature = "default";
        }
        Map<String, Object> merged = UnifiedConfigLoader.loadMergedConfig(feature);
        Object value = UnifiedConfigLoader.navigate(merged, dottedPath.trim());
        if (value == null) {
            throw new WrapperException("Config key not found: $config:" + dottedPath
                    + " (profile=" + UnifiedConfigLoader.getProfile() + ", feature=" + feature + ")");
        }
        return String.valueOf(value);
    }
}
