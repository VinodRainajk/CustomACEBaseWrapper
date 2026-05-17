package com.qa.framework.payload;

import com.qa.framework.exceptions.WrapperException;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Loads and parses curl-like API JSON files referenced by {@link PayloadRegistry}.
 */
public final class ApiRequestPayloadParser {

    private ApiRequestPayloadParser() {
    }

    @SuppressWarnings("unchecked")
    public static ApiRequestPayload loadAndResolve(PayloadOperation operation) {
        String raw = PayloadRegistry.readClasspathText(operation.getFile());
        Yaml yaml = new Yaml(new LoaderOptions());
        Object data = yaml.load(new StringReader(raw));
        if (!(data instanceof Map)) {
            throw new WrapperException("API payload file must be a JSON object: " + operation.getFile());
        }
        Map<String, Object> map = (Map<String, Object>) data;
        Map<String, Object> resolved = PlaceholderResolver.resolveMap(map);

        Object methodObj = resolved.get("method");
        Object urlObj = resolved.get("url");
        if (methodObj == null || urlObj == null) {
            throw new WrapperException("API payload must contain 'method' and 'url': " + operation.getFile());
        }

        String method = String.valueOf(methodObj).trim().toUpperCase(Locale.ROOT);
        String url = String.valueOf(urlObj).trim();

        Map<String, String> headers = new LinkedHashMap<>();
        Object headersObj = resolved.get("headers");
        if (headersObj instanceof Map) {
            Map<String, Object> headerMap = (Map<String, Object>) headersObj;
            for (Map.Entry<String, Object> e : headerMap.entrySet()) {
                if (e.getValue() != null) {
                    headers.put(e.getKey(), String.valueOf(e.getValue()));
                }
            }
        }

        Object body = resolved.get("body");
        return new ApiRequestPayload(method, url, headers, body);
    }
}
