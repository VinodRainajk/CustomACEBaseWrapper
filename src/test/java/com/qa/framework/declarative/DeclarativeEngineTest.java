package com.qa.framework.declarative;

import com.acebase.context.AcebaseObjectFactory;
import com.acebase.context.TestContext;
import com.acebase.steps.Steps;
import com.qa.framework.exceptions.WrapperException;
import com.qa.framework.payload.ScenarioVariableStore;
import com.qa.framework.payload.VariableStepDefinitions;
import com.qa.framework.stepdefinitions.ui.UIActionStepDefinitions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
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
        objectFactory.addClass(VariableStepDefinitions.class);
        registry.attach(objectFactory::getInstance);

        new AtomicStepInvoker(registry).run(segment, new Object[] {"laptop"});

        assertEquals("laptop", ScenarioVariableStore.get("searchTerm"));
        assertEquals("laptop", ScenarioVariableStore.get("lastSearchTerm"));
    }

    @Test
    void instantiatePassesTheScenarioTestContextIntoUiSteps() throws Exception {
        AtomicStepRegistry registry = AtomicStepRegistry.scan(List.of("com.qa.framework.stepdefinitions.ui"));
        objectFactory.addClass(UIActionStepDefinitions.class);
        registry.attach(objectFactory::getInstance);

        Object instance = registry.instantiate(UIActionStepDefinitions.class);

        assertSame(objectFactory.getInstance(TestContext.class), testContextOf(instance));
        assertSame(objectFactory.getInstance(UIActionStepDefinitions.class), instance);
    }

    @Test
    void instantiateDoesNotLookUpTestContextStatically() {
        AtomicStepRegistry registry = AtomicStepRegistry.scan(List.of(PAYLOAD_GLUE));

        WrapperException failure = assertThrows(WrapperException.class,
                () -> registry.instantiate(VariableStepDefinitions.class));

        assertTrue(failure.getMessage().contains("no static TestContext.get()"), failure.getMessage());
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
        AtomicStepRegistry registry = AtomicStepRegistry.scan(List.of(PAYLOAD_GLUE));
        objectFactory.addClass(VariableStepDefinitions.class);
        registry.attach(objectFactory::getInstance);
        AtomicStepInvoker invoker = new AtomicStepInvoker(registry);

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

    private static TestContext<?> testContextOf(Object steps) throws Exception {
        Method getter = Steps.class.getDeclaredMethod("getTestContext");
        getter.setAccessible(true);
        return (TestContext<?>) getter.invoke(steps);
    }
}
