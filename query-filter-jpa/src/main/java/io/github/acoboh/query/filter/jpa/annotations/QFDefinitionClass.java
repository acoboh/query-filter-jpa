package io.github.acoboh.query.filter.jpa.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.data.domain.Sort.Direction;

/**
 * Annotation used to define the matching class of the query filter param
 *
 * @author Adrián Cobo
 * 
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface QFDefinitionClass {

	/**
	 * Entity class to be filtered
	 *
	 * @return class
	 */
	Class<?> value();

	/**
	 * Used to disable all sortable fields
	 *
	 * @return true if the class is sortable, false otherwise
	 */
	boolean sortable() default true;

	/**
	 * Used to select a default predicate. Empty if no predicated must be used
	 *
	 * @return default predicate name
	 */
	String defaultPredicate() default "";

	/**
	 * Enable or disable distinct on JPA Query
	 *
	 * @return true if enabled. False otherwise
	 */
	boolean distinct() default true;

	/**
	 * Allows the user to determine default sorting option
	 * 
	 * @return sorting options
	 */
	QFDefaultSort[] defaultSort() default {};

	/**
	 * Allows the user to select a default sort parts.
	 * <p>
	 * If the filter is manually cleared or manipulated with other sorting options, this default sorting options will be ignored
	 * 
	 * @author Adrián Cobo
	 *
	 */
	public @interface QFDefaultSort {

		/**
		 * Name of the property to be sorted
		 * 
		 * @return property name
		 */
		String value();

		/**
		 * Select the sort direction
		 * 
		 * @return sort direction
		 */
		Direction direction() default Direction.ASC;

	}
}
