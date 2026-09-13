package com.qa.framework.declarative;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Turns a sentence pattern into a concrete example of text that matches it.
 * <p>
 * Cucumber can tell whether two step definitions are ambiguous only when it has real step text, so
 * startup checks need a stand in. {@code a payment of {int} is settled for customer {string}} becomes
 * {@code a payment of 1 is settled for customer "probe"}, which is then matched against the atomic
 * steps and the other declarative sentences.
 * </p>
 */
final class SentenceProbe {

    private static final Pattern PARAMETER = Pattern.compile("\\{([^{}]*)}");

    private SentenceProbe() {
    }

    /** Best effort concrete text for a Cucumber Expression. Returns null for regular expressions. */
    static String of(String sentence) {
        if (sentence.startsWith("^") || sentence.endsWith("$")) {
            return null;
        }
        String text = replaceParameters(sentence);
        text = resolveOptional(text);
        text = resolveAlternation(text);
        return text.replace("\\{", "{").replace("\\}", "}").replace("\\(", "(").replace("\\)", ")");
    }

    private static String replaceParameters(String sentence) {
        Matcher matcher = PARAMETER.matcher(sentence);
        StringBuilder out = new StringBuilder();
        int last = 0;
        while (matcher.find()) {
            if (matcher.start() > 0 && sentence.charAt(matcher.start() - 1) == '\\') {
                continue;
            }
            out.append(sentence, last, matcher.start()).append(sampleFor(matcher.group(1).strip()));
            last = matcher.end();
        }
        out.append(sentence.substring(last));
        return out.toString();
    }

    private static String sampleFor(String parameterType) {
        return switch (parameterType) {
            case "int", "long", "short", "byte", "biginteger" -> "1";
            case "float", "double", "bigdecimal" -> "1.5";
            case "string" -> "\"probe\"";
            default -> "probe";
        };
    }

    /** {@code row(s)} becomes {@code rows}: the optional text is kept so the probe stays realistic. */
    private static String resolveOptional(String text) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char current = text.charAt(i);
            boolean escaped = i > 0 && text.charAt(i - 1) == '\\';
            if ((current == '(' || current == ')') && !escaped) {
                continue;
            }
            out.append(current);
        }
        return out.toString();
    }

    /** {@code wire/SWIFT} becomes {@code wire}: alternation always resolves to its first choice. */
    private static String resolveAlternation(String text) {
        String[] words = text.split(" ", -1);
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            int slash = indexOfUnescaped(word);
            if (slash > 0) {
                words[i] = word.substring(0, slash);
            }
        }
        return String.join(" ", words);
    }

    private static int indexOfUnescaped(String word) {
        for (int i = 0; i < word.length(); i++) {
            if (word.charAt(i) == '/' && (i == 0 || word.charAt(i - 1) != '\\')) {
                return i;
            }
        }
        return -1;
    }
}
