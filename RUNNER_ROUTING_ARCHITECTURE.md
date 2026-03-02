# Runner Routing Architecture

## Overview

The runners act as **interceptors/routers** that direct tests to appropriate step definition libraries based on tags.

```
┌─────────────────────────────────────────────────────────────┐
│  Template Project (ACEBaseCustomTemplate)                   │
│  ├─ features/                                                │
│  │  ├─ database.feature (@DB)                               │
│  │  ├─ ui.feature (@UI)                                     │
│  │  └─ api.feature (@API)                                   │
│  └─ No runners needed!                                      │
└────────────────────────┬────────────────────────────────────┘
                         │
                         │ imports runners from
                         ▼
┌─────────────────────────────────────────────────────────────┐
│  Wrapper Library (CustomACEBaseWrapper)                     │
│  ├─ runners/                                                 │
│  │  ├─ BaseTestRunner     (routes to all)                  │
│  │  ├─ DBTestRunner       (routes to @DB)                  │
│  │  ├─ UITestRunner       (routes to @UI)                  │
│  │  └─ APITestRunner      (routes to @API)                 │
│  └─ stepdefinitions/                                        │
│     └─ db/                                                  │
│        └─ DatabaseStepDefinitions                           │
└────────────────────────┬────────────────────────────────────┘
                         │
                         │ routes to external libraries
                         ▼
┌─────────────────────────────────────────────────────────────┐
│  External Libraries (Your UI/API Libraries)                 │
│  ├─ UI Library                                              │
│  │  └─ com.yourcompany.ui.stepdefinitions                  │
│  │     └─ UIStepDefinitions                                │
│  └─ API Library                                             │
│     └─ com.yourcompany.api.stepdefinitions                 │
│        └─ APIStepDefinitions                                │
└─────────────────────────────────────────────────────────────┘
```

---

## How Routing Works

### 1. Feature File with Tags

```gherkin
@DB
Feature: Database Tests
  Scenario: Test database
    Given I connect to database
    When I execute query
    Then verify results

@UI
Feature: UI Tests
  Scenario: Test UI
    Given I open browser
    When I click button
    Then verify page

@API
Feature: API Tests
  Scenario: Test API
    Given I send GET request
    When I receive response
    Then verify status code
```

### 2. Runner Intercepts and Routes

```
User runs: mvn test -Dtest=DBTestRunner
        │
        ▼
DBTestRunner intercepts
        │
        ├─ Filters: Only @DB tagged scenarios
        ├─ Glue path: com.qa.framework.stepdefinitions.db
        └─ Routes to: DatabaseStepDefinitions
        
User runs: mvn test -Dtest=UITestRunner
        │
        ▼
UITestRunner intercepts
        │
        ├─ Filters: Only @UI tagged scenarios
        ├─ Glue path: com.yourcompany.ui.stepdefinitions
        └─ Routes to: Your UI library step definitions

User runs: mvn test -Dtest=APITestRunner
        │
        ▼
APITestRunner intercepts
        │
        ├─ Filters: Only @API tagged scenarios
        ├─ Glue path: com.yourcompany.api.stepdefinitions
        └─ Routes to: Your API library step definitions
```

---

## Available Runners

### BaseTestRunner (All Tests)

**Purpose:** Routes to ALL test types (DB, UI, API)

**Glue Paths:**
```java
"com.qa.framework.stepdefinitions.db," +
"com.qa.framework.stepdefinitions.ui," +
"com.qa.framework.stepdefinitions.api"
```

**Usage:**
```bash
mvn test -Dtest=com.qa.framework.runners.BaseTestRunner
```

**When to use:** Run all tests together (integration testing)

---

### DBTestRunner (Database Tests Only)

**Purpose:** Routes only @DB tagged tests to database step definitions

**Filter:** `@DB`

**Glue Path:** `com.qa.framework.stepdefinitions.db`

**Usage:**
```bash
mvn test -Dtest=com.qa.framework.runners.DBTestRunner
```

