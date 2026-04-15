package com.qa.framework.utils;

import com.qa.framework.exceptions.WrapperException;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolves dynamic tokens in payload/query/body text.
 * Supported tokens:
 * ${auto.uuid}, ${auto.email}, ${auto.now.iso}, ${auto.now.sql}, ${auto.int:min:max}, ${vars.name}
 * Optional store syntax: ${auto.uuid->vars.someKey}
 */
public final class DynamicValueUtils {

    private static final Pattern TOKEN_PATTERN = Pattern.compile("\\$\\{([^}]+)}");
    private static final ThreadLocal<Map<String, String>> SCENARIO_VARIABLES =
            ThreadLocal.withInitial(HashMap::new);
    private static final DateTimeFormatter SQL_TS_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private DynamicValueUtils() {
    }

    public static String resolveTokens(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        Matcher matcher = TOKEN_PATTERN.matcher(input);
        StringBuffer out = new StringBuffer();
        while (matcher.find()) {
            String tokenBody = matcher.group(1).trim();
            String replacement = resolveSingleToken(tokenBody);
            matcher.appendReplacement(out, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(out);
        return out.toString();
    }

    public static void clearScenarioVariables() {
        SCENARIO_VARIABLES.remove();
    }

    public static Map<String, String> snapshotScenarioVariables() {
        return new HashMap<>(SCENARIO_VARIABLES.get());
    }

    public static void setScenarioVariable(String key, String value) {
        if (key == null || key.isBlank()) {
            throw new WrapperException("Variable key cannot be blank.");
        }
        SCENARIO_VARIABLES.get().put(key, value == null ? "" : value);
    }

    private static String resolveSingleToken(String tokenBody) {
        String expr = tokenBody;
        String storeKey = null;

        int storeIdx = tokenBody.indexOf("->vars.");
        if (storeIdx >= 0) {
            expr = tokenBody.substring(0, storeIdx).trim();
            storeKey = tokenBody.substring(storeIdx + "->vars.".length()).trim();
            if (storeKey.isEmpty()) {
                throw new WrapperException("Invalid variable store token: ${" + tokenBody + "}");
            }
        }

        String value = evaluateExpression(expr);
        if (storeKey != null) {
            SCENARIO_VARIABLES.get().put(storeKey, value);
        }
        return value;
    }

    private static String evaluateExpression(String expr) {
        if (expr.startsWith("vars.")) {
            String key = expr.substring("vars.".length()).trim();
            String value = SCENARIO_VARIABLES.get().get(key);
            if (value == null) {
                throw new WrapperException("Variable not found: vars." + key);
            }
            return value;
        }
        if ("auto.uuid".equals(expr)) {
            return UUID.randomUUID().toString();
        }
        if ("auto.email".equals(expr)) {
            return "qa+" + UUID.randomUUID().toString().replace("-", "") + "@example.com";
        }
        if ("auto.now.iso".equals(expr)) {
            return LocalDateTime.now(ZoneOffset.UTC).toString();
        }
        if ("auto.now.sql".equals(expr)) {
            return LocalDateTime.now().format(SQL_TS_FORMAT);
        }
        if (expr.startsWith("auto.int:")) {
            String[] parts = expr.split(":");
            if (parts.length != 3) {
                throw new WrapperException("Invalid auto.int format. Use auto.int:min:max");
            }
            int min = parseInt(parts[1], "auto.int min");
            int max = parseInt(parts[2], "auto.int max");
            if (max < min) {
                throw new WrapperException("auto.int max must be >= min");
            }
            int value = ThreadLocalRandom.current().nextInt(min, max + 1);
            return String.valueOf(value);
        }
        throw new WrapperException("Unsupported dynamic token expression: " + expr);
    }

    private static int parseInt(String raw, String label) {
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            throw new WrapperException("Invalid integer for " + label + ": " + raw, e);
        }
    }
}
