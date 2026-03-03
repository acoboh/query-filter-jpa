[![License](https://img.shields.io/github/license/acoboh/query-filter-jpa.svg)](https://raw.githubusercontent.com/acoboh/query-filter-jpa/main/LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.acoboh/query-filter-jpa-4.svg)](https://central.sonatype.com/artifact/io.github.acoboh/query-filter-jpa-4)
[![javadoc](https://javadoc.io/badge2/io.github.acoboh/query-filter-jpa-4/javadoc.svg)](https://javadoc.io/doc/io.github.acoboh/query-filter-jpa-4)
[![CodeQL](https://github.com/acoboh/query-filter-jpa/actions/workflows/codeql.yml/badge.svg)](https://github.com/acoboh/query-filter-jpa/actions/workflows/codeql.yml)
[![Maven Publish](https://github.com/acoboh/query-filter-jpa/actions/workflows/maven-publish.yml/badge.svg)](https://github.com/acoboh/query-filter-jpa/actions/workflows/maven-publish.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=acoboh_query-filter-jpa&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=acoboh_query-filter-jpa)
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/acoboh/query-filter-jpa)

### 🔹 The simplest way to filter JPA queries in Spring Boot!

**QueryFilterJPA** lets you create powerful, dynamic filters with minimal setup. Define filterable fields, integrate
with OpenAPI, and use intuitive query syntax—all without writing complex specifications.

Supports **Spring Boot 3 and Spring Boot 4** (dedicated artifacts for each version).

## ✨ Features

✅ **Easy-to-use annotations** for defining filters in your entities.  
✅ **Supports RHS Colon (`field=eq:value`) and LHS Brackets (`field[eq]=value`) syntax.**  
✅ **Automatic OpenAPI documentation** generation for filters.  
✅ **PostgreSQL support** for advanced filtering like arrays and JSON fields.  
✅ **Seamless integration** with `JpaSpecificationExecutor`.  
✅ **`@QFMultiParam`** to combine multiple filter parameters in a single request.

## 🚀 Quick Start

### 1️⃣ Install the dependency

```xml
<!-- Spring Boot 4 -->
<dependency>
    <groupId>io.github.acoboh</groupId>
    <artifactId>query-filter-jpa-4</artifactId>
    <version>2.0.0</version>
</dependency>

<!-- Spring Boot 3 -->
<dependency>
    <groupId>io.github.acoboh</groupId>
    <artifactId>query-filter-jpa-3</artifactId>
    <version>2.0.0</version>
</dependency>
```

### 2️⃣ Define your entity

```java

@Entity
public class PostBlog {
    @Id
    private Long id;
    private String author;
    private int likes;
    private LocalDateTime createDate;
}
```

### 3️⃣ Create a filter class

```java

@QFDefinitionClass(PostBlog.class)
public class PostFilterDef {
    @QFElement("author")
    private String author;
    @QFElement("likes")
    private int likes;
    @QFDate
    @QFElement("createDate")
    private LocalDateTime createDate;
}
```

### 4️⃣ Enable QueryFilterJPA

```java
@EnableQueryFilter(basePackageClasses = PostFilterDef.class)
```

### 5️⃣ Use it in your repository

```java
public interface PostBlogRepository extends JpaRepository<PostBlog, Long>, JpaSpecificationExecutor<PostBlog> {
}
```

### 6️⃣ Apply filters in your controller (choose one option)

**Option A: `@QFParam`**

Use when your endpoint receives a single `filter` parameter and you want a `QueryFilter` object.

```java

@RestController
@RequestMapping("/posts")
public class PostController {
    @Autowired
    private PostBlogRepository repository;

    @GetMapping
    public List<PostBlog> getPosts(
            @RequestParam(required = false, defaultValue = "") @QFParam(PostFilterDef.class) QueryFilter<PostBlog> filter) {
        return repository.findAll(filter);
    }
}
```

**Option B: `@QFMultiParam`**

Use when you want to bind multiple filter parameters directly into the filter definition object.

```java

@RestController
@RequestMapping("/posts")
public class PostMultiParamController {
    @Autowired
    private PostBlogRepository repository;

    @GetMapping
    public List<PostBlog> getPosts(@QFMultiParam PostFilterDef multi) {
        return repository.findAll(multi);
    }
}
```

_Choose one option per endpoint. `@QFMultiParam` is not compatible with `@QFParam` in the same endpoint._

## 🎯 How to Use Query Filters

### ✅ With `@QFParam`

**RHS Colon Syntax (`field=operation:value`)**

```url
/posts?filter=author=eq:john&likes=gte:10&sort=-createDate
```

_Equivalent SQL:_

```sql
SELECT *
FROM posts
WHERE author = 'john'
  AND likes >= 10
ORDER BY create_date DESC;
```

**LHS Brackets Syntax (`field[operation]=value`)**

```url
/posts?filter=author[eq]=john&likes[gte]=10&sort=-createDate
```

_Same SQL output as above._

### ✅ With `@QFMultiParam`

**RHS Colon Syntax (`field=operation:value`)**

```url
/posts?author=eq:john&likes=gte:10&sort=-createDate
```

_Equivalent SQL:_

```sql
SELECT *
FROM posts
WHERE author = 'john'
  AND likes >= 10
ORDER BY create_date DESC;
```

**LHS Brackets Syntax (`field=[operation]value`)**

```url
/posts?author=john&likes=[gte]10&sort=-createDate
```
_Same SQL output as above._

> _**NOTE**_: There is a difference in the LHS syntax for `@QFMultiParam`: the operator is enclosed in brackets **after** the field name, and the value follows immediately after the closing bracket.
> Example: on QFParam: `author[eq]=john` vs on QFMultiParam: `author=[eq]john`
>
> _This is necessary to avoid ambiguity when multiple parameters are used without a common `filter` wrapper._

## 📢 OpenAPI Integration

QueryFilterJPA automatically documents your filters in **Swagger-UI**. Add the following dependency:

```xml
<!-- Spring Boot 4 -->
<dependency>
    <groupId>io.github.acoboh</groupId>
    <artifactId>query-filter-jpa-openapi-4</artifactId>
    <version>2.0.0</version>
</dependency>

<!-- Spring Boot 3 -->
<dependency>
    <groupId>io.github.acoboh</groupId>
    <artifactId>query-filter-jpa-openapi-3</artifactId>
    <version>2.0.0</version>
</dependency>
```

### Example for `@QFParam`:

![OpenAPI Example](/doc/resources/swagger-single-example.png)

### Example for `@QFMultiParam`:

![OpenAPI Example Multi](/doc/resources/swagger-multi-example.png)

## 📖 More Documentation

Check out the **[Wiki](https://github.com/acoboh/query-filter-jpa/wiki)** for advanced configurations and examples!

---
⭐ **Star this repo** if QueryFilterJPA saves you time! 🚀
