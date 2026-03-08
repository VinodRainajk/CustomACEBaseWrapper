# API Architecture (Proposal)

## Overview

This document lists proposed API operations and the architecture for API testing, from InterceptorRunner down to the API layer. **Review only—no code changes yet.**

---

## 1. List of API Operations

### HTTP Methods
| Operation | Description | Example |
|-----------|-------------|---------|
| GET | Retrieve resource(s) | Fetch user, list items |
| POST | Create resource | Create user, submit form |
| PUT | Replace resource | Full update |
| PATCH | Partial update | Update specific fields |
| DELETE | Remove resource | Delete user |

### Setup / Configuration
| Operation | Description |
|-----------|-------------|
| Set base URL | From config (master + feature override) or explicit override |
| Set endpoint path | Set or append path for request |
| Set request headers | Content-Type, Authorization, custom headers |
| Set query parameters | ?key=value&foo=bar |
| Set path parameters | /users/{id} |
| Set request body from payload file | Load from path (e.g., user/create-user → payloads/user/create-user.json) |
| Set authentication | Basic, Bearer, API Key, OAuth |
| Set timeout | Request timeout in ms/seconds |

### Request Execution
| Operation | Description |
|-----------|-------------|
| Send GET request | GET to endpoint |
| Send POST request | POST with body |
| Send PUT request | PUT with body |
| Send PATCH request | PATCH with body |
| Send DELETE request | DELETE to endpoint |
| Send request with headers | Custom headers for this request |
| Send multipart request | Form data / file upload |

### Response Validation – Status
| Operation | Description |
|-----------|-------------|
| Assert status code | 200, 201, 204, 400, 401, 404, 500, etc. |
| Assert status code is success | 2xx |
| Assert status code is client error | 4xx |
| Assert status code is server error | 5xx |
| Assert status code is redirect | 3xx |

### Response Validation – Body
| Operation | Description |
|-----------|-------------|
| Assert response body contains text | String presence |
| Assert response body equals | Exact match |
| Assert JSON path value | $.user.name, $.data[0].id |
| Assert JSON path equals | Specific value |
| Assert JSON path exists | Key/path exists |
| Assert JSON array size | Length of array |
| Assert JSON schema | Schema validation |
| Assert XML path value | XPath query |

### Response Validation – Headers
| Operation | Description |
|-----------|-------------|
| Assert response header | Header present and value |
| Assert content type | application/json, application/xml |
| Assert header contains | Partial match |

### Cookies & Session
| Operation | Description |
|-----------|-------------|
| Set cookie | Add cookie to request |
| Assert response cookie | Cookie present and value |
| Clear cookies | Reset cookies |

### Error Handling
| Operation | Description |
|-----------|-------------|
| Assert request fails | Expect error (4xx/5xx) |
| Assert error message contains | Error message validation |
| Capture response for debugging | Store last response |

### Advanced
| Operation | Description |
|-----------|-------------|
| Save response value to variable | Extract for later steps |
| Use variable in request | Dynamic request building |
| Chain requests | Use response from one as input to next |
| GraphQL query | Execute GraphQL query |
| GraphQL mutation | Execute GraphQL mutation |
| WebSocket connect/send/receive | WebSocket support (optional) |
| File upload | Multipart file upload |
| File download | Download and validate file |
| SOAP request | XML/SOAP envelope (if needed) |

---

## 2. Payloads (External, Referenced by Path)

