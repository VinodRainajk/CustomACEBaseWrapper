package com.qa.framework.payload;

import io.cucumber.java.en.When;

import java.util.List;

/**
 * Shared steps: load content from {@code {feature}_payload.yml} for reuse by API and DB steps.
 */
public class FeaturePayloadBodyStepDefinitions {

    @When("I set the body from feature payload {string}")
    public void iSetTheBodyFromFeaturePayload(String dottedKey) {
        PayloadStepContext.setPendingBody(FeaturePayloadLoader.getString(dottedKey));
    }

    @When("I set the SQL statement from feature payload {string}")
    public void iSetTheSqlStatementFromFeaturePayload(String dottedKey) {
        PayloadStepContext.setPendingSqlStatement(FeaturePayloadLoader.getString(dottedKey));
    }

    @When("I set the prepared statement from feature payload {string}")
    public void iSetThePreparedStatementFromFeaturePayload(String dottedKey) {
        String preparedKey = dottedKey.startsWith("prepared.") ? dottedKey.substring("prepared.".length()) : dottedKey;
        String query = FeaturePayloadLoader.getPreparedQuery(preparedKey);
        List<String> params = FeaturePayloadLoader.getPreparedParameters(preparedKey);
        PayloadStepContext.setPendingPreparedStatement(query, params);
    }
}
