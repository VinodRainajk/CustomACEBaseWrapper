package com.qa.framework.declarative;

/**
 * Failure of an atomic step inside a recipe, reported against the declarative sentence.
 * <p>
 * Wrapping is required as well as useful: atomic step methods may declare checked exceptions such as
 * {@code SQLException}, which cannot be thrown from the Cucumber backend contract. The message names
 * the sentence, the exact atomic step that failed and its bundle, so a red step in the report leads
 * straight to the line that broke.
 * </p>
 */
public class DeclarativeStepFailure extends RuntimeException {

    private static final long serialVersionUID = 1L;

    DeclarativeStepFailure(BundleSegment segment, RecipeLine line, int position, int total, Throwable cause) {
        super(describe(segment, line, position, total, cause), cause);
    }

    private static String describe(BundleSegment segment, RecipeLine line, int position, int total, Throwable cause) {
        String newLine = System.lineSeparator();
        return "'" + segment.sentence() + "' failed at atomic step " + position + " of " + total
                + newLine + "  step   : " + line.keyword() + " " + line.display()
                + newLine + "  bundle : @bundle:" + segment.bundleName() + " (" + segment.sourceFile()
                + ":" + line.lineNumber() + ")"
                + newLine + "  cause  : " + cause.getClass().getSimpleName()
                + (cause.getMessage() == null ? "" : ": " + cause.getMessage());
    }
}
