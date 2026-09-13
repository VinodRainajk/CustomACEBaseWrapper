package com.qa.framework.stepdefinitions.ui;

import com.qa.framework.exceptions.WrapperException;
import org.openqa.selenium.By;

/**
 * Turns a locator written in a feature file into a Selenium {@link By}.
 * <p>
 * Prefixes keep feature files readable and explicit: {@code name=q}, {@code id=search},
 * {@code css=input[name=q]}, {@code xpath=//input}, {@code link=Images},
 * {@code partialLink=Imag}, {@code class=gLFyf}, {@code tag=input}. Without a prefix the value is
 * treated as a CSS selector.
 * </p>
 */
public final class UILocator {

    private UILocator() {
    }

    public static By of(String locator) {
        if (locator == null || locator.isBlank()) {
            throw new WrapperException("Locator must not be empty");
        }
        int separator = locator.indexOf('=');
        if (separator < 0) {
            return By.cssSelector(locator);
        }
        String strategy = locator.substring(0, separator).strip().toLowerCase();
        String value = locator.substring(separator + 1).strip();
        return switch (strategy) {
            case "id" -> By.id(value);
            case "name" -> By.name(value);
            case "css" -> By.cssSelector(value);
            case "xpath" -> By.xpath(value);
            case "link" -> By.linkText(value);
            case "partiallink" -> By.partialLinkText(value);
            case "class" -> By.className(value);
            case "tag" -> By.tagName(value);
            default -> By.cssSelector(locator);
        };
    }
}
