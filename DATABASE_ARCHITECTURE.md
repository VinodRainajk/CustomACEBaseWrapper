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
│  │  @Before("@DB") setUp    │───▶│  • dbManager, configManager, currentConnection               │    │
│  │  @After("@DB") tearDown  │    │  • queryResults, updateCount, insertCount, deleteCount       │    │
│  └──────────────────────────┘    │  • lastException, procedureResult, functionResult           │    │
│                                  └──────────────────────────────────────────────────────────────┘    │
│                                                    ▲                                                    │
│                                                    │ uses                                                │
│  ┌────────────────────────────────────────────────────────────────────────────────────────────────┐   │
│  │  CATEGORIZED STEP DEFINITION CLASSES                                                            │   │
│  ├────────────────────────────────────────────────────────────────────────────────────────────────┤   │
│  │  DatabaseConnectionStepDefinitions   │ Connection: create, connect, disconnect, profile       │   │
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
│  │  DatabaseManager            │  │  DatabaseConnectionFactory   │  │  ConfigurationManager       │   │
│  │  (Singleton)                │  │  createConnectionFromProfile │  │  (Singleton)                │   │
│  │  • addConnection            │  │  createAndConnect            │  │  • setActiveProfile         │   │
│  │  • getConnection            │  └──────────────┬──────────────┘  │  • getActiveProfile         │   │
│  │  • closeAllConnections      │                 │                 └─────────────────────────────┘   │
│  └──────────────┬──────────────┘                 │                                                    │
│                 │                                │                                                    │
│                 ▼                                ▼                                                    │
│  ┌─────────────────────────────────────────────────────────────────────────────────────────────────┐ │
│  │  DatabaseConnection                                                                               │ │
│  │  • connect() / disconnect()                                                                       │ │
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
   │                              │                             │                            │  Given I connect...            │
   │                              │                             │                            │  → DatabaseConnectionStepDef  │
   │                              │                             │                            │  → DatabaseManager.connect    │
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

## Step Definition Categories Summary

| Category | Class | Key Operations |
|----------|-------|----------------|
| Hooks | DatabaseHooks | @Before/@After setup and teardown |
| Context | DatabaseStepContext | Shared state (ThreadLocal) |
| Connection | DatabaseConnectionStepDefinitions | Connect, disconnect, profile |
| SELECT | DatabaseSelectStepDefinitions | Query, prepared query, row/column assertions |
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
