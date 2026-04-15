package com.qa.framework.stepdefinitions.db;

import com.qa.framework.exceptions.WrapperException;
import com.qa.framework.utils.DynamicValueUtils;
import com.qa.framework.utils.FileSearchUtils;
import com.qa.framework.utils.RegexUtils;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Step definitions for file operations related to database testing.
 * Handles copy, export/import scenarios. Database-specific file ops (e.g. BFILE, UTL_FILE)
 * may require vendor-specific implementations.
 */
public class DatabaseFileStepDefinitions {

    @When("I copy the file from {string} to {string}")
    public void iCopyTheFileFromTo(String sourcePath, String destPath) {
        try {
            Path src = Paths.get(sourcePath);
            Path dest = Paths.get(destPath);
            Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            DatabaseStepContext.getInstance().setLastException(e);
        }
    }

    @Then("the file {string} should exist")
    public void theFileShouldExist(String filePath) {
        assertTrue(Files.exists(Paths.get(filePath)), "File should exist: " + filePath);
    }

    @Then("the file copy should succeed")
    public void theFileCopyShouldSucceed() {
        assertNull(DatabaseStepContext.getInstance().getLastException(), "File copy should succeed without exception");
    }

    @Then("file {string} should exist in path {string}")
    public void fileShouldExistInPath(String fileName, String directoryPath) {
        assertTrue(
                FileSearchUtils.fileExistsInPath(fileName, directoryPath),
                "Expected file '" + fileName + "' to exist in directory: " + directoryPath
        );
    }

    @Then("file {string} should not exist in path {string}")
    public void fileShouldNotExistInPath(String fileName, String directoryPath) {
        assertFalse(
                FileSearchUtils.fileExistsInPath(fileName, directoryPath),
                "Expected file '" + fileName + "' to not exist in directory: " + directoryPath
        );
    }

    @Then("at least {int} files matching {string} should exist in path {string}")
    public void atLeastFilesMatchingShouldExistInPath(int minCount, String globPattern, String directoryPath) {
        long count = FileSearchUtils.countFilesMatchingGlob(globPattern, directoryPath);
        assertTrue(count >= minCount,
                "Expected at least " + minCount + " files matching '" + globPattern + "' in " + directoryPath
                        + " but found " + count);
    }

    @Then("at least {int} files matching regex {string} should exist in path {string}")
    public void atLeastFilesMatchingRegexShouldExistInPath(int minCount, String regexPattern, String directoryPath) {
        List<Path> matches = FileSearchUtils.findFilesMatchingRegex(regexPattern, directoryPath);
        assertTrue(matches.size() >= minCount,
                "Expected at least " + minCount + " files matching regex '" + regexPattern + "' in " + directoryPath
                        + " but found " + matches.size());
    }

    @Then("file name matching regex {string} should exist in path {string}")
    public void fileNameMatchingRegexShouldExistInPath(String regexPattern, String directoryPath) {
        List<Path> matches = FileSearchUtils.findFilesMatchingRegex(regexPattern, directoryPath);
        assertFalse(matches.isEmpty(),
                "Expected at least one file matching regex '" + regexPattern + "' in " + directoryPath);
    }

    @Then("I capture first file matching regex {string} in path {string} as {string}")
    public void iCaptureFirstFileMatchingRegexInPathAs(String regexPattern, String directoryPath, String variableName) {
        List<Path> matches = FileSearchUtils.findFilesMatchingRegex(regexPattern, directoryPath);
        if (matches.isEmpty()) {
            throw new WrapperException("No files found matching regex '" + regexPattern + "' in " + directoryPath);
        }
        DynamicValueUtils.setScenarioVariable(variableName, matches.get(0).toString());
    }

    @Then("captured file path {string} should match regex {string}")
    public void capturedFilePathShouldMatchRegex(String variableName, String regexPattern) {
        String pathValue = DynamicValueUtils.resolveTokens("${vars." + variableName + "}");
        assertTrue(RegexUtils.matches(pathValue, regexPattern),
                "Captured file path '" + pathValue + "' did not match regex '" + regexPattern + "'");
    }
}
