package com.qa.framework.payload;

import java.util.Collections;
import java.util.Map;

/**
 * Curl-like API request loaded from JSON and resolved via {@link PlaceholderResolver}.
 */
public final class ApiRequestPayload {

    private final String method;
    private final String url;
    private final Map<String, String> headers;
    private final Object body;

    public ApiRequestPayload(String method, String url, Map<String, String> headers, Object body) {
        this.method = method;
        this.url = url;
        this.headers = headers == null ? Collections.emptyMap() : Map.copyOf(headers);
        this.body = body;
    }

    public String getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    public Map<String, String> headers() {
        return headers;
    }

    public Object getBody() {
        return body;
    }
}
