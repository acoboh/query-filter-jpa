
[![License](https://img.shields.io/github/license/acoboh/query-filter-jpa.svg)](https://raw.githubusercontent.com/acoboh/query-filter-jpa/main/LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.acoboh/query-filter-jpa.svg)](https://central.sonatype.com/artifact/io.github.acoboh/query-filter-jpa)
[![javadoc](https://javadoc.io/badge2/io.github.acoboh/query-filter-jpa/javadoc.svg)](https://javadoc.io/doc/io.github.acoboh/query-filter-jpa)
[![CodeQL](https://github.com/acoboh/query-filter-jpa/actions/workflows/codeql.yml/badge.svg)](https://github.com/acoboh/query-filter-jpa/actions/workflows/codeql.yml)
[![Maven Publish](https://github.com/acoboh/query-filter-jpa/actions/workflows/maven-publish.yml/badge.svg)](https://github.com/acoboh/query-filter-jpa/actions/workflows/maven-publish.yml)

## Introduction

The QueryFilterJPA Library adds the possibility of creating custom filters with RHS Colon and LHS Brackets with Spring JPA easily. This library is useful for allowing the user to obtain data according to their requirements in an easy way for the programmer. With just a few small configuration classes, users will have the ability to create filters with infinite possibilities.

## Features

* Create filter specifications based on Entity Models
* Convert RHS Colon or LHS Brackets filter into a JPA Query.
* Manually add fields and block operations on each new filter field.
* Create custom predicates for each query filter.
* Expose filter documentation on endpoints.
* Create extended OpenAPI documentation with QueryFilter specification.

## Installation

You can install the library by adding the following dependency to your project's `pom.xml` file:

#### Spring Boot 2.7.X

```xml
<dependency>
    <groupId>io.github.acoboh</groupId>
    <artifactId>query-filter-jpa</artifactId>
    <version>0.0.4</version>
</dependency>
``` 

#### Spring Boot 3.1.X

```xml
<dependency>
    <groupId>io.github.acoboh</groupId>
    <artifactId>query-filter-jpa-3</artifactId>
    <version>0.0.4</version>
</dependency>
```

## Configuration

#### Spring Boot 2.7.X


If you are running the Query Filter version for Spring Boot 2.7.X, you need to configure Hibernate to use the extended functions for PostgreSQL Arrays. To do that, you need to add the following property to hibernate:

```properties
hibernate.metadata_builder_contributor="io.github.acoboh.query.filter.jpa.contributor.QfMetadataBuilderContributor"
```

> **_NOTE_**: This is not necessary for Spring Boot 3.1.X version 

## Getting Started


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

Once you import the library, creating new filters becomes remarkably easy. To create your first filter, you only need to specify it within a new class.

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

With the class annotation @QFDefinitionClass, you specify the entity model on which you want to apply the filters. Additionally, you have other annotations to indicate each of the available fields for filtering:

- `@QFElement`: Specifies the field name on which filtering operations can be performed. The field name indicates the text to be used on the RHS or LHS of the filter. _(The name used on RHS or LHS can be overridden with the annotation properties.)_
- `@QFDate`: Specifies that the selected field is a date. You can select the format of the text to be parsed. _(The default format is **yyyy-MM-dd'T'HH:mm:ss'Z'** and the timezone is **UTC**)_
- `@QFSortable`: Specifies that the field is only sortable and cannot be filtered. This is useful when you only want to enable sorting by a field but do not want it to be filterable. _(If you already used the `QFElement` annotation, the field will be sortable by default, and you do not need to use this annotation)_
- `@QFBlockParsing`: Specifies that this field is blocked during the stage of parsing from the *String* filter to the *QueryFilter* object. If the field is present in the *String*, an exception will be thrown. This is useful when you need to ensure that some fields cannot be filtered by a user but need to be filtered manually in the code. _(For example, usernames, roles, and other sensitive data.)_

Once you have created that class, there are only two more steps.

The first step is to enable the **Query Filter** bean processors. You can do that with the following annotation on the main class:

```java
@EnableQueryFilter(basePackageClasses = PostFilterDef.class)
```

> **_NOTE_**: The `basePackageClasses` and `basePackages` are not required by default

The **Query Filter** object is an implementation of the `Specification` interface from JPA. To utilize it, your repository needs to extend the `JpaSpecificationExecutor` interface. This integration enables the Query Filter to work seamlessly with Spring Data JPA and perform dynamic filtering based on the user's input. By combining the Query Filter's custom filtering capabilities with the power of JPA's `Specification` and `JpaSpecificationExecutor`, you can efficiently retrieve data that meets the specified criteria.

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

With the `@QFParam` annotation, you can select the filter to be used. By utilizing the `QueryFilter<PostBlog>` object, you can automatically create the final query filter object. Once you have the `filter` object, you have the flexibility to perform operations directly with it or use it directly on the repository.

The `@QFParam` annotation allows you to define a parameter in your controller method, which will be used to receive the filter provided by the client. The Query Filter library will handle the conversion of the client's filter into the `QueryFilter<PostBlog>` object, which can then be used for querying your data.

Once you have the `QueryFilter<PostBlog>` object, you have multiple options for using it:

- Perform Operations with `filter` object: You can manually operate on the `filter` object to further customize the filtering behavior or perform additional actions.
- Use `filter` object with Repository: You can pass the `filter` object directly to the repository's query method that extends `JpaSpecificationExecutor`. The Query Filter will automatically apply the specified filtering criteria to the query.

Both approaches provide a straightforward and efficient way to work with the Query Filter and retrieve data according to the user's requirements.

## OpenAPI Documentation

If you use Swagger-UI with OpenAPI 3 documentation, you can easily expose an automatic generated documentation of the filter. 

![Image from OpenAPI example](/doc/resources/swagger-example-posts.png)

You need to import the following library:

#### Spring Boot 2.7.X

```xml
<dependency>
    <groupId>io.github.acoboh</groupId>
    <artifactId>query-filter-jpa-openapi</artifactId>
    <version>0.0.4</version>
</dependency>
``` 

#### Spring Boot 3.1.X

```xml
<dependency>
    <groupId>io.github.acoboh</groupId>
    <artifactId>query-filter-jpa-openapi-3</artifactId>
    <version>0.0.4</version>
</dependency>
```

You can easily enable with the following custom annotation on the main class:

```java
@EnableQueryFilterOpenApi(basePackageClasses = PostRestController.class)
```

> **_NOTE_**: The `basePackageClasses` and `basePackages` are not required by default

## Examples

You can find more examples of how to use the library in the [examples](/examples/) directory.

## How to write *String Filters*

Once you have your service with the **Query Filter** enabled, you can start using **RHS Colon** and **LHS Brackets** standards to filter data effectively.

Following the OpenAPI documentation, you have several options to filter on each field. 

### Allowed operations

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

### RHS Colon

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

### LHS Brackets

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

### Sort results

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

### Concatenate multiple filters

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

## MORE DOCUMENTATION 

To see full documentation, check the [Wiki section](https://github.com/acoboh/query-filter-jpa/wiki)
