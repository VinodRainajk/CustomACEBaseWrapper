# Architecture Overview

## System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        User Application                          │
│                     (Cucumber Feature Files)                     │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                    CucumberTestRunner                            │
│              (Executes @DB tagged scenarios)                     │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                  DatabaseStepDefinitions                         │
│           (Cucumber Step Implementation Layer)                   │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                  DatabaseConnectionFactory                       │
│              (Creates connections from profiles)                 │
└────────────────────────────┬────────────────────────────────────┘
                             │
                ┌────────────┴────────────┐
                ▼                         ▼
┌───────────────────────────┐  ┌──────────────────────────┐
│  ConfigurationManager     │  │   DatabaseManager        │
│  (Manages profiles)       │  │   (Manages connections)  │
└────────────┬──────────────┘  └────────────┬─────────────┘
             │                              │
             ▼                              ▼
┌───────────────────────────┐  ┌──────────────────────────┐
│    DatabaseConfig         │  │  DatabaseConnection      │
│  (Loads properties)       │  │  (JDBC wrapper)          │
└────────────┬──────────────┘  └────────────┬─────────────┘
             │                              │
             ▼                              ▼
┌───────────────────────────┐  ┌──────────────────────────┐
│  Configuration Files      │  │   Database Servers       │
│  (*.properties)           │  │   (MySQL, PostgreSQL,    │
│                           │  │    SQL Server, Oracle)   │
└───────────────────────────┘  └──────────────────────────┘
```

## Configuration Flow

```
1. User sets profile
   ├─ Via system property: -Ddb.profile=mysql
   ├─ Via ConfigurationManager: setActiveProfile("mysql")
   └─ Via Cucumber step: "I set the active database profile to 'mysql'"

2. ConfigurationManager loads profile
   └─ Reads: src/test/resources/config/mysql-config.properties

3. DatabaseConfig parses properties
   ├─ db.type
   ├─ db.driver
   ├─ db.url
   ├─ db.username
   └─ db.password

4. DatabaseConnectionFactory creates connection
   └─ Uses DatabaseConfig to instantiate DatabaseConnection

5. DatabaseConnection establishes JDBC connection
   └─ Connects to actual database server

6. User executes queries
   ├─ executeQuery() - SELECT statements
   ├─ executeUpdate() - INSERT/UPDATE/DELETE
   └─ executePreparedQuery() - Parameterized queries

7. Connection cleanup
   └─ disconnect() closes JDBC connection
```

## Class Relationships

```
┌──────────────────────┐
│  ConfigurationManager│
│  (Singleton)         │
└──────────┬───────────┘
           │ manages
           ▼
┌──────────────────────┐
│   DatabaseConfig     │
│   (per profile)      │
└──────────────────────┘

┌──────────────────────┐
│  DatabaseManager     │
│  (Singleton)         │
└──────────┬───────────┘
           │ manages
           ▼
┌──────────────────────┐
│ DatabaseConnection   │
│ (per connection)     │
└──────────────────────┘

┌──────────────────────────┐
│DatabaseConnectionFactory │
│  (Static methods)        │
└────────┬─────────────────┘
         │ creates
         ▼
┌──────────────────────┐
│ DatabaseConnection   │
└──────────────────────┘
```

## Profile Configuration System

```
Profile Selection
       │
       ├─ mysql ──────────► mysql-config.properties
       │                    ├─ db.type=mysql
       │                    ├─ db.driver=com.mysql.cj.jdbc.Driver
       │                    ├─ db.url=jdbc:mysql://...
       │                    └─ db.username, db.password
       │
       ├─ postgresql ─────► postgresql-config.properties
       │                    ├─ db.type=postgresql
       │                    ├─ db.driver=org.postgresql.Driver
       │                    └─ ...
       │
       ├─ sqlserver ──────► sqlserver-config.properties
       │                    ├─ db.type=sqlserver
       │                    ├─ db.driver=com.microsoft.sqlserver...
       │                    └─ ...
       │
       └─ oracle ─────────► oracle-config.properties
                            ├─ db.type=oracle
                            ├─ db.driver=oracle.jdbc.driver...
                            └─ ...
```

## Cucumber Test Execution Flow

```
1. CucumberTestRunner starts
   └─ Filters scenarios with @DB tag

2. Feature file loaded
   └─ Parses Gherkin syntax

3. Scenario execution begins
   └─ @Before hook: setUp()
      └─ Initialize DatabaseManager and ConfigurationManager

4. Step execution
   ├─ Given: Setup database connection
   │  └─ DatabaseConnectionFactory.createConnectionFromProfile()
   │
   ├─ When: Execute database operations
   │  ├─ executeQuery()
   │  ├─ executeUpdate()
   │  └─ executePreparedQuery()
   │
   └─ Then: Verify results
      ├─ Assert row count
      ├─ Assert column values
      └─ Assert query success

