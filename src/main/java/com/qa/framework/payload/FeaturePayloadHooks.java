package com.qa.framework.payload;

import com.qa.framework.db.DatabaseConfigLoader;
import com.qa.framework.utils.DynamicValueUtils;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

/**
 * Sets active feature name for {@link FeaturePayloadLoader} from the running scenario URI.
 */
public class FeaturePayloadHooks {

    @Before(order = 0)
    public void captureFeatureName(Scenario scenario) {
        PayloadStepContext.clearPendingBody();
        PayloadStepContext.clearPendingSqlStatement();
        PayloadStepContext.clearPendingPreparedStatement();
        DynamicValueUtils.clearScenarioVariables();
        if (scenario.getUri() != null) {
            FeaturePayloadLoader.setActiveFeature(
                    DatabaseConfigLoader.extractFeatureName(scenario.getUri().toString()));
        }
    }

    @After(order = 10000)
    public void clearPayloadContext() {
        FeaturePayloadLoader.clear();
        PayloadStepContext.clearPendingBody();
        PayloadStepContext.clearPendingSqlStatement();
        PayloadStepContext.clearPendingPreparedStatement();
        DynamicValueUtils.clearScenarioVariables();
    }
}
