package com.qa.framework.stepdefinitions.ui;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UILocatorTest {

    @Test
    void namePrefixMatchesGoogleSearchBox() {
        assertEquals(By.name("q"), UILocator.of("name=q"));
    }

    @Test
    void xpathPrefixKeepsTheExpression() {
        assertEquals(By.xpath("//button[normalize-space()='Accept all']"),
                UILocator.of("xpath=//button[normalize-space()='Accept all']"));
    }

    @Test
    void valueWithoutPrefixIsCss() {
        assertEquals(By.cssSelector("#search"), UILocator.of("#search"));
    }
}
