package com.qa.framework.declarative;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** Probes stand in for real step text during the startup ambiguity checks. */
class SentenceProbeTest {

    @Test
    void parametersBecomeConcreteValues() {
        assertEquals("a payment of 1 is settled for customer \"probe\"",
                SentenceProbe.of("a payment of {int} is settled for customer {string}"));
    }

    @Test
    void optionalTextIsKept() {
        assertEquals("the query returns 1 rows", SentenceProbe.of("the query returns {int} row(s)"));
    }

    @Test
    void alternationResolvesToItsFirstChoice() {
        assertEquals("the wire is sent", SentenceProbe.of("the wire/SWIFT is sent"));
    }

    @Test
    void unknownParameterTypesFallBackToAWord() {
        assertEquals("the account probe is closed", SentenceProbe.of("the account {accountNumber} is closed"));
    }

    @Test
    void regularExpressionsHaveNoProbe() {
        assertNull(SentenceProbe.of("^the payment is settled$"));
    }
}