**When to use:** Database-only testing, DB regression tests

---

### UITestRunner (UI Tests Only)

**Purpose:** Routes only @UI tagged tests to UI step definitions

**Filter:** `@UI`

**Glue Path:** `com.qa.framework.stepdefinitions.ui` (update to your UI library)

**Usage:**
```bash
mvn test -Dtest=com.qa.framework.runners.UITestRunner
```

**When to use:** UI-only testing, UI regression tests

---

### APITestRunner (API Tests Only)

**Purpose:** Routes only @API tagged tests to API step definitions

**Filter:** `@API`

**Glue Path:** `com.qa.framework.stepdefinitions.api` (update to your API library)

**Usage:**
```bash
mvn test -Dtest=com.qa.framework.runners.APITestRunner
```

**When to use:** API-only testing, API regression tests

---

## Integrating Your UI and API Libraries

### Step 1: Update Glue Paths in Runners

**Edit UITestRunner.java:**
```java
@ConfigurationParameter(
    key = GLUE_PROPERTY_NAME, 
    value = "com.yourcompany.ui.stepdefinitions"  // Your UI library package
)
```

**Edit APITestRunner.java:**
```java
@ConfigurationParameter(
    key = GLUE_PROPERTY_NAME, 
    value = "com.yourcompany.api.stepdefinitions"  // Your API library package
)
```

### Step 2: Add Dependencies in Template Project

**Edit ACEBaseCustomTemplate/pom.xml:**
```xml
<dependencies>
    <!-- DB Library (this one) -->
    <dependency>
        <groupId>com.qa.framework</groupId>
        <artifactId>custom-ace-base-wrapper</artifactId>
        <version>1.0.0</version>
    </dependency>
    
    <!-- Your UI Library -->
    <dependency>
        <groupId>com.yourcompany</groupId>
        <artifactId>ui-test-library</artifactId>
        <version>1.0.0</version>
    </dependency>
    
    <!-- Your API Library -->
    <dependency>
        <groupId>com.yourcompany</groupId>
        <artifactId>api-test-library</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependencies>
```

### Step 3: Create Feature Files with Appropriate Tags

**features/database.feature:**
```gherkin
@DB
Feature: Database Tests
  Scenario: Test database connection
    Given I set the active database profile to "mysql"
    When I connect to the database "testdb"
    Then I should be connected
```

**features/ui.feature:**
```gherkin
@UI
Feature: UI Tests
  Scenario: Test login page
    Given I open the login page
    When I enter credentials
    Then I should see dashboard
```

**features/api.feature:**
```gherkin
@API
Feature: API Tests
  Scenario: Test GET endpoint
    Given I have API endpoint "/users"
    When I send GET request
    Then response status should be 200
```

### Step 4: Run Tests

```bash
# Run only DB tests
mvn test -Dtest=com.qa.framework.runners.DBTestRunner

# Run only UI tests
mvn test -Dtest=com.qa.framework.runners.UITestRunner

# Run only API tests
mvn test -Dtest=com.qa.framework.runners.APITestRunner

# Run all tests
mvn test -Dtest=com.qa.framework.runners.BaseTestRunner
```

---

## Multi-Library Architecture

