# Custom ACE Base Wrapper

A standalone Java library JAR providing base wrapper functionality and Cucumber step definitions for database testing. This library is designed to be imported as a Maven dependency in test projects.

## 🎯 Purpose

This is a **library JAR** that contains:
- ✅ Database connection management
- ✅ Configuration system for multiple database types
- ✅ Cucumber step definitions for database testing
- ✅ Base wrapper classes for custom implementations

This library does **NOT** contain:
- ❌ Feature files (these go in your test project)
- ❌ Test runners (these go in your test project)
- ❌ Database credentials (these go in your test project profiles)

## 📦 Companion Project

Use this library with the **ACEBaseCustomTemplate** project, which contains:
- Feature files
- Database configuration profiles
- Test runner
- Example test scenarios

See: `../ACEBaseCustomTemplate/` for the template project.

## Features

### Core Framework
- **BaseWrapper**: Abstract base class for creating custom wrappers with initialization and cleanup lifecycle
- **ConfigurableWrapper**: Extends BaseWrapper with configuration management capabilities
- **StringUtils**: Common string utility methods
- **WrapperException**: Custom exception handling for wrapper operations

### Database Testing (Standalone Library)
- **DatabaseConnection**: Wrapper for managing database connections and executing queries
- **DatabaseManager**: Singleton manager for handling multiple database connections
- **Profile-Based Configuration**: Support for MySQL, PostgreSQL, SQL Server, and Oracle
- **DatabaseConfig**: Configuration loader for profile-specific database settings
- **ConfigurationManager**: Manage multiple database profiles and switch between them
- **DatabaseConnectionFactory**: Factory for creating connections from profiles
- **DatabaseStepDefinitions**: Pre-built Cucumber step definitions (in main source, not test)
  - All step definitions are packaged in the JAR
  - Automatically discovered by Cucumber when library is imported
  - Tagged with `@DB` for selective execution

## Building the Library

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

### Build Commands

```bash
# Clean and compile
mvn clean compile

# Run unit tests
mvn test

# Package the JAR (includes step definitions in main source)
mvn package

# Install to local Maven repository (required for use in other projects)
mvn clean install
```

**Important**: This library must be installed to your local Maven repository before it can be used in test projects.

## Using This Library in Your Test Project

### Step 1: Install the Library

```bash
# From this project directory
mvn clean install
```

This installs the JAR to your local Maven repository (`~/.m2/repository`).

### Step 2: Add Dependency to Your Test Project

In your test project's `pom.xml`, add:

```xml
<dependency>
    <groupId>com.qa.framework</groupId>
    <artifactId>custom-ace-base-wrapper</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Step 3: Configure Cucumber Glue

In your test project's `cucumber.properties`:

```properties
cucumber.glue=com.qa.framework.stepdefinitions.db
```

Or in your test runner:

```java
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.qa.framework.stepdefinitions.db")
```

### Step 4: Create Feature Files and Profiles

See the **ACEBaseCustomTemplate** project for examples:
- Feature files go in `src/test/resources/features/`
- Database profiles go in `src/test/resources/config/`

**The step definitions are automatically available from this library!**

## Usage Examples

### Using Base Wrapper

```java
import com.qa.framework.base.ConfigurableWrapper;

public class MyCustomWrapper extends ConfigurableWrapper {
    
    public MyCustomWrapper(String name) {
        super(name);
    }
    
    @Override
    protected void doInitialize() {
        // Your initialization logic
        System.out.println("Initializing " + getName());
    }
    
    @Override
    protected void doCleanup() {
        // Your cleanup logic
        System.out.println("Cleaning up " + getName());
    }
    
    public void performAction() {
        if (!isInitialized()) {
            initialize();
        }
        // Your action logic
    }
}
```

### Using Database Connection with Profiles

```java
import com.qa.framework.config.ConfigurationManager;
import com.qa.framework.db.DatabaseConnection;
import com.qa.framework.db.DatabaseConnectionFactory;

// Method 1: Using active profile (set via -Ddb.profile=mysql)
DatabaseConnection conn = DatabaseConnectionFactory.createAndConnect();

// Method 2: Using specific profile
DatabaseConnection mysqlConn = DatabaseConnectionFactory.createAndConnectFromProfile("mysql");
DatabaseConnection pgConn = DatabaseConnectionFactory.createAndConnectFromProfile("postgresql");

