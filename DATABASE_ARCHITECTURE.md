# Database Architecture

## Overview

This document describes the architecture of the database testing flow, from the InterceptorRunner entry point down to the database layer.

---

## High-Level Flow

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│  1. InterceptorRunner (Entry Point)                                                  │
│     mvn test -Dtest=InterceptorRunnerTest                                            │
└─────────────────────────────────────┬───────────────────────────────────────────────┘
                                      │
                                      │  @SelectClasses routes by tag
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────────────┐
│  2. DBTestRunner                                                                     │
│     FILTER_TAGS=@DB  |  GLUE=com.qa.framework.stepdefinitions.db                     │
└─────────────────────────────────────┬───────────────────────────────────────────────┘
                                      │
                                      │  Cucumber discovers & executes
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────────────┐
│  3. Database Step Definitions (Categorized)                                          │
│     DatabaseHooks → DatabaseStepContext → Categorized Step Definition Classes        │
└─────────────────────────────────────┬───────────────────────────────────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────────────┐
│  4. Database Layer                                                                   │
│     DatabaseManager | DatabaseConnection | DatabaseConnectionFactory                 │
└─────────────────────────────────────┬───────────────────────────────────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────────────┐
│  5. Database Servers (MySQL, PostgreSQL, SQL Server, Oracle)                         │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

---

## Detailed Architecture Diagram

