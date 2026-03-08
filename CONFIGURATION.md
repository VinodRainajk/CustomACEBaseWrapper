# Configuration Guide

This document describes the configuration structure for database and API testing, including the profile-as-folder approach for environment-specific configs.

---

## Overview

- **Profile** = environment (dev, qa, staging, uat, preprod, prod, etc.)
- **Profile is passed from Maven**: `-Dprofile=dev`
- **Option B: Profile as folder** — each profile has its own subfolder under `config/`
- **No hardcoded profile names** — add a folder to add a profile; any folder name is valid

---

## Profile Selection

Pass the profile via system property when running tests:

```bash
# Run with dev profile
mvn test -Dprofile=dev

# Run with qa profile
mvn test -Dprofile=qa

# Run with staging profile
mvn test -Dprofile=staging

# Run without profile (base config only)
mvn test
```

The profile name is read from `System.getProperty("profile")`. If not set, only base configs are used.

---

## Folder Structure (Option B)

```
src/test/resources/
└── config/
    ├── master_database.yml           # Base DB config (always)
    ├── master.yaml                   # Base API config (always)
    ├── cross-db-database.yml         # Feature override (DB)
    ├── user-api-config.yaml          # Feature override (API)
    │
    ├── dev/                          # Profile: dev
    │   ├── master_database.yml
    │   ├── master.yaml
    │   ├── cross-db-database.yml
    │   └── user-api-config.yaml
    │
    ├── qa/                           # Profile: qa
    │   ├── master_database.yml
    │   └── master.yaml
    │
    ├── staging/                      # Profile: staging
    │   └── master_database.yml
    │
    └── uat/                          # Profile: uat
        └── master_database.yml
```

---

## Resolution Order

### Database Config

1. **Base**: `config/master_database.yml`
2. **Profile override**: `config/{profile}/master_database.yml` (if `-Dprofile=X` and file exists)
3. **Feature override**: `config/{feature-name}-database.yml` (e.g. cross-db-database.yml)
4. **Profile + feature**: `config/{profile}/{feature-name}-database.yml` (if profile set and file exists)
5. **Sections**: scenario-level overrides within the feature config

### API Config

1. **Base**: `config/master.yaml`
2. **Profile override**: `config/{profile}/master.yaml` (if profile set)
3. **Feature override**: `config/{feature-name}-config.yaml`
4. **Profile + feature**: `config/{profile}/{feature-name}-config.yaml` (if profile set)

---

## Adding a New Profile

1. Create a folder under `config/` with the profile name:
   ```
   config/your-profile/
   ```

2. Add the config files you want to override:
   - `master_database.yml` — for DB config
   - `master.yaml` — for API config
   - `{feature}-database.yml` — for feature-specific DB overrides
   - `{feature}-config.yaml` — for feature-specific API overrides

3. Run tests with that profile:
   ```bash
   mvn test -Dprofile=your-profile
   ```

**Example: Add preprod**

```text
config/
└── preprod/
    ├── master_database.yml   # Override DB URLs, credentials for preprod
    └── master.yaml           # Override API base URL for preprod
```

No code changes required. The framework discovers profiles by folder name.

---

## Profile Naming

Profiles are defined by folder names. Teams can use any naming:

| Team A   | Team B    | Team C   |
|----------|-----------|----------|
| dev      | dev       | local    |
| qa       | qa        | dev      |
| staging  | staging   | uat      |
| prod     | uat       | preprod  |
|          | prod      | prod     |

---

## Maven Surefire

Ensure the profile system property is passed to tests. In `pom.xml`:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <systemPropertyVariables>
            <profile>${profile}</profile>
        </systemPropertyVariables>
    </configuration>
</plugin>
```

Then:

```bash
mvn test -Dprofile=qa
```

---

## Database Config Details

See [DATABASE_ARCHITECTURE.md](DATABASE_ARCHITECTURE.md) for:

- Config names (mysql, oracle, etc.) used in steps
- Feature-specific and section-level overrides
- Step syntax: `I connect to database "mysql"`

---

## API Config Details

See [API_ARCHITECTURE.md](API_ARCHITECTURE.md) for:

- Base URL and auth config
- Feature-specific overrides
- Payload structure
