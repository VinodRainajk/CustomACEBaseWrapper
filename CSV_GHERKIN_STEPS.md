# CSV Gherkin Steps Guide

This document explains all CSV comparison Gherkin steps supported by the DB wrapper, with a short description and an example for each step.

## Prerequisite

Before using CSV validation steps, run a query so data exists in scenario context:

```gherkin
When I execute the query "{queries.city_count}"
```

`{queries.city_count}` is resolved from your feature payload file (example: `payloads/features/world-city-csv_payload.yml`).

---

## 1) Ordered match

**Step**

```gherkin
Then the query result should match CSV from feature payload "expected.city_count_ordered" in order
```

**Description**

Validates query result against CSV with row order and row values both required to match.

**Example scenario**

```gherkin
Scenario: Ordered CSV match
  When I execute the query "{queries.city_count}"
  Then the query result should match CSV from feature payload "expected.city_count_ordered" in order
```

---

## 2) Unordered match

**Step**

```gherkin
Then the query result should match CSV from feature payload "expected.city_count_ordered" ignoring order
```

**Description**

Validates query result against CSV as an unordered set of rows. Row values must match, row sequence can differ.

**Example scenario**

```gherkin
Scenario: Unordered CSV match
  When I execute the query "{queries.city_count}"
  Then the query result should match CSV from feature payload "expected.city_count_ordered" ignoring order
```

---

## 3) Ordered negative assertion

**Step**

```gherkin
Then the query result should not match CSV from feature payload "expected.city_count_mismatch" in order
```

**Description**

Asserts that ordered comparison should fail (useful for negative tests).

**Example scenario**

```gherkin
Scenario: Ordered CSV mismatch expected
  When I execute the query "{queries.city_count}"
  Then the query result should not match CSV from feature payload "expected.city_count_mismatch" in order
```

---

## 4) Unordered negative assertion

**Step**

```gherkin
Then the query result should not match CSV from feature payload "expected.city_count_mismatch" ignoring order
```

**Description**

Asserts that unordered comparison should fail.

**Example scenario**

```gherkin
Scenario: Unordered CSV mismatch expected
  When I execute the query "{queries.city_count}"
  Then the query result should not match CSV from feature payload "expected.city_count_mismatch" ignoring order
```

---

## 5) Deferred match (for export flow)

**Step**

```gherkin
Then the query result should match CSV from feature payload "expected.city_count_mismatch"
```

**Description**

Runs CSV comparison in deferred mode and stores mismatches in memory. Final pass/fail happens in export step.

**Example scenario**

```gherkin
Scenario: Deferred match check before export
  When I execute the query "{queries.city_count}"
  Then the query result should match CSV from feature payload "expected.city_count_mismatch"
  And I export CSV mismatches with remarks to "target/csv-failures/world-city-mismatch-report.csv"
```

---

## 6) Deferred negative match (for export flow)

**Step**

```gherkin
Then the query result should not match CSV from feature payload "expected.city_count_ordered"
```

**Description**

Runs CSV comparison in deferred mode for negative expectation and stores mismatch details for export.

**Example scenario**

```gherkin
Scenario: Deferred negative check before export
  When I execute the query "{queries.city_count}"
  Then the query result should not match CSV from feature payload "expected.city_count_ordered"
  And I export CSV mismatches with remarks to "target/csv-failures/world-city-negative-report.csv"
```

---

## 7) Export mismatches with remarks

**Step**

```gherkin
And I export CSV mismatches with remarks to "target/csv-failures/world-city-mismatch-report.csv"
```

**Description**

Exports mismatch artifact as CSV. This step also performs the final assertion for deferred steps (pass/fail).

**Report columns**

- `row_type`
- `row_identifier`
- `column_name`
- `expected_value`
- `actual_value`
- `remarks`

**Example scenario (recommended 3-step flow)**

```gherkin
Scenario: Compare and export mismatch report
  When I execute the query "{queries.city_count}"
  Then the query result should match CSV from feature payload "expected.city_count_mismatch"
  And I export CSV mismatches with remarks to "target/csv-failures/world-city-mismatch-report.csv"
```

---

## Example payload mapping

```yaml
queries:
  city_count: "SELECT COUNT(*) AS city_count FROM city"

expected:
  city_count_ordered:
    root: csv
    file: world/city_count_expected.csv
  city_count_mismatch:
    root: csv
    file: world/city_count_mismatch.csv
```

