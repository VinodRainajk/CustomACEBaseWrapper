# Declarative Steps Architecture (Design)

Status: **engine implemented in `com.qa.framework.declarative`.** Template examples live in
`ACEBaseCustomTemplate` (`bundles/google-search.feature` plus the two Google feature files).

## Agreed rules (v1)

- `@Bundle` on the Feature; `@bundle:name` on each bundle scenario (unique, case-insensitive).
- `# BA:` declares a sentence. Scope is until the next `# BA:` or the end of that scenario.
- Every atomic step belongs to a marker. Two markers in a row is a startup error.
- Arguments are `<0>`, `<1>`. The sentence *is* the Cucumber Expression.
- `@declarative:name` on the BA scenario names the bundle it uses. One bundle, many scenarios.
  Several `@declarative:` tags when a scenario draws on several bundles.
- Ticket tags are metadata only. No scoping by tag. Distinct sentences for distinct meanings.
- Bundles live under `classpath:bundles/`. Runners are not modified. The layer is silent with no bundles.
- Recipe lines resolve only against Java atomic steps, never against other `# BA:` sentences.

## Purpose

Give business analysts and customers a Gherkin vocabulary they can read, without adding a second
implementation layer underneath it.

A customer does not care that searching Google is three UI actions, or that verifying a payment
crosses an API and three tables. They care that the business outcome happened.

```
Customer / BA reads:   When the user searches Google for "vinod"

Under the hood runs:   I launch the url "https://www.google.com"
                       I enter "vinod" in the text box "q"
                       I click the button "Google Search"
```

The declarative sentence is a *proxy*. No Java exists for it. Executing it runs atomic step
definitions that already exist.

### Goals

- Adding a declarative step requires **writing Gherkin only** — no Java class, no stub method, no
  empty body per phrase.
- The atomic step definitions in this wrapper remain the single implementation, untouched.
- A declarative sentence is **independent of the size and shape of its recipe**: two atomic steps or
  thirteen, one domain or three, and the BA's feature file never changes.
- One declarative sentence may span UI, API and DB atomic steps.
- The declarative sentence appears in the Extent report, with the atomic steps beneath it.
- Anything missing, ambiguous or misbound fails at **startup**, not halfway through a run.

### Non-goals

- Replacing the atomic layer. It stays the reusable engine and remains directly usable.
- Generating Java sources at build time — that is Java per phrase with extra steps.
- Rewriting or pre-expanding feature files. The BA's feature file is what executes.
- **Branching.** Bundles compose atomic steps in sequence and nothing else. No conditionals, no
  loops, no expressions. When a flow needs logic, it becomes a Java atomic step. Without this rule
  the bundle format slowly turns into a poor programming language.

---

## The three layers

The layers live in **different projects**, which is what makes the split work.

| Layer | Lives in | Written in | Example | Audience |
|---|---|---|---|---|
| **Atomic steps** | `CustomACEBaseWrapper` | Java | `I click the button {string}` | Automation engineer |
| **Bundle** | Template project | Gherkin | the three Google actions, named | Automation engineer |
| **Declarative step** | Template project features | Gherkin | `the user searches Google for "vinod"` | BA / customer |

```
┌────────────────────────────────────────────────────────────────────────────┐
│  CustomACEBaseWrapper  (library)                                            │
│                                                                             │
│  Atomic step definitions — Java, generic, reusable anywhere                 │
│    @When("I launch the url {string}")                                       │
│    @When("I enter {string} in the text box {string}")                       │
│    ... plus all db/, api/, payload/ steps                                   │
│                                                                             │
│  Declarative engine — written ONCE, knows no individual bundle              │
└──────────────────────────────────────┬─────────────────────────────────────┘
                                       │  dependency
                                       ▼
┌────────────────────────────────────────────────────────────────────────────┐
│  Template project  (e.g. ACEBaseCustomTemplate)                             │
│                                                                             │
│  src/test/resources/bundles/google.feature        ← engineer writes Gherkin │
│     @GoogleSearch                                                           │
│     Scenario: Google search                                                 │
│       # BA: the user searches Google for {string}                           │
│       When I launch the url "https://www.google.com"                        │
│       And I enter "<0>" in the text box "q"                                 │
│       And I click the button "Google Search"                                │
│                                                                             │
│  src/test/resources/features/search.feature       ← BA writes Gherkin       │
│     Scenario: Customer finds a profile                                      │
│       When the user searches Google for "vinod"                             │
└────────────────────────────────────────────────────────────────────────────┘
```

