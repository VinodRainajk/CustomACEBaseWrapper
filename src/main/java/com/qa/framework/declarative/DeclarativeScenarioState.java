package com.qa.framework.declarative;

import io.cucumber.core.backend.TestCaseState;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Per thread handle on the running scenario, captured by {@link DeclarativeHooks}.
 * <p>
 * Used to write the atomic steps of a recipe into the report underneath the declarative step, and to
 * read scenario tags. Follows the same {@code ThreadLocal} pattern as
 * {@code com.qa.framework.payload.ScenarioVariableStore}, so parallel execution stays isolated.
 * </p>
 */
final class DeclarativeScenarioState {

    private static final ThreadLocal<TestCaseState> CURRENT = new ThreadLocal<>();

    private DeclarativeScenarioState() {
    }

    static void set(TestCaseState state) {
        CURRENT.set(state);
    }

    static void clear() {
        CURRENT.remove();
    }

    static Optional<TestCaseState> current() {
        return Optional.ofNullable(CURRENT.get());
    }

    /** Tags of the running scenario, or an empty list outside a scenario. */
    static Collection<String> tags() {
        TestCaseState state = CURRENT.get();
        return state == null ? List.of() : state.getSourceTagNames();
    }

    /** Writes a line into the report under the current step. Silent when no scenario is running. */
    static void log(String text) {
        TestCaseState state = CURRENT.get();
        if (state != null) {
            state.log(text);
        }
    }
}
