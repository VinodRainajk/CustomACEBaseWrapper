package com.qa.framework.payload;

/**
 * Type of a registered payload operation in {@code *_payload.yml}.
 */
public enum PayloadOperationType {
    API,
    SQL;

    public static PayloadOperationType fromString(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Payload operation type is required");
        }
        return PayloadOperationType.valueOf(raw.trim().toUpperCase());
    }
}