Nothing in the middle column is Java. The engineer composes atomic steps into a **bundle**; a
`# BA:` marker inside it declares the sentence the BA writes.

### Terminology

- **Atomic step** — one Java step definition in the wrapper. The primitive.
- **Bundle** — a Gherkin file in the template composing atomic steps. What you called
  "the imperative one".
- **Segment** — one `# BA:` marker plus the atomic steps beneath it. Binds one sentence to one recipe.
- **Declarative step** — the BA-facing sentence. Proxies to a segment. No Java.

---

## Why the obvious bindings do not work

**Tags on individual steps.** Gherkin permits tags on `Feature`, `Scenario`, `Scenario Outline` and
`Examples` only — never on a `Given`/`When`/`Then` line. So "tag on the declarative step equals tag
on the imperative step" cannot be expressed at step level. Smuggling the tag into the step text
leaks implementation noise into the sentence the customer reads.

**A catch-all step definition.** A single `@When("^(.*)$")` proxy also matches every atomic step, so
Cucumber raises `AmbiguousStepDefinitionsException` across the suite. Avoiding that needs a phrasing
convention like `When business rule "..."`, which puts quotes and keywords back into the sentence.

**Binding by Jira number.** Tempting for traceability, but it makes the BA's text decorative rather
than load-bearing. With sentence binding, a reworded line fails as undefined, which guarantees the
report's words describe the automation that ran; with Jira binding the two drift apart silently. It
also forces order-based mapping of N lines to N segments, so inserting one BA line silently shifts
every binding below it and still passes. And a Jira number is not a stable identity for a behaviour:
one ticket spawns many scenarios, and one scenario is touched by many tickets. Jira belongs in this
design as **metadata**, not as a key — see *Binding and traceability*.

**Build-time feature expansion.** Workable, but then the report shows atomic steps and the
declarative sentence survives only as a scenario name, defeating the reporting requirement.

---

## Chosen mechanism: a second Cucumber backend

Cucumber discovers step-definition providers through `ServiceLoader`. `cucumber-java` is one such
provider; the wrapper registers a second one. Ours scans the classpath for bundle files at startup
and registers a synthetic step definition per segment. To Cucumber these are ordinary steps — they
match, report and fail normally — but no Java exists per phrase.

Because it is a classpath scan, the engine ships in the wrapper while the bundles it reads live in
the template.

All four interfaces are `@API(status = STABLE)` in Cucumber 7.15.0, verified against the `v7.15.0`
tag of `cucumber-jvm`:

```java
public interface BackendProviderService {
    Backend create(Lookup lookup, Container container, Supplier<ClassLoader> classLoader);
}

public interface Backend {
    void loadGlue(Glue glue, List<URI> gluePaths);
    void buildWorld();
    void disposeWorld();
    Snippet getSnippet();
}

public interface Glue {
    void addStepDefinition(StepDefinition stepDefinition);
    // ... hooks, parameter types, data table types
}

public interface StepDefinition extends Located {
    void execute(Object[] args) throws CucumberBackendException, CucumberInvocationTargetException;
    List<ParameterInfo> parameterInfos();
    String getPattern();
}
```

Registration is additive, so declarative and atomic steps coexist in one scenario.

---

## The bundle format (v1)

**Location:** `src/test/resources/bundles/*.feature` in the template. Deliberately *not* under
`features/`, so no runner can execute it — `DBTestRunner` uses `@SelectClasspathResource("features")`
and `UIAPITestNGRunner` uses `features = "classpath:features"`. No tag-filter changes needed.

**The entire format is one marker and one rule.** `# BA:` declares a sentence; the atomic steps
beneath it, up to the next marker, are its recipe. Every atomic step belongs to exactly one segment,
and every segment owns at least one atomic step.

