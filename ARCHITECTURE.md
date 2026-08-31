# Architecture

This document explains how QueryFilterJPA turns an HTTP query string into a JPA
`Specification`, and where each concern lives in the source tree. It assumes you've
already read the module layout in [CLAUDE.md](CLAUDE.md)/[CONTRIBUTING.md](CONTRIBUTING.md)
— in particular, that `sb3/` and `sb4/` are parallel implementations of the same
architecture, one per Spring Boot major version. Everything below applies to both trees;
package/class names are identical unless noted.

## End-to-end flow

```
 startup:
   @QFDefinitionClass classes  --(scanned by)-->  QFBeanFactoryPostProcessor
                                                        |
                                                        v
                                          registers one QFProcessor<F,E> bean per
                                          definition class (parses annotations once,
                                          caches field metadata)

 per request:
   HTTP query string / params
        |
        v
   QueryFilterMethodArgumentResolver (@QFMultiParam)      <-- MVC HandlerMethodArgumentResolver
   or QFCustomConverter (@QFParam, single "filter=" param) <-- Spring GenericConverter
        |
        v
   QFProcessor.newQueryFilter(...) / newQueryFilterMap(...)
        |
        v
   QueryFilter<E>  (implements Specification<E>, holds parsed QFSpecificationPart list + sort)
        |
        v
   repository.findAll(queryFilter)   // via JpaSpecificationExecutor
        |
        v
   QueryFilter.toPredicate(root, query, cb)  -->  operations/* build jakarta.persistence.criteria.Predicate
        |
        v
   QFExceptionAdvisor (@ControllerAdvice) turns any QueryFilterException into an
   HTTP error response, localized via messages_{en,es}.properties
```

## 1. Definition classes — declaring what's filterable

A **definition class** is a plain POJO annotated `@QFDefinitionClass(Entity.class)`. Its
fields carry annotations from `annotations/` describing how each maps to the entity:

| Annotation | Purpose |
|---|---|
| `@QFElement` / `@QFElements` (repeatable) | A single filterable attribute, addressed by a dotted `value()` path into the entity's attribute graph. Carries most of the knobs: `sortable`, `defaultValues`/`defaultOperation`, `caseSensitive`, `arrayTyped` (Postgres arrays), `isSpPELExpression` (SpEL security expressions), `subClassMapping`/`subClassMappingPath` (JOINED-inheritance discriminator subclasses), `joinTypes`, `allowedOperations`, `order` (resolution order, needed when SpEL fields read other fields' values). |
| `@QFCollectionElement` | Filtering/matching against a `Collection`-typed attribute (uses `operations/QFCollectionOperationEnum`). |
| `@QFJsonElement` | Filtering into a JSON column (Postgres), via `operations/QFOperationJsonEnum`. |
| `@QFDiscriminator` | Filtering by JPA inheritance discriminator value, via `operations/QFOperationDiscriminatorEnum`. |
| `@QFDate` | Marks a field as date/time-typed so incoming strings are parsed with `utils/DateUtils` and validated against expected formats. |
| `@QFPredicate` / `@QFPredicates` | Attach a raw, hand-written predicate contributed by a Spring bean (`predicate/` package) rather than a value comparison. |
| `@QFSortable` | Marks a field as sortable-only (no filtering). |
| `@QFRequired` | Field must be present — independently controllable for the string-filter phase, execution phase, and sort phase (see `QFRequired.onStringFilter()/onExecution()/onSort()`). |
| `@QFOnFilterPresent` | When the annotated field is present in the incoming filter, apply the *default values* of the named other fields even though those fields weren't submitted. |
| `@QFBlockParsing` | Blocks a field from being set from raw user input entirely — typically paired with `isSpPELExpression` so the value can only come from a SpEL security expression, never directly from the query string. |

These are parsed once per definition class into `processor/definitions/` objects, all
extending `QFAbstractDefinition`:

- `QFDefinitionElement` — the common case, for `@QFElement`.
- `QFDefinitionCollection`, `QFDefinitionJson`, `QFDefinitionDiscriminator`,
  `QFDefinitionSortable` — one per specialized annotation above.
- `FilterFieldInfo` — a record holding the shared reflected-field metadata
  (`Field`, filter/entity classes, and the cross-cutting `@QFBlockParsing`/`@QFRequired`/
  `@QFOnFilterPresent` annotations) that every `QFAbstractDefinition` is built from.
