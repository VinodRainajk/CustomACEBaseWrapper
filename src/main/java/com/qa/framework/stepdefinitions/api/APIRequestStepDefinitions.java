package com.qa.framework.stepdefinitions.api;



import com.qa.framework.api.APIConfigLoader;

import com.qa.framework.api.PayloadLoader;

import com.qa.framework.exceptions.WrapperException;

import com.qa.framework.payload.FeaturePayloadLoader;

import com.qa.framework.payload.PayloadStepContext;

import io.cucumber.java.en.When;

import io.restassured.http.ContentType;

import io.restassured.response.Response;



import java.util.Locale;



import static io.restassured.RestAssured.given;



/**

 * Step definitions for API requests - GET, POST, PUT, PATCH, DELETE.

 * <p>

 * For POST/PUT/PATCH with a feature YAML body, use {@code When I set the body from feature payload "..."}

 * then {@code When I send a POST request to path from feature payload "..."} (or API config path).

 */

public class APIRequestStepDefinitions {



    private APIStepContext ctx() {

        return APIStepContext.getInstance();

    }



    /** Path from {@code payloads/features/{feature}_payload.yml} e.g. key {@code paths.user_by_id}. */

    @When("I send a GET request to path from feature payload {string}")

    public void iSendAGetRequestToPathFromFeaturePayload(String pathKey) {

        clearPendingBodyBeforeStatelessRequest();

        iSendAGetRequestTo(FeaturePayloadLoader.getString(pathKey));

    }



    /** Relative path from merged API YAML (e.g. {@code paths.users}); base URL from {@code @API} hook or Background. */

    @When("I send a GET request to path from API config {string}")

    public void iSendAGetRequestToPathFromApiConfig(String pathConfigKey) {

        clearPendingBodyBeforeStatelessRequest();

        iSendAGetRequestTo(APIConfigLoader.requireScalar(ctx().getConfig(), pathConfigKey));

    }



    /**

     * POST after {@code When I set the body from feature payload "..."}.

     */

    @When("I send a POST request to path from feature payload {string}")

    public void iSendAPostToPathFromFeaturePayloadUsingPendingBody(String pathKey) {

        try {

            sendJsonWithPendingBody("POST", FeaturePayloadLoader.getString(pathKey));

        } catch (Exception e) {

            ctx().setLastException(e);

        }

    }



    @When("I send a PUT request to path from feature payload {string}")

    public void iSendAPutToPathFromFeaturePayloadUsingPendingBody(String pathKey) {

        try {

            sendJsonWithPendingBody("PUT", FeaturePayloadLoader.getString(pathKey));

        } catch (Exception e) {

            ctx().setLastException(e);

        }

    }



    @When("I send a PATCH request to path from feature payload {string}")

    public void iSendAPatchToPathFromFeaturePayloadUsingPendingBody(String pathKey) {

        try {

            sendJsonWithPendingBody("PATCH", FeaturePayloadLoader.getString(pathKey));

        } catch (Exception e) {

            ctx().setLastException(e);

        }

    }



    @When("I send a POST request to path from API config {string}")

    public void iSendAPostToPathFromApiConfigUsingPendingBody(String pathConfigKey) {

        try {

            sendJsonWithPendingBody("POST", APIConfigLoader.requireScalar(ctx().getConfig(), pathConfigKey));

        } catch (Exception e) {

            ctx().setLastException(e);

        }

    }



    @When("I send a PUT request to path from API config {string}")

    public void iSendAPutToPathFromApiConfigUsingPendingBody(String pathConfigKey) {

        try {

            sendJsonWithPendingBody("PUT", APIConfigLoader.requireScalar(ctx().getConfig(), pathConfigKey));

        } catch (Exception e) {

            ctx().setLastException(e);

        }

    }



    @When("I send a PATCH request to path from API config {string}")

    public void iSendAPatchToPathFromApiConfigUsingPendingBody(String pathConfigKey) {

        try {

            sendJsonWithPendingBody("PATCH", APIConfigLoader.requireScalar(ctx().getConfig(), pathConfigKey));

        } catch (Exception e) {

            ctx().setLastException(e);

        }

    }



    /**

     * POST: path from API config YAML + JSON body from feature payload YAML (single line; same as set body + path).

     */

    @When("I send a POST request with path from API config {string} and body from feature payload {string}")

