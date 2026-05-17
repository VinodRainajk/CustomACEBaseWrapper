package com.qa.framework.payload;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiRequestPayloadParserTest {

    @BeforeEach
    void setUp() {
        PayloadRegistry.setActiveFeature("users-api");
        ScenarioVariableStore.set("title", "Parsed title");
    }

    @AfterEach
    void tearDown() {
        PayloadRegistry.clear();
        ScenarioVariableStore.clear();
    }

    @Test
    void loadsAndResolvesCreatePostPayload() {
        PayloadOperation op = PayloadRegistry.getOperation("create_post");
        ApiRequestPayload request = ApiRequestPayloadParser.loadAndResolve(op);
        assertEquals("POST", request.getMethod());
        assertTrue(request.getUrl().contains("jsonplaceholder"));
        assertEquals("Parsed title", ((java.util.Map<?, ?>) request.getBody()).get("title"));
    }
}