```gherkin
@Bundle
Feature: Bundles - Payment

  @PaymentSettlement
  Scenario: Payment settlement
    # BA: a payment of {int} is/was settled for customer {string}
    Given variable "amount" is "<0>"
    And variable "customer" is "<1>"
    When I send a POST request to path from feature payload "paths.payments" with body from feature payload "bodies.create_payment"
    Then the response status code should be 201
    And I store response "$.paymentRef" as variable "paymentRef"
    When I execute the query "SELECT status FROM payments WHERE ref = '$var:paymentRef'"
    Then the query should return 1 row(s)
    And the first row should contain column "status" with value "PROCESSED"

    # BA: the wire/SWIFT information is sent to the recipient
    When I execute the query "SELECT status FROM swift_messages WHERE payment_ref = '$var:paymentRef'"
    Then the query should return 1 row(s)
    And the first row should contain column "status" with value "SENT"

    # BA: the payment is credited to customer
    When I execute the query "SELECT amount FROM ledger WHERE payment_ref = '$var:paymentRef' AND direction = 'CREDIT'"
    Then the query should return 1 row(s)
```

The BA then writes as many or as few of those lines as reads naturally:

```gherkin
@DB @API @all
Feature: Payment settlement

  Scenario: A customer payment settles end to end
    Then a payment of 500 is settled for customer "jsmith"
    And the wire information is sent to the recipient
    And the payment is credited to customer
```

Comment markers are safe here because **bundles are parsed by our loader and never executed by
Cucumber**. The objection to comment markers applies only to the BA's feature file, whose pickles
drop comments.

### Arguments

Recipe lines reference what the sentence captured positionally: `<0>`, `<1>`. Angle brackets, not
braces, because `{...}` is already meaningful in your steps — `FeaturePayloadLoader.resolveBracedPayloadOrLiteral`
treats `"{queries.count_cities}"` as a payload lookup — and because `<0>` will look familiar to
anyone who has written a Scenario Outline.

### Two authoring shapes, one result

A sentence may come from a `# BA:` marker inside a scenario, or from a scenario with no markers at
all (its name becomes the sentence). Both normalise to the same thing, so Cucumber cannot tell which
produced a given step:

```
sections in one scenario  ─┐
                           ├─►  N × (sentence + recipe)  ─►  glue.addStepDefinition
scenarios without markers ─┘
```

| | Sections (`# BA:`) | Separate scenarios |
|---|---|---|
| Reads as | one business process, top to bottom | independent vocabulary entries |
| Per-sentence tags | no — all share the scenario's tags | yes |
| Best for | steps always used together in a fixed order | phrases reused across many flows |

The tag row is the only hard difference, and it matters only once per-sentence tagging is needed.
Both shapes can live in one file.

### Parsing rules

| Situation | Behaviour |
|---|---|
| No `# BA:` in the scenario | One segment; sentence taken from the scenario name |
| `# BA:` followed by atomic steps | Normal segment |
| Two `# BA:` on one physical line | **Startup error** — a single Gherkin comment, unparseable as two |
| Two `# BA:` on consecutive lines | **Startup error** — the first sentence owns no steps |
| Atomic steps before the first `# BA:` | **Startup error** — those steps belong to no sentence |
| Atomic steps after a segment, no new marker | Extend that segment |

The consecutive-marker case gets an error rather than a default because both readings produce a false
green: either the first sentence passes having run nothing, or the second passes by running the
first's recipe.

```
Bundle error: bundles/payment-bundles.feature:15
  Two consecutive '# BA:' markers with no steps between them.

    line 15  # BA: the wire information is sent to the recipient
    line 16  # BA: the payment is credited to customer

  'the wire information is sent to the recipient' has no atomic steps.
  Add the steps that verify it, or delete the marker.
```

### Deliberately excluded from v1

Each of these was considered and has a cheaper answer that already exists:

