package com.qa.framework.payload;

import com.qa.framework.exceptions.WrapperException;
import com.qa.framework.utils.DynamicValueUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolves {@code $config:path}, {@code $var:name}, and {@code $auto:token} in text and nested maps.
 */
public final class PlaceholderResolver {

    private static final Pattern PLACEHOLDER =
            Pattern.compile("\\$(config|var|auto):([\\w.]+)");

    private PlaceholderResolver() {
    }

    public static String resolveText(String input) {
        if (input == null) {
            return null;
        }
        Matcher matcher = PLACEHOLDER.matcher(input);
        StringBuffer out = new StringBuffer();
        while (matcher.find()) {
            String kind = matcher.group(1);
            String name = matcher.group(2);
            String replacement = resolvePlaceholder(kind, name);
            matcher.appendReplacement(out, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(out);
        return out.toString();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> resolveMap(Map<String, Object> map) {
        if (map == null) {
            return null;
        }
        Map<String, Object> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : map.entrySet()) {
            out.put(e.getKey(), resolveValue(e.getValue()));
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private static Object resolveValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return resolveText((String) value);
        }
        if (value instanceof Map) {
            return resolveMap((Map<String, Object>) value);
        }
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            List<Object> resolved = new ArrayList<>(list.size());
            for (Object item : list) {
                resolved.add(resolveValue(item));
            }
            return resolved;
        }
        return value;
    }

    private static String resolvePlaceholder(String kind, String name) {
        return switch (kind) {
            case "config" -> ConfigValueSource.resolve(name);
            case "var" -> ScenarioVariableStore.get(name);
            case "auto" -> resolveAuto(name);
            default -> throw new WrapperException("Unknown placeholder kind: $" + kind);
        };
    }

    private static String resolveAuto(String name) {
        return DynamicValueUtils.resolveTokens("${auto." + name + "}");
    }
}
