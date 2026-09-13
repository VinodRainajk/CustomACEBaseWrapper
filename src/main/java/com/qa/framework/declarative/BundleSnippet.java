package com.qa.framework.declarative;

import io.cucumber.core.backend.Snippet;

import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.Map;

/**
 * Snippet Cucumber prints for an undefined step.
 * <p>
 * The usual Cucumber suggestion is a Java method, which is exactly what this layer exists to avoid.
 * This one suggests a bundle instead, so the fix stays in Gherkin.
 * </p>
 */
final class BundleSnippet implements Snippet {

    @Override
    public MessageFormat template() {
        return new MessageFormat("""
                @Bundle
                Feature: Bundles - <area>

                  @bundle:<unique-name>
                  Scenario: <what this group covers>
                    # BA: {1}
                    When <the atomic step that does the work>
                    Then <the atomic step that checks it>
                """);
    }

    @Override
    public String tableHint() {
        return "";
    }

    @Override
    public String arguments(Map<String, Type> arguments) {
        return "";
    }

    @Override
    public String escapePattern(String pattern) {
        return pattern;
    }
}
