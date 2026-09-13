package com.qa.framework.declarative;

/** Console output for startup information and warnings from the declarative layer. */
final class DeclarativeLog {

    private static final String PREFIX = "[declarative] ";

    private DeclarativeLog() {
    }

    static void info(String message) {
        System.out.println(PREFIX + message);
    }

    static void warn(String message) {
        System.out.println(PREFIX + "WARN " + message);
    }
}
