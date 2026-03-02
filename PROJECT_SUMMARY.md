# Custom ACE Base Wrapper - Project Summary

## Overview

This is a Java 17 Maven library designed for database testing with Cucumber integration. It provides a flexible, profile-based configuration system that supports multiple database types (MySQL, PostgreSQL, SQL Server, Oracle).

## Key Features

✅ **Multi-Database Support** - MySQL, PostgreSQL, SQL Server, Oracle  
✅ **Profile-Based Configuration** - Easy switching between databases  
✅ **Cucumber Integration** - BDD-style database testing  
✅ **Pre-built Step Definitions** - Ready-to-use Gherkin steps  
✅ **Connection Management** - Singleton pattern for managing multiple connections  
✅ **Tag-Based Execution** - Only runs scenarios tagged with @DB  

## Project Structure

```
CustomACEBaseWrapper/
├── src/
│   ├── main/java/com/qa/framework/
│   │   ├── base/                      # Base wrapper classes
│   │   │   ├── BaseWrapper.java
│   │   │   └── ConfigurableWrapper.java
│   │   ├── config/                    # Configuration management
│   │   │   ├── DatabaseConfig.java
│   │   │   └── ConfigurationManager.java
│   │   ├── db/                        # Database connection classes
│   │   │   ├── DatabaseConnection.java
│   │   │   ├── DatabaseConnectionFactory.java
│   │   │   └── DatabaseManager.java
│   │   ├── utils/                     # Utility classes
│   │   │   └── StringUtils.java
│   │   └── exceptions/                # Custom exceptions
│   │       └── WrapperException.java
│   │
│   └── test/
│       ├── java/com/qa/framework/
│       │   ├── base/                  # Unit tests
│       │   │   └── BaseWrapperTest.java
│       │   ├── config/                # Configuration tests
│       │   │   ├── DatabaseConfigTest.java
│       │   │   └── ConfigurationManagerTest.java
│       │   ├── runners/               # Cucumber runner
│       │   │   └── CucumberTestRunner.java
│       │   ├── stepdefinitions/db/    # Cucumber step definitions
│       │   │   └── DatabaseStepDefinitions.java
│       │   └── utils/                 # Utility tests
│       │       └── StringUtilsTest.java
│       │
│       └── resources/
│           ├── config/                # Database configuration files
│           │   ├── mysql-config.properties
│           │   ├── postgresql-config.properties
│           │   ├── sqlserver-config.properties
│           │   └── oracle-config.properties
│           ├── features/db/           # Cucumber feature files
│           │   ├── database_operations.feature
│           │   ├── database_profile_test.feature
│           │   └── sample_db_test.feature
│           └── cucumber.properties
│
├── pom.xml                            # Maven configuration
├── README.md                          # Main documentation
├── DATABASE_CONFIGURATION.md          # Configuration guide
├── QUICK_START.md                     # Quick start guide
└── PROJECT_SUMMARY.md                 # This file
```

## Core Components

### 1. Configuration System

**DatabaseConfig.java**
- Loads database configuration from profile-specific properties files
- Provides methods to access connection details
- Supports custom properties per database type

**ConfigurationManager.java**
- Singleton manager for multiple database profiles
- Manages active profile selection
- Caches loaded configurations

**Configuration Files** (`src/test/resources/config/`)
- `mysql-config.properties` - MySQL configuration
- `postgresql-config.properties` - PostgreSQL configuration
- `sqlserver-config.properties` - SQL Server configuration
- `oracle-config.properties` - Oracle configuration

### 2. Database Connection

**DatabaseConnection.java**
- Manages JDBC connections
- Executes queries and updates
- Supports prepared statements
- Handles connection lifecycle

**DatabaseConnectionFactory.java**
- Factory pattern for creating connections
- Creates connections from profiles
- Simplifies connection instantiation

**DatabaseManager.java**
- Singleton manager for multiple connections
- Named connection management
- Automatic cleanup

### 3. Cucumber Integration

**CucumberTestRunner.java**
- JUnit Platform Suite runner
- Filters scenarios by @DB tag
- Generates HTML and JSON reports
- Configures feature and step definition locations

**DatabaseStepDefinitions.java**
- Pre-built step definitions for database operations
- Connection management steps
- Query execution steps
- Result verification steps
- Profile-based connection steps

### 4. Feature Files

**database_operations.feature**
- Basic database operations
- Query execution and verification
- Connection lifecycle testing

**database_profile_test.feature**
- Profile-based connection testing
- Multi-database support demonstration
- Database-specific scenarios

**sample_db_test.feature**
- Sample test scenarios
- Example usage patterns

