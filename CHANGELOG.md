# Changelog

All notable changes to this project will be documented in this file.

## [1.0.0] - 2026-03-02

### Added - Initial Release

#### Core Framework
- ✅ BaseWrapper abstract class with initialization/cleanup lifecycle
- ✅ ConfigurableWrapper with configuration management
- ✅ StringUtils utility class with common string operations
- ✅ WrapperException for custom error handling

#### Database Configuration System
- ✅ DatabaseConfig class for loading profile-specific configurations
- ✅ ConfigurationManager singleton for managing multiple profiles
- ✅ Profile-based configuration files for 4 database types:
  - mysql-config.properties
  - postgresql-config.properties
  - sqlserver-config.properties
  - oracle-config.properties

#### Database Connection Management
- ✅ DatabaseConnection class with JDBC wrapper functionality
  - Execute SELECT queries
  - Execute UPDATE/INSERT/DELETE queries
  - Execute prepared statements with parameters
  - Connection lifecycle management
- ✅ DatabaseConnectionFactory for creating connections from profiles
- ✅ DatabaseManager singleton for managing multiple connections

#### Cucumber Integration
- ✅ CucumberTestRunner with @DB tag filtering
- ✅ DatabaseStepDefinitions with comprehensive step implementations:
  - Connection management steps
  - Profile-based connection steps
  - Query execution steps
  - Result verification steps
  - Update operation steps
- ✅ Feature files demonstrating usage:
  - database_operations.feature
  - database_profile_test.feature
  - sample_db_test.feature

#### Database Support
- ✅ MySQL 8.3.0 driver included
- ✅ PostgreSQL 42.7.1 driver included
- ✅ Microsoft SQL Server 12.6.0 driver included
- ✅ Oracle 23.3.0 driver included

#### Testing
- ✅ Unit tests for BaseWrapper
- ✅ Unit tests for StringUtils
- ✅ Unit tests for DatabaseConfig
- ✅ Unit tests for ConfigurationManager
- ✅ Cucumber feature files with @DB tags

#### Documentation
- ✅ README.md - Main project documentation
- ✅ QUICK_START.md - Quick start guide
- ✅ DATABASE_CONFIGURATION.md - Detailed configuration guide
- ✅ ARCHITECTURE.md - System architecture documentation
- ✅ PROJECT_SUMMARY.md - Complete project overview
- ✅ CHANGELOG.md - This file

#### Build Configuration
- ✅ Maven POM with Java 17 support
- ✅ Cucumber 7.15.0 integration
- ✅ JUnit 5.9.3 for testing
- ✅ Maven plugins for JAR, source, and Javadoc generation
- ✅ .gitignore for Maven and IDE files

### Configuration Features
- Profile selection via system property: `-Ddb.profile=mysql`
- Profile selection via ConfigurationManager API
- Profile selection via Cucumber steps
- Support for custom database-specific properties
- Connection timeout and pool size configuration
- SSL/TLS configuration options

### Cucumber Step Definitions

#### Connection Steps
- `Given I have a database connection named {string} with URL {string} username {string} and password {string}`
- `Given I have a database connection named {string} using profile {string}`
- `Given I set the active database profile to {string}`
- `Given I connect to the database {string}`
- `When I connect to database using the active profile`
- `When I disconnect from the database`
- `Then I should be connected to the database`
- `Then I should not be connected to the database`

#### Query Execution Steps
- `When I execute the query {string}`
- `When I execute the update query {string}`
- `When I execute the prepared query {string} with parameters:`

#### Verification Steps
- `Then the query should return {int} row(s)`
- `Then the query should return at least {int} row(s)`
- `Then the first row should contain column {string} with value {string}`
- `Then the update should affect {int} row(s)`
- `Then the query should execute successfully`
- `Then the query should fail with an error`
- `Then the result set should contain a column {string}`
- `Then all rows should have column {string} not null`

### Design Patterns Implemented
- Singleton Pattern (ConfigurationManager, DatabaseManager)
- Factory Pattern (DatabaseConnectionFactory)
- Template Method Pattern (BaseWrapper)
- Strategy Pattern (DatabaseConfig profiles)

### Maven Coordinates
```xml
<groupId>com.qa.framework</groupId>
<artifactId>custom-ace-base-wrapper</artifactId>
<version>1.0.0</version>
```

### System Requirements
- Java 17 or higher
- Maven 3.6 or higher
- Supported databases: MySQL, PostgreSQL, SQL Server, Oracle

### Known Limitations
- Configuration files must be in classpath
- Only supports JDBC-based databases
- No connection pooling implementation (uses basic JDBC connections)
- No transaction management utilities

### Future Enhancements (Planned)
- Connection pooling support (HikariCP)
- Transaction management utilities
- Database migration support
- Query builder utilities
- Result set mapping to POJOs
- Async query execution
- Database health checks
- Connection retry logic
- Query performance metrics

---

## Version History

### [1.0.0] - 2026-03-02
- Initial release with full database testing framework
- Multi-database support (MySQL, PostgreSQL, SQL Server, Oracle)
- Profile-based configuration system
- Cucumber integration with @DB tag filtering
- Comprehensive step definitions
- Complete documentation suite

---

**Note**: This is the initial release. Future versions will be documented here.
