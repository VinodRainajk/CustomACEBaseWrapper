package com.qa.framework.declarative;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * One {@code # BA:} marker and the atomic steps beneath it.
 * <p>
 * The sentence is registered with Cucumber as a step definition pattern, so it is written as a
 * Cucumber Expression and supports {@code {string}}, {@code {int}}, word level alternation
 * ({@code wire/SWIFT}) and optional text ({@code row(s)}).
 * </p>
 *
 * @param sentence   the business readable sentence, used as the step pattern
 * @param bundleName value of the owning scenario's {@code @bundle:} tag
 * @param sourceFile bundle file this came from
 * @param markerLine line of the {@code # BA:} marker
 * @param recipe     atomic steps to run, in order
 */
record BundleSegment(String sentence, String bundleName, String sourceFile, int markerLine, List<RecipeLine> recipe) {

    /** Matches a Cucumber Expression parameter such as {@code {string}}, ignoring escaped braces. */
    private static final Pattern PARAMETER = Pattern.compile("(?<!\\\\)\\{[^{}]*}");

    /** Number of arguments this sentence captures. */
    int parameterCount() {
        int count = 0;
        Matcher matcher = PARAMETER.matcher(sentence);
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    /** Location reported to Cucumber, so failures point at the bundle rather than framework internals. */
    String location() {
        return sourceFile + ":" + markerLine;
    }

    @Override
    public String toString() {
        return "'" + sentence + "' (" + location() + ", @bundle:" + bundleName + ")";
    }
}