## Dependencies

### Core Dependencies
- Java 17
- Maven 3.6+
- Cucumber 7.15.0
- JUnit 5.9.3

### Database Drivers
- MySQL Connector/J 8.3.0
- PostgreSQL JDBC 42.7.1
- Microsoft SQL Server JDBC 12.6.0
- Oracle JDBC 23.3.0

## Usage Patterns

### Pattern 1: Profile-Based Connection
```java
DatabaseConnection conn = DatabaseConnectionFactory
    .createAndConnectFromProfile("mysql");
```

### Pattern 2: Active Profile
```java
ConfigurationManager.getInstance().setActiveProfile("postgresql");
DatabaseConnection conn = DatabaseConnectionFactory.createAndConnect();
```

### Pattern 3: Multiple Connections
```java
DatabaseManager manager = DatabaseManager.getInstance();
manager.addConnection("db1", conn1);
manager.addConnection("db2", conn2);
```

### Pattern 4: Cucumber Steps
```gherkin
Given I set the active database profile to "mysql"
And I have a database connection named "testdb" using profile "mysql"
When I connect to the database "testdb"
```

## Running Tests

### Build and Install
```bash
mvn clean install
```

### Run Unit Tests
```bash
mvn test -Dtest=*Test
```

### Run Cucumber Tests
```bash
# Default profile (mysql)
mvn test -Dtest=CucumberTestRunner

# Specific profile
mvn test -Dtest=CucumberTestRunner -Ddb.profile=postgresql

# Specific tags
mvn test -Dtest=CucumberTestRunner -Dcucumber.filter.tags="@DB and @MySQL"
```

## Configuration Examples

### MySQL Configuration
```properties
db.type=mysql
db.driver=com.mysql.cj.jdbc.Driver
db.url=jdbc:mysql://localhost:3306/testdb
db.username=root
db.password=password
```

### PostgreSQL Configuration
```properties
db.type=postgresql
db.driver=org.postgresql.Driver
db.url=jdbc:postgresql://localhost:5432/testdb
db.username=postgres
db.password=password
```

## Available Cucumber Steps

### Connection Management
- `Given I have a database connection named {string} using profile {string}`
- `Given I set the active database profile to {string}`
- `Given I connect to the database {string}`
- `When I connect to database using the active profile`
- `When I disconnect from the database`

### Query Execution
- `When I execute the query {string}`
- `When I execute the update query {string}`
- `When I execute the prepared query {string} with parameters:`

### Verification
- `Then the query should return {int} row(s)`
- `Then the query should return at least {int} row(s)`
- `Then the first row should contain column {string} with value {string}`
- `Then the update should affect {int} row(s)`
- `Then the query should execute successfully`
- `Then the result set should contain a column {string}`
- `Then all rows should have column {string} not null`

## Extending the Library

### Add New Database Profile
1. Create new config file: `{database}-config.properties`
2. Add database driver to `pom.xml`
3. Use the profile: `createConnectionFromProfile("database")`

### Add Custom Step Definitions
1. Create class in `com.qa.framework.stepdefinitions.db` package
2. Add Cucumber annotations (@Given, @When, @Then)
3. Runner will automatically discover new steps

### Add Custom Wrapper
1. Extend `BaseWrapper` or `ConfigurableWrapper`
2. Implement `doInitialize()` and `doCleanup()`
3. Add custom business logic

## Best Practices

1. ✅ Use profiles for different environments (dev, test, prod)
2. ✅ Never commit sensitive credentials
3. ✅ Tag all DB scenarios with @DB
4. ✅ Close connections after use
5. ✅ Use prepared statements for parameterized queries
6. ✅ Test connection before running test suites
7. ✅ Use meaningful connection names
8. ✅ Document custom configurations

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Driver not found | Check pom.xml dependencies |
| Connection refused | Verify host, port, and database server status |
| Authentication failed | Check username/password in config file |
| Profile not found | Ensure config file exists in src/test/resources/config/ |
| Tag filter not working | Verify @DB tag on feature/scenario |

## Documentation Files

- **README.md** - Main project documentation
- **DATABASE_CONFIGURATION.md** - Detailed configuration guide
- **QUICK_START.md** - Quick start guide for new users
- **PROJECT_SUMMARY.md** - This file (project overview)

## Version Information

- **Version**: 1.0.0
- **Group ID**: com.qa.framework
- **Artifact ID**: custom-ace-base-wrapper
- **Java Version**: 17
- **Maven Version**: 3.6+

## License

This is a custom library for internal use.

---

**Last Updated**: March 2, 2026
