package com.qa.framework.stepdefinitions.ui;

import com.qa.framework.payload.PlaceholderResolver;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Atomic browser steps: one action or one check each, no knowledge of any application.
 * <p>
 * These are the building blocks a bundle recipe composes. "Search Google for a term" is not a step
 * here, it is a sentence in a bundle made of the steps below.
 * </p>
 * <p>
 * Locators use the prefixes described in {@link UILocator}, and every text argument goes through
 * {@link PlaceholderResolver}, so {@code $var:searchTerm} and {@code $config:baseUrl} work the same
 * way as in the API and database steps.
 * </p>
 */
public class UIActionStepDefinitions {

    @Given("I launch the url {string}")
    public void iLaunchTheUrl(String url) {
        UIStepContext.driver().get(PlaceholderResolver.resolveText(url));
    }

    @When("I enter {string} in the field {string}")
    public void iEnterInTheField(String value, String locator) {
        WebElement field = UIStepContext.visible(locator);
        field.clear();
        field.sendKeys(PlaceholderResolver.resolveText(value));
    }

    @When("I click on the element {string}")
    public void iClickOnTheElement(String locator) {
        UIStepContext.clickable(locator).click();
    }

    @When("I click the element {string} if it is visible")
    public void iClickTheElementIfItIsVisible(String locator) {
        WebElement element = UIStepContext.visibleOrNull(locator, 3);
        if (element != null) {
            element.click();
        }
    }

    @When("I clear the field {string}")
    public void iClearTheField(String locator) {
        UIStepContext.visible(locator).clear();
    }

    @When("I press enter in the field {string}")
    public void iPressEnterInTheField(String locator) {
        UIStepContext.visible(locator).sendKeys(Keys.ENTER);
    }

    @When("I wait for the element {string} to be visible")
    public void iWaitForTheElementToBeVisible(String locator) {
        UIStepContext.visible(locator);
    }

    @Then("the page title should contain {string}")
    public void thePageTitleShouldContain(String expected) {
        String resolved = PlaceholderResolver.resolveText(expected);
        UIStepContext.waiter().until(ExpectedConditions.titleContains(resolved));
    }

    @Then("the current url should contain {string}")
    public void theCurrentUrlShouldContain(String expected) {
        String resolved = PlaceholderResolver.resolveText(expected);
        UIStepContext.waiter().until(ExpectedConditions.urlContains(resolved));
    }

    @Then("the element {string} should contain the text {string}")
    public void theElementShouldContainTheText(String locator, String expected) {
        String resolved = PlaceholderResolver.resolveText(expected);
        String actual = UIStepContext.visible(locator).getText();
        assertTrue(actual.contains(resolved),
                "Element " + locator + " should contain '" + resolved + "' but was '" + actual + "'");
    }

    @Then("the page should contain the text {string}")
    public void thePageShouldContainTheText(String expected) {
        String resolved = PlaceholderResolver.resolveText(expected);
        String source = UIStepContext.driver().getPageSource();
        assertTrue(source != null && source.contains(resolved),
                "Page should contain '" + resolved + "' but it does not");
    }
}
