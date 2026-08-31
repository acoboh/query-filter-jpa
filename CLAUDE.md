# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

QueryFilterJPA is a Java library that lets Spring Boot apps expose dynamic, URL-based filters
(`?field=eq:value` / `field[eq]=value`) over JPA entities without hand-writing `Specification`s.
It ships as separate artifacts for Spring Boot 3 (`sb3/`) and Spring Boot 4 (`sb4/`), plus an
OpenAPI/Swagger integration module for each.

For a deeper walkthrough of the parsing/predicate pipeline than the summary below, see
[ARCHITECTURE.md](ARCHITECTURE.md). For the contribution workflow (Javadoc expectations,
the dual-tree mirroring rule, running the Testcontainers-based test suite), see
[CONTRIBUTING.md](CONTRIBUTING.md).

## Repository layout

This is a multi-module Maven build with **two parallel trees that mirror each other**:

- `pom.xml` — root aggregator, declares shared plugin/dependency versions, has modules `sb3` and `sb4`.
- `sb3/` (parent artifact `query-filter-jpa-3-parent`, targets Spring Boot 3.x)
  - `sb3/query-filter-jpa-3` — the core library
  - `sb3/query-filter-jpa-openapi-3` — springdoc/OpenAPI integration for the core library
- `sb4/` (parent artifact `query-filter-jpa-4-parent`, targets Spring Boot 4.x)
  - `sb4/query-filter-jpa-4` — the core library
  - `sb4/query-filter-jpa-openapi-4` — springdoc/OpenAPI integration for the core library
- `examples/basic-example-sb-3` and `examples/basic-example-sb-4` — standalone runnable Spring Boot apps demonstrating usage (each has a `compile-native.sh` for GraalVM native builds).
- `doc/` — static assets (README screenshots).

**The `sb3` and `sb4` core modules (and their openapi counterparts) contain near-duplicate source
trees** — the same package (`io.github.acoboh.query.filter.jpa.*`) and mostly the same class names,
reimplemented per Spring version (e.g. Jakarta `@Nullable`/`@NonNull` vs `org.jspecify` annotations,
`JpaSpecificationExecutor` API differences, use of newer Java pattern-matching in sb4). **When fixing
a bug or adding a feature that isn't version-specific, check whether the equivalent file exists under
both `sb3/.../src/main/java/...` and `sb4/.../src/main/java/...` and apply the change to both** unless
the task is explicitly about one Spring Boot version only. There is no automated sync tool — this is
done by hand, so diff the two files before editing to see how they've already diverged.

## Build, test, and lint

Standard Maven, no wrapper is checked in (use a system `mvn`, Java 17+).

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

Notes:
- JaCoCo runs on every `test`/`package` (see `jacoco-initialize` / `jacoco-site` executions in the root `pom.xml`); aggregate coverage XML lands under `target/site/jacoco-aggregate/jacoco.xml` and feeds SonarCloud.
- Javadoc and source jars are attached on `install`/`verify` (see `maven-javadoc-plugin` / `maven-source-plugin` in the root `pom.xml`) — a broken Javadoc comment (mismatched `@throws`/`@link` targets, invalid HTML) can fail a full build even though `-Xdoclint:none` is set for content warnings.
- GPG signing (`maven-gpg-plugin`, phase `verify`) and Sonatype Central publishing are configured for releases; these require credentials and are not part of a normal local dev loop — don't run `verify`/`deploy` unless you intend to sign/publish.
- CI (`.github/workflows/maven-publish.yml`, `.github/workflows/codeql.yml`) builds on GitHub Actions; CodeQL runs security scanning.

## Core architecture (per Spring Boot version tree)

The library works by scanning for filter *definition classes* at startup and turning incoming HTTP
query parameters into JPA `Specification`s at request time.

