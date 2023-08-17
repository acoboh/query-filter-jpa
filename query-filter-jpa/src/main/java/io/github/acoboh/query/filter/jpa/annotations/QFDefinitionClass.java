package io.github.acoboh.query.filter.jpa.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation used to define the matching class of the query filter param
 *
 * @author Adrián Cobo
 
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
	 * Sort property key name
	 *
	 * @return sort key
	 */
	String sortProperty() default "sort";

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
}
