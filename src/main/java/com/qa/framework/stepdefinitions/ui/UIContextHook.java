package com.qa.framework.stepdefinitions.ui;

import com.acebase.context.TestContext;
import io.cucumber.java.Before;

/**
 * Ace-base {@code @Before("@driver")} can run before {@code ContextSteps} creates {@link TestContext}.
 * This hook runs first so the driver factory always has a context.
 */
public class UIContextHook {

    @Before(order = 0)
    public void ensureTestContext() {
        if (TestContext.get() == null) {
            TestContext.set(new TestContext<>());
        }
    }
}