1. **Definition classes** (`annotations/QFDefinitionClass`) are POJOs annotated with `@QFDefinitionClass(Entity.class)`
   whose fields carry annotations describing filterable attributes:
   - `@QFElement` — a single filterable field, mapped by dotted `value()` path to the entity's attribute graph (supports joins, subqueries, arrays, SpEL security expressions, subclass/discriminator mapping — see the annotation's Javadoc for the full option set).
   - `@QFElements` — repeatable wrapper for multiple `@QFElement`s on one field.
   - `@QFCollectionElement`, `@QFJsonElement`, `@QFDiscriminator`, `@QFDate`, `@QFPredicate`/`@QFPredicates`, `@QFSortable`, `@QFRequired`, `@QFOnFilterPresent`, `@QFBlockParsing` — variants/modifiers for collections, JSON columns (Postgres), inheritance discriminators, date handling, raw predicates, sortability, and validation.
   - All of these live in `processor/definitions/` (parsed into `QFAbstractDefinition` subclasses) and `processor/match/` (the runtime "this param matched this definition" objects, e.g. `QFElementMatch`, `QFCollectionMatch`).

2. **Startup wiring** (`config/QFBeanFactoryPostProcessor`) scans the application context for
   classes annotated `@QFDefinitionClass`, discovering the base packages via `@EnableQueryFilter`,
   falling back to `@ComponentScan`/`@SpringBootApplication` packages. For each one found, it
   registers a `QFProcessor<Def, Entity>` bean (see `processor/QFProcessor`) that parses the
   annotations once and caches the resulting field metadata.

3. **Request-time binding** happens via a Spring MVC `HandlerMethodArgumentResolver`
   (`converters/QueryFilterMethodArgumentResolver`, wired in `config/QFWebMvcConfigurer`), triggered
   by controller parameters annotated `@QFParam` (single wrapped `filter=` query param) or
   `@QFMultiParam` (each filter field is its own top-level query param — note the LHS-brackets syntax
   differs slightly between the two modes, see README). It produces a `processor/QueryFilter<Entity>`,
   which implements `Specification<Entity>` (via `JpaSpecificationExecutor`) and exposes sort/paging
   info.

4. **Predicate building** (`operations/`, `operations/resolutors/`, `predicate/`) turns each parsed
   filter part into a JPA Criteria `Predicate`. `operations/QFOperationEnum` is the source of truth
   for supported operators (`eq`, `ne`, `gt`, `gte`, `lt`, `lte`, `btw`, `like`/`nlike`/`rlike`,
   `starts`/`ends`, `in`/`nin`, `null`, plus Postgres-array-specific ones: `ovlp`/`novlp`,
   `containedBy`/`notContainedBy`) — each enum constant knows which JPA/SQL function resolves it and
   whether it's array-typed.
   - `contributor/QfArraySQLFunction` + `META-INF/services/org.hibernate.boot.model.FunctionContributor`
     register a custom Hibernate SQL function used for the Postgres array operations.
   - `exceptions/` and `exceptions/definition` / `exceptions/language` hold the typed exceptions
     (e.g. field not found, invalid operation) that `advisor/QFExceptionAdvisor` turns into HTTP error
     responses; user-facing messages are internationalized in
     `src/main/resources/queryfilter-messages/messages_{en,es}.properties`.

5. **OpenAPI modules** (`query-filter-jpa-openapi-{3,4}`) hook into springdoc to auto-document the
   generated filter parameters in Swagger UI; they're a separate autoconfiguration
   (`openapi.config.QueryFilterOpenApiAutoconfigurer`) layered on top of the core module and are
   optional at runtime.

Both autoconfiguration classes are registered the Spring Boot 3+ way via
`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` (no
`spring.factories`).

## Tests

Tests live under each module's `src/test/java/io/github/acoboh/query/filter/jpa/...` and are
JUnit 5 + AssertJ + Spring's `SpringJUnitWebConfig`/`MockMvc` for integration-style controller tests
(see `controllers/PostBlogExampleControllerTest` for the idiomatic pattern: a `@SpringJUnitWebConfig`
test bootstraps `SpringIntegrationTestBase.Config`, imports a small ad-hoc `@RestController` inline in
the test, and drives it with `MockMvc`). Domain/filter-definition fixtures used across tests live
under `domain/`, `model/`, and `repositories/` in the test sources; `performance/` holds
performance-oriented tests. Both `sb3` and `sb4` test trees are structured the same way and often test
the same scenarios independently — when adding a test for a version-agnostic behavior, check whether
it should be mirrored in the other tree too.