| Rejected | What it was for | Use instead |
|---|---|---|
| `# BA-note:` | a narrative BA line with no automation | Gherkin's free-text description under `Scenario:` — reads the same, produces no fake pass |
| `# BA-alias:` | two phrasings for one check | Cucumber Expression alternation and optional text: `is/was settled`, `entry/entries`, `item(s)` |
| `# requires:` | friendlier error when prior state is missing | The existing message: `Variable not set: $var:paymentRef. Use: Given variable "paymentRef" is "<value>"` |
| Nesting | a sentence whose recipe is other sentences | Defer until a real flow needs it |
| Tag-selected variants | `@web` / `@mobile` recipes for one sentence | Defer; revisit with a second platform |

Narrative deserves the clearest statement: a report row that verified nothing is worse than no row.
Prose belongs in the scenario description, where it informs without producing a tick.

---

## How matching works

There is no separate lookup step. **The sentence is the step definition pattern.**

1. At startup, each segment registers a `StepDefinition` whose `getPattern()` returns the sentence
   verbatim and whose `parameterInfos()` reports its captured types.
2. `CoreStepDefinition` builds a `StepExpression` from that pattern plus those types.
   `ExpressionFactory` treats a pattern starting with `^`, ending with `$`, or wrapped in `/.../` as
   a regex, and **everything else as a Cucumber Expression** — so plain sentences behave exactly like
   annotation values, and regex remains available.
3. At runtime the step text arrives with the keyword stripped, every registered definition is tried,
   and exactly one must match.
4. Captured arguments arrive as `Object[]` and substitute into `<0>`, `<1>`.

| Stage | Value |
|---|---|
| Step text after keyword strip | `customer "jsmith" exists in the system` |
| Matched pattern | `customer {string} exists in the system` |
| Captured args | `["jsmith"]` |
| `<0>` resolves to | `jsmith` |

Consequences worth knowing:

- **Keywords are irrelevant to matching.** A sentence can be used as `Given`, `When`, `Then` or
  `And`, whatever reads best, regardless of the keywords in the recipe.
- **Scenario Outline works unchanged**, because matching happens against substituted text.
- **Alternation and optional text work in sentences**, at word level: `wire/SWIFT`, `is/was`, `(s)`.
  `wire information/SWIFT message` does **not** work — `/` swaps only the adjacent words.
- **Data tables need declaring.** `StepExpressionFactory` appends a table or doc string as the last
  argument and takes its conversion type from the *last* entry of `parameterInfos()`, throwing
  `step definition at %s does not take any parameters` when that list is empty. A segment meant to
  receive a table must therefore contribute a trailing `ParameterInfo` of type `DataTable`; it cannot
  be inferred from the sentence.
- **`Backend.getSnippet()` is ours**, so an undefined declarative step can print a ready-to-paste
  *bundle skeleton* instead of a Java stub. The `cucumber-java` snippet still appears alongside it.

---

## State and lifecycle

**A bundle's `Scenario:` line has no runtime existence.** Hooks never fire for it, nothing resets at
its boundaries. Context lifetime is defined by the **BA's** scenario:

```13:23:src/main/java/com/qa/framework/payload/FeaturePayloadHooks.java
    @Before(order = 0)
    public void captureFeatureName(Scenario scenario) {
        PayloadStepContext.clearPendingBody();
        PayloadStepContext.clearPendingSqlStatement();
        PayloadStepContext.clearPendingPreparedStatement();
        ScenarioVariableStore.clear();
        if (scenario.getUri() != null) {
            PayloadRegistry.setActiveFeature(
                    DatabaseConfigLoader.extractFeatureName(scenario.getUri().toString()));
        }
    }
```

Four consequences:

**Segments share state automatically.** All segments invoked within one BA scenario run between one
`@Before` and one `@After`, so `$var:paymentRef` set by the first is visible to the rest, the
connection stays open, and `DatabaseStepContext` carries results across. Identical whether the
segments came from one bundle scenario or several — sharing follows the BA's scenario, not the
bundle's shape.

**Splitting across BA scenarios resets everything.** A second scenario gets a fresh `@Before`, so
`ScenarioVariableStore.clear()` has wiped the reference and `DatabaseHooks` has closed connections
and reset context. A sentence that depends on earlier state fails with `Variable not set`.