5. Scenario completion
   └─ @After hook: tearDown()
      └─ Close all database connections

6. Report generation
   ├─ HTML report: target/cucumber-reports/cucumber.html
   └─ JSON report: target/cucumber-reports/cucumber.json
```

## Database Connection Lifecycle

```
┌─────────────────────────────────────────────────────────┐
│                  Connection Lifecycle                    │
└─────────────────────────────────────────────────────────┘

1. CREATE
   DatabaseConnection conn = new DatabaseConnection(url, user, pass, driver)
   Status: [CREATED] - Not connected

2. CONNECT
   conn.connect()
   Status: [CONNECTED] - Ready for queries
   └─ Loads JDBC driver
   └─ Establishes connection

3. EXECUTE
   conn.executeQuery("SELECT...")
   conn.executeUpdate("UPDATE...")
   Status: [ACTIVE] - Executing queries

4. DISCONNECT
   conn.disconnect()
   Status: [CLOSED] - Connection closed
   └─ Releases resources

5. CLEANUP
   DatabaseManager.closeAllConnections()
   └─ Closes all managed connections
```

## Design Patterns Used

### 1. Singleton Pattern
- **ConfigurationManager**: Single instance manages all configurations
- **DatabaseManager**: Single instance manages all connections

### 2. Factory Pattern
- **DatabaseConnectionFactory**: Creates DatabaseConnection objects from profiles

### 3. Template Method Pattern
- **BaseWrapper**: Defines initialization/cleanup template
- Subclasses implement `doInitialize()` and `doCleanup()`

### 4. Strategy Pattern
- **DatabaseConfig**: Different strategies for different database types
- Each profile represents a different configuration strategy

## Component Responsibilities

### Configuration Layer
```
ConfigurationManager
├─ Manages multiple DatabaseConfig instances
├─ Tracks active profile
└─ Provides access to configurations

DatabaseConfig
├─ Loads properties from file
├─ Validates configuration
└─ Provides typed access to properties
```

### Connection Layer
```
DatabaseConnectionFactory
├─ Creates connections from profiles
├─ Simplifies connection creation
└─ Handles driver loading

DatabaseConnection
├─ Wraps JDBC Connection
├─ Executes SQL queries
├─ Manages connection lifecycle
└─ Provides result mapping

DatabaseManager
├─ Manages multiple connections
├─ Provides named access
└─ Handles cleanup
```

### Testing Layer
```
CucumberTestRunner
├─ Configures Cucumber engine
├─ Filters by @DB tag
└─ Generates reports

DatabaseStepDefinitions
├─ Implements Gherkin steps
├─ Uses configuration system
├─ Performs assertions
└─ Manages test lifecycle
```

## Data Flow Example

```
Feature File:
  Given I set the active database profile to "mysql"
    │
    ▼
  ConfigurationManager.setActiveProfile("mysql")
    │
    ▼
  activeProfile = "mysql"

  Given I have a database connection named "testdb" using profile "mysql"
    │
    ▼
  DatabaseConnectionFactory.createConnectionFromProfile("mysql")
    │
    ▼
  ConfigurationManager.loadConfiguration("mysql")
    │
    ▼
  DatabaseConfig reads mysql-config.properties
    │
    ▼
  new DatabaseConnection(url, username, password, driver)
    │
    ▼
  DatabaseManager.addConnection("testdb", connection)

  When I connect to the database "testdb"
    │
    ▼
  connection = DatabaseManager.getConnection("testdb")
    │
    ▼
  connection.connect()
    │
    ▼
  DriverManager.getConnection(url, username, password)
    │
    ▼
  [CONNECTED TO DATABASE]

  When I execute the query "SELECT * FROM users"
    │
    ▼
  connection.executeQuery("SELECT * FROM users")
    │
    ▼
  statement.executeQuery(query)
    │
    ▼
  ResultSet → List<Map<String, Object>>

  Then the query should return 1 row(s)
    │
    ▼
  assertEquals(1, queryResults.size())
    │
    ▼
  [ASSERTION PASSED]
```

## Extension Points

### Add New Database Type
1. Create config file: `{dbtype}-config.properties`
2. Add JDBC driver to `pom.xml`
3. Use existing infrastructure (no code changes needed)

### Add Custom Step Definitions
1. Create class in `stepdefinitions.db` package
2. Inject DatabaseManager/ConfigurationManager
3. Add @Given/@When/@Then methods

### Add Custom Configuration Properties
1. Add property to config file
2. Access via `DatabaseConfig.getProperty(key)`

### Add Custom Connection Logic
1. Extend DatabaseConnection
2. Override connect() method
3. Use in DatabaseConnectionFactory

---

This architecture provides:
✅ Separation of concerns
✅ Easy extensibility
✅ Profile-based flexibility
✅ Reusable components
✅ Clean test integration
