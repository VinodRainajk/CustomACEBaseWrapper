# Configuration Guide

All config files live **inside profile folders**. No config at root. Profile is selected via `-Dprofile=dev`; default is `local` when omitted.

---

## Overview

- **Profile** = environment (local, dev, qa, staging, uat, preprod, prod, etc.)
- **Default profile = local** when no `-Dprofile` is passed
- **All config inside** `config/{profile}/` — nothing at `config/` root
- Add a folder to add a profile; any folder name is valid

---

## Profile Selection

```bash
# Run with local (default) — uses config/local/
mvn test

# Same as above
mvn test -Dprofile=local

# Run with dev — uses config/dev/
mvn test -Dprofile=dev

# Run with qa — uses config/qa/
mvn test -Dprofile=qa
```

---

## Folder Structure

```
src/test/resources/
└── config/
    ├── local/                     # Default (mvn test or mvn test -Dprofile=local)
    │   ├── master_database.yml
    │   ├── master-api.yaml
    │   ├── cross-db-database.yml
    │
    ├── dev/
    │   ├── master_database.yml
    │   ├── master-api.yaml
    │   └── cross-db-database.yml
    │
    ├── qa/
    │   ├── master_database.yml
    │   ├── master-api.yaml
    │   └── cross-db-database.yml
    │
    └── staging/
        ├── master_database.yml
        └── master-api.yaml
```

**No config files at `config/` root.**

---

## Resolution

Config is loaded only from the active profile folder:

- **Database:** `config/{profile}/master_database.yml` + `config/{profile}/{feature}-database.yml` + sections
- **API:** `config/{profile}/master-api.yaml` + `config/{profile}/{feature}-config-api.yaml`

---

## Adding a New Profile

1. Create `config/your-profile/`
2. Add `master_database.yml`, `master-api.yaml`, and any feature-specific overrides
3. Run: `mvn test -Dprofile=your-profile`

---

## Maven Surefire

```xml
<configuration>
    <systemPropertyVariables>
        <profile>${profile}</profile>
    </systemPropertyVariables>
</configuration>
```

---

## Environment Tags (Feature Filtering)

Feature files can be filtered by environment using tags at the top of each feature:

| Tag       | Meaning                                   |
|-----------|-------------------------------------------|
| `@all`    | Run in all environments                   |
| `@local`  | Run only when `profile=local` (default)   |
| `@dev`    | Run only when `profile=dev`               |
| `@qa`     | Run only when `profile=qa`                |
| `@staging`| Run only when `profile=staging`           |
| `@preprod`| Run only when `profile=preprod`           |
| `@prod`   | Run only when `profile=prod`              |
| `@nonProd`| Run in all environments except production |

**Examples:**

```gherkin
@DB @all
Feature: Runs in every environment

@DB @qa
Feature: Runs only in QA

@API @nonProd
Feature: Runs in local, dev, qa, staging, preprod (not prod)

@DB @local @Smoke
Feature: Runs only with mvn test (default local)
```

**Commands:**

```bash
# Default (local) – runs @all and @local
mvn clean test

# QA – runs @all, @qa, @nonProd
mvn clean test -Dprofile=qa

# Production – runs only @all and @prod (excludes @nonProd)
mvn clean test -Dprofile=prod
```