**Payload keys resolve against the BA's feature, not the bundle.** `setActiveFeature` uses
`scenario.getUri()`, so a recipe line referencing `bodies.create_payment` looks in the payload YAML
belonging to the BA's feature file. Bundles have no payload identity, so two BA features reusing one
bundle each need the key.

**Carried-over state can create a false pass.** Because `DatabaseStepContext` survives across
segments, a segment that asserts without acting first asserts on stale results:

```gherkin
    # BA: the payment is credited to customer
    Then the query should return 1 row(s)     # no query of its own — passes on stale results
```

Every segment's recipe must be self-contained: act, then assert. The loader can warn when a segment
contains assertions but no action step; it is a heuristic, so warn rather than fail.

Parallelism is unaffected: every context is `ThreadLocal`, and a recipe runs on the same thread as
the declarative step that triggered it.

---

## Binding and traceability

Sentence binding is what forces the BA's words and the automation to stay in agreement. Jira numbers
and tags then do the jobs they are actually good at, without carrying the binding:

```gherkin
@DB @API @all @JIRA-1234 @JIRA-5678
Feature: Payment settlement

  Scenario: A customer payment settles end to end
    Then a payment of 500 is settled for customer "jsmith"
```

```gherkin
  @PaymentSettlement
  Scenario: Payment settlement
    # BA: a payment of {int} is/was settled for customer {string}
    # jira: PAY-1234
```

That supports filtering a run by ticket, reporting which tickets have automation, and tracing a
failure to a requirement — while the sentence keeps doing the one job only it can do.

---

## Sentences that have no automation yet

BAs should be able to write vocabulary before engineers build it. An unmapped sentence is undefined
to Cucumber, and since Cucumber 7 is strict by default, undefined fails. Rather than loosening that,
declare the gap:

```gherkin
@notAutomated @JIRA-5678
Scenario: Refunds settle within one business day
  Then a refund of 500 is settled for customer "jsmith"
  And the refund is credited to customer
```

Runners exclude it — `EnvTagFilter.buildFilter` already composes tag expressions, so appending
`and not @notAutomated` is one line. The scenario stays specified, visible and traceable while
executing nothing.

A Maven goal then turns silence into a queue:

```
Declarative coverage
  42 sentences registered from 9 bundle files
  2 scenarios tagged @notAutomated
     features/refunds.feature:4   a refund of {int} is settled for customer {string}   — no bundle
     features/refunds.feature:5   the refund is credited to customer                   — no bundle
```

---

## Cross-domain execution: a gap to close first

A recipe mixing UI, API and DB can only run if the executing runner has **all** the relevant glue.
Today none does:

| Runner | Engine | Tag filter | Glue | Can run a UI+API+DB recipe? |
|---|---|---|---|---|
| `DBTestRunner` | JUnit | `@DB` | db, payload | No — no UI or API glue |
| `UIAPITestNGRunner` | TestNG | ace-base | acebase, ui, api, payload | No — no DB glue |
| `BaseTestRunner` | JUnit | none | db, ui, api, payload | No — missing `com.acebase.glue`, so no driver lifecycle or Extent plugin |

So cross-domain scenarios need one more runner: TestNG, for the ace-base driver lifecycle and
`com.acebase.runner.CucumberPlugin` reporting, with glue `com.acebase.glue` plus db, ui, api and
payload. Concretely `UIAPITestNGRunner` with the db package added.

Hook details checked in the current code: `DatabaseHooks` and `APIHooks` use untagged `@Before`, so
they fire for any scenario once their glue is loaded. `UIStepDefinitions` uses `@Before("@UI")`, so a
mixed scenario still needs `@UI` to get a browser. Ace-base's own hook tags are unverified.

---

## Components

New package `com.qa.framework.declarative` in the wrapper.

