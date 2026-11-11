# Changelog

All notable changes to QueryFilterJPA are documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and the project
follows [Semantic Versioning](https://semver.org/).

## [2.1.0] - Unreleased

> **This is a security release. Upgrading is strongly recommended for all users of both the
> Spring Boot 3 (`query-filter-jpa-3`) and Spring Boot 4 (`query-filter-jpa-4`) artifacts.**
>
> ⚠️ **2.1.0 is the last release that ships Spring Boot 3 artifacts.** `query-filter-jpa-3` and
> `query-filter-jpa-openapi-3` will not receive further updates, including security fixes.
> Please plan your migration to Spring Boot 4.

Unless stated otherwise, every change applies to both the Spring Boot 3 and Spring Boot 4 artifacts.

### ⚠️ Upgrade notes (behavior changes)

Read this section before upgrading: some changes may require configuration or code updates.

* **SpEL fields can no longer be set from the request.**
  Fields with `@QFElement(isSpPELExpression = true)` are now always treated as blocked
  (`@QFBlockParsing`) for HTTP input. A request that sends a value for them is rejected with
  `QFBlockException` (HTTP 400). SpEL expressions can only come from `defaultValues` or from the
  programmatic API (`addNewField` / `overrideField`). A warning is logged at startup for SpEL fields
  that are not explicitly annotated with `@QFBlockParsing`.
  *Never pass user input to SpEL fields through the programmatic API.*
* **Restricted SpEL evaluation context.**
  When the evaluation context is provided by a Spring Security expression handler, type references
  (`T(...)`), constructors and access to `Class`, `ClassLoader` and reflection members are disabled,
  and expressions are read-only. If your `defaultValues` expressions need any of these, set
  `query-filter.spel.restricted=false` (not recommended).
* **Startup validation of filter endpoints.**
  The application now **fails to start** if a controller parameter annotated with `@QFParam` or
  `@QFMultiParam` references a filter definition without a registered `QFProcessor` (for example,
  a definition class outside the `@EnableQueryFilter` base packages). Previously, this was only
  detected at request time. It can be disabled with `query-filter.validate-endpoints=false`.
* **`@QFMultiParam` never resolves to `null`.**
  If the processor or the entity type cannot be resolved, an `IllegalStateException` is thrown
  instead of injecting a `null` filter into the controller.
* **`@QFPredicate` expressions are validated at startup.**
  Expressions with unknown fields, unbalanced parenthesis, empty groups, dangling operators or mixed
  `AND`/`OR` on the same level now make the application fail to start with a descriptive error.
  Expressions that were previously accepted but evaluated incorrectly (for example, an expression
  fully wrapped in parenthesis such as `(a OR b)`, or a single field such as `a`) now work as expected.
* **Default input limits.**
  The input parsed from the request is now limited (see *New configuration properties*). Requests
  that exceed a limit are rejected with the new `QFLimitExceededException` (HTTP 400). Increase the
  limits if your clients send larger filters (e.g. `in` lists with more than 500 values). The
  programmatic API of `QueryFilter` is not limited.
* **Parse error details.**
  `QFParseException.getField()` now reports the offending part of the filter without its leading
  `&` separator (e.g. `garbage` instead of `&garbage`).

### 🔐 Security

* Hardened the evaluation of SpEL expressions: SpEL fields are no longer reachable from the request
  and the evaluation context provided by Spring Security is restricted.
* Fixed an issue where an endpoint using `@QFMultiParam` could be executed **without its filter**
  when the filter processor was missing or misconfigured. Misconfigurations now fail at startup.
* Fixed a concurrency issue in filters with SpEL default values: filter state was shared between
  concurrent requests, which could lead to a request being evaluated with values of another request.
  Every `QueryFilter` now uses its own instances.
* Fixed an issue where some `@QFPredicate` expressions could result in **no filtering at all**.
  Predicates are now resolved fail-closed and validated at startup.
* Replaced the regular-expression based filter parser with a linear tokenizer. Large inputs could
  exhaust the stack of the request thread. The accepted syntax is unchanged.
* Added configurable limits for the length of the input, the number of filters, values per field
  and sort fields.

### 🐞 Bug fixes

* Fixed a memory leak in filters with SpEL values: internal state grew on every execution.
* Fixed `@QFPredicate` expressions with a single field (e.g. `"author"`), which failed at runtime.
* Fixed custom predicates being dropped when a group resolved to a single simple predicate
  (comparison, `in`, `like`…).
* Fixed `QueryFilter.deleteSortBy()` throwing `UnsupportedOperationException` when the default sort
  was active. The default sort list is now copied per `QueryFilter` instance, so changes no longer
  affect other instances.
* `like`, `nlike`, `starts` and `ends` now escape the `%`, `_` and `\` wildcards of the user value, so
  the value is matched literally (e.g. `title=like:100%`). `rlike` keeps passing the raw `LIKE`
  pattern, and its patterns can now use `\` as escape character.
* Malformed filter strings (unparsed or trailing content) now throw `QFParseException` instead of
  being silently ignored.
* Improved parsing for `@QFMultiParam`, numeric and UUID values: invalid values now throw
  `QFParseException`.

### ✨ New configuration properties

| Property | Default | Description |
|---|---|---|
| `query-filter.spel.restricted` | `true` | Restrict the SpEL evaluation context provided by Spring Security |
| `query-filter.validate-endpoints` | `true` | Validate at startup that every `@QFParam` / `@QFMultiParam` has a processor |
| `query-filter.limits.max-input-length` | `8192` | Maximum length of the filter input (single param) or of all keys and values (multi param) |
| `query-filter.limits.max-filters` | `100` | Maximum number of filter parts in one request |
| `query-filter.limits.max-values-per-field` | `500` | Maximum number of values of one filter part (e.g. `in` lists) |
| `query-filter.limits.max-sort-fields` | `10` | Maximum number of sort fields in one request |

### 🆕 Added

* `QFLimitExceededException` (HTTP 400, message code `qf.exceptions.limit`, translated to English
  and Spanish).

### 🗑️ Deprecated

* `QFParamType.getPattern()`: the input is no longer parsed with this regular expression. It will be
  removed in the next major version.
* `StringParseUtils`: no longer used. It will be removed in the next major version.

### 🧪 Tests

* New regression tests for `deleteSortBy()` with default sorting, `LIKE` wildcard escaping, `@QFDate`
  and the OpenAPI modules.

**Full Changelog**: https://github.com/acoboh/query-filter-jpa/compare/v2.0.0...v2.1.0

## [2.0.0] - 2026-03-03

See the [GitHub release](https://github.com/acoboh/query-filter-jpa/releases/tag/v2.0.0).
