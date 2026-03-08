package com.qa.framework.stepdefinitions.api;

import com.qa.framework.api.APIConfig;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;

/**
 * Shared context for API step definitions.
 */
public class APIStepContext {

    private static final ThreadLocal<APIStepContext> INSTANCE = ThreadLocal.withInitial(APIStepContext::new);

    private APIConfig config;
    private String baseUrl;
    private Response lastResponse;
    private Map<String, String> variables = new HashMap<>();
    private Exception lastException;

    public static APIStepContext getInstance() {
        return INSTANCE.get();
    }

    public static void reset() {
        INSTANCE.remove();
    }

    public APIConfig getConfig() {
        return config;
    }

    public void setConfig(APIConfig config) {
        this.config = config;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public Response getLastResponse() {
        return lastResponse;
    }

    public void setLastResponse(Response lastResponse) {
        this.lastResponse = lastResponse;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public void setVariable(String key, String value) {
        variables.put(key, value);
    }

    public String getVariable(String key) {
        return variables.get(key);
    }

    public Exception getLastException() {
        return lastException;
    }

    public void setLastException(Exception lastException) {
        this.lastException = lastException;
    }
}