**Principle:** Request payloads are **not** embedded in feature files. They live in a dedicated **payloads/** folder and are referenced by path. A feature can have multiple test cases, each using different payloads.

### Base Directory
- **Always** `payloads/` — all payload fetches resolve under this directory.
- Feature files never include the `payloads/` prefix; it is implicit.

### Structure
```
src/test/resources/
└── payloads/
    ├── user/
    │   ├── create-user.json
    │   ├── update-user.json
    │   └── delete-user.json
    ├── order/
    │   ├── create-order.json
    │   └── update-order.json
    └── api/
        └── v2/
            └── search-query.json
```

### Path in Feature File
Use the path **relative to payloads/** (no `payloads/` prefix):

| Path in feature       | Resolves to file                          |
|-----------------------|-------------------------------------------|
| `user/create-user`    | `payloads/user/create-user.json`          |
| `user/update-user`    | `payloads/user/update-user.json`          |
| `order/create-order`  | `payloads/order/create-order.json`        |
| `api/v2/search-query` | `payloads/api/v2/search-query.json`       |

### Usage in Feature Files
```gherkin
Scenario: Create user
  Given the API base URL from config
  When I send a POST request to "/users" with payload "user/create-user"
  Then the response status code should be 201

Scenario: Update user
  Given the API base URL from config
  When I send a PUT request to "/users/1" with payload "user/update-user"
  Then the response status code should be 200

Scenario: Create order
  When I send a POST request to "/orders" with payload "order/create-order"
  Then the response status code should be 201
```

**Note:** The payload path must be in quotes (e.g. `"user/create-user"`) so Cucumber's `{string}` parameter matches it.

The step definition resolves: `payloads/` + path + `.json` (or configurable default extension).

### Resolution
- Base: `src/test/resources/payloads/` (or classpath equivalent)
- Path from feature: `user/create-user` → `payloads/user/create-user.json`

---

## 3. Configuration (Master + Optional Feature-Specific Override)

**Principle:** API URLs and other settings come from config files. A **master config** is the base. A **feature-specific config** is **optional** and overrides only when present.

### Config Hierarchy
```
1. master.yaml (or master-config.yaml)   → Base config; used for all features
2. {feature-name}-config.yaml            → OPTIONAL; if present, overrides master for that feature only
```

**Fallback:** If `google-config.yaml` is **not present**, all values come from `master.yaml`. The feature-specific config is optional.

### Naming Convention
| Feature File     | Feature-Specific Config |
|------------------|-------------------------|
| google.feature   | google-config.yaml      |
| user-api.feature | user-api-config.yaml    |
| orders.feature   | orders-config.yaml      |

**Rule:** For feature file `{name}.feature`, the override config is `{name}-config.yaml` (e.g., `google.feature` → `google-config.yaml`).

### Example

**master.yaml** (base for all features):
```yaml
application:
  url: https://api.example.com
  timeout: 30000
auth:
  type: bearer
```

**google-config.yaml** (override when running `google.feature`):
```yaml
application:
  url: https://www.google.com  # Overrides master
# auth, timeout inherit from master unless overridden
```

### Resolution Logic
1. Load `master.yaml` (base config—always used)
2. If current feature is `google.feature`, look for `google-config.yaml`
3. If `google-config.yaml` **exists** → merge: feature-specific values override master; unset values inherit from master
4. If `google-config.yaml` **does not exist** → use `master.yaml` as-is
5. Use resolved config for base URL, headers, timeout, etc.

### Config Location
```
src/test/resources/
├── config/
│   ├── master.yaml
│   ├── google-config.yaml
│   ├── user-api-config.yaml
│   └── orders-config.yaml
```

### Usage in Step Definitions
- Base URL: `config.application.url` (from merged config)
- Other settings: `config.auth`, `config.application.timeout`, etc.
- Step `Given I have the API base URL` can use config by default, or accept explicit override

---

## 4. Proposed Categorized Step Definition Classes (API)

| Category | Class | Purpose |
|----------|-------|---------|
| Hooks | APIHooks | @Before/@After setup, teardown; load merged config (master + feature override) |
| Context | APIStepContext | Shared state (baseUrl from config, lastRequest, lastResponse, payload path resolver) |
| Setup | APIConfigurationStepDefinitions | Base URL from config, headers, auth, timeout; payload from path |
| Request | APIRequestStepDefinitions | Send GET/POST/PUT/PATCH/DELETE |
| Response Status | APIResponseStatusStepDefinitions | Status code assertions |
| Response Body | APIResponseBodyStepDefinitions | Body, JSON path, XML assertions |
| Response Headers | APIResponseHeadersStepDefinitions | Header assertions |
| Cookies | APICookieStepDefinitions | Cookie set/assert |
| Error | APIErrorHandlingStepDefinitions | Failure assertions |
| Advanced | APIVariableStepDefinitions | Save/use variables |
| GraphQL | APIGraphQLStepDefinitions | GraphQL query/mutation (optional) |
| File | APIFileStepDefinitions | Upload/download (optional) |

---

## 5. API Architecture Diagram

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
│  │  • @DB  → DBTestRunner                                                                          │  │
│  │  • @UI  → UITestRunner                                                                          │  │
│  │  • @API → APITestRunner (ACBase)                                                                │  │
│  └─────────────────────────────────────────────────────────────────────────────────────────────────┘  │
└───────────────────────────────────────────────────────────────────────────────────────────────────────┘
                                                        │
                                    @API scenarios only │
                                                        ▼
┌───────────────────────────────────────────────────────────────────────────────────────────────────────┐
│  APITestRunner (ACBase)                                                                                 │
│  • @Suite @IncludeEngines("cucumber")                                                                   │
│  • FILTER_TAGS_PROPERTY = @API                                                                         │
│  • GLUE_PROPERTY = com.qa.framework.stepdefinitions.api                                                │
│  • FEATURES = src/test/resources/features                                                              │
└───────────────────────────────────────────────────────────────────────────────────────────────────────┘
                                                        │
                                                        │  Loads glue package
                                                        ▼
┌───────────────────────────────────────────────────────────────────────────────────────────────────────┐
│  stepdefinitions.api PACKAGE (CustomACEBaseWrapper)                                                     │
│                                                                                                        │
│  ┌──────────────────────────┐    ┌──────────────────────────────────────────────────────────────┐    │
│  │  APIHooks                │    │  APIStepContext (shared state)                               │    │
│  │  @Before("@API") setUp   │───▶│  • baseUrl, headers, auth                                    │    │
│  │  @After("@API") tearDown │    │  • lastRequest, lastResponse                                 │    │
│  └──────────────────────────┘    │  • extractedVariables                                        │    │
│                                  └──────────────────────────────────────────────────────────────┘    │
│                                                    ▲                                                    │
│                                                    │ uses                                                │
│  ┌────────────────────────────────────────────────────────────────────────────────────────────────┐   │
│  │  CATEGORIZED STEP DEFINITION CLASSES (Proposed)                                                │   │
│  ├────────────────────────────────────────────────────────────────────────────────────────────────┤   │
│  │  APIConfigurationStepDefinitions      │ Base URL, headers, auth, timeout                      │   │
│  │  APIRequestStepDefinitions            │ Send GET/POST/PUT/PATCH/DELETE                       │   │
│  │  APIResponseStatusStepDefinitions     │ Status code assertions                               │   │
│  │  APIResponseBodyStepDefinitions       │ Body, JSON path, XML                                 │   │
│  │  APIResponseHeadersStepDefinitions    │ Header assertions                                    │   │
│  │  APICookieStepDefinitions             │ Cookie set/assert                                    │   │
│  │  APIErrorHandlingStepDefinitions      │ Failure assertions                                   │   │
│  │  APIVariableStepDefinitions           │ Save/use variables                                   │   │
│  └────────────────────────────────────────────────────────────────────────────────────────────────┘   │
└───────────────────────────────────────────────────────────────────────────────────────────────────────┘
                                                        │
                                                        │  HTTP Client (e.g., RestAssured)
                                                        ▼
┌───────────────────────────────────────────────────────────────────────────────────────────────────────┐
│  API LAYER                                                                                             │
│  • REST Client (RestAssured / HttpClient)                                                              │
│  • Request builder: method, URL, headers, body                                                         │
│  • Response: status, body, headers                                                                     │
└───────────────────────────────────────────────────────────────────────────────────────────────────────┘
                                                        │
                                                        │  HTTP/HTTPS
                                                        ▼
┌───────────────────────────────────────────────────────────────────────────────────────────────────────┐
│  EXTERNAL / UNDER TEST APIs                                                                            │
│  REST APIs | GraphQL | SOAP (optional)                                                                 │
└───────────────────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 6. Execution Flow (Sequence)

```
Template                    InterceptorRunner              APITestRunner                API Step Definitions           API Client
   │                              │                             │                            │                              │
   │  mvn test                    │                             │                            │                              │
   │  -Dtest=InterceptorRunnerTest│                             │                            │                              │
   ├─────────────────────────────▶│                             │                            │                              │
   │                              │  JUnit Suite invokes        │                            │                              │
   │                              │  APITestRunner              │                            │                              │
   │                              ├────────────────────────────▶│                            │                              │
   │                              │                             │  Cucumber picks @API       │                              │
   │                              │                             │  scenarios                 │                              │
   │                              │                             ├────────────────────────────▶│                              │
   │                              │                             │                            │  @Before: APIHooks setup      │
   │                              │                             │                            │  Given I have base URL        │
   │                              │                             │                            │  → APIConfigurationStepDef   │
   │                              │                             │                            │  When I send GET to /users    │
   │                              │                             │                            │  → APIRequestStepDef         │
   │                              │                             │                            │  → HTTP client executes      │
   │                              │                             │                            ├──────────────────────────────▶│
   │                              │                             │                            │  Then status should be 200    │
   │                              │                             │                            │  → APIResponseStatusStepDef  │
   │                              │                             │                            │  Then body contains "id"      │
   │                              │                             │                            │  → APIResponseBodyStepDef    │
   │                              │                             │                            │  @After: tearDown             │
   │                              │                             │◀────────────────────────────┤                              │
   │                              │◀────────────────────────────│                            │                              │
   │◀─────────────────────────────│                             │                            │                              │
```

---

## 7. Dependencies (Proposed)

| Library | Purpose |
|---------|---------|
| RestAssured | REST API client, fluent assertions |
| JsonPath | JSON path extraction (included with RestAssured) |
| Jackson / Gson | JSON serialization |
| (Optional) OkHttp / HttpClient | Alternative HTTP client |
| (Optional) GraphQL Java | GraphQL support |

---

## 8. Config & Payload Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│  Feature: google.feature                                                             │
└────────────────────────────────────┬────────────────────────────────────────────────┘
                                     │
                                     ▼
┌─────────────────────────────────────────────────────────────────────────────────────┐
│  CONFIG RESOLUTION                                                                   │
│  1. Load master.yaml (application.url, timeout, auth, ...) — always                  │
│  2. If google-config.yaml exists → load and merge (overrides master)                 │
│     If NOT exists → use master.yaml as-is                                            │
│  3. Result: from feature-config if present, else from master                         │
└────────────────────────────────────┬────────────────────────────────────────────────┘
                                     │
                                     ▼
┌─────────────────────────────────────────────────────────────────────────────────────┐
│  PAYLOAD RESOLUTION                                                                  │
│  Step: "with payload user/create-user"                                               │
│  → Resolve: payloads/ + user/create-user + .json → payloads/user/create-user.json    │
│  → Use as request body (no payload in feature file)                                  │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 9. Review Checklist

- [ ] Confirm list of operations covers your needs
- [ ] Confirm proposed categories / classes
- [ ] Confirm RestAssured as HTTP client
- [ ] Add/remove operations as needed
- [ ] Decide: GraphQL, SOAP, WebSocket support?
- [ ] Confirm payload folder structure and path resolution
- [ ] Confirm config hierarchy: master + feature-name-config override