| Class | Responsibility |
|---|---|
| `DeclarativeBackendProvider` | `ServiceLoader` entry point; implements `BackendProviderService` |
| `DeclarativeBackend` | On `loadGlue`, registers one `DeclarativeStepDefinition` per segment |
| `BundleLoader` | Finds and parses bundle features from the classpath; applies the parsing rules |
| `BundleSegment` | Model: sentence expression, tags, ordered recipe lines, source file and line |
| `DeclarativeStepDefinition` | Synthetic `StepDefinition`; substitutes args and delegates |
| `AtomicStepRegistry` | Scans glue packages for `@Given`/`@When`/`@Then`; resolves a sentence to a method plus args |
| `AtomicStepInvoker` | Invokes the resolved method, reports each line, wraps failures |
| `DeclarativeHooks` | `@Before` hook capturing the `Scenario` into a `ThreadLocal` for reporting |
| `DeclarativeConfig` | Bundle paths, glue packages, strictness, reporting |

Service registration:

```
src/main/resources/META-INF/services/io.cucumber.core.backend.BackendProviderService
  → com.qa.framework.declarative.DeclarativeBackendProvider
```

### Sketch: the synthetic step definition

```java
final class DeclarativeStepDefinition implements StepDefinition {

    private final BundleSegment segment;
    private final AtomicStepInvoker invoker;

    @Override
    public void execute(Object[] args) {
        for (RecipeLine line : segment.recipe()) {
            invoker.invoke(line.substitute(args));   // throws on failure
        }
    }

    @Override
    public List<ParameterInfo> parameterInfos() {
        return segment.parameterInfos();             // types for expression matching
    }

    @Override
    public String getPattern() {
        return segment.sentence();                   // "the user searches Google for {string}"
    }

    @Override
    public String getLocation() {
        return segment.sourceFile() + ":" + segment.line();   // the bundle, for stack traces
    }

    @Override
    public boolean isDefinedAt(StackTraceElement element) {
        return false;                                // no Java frame corresponds to a segment
    }
}
```

`getLocation()` pointing at the bundle is what makes a failure point at Gherkin someone wrote rather
than into framework internals.

### Sketch: resolving an atomic sentence

`AtomicStepRegistry` is built once per JVM:

1. Scan the configured glue packages for methods annotated with `io.cucumber.java.en.*`.
2. Build a matcher per annotation value using `io.cucumber.cucumberexpressions.ExpressionFactory`
   with a `ParameterTypeRegistry(Locale.ENGLISH)` — the same machinery Cucumber uses, so `{string}`,
   `{int}` and optional text like `row(s)` behave identically.
3. Match, convert captured arguments to the method's parameter types, return method plus args.

Two facts about the current code keep this simple:

- **No custom parameter types.** A scan for `@ParameterType`, `@DataTableType` and `@DocStringType`
  found none, so only built-in types and `List<String>` tables need handling.
- **State is `ThreadLocal`, not injected.** Contexts come from statics such as
  `DatabaseStepContext.getInstance()`, so a reflectively created instance shares state with normally
  invoked steps. No object factory to reproduce.

---

## Execution flow

```
┌──────────────────────────────────────────────────────────────────────────────┐
│ Startup (once)                                                                │
│                                                                               │
│  ServiceLoader ──► DeclarativeBackendProvider ──► DeclarativeBackend           │
│                                        │                                      │
│                          loadGlue(glue, gluePaths)                            │
│                                        │                                      │
│  BundleLoader        ── scans ──► classpath:bundles/*.feature  (template)     │
│  AtomicStepRegistry  ── scans ──► glue packages                (wrapper)      │
│                                        │                                      │
│  validation: marker rules, unknown recipe line, duplicate sentence,           │
│              collision with an atomic step, glue not loaded ──► FAIL           │
│                                        │                                      │
│  glue.addStepDefinition(DeclarativeStepDefinition) × N segments                │
└──────────────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────────────┐
│ Per scenario step                                                             │
│                                                                               │
│  Then a payment of 500 is settled for customer "jsmith"                        │
│        │  matched by Cucumber like any other step                             │
│        ▼                                                                      │
│  DeclarativeStepDefinition.execute([500, "jsmith"])                            │
│        │  substitute <0>, <1> into each recipe line                           │
│        ▼                                                                      │
│  AtomicStepInvoker.invoke("I execute the query \"SELECT ... 'PAY-00123'\"")    │
│        ├──► AtomicStepRegistry.resolve(...) ──► Method + args                  │
│        ├──► method.invoke(instance, args)                                     │
│        └──► scenario.log the resolved line and its outcome                     │
│                                                                               │
│  any failure ──► throw, naming the recipe line and its bundle source           │
└──────────────────────────────────────────────────────────────────────────────┘
```