    public void iSendAPostWithPathFromApiConfigAndBodyFromFeature(String pathConfigKey, String bodyKey) {

        try {

            String path = APIConfigLoader.requireScalar(ctx().getConfig(), pathConfigKey);

            runJsonRequest("POST", path, FeaturePayloadLoader.getString(bodyKey));

        } catch (Exception e) {

            ctx().setLastException(e);

        }

    }



    /**

     * Same as {@link #iSendAPostWithPathFromApiConfigAndBodyFromFeature} but re-applies base URL from API config first

     * (e.g. {@code application.url}) — use when you want the full triple in one line instead of only Background.

     */

    @When("I send a POST request applying base URL from API config {string} with path from API config {string} and body from feature payload {string}")

    public void iSendAPostApplyingBasePathAndBody(String baseUrlKey, String pathConfigKey, String bodyKey) {

        try {

            String base = APIConfigLoader.requireScalar(ctx().getConfig(), baseUrlKey);

            ctx().setBaseUrl(trimTrailingSlash(base));

            String path = APIConfigLoader.requireScalar(ctx().getConfig(), pathConfigKey);

            runJsonRequest("POST", path, FeaturePayloadLoader.getString(bodyKey));

        } catch (Exception e) {

            ctx().setLastException(e);

        }

    }



    @When("I send a PUT request with path from API config {string} and body from feature payload {string}")

    public void iSendAPutWithPathFromApiConfigAndBodyFromFeature(String pathConfigKey, String bodyKey) {

        try {

            String path = APIConfigLoader.requireScalar(ctx().getConfig(), pathConfigKey);

            runJsonRequest("PUT", path, FeaturePayloadLoader.getString(bodyKey));

        } catch (Exception e) {

            ctx().setLastException(e);

        }

    }



    @When("I send a PATCH request with path from API config {string} and body from feature payload {string}")

    public void iSendAPatchWithPathFromApiConfigAndBodyFromFeature(String pathConfigKey, String bodyKey) {

        try {

            String path = APIConfigLoader.requireScalar(ctx().getConfig(), pathConfigKey);

            runJsonRequest("PATCH", path, FeaturePayloadLoader.getString(bodyKey));

        } catch (Exception e) {

            ctx().setLastException(e);

        }

    }



    @When("I send a POST request to path from feature payload {string} with body from feature payload {string}")

    public void iSendAPostRequestToPathFromFeaturePayloadWithBody(String pathKey, String bodyKey) {

        try {

            runJsonRequest("POST", FeaturePayloadLoader.getString(pathKey), FeaturePayloadLoader.getString(bodyKey));

        } catch (Exception e) {

            ctx().setLastException(e);

        }

    }



    @When("I send a PUT request to path from feature payload {string} with body from feature payload {string}")

    public void iSendAPutRequestToPathFromFeaturePayloadWithBody(String pathKey, String bodyKey) {

        try {

            runJsonRequest("PUT", FeaturePayloadLoader.getString(pathKey), FeaturePayloadLoader.getString(bodyKey));

        } catch (Exception e) {

            ctx().setLastException(e);

        }

    }



    @When("I send a PATCH request to path from feature payload {string} with body from feature payload {string}")

    public void iSendAPatchRequestToPathFromFeaturePayloadWithBody(String pathKey, String bodyKey) {

        try {

            runJsonRequest("PATCH", FeaturePayloadLoader.getString(pathKey), FeaturePayloadLoader.getString(bodyKey));

        } catch (Exception e) {

            ctx().setLastException(e);

        }

    }



    @When("I send a DELETE request to path from feature payload {string}")

    public void iSendADeleteRequestToPathFromFeaturePayload(String pathKey) {

        clearPendingBodyBeforeStatelessRequest();

        iSendADeleteRequestTo(FeaturePayloadLoader.getString(pathKey));

    }



    @When("I send a GET request to {string}")

    public void iSendAGetRequestTo(String endpoint) {

        clearPendingBodyBeforeStatelessRequest();

        try {

            String url = buildUrl(endpoint);

            Response resp = given().when().get(url);

            ctx().setLastResponse(resp);

            ctx().setLastException(null);

        } catch (Exception e) {

            ctx().setLastException(e);

        }

    }



    @When("I send a POST request to {string}")

    public void iSendAPostRequestTo(String endpoint) {

        try {

            String url = buildUrl(endpoint);

            Response resp = given().contentType(ContentType.JSON).when().post(url);

            ctx().setLastResponse(resp);

            ctx().setLastException(null);

        } catch (Exception e) {

            ctx().setLastException(e);

        }

    }



