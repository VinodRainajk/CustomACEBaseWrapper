package com.qa.framework.declarative;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Finds {@code @declarative:name} tags in feature files.
 * <p>
 * Binding a sentence to its recipe is done by the sentence itself, so these tags are traceability:
 * they say out loud which bundle a scenario draws on, and they make the two kinds of orphan visible,
 * a bundle nobody uses and a reference to a bundle that does not exist.
 * </p>
 */
final class DeclarativeReferences {

    private static final Pattern TAG =
            Pattern.compile("@(?:declarative|bundle):([^\\s@]+)", Pattern.CASE_INSENSITIVE);

    private DeclarativeReferences() {
    }

    /** Bundle name in lower case to the places that reference it. */
    static Map<String, List<String>> scan() {
        Map<String, List<String>> references = new LinkedHashMap<>();
        for (String path : DeclarativeConfig.featurePaths()) {
            for (ClasspathScanner.Resource resource : ClasspathScanner.resources(path, ".feature")) {
                if (isBundleFile(resource.content())) {
                    continue;
                }
                String[] lines = resource.content().split("\r?\n", -1);
                for (int i = 0; i < lines.length; i++) {
                    String line = lines[i].strip();
                    if (!line.startsWith("@")) {
                        continue;
                    }
                    Matcher matcher = TAG.matcher(line);
                    while (matcher.find()) {
                        references.computeIfAbsent(matcher.group(1).toLowerCase(Locale.ROOT),
                                key -> new ArrayList<>()).add(resource.name() + ":" + (i + 1));
                    }
                }
            }
        }
        return references;
    }

    private static boolean isBundleFile(String content) {
        for (String line : content.split("\r?\n", -1)) {
            String stripped = line.strip();
            if (stripped.startsWith("Feature:")) {
                return false;
            }
            for (String token : stripped.split("\\s+")) {
                if (token.equalsIgnoreCase("@Bundle")) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Warnings for bundles nothing references and for references with no bundle. */
    static List<String> orphans(List<Bundle> bundles) {
        Map<String, List<String>> references = scan();
        Set<String> names = new LinkedHashSet<>();
        for (Bundle bundle : bundles) {
            names.add(bundle.name().toLowerCase(Locale.ROOT));
        }

        List<String> warnings = new ArrayList<>();
        for (Map.Entry<String, List<String>> reference : references.entrySet()) {
            if (!names.contains(reference.getKey())) {
                warnings.add("@declarative:" + reference.getKey() + " at " + String.join(", ", reference.getValue())
                        + " has no matching @bundle: tag");
            }
        }
        for (Bundle bundle : bundles) {
            if (!references.containsKey(bundle.name().toLowerCase(Locale.ROOT))) {
                warnings.add(bundle + " is not referenced by any @declarative: tag."
                        + " Either add the tag to the scenario that uses it, or delete the bundle.");
            }
        }
        return warnings;
    }
}
