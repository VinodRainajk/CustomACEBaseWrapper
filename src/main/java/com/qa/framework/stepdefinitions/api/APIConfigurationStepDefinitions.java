package com.qa.framework.stepdefinitions.api;


import com.acebase.context.TestContext;
import com.acebase.steps.Steps;
import io.cucumber.java.en.Given;

/**
 * Step definitions for API configuration - base URL, auth, etc.
 */
public class APIConfigurationStepDefinitions extends Steps {

    public APIConfigurationStepDefinitions(TestContext<?> testContext) {
        super(testContext);
    }


    private APIStepContext ctx() {
        return APIStepContext.getInstance();
    }

    @Given("the API base URL from config")
    public void theApiBaseUrlFromConfig() {
        String url = ctx().getBaseUrl();
        if (url == null && ctx().getConfig() != null && ctx().getConfig().getApplication() != null) {
            url = ctx().getConfig().getApplication().getUrl();
            if (url != null && url.endsWith("/")) {
                url = url.substring(0, url.length() - 1);
            }
            ctx().setBaseUrl(url);
        }
    }

    @Given("I have the API base URL {string}")
    public void iHaveTheApiBaseUrl(String baseUrl) {
        String url = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        ctx().setBaseUrl(url);
    }
}
