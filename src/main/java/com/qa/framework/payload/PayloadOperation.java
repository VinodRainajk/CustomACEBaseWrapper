package com.qa.framework.payload;

/**
 * One entry under {@code operations.<id>} in merged feature payload YAML.
 */
public final class PayloadOperation {

    private final String id;
    private final PayloadOperationType type;
    private final String file;

    public PayloadOperation(String id, PayloadOperationType type, String file) {
        this.id = id;
        this.type = type;
        this.file = file;
    }

    public String getId() {
        return id;
    }

    public PayloadOperationType getType() {
        return type;
    }

    public String getFile() {
        return file;
    }
}
