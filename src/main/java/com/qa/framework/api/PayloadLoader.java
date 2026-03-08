package com.qa.framework.api;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Loads payload files from the payloads/ directory.
 * Path in feature: user/create-user -> payloads/user/create-user.json
 */
public class PayloadLoader {

    private static final String PAYLOAD_BASE = "payloads/";
    private static final String DEFAULT_EXT = ".json";

    /**
     * Load payload content by path (relative to payloads/, no extension needed).
     * @param path e.g. user/create-user
     * @return file content or null if not found
     */
    public static String loadPayload(String path) {
        if (path == null || path.isEmpty()) return null;
        String cleanPath = path.trim();
        if (cleanPath.startsWith("payloads/")) {
            cleanPath = cleanPath.substring("payloads/".length());
        }
        String fullPath = PAYLOAD_BASE + cleanPath;
        if (!fullPath.endsWith(".json") && !fullPath.endsWith(".xml")) {
            fullPath = fullPath + DEFAULT_EXT;
        }
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fullPath)) {
            if (is == null) return null;
            Scanner s = new Scanner(is, StandardCharsets.UTF_8.name()).useDelimiter("\\A");
            return s.hasNext() ? s.next() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