// Execute SELECT query
List<Map<String, Object>> results = conn.executeQuery("SELECT * FROM users");

// Execute UPDATE query
int rowsAffected = conn.executeUpdate("UPDATE users SET status = 'active' WHERE id = 1");

// Execute prepared statement
List<Map<String, Object>> results = conn.executePreparedQuery(
    "SELECT * FROM users WHERE email = ?", 
    "user@example.com"
);

conn.disconnect();
```

### Supported Database Profiles

The library includes pre-configured profiles for:
- **MySQL** (`mysql`) - Port 3306
- **PostgreSQL** (`postgresql`) - Port 5432
- **SQL Server** (`sqlserver`) - Port 1433
- **Oracle** (`oracle`) - Port 1521

Configuration files are located in `src/test/resources/config/`:
- `mysql-config.properties`
- `postgresql-config.properties`
- `sqlserver-config.properties`
- `oracle-config.properties`

**See [DATABASE_CONFIGURATION.md](DATABASE_CONFIGURATION.md) for detailed configuration guide.**

### Running Cucumber Tests

The library includes a Cucumber test runner that can execute database step definitions:

```java
// Run the CucumberTestRunner class
// It will execute all feature files in src/test/resources/features/db/
```

### Creating Custom Step Definitions

Add your own step definitions in the `com.qa.framework.stepdefinitions.db` package:

```java
package com.qa.framework.stepdefinitions.db;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

public class MyCustomSteps {
    
    @Given("I have custom setup")
    public void customSetup() {
        // Your setup logic
    }
    
    @When("I perform custom action")
    public void customAction() {
        // Your action logic
    }
    
    @Then("I verify custom result")
    public void verifyResult() {
        // Your verification logic
    }
}
```

## Project Structure

```
CustomACEBaseWrapper/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/
│   │           └── qa/
│   │               └── framework/
│   │                   ├── base/
│   │                   │   ├── BaseWrapper.java
│   │                   │   └── ConfigurableWrapper.java
│   │                   ├── db/
│   │                   │   ├── DatabaseConnection.java
│   │                   │   └── DatabaseManager.java
│   │                   ├── utils/
│   │                   │   └── StringUtils.java
│   │                   └── exceptions/
│   │                       └── WrapperException.java
│   └── test/
│       ├── java/
│       │   └── com/
│       │       └── qa/
│       │           └── framework/
│       │               ├── base/
│       │               │   └── BaseWrapperTest.java
│       │               ├── utils/
│       │               │   └── StringUtilsTest.java
│       │               ├── runners/
│       │               │   └── CucumberTestRunner.java
│       │               └── stepdefinitions/
│       │                   └── db/
│       │                       └── DatabaseStepDefinitions.java
│       └── resources/
│           ├── features/
│           │   └── db/
│           │       ├── database_operations.feature
│           │       └── sample_db_test.feature
│           └── cucumber.properties
├── pom.xml
└── README.md
```

## Available Cucumber Step Definitions

The library provides the following pre-built step definitions for database testing:

### Connection Management
- `Given I have a database connection named {string} with URL {string} username {string} and password {string}`
- `Given I connect to the database {string}`
- `When I disconnect from the database`
- `Then I should be connected to the database`
- `Then I should not be connected to the database`

### Query Execution
- `When I execute the query {string}`
- `When I execute the update query {string}`
- `When I execute the prepared query {string} with parameters:`

### Result Verification
- `Then the query should return {int} row(s)`
- `Then the query should return at least {int} row(s)`
- `Then the first row should contain column {string} with value {string}`
- `Then the update should affect {int} row(s)`
- `Then the query should execute successfully`
- `Then the query should fail with an error`
- `Then the result set should contain a column {string}`
- `Then all rows should have column {string} not null`

## Documentation

- **[QUICK_START.md](QUICK_START.md)** - Quick start guide for new users
- **[DATABASE_CONFIGURATION.md](DATABASE_CONFIGURATION.md)** - Detailed database configuration guide
- **[ARCHITECTURE.md](ARCHITECTURE.md)** - System architecture and design patterns
- **[PROJECT_SUMMARY.md](PROJECT_SUMMARY.md)** - Complete project overview

## License

This is a custom library for internal use.
"# CustomACEBaseWrapper" 
