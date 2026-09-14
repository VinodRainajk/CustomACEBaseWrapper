package com.qa.framework.stepdefinitions.ui;

import com.acebase.context.TestContext;
import com.qa.framework.exceptions.WrapperException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Access to the browser ace-base started for this scenario.
 * <p>
 * The driver lives on the scenario {@link TestContext} that Pico constructed and passed into
 * {@code Steps(TestContext)}. Callers must pass that instance — do not look up a new context here.
 * </p>
 */
public final class UIStepContext {

    private static final String TIMEOUT_PROPERTY = "ui.timeout.seconds";
    private static final long DEFAULT_TIMEOUT_SECONDS = 10;

    private UIStepContext() {
    }

    /** The browser for this scenario. */
    public static WebDriver driver(TestContext<?> context) {
        if (context == null || context.TestDriver == null || context.TestDriver.driver == null
                || context.TestDriver.driver.Driver == null) {
            throw new WrapperException("No browser is running for this scenario."
                    + " Tag the scenario @driver so ace-base starts one before the steps run.");
        }
        return context.TestDriver.driver.Driver;
    }

    /** Waits for an element to be visible and returns it. */
    public static WebElement visible(TestContext<?> context, String locator) {
        return waiter(context).until(ExpectedConditions.visibilityOfElementLocated(UILocator.of(locator)));
    }

    /** Waits for an element to be clickable and returns it. */
    public static WebElement clickable(TestContext<?> context, String locator) {
        return waiter(context).until(ExpectedConditions.elementToBeClickable(UILocator.of(locator)));
    }

    /**
     * Returns the element if it becomes visible within {@code seconds}, otherwise {@code null}.
     * Used by optional clicks such as a cookie banner that is not always there.
     */
    public static WebElement visibleOrNull(TestContext<?> context, String locator, long seconds) {
        try {
            return new WebDriverWait(driver(context), Duration.ofSeconds(Math.max(1, seconds)))
                    .until(ExpectedConditions.visibilityOfElementLocated(UILocator.of(locator)));
        } catch (org.openqa.selenium.TimeoutException ignored) {
            return null;
        }
    }

    /** A wait bounded by {@code ui.timeout.seconds}. */
    public static WebDriverWait waiter(TestContext<?> context) {
        return new WebDriverWait(driver(context), Duration.ofSeconds(timeoutSeconds()));
    }

    private static long timeoutSeconds() {
        try {
            return Long.parseLong(System.getProperty(TIMEOUT_PROPERTY, String.valueOf(DEFAULT_TIMEOUT_SECONDS)));
        } catch (NumberFormatException e) {
            return DEFAULT_TIMEOUT_SECONDS;
        }
    }
}
