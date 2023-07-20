<!-- 
[![License](https://img.shields.io/github/license/acoboh/query-filter-jpa.svg)](https://raw.githubusercontent.com/acoboh/query-filter-jpa/main/LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.acobo/query-filter-jpa.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.acobo/query-filter-jpa)
[![javadoc](https://javadoc.io/badge2/io.github.acobo/query-filter-jpa/javadoc.svg)](https://javadoc.io/doc/io.github.acobo/query-filter-jpa) -->

# Introduction

The QueryFilterJPA Library adds the possibility of create custom filters with RHS Colon and LHS Brackets with Spring JPA easily. This library is useful for allowing the user to obtain data according to their requirements in an easy way for the programmer, since with a few small configuration classes, they will have the ability to create filters with infinite possibilities.

# Features

* Create filter specification based on Entity Models
* Convert RHS Colon or LHS Brackets filter into a JPA Query
* Add manually fields and block operations on each new filter field
* Create custom predicates on each query filter
* Expose filter documentation on endpoints
* Create extended OpenAPI documentation with QueryFilter specification

# Installation

**COMING SOON**

<!-- You can install the library by adding the following dependency to your project's `pom.xml` file:

```xml
<dependency>
    <groupId>io.github.acoboh</groupId>
    <artifactId>query-filter-jpa</artifactId>
    <version>${project-version}</version>
</dependency>

<dependency>
    <groupId>io.github.acoboh</groupId>
    <artifactId>query-filter-jpa-3</artifactId>
    <version>${project-version}</version>
</dependency>

``` -->


# Getting Started

First of all, you need an entity model. 

```java
@Entity
public class PostBlog {

	public enum PostType {
		VIDEO, TEXT
	}

	@Id
	private Long tsid;

	private String author;

	private String text;

	private double avgNote;

	private int likes;

	private LocalDateTime createDate;

	private Timestamp lastTimestamp;

	private boolean published;

	@Enumerated(EnumType.STRING)
	private PostType postType;

}
```

Once you have import the library, you can easily create new filters. To create your first filter, you only need to specify it with a new class. 


```java

@QFDefinitionClass(PostBlog.class) // Select the entity related model
public class PostFilterDef {

	@QFElement("author")
	private String author;

	@QFElement("likes")
	private int likes;

	@QFElement("avgNote")
	private double avgNote;

	@QFDate
	@QFElement("createDate")
	private LocalDateTime createDate;

	@QFSortable("lastTimestamp")
	private Timestamp lastTimestamp;

	@QFElement("postType")
	private String postType;

	@QFElement("published")
	@QFBlockParsing
	private boolean published;

}
```

With the class annotation `@QFDefinitionClass` you specify the entity model on which you want to apply the filters. 

You also have other annotations to indicate each of the available fields for filtering:

- `@QFElement`: Specify the field name on which filtering operations can be performed. The field name indicates the text to be used on the RHS or LHS of the filter. _(The name used on RHS or LHS can be override with the annotation properties)_
- `@QFDate`: Specify that the selected field is a date. You can select the format of the text to be parsed _(The default format is **yyyy-MM-dd'T'HH:mm:ss'Z'** and the timezone is **UTC**)_
- `@QFSortable`: Specify that the field is only sortable and can not be filtered. Useful when you only want to enable sorting by a field but do not want it to be filterable. _(If you already used the `QFElement` annotation, the field will be sortable by default. You do not need to use that annotation)_
- `@QFBlockParsing`: Specify that this field is blocked on the stage of parsing from the *String* filter to the *QueryFilter* object. If the field is present on the *String*, an exception will be thrown. Useful when you need to ensure that some fields can not be filtered by an user but you need to filter manually on code. _(An example can be usernames, roles and other sensitive data)_

Once you have created that class, there are only two more steps.

The first step is to enable the **Query Filter** bean processors. You can do that with the following annotation on the main class: 

```java
@EnableQueryFilter(basePackageClasses = PostFilterDef.class)
```

> **_NOTE_**: The `basePackageClasses` and `basePackages` are not required by default

The **Query Filter** object is an implementation of the `Specification` interface from JPA, so you need your repository to extend the `JpaSpecificationExecutor` interface.

```java
public interface PostBlogRepository extends JpaSpecificationExecutor<PostBlog>, JpaRepository<PostBlog, Long> {

}
```

Now you can use the filter on the controller easily:

```java

@RestController
@RequestMapping("/posts")
public class PostRestController {

	@Autowired
	private PostBlogRepository repository;

	@GetMapping
	public List<PostBlog> getPosts(
			@RequestParam(required = false) @QFParam(PostFilterDef.class) QueryFilter<PostBlog> filter) {
		return repository.findAll(filter);
	}
}
```

With the `@QFParam` you select the filter to be used. You can use the `QueryFilter<PostBlog>` object to automatically create the final query filter object. Now you can operate with the `filter` object or you can use it directly on the repository.

# OpenAPI Documentation

If you use Swagger-UI with OpenAPI 3 documentation, you can easily expose an automatic generated documentation of the filter. 

![Image from OpenAPI example](/doc/resources/swagger-example-posts.png)

You can easily enable with the following custom annotation on the main class:

```java
@EnableQueryFilterOpenApi(basePackageClasses = PostRestController.class)
```

> **_NOTE_**: The `basePackageClasses` and `basePackages` are not required by default

# Examples

You can find more examples of how to use the library in the [examples](/examples/) directory.

# How to write *String Filters*

Once you have your service with the **Query Filter** enabled, you can start using **RHS Colon** and **LHS Brackets** standards to filter.

Following the **OpenAPI** documentation, you have several options to filter on each field.

## Allowed operations

- **eq**: Equals
- **ne**: Not equals
- **gt**: Greater than
- **gte**: Greater or equal than
- **lt**: Less than
- **lte**: Less or equal than
- **like**: Like _(for string operations)_
- **starts**: Starts with _(for string operations)_
- **ends**: Ends with _(for string opertions)_
- **in**: IN (in operator of SQL)
- **nin**: Not IN (not it operator of SQL)
- **null**: Is null (is null or is not null. The value must be `false` or `true`)
- **ovlp**: Overlap _(for PostgreSQL arrays)_
- **containedBy**: Contained by _(for PostgreSQL arrays)_

## RHS Colon

The syntax of this standard is the following one:

```log
<field>=<operation>:<value>
```

An example could be:

```log
author=eq:acobo
```

The filter will produce an SQL query like:

```sql
SELECT * FROM posts WHERE author = 'acobo'
```

You can use other operations. Examples:

- `avgNote=gte:5`
- `postType=ne:VIDEO`

## LHS Brackets

The syntax of this standard is the following one:

```log
<field>[<operation>]=<value>
```

An example could be:

```log
author[eq]=acobo
```

The filter will produce an SQL query like:

```sql
SELECT * FROM posts WHERE author = 'acobo'
```

You can use other operations. Examples:

- `avgNote[gte]=5`
- `postType[ne]=VIDEO`

## Sort results

If you want to sort, you can do it with the following syntax:

```log
sort=<direction><field>
```

The direction can be:

- **+**: For ascending direction
- **-**: For descending direction

An example could be:

```log
sort=+author
```

## Concatenate multiple filters

If you want to concatenate multiple filters, you can easily do it with the `&` operator.

And example with **RHS Colon** could be:

```log
author=eq:acobo&avgNote=gte:5&sort=-avgNote
```

The same example with **LHS Brakets**:

```log
author[eq]=acobo&avgNote[gte]=5&sort=-avgNote
```

You can concatenate multiple sort operations. If you do that, the order is important

```log
sort=-avgNote&sort=+likes
```
```sql
order by avgNote desc, likes asc
```

If you change the order:

```log
sort=+likes&sort=-avgNote
```
```sql
order by likes asc, avgNote desc
```