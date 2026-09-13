package com.qa.framework.declarative;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The parsing rules that decide what a {@code # BA:} marker owns. */
class BundleLoaderTest {

    private final List<String> errors = new ArrayList<>();

    @Test
    void aMarkerOwnsEveryStepUntilTheNextMarker() {
        List<Bundle> bundles = parse("""
                @Bundle
                Feature: Bundles - payment

                  @bundle:paymentSettlement
                  Scenario: Payment settlement
                    # BA: a payment of {int} is settled for customer {string}
                    Given variable "amount" is "<0>"
                    And variable "customer" is "<1>"

                    # BA: the wire information is sent to the recipient
                    Then I wait for 1 seconds before next step
                """);

        assertEquals(List.of(), errors);
        assertEquals(1, bundles.size());
        Bundle bundle = bundles.get(0);
        assertEquals("paymentSettlement", bundle.name());
        assertEquals(2, bundle.segments().size());

        BundleSegment first = bundle.segments().get(0);
        assertEquals("a payment of {int} is settled for customer {string}", first.sentence());
        assertEquals(2, first.parameterCount());
        assertEquals(2, first.recipe().size());
        assertEquals(1, bundle.segments().get(1).recipe().size());
    }

    @Test
    void argumentsAreSubstitutedByPosition() {
        BundleSegment segment = parse("""
                @Bundle
                Feature: Bundles - payment

                  @bundle:payment
                  Scenario: Payment
                    # BA: a payment of {int} is settled for customer {string}
                    Given variable "amount" is "<0>"
                    And variable "customer" is "<1>"
                """).get(0).segments().get(0);

        RecipeLine resolved = segment.recipe().get(1).substitute(new Object[] {500, "jsmith"});
        assertEquals("variable \"customer\" is \"jsmith\"", resolved.text());
    }

    @Test
    void twoMarkersInARowLeaveOneWithNothingToRun() {
        parse("""
                @Bundle
                Feature: Bundles - payment

                  @bundle:payment
                  Scenario: Payment
                    # BA: the wire information is sent to the recipient
                    # BA: the payment is credited to the customer
                    Then I wait for 1 seconds before next step
                """);

        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("has no atomic steps"), errors.get(0));
        assertTrue(errors.get(0).contains("the wire information is sent to the recipient"), errors.get(0));
    }

    @Test
    void twoMarkersOnOneLineAreRejected() {
        parse("""
                @Bundle
                Feature: Bundles - payment

                  @bundle:payment
                  Scenario: Payment
                    # BA: the wire is sent # BA: the customer is credited
                    Then I wait for 1 seconds before next step
                """);

        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("two '# BA:' markers on one line"), errors.get(0));
    }

    @Test
    void stepsBeforeTheFirstMarkerAreRejected() {
        parse("""
                @Bundle
                Feature: Bundles - payment

                  @bundle:payment
                  Scenario: Payment
                    Given variable "amount" is "500"
                    # BA: the payment is settled
                    Then I wait for 1 seconds before next step
                """);

        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("before the first '# BA:' marker"), errors.get(0));
    }

    @Test
    void aScenarioWithoutABundleTagIsRejected() {
        List<Bundle> bundles = parse("""
                @Bundle
                Feature: Bundles - payment

                  Scenario: Payment
                    # BA: the payment is settled
                    Then I wait for 1 seconds before next step
                """);

        assertEquals(List.of(), bundles);
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("no @bundle:<name> tag"), errors.get(0));
    }

    @Test
    void aFeatureDescriptionThatMentionsTheMarkerIsIgnored() {
        List<Bundle> bundles = parse("""
                @Bundle
                Feature: Bundles - Google
                  # BA: is the glue. Documentation only.

                  @bundle:googleSearch
                  Scenario: Search Google
                    # BA: the user searches Google for {string}
                    When I launch the url "https://www.google.com"
                """);

        assertEquals(List.of(), errors);
        assertEquals(1, bundles.size());
        assertEquals("the user searches Google for {string}", bundles.get(0).segments().get(0).sentence());
    }

    @Test
    void aFeatureWithoutTheBundleTagIsNotABundle() {
        List<Bundle> bundles = parse("""
                Feature: Payment settlement

                  @bundle:payment
                  Scenario: Payment
                    # BA: the payment is settled
                    Then I wait for 1 seconds before next step
                """);

        assertEquals(List.of(), bundles);
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("no @Bundle tag"), errors.get(0));
    }

    @Test
    void dataTablesAndDocStringsStayWithTheirStep() {
        BundleSegment segment = parse("""
                @Bundle
                Feature: Bundles - reference data

                  @bundle:referenceData
                  Scenario: Reference data
                    # BA: the currency table holds the standard rates
                    Given I wait for 1 seconds before next step
                      | currency | rate |
                      | USD      | 1.00 |
                    Then I wait for 1 seconds before next step
                      \"""
                      any doc string
                      \"""
                """).get(0).segments().get(0);

        assertEquals(List.of(), errors);
        assertEquals(List.of(List.of("currency", "rate"), List.of("USD", "1.00")),
                segment.recipe().get(0).table());
        assertTrue(segment.recipe().get(1).docString().contains("any doc string"));
    }

    @Test
    void scenarioOutlinesAreRejected() {
        parse("""
                @Bundle
                Feature: Bundles - payment

                  @bundle:payment
                  Scenario Outline: Payment
                    # BA: the payment is settled
                    Then I wait for 1 seconds before next step
                """);

        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("Scenario Outline is not supported"), errors.get(0));
    }

    private List<Bundle> parse(String content) {
        return BundleLoader.parse("bundles/test.feature", content, errors);
    }
}
