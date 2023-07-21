package io.github.acoboh.query.filter.jpa.annotations;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.github.acoboh.query.filter.jpa.predicate.PredicateOperation;

/**
 * Annotation used for group multiple query filter elements in the same field
 *
 * @author Adrián Cobo
 */
@Documented
@Retention(RUNTIME)
@Target({ FIELD, ANNOTATION_TYPE })
public @interface QFElements {

	/**
	 * All {@linkplain QFElement} annotations
	 * 
	 * @return {@linkplain QFElement} annotations
	 */
	QFElement[] value();

	/**
	 * Operation to be applied to all elements
	 * 
	 * @return operation selected
	 */
	PredicateOperation operation() default PredicateOperation.AND;
}
