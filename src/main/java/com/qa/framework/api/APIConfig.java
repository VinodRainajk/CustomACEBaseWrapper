package com.qa.framework.api;

import java.util.Map;

/**
 * API configuration model. Supports nested structure from YAML.
 * <p>
 * After load, {@link #getMergedRoot()} holds the deep-merged YAML map for dotted-path lookups
 * (e.g. {@code paths.users}, {@code application.url}).
 */
public class APIConfig {

    private Application application;
    private Auth auth;
    private Map<String, Object> custom;
    /** Deep-merged master + feature API YAML; used for dotted config keys in steps. */
    private Map<String, Object> mergedRoot;

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public Auth getAuth() {
        return auth;
    }

    public void setAuth(Auth auth) {
        this.auth = auth;
    }

    public Map<String, Object> getCustom() {
        return custom;
    }

    public void setCustom(Map<String, Object> custom) {
        this.custom = custom;
    }

    public Map<String, Object> getMergedRoot() {
        return mergedRoot;
    }

    public void setMergedRoot(Map<String, Object> mergedRoot) {
        this.mergedRoot = mergedRoot;
    }

    public static class Application {
        private String url;
        private Integer timeout;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public Integer getTimeout() {
            return timeout;
        }

        public void setTimeout(Integer timeout) {
            this.timeout = timeout;
        }
    }

    public static class Auth {
        private String type;
        private String token;
        private String apiKey;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
    }
}
