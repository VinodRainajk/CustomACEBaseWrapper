package com.qa.framework.declarative;

import java.util.List;

/**
 * Runs the recipe behind one declarative sentence.
 * <p>
 * Each atomic step is resolved against {@link AtomicStepRegistry}, logged under the declarative step
 * so the report shows what actually ran, then invoked. The first failure stops the recipe and is
 * reported with the sentence, the failing line and the bundle location.
 * </p>
 */
final class AtomicStepInvoker {

    private final AtomicStepRegistry registry;

    AtomicStepInvoker(AtomicStepRegistry registry) {
        this.registry = registry;
    }

    /**
     * Executes every atomic step of the segment.
     *
     * @param segment   the sentence being run
     * @param arguments arguments captured by the sentence, substituted into {@code <0>} placeholders
     */
    void run(BundleSegment segment, Object[] arguments) {
        List<RecipeLine> recipe = segment.recipe();
        boolean report = DeclarativeConfig.reportSubsteps();

        for (int index = 0; index < recipe.size(); index++) {
            RecipeLine line = recipe.get(index).substitute(arguments);
            AtomicStepRegistry.Match match = registry.resolve(line);
            if (report) {
                DeclarativeScenarioState.log(line.keyword() + " " + line.display());
            }
            try {
                registry.invoke(match);
            } catch (Throwable failure) {
                throw new DeclarativeStepFailure(segment, line, index + 1, recipe.size(), failure);
            }
        }
    }
}
