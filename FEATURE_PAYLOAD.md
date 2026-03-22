# Feature payload YAML (`{featureName}_payload.yml`)

Keep Gherkin free of long SQL and JSON bodies. Put them in one YAML per feature file.

## File locations (classpath)

1. **Base (shared):** `src/test/resources/payloads/features/{featureName}_payload.yml`  
   `{featureName}` = feature file base name, e.g. `user-api.feature` → `user-api_payload.yml`.

2. **Profile override (optional):** `src/test/resources/config/{profile}/{featureName}_payload.yml`  
   Merged on top of the base file for the active `-Dprofile` (same as DB/API config).

## YAML shape

```yaml
# SQL strings (any keys under queries.*)
queries:
  count_cities: "SELECT COUNT(*) AS city_count FROM city"
  top_five: "SELECT Name, Population FROM city ORDER BY Population DESC LIMIT 5"

# API paths (relative to base URL from API config)
paths:
  user_by_id: "/users/1"
  posts: "/posts"

# Raw JSON bodies (use | for multiline)
bodies:
  create_user: |
    {"name": "Test User", "username": "testuser", "email": "t@example.com"}

# Parameterised SELECT (optional)
prepared:
  city_by_id:
    query: "SELECT * FROM city WHERE ID = ?"
    parameters:
      - "1"
```

## Gherkin steps (wrapper)

### Database

| Step | Payload key example |
|------|---------------------|
| `When I execute the query from feature payload "queries.count_cities"` | Scalar string under dotted path |
| `When I execute the query from feature payload "queries.x" on "mysql"` | Second arg = connection name |
| `When I execute the prepared query from feature payload "city_by_id"` | Block under `prepared.city_by_id` |
| `When I execute the insert query from feature payload "inserts.row"` | Same pattern for DML keys you define |
| `When I execute the update query from feature payload "updates.noop"` | |
| `When I execute the delete query from feature payload "deletes.safe"` | |

### API

**Base URL (once per scenario):** Tag scenarios with `@API`. The hook loads `config/{profile}/master-api.yaml` (+ optional `{feature}-config-api.yaml`) and sets the base URL from `application.url`. You can repeat or override it in **Background** / **Given** with `Given the API base URL from config` or `Given I have the API base URL "https://..."`.

**Paths and bodies:** You can keep both in `{feature}_payload.yml`, or put **paths** in API config YAML and **JSON bodies** in the feature payload file.

**Reusable body (recommended):** Define the JSON body once per scenario block, then send with POST/PUT/PATCH — same step text everywhere.

```gherkin
When I set the body from feature payload "bodies.create_user"
And I send a POST request to path from feature payload "paths.users"
```

Order matters: **set body** before **send**. GET/DELETE clear any pending body so it cannot leak into the next step.

| Step | Where keys resolve |
|------|---------------------|
| `When I set the body from feature payload "bodies.create_user"` | Feature payload YAML (scalar); stored for the next POST/PUT/PATCH path step |
| `When I send a GET request to path from feature payload "paths.user_by_id"` | Feature payload YAML |
| `When I send a GET request to path from API config "paths.users"` | Merged API YAML (`master-api.yaml` + feature override), dotted path |
| `When I send a POST request to path from feature payload "paths.users"` | Path from feature payload; **body** = last `I set the body from feature payload "..."` |
| `When I send a PUT request to path from feature payload "paths.user_by_id"` | Same pattern |
| `When I send a PATCH request to path from feature payload "paths.user_by_id"` | Same pattern |
| `When I send a POST request to path from API config "paths.users"` | Path from API YAML; body from pending step |
| `When I send a PUT request to path from API config "paths.user_by_id"` | |
| `When I send a PATCH request to path from API config "paths.user_by_id"` | |
| `When I send a POST request to path from feature payload "paths.users" with body from feature payload "bodies.create_user"` | One-line alternative (both from feature payload) |
| `When I send a POST request with path from API config "paths.users" and body from feature payload "bodies.create_user"` | Path from API YAML, body from feature payload (base URL already from hook/Background) |
| `When I send a POST request applying base URL from API config "application.url" with path from API config "paths.users" and body from feature payload "bodies.create_user"` | Same as above but re-reads base URL from config in this step (optional one-liner) |
| `When I send a PUT request with path from API config "paths.user_by_id" and body from feature payload "bodies.update_user"` | |
| `When I send a PATCH request with path from API config "paths.user_by_id" and body from feature payload "bodies.patch_user"` | |
| `When I send a PUT request to path from feature payload "paths.user_by_id" with body from feature payload "bodies.update_user"` | Feature payload |
| `When I send a PATCH request to path from feature payload "paths.user_by_id" with body from feature payload "bodies.patch_user"` | Feature payload |
| `When I send a DELETE request to path from feature payload "paths.user_by_id"` | Feature payload |

Dotted keys (e.g. `paths.users`, `application.url`) use the **merged API config map** loaded for the active feature and profile.

### Existing JSON payloads

Steps like `with payload "user/create-user"` still load `payloads/user/create-user.json`. The feature YAML is **additional** for keeping SQL and inline JSON out of `.feature` files.

## Hooks

`FeaturePayloadHooks` sets the active feature name from each scenario’s URI so the correct `{featureName}_payload.yml` is loaded automatically. It also clears the pending feature-payload body at scenario start and end.

**Glue:** Include `com.qa.framework.payload` in your Cucumber glue (see `BaseTestRunner`, `UIAPITestNGRunner`, `DBTestRunner`) so `FeaturePayloadHooks` and `I set the body from feature payload` are registered.
