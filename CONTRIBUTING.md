# Contributing to QueryFilterJPA

Thanks for taking the time to contribute! This document covers what you need to know to
work on this codebase productively. For a deeper explanation of how the library is built
internally, see [ARCHITECTURE.md](ARCHITECTURE.md).

## Prerequisites

- JDK 17+ (CI builds with Temurin 21; the poms target `--release 17`).
- Maven (no wrapper is checked in — use your system `mvn`, tested with 3.8+).
- Docker running locally — the core modules' integration tests spin up a real
  PostgreSQL instance via Testcontainers (`SpringIntegrationTestBase`). Tests will fail
  to start if the Docker daemon isn't reachable.

## Repository layout

This is a multi-module Maven build with **two parallel trees that mirror each other**,
one per supported Spring Boot major version:

- `sb3/` — targets Spring Boot 3.x (`query-filter-jpa-3`, `query-filter-jpa-openapi-3`)
- `sb4/` — targets Spring Boot 4.x (`query-filter-jpa-4`, `query-filter-jpa-openapi-4`)

Both core modules implement the same package (`io.github.acoboh.query.filter.jpa.*`)
with mostly the same class names, adapted to each Spring Boot generation's APIs (Jakarta
`@Nullable`/`@NonNull` vs `org.jspecify` annotations, `JpaSpecificationExecutor`
differences, newer Java syntax used in `sb4`, etc.). `examples/basic-example-sb-3` and
`examples/basic-example-sb-4` are runnable demo apps for each version.

### The dual-tree rule

**When you fix a bug or add a feature that is not specific to one Spring Boot version,
apply the change to both `sb3/.../src/main/java/...` and `sb4/.../src/main/java/...`.**
There is no code-generation or sync tooling between the trees — this is done by hand. A
useful habit before editing:

```bash
diff sb3/query-filter-jpa-3/src/main/java/io/github/acoboh/query/filter/jpa/<path>.java \
     sb4/query-filter-jpa-4/src/main/java/io/github/acoboh/query/filter/jpa/<path>.java
```

so you can see how the file has already diverged between versions before adding more
divergence. The same rule applies to:

- Tests under each module's `src/test/java/...` (often the same scenario is tested
  independently in both trees).
- The i18n exception messages in
  `src/main/resources/queryfilter-messages/messages_{en,es}.properties` — if you add or
  change an exception, keep both language files and both trees in sync.
- The two OpenAPI modules (`query-filter-jpa-openapi-3` / `-openapi-4`), when the change
  affects filter documentation generation.

If a change is genuinely version-specific (e.g. adapting to an API that only changed in
Spring Boot 4), it's fine for it to land in only one tree — just say so in the PR
description.

## Build & test

```bash
# Build everything (both sb3 and sb4 trees)
mvn -f pom.xml install

# Build/test only one Spring Boot version's tree
mvn -f sb4/pom.xml test
mvn -f sb3/pom.xml test

# Run tests for a single module
mvn -f sb4/pom.xml -pl query-filter-jpa-4 test

# Run a single test class / method
mvn -f sb4/pom.xml -pl query-filter-jpa-4 test -Dtest=PostBlogExampleControllerTest
mvn -f sb4/pom.xml -pl query-filter-jpa-4 test -Dtest=PostBlogExampleControllerTest#someMethod
```

Before opening a PR, run the relevant tree(s) end to end (`mvn -f sbX/pom.xml install`)
rather than just `test`, since `install` also runs the Javadoc generation described
below.

### Javadoc must build cleanly

The `maven-javadoc-plugin` runs on `install`/`verify` (`-Xdoclint:none` only suppresses
content-style warnings, not structural errors). A malformed Javadoc comment — a
`{@link}`/`@throws` target that doesn't resolve, unbalanced HTML — can fail a full build
even though the code itself compiles. Public classes, interfaces, enums, annotations,
and their non-override public members are expected to carry a Javadoc comment (see
existing files for the style: a short summary, `@param`/`@return`/`@throws` as needed,
and an `@author Adrián Cobo` tag on types). A method that `@Override`s a documented
interface or abstract method does not need its own comment — Javadoc inherits it.

You can check Javadoc in isolation without a full build:

```bash
mvn -f sb4/pom.xml -pl query-filter-jpa-4 javadoc:javadoc
```

### What CI checks

- `.github/workflows/codeql.yml` builds the project (with checkstyle/spotbugs/PMD/
  license/javadoc/rat/spotless checks all explicitly skipped — none of those are
  currently wired into this build) and runs CodeQL security analysis on every push/PR
  touching `**/*.java` or `pom.xml`.
- `.github/workflows/maven-publish.yml` builds and deploys to Maven Central on GitHub
  release creation — not relevant to day-to-day PRs.
- There is no enforced code formatter in the Maven build. `.idea/` contains IDE-level
  Checkstyle (bundled Sun/Google profiles) and Eclipse-formatter settings, but neither is
  wired into the CI build — match the surrounding code's style by eye.

Don't run `mvn verify`/`deploy` locally unless you intend to sign/publish artifacts —
`verify` triggers GPG signing (`maven-gpg-plugin`), which requires credentials you likely
don't have configured.

## Adding a new filterable annotation or operation

If you're extending the filter DSL itself (a new `@QFxxx` annotation, or a new operation
in `operations/QFOperationEnum` / `QFCollectionOperationEnum` / `QFOperationJsonEnum`),
see [ARCHITECTURE.md](ARCHITECTURE.md) for where each piece of the parsing/predicate
pipeline lives. At minimum you'll likely touch, in **both** trees:

1. The annotation itself (`annotations/`).
2. Its definition-parsing logic (`processor/definitions/`) and/or match type
   (`processor/match/`).
3. The operation enum and its predicate-building logic (`operations/`,
   `operations/resolutors/`).
4. Any new exception type, plus its `messages_en.properties` / `messages_es.properties`
   entries.
5. Tests exercising the new behavior via `MockMvc` (see
   `controllers/PostBlogExampleControllerTest` for the idiomatic pattern).
6. If the change affects generated Swagger parameters, the corresponding
   `query-filter-jpa-openapi-{3,4}` module.

## Supporting a future Spring Boot version

Adding support for a new Spring Boot major version means creating a new `sbN/` tree
following the same shape as `sb3`/`sb4`: a parent pom pinning that version's Spring Boot
BOM, a core module, and an OpenAPI module. Since there's no shared source between trees,
expect to copy the closest existing tree (`sb4/`, being the most recent) as a starting
point and adapt it to the new APIs, rather than writing it from scratch.

## Commit/PR conventions

Recent history (`git log`) mixes direct commits and squash-merged PRs; there's no
enforced commit message format. Keep PRs focused, and mention in the description whether
a change was mirrored across both `sb3` and `sb4`, applied to only one intentionally, or
only touches shared docs/CI.
