package io.github.acoboh.query.filter.jpa.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.github.acoboh.query.filter.jpa.predicate.PredicateOperation;

/**
 * Annotation used to create custom predicates
 *
 * @author Adrián Cobo
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
@Repeatable(QFPredicates.class)
public @interface QFPredicate {

	/**
	 * The name of the predicate
	 * <p>
	 * It will be used on realtime to select between multiple predicates
	 *
	 * @return name of the predicate
	 */
	String name();

	/**
	 * Used to create a new expression of the predicate
	 * <p>
	 * An example:
	 * <p>
	 * <code>
	 * (name OR shortname) AND user
	 * </code>
	 * <p>
	 * All the fields not included will be ignored on the executed query
	 *
	 * @return the expression
	 */
	String expression();

	/**
	 * If the flag is active, all the parameters filtered that are not present on
	 * the expression will be included on a surrounding AND
	 * <p>
	 * Example:
	 * <p>
	 * If you have the expression 'A OR B' and you filter also by C, the final
	 * expression will be '(A OR B) AND C'
	 *
	 * @return true if the missing parameters should be used, false otherwise
	 */
	boolean includeMissing() default true;

	/**
	 * Operation to be applied on additional fields missing on expression
	 *
	 * @return selected operation
	 */
	PredicateOperation missingOperator() default PredicateOperation.AND;
}
