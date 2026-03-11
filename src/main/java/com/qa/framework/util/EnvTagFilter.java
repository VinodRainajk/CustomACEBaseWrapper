package com.qa.framework.util;

/**
 * Builds Cucumber tag expressions for environment-based feature filtering.
 * <p>
 * Environment tags: @all (all envs), @local, @dev, @qa, @staging, @prod, @nonProd (all except prod).
 * Profile is read from system property "profile" (default: local).
 * </p>
 * <p>
 * Tag expression rules:
 * <ul>
 *   <li>prod: (@all or @prod) - excludes @nonProd</li>
 *   <li>other envs: ((@all or @{profile}) or @nonProd)</li>
 * </ul>
 * </p>
 */
public final class EnvTagFilter {

    private EnvTagFilter() {}

    /**
     * Returns the environment tag expression for the current profile.
     * Profile is read from system property "profile" (default: local).
     */
    public static String getEnvTagExpression() {
        String profile = System.getProperty("profile", "local");
        return getEnvTagExpression(profile);
    }

    /**
     * Returns the environment tag expression for the given profile.
     *
     * @param profile one of: local, dev, qa, staging, prod, preprod
     */
    public static String getEnvTagExpression(String profile) {
        if (profile == null || profile.isBlank()) {
            profile = "local";
        }
        String p = profile.trim().toLowerCase();
        if ("prod".equals(p) || "production".equals(p)) {
            return "(@all or @prod)";
        }
        // local, dev, qa, staging, preprod: include @all, @{profile}, and @nonProd
        return "((@all or @" + profile + ") or @nonProd)";
    }

    /**
     * Builds the full tag filter: type tag AND environment expression.
     * Use for DB: buildFilter("@DB"), API: buildFilter("@API"), UI: buildFilter("@UI").
     */
    public static String buildFilter(String typeTag) {
        return typeTag + " and (" + getEnvTagExpression() + ")";
    }
}
