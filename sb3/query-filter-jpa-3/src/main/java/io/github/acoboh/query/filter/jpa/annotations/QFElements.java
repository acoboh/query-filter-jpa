package io.github.acoboh.query.filter.jpa.annotations;

import io.github.acoboh.query.filter.jpa.operations.QFOperationEnum;
import io.github.acoboh.query.filter.jpa.predicate.PredicateOperation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

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

    /**
     * List of allowed operations for this element
     * <p>
     * If the allowed operations are not specified, all operations will be allowed.
     * 
     * <p>
     * When using this annotation, the allowed operations will be applied to all
     * elements defined in the {@link #value()} attribute and the configuration of
     * the {@link QFElement} will be ignored.
     *
     * @return array of allowed operations
     */
    QFOperationEnum[] allowedOperations() default {};
}
