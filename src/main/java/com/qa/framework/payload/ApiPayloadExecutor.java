package com.qa.framework.payload;

import com.qa.framework.exceptions.WrapperException;
import com.qa.framework.stepdefinitions.api.APIStepContext;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Locale;
import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * Executes a resolved {@link ApiRequestPayload} and stores the response in {@link APIStepContext}.
 */
public final class ApiPayloadExecutor {

    private ApiPayloadExecutor() {
    }

    public static void execute(String operationId) {
        APIStepContext ctx = APIStepContext.getInstance();
        try {
            PayloadOperation op = PayloadRegistry.getOperation(operationId);
            if (op.getType() != PayloadOperationType.API) {
                throw new WrapperException("Operation " + operationId + " is not type API");
            }
            ApiRequestPayload request = ApiRequestPayloadParser.loadAndResolve(op);
            Response response = send(request);
            ctx.setLastResponse(response);
            ctx.setLastException(null);
            String bodyForLog = request.getBody() != null ? String.valueOf(request.getBody()) : null;
            ctx.setLastApiRequest(new APIStepContext.LastApiRequest(
                    request.getMethod(), request.getUrl(), bodyForLog));
            if (request.getUrl().startsWith("http://") || request.getUrl().startsWith("https://")) {
                ctx.setBaseUrl(extractBaseUrl(request.getUrl()));
            }
        } catch (Exception e) {
            ctx.setLastException(e);
        }
    }

    private static Response send(ApiRequestPayload request) {
        RequestSpecification spec = given();
        for (Map.Entry<String, String> header : request.headers().entrySet()) {
            spec = spec.header(header.getKey(), header.getValue());
        }

        String method = request.getMethod();
        Object body = request.getBody();
        String url = request.getUrl();

        if (body != null && !isBodyless(method)) {
            if (body instanceof String) {
                String bodyStr = (String) body;
                if (!bodyStr.isBlank()) {
                    spec = spec.contentType(ContentType.JSON).body(bodyStr);
                }
            } else {
                spec = spec.contentType(ContentType.JSON).body(body);
            }
        }

        return switch (method) {
            case "GET" -> spec.when().get(url);
            case "POST" -> spec.when().post(url);
            case "PUT" -> spec.when().put(url);
            case "PATCH" -> spec.when().patch(url);
            case "DELETE" -> spec.when().delete(url);
            default -> throw new WrapperException("Unsupported HTTP method: " + method);
        };
    }

    private static boolean isBodyless(String method) {
        return "GET".equals(method) || "DELETE".equals(method);
    }

    private static String extractBaseUrl(String url) {
        int schemeEnd = url.indexOf("://");
        if (schemeEnd < 0) {
            return url;
        }
        int pathStart = url.indexOf('/', schemeEnd + 3);
        if (pathStart < 0) {
            return url;
        }
        return url.substring(0, pathStart);
    }
}