- `traits/IDefinitionSortable` — trait interface implemented by definitions that support
  being used in a `sort=` clause.

At **request time**, once a raw filter part (`field`, `operation`, `values`) is matched
against a `QFAbstractDefinition`, it becomes a `processor/match/` object implementing
`QFSpecificationPart` — `QFElementMatch`, `QFCollectionMatch`, `QFJsonElementMatch`,
`QFDiscriminatorMatch`. These are the objects that actually know how to produce a
`Predicate` for a given match, and are what `QueryFilter` accumulates internally.

## 2. Startup wiring — discovering definitions and registering processors

`config/QFBeanFactoryPostProcessor` (a `BeanFactoryPostProcessor`) runs at context
startup:

1. Finds the base package(s) to scan, in priority order: beans annotated
   `@EnableQueryFilter` (its `basePackageClasses`/`basePackages`), falling back to
   `@ComponentScan`, falling back to `@SpringBootApplication`'s package.
2. Scans those packages for classes annotated `@QFDefinitionClass`.
3. For each one, registers a `BeanDefinition` for `processor/QFProcessor<F, E>`
   (generic-typed to the filter class `F` and entity class `E`), so it becomes an
   ordinary autowirable Spring bean.

`QFProcessor` is the parsed, cached representation of one definition class: it builds the
`Map<String, QFAbstractDefinition>` (field name → definition) once, using the JPA
`Metamodel` to validate that annotated paths actually exist on the entity, and exposes
factory methods to build a fresh `QueryFilter` per request (`newQueryFilter`,
`newQueryFilterMap`) without re-parsing annotations each time.

