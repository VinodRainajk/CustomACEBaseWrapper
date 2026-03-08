# ACEBase + CustomACEBaseWrapper Architecture Plan

## Overview

This plan describes the split of responsibility between two projects:

| Project | Runners | Step Definitions | Purpose |
|---------|---------|------------------|---------|
| **CustomACEBaseWrapper** | DB runner only | UI, DB, API (all) | Step definition library + DB execution |
| **ACBase** | UI runner, API runner | None | UI/API execution only |

**Key principle:** Step definitions for UI, DB, and API are written in CustomACEBaseWrapper. CustomACEBaseWrapper is the entry point: it loads all step definitions, inspects feature tags (@DB, @UI, @API), and routes execution—DB runs in CustomACEBaseWrapper, UI/API run in ACEBase.

---

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│  Test Project (e.g., ACEBaseCustomTemplate)                                  │
│  ├─ features/ (database.feature, ui.feature, api.feature)                    │
│  └─ Depends on: CustomACEBaseWrapper only (1 dependency)                     │
└────────────────────────────────────┬────────────────────────────────────────┘
                                     │
                                     │  imports
                                     ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│  CustomACEBaseWrapper (Entry Point)                                          │
│  ├─ Runners: InterceptorRunner (routes by tag), DBTestRunner                 │
│  ├─ Step Definitions: db/*, ui/*, api/*  (all step definitions live here)    │
│  ├─ Dependencies: DB drivers, Cucumber, JUnit                                │
│  └─ Depends on: ACEBase                                                      │
└────────────────────────────────────┬────────────────────────────────────────┘
                                     │
                                     │  imports (transitive)
                                     ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│  ACEBase                                                                    │
│  ├─ Runners: UITestRunner, APITestRunner                                     │
│  ├─ Step Definitions: NONE (glue paths point to CustomACEBaseWrapper)        │
│  └─ Dependencies: Cucumber, JUnit (no dependency on CustomACEBaseWrapper)    │
└────────────────────────────────────┬────────────────────────────────────────┘
                                     │
         At runtime: Template classpath = CustomACEBaseWrapper + ACEBase
         ACEBase runners find step definitions in CustomACEBaseWrapper (on classpath)

┌─────────────────────────────────────────────────────────────────────────────────┐
│  INTERCEPTOR (Dynamic - no tag parameter required)                               │
│                                                                                  │
│  InterceptorRunner = JUnit Suite including DBTestRunner, UITestRunner, APITestRunner │
│  Each runner has FILTER_TAGS → picks only its scenarios automatically            │
│  Invoke: mvn test -Dtest=InterceptorRunner                                       │
└─────────────────────────────────────────────────────────────────────────────────┘
```

---

## Project Structure

### CustomACEBaseWrapper (Modify)

```
CustomACEBaseWrapper/
├── pom.xml
├── src/main/java/com/qa/framework/
│   ├── base/                    # BaseWrapper, ConfigurableWrapper
│   ├── config/                  # ConfigurationManager, DatabaseConfig
│   ├── db/                      # DatabaseManager, DatabaseConnection, etc.
│   ├── exceptions/
│   ├── runners/
│   │   ├── InterceptorRunner.java  # ADD - entry point, routes by tag to DBTestRunner or ACEBase runners
│   │   ├── BaseTestRunner.java     # (optional) runs all, or remove
│   │   └── DBTestRunner.java       # KEEP - only DB runner here
│   │   # REMOVE: UITestRunner, APITestRunner
│   └── stepdefinitions/
│       ├── db/
│       │   └── DatabaseStepDefinitions.java   # EXISTS
│       ├── ui/                                 # ADD
│       │   └── UIStepDefinitions.java         # ADD - placeholder/stub
│       └── api/                                # ADD
│           └── APIStepDefinitions.java        # ADD - placeholder/stub
└── src/test/resources/
    ├── features/
    └── config/
```

**Changes:**
1. Remove `UITestRunner.java` and `APITestRunner.java`
2. Add `stepdefinitions/ui/` with `UIStepDefinitions.java` (stub/placeholder)
3. Add `stepdefinitions/api/` with `APIStepDefinitions.java` (stub/placeholder)
4. Add `InterceptorRunner.java` as the single entry point that routes by tag

---

### ACEBase (New Project)

```
ACBase/
├── pom.xml
├── src/main/java/com/acebase/
│   └── runners/
│       ├── UITestRunner.java
│       └── APITestRunner.java
└── src/test/resources/
    └── features/              # Optional: for local feature files
```

**ACBase pom.xml key sections:**
- No dependency on CustomACEBaseWrapper (stays a leaf to avoid circular dependency)
- Cucumber dependencies
- JUnit Platform
- No DB drivers (not needed for UI/API runners)
- Optional: Selenium/RestAssured if runners need them (or keep in CustomACEBaseWrapper with step defs)

**ACBase UITestRunner.java:**
```java
@ConfigurationParameter(
    key = GLUE_PROPERTY_NAME, 
    value = "com.qa.framework.stepdefinitions.ui"  // From CustomACEBaseWrapper
)
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "@UI")
```

**ACBase APITestRunner.java:**
```java
@ConfigurationParameter(
    key = GLUE_PROPERTY_NAME, 
    value = "com.qa.framework.stepdefinitions.api"  // From CustomACEBaseWrapper
)
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "@API")
```

---

## Interceptor Flow (Detailed)

The Template runs feature files. Each feature file has a tag at the top: `@DB`, `@UI`, or `@API`. The interceptor lives in **CustomACEBaseWrapper** and orchestrates execution as follows:

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│  1. TEMPLATE runs feature files (e.g., mvn test)                                 │
│     Feature files: database.feature (@DB), ui.feature (@UI), api.feature (@API)  │
└─────────────────────────────────────┬───────────────────────────────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│  2. FLOW GOES TO CustomACEBaseWrapper (entry point)                              │
│     Template depends on CustomACEBaseWrapper → execution starts here             │
└─────────────────────────────────────┬───────────────────────────────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│  3. CUSTOMACEBASEWRAPPER loads all step definitions needed                       │
│     All step definitions live in CustomACEBaseWrapper: db/*, ui/*, api/*         │
└─────────────────────────────────────┬───────────────────────────────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│  4. INTERCEPTOR routes based on annotation (tag)                                 │
│                                                                                  │
│     @DB  → Execute in CustomACEBaseWrapper (DBTestRunner)                        │
│     @UI  → Execute in ACEBase (UITestRunner)                                     │
│     @API → Execute in ACEBase (APITestRunner)                                    │
└─────────────────────────────────────────────────────────────────────────────────┘
```

**Summary:** CustomACEBaseWrapper is the single entry point. It loads step definitions, inspects the feature tags, and routes execution—either locally (DB) or to ACEBase (UI, API).

---

## Interceptor Implementation (Dynamic)

**InterceptorRunner** is a JUnit `@Suite` that includes all three runners. No tag parameter is required—each runner filters scenarios by its own tag:

- `DBTestRunner` → FILTER_TAGS=@DB (runs only @DB scenarios)
- `UITestRunner` → FILTER_TAGS=@UI (runs only @UI scenarios)
- `APITestRunner` → FILTER_TAGS=@API (runs only @API scenarios)

**Template invocation:**
```bash
mvn test -Dtest=InterceptorRunner
```

Routing is automatic: each runner picks up only the scenarios that match its tag. No `-Dcucumber.filter.tags` needed.

---

**Test project pom.xml** (e.g., ACEBaseCustomTemplate):
```xml
<dependencies>
    <dependency>
        <groupId>com.qa.framework</groupId>
        <artifactId>custom-ace-base-wrapper</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependencies>
```
(ACEBase is pulled in transitively via CustomACEBaseWrapper)

---

## Dependency Flow

```
Test Project (Template)
  └── depends on CustomACEBaseWrapper only
        (gets CustomACEBaseWrapper + ACEBase transitively)

CustomACEBaseWrapper
  └── depends on ACEBase
        (aggregates all: step definitions + DB runner + UI/API runners via ACEBase)

ACBase
  └── no dependency on CustomACEBaseWrapper
        (runners find step definitions via classpath at runtime)
```

---

## Implementation Checklist

### Phase 1: CustomACEBaseWrapper Changes

- [ ] Add ACEBase dependency to CustomACEBaseWrapper pom.xml
- [ ] Create `stepdefinitions/ui/UIStepDefinitions.java` (stub with @UI hooks)
- [ ] Create `stepdefinitions/api/APIStepDefinitions.java` (stub with @API hooks)
- [ ] Remove `UITestRunner.java` and `APITestRunner.java`
- [ ] Add `InterceptorRunner.java` as entry point that routes by tag
- [ ] Add UI/API dependencies to CustomACEBaseWrapper if step defs need them (Selenium, RestAssured, etc.)
- [ ] Update README / RUNNER_ROUTING_ARCHITECTURE.md

### Phase 2: ACEBase Project Creation

- [ ] Create `f:\Learning\QAFramework\ACBase` directory
- [ ] Create `pom.xml` with:
  - GroupId: `com.acebase`, ArtifactId: `ace-base`
  - No dependency on CustomACEBaseWrapper (CustomACEBaseWrapper will depend on ACEBase instead)
  - Cucumber, JUnit Platform
- [ ] Create `UITestRunner.java` in `com.acebase.runners`
- [ ] Create `APITestRunner.java` in `com.acebase.runners`
- [ ] Set glue to `com.qa.framework.stepdefinitions.ui` and `com.qa.framework.stepdefinitions.api`

### Phase 3: Test Project / Interceptor

- [ ] Update test project (e.g., ACEBaseCustomTemplate) to depend on CustomACEBaseWrapper only
- [ ] Add Maven profiles or CI job definitions for db-tests, ui-tests, api-tests
- [ ] Document how to run each type

### Phase 4: Documentation

- [ ] Update RUNNER_ROUTING_ARCHITECTURE.md with ACEBase split
- [ ] Add ACEBASE_ARCHITECTURE_PLAN.md (this file) to repo
- [ ] Update README with new project layout and run commands

---

## Summary

| Component | Location | Notes |
|-----------|----------|-------|
| DB Runner | CustomACEBaseWrapper | DBTestRunner |
| UI Runner | ACEBase | UITestRunner, glue → CustomACEBaseWrapper |
| API Runner | ACEBase | APITestRunner, glue → CustomACEBaseWrapper |
| DB Step Defs | CustomACEBaseWrapper | DatabaseStepDefinitions |
| UI Step Defs | CustomACEBaseWrapper | UIStepDefinitions (to add) |
| API Step Defs | CustomACEBaseWrapper | APIStepDefinitions (to add) |
| Interceptor | CustomACEBaseWrapper | Entry point; loads step defs, routes by tag (@DB→local, @UI/@API→ACBase) |