```
                                    ┌─────────────────────────────────────┐
                                    │         Template / Test Project      │
                                    │  mvn test -Dtest=InterceptorRunnerTest│
                                    └───────────────────┬─────────────────┘
                                                        │
                                                        ▼
┌───────────────────────────────────────────────────────────────────────────────────────────────────────┐
│  INTERCEPTOR (CustomACEBaseWrapper)                                                                    │
│  ┌─────────────────────────────────────────────────────────────────────────────────────────────────┐  │
│  │  InterceptorRunner                                                                               │  │
│  │  @Suite @SelectClasses(DBTestRunner, UITestRunner, APITestRunner)                                │  │
│  │  • Routes @DB → DBTestRunner                                                                     │  │
│  │  • Routes @UI → UITestRunner                                                                     │  │
│  │  • Routes @API → APITestRunner                                                                   │  │
│  └─────────────────────────────────────────────────────────────────────────────────────────────────┘  │
└───────────────────────────────────────────────────────────────────────────────────────────────────────┘
                                                        │
                                    @DB scenarios only  │
                                                        ▼
┌───────────────────────────────────────────────────────────────────────────────────────────────────────┐
│  DBTestRunner                                                                                          │
│  • @Suite @IncludeEngines("cucumber")                                                                  │
│  • FILTER_TAGS_PROPERTY = @DB                                                                         │
│  • GLUE_PROPERTY = com.qa.framework.stepdefinitions.db                                                │
│  • FEATURES = src/test/resources/features                                                             │
└───────────────────────────────────────────────────────────────────────────────────────────────────────┘
                                                        │
                                                        │  Loads glue package
                                                        ▼
┌───────────────────────────────────────────────────────────────────────────────────────────────────────┐
│  stepdefinitions.db PACKAGE                                                                            │
│                                                                                                        │
│  ┌──────────────────────────┐    ┌──────────────────────────────────────────────────────────────┐    │
│  │  DatabaseHooks           │    │  DatabaseStepContext (ThreadLocal - shared state)            │    │
│  │  @Before("@DB") setUp    │───▶│  • dbManager, namedConnections (configName→connection)       │    │
│  │  @After("@DB") tearDown  │    │  • queryResults, updateCount, insertCount, deleteCount       │    │
│  └──────────────────────────┘    │  • lastException, procedureResult, functionResult           │    │
│                                  └──────────────────────────────────────────────────────────────┘    │
│                                                    ▲                                                    │
│                                                    │ uses                                                │
│  ┌────────────────────────────────────────────────────────────────────────────────────────────────┐   │
│  │  CATEGORIZED STEP DEFINITION CLASSES                                                            │   │
│  ├────────────────────────────────────────────────────────────────────────────────────────────────┤   │
│  │  DatabaseConnectionStepDefinitions   │ Connect by config name "mysql"/"oracle", optional alias│   │
│  │  DatabaseSelectStepDefinitions       │ SELECT, prepared query, row/column assertions          │   │
│  │  DatabaseInsertStepDefinitions       │ INSERT, prepared insert                                │   │
│  │  DatabaseUpdateStepDefinitions       │ UPDATE, prepared update                                │   │
│  │  DatabaseDeleteStepDefinitions       │ DELETE, prepared delete, TRUNCATE                      │   │
│  │  DatabaseProcedureStepDefinitions    │ Stored procedures (callable)                           │   │
│  │  DatabasePackageStepDefinitions      │ Oracle packaged procedures                             │   │
│  │  DatabaseFunctionStepDefinitions     │ Scalar functions via SELECT                            │   │
│  │  DatabaseTransactionStepDefinitions  │ Begin, commit, rollback                                │   │
│  │  DatabaseSchemaStepDefinitions       │ Table row count, metadata                              │   │
│  │  DatabaseErrorHandlingStepDefinitions│ Assert failures, error messages                        │   │
│  │  DatabaseValidationStepDefinitions   │ Regex, allowed values                                  │   │
│  │  DatabaseFileStepDefinitions         │ File copy, file existence                              │   │
│  └────────────────────────────────────────────────────────────────────────────────────────────────┘   │
└───────────────────────────────────────────────────────────────────────────────────────────────────────┘
                                                        │
                                                        │  invoke
                                                        ▼
┌───────────────────────────────────────────────────────────────────────────────────────────────────────┐
│  DATABASE LAYER                                                                                        │
│                                                                                                        │
│  ┌─────────────────────────────┐  ┌─────────────────────────────┐  ┌─────────────────────────────┐   │
│  │  DatabaseConfigLoader       │  │  DatabaseConnectionFactory   │  │  DatabaseManager            │   │
│  │  loadMaster + feature +     │──▶│  createConnection(config)   │──▶│  (Singleton)                │   │
│  │  section; resolve by name   │  │  createAndConnect(config)    │  │  • addConnection(name, conn)│   │
│  └─────────────────────────────┘  └─────────────────────────────┘  │  • getConnection(name)      │   │
│            ▲                                │                        │  • closeAllConnections      │   │
│            │ config/                        │                        └──────────────┬──────────────┘   │
│            │ master_database.yml             │                                       │                 │
│            │ {feature}-database.yml          │                                       │                 │
│                 │                                │                                                    │
│                 └────────────────────────────────┘                                                    │
│  ┌─────────────────────────────────────────────────────────────────────────────────────────────────┐ │
│  │  DatabaseConnection                                                                               │ │
│  │  • connect() / disconnect()                                                                      │ │
│  │  • executeQuery()         - SELECT                                                                │ │
│  │  • executeUpdate()        - INSERT / UPDATE / DELETE                                              │ │
│  │  • executePreparedQuery() - Parameterized SELECT                                                  │ │
│  │  • executePreparedUpdate()- Parameterized INSERT/UPDATE/DELETE                                    │ │
│  │  • executeCallable()      - Stored procedures                                                     │ │
│  └─────────────────────────────────────────────────────────────────────────────────────────────────┘ │
└───────────────────────────────────────────────────────────────────────────────────────────────────────┘
                                                        │
                                                        │  JDBC
                                                        ▼
┌───────────────────────────────────────────────────────────────────────────────────────────────────────┐
│  DATABASE SERVERS                                                                                      │
│  MySQL | PostgreSQL | SQL Server | Oracle                                                              │
└───────────────────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## Execution Flow (Sequence)

```
Template                    InterceptorRunner              DBTestRunner                Step Definitions              Database Layer
   │                              │                             │                            │                              │
   │  mvn test                    │                             │                            │                              │
   │  -Dtest=InterceptorRunnerTest│                             │                            │                              │
   ├─────────────────────────────▶│                             │                            │                              │
   │                              │  JUnit Suite invokes        │                            │                              │
   │                              │  DBTestRunner               │                            │                              │
   │                              ├────────────────────────────▶│                            │                              │
   │                              │                             │  Cucumber picks @DB        │                              │
   │                              │                             │  scenarios                 │                              │
   │                              │                             ├────────────────────────────▶│                              │
   │                              │                             │                            │  @Before: DatabaseHooks       │
   │                              │                             │                            │  sets up DatabaseStepContext  │
   │                              │                             │                            ├──────────────────────────────▶│
   │                              │                             │                            │  Given I connect to database "mysql"            │
   │                              │                             │                            │  → DatabaseConnectionStepDef  │
   │                              │                             │                            │  → DatabaseConfigLoader → Factory → DatabaseManager (config by name)    │
   │                              │                             │                            ├──────────────────────────────▶│
   │                              │                             │                            │  When I execute the query     │
   │                              │                             │                            │  → DatabaseSelectStepDef      │
   │                              │                             │                            │  → executeQuery()             │
   │                              │                             │                            ├──────────────────────────────▶│
   │                              │                             │                            │  Then the query should return │
   │                              │                             │                            │  → DatabaseSelectStepDef      │
   │                              │                             │                            │  → assert on queryResults     │
   │                              │                             │                            │  @After: tearDown, reset       │
   │                              │                             │◀────────────────────────────┤                              │
   │                              │◀────────────────────────────│                            │                              │
   │◀─────────────────────────────│                             │                            │                              │
   │  Test result                 │                             │                            │                              │
