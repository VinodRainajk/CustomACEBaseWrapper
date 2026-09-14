package com.qa.framework.declarative;

import com.acebase.context.AcebaseObjectFactory;
import com.qa.framework.exceptions.WrapperException;
import com.qa.framework.payload.ScenarioVariableStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/** Loads the real bundles from the classpath and runs one recipe against the real step definitions. */
class DeclarativeEngineTest {

    private static final String PAYLOAD_GLUE = "com.qa.framework.payload";

    private final AcebaseObjectFactory objectFactory = new AcebaseObjectFactory();

    @BeforeEach
    void startScenarioContext() {
        objectFactory.start();
    }

    @AfterEach
    void clearVariables() {
        objectFactory.stop();
        ScenarioVariableStore.clear();
    }

    @Test
    void aSentenceRunsItsRecipeAgainstTheRealStepDefinitions() {
        BundleSegment segment = segment("the tester records a search term of {string}");
        AtomicStepRegistry registry = AtomicStepRegistry.scan(List.of(PAYLOAD_GLUE));

        new AtomicStepInvoker(registry).run(segment, new Object[] {"laptop"});

        assertEquals("laptop", ScenarioVariableStore.get("searchTerm"));
        assertEquals("laptop", ScenarioVariableStore.get("lastSearchTerm"));
    }

    @Test
    void theDemoBundleHasNoValidationErrors() {
        AtomicStepRegistry registry = AtomicStepRegistry.scan(List.of(PAYLOAD_GLUE));

        List<BundleValidator.Problem> errors = BundleValidator.validate(BundleLoader.load(), registry).stream()
                .filter(BundleValidator.Problem::error)
                .toList();

        assertEquals(List.of(), errors);
    }

    @Test
    void anUnknownAtomicStepFailsWithTheSentenceAndTheLine() {
        BundleSegment segment = new BundleSegment("the ledger balances", "made-up",
                "bundles/made-up.feature", 7,
                List.of(new RecipeLine("Then", "the ledger balances by magic", null, null, 8)));
        AtomicStepInvoker invoker = new AtomicStepInvoker(AtomicStepRegistry.scan(List.of(PAYLOAD_GLUE)));

        WrapperException failure = assertThrows(WrapperException.class,
                () -> invoker.run(segment, new Object[0]));

        assertTrue(failure.getMessage().contains("No atomic step matches"), failure.getMessage());
    }

    @Test
    void aFailingAtomicStepReportsTheSentenceThatOwnsIt() {
        BundleSegment segment = new BundleSegment("the tester records nothing", "made-up",
                "bundles/made-up.feature", 3,
                List.of(new RecipeLine("Given", "variable \"\" is \"x\"", null, null, 4)));
        AtomicStepInvoker invoker = new AtomicStepInvoker(AtomicStepRegistry.scan(List.of(PAYLOAD_GLUE)));

        DeclarativeStepFailure failure = assertThrows(DeclarativeStepFailure.class,
                () -> invoker.run(segment, new Object[0]));

        assertTrue(failure.getMessage().contains("the tester records nothing"), failure.getMessage());
        assertTrue(failure.getMessage().contains("bundles/made-up.feature:4"), failure.getMessage());
    }

    private static BundleSegment segment(String sentence) {
        Optional<BundleSegment> found = BundleLoader.load().stream()
                .flatMap(bundle -> bundle.segments().stream())
                .filter(candidate -> candidate.sentence().equals(sentence))
                .findFirst();
        return found.orElseGet(() -> fail("No bundle declares the sentence: " + sentence));
    }
}
