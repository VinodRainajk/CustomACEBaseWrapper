package com.qa.framework.declarative;

import com.qa.framework.exceptions.WrapperException;
import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.Lookup;
import io.cucumber.core.backend.Snippet;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Cucumber backend that registers declarative sentences as step definitions.
 * <p>
 * Cucumber asks every backend on the classpath to load its glue. The Java backend contributes methods
 * annotated with {@code @Given} and friends; this one contributes the {@code # BA:} sentences found in
 * bundle files. Both end up in the same glue, so a feature file can mix declarative and atomic steps
 * freely and reporting, tags, retries and parallelism all behave as usual.
 * </p>
 * <p>
 * With no bundle files on the classpath the only thing registered is the guard hook that refuses to
 * run a bundle as a test, so adding this library changes nothing until bundles exist.
 * </p>
 */
public final class DeclarativeBackend implements Backend {

    private static volatile List<Bundle> cachedBundles;

    private final Lookup lookup;
    private AtomicStepRegistry registry;

    public DeclarativeBackend(Lookup lookup) {
        this.lookup = lookup;
    }

    @Override
    public void loadGlue(Glue glue, List<URI> gluePaths) {
        List<Bundle> bundles = bundles();

        registry = AtomicStepRegistry.scan(packagesOf(gluePaths));
        registry.attach(lookup);
        glue.addBeforeHook(DeclarativeHooks.bundleGuard());

        if (bundles.isEmpty()) {
            return;
        }

        report(BundleValidator.validate(bundles, registry));
        for (String warning : DeclarativeReferences.orphans(bundles)) {
            DeclarativeLog.warn(warning);
        }

        AtomicStepInvoker invoker = new AtomicStepInvoker(registry);
        Set<String> bundleNames = new LinkedHashSet<>();
        int sentences = 0;
        for (Bundle bundle : bundles) {
            bundleNames.add(bundle.name().toLowerCase(Locale.ROOT));
            for (BundleSegment segment : bundle.segments()) {
                glue.addStepDefinition(new DeclarativeStepDefinition(segment, invoker));
                sentences++;
            }
        }
        glue.addBeforeHook(DeclarativeHooks.capture(bundleNames));
        glue.addAfterHook(DeclarativeHooks.cleanup(registry));

        DeclarativeLog.info("registered " + sentences + " sentence(s) from " + bundles.size()
                + " bundle(s), against " + registry.entries().size() + " atomic step(s)");
    }

    @Override
    public void buildWorld() {
        // Declarative steps hold no state of their own; scenario state lives in the framework contexts.
    }

    @Override
    public void disposeWorld() {
        if (registry != null) {
            registry.clearInstances();
        }
        DeclarativeScenarioState.clear();
    }

    @Override
    public Snippet getSnippet() {
        return new BundleSnippet();
    }

    /** Bundles are the same for every runner in the JVM, so they are parsed once. */
    private static List<Bundle> bundles() {
        List<Bundle> loaded = cachedBundles;
        if (loaded == null) {
            synchronized (DeclarativeBackend.class) {
                loaded = cachedBundles;
                if (loaded == null) {
                    loaded = List.copyOf(BundleLoader.load());
                    cachedBundles = loaded;
                }
            }
        }
        return loaded;
    }

    /**
     * Turns the runner's glue paths into package names, so the atomic steps this layer can call are
     * exactly the ones the run already loads.
     */
    private static Set<String> packagesOf(List<URI> gluePaths) {
        Set<String> packages = new LinkedHashSet<>();
        for (URI gluePath : gluePaths) {
            if (!"classpath".equals(gluePath.getScheme())) {
                continue;
            }
            String path = gluePath.getSchemeSpecificPart();
            while (path.startsWith("/")) {
                path = path.substring(1);
            }
            while (path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }
            if (!path.isEmpty()) {
                packages.add(path.replace('/', '.'));
            }
        }
        packages.addAll(DeclarativeConfig.extraGluePackages());
        return packages;
    }

    private static void report(List<BundleValidator.Problem> problems) {
        List<String> errors = new ArrayList<>();
        for (BundleValidator.Problem problem : problems) {
            if (problem.error()) {
                errors.add(problem.message());
            } else {
                DeclarativeLog.warn(problem.message());
            }
        }
        if (errors.isEmpty()) {
            return;
        }
        StringBuilder message = new StringBuilder("Bundle validation failed (").append(errors.size())
                .append(" error(s)):");
        for (String error : errors) {
            message.append(System.lineSeparator()).append("  - ").append(error);
        }
        if (!DeclarativeConfig.strict()) {
            DeclarativeLog.warn(message.toString());
            return;
        }
        message.append(System.lineSeparator())
                .append("  Run with -Ddeclarative.strict=false to downgrade these to warnings.");
        throw new WrapperException(message.toString());
    }
}
