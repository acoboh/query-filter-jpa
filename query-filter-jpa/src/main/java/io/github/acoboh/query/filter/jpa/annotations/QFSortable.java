package io.github.acoboh.query.filter.jpa.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Query filter annotation to mark field as sortable
 *
 * @author Adrián Cobo
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface QFSortable {

	/**
	 * New name of filter
	 *
	 * @return new name of filter
	 */
	String value();

	/**
	 * Enable Fetch Load if sort is present on the filter
	 *
	 * @return true if enabled
	 */
	boolean autoFetch() default true;
}
