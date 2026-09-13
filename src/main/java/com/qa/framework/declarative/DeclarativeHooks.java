package com.qa.framework.declarative;

import com.qa.framework.exceptions.WrapperException;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.TestCaseState;

import java.util.Locale;
import java.util.Set;

/**
 * Hooks the declarative layer registers itself, without a Java hook class.
 * <p>
 * They are deliberately tiny: capture the running scenario so recipes can log into the report, clean
 * up afterwards, and refuse to run a bundle file as a test.
 * </p>
 */
final class DeclarativeHooks {

    /** Runs before framework hooks such as {@code FeaturePayloadHooks} (order 0). */
    private static final int EARLY = -10_000;

    private static final String DECLARATIVE_TAG = "@declarative:";
    private static final String BUNDLE_TAG = "@bundle:";

    private DeclarativeHooks() {
    }

    /**
     * Captures the scenario so {@link DeclarativeScenarioState} can log under the current step, and
     * checks that every {@code @declarative:name} tag points at a bundle that exists.
     *
     * @param bundleNames known bundle names, lower case
     */
    static HookDefinition capture(Set<String> bundleNames) {
        return new Hook("", EARLY, "DeclarativeHooks.capture") {
            @Override
            public void execute(TestCaseState state) {
                DeclarativeScenarioState.set(state);
                for (String tag : state.getSourceTagNames()) {
                    String lower = tag.toLowerCase(Locale.ROOT);
                    String name = referencedBundle(lower);
                    if (name == null) {
                        continue;
                    }
                    if (!bundleNames.contains(name)) {
                        throw new WrapperException("'" + state.getName() + "' is tagged " + tag
                                + " but no scenario declares @bundle:" + name + "."
                                + " Check the spelling, or create the bundle.");
                    }
                }
            }
        };
    }

    /** Clears per scenario state. Registered as an after hook so it runs last. */
    static HookDefinition cleanup(AtomicStepRegistry registry) {
        return new Hook("", EARLY, "DeclarativeHooks.cleanup") {
            @Override
            public void execute(TestCaseState state) {
                registry.clearInstances();
                DeclarativeScenarioState.clear();
            }
        };
    }

    /**
     * Fails any scenario in a bundle file that a runner picks up as a test. Bundles are vocabulary,
     * not tests: they only run when a declarative sentence calls them.
     */
    static HookDefinition bundleGuard() {
        return new Hook("@Bundle", EARLY - 1, "DeclarativeHooks.bundleGuard") {
            @Override
            public void execute(TestCaseState state) {
                throw new WrapperException("'" + state.getName() + "' comes from a @Bundle file ("
                        + state.getUri() + ") and must not run as a test."
                        + " Move bundle files to a path the runners do not scan, such as"
                        + " src/test/resources/bundles, and call their sentences from a declarative feature.");
            }
        };
    }

    /** {@code @bundle:name} or {@code @declarative:name} on a BA scenario. */
    private static String referencedBundle(String lowerTag) {
        if (lowerTag.startsWith(BUNDLE_TAG)) {
            return lowerTag.substring(BUNDLE_TAG.length());
        }
        if (lowerTag.startsWith(DECLARATIVE_TAG)) {
            return lowerTag.substring(DECLARATIVE_TAG.length());
        }
        return null;
    }

    /** Shared plumbing for the hooks above. */
    private abstract static class Hook implements HookDefinition {

        private final String tagExpression;
        private final int order;
        private final String location;

        Hook(String tagExpression, int order, String location) {
            this.tagExpression = tagExpression;
            this.order = order;
            this.location = location;
        }

        @Override
        public String getTagExpression() {
            return tagExpression;
        }

        @Override
        public int getOrder() {
            return order;
        }

        @Override
        public boolean isDefinedAt(StackTraceElement stackTraceElement) {
            return false;
        }

        @Override
        public String getLocation() {
            return location;
        }
    }
}
