package com.qa.framework.stepdefinitions.api;

import com.qa.framework.api.PayloadLoader;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

/**
 * Step definitions for API requests - GET, POST, PUT, PATCH, DELETE.
 */
public class APIRequestStepDefinitions {

    private APIStepContext ctx() {
        return APIStepContext.getInstance();
    }

    @When("I send a GET request to {string}")
    public void iSendAGetRequestTo(String endpoint) {
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
        try {
            String url = buildUrl(endpoint);
            Response resp = given().when().delete(url);
            ctx().setLastResponse(resp);
            ctx().setLastException(null);
        } catch (Exception e) {
            ctx().setLastException(e);
        }
    }

    private String buildUrl(String endpoint) {
        String base = ctx().getBaseUrl();
        if (base == null) base = "";
        String path = endpoint.startsWith("/") ? endpoint : "/" + endpoint;
        return base + path;
    }
}