---

## Reporting

The declarative step is a genuine Cucumber step, so `com.acebase.runner.CucumberPlugin` renders it
unchanged — the BA sentence *is* the step. `DeclarativeHooks` captures the `Scenario`, and
`AtomicStepInvoker` logs one line per recipe step with its outcome, so detail nests beneath.

```
Scenario: A customer payment settles end to end
  ✓ Then a payment of 500 is settled for customer "jsmith"
        ✓ I send a POST request to path from feature payload "paths.payments" ...
        ✓ the response status code should be 201
        ✓ I execute the query "SELECT status FROM payments WHERE ref = 'PAY-00123'"
        ✓ the first row should contain column "status" with value "PROCESSED"
  ✗ And the wire information is sent to the recipient
        ✗ the first row should contain column "status" with value "SENT"
          Expected "SENT" but was "QUEUED"
  – And the payment is credited to customer          (skipped)
```

Logged lines show the **resolved** text, with `$var:` and `<0>` already substituted, since logging
happens after resolution.

Separate sentences localise failures, which is the main argument for segmenting a flow rather than
collapsing it into one sentence: the wire row is red and nobody counts recipe lines.

Failure text must name three things — the sentence, the recipe line, and the bundle file and line:

```
Then the wire information is sent to the recipient
  failed at recipe line 3: the first row should contain column "status" with value "SENT"
  bundle: bundles/payment-bundles.feature:15
  Expected "SENT" but was "QUEUED"
```

Open item: if `com.acebase.runner.CucumberPlugin` exposes an Extent node API, recipe steps should
become true child nodes rather than log lines. The `ace-base` jar is not readable from this
workspace.

---

## Startup validation

Everything below fails the run immediately, quoting the bundle file and line.

| Condition | Why it must fail early |
|---|---|
| Marker rule violated (see parsing rules) | Ambiguous authoring, and both readings can produce a false green |
| Recipe line matches no atomic step | Otherwise a confusing mid-run error |
| Recipe line matches more than one atomic step | Ambiguity is an authoring bug |
| Two segments define the same sentence | Non-deterministic binding |
| A sentence also matches an existing atomic step | Cucumber would throw `AmbiguousStepDefinitionsException` mid-run |
| Recipe references `<2>` when the sentence captures two arguments | Off-by-one, cheap to catch |
| Recipe line belongs to glue the current runner has not loaded | The cross-domain trap; see the runner table |

Warnings, not failures: a segment containing assertions but no action step (possible stale-state
false pass).

---

## Configuration

| Property | Default | Meaning |
|---|---|---|
| `declarative.bundle.paths` | `classpath:bundles` | Where bundle files live |
| `declarative.glue.packages` | the four wrapper glue packages | Where to scan for atomic steps |
| `declarative.strict` | `true` | Fail at startup on any validation error |
| `declarative.report.substeps` | `true` | Log recipe lines under the declarative step |

`declarative.strict=false` is for migration only: skip the offending bundle with a warning rather
than degrade to a runtime surprise.

---

## Phase 1 spike (DB)

DB is the target because `stepdefinitions/db` is fully implemented and runnable, unlike
`UIStepDefinitions`, whose bodies are still `TODO`. Single domain also avoids the runner gap while
the mechanism is proven.

Scope: one declarative step driving real atomic steps end to end. Hardcode the segment in Java if the
loader is not ready — the point is to validate the backend and invoker, not the file format.

Bundle — `src/test/resources/bundles/db-bundles.feature`:

```gherkin
@Bundle
Feature: Bundles - Database

  @UserCount
  Scenario: User counts
    # BA: the users table contains {int} records
    When I execute the query "SELECT * FROM users"
    Then the query should return <0> row(s)
```

