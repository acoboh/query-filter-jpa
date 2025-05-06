package io.github.acoboh.query.filter.jpa.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.persistence.criteria.JoinType;

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
	 * Example: If you specify {@code JoinType.LEFT, JoinType.INNER}, the first join
	 * will be a LEFT join, the second will be an INNER join, and all the rest will
	 * be INNER joins.
	 *
	 * @return join type to use
	 */
	JoinType[] joinTypes() default {JoinType.INNER};
}