```
┌─────────────────────────────────────────────────────────────┐
│  Your Test Project                                           │
│                                                              │
│  features/                                                   │
│  ├─ database/                                               │
│  │  ├─ users.feature (@DB)                                 │
│  │  └─ orders.feature (@DB)                                │
│  ├─ ui/                                                     │
│  │  ├─ login.feature (@UI)                                 │
│  │  └─ checkout.feature (@UI)                              │
│  └─ api/                                                    │
│     ├─ rest.feature (@API)                                 │
│     └─ graphql.feature (@API)                              │
│                                                              │
│  pom.xml (imports all 3 libraries)                         │
└────────────────────────┬────────────────────────────────────┘
                         │
                         │ uses runners from
                         ▼
┌─────────────────────────────────────────────────────────────┐
│  custom-ace-base-wrapper (DB Library)                       │
│  ├─ runners/ (BaseTestRunner, DBTestRunner, etc.)          │
│  └─ stepdefinitions/db/                                     │
└────────────────────────┬────────────────────────────────────┘
                         │
                         │ routes to
                         ▼
┌─────────────────────────────────────────────────────────────┐
│  ui-test-library (Your UI Library)                          │
│  └─ stepdefinitions/ui/                                     │
│     ├─ LoginSteps                                           │
│     ├─ NavigationSteps                                      │
│     └─ CheckoutSteps                                        │
└─────────────────────────────────────────────────────────────┘
                         │
                         │ and
                         ▼
┌─────────────────────────────────────────────────────────────┐
│  api-test-library (Your API Library)                        │
│  └─ stepdefinitions/api/                                    │
│     ├─ RestSteps                                            │
│     ├─ GraphQLSteps                                         │
│     └─ AuthSteps                                            │
└─────────────────────────────────────────────────────────────┘
```

---

## CI/CD Pipeline Integration

### Jenkins Example

```groovy
pipeline {
    stages {
        stage('DB Tests') {
            steps {
                sh 'mvn test -Dtest=com.qa.framework.runners.DBTestRunner'
            }
        }
        stage('UI Tests') {
            steps {
                sh 'mvn test -Dtest=com.qa.framework.runners.UITestRunner'
            }
        }
        stage('API Tests') {
            steps {
                sh 'mvn test -Dtest=com.qa.framework.runners.APITestRunner'
            }
        }
    }
}
```

### GitLab CI Example

```yaml
db-tests:
  script:
    - mvn test -Dtest=com.qa.framework.runners.DBTestRunner
  artifacts:
    reports:
      junit: target/cucumber-reports/db-tests.xml

ui-tests:
  script:
    - mvn test -Dtest=com.qa.framework.runners.UITestRunner
  artifacts:
    reports:
      junit: target/cucumber-reports/ui-tests.xml

api-tests:
  script:
    - mvn test -Dtest=com.qa.framework.runners.APITestRunner
  artifacts:
    reports:
      junit: target/cucumber-reports/api-tests.xml
```

---

## Benefits of This Architecture

### ✅ Centralized Routing
- All runners in one place (wrapper library)
- No need to create runners in each test project
- Easy to update routing logic

### ✅ Library Independence
- DB, UI, and API libraries are independent
- Each library can be developed separately
- Easy to add new libraries

### ✅ Tag-Based Routing
- Clear separation by tags (@DB, @UI, @API)
- Easy to filter and run specific test types
- Flexible test organization

### ✅ Separate Reports
- Each runner generates its own report
- Easy to track DB vs UI vs API test results
- Better debugging and analysis

### ✅ Parallel Execution
- Can run DB, UI, and API tests in parallel
- Faster CI/CD pipelines
- Better resource utilization

---

## Advanced: Custom Runners

If you need custom routing logic, extend the base runners:

```java
package com.qa.template.runners;

import com.qa.framework.runners.DBTestRunner;
import org.junit.platform.suite.api.ConfigurationParameter;

import static io.cucumber.junit.platform.engine.Constants.FILTER_TAGS_PROPERTY_NAME;

/**
 * Custom runner for smoke tests only
 */
@ConfigurationParameter(
    key = FILTER_TAGS_PROPERTY_NAME, 
    value = "@DB and @Smoke"
)
public class DBSmokeTestRunner extends DBTestRunner {
}
```

---

## Summary

**Runners are interceptors that:**
1. ✅ Filter tests by tags
2. ✅ Route to appropriate step definitions
3. ✅ Generate separate reports
4. ✅ Enable parallel execution
5. ✅ Centralize test routing logic

**Your test project only needs:**
- Feature files with tags
- Import the wrapper library (with runners)
- Import your UI/API libraries
- Run: `mvn test -Dtest=<RunnerName>`

**No need to create runners in test projects!** They're all in the wrapper library.
