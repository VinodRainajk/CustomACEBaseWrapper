package com.qa.framework.declarative;

import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.BackendProviderService;
import io.cucumber.core.backend.Container;
import io.cucumber.core.backend.Lookup;

import java.util.function.Supplier;

/**
 * Entry point Cucumber finds through {@code META-INF/services}.
 * <p>
 * Nothing has to be configured in a runner: as soon as this jar is on the classpath Cucumber asks this
 * provider for a backend alongside the Java one.
 * </p>
 */
public final class DeclarativeBackendProvider implements BackendProviderService {

    @Override
    public Backend create(Lookup lookup, Container container, Supplier<ClassLoader> classLoader) {
        return new DeclarativeBackend(lookup);
    }
}
