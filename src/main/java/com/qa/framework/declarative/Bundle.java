package com.qa.framework.declarative;

import java.util.List;

/**
 * One scenario inside a bundle feature file: a uniquely named group of declarative sentences.
 * <p>
 * The scenario name is documentation only. Identity comes from the {@code @bundle:} tag, and every
 * sentence comes from a {@code # BA:} marker.
 * </p>
 *
 * @param name         value of the {@code @bundle:} tag, unique across the project
 * @param sourceFile   bundle file this came from
 * @param scenarioName scenario name, for messages
 * @param scenarioLine line of the {@code Scenario:} keyword
 * @param segments     declarative sentences declared in this scenario
 */
record Bundle(String name, String sourceFile, String scenarioName, int scenarioLine, List<BundleSegment> segments) {

    @Override
    public String toString() {
        return "@bundle:" + name + " (" + sourceFile + ":" + scenarioLine + ")";
    }
}
