package io.github.acoboh.query.filter.jpa.annotations;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.github.acoboh.query.filter.jpa.operations.QFOperationEnum;
import jakarta.persistence.criteria.JoinType;

/**
 * Annotation used to define the query filter param filter.
 * <p>
 * Name is the field selected on the query filter input string
 * <p>
 * Path is the access level to the object of the
 * {@link io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass}
 * selected class
 * <p>
 * You can choose the element type directly instead of check the class type
 * using <b>abstractElement</b> value in annotation
 *
 * @author Adrián Cobo
 */

@Documented
@Retention(RUNTIME)
@Target({FIELD, ANNOTATION_TYPE})
@Repeatable(QFElements.class)
public @interface QFElement {

	/**
	 * Path to check the input part. Is the full level access until the variable you
	 * want to filter
	 *
	 * @return value
	 */
	String value();

	/**
	 * Name to use in the input filter. If it is not specified, it will be used the
	 * name of the variable associated
	 *
	 * @return name
	 */
	String name() default "";

	/**
	 * Use it to sub-query filter instance of join attributes
	 *
	 * @return true if sub-query is used. False otherwise
	 */
	boolean subquery() default false;

	/**
	 * Define if it is possible to order with this element
	 *
	 * @return true if sortable, false otherwise
	 */
	boolean sortable() default true;

	/**
	 * Default value
	 *
	 * @return default values of field
	 */
	String[] defaultValues() default {};

	/**
	 * Default operation of default value
	 *
	 * @return default operation of the field used by default values
	 */
	QFOperationEnum defaultOperation() default QFOperationEnum.EQUAL;

	/**
	 * If True, only case-sensitive string will be match on like operations.
	 * Otherwise, all strings will match.
	 *
	 * <p>
	 * Example:
	 * <p>
	 * true -- "Example" will match only "Example"
	 * <p>
	 * false -- "Example" will match "EXAMPLE", "example", "eXAmple"...
	 *
	 * @return true if case-sensitive
	 */
	boolean caseSensitive() default false;

	/**
	 * If true, the queries will be created as Postgresql ARRAY[]
	 *
	 * @return true for Postgresql arrays, false for default format
	 */
	boolean arrayTyped() default false;

	/**
	 * Can resolve SpEL security expressions like:
	 * <p>
	 * {@code principal?.name}
	 * <p>
	 * <b>Use with caution!</b>
	 * <p>
	 * It is highly recommended to use with {@link QFBlockParsing}
	 * <p>
	 * If there is more than one value, only the first one will be used. Example:
	 * <p>
	 * {@code @security.isAuthorized(),isAuthenticated() }
	 * <p>
	 * In this case, only the first part {@code @security.isAuthorized()} will be
	 * used
	 * <p>
	 * You can also reuse vales from other fields used example:
	 * <p>
	 * Example of use the value of a query element named 'otherElement' to check if
	 * filter value is greater than 10
	 * <p>
	 * {@code
	 * #otherElement > 10
	 * }
	 * <p>
	 * You need to use the option <b>order</b> to use this functionality.
	 *
	 * @return true if SpEL is enabled. False otherwise
	 */
	boolean isSpPELExpression() default false;

	/**
	 * Can specify if any filter property is null or empty, will be ignored and
	 * removed from the filter
	 *
	 * @return true, if null and blank values will be ignored. False otherwise
	 */
	boolean blankIgnore() default true;

	/**
	 * Order for resolver filter. It its need if any QueryFilterElement is SpEL and
	 * needs the values from previous fields
	 *
	 * @return order of filters
	 */
	int order() default 0;

	/**
	 * If the filter is sortable, you can activate Fetch Load automatically
	 *
	 * @return true if fetch is enabled
	 */
	boolean autoFetch() default true;

	/**
	 * Select the class of the element if it is only available on a nested
	 * Discriminator class.
	 * <p>
	 * Example: <blockquote>
	 *
	 * <pre>
	 * {@code @Entity }
	 * {@code @Inheritance(strategy = InheritanceType.JOINED)}
	 * public class ParentEntity {
	 *     // Base data
	 * }
	 *
	 * {@code @Entity }
	 * public class SubclassAEntity extends ParentEntity {
	 *     // Subclass A data
	 *     private String subClassField;
	 * }
	 *
	 * {@code @QFDefinitionClass(ParentEntity.class)}
	 * public class FilterParentEntityDef {
	 *    {@code @QFElement(value = "subClassField", subClassMapping = SubclassAEntity.class)}
	 *    private String subClassField;
	 * {@code}}
	 * </pre>
	 *
	 * </blockquote>
	 *
	 * @return subClassMapping
	 */
	Class<?> subClassMapping() default Void.class;

	/**
	 * Select the path of the subclass level if it is only available on a nested
	 * Discriminator class.
	 * <p>
	 * Need to be used with {@linkplain #subClassMapping()}
	 *
	 * @return path to apply subclass scanning
	 */
	String subClassMappingPath() default "";

	/**
	 * Select the join type to use on the query.
	 * 
	 * <p>
	 * If you only specify one join type, it will be used for all joins.
	 * <p>
	 * If you specify multiple join types, and there are multiple joins, il will use
	 * the first join type for the first until the latest is reached, and it will be
	 * used for the rest.
	 * <p>
	 * Example: If you specify {@code {LEFT, INNER}}, the first join will be a LEFT
	 * join, the second will be an INNER join, and all the rest will be INNER joins.
	 * 
	 * @return join type to use
	 */
	JoinType[] joinTypes() default {JoinType.INNER};

	/**
	 * List of allowed operations for this element
	 * <p>
	 * If the allowed operations are not specified, all operations will be allowed.
	 * <p>
	 * If the field is annotated with {@link QFElements}, the allowed operations
	 * will be applied to all elements defined in the {@link #value()} attribute and
	 * the configuration of the {@link QFElement} will be ignored.
	 *
	 * @return array of allowed operations
	 */
	QFOperationEnum[] allowedOperations() default {};

}
