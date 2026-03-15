package com.qa.framework.runners;

import com.acebase.runner.TestNGRunner;
import io.cucumber.testng.CucumberOptions;

/**
 * TestNG entry point for UI and API scenarios.
 * Extends ace-base TestNGRunner and adds wrapper step definition packages.
 * Glue: ace-base + wrapper UI/API. Run from template or any project that has features on classpath.
 *
 * Invoke: mvn test -Dtest=com.qa.framework.runners.UIAPITestNGRunner
 */
@CucumberOptions(
    features = "classpath:features",
    plugin = {
        "pretty",
        "com.acebase.runner.CucumberPlugin",
        "html:target/cucumber-reports/ui-api-cucumber.html",
        "json:target/cucumber-reports/ui-api-cucumber.json",
        "junit:target/cucumber-reports/ui-api-cucumber.xml"
    },
    glue = {
        "com.acebase.glue",
        "com.qa.framework.stepdefinitions.ui",
        "com.qa.framework.stepdefinitions.api"
    }
)
public class UIAPITestNGRunner extends TestNGRunner {
}