`config/QueryFilterAutoconfigure` (a standard `@AutoConfiguration`, registered the Spring
Boot 3+ way via `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
— no `spring.factories`) wires together: `QFExceptionAdvisor`, `SpelResolverBeanConfig`,
`QFBeanFactoryPostProcessor`, `QFWebMvcConfigurer`, `QueryFilterProperties`,
`ApplicationContextAwareSupport`, `HintsRegistrarDef`.

## 3. Request-time binding — two entry points

Controllers opt into filtering with one of two mutually-exclusive annotations on a
`QueryFilter<Entity>` parameter:

- **`@QFParam(FilterDef.class)`** — a single wrapped query parameter (`filter=...`)
  containing the whole RHS-colon or LHS-brackets expression. Bound via
  `converters/QFCustomConverter`, a Spring `GenericConverter` registered for
  `String -> QueryFilter` conversions (wired by `config/QFWebMvcConfigurer`).
- **`@QFMultiParam(FilterDef.class)`** — each filter field is its own top-level query
  parameter. Bound via `converters/QueryFilterMethodArgumentResolver`, a
  `HandlerMethodArgumentResolver`. Note the LHS-brackets syntax differs subtly between
  the two modes (`field[eq]=value` for `@QFParam` vs. `field=[eq]value` for
  `@QFMultiParam`) to avoid ambiguity when there's no wrapper — see the root README for
  worked examples.

Both paths resolve the right `QFProcessor` bean (keyed by `(filterClass, entityClass)`
pair) and call `newQueryFilter`/`newQueryFilterMap`, producing a `processor/QueryFilter<E>`.

`QueryFilter<E>` implements `Specification<E>` (so it plugs directly into
`JpaSpecificationExecutor.findAll(...)`), and also carries the parsed `sort=` clause and
exposes introspection methods (e.g. `getAllFieldValues()`) used by the OpenAPI module and
by tests.

## 4. Predicate building

When Spring Data invokes `QueryFilter.toPredicate(root, query, criteriaBuilder)`, each
accumulated `QFSpecificationPart` builds its own `Predicate`, delegating to the
`operations/` enums via the `operations/resolutors/` interfaces:

- `QFOperationEnum` (implements `QFPredicateResolutor`) — the source of truth for
  standard scalar operations: `eq`, `ne`, `gt`, `gte`, `lt`, `lte`, `btw`, `like`/`nlike`/
  `rlike`, `starts`/`ends`, `in`/`nin`, `null`, plus Postgres-array-specific operations
  `ovlp`/`novlp` (overlap) and `containedBy`/`notContainedBy`. Each constant knows which
  JPA Criteria call (or custom SQL function, for arrays) resolves it, and whether it's
  valid for array-typed fields.
- `QFCollectionOperationEnum` (implements `QFPredicateCollectionResolutor`) — operations
  for `@QFCollectionElement` fields.
- `QFOperationJsonEnum` (implements `QFPredicateJsonResolutor`) — operations for
  `@QFJsonElement` fields.
- `QFOperationDiscriminatorEnum` (implements `QFPredicateDiscriminatorResolutor`) —
  operations for `@QFDiscriminator` fields.
- `predicate/PredicateOperation` (`AND`/`OR`) and `predicate/PredicateProcessorResolutor`
  — combine raw predicates contributed via `@QFPredicate`/`@QFPredicates`.

Array operations are backed by a real Postgres SQL function:
`contributor/QfArraySQLFunction` + `contributor/FunctionContributorImpl`, registered via
the Hibernate SPI file
`META-INF/services/org.hibernate.boot.model.FunctionContributor`. `contributor/ArrayFunction`
enumerates the supported array SQL functions.

### SpEL security expressions

Fields with `@QFElement(isSpPELExpression = true)` have their value resolved via a SpEL
`ExpressionParser` instead of taken verbatim from the query string — e.g.
`principal?.name` or referencing another field's parsed value (`#otherElement`). This is
implemented in `spel/`: `SpelResolverContext` is the base evaluation context (request/
response-aware), `SpelResolverContextBasic` and `SecuritySpelResolverContext` are
concrete variants, and `SpelResolverBeanConfig` wires the active one as a bean. Combine
with `@QFBlockParsing` to prevent a SpEL-only field from also being settable directly by
the caller.

## 5. Errors and localization

All library-thrown runtime exceptions extend `exceptions/QueryFilterException`
(implements `exceptions/language/ExceptionLanguageResolver`, exposing an HTTP status, a
message code, and message arguments). `advisor/QFExceptionAdvisor` is a
`@ControllerAdvice` (toggleable via the `query-filter.advisor.enabled` property, default
`true`) that catches these and renders a localized JSON error body, resolving message
codes against `queryfilter-messages/messages_{en,es}.properties` via a
`ResourceBundleMessageSource` configured from `properties/AdvisorProperties`.

A second, separate exception hierarchy —
`exceptions/definition/QueryFilterDefinitionException` and its subclasses — covers
**definition-time** errors (invalid annotation combinations on a definition class,
discovered while `QFProcessor` parses it at startup), as opposed to **request-time**
errors in the `exceptions/` root package above.

`properties/QueryFilterProperties` (`@ConfigurationProperties("query-filter")`) is the
single root for all runtime-tunable behavior; today it holds `advisor` (→
`AdvisorProperties`: enable/disable the advisor, message-bundle basename/encoding,
whether to use the message code as a fallback message, whether to extend the error body
with extra detail).

`hints/HintsRegistrarDef` registers GraalVM native-image reflection hints for the
library's own classes (relevant to the `examples/*/compile-native.sh` native builds).

## 6. OpenAPI integration

`query-filter-jpa-openapi-{3,4}` are separate, optional modules/autoconfigurations
(`openapi.config.QueryFilterOpenApiAutoconfigurer`, registered the same
`AutoConfiguration.imports` way) that hook into springdoc to introspect the same
`QFProcessor`/`QFAbstractDefinition` metadata built by the core module and turn it into
documented Swagger UI parameters — one per filterable field, with the allowed operations
and value types reflected in the generated schema. They depend on the core module of the
same Spring Boot version; there's no cross-version dependency.

## Testing architecture

Tests are JUnit 5 + AssertJ, split between:

- **Unit-ish tests** around individual parsing/predicate classes.
- **Integration tests** using `spring/SpringIntegrationTestBase` (per test tree) — a
  hand-assembled Spring context (JPA `EntityManagerFactory`, transaction manager,
  `@EnableJpaRepositories`) pointed at a **real PostgreSQL instance via Testcontainers**
  (`@Container PostgreSQLContainer`), with SQL logging through `datasource-proxy`. This
  is why Docker must be running to execute the test suite (see CONTRIBUTING.md).
  `controllers/` tests then layer `@SpringJUnitWebConfig` + `MockMvc` on top to exercise
  the full HTTP-parameter-to-predicate pipeline through an ad-hoc `@RestController`
  declared inline in the test.
- Fixtures shared across a tree's tests live under `domain/` (filter definitions),
  `model/` (JPA entities), and `repositories/`.
- `performance/` holds tests focused on parsing/predicate-building performance rather
  than correctness.
