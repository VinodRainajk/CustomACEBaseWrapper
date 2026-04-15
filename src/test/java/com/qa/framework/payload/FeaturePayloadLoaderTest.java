package com.qa.framework.payload;

import com.qa.framework.utils.DynamicValueUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FeaturePayloadLoaderTest {

    @AfterEach
    void tearDown() {
        FeaturePayloadLoader.clear();
        DynamicValueUtils.clearScenarioVariables();
    }

    @Test
    void resolvesSqlFromClasspathFileViaPayloadYaml() {
        FeaturePayloadLoader.setActiveFeature("demo");
        String sql = FeaturePayloadLoader.getString("queries.from_file");
        assertTrue(sql.contains("SELECT"), sql);
        assertTrue(sql.contains("1"), sql);
    }

    @Test
    void resolveBracedPayloadOrLiteralReturnsLiteralWhenNotBraced() {
        FeaturePayloadLoader.setActiveFeature("demo");
        String literal = "SELECT 2";
        assertTrue(FeaturePayloadLoader.resolveBracedPayloadOrLiteral(literal).contains("SELECT 2"));
    }

    @Test
    void resolvesAutoTokensAndReusesVarsAcrossPayloadKeys() {
        FeaturePayloadLoader.setActiveFeature("demo");
        String insert = FeaturePayloadLoader.getString("queries.dynamic_insert");
        String verify = FeaturePayloadLoader.getString("queries.dynamic_verify");

        assertFalse(insert.contains("${"), insert);
        assertFalse(verify.contains("${"), verify);
        assertTrue(insert.contains("INSERT INTO t"), insert);
        assertTrue(verify.contains("SELECT * FROM t"), verify);
    }
}