    @When("I send a POST request to {string} with payload {string}")

    public void iSendAPostRequestWithPayload(String endpoint, String payloadPath) {

        try {

            String body = PayloadLoader.loadPayload(payloadPath);

            String url = buildUrl(endpoint);

            Response resp = given()

                    .contentType(ContentType.JSON)

                    .body(body != null ? body : "{}")

                    .when()

                    .post(url);

            ctx().setLastResponse(resp);

            ctx().setLastException(null);

        } catch (Exception e) {

            ctx().setLastException(e);

        }

    }



    @When("I send a PUT request to {string}")

    public void iSendAPutRequestTo(String endpoint) {

        try {

            String url = buildUrl(endpoint);

            Response resp = given().contentType(ContentType.JSON).when().put(url);

            ctx().setLastResponse(resp);

            ctx().setLastException(null);

        } catch (Exception e) {

            ctx().setLastException(e);

        }

    }



    @When("I send a PUT request to {string} with payload {string}")

    public void iSendAPutRequestWithPayload(String endpoint, String payloadPath) {

        try {

            String body = PayloadLoader.loadPayload(payloadPath);

            String url = buildUrl(endpoint);

            Response resp = given()

                    .contentType(ContentType.JSON)

                    .body(body != null ? body : "{}")

                    .when()

                    .put(url);

            ctx().setLastResponse(resp);

            ctx().setLastException(null);

        } catch (Exception e) {

            ctx().setLastException(e);

        }

    }



    @When("I send a PATCH request to {string} with payload {string}")

    public void iSendAPatchRequestWithPayload(String endpoint, String payloadPath) {

        try {

            String body = PayloadLoader.loadPayload(payloadPath);

            String url = buildUrl(endpoint);

            Response resp = given()

                    .contentType(ContentType.JSON)

                    .body(body != null ? body : "{}")

                    .when()

                    .patch(url);

            ctx().setLastResponse(resp);

            ctx().setLastException(null);

        } catch (Exception e) {

            ctx().setLastException(e);

        }

    }



    @When("I send a DELETE request to {string}")

    public void iSendADeleteRequestTo(String endpoint) {

        clearPendingBodyBeforeStatelessRequest();

        try {

            String url = buildUrl(endpoint);

            Response resp = given().when().delete(url);

            ctx().setLastResponse(resp);

            ctx().setLastException(null);

        } catch (Exception e) {

            ctx().setLastException(e);

        }

    }



    private void clearPendingBodyBeforeStatelessRequest() {

        PayloadStepContext.clearPendingBody();

    }



    /**

     * Uses body set via {@code When I set the body from feature payload "..."}; clears it after a successful call.

     */

    private void sendJsonWithPendingBody(String httpMethod, String endpointPath) {

        String pending = PayloadStepContext.getPendingBody();

        if (pending == null) {

            ctx().setLastException(new WrapperException(

                    "No request body was set. First use: When I set the body from feature payload \"<key>\""));

            return;

        }

        runJsonRequest(httpMethod, endpointPath, pending);

        if (ctx().getLastException() == null) {

            PayloadStepContext.clearPendingBody();

        }

    }



    /** Single implementation for POST/PUT/PATCH with JSON body. */

    private void runJsonRequest(String httpMethod, String endpointPath, String bodyJson) {

        try {

            String url = buildUrl(endpointPath);

            String body = bodyJson != null ? bodyJson : "{}";

            Response resp;

            switch (httpMethod.toUpperCase(Locale.ROOT)) {

                case "POST":

                    resp = given().contentType(ContentType.JSON).body(body).when().post(url);

                    break;

                case "PUT":

                    resp = given().contentType(ContentType.JSON).body(body).when().put(url);

                    break;

                case "PATCH":

                    resp = given().contentType(ContentType.JSON).body(body).when().patch(url);

                    break;

                default:

                    throw new IllegalArgumentException("Unsupported JSON method: " + httpMethod);

            }

            ctx().setLastResponse(resp);

            ctx().setLastException(null);

        } catch (Exception e) {

            ctx().setLastException(e);

        }

    }



    private String buildUrl(String endpoint) {

        String base = ctx().getBaseUrl();

        if (base == null) {

            base = "";

        }

        String path = endpoint.startsWith("/") ? endpoint : "/" + endpoint;

        return base + path;

    }



    private static String trimTrailingSlash(String url) {

        if (url == null || url.length() <= 1) {

            return url;

        }

        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;

    }

}


