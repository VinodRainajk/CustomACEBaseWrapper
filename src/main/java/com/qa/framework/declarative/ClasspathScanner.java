package com.qa.framework.declarative;

import com.qa.framework.exceptions.WrapperException;

import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

/**
 * Minimal classpath scanning for resources and classes.
 * <p>
 * Deliberately dependency free: handles {@code file:} (exploded target/classes, test-classes) and
 * {@code jar:} URLs, which covers Maven builds and packaged jars. Missing directories yield an
 * empty result rather than an error.
 * </p>
 */
final class ClasspathScanner {

    private ClasspathScanner() {
    }

    /** A resource found on the classpath, with its content already read. */
    record Resource(String name, String content) {
    }

    private static ClassLoader classLoader() {
        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        return contextLoader != null ? contextLoader : ClasspathScanner.class.getClassLoader();
    }

    /**
     * When IntelliJ runs a feature, the working directory is often {@code features/} and the
     * {@code bundles} folder is a sibling that is not yet on the classpath. Look beside the
     * working directory so those files are still picked up.
     */
    static List<Resource> resourcesOnDisk(List<String> basePaths, String suffix) {
        List<Resource> found = new ArrayList<>();
        Path cwd = Path.of(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
        List<Path> roots = new ArrayList<>();
        roots.add(cwd);
        if (cwd.getParent() != null) {
            roots.add(cwd.getParent());
        }
        roots.add(cwd.resolve("src/test/resources"));
        roots.add(cwd.resolve("../src/test/resources").normalize());
        for (String basePath : basePaths) {
            String folder = normalise(basePath);
            for (Path root : roots) {
                Path dir = root.resolve(folder);
                if (Files.isDirectory(dir)) {
                    collectFromDirectory(dir, folder, suffix, found);
                }
            }
        }
        return found;
    }

    /**
     * Finds every resource under {@code basePath} whose name ends with {@code suffix}.
     *
     * @param basePath classpath directory, e.g. {@code bundles}
     * @param suffix   file suffix, e.g. {@code .feature}
     */
    static List<Resource> resources(String basePath, String suffix) {
        String normalised = normalise(basePath);
        List<Resource> found = new ArrayList<>();
        try {
            Enumeration<URL> urls = classLoader().getResources(normalised);
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                if ("jar".equals(url.getProtocol())) {
                    collectFromJar(url, normalised, suffix, found);
                } else if ("file".equals(url.getProtocol())) {
                    Path root = toPath(url);
                    if (root != null) {
                        collectFromDirectory(root, normalised, suffix, found);
                    }
                }
            }
        } catch (IOException e) {
            throw new WrapperException("Failed to scan classpath path: " + basePath, e);
        }
        return found;
    }

    /**
     * Loads every class in {@code packageName}. Classes that cannot be initialised are skipped, so a
     * glue package with an unrelated broken class does not fail the run.
     */
    static List<Class<?>> classesInPackage(String packageName) {
        String basePath = packageName.replace('.', '/');
        List<Class<?>> classes = new ArrayList<>();
        for (Resource resource : classNames(basePath)) {
            String binaryName = resource.name()
                    .substring(0, resource.name().length() - ".class".length())
                    .replace('/', '.');
            try {
                classes.add(Class.forName(binaryName, false, classLoader()));
            } catch (Throwable ignored) {
                // Not loadable on this classpath; nothing this layer can do with it.
            }
        }
        return classes;
    }

    private static List<Resource> classNames(String basePath) {
        List<Resource> found = new ArrayList<>();
        try {
            Enumeration<URL> urls = classLoader().getResources(basePath);
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                if ("jar".equals(url.getProtocol())) {
                    collectNamesFromJar(url, basePath, found);
                } else if ("file".equals(url.getProtocol())) {
                    collectNamesFromDirectory(url, basePath, found);
                }
            }
        } catch (IOException e) {
            throw new WrapperException("Failed to scan classpath package: " + basePath, e);
        }
        return found;
    }

    private static void collectFromJar(URL url, String basePath, String suffix, List<Resource> found)
            throws IOException {
        JarURLConnection connection = (JarURLConnection) url.openConnection();
        JarFile jar = connection.getJarFile();
        Enumeration<JarEntry> entries = jar.entries();
        String prefix = basePath + "/";
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String name = entry.getName();
            if (entry.isDirectory() || !name.startsWith(prefix) || !name.endsWith(suffix)) {
                continue;
            }
            try (InputStream in = jar.getInputStream(entry)) {
                found.add(new Resource(name, new String(in.readAllBytes(), StandardCharsets.UTF_8)));
            }
        }
    }

    private static void collectFromDirectory(Path root, String basePath, String suffix, List<Resource> found) {
        if (root == null || !Files.isDirectory(root)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(root)) {
            List<Path> files = paths.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(suffix))
                    .toList();
            for (Path file : files) {
                String relative = root.relativize(file).toString().replace('\\', '/');
                found.add(new Resource(basePath + "/" + relative, Files.readString(file, StandardCharsets.UTF_8)));
            }
        } catch (IOException e) {
            throw new WrapperException("Failed to read classpath directory: " + root, e);
        }
    }

    private static void collectNamesFromJar(URL url, String basePath, List<Resource> found) throws IOException {
        JarURLConnection connection = (JarURLConnection) url.openConnection();
        JarFile jar = connection.getJarFile();
        Enumeration<JarEntry> entries = jar.entries();
        String prefix = basePath + "/";
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String name = entry.getName();
            boolean directChild = name.startsWith(prefix)
                    && name.endsWith(".class")
                    && name.indexOf('/', prefix.length()) < 0;
            if (directChild) {
                found.add(new Resource(name, ""));
            }
        }
    }

    private static void collectNamesFromDirectory(URL url, String basePath, List<Resource> found) {
        Path root = toPath(url);
        if (root == null || !Files.isDirectory(root)) {
            return;
        }
        try (Stream<Path> paths = Files.list(root)) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(".class"))
                    .forEach(p -> found.add(new Resource(basePath + "/" + p.getFileName(), "")));
        } catch (IOException e) {
            throw new WrapperException("Failed to list classpath directory: " + root, e);
        }
    }

    private static Path toPath(URL url) {
        try {
            return Path.of(url.toURI());
        } catch (Exception e) {
            return null;
        }
    }

    private static String normalise(String basePath) {
        String trimmed = basePath.trim();
        if (trimmed.startsWith("classpath:")) {
            trimmed = trimmed.substring("classpath:".length());
        }
        while (trimmed.startsWith("/")) {
            trimmed = trimmed.substring(1);
        }
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }
}
