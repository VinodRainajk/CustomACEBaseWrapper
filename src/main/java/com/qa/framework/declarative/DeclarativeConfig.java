package com.qa.framework.declarative;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Configuration for the declarative layer, read from system properties.
 *
 * <table>
 *   <tr><th>Property</th><th>Default</th></tr>
 *   <tr><td>{@code declarative.bundle.paths}</td><td>{@code bundles}</td></tr>
 *   <tr><td>{@code declarative.glue.packages}</td><td>(none; glue paths come from the runner)</td></tr>
 *   <tr><td>{@code declarative.strict}</td><td>{@code true}</td></tr>
 *   <tr><td>{@code declarative.report.substeps}</td><td>{@code true}</td></tr>
 * </table>
 */
public final class DeclarativeConfig {

    private static final String BUNDLE_PATHS = "declarative.bundle.paths";
    private static final String FEATURE_PATHS = "declarative.feature.paths";
    private static final String GLUE_PACKAGES = "declarative.glue.packages";
    private static final String STRICT = "declarative.strict";
    private static final String REPORT_SUBSTEPS = "declarative.report.substeps";

    private static final String DEFAULT_BUNDLE_PATHS = "bundles";
    private static final String DEFAULT_FEATURE_PATHS = "features";

    private DeclarativeConfig() {
    }

    /** Classpath directories scanned for bundle feature files. */
    public static List<String> bundlePaths() {
        return split(System.getProperty(BUNDLE_PATHS, DEFAULT_BUNDLE_PATHS));
    }

    /**
     * Classpath directories scanned for feature files when reporting bundles nothing references.
     * Detection only, never execution.
     */
    public static List<String> featurePaths() {
        return split(System.getProperty(FEATURE_PATHS, DEFAULT_FEATURE_PATHS));
    }

    /**
     * Extra packages to scan for atomic step definitions, in addition to the glue paths the runner
     * already declares. Normally empty.
     */
    public static List<String> extraGluePackages() {
        return split(System.getProperty(GLUE_PACKAGES, ""));
    }

    /** When true (default) any bundle validation error fails the run at startup. */
    public static boolean strict() {
        return !"false".equalsIgnoreCase(System.getProperty(STRICT, "true"));
    }

    /** When true (default) each atomic step of a recipe is logged under the declarative step. */
    public static boolean reportSubsteps() {
        return !"false".equalsIgnoreCase(System.getProperty(REPORT_SUBSTEPS, "true"));
    }

    private static List<String> split(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
