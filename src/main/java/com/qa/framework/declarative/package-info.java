/**
 * Declarative step layer.
 * <p>
 * Registers business readable Gherkin sentences as real Cucumber step definitions at startup,
 * without any Java class per sentence. Sentences are declared by {@code # BA:} markers inside
 * bundle feature files (files whose Feature carries {@code @Bundle}); the atomic steps beneath a
 * marker are the recipe that runs when the sentence executes.
 * </p>
 * <p>
 * The layer is additive: nothing here modifies existing runners, hooks or step definitions. When no
 * bundle files are on the classpath, nothing is registered and behaviour is unchanged.
 * </p>
 *
 * @see com.qa.framework.declarative.DeclarativeBackend
 */
package com.qa.framework.declarative;
