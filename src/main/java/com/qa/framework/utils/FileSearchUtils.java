package com.qa.framework.utils;

import com.qa.framework.exceptions.WrapperException;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility methods to find files by exact name, glob, and regex.
 */
public final class FileSearchUtils {

    private FileSearchUtils() {
    }

    public static boolean fileExistsInPath(String fileName, String directoryPath) {
        Path dir = requireDirectory(directoryPath);
        try (Stream<Path> stream = Files.walk(dir)) {
            return stream
                    .filter(Files::isRegularFile)
                    .anyMatch(p -> p.getFileName().toString().equals(fileName));
        } catch (IOException e) {
            throw new WrapperException("Failed to scan directory: " + directoryPath, e);
        }
    }

    public static long countFilesMatchingGlob(String globPattern, String directoryPath) {
        Path dir = requireDirectory(directoryPath);
        String matcher = "glob:" + globPattern;
        try (Stream<Path> stream = Files.walk(dir)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(p -> FileSystems.getDefault().getPathMatcher(matcher).matches(p.getFileName()))
                    .count();
        } catch (IOException e) {
            throw new WrapperException("Failed to scan directory: " + directoryPath, e);
        }
    }

    public static List<Path> findFilesMatchingRegex(String regexPattern, String directoryPath) {
        Path dir = requireDirectory(directoryPath);
        try (Stream<Path> stream = Files.walk(dir)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(p -> RegexUtils.matches(p.getFileName().toString(), regexPattern))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new WrapperException("Failed to scan directory: " + directoryPath, e);
        }
    }

    private static Path requireDirectory(String directoryPath) {
        Path dir = Path.of(directoryPath);
        if (!Files.exists(dir)) {
            throw new WrapperException("Directory does not exist: " + directoryPath);
        }
        if (!Files.isDirectory(dir)) {
            throw new WrapperException("Path is not a directory: " + directoryPath);
        }
        return dir;
    }
}