Feature — run through `DBTestRunner`:

```gherkin
@DB @all
Scenario: User table is populated
  Given the database connection is established
  Then the users table contains 3 records
```

Success criteria: the scenario passes; changing the expected count fails the declarative step with
the recipe line and bundle location named; `target/cucumber-reports/db-tests.html` shows the
declarative sentence as the step.

Two decisions the spike should settle:

1. **Bundle parsing.** `io.cucumber.gherkin.GherkinParser` gives tables, doc strings, tags and
   comment locations for free but has a message-oriented API. A line parser over this constrained
   subset is more predictable. Try the real parser first; fall back if it fights us.
2. **Instance lifecycle.** Whether a step-definition instance can be created per invocation (expected
   to be fine given `ThreadLocal` contexts) or must be cached per thread.

---

## Risks and limitations

**Format creep is the main long-term risk.** Every marker added is a parser branch, a validation
rule, an error message and a paragraph of documentation that exists nowhere else in the world. The
well-known failure mode of keyword-driven layers over Cucumber is a DSL that slowly reimplements the
host language badly. v1 is one marker and one rule on purpose; additions should require a real
scenario that cannot be expressed otherwise.

**False greens are the deepest hazard.** A declarative sentence hides its plumbing by design, which
is exactly why an empty or stale-state recipe is so dangerous — the reader cannot see that nothing
was verified. Bundles need reviewing as seriously as code, and no feature should make a passing row
easier to produce without a real assertion.

**IntelliJ marks declarative steps undefined.** The IDE inspector does not run our backend, so
runtime-registered sentences are invisible to it. Execution is unaffected, but a BA facing a wall of
red squiggles loses confidence in the tool. Mitigations worth building rather than deferring: a
generated vocabulary listing of every available sentence, a Maven goal that validates a feature file
against the registry and suggests the nearest match on a typo, and the bundle-shaped snippet from
`getSnippet()`.

**Debuggability drops a notch.** There is no Java frame to breakpoint for a declarative step, and
stack traces route through the invoker. `getLocation()` and the failure text carry the load instead.

**Pin the Cucumber version.** `7.15.0` and its `STABLE` SPI are fine, but a custom backend is a
framework-level integration, and details like `StepExpressionFactory` taking the table type from the
last `ParameterInfo` are the sort of thing that shifts. Upgrades should be deliberate, with the spike
scenario as the canary.

**Reflective invocation bypasses `@BeforeStep`/`@AfterStep`** for recipe lines. Cucumber fires those
around the declarative step, not around each atomic line. Screenshot-per-step and similar need the
invoker to trigger them explicitly.

**Bundle drift is cross-repo.** Renaming an atomic step in the wrapper breaks bundles in templates,
so the break surfaces downstream of the change. A per-template CI job that loads bundles without
running tests is a requirement, not a nice-to-have.

**A cheaper alternative exists.** A single generic proxy — `When business rule "the payment is
settled"` — needs no framework internals, roughly fifty lines, no version coupling. It costs quotes
and a prefix in the BA's sentence. The backend is recommended because BA readability was the point,
but if the vocabulary stays small the cheap version would have been the right call.

---

## Delivery phases

| Phase | Content |
|---|---|
| 1 | Backend, registry, invoker, one DB segment end to end |
| 2 | Bundle loader, marker parsing rules, arguments, data tables, full startup validation |
| 3 | Reporting polish (Extent nodes if available), vocabulary listing, feature-validation goal, coverage report, unit tests |
| 4 | Cross-domain runner (`com.acebase.glue` + db + ui + api + payload) and a UI+API+DB bundle |

Deferred until a real need appears: nesting, tag-selected variants, aliases, dependency declarations.

---

## Open questions

1. Does `com.acebase.runner.CucumberPlugin` expose an Extent node API for nesting recipe steps, and
   are ace-base's driver hooks tagged `@UI`? Both need the `ace-base` source.
2. Should the wrapper ship a small set of shared bundles for vocabulary common to every project, with
   templates adding their own? The classpath scan supports both; the decision affects packaging.
3. Should the report show recipe lines always, or only on failure?
