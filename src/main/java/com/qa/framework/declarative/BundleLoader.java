package com.qa.framework.declarative;

import com.qa.framework.exceptions.WrapperException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Finds and parses bundle feature files.
 * <p>
 * A bundle file is a feature file whose {@code Feature} carries {@code @Bundle}. Each scenario in it
 * must carry a {@code @bundle:name} tag, and every sentence is declared by a {@code # BA:} marker.
 * The atomic steps beneath a marker, up to the next marker or the end of the scenario, are its
 * recipe.
 * </p>
 * <p>
 * Parse errors are always fatal and are reported together, each quoting file and line.
 * </p>
 */
final class BundleLoader {

    private static final Pattern TAG_BUNDLE = Pattern.compile("^@bundle:(.+)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern MARKER = Pattern.compile("^#\\s*BA\\s*:\\s*(.+)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern MARKER_ANYWHERE = Pattern.compile("#\\s*BA\\s*:", Pattern.CASE_INSENSITIVE);
    private static final Pattern STEP = Pattern.compile("^(Given|When|Then|And|But|\\*)\\s+(.*)$");
    private static final String DOC_STRING = "\"\"\"";

    private BundleLoader() {
    }

    /** Loads every bundle from the configured classpath paths, then from nearby folders. */
    static List<Bundle> load() {
        List<Bundle> bundles = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        Map<String, Bundle> byName = new LinkedHashMap<>();

        List<ClasspathScanner.Resource> resources = new ArrayList<>();
        for (String path : DeclarativeConfig.bundlePaths()) {
            resources.addAll(ClasspathScanner.resources(path, ".feature"));
        }
        if (resources.isEmpty()) {
            resources.addAll(ClasspathScanner.resourcesOnDisk(DeclarativeConfig.bundlePaths(), ".feature"));
        }
        if (resources.isEmpty()) {
            DeclarativeLog.warn("no bundle files found under " + DeclarativeConfig.bundlePaths()
                    + ". Put @Bundle features in src/test/resources/bundles, then rebuild the project.");
        }

        for (ClasspathScanner.Resource resource : resources) {
            List<Bundle> parsed = parse(resource.name(), resource.content(), errors);
            for (Bundle bundle : parsed) {
                String key = bundle.name().toLowerCase(Locale.ROOT);
                Bundle existing = byName.putIfAbsent(key, bundle);
                if (existing != null) {
                    errors.add("duplicate bundle name @bundle:" + bundle.name()
                            + " at " + bundle.sourceFile() + ":" + bundle.scenarioLine()
                            + " and " + existing.sourceFile() + ":" + existing.scenarioLine());
                } else {
                    bundles.add(bundle);
                }
            }
        }

        if (!errors.isEmpty()) {
            throw new WrapperException(message(errors));
        }
        return bundles;
    }

    /** Parses one bundle file, collecting problems instead of throwing. Used by {@link #load()} and tests. */
    static List<Bundle> parse(String file, String content, List<String> errors) {
        return new FileParser(file, content, errors).parse();
    }

    private static String message(List<String> errors) {
        StringBuilder text = new StringBuilder("Bundle errors (").append(errors.size()).append("):");
        for (String error : errors) {
            text.append(System.lineSeparator()).append("  - ").append(error);
        }
        return text.toString();
    }

    /** Parses a single bundle file. Kept as an inner class so parsing state stays local. */
    private static final class FileParser {

        private final String file;
        private final String content;
        private final List<String> errors;

        private final List<Bundle> bundles = new ArrayList<>();
        private final List<String> pendingTags = new ArrayList<>();

        private boolean featureSeen;
        private boolean bundleFeature;

        private String bundleName;
        private String scenarioName;
        private int scenarioLine;
        /** Set when a scenario is already known to be unusable, so one mistake reports one error. */
        private boolean scenarioBroken;
        private List<BundleSegment> segments = new ArrayList<>();

        private String sentence;
        private int markerLine;
        private List<RecipeLine> recipe = new ArrayList<>();

        private RecipeLine lastLine;
        private boolean inDocString;
        private StringBuilder docString;

        FileParser(String file, String content, List<String> errors) {
            this.file = file;
            this.content = content;
            this.errors = errors;
        }

        List<Bundle> parse() {
            String[] lines = content.split("\r?\n", -1);
            for (int i = 0; i < lines.length; i++) {
                handle(lines[i], i + 1);
            }
            if (inDocString) {
                errors.add(at(lines.length) + " doc string was never closed");
            }
            finishScenario();
            if (featureSeen && !bundleFeature) {
                errors.add(file + " has no @Bundle tag on its Feature, so it is not a bundle file."
                        + " Add @Bundle, or move the file out of the bundle path.");
                return List.of();
            }
            return bundles;
        }

        private void handle(String raw, int lineNumber) {
            String line = raw.strip();

            if (inDocString) {
                if (DOC_STRING.equals(line)) {
                    attachDocString(lineNumber);
                } else {
                    docString.append(raw).append(System.lineSeparator());
                }
                return;
            }
            if (line.isEmpty()) {
                return;
            }
            if (line.startsWith("#")) {
                handleComment(line, lineNumber);
                return;
            }
            if (line.startsWith("@")) {
                collectTags(line);
                return;
            }
            if (line.startsWith("Feature:")) {
                featureSeen = true;
                bundleFeature = pendingTags.stream().anyMatch(t -> t.equalsIgnoreCase("@Bundle"));
                pendingTags.clear();
                return;
            }
            if (line.startsWith("Scenario:") || line.startsWith("Example:")) {
                startScenario(line, lineNumber);
                return;
            }
            if (line.startsWith("Scenario Outline:") || line.startsWith("Scenario Template:")
                    || line.startsWith("Examples:") || line.startsWith("Background:") || line.startsWith("Rule:")) {
                finishScenario();
                errors.add(at(lineNumber) + " " + keywordOf(line)
                        + " is not supported in a bundle file; use plain scenarios with # BA: markers");
                pendingTags.clear();
                scenarioBroken = true;
                return;
            }
            if (DOC_STRING.equals(line)) {
                openDocString(lineNumber);
                return;
            }
            if (line.startsWith("|")) {
                attachTableRow(line, lineNumber);
                return;
            }
            Matcher step = STEP.matcher(line);
            if (step.matches()) {
                addStep(step.group(1), step.group(2).strip(), lineNumber);
                return;
            }
            // Anything else is free text description under Feature or Scenario; ignored.
        }

        private void handleComment(String line, int lineNumber) {
            Matcher anywhere = MARKER_ANYWHERE.matcher(line);
            int markerCount = 0;
            while (anywhere.find()) {
                markerCount++;
            }
            if (markerCount > 1) {
                if (!scenarioBroken) {
                    errors.add(at(lineNumber) + " two '# BA:' markers on one line."
                            + " Put each sentence on its own line with its own steps.");
                    scenarioBroken = true;
                }
                return;
            }
            Matcher marker = MARKER.matcher(line);
            if (!marker.matches() || scenarioBroken) {
                return;
            }
            if (bundleName == null) {
                // Feature description may mention the marker. Only sentences inside a scenario count.
                return;
            }
            finishSegment(lineNumber);
            sentence = marker.group(1).strip();
            markerLine = lineNumber;
            recipe = new ArrayList<>();
            lastLine = null;
        }

        private void collectTags(String line) {
            for (String token : line.split("\\s+")) {
                if (token.startsWith("@")) {
                    pendingTags.add(token);
                }
            }
        }

        private void startScenario(String line, int lineNumber) {
            finishScenario();
            scenarioName = line.substring(line.indexOf(':') + 1).strip();
            scenarioLine = lineNumber;
            segments = new ArrayList<>();
            sentence = null;
            recipe = new ArrayList<>();
            lastLine = null;
            scenarioBroken = false;

            List<String> names = new ArrayList<>();
            for (String tag : pendingTags) {
                Matcher matcher = TAG_BUNDLE.matcher(tag);
                if (matcher.matches()) {
                    names.add(matcher.group(1).strip());
                }
            }
            pendingTags.clear();

            if (names.isEmpty()) {
                errors.add(at(lineNumber) + " scenario '" + scenarioName + "' has no @bundle:<name> tag");
                bundleName = null;
                scenarioBroken = true;
            } else if (names.size() > 1) {
                errors.add(at(lineNumber) + " scenario '" + scenarioName + "' has "
                        + names.size() + " @bundle: tags (" + String.join(", ", names) + "); exactly one is allowed");
                bundleName = null;
                scenarioBroken = true;
            } else {
                bundleName = names.get(0);
            }
        }

        private void addStep(String keyword, String text, int lineNumber) {
            if (bundleName == null || scenarioBroken) {
                return;
            }
            if (sentence == null) {
                errors.add(at(lineNumber) + " step appears before the first '# BA:' marker in scenario '"
                        + scenarioName + "'. Every atomic step must belong to a sentence.");
                scenarioBroken = true;
                return;
            }
            lastLine = new RecipeLine(keyword, text, null, null, lineNumber);
            recipe.add(lastLine);
        }

        private void attachTableRow(String line, int lineNumber) {
            if (scenarioBroken) {
                return;
            }
            if (lastLine == null) {
                errors.add(at(lineNumber) + " table row does not follow a step");
                return;
            }
            List<String> cells = new ArrayList<>();
            String trimmed = line.substring(1, line.endsWith("|") ? line.length() - 1 : line.length());
            for (String cell : trimmed.split("\\|", -1)) {
                cells.add(cell.strip());
            }
            List<List<String>> rows = lastLine.table() == null ? new ArrayList<>() : new ArrayList<>(lastLine.table());
            rows.add(cells);
            replaceLastLine(new RecipeLine(lastLine.keyword(), lastLine.text(), rows,
                    lastLine.docString(), lastLine.lineNumber()));
        }

        private void openDocString(int lineNumber) {
            if (scenarioBroken) {
                return;
            }
            if (lastLine == null) {
                errors.add(at(lineNumber) + " doc string does not follow a step");
                return;
            }
            inDocString = true;
            docString = new StringBuilder();
        }

        private void attachDocString(int lineNumber) {
            inDocString = false;
            if (lastLine == null) {
                errors.add(at(lineNumber) + " doc string does not follow a step");
                return;
            }
            replaceLastLine(new RecipeLine(lastLine.keyword(), lastLine.text(), lastLine.table(),
                    docString.toString(), lastLine.lineNumber()));
            docString = null;
        }

        private void replaceLastLine(RecipeLine updated) {
            recipe.set(recipe.size() - 1, updated);
            lastLine = updated;
        }

        private void finishSegment(int nextMarkerLine) {
            if (sentence == null) {
                return;
            }
            if (recipe.isEmpty()) {
                errors.add(at(markerLine) + " '" + sentence + "' has no atomic steps."
                        + (nextMarkerLine > 0 ? " The next '# BA:' marker is at line " + nextMarkerLine + "." : "")
                        + " Add the steps that verify it, or delete the marker.");
            } else {
                segments.add(new BundleSegment(sentence, bundleName, file, markerLine, List.copyOf(recipe)));
            }
            sentence = null;
            recipe = new ArrayList<>();
            lastLine = null;
        }

        private void finishScenario() {
            if (bundleName == null && scenarioName == null) {
                return;
            }
            if (!scenarioBroken) {
                finishSegment(0);
            }
            if (bundleName != null && !scenarioBroken) {
                if (segments.isEmpty()) {
                    errors.add(file + ":" + scenarioLine + " scenario '" + scenarioName
                            + "' declares @bundle:" + bundleName + " but has no '# BA:' marker");
                } else {
                    bundles.add(new Bundle(bundleName, file, scenarioName, scenarioLine, List.copyOf(segments)));
                }
            }
            bundleName = null;
            scenarioName = null;
            segments = new ArrayList<>();
        }

        private String at(int lineNumber) {
            return file + ":" + lineNumber;
        }

        private static String keywordOf(String line) {
            int colon = line.indexOf(':');
            return colon > 0 ? line.substring(0, colon) : line;
        }
    }
}