```

---

---

## Database Configuration

**Principle:** DB credentials and connection details are not embedded in feature files. Config is referenced by name (e.g. `"mysql"`, `"oracle"`) in steps. Profile is passed via `-Dprofile=dev` (Option B: profile as folder).

### Config Hierarchy

| Level | Source | Purpose |
|-------|--------|---------|
| 1 | `master_database.yml` | Base configs (mysql, oracle, postgresql, etc.) |
| 2 | `config/{profile}/master_database.yml` | Profile override (when `-Dprofile=X`) |
| 3 | `{feature-name}-database.yml` | Optional overrides for that feature |
| 4 | `config/{profile}/{feature}-database.yml` | Profile + feature override |
| 5 | `sections` (in feature yml) | Optional overrides keyed by scenario name |

### Config File Location (Option B: Profile as Folder)

```
src/test/resources/
└── config/
    ├── master_database.yml           # Base (always loaded)
    ├── cross-db-database.yml         # For cross-db.feature
    ├── user-db-database.yml          # For user-db.feature
    │
    ├── dev/                          # Profile: -Dprofile=dev
    │   ├── master_database.yml
    │   └── cross-db-database.yml
    ├── qa/
    │   └── master_database.yml
    └── staging/
        └── master_database.yml
```

See [CONFIGURATION.md](CONFIGURATION.md) for full profile documentation.

### Naming Convention

| Feature File | Feature-Specific Config |
|--------------|-------------------------|
| `cross-db.feature` | `cross-db-database.yml` |
| `user-db.feature` | `user-db-database.yml` |
| `orders.feature` | `orders-database.yml` |

### Step Syntax

- `I connect to database "mysql"` — use config `"mysql"`, connection stored as `"mysql"`
- `I connect to database "oracle" as "ora"` — use config `"oracle"`, connection stored as `"ora"` (alias optional)
- `I execute the query "SELECT 1" on "mysql"` — run on connection `"mysql"`
- No `@db:mysql` type tags on feature file; only `@DB` to mark DB scenarios.

### Config Resolution Flow

```
Step: I connect to database "mysql"
                │
                ▼
┌───────────────────────────────────────────────────────────────────┐
│  1. Load master_database.yml → base "mysql" config                 │
│  2. If -Dprofile=X: load config/{profile}/master_database.yml      │
│     → merge into base                                              │
└───────────────────────────────────────────────────────────────────┘
                │
                ▼
┌───────────────────────────────────────────────────────────────────┐
│  3. Load {feature-name}-database.yml (if exists)                   │
│  4. If -Dprofile=X: load config/{profile}/{feature}-database.yml   │
│     → merge overrides into "mysql"                                 │
└───────────────────────────────────────────────────────────────────┘
                │
                ▼
┌───────────────────────────────────────────────────────────────────┐
│  5. If current scenario name matches a key under "sections"        │
│     → merge section overrides for "mysql"                          │
└───────────────────────────────────────────────────────────────────┘
                │
                ▼
┌───────────────────────────────────────────────────────────────────┐
│  4. Build DatabaseConnection from merged config                    │
│     → store in DatabaseManager by connection name (or alias)       │
└───────────────────────────────────────────────────────────────────┘
```

### YAML Structure (master_database.yml)

```yaml
mysql:
  type: mysql
  url: jdbc:mysql://localhost:3306/testdb
  username: testuser
  password: testpass
  driver: com.mysql.cj.jdbc.Driver
  timeout: 30000

oracle:
  type: oracle
  url: jdbc:oracle:thin:@localhost:1521:xe
  username: system
  password: oracle
  driver: oracle.jdbc.OracleDriver
```

### Feature-Specific Override (cross-db-database.yml)

```yaml
mysql:
  url: jdbc:mysql://localhost:3306/userdb
  username: userdb_app
  # password inherits from master

sections:
  "Compare user counts across MySQL and Oracle":
    mysql:
      url: jdbc:mysql://compare-host:3306/compare_db
```

### DatabaseConfigLoader (New Component)

| Component | Purpose |
|-----------|---------|
| DatabaseConfigLoader | Load and merge master + feature + section configs; resolve config by name |

---

## Step Definition Categories Summary

| Category | Class | Key Operations |
|----------|-------|----------------|
| Hooks | DatabaseHooks | @Before/@After setup and teardown |
| Context | DatabaseStepContext | Shared state (ThreadLocal), named connections map |
| Connection | DatabaseConnectionStepDefinitions | Connect by config name, optional alias; disconnect |
| SELECT | DatabaseSelectStepDefinitions | Query, prepared query, row/column assertions; `on "connectionName"` |
| INSERT | DatabaseInsertStepDefinitions | Insert, prepared insert |
| UPDATE | DatabaseUpdateStepDefinitions | Update, prepared update |
| DELETE | DatabaseDeleteStepDefinitions | Delete, truncate |
| Procedure | DatabaseProcedureStepDefinitions | Callable stored procedures |
| Package | DatabasePackageStepDefinitions | Oracle packaged procedures |
| Function | DatabaseFunctionStepDefinitions | Scalar functions |
| Transaction | DatabaseTransactionStepDefinitions | Begin, commit, rollback |
| Schema | DatabaseSchemaStepDefinitions | Table row count |
| Error | DatabaseErrorHandlingStepDefinitions | Assert failures |
| Validation | DatabaseValidationStepDefinitions | Regex, allowed values |
| File | DatabaseFileStepDefinitions | File copy, existence |
