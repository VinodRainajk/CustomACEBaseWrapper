package com.qa.framework.declarative;

import com.qa.framework.exceptions.WrapperException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * One atomic step inside a bundle recipe, with its optional data table or doc string.
 * <p>
 * Positional placeholders {@code <0>}, {@code <1>} refer to the arguments captured by the
 * declarative sentence that owns this line. Angle brackets are used rather than braces so bundle
 * arguments never look like payload keys such as {@code {queries.count_cities}}.
 * </p>
 *
 * @param keyword    Gherkin keyword as written, for reporting only
 * @param text       step text without the keyword
 * @param table      data table rows, or {@code null}
 * @param docString  doc string content, or {@code null}
 * @param lineNumber line in the bundle file, for error messages
 */
record RecipeLine(String keyword, String text, List<List<String>> table, String docString, int lineNumber) {

    private static final Pattern PLACEHOLDER = Pattern.compile("<(\\d+)>");

    /** Highest placeholder index used by this line, or -1 when it uses none. */
    int highestPlaceholderIndex() {
        int highest = -1;
        Matcher matcher = PLACEHOLDER.matcher(text);
        while (matcher.find()) {
            highest = Math.max(highest, Integer.parseInt(matcher.group(1)));
        }
        if (docString != null) {
            highest = Math.max(highest, highestIn(docString));
        }
        if (table != null) {
            for (List<String> row : table) {
                for (String cell : row) {
                    highest = Math.max(highest, highestIn(cell));
                }
            }
        }
        return highest;
    }

    /** Returns a copy with every {@code <n>} replaced by the matching captured argument. */
    RecipeLine substitute(Object[] args) {
        String resolvedText = replace(text, args);
        String resolvedDocString = docString == null ? null : replace(docString, args);
        List<List<String>> resolvedTable = null;
        if (table != null) {
            resolvedTable = new ArrayList<>(table.size());
            for (List<String> row : table) {
                List<String> resolvedRow = new ArrayList<>(row.size());
                for (String cell : row) {
                    resolvedRow.add(replace(cell, args));
                }
                resolvedTable.add(resolvedRow);
            }
        }
        return new RecipeLine(keyword, resolvedText, resolvedTable, resolvedDocString, lineNumber);
    }

    /** How this line appears in the report. */
    String display() {
        return text;
    }

    private static int highestIn(String value) {
        int highest = -1;
        Matcher matcher = PLACEHOLDER.matcher(value);
        while (matcher.find()) {
            highest = Math.max(highest, Integer.parseInt(matcher.group(1)));
        }
        return highest;
    }

    private static String replace(String value, Object[] args) {
        Matcher matcher = PLACEHOLDER.matcher(value);
        StringBuilder out = new StringBuilder();
        int last = 0;
        while (matcher.find()) {
            int index = Integer.parseInt(matcher.group(1));
            if (index >= args.length) {
                throw new WrapperException("Recipe line uses <" + index + "> but the sentence captured only "
                        + args.length + " argument(s): " + value);
            }
            out.append(value, last, matcher.start());
            out.append(args[index] == null ? "" : String.valueOf(args[index]));
            last = matcher.end();
        }
        out.append(value.substring(last));
        return out.toString();
    }
}
