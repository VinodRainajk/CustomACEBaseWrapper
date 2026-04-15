package com.qa.framework.utils;

import com.qa.framework.exceptions.WrapperException;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Shared regex helpers for step definitions and utilities.
 */
public final class RegexUtils {

    private RegexUtils() {
    }

    public static Pattern compile(String regex) {
        try {
            return Pattern.compile(regex);
        } catch (PatternSyntaxException e) {
            throw new WrapperException("Invalid regex pattern: " + regex, e);
        }
    }

    public static boolean matches(String input, String regex) {
        return compile(regex).matcher(input == null ? "" : input).find();
    }
}
