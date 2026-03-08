package com.qa.framework.stepdefinitions.db;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

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
}
