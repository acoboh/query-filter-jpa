package io.github.acoboh.query.filter.jpa.annotations;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.github.acoboh.query.filter.jpa.operations.QFOperationEnum;

/**
 * Annotation used to define the query filter param filter.
 * <p>
 * Name is the field selected on the query filter input string
 * <p>
 * Path is the access level to the object of the {@link io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass} selected
 * class
 * <p>
 * You can choose the element type directly instead of check the class type using <b>abstractElement</b> value in annotation
 *
 * @author Adrián Cobo
 * 
 */

@Documented
@Retention(RUNTIME)
@Target({ FIELD, ANNOTATION_TYPE })
@Repeatable(QFElements.class)
public @interface QFElement {

	/**
	 * Path to check the input part. Is the full level access until the variable you want to filter
	 *
	 * @return value
	 */
	String value();

	/**
	 * Name to use in the input filter. If it is not specified, it will be used the name of the variable associated
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
	 * If True, only case-sensitive string will be match on like operations. Otherwise, all strings will match.
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
	 * <code>principal?.name</code>
	 * <p>
	 * <b>Use with caution!</b>
	 * <p>
	 * It is highly recommended to use with {@link QFBlockParsing}
	 * <p>
	 * If there is more than one value, only the first one will be used. Example:
	 * <p>
	 * <code>
	 * &#64;security.isAuthorized(),isAuthenticated()
	 * </code>
	 * <p>
	 * In this case, only the first part <code>@security.isAuthorized()</code> will be used
	 * <p>
	 * You can also reuse vales from other fields used example:
	 * <p>
	 * Example of use the value of a query element named 'otherElement' to check if filter value is greater than 10
	 * <p>
	 * <code>
	 * #otherElement &gt; 10
	 * </code>
	 * <p>
	 * You need to use the option <b>order</b> to use this functionality.
	 *
	 * @return true if SpEL is enabled. False otherwise
	 */
	boolean isSpPELExpression() default false;

	/**
	 * Can specify if any filter property is null or empty, will be ignored and removed from the filter
	 *
	 * @return true, if null and blank values will be ignored. False otherwise
	 */
	boolean blankIgnore() default true;

	/**
	 * Order for resolver filter. It its need if any QueryFilterElement is SpEL and needs the values from previous fields
	 *
	 * @return order of filters
	 */
	int order() default Integer.MIN_VALUE;

}
