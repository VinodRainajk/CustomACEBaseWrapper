package com.qa.framework.declarative;

import io.cucumber.cucumberexpressions.Expression;
import io.cucumber.cucumberexpressions.ExpressionFactory;
import io.cucumber.cucumberexpressions.ParameterTypeRegistry;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Startup checks over the loaded bundles.
 * <p>
 * Errors are things that would break the run whatever it does next: a sentence that cannot compile, a
 * placeholder with no argument to fill it, two sentences that match the same text, or a sentence that
 * collides with an atomic step. Warnings are advisory, most importantly a recipe line no atomic step
 * matches: that is fatal only if the sentence actually runs, because each runner loads a different
 * glue and a UI recipe is legitimately unresolvable during a database only run.
 * </p>
 */
final class BundleValidator {

    /** One validation finding. */
    record Problem(boolean error, String message) {
    }

    private BundleValidator() {
    }

    static List<Problem> validate(List<Bundle> bundles, AtomicStepRegistry registry) {
        List<Problem> problems = new ArrayList<>();
        ExpressionFactory expressions = new ExpressionFactory(new ParameterTypeRegistry(Locale.ENGLISH));

        Map<BundleSegment, Expression> compiled = new LinkedHashMap<>();
        Map<String, BundleSegment> bySentence = new LinkedHashMap<>();

        for (Bundle bundle : bundles) {
            for (BundleSegment segment : bundle.segments()) {
                BundleSegment duplicate = bySentence.putIfAbsent(segment.sentence(), segment);
                if (duplicate != null) {
                    problems.add(new Problem(true, "the same sentence is declared twice: "
                            + segment + " and " + duplicate
                            + ". Reword one of them, or move the shared steps into a single sentence."));
                    continue;
                }
                try {
                    compiled.put(segment, expressions.createExpression(segment.sentence()));
                } catch (RuntimeException e) {
                    problems.add(new Problem(true, "sentence is not a valid Cucumber Expression: "
                            + segment + " - " + e.getMessage()));
                    continue;
                }
                checkPlaceholders(segment, problems);
                checkStyle(segment, problems);
                checkRecipe(segment, registry, problems);
            }
        }

        checkCollisions(compiled, registry, problems);
        return problems;
    }

    /** A {@code <2>} in a recipe needs a third argument in the sentence. */
    private static void checkPlaceholders(BundleSegment segment, List<Problem> problems) {
        int available = segment.parameterCount();
        for (RecipeLine line : segment.recipe()) {
            int highest = line.highestPlaceholderIndex();
            if (highest >= available) {
                problems.add(new Problem(true, "recipe uses <" + highest + "> but " + segment
                        + " captures " + available + " argument(s) at line " + line.lineNumber()));
            }
        }
    }

    private static void checkStyle(BundleSegment segment, List<Problem> problems) {
        if (segment.sentence().startsWith("I ")) {
            problems.add(new Problem(false, "sentence reads like an atomic step: " + segment
                    + ". Business sentences describe outcomes ('the payment is settled')"
                    + " while atomic steps describe actions ('I execute the query ...')."));
        }
    }

    private static void checkRecipe(BundleSegment segment, AtomicStepRegistry registry, List<Problem> problems) {
        for (RecipeLine line : segment.recipe()) {
            try {
                registry.resolve(line.substitute(placeholders(segment.parameterCount())));
            } catch (RuntimeException e) {
                problems.add(new Problem(false, "recipe line at " + segment.sourceFile() + ":"
                        + line.lineNumber() + " does not resolve with the glue this runner loads."
                        + " It will fail if '" + segment.sentence() + "' runs here. " + e.getMessage()));
            }
        }
    }

    /** Placeholder values used only to make a recipe line resolvable during validation. */
    private static Object[] placeholders(int count) {
        Object[] values = new Object[count];
        for (int i = 0; i < count; i++) {
            values[i] = "probe";
        }
        return values;
    }

    private static void checkCollisions(Map<BundleSegment, Expression> compiled,
                                        AtomicStepRegistry registry,
                                        List<Problem> problems) {
        List<BundleSegment> segments = new ArrayList<>(compiled.keySet());
        for (int i = 0; i < segments.size(); i++) {
            BundleSegment segment = segments.get(i);
            String probe = SentenceProbe.of(segment.sentence());
            if (probe == null) {
                continue;
            }
            if (registry.matchesAny(probe)) {
                problems.add(new Problem(true, "sentence collides with an atomic step: " + segment
                        + ". Both match '" + probe + "', so Cucumber cannot tell them apart."));
            }
            for (int j = i + 1; j < segments.size(); j++) {
                BundleSegment other = segments.get(j);
                if (compiled.get(other).match(probe) != null) {
                    problems.add(new Problem(true, "two sentences match the same text '" + probe + "': "
                            + segment + " and " + other
                            + ". Give them different wording, for example name the payment type."));
                }
            }
        }
    }
}
