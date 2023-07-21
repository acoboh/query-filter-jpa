package io.github.acoboh.query.filter.jpa.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation used to create multiple custom predicates
 *
 * @author Adrián Cobo
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface QFPredicates {

	/**
	 * List of predicates
	 * 
	 * @return predicates
	 */
	QFPredicate[] value();
}
