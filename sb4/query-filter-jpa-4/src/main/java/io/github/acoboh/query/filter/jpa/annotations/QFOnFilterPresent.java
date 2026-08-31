package io.github.acoboh.query.filter.jpa.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation used to trigger the default values of other filter fields whenever the
 * annotated field is present in the incoming request filter.
 * <p>
 * When the annotated field appears in the filter (with any value), the default values
 * configured on each field named in {@link #value()} are applied automatically, even if
 * those fields were not present in the request.
 *
 * @author Adrián Cobo
 */
@Documented
@Retention(RUNTIME)
@Target(FIELD)
public @interface QFOnFilterPresent {

    /**
     * Names of the other filter fields whose default values must be applied when the
     * annotated field is present in the filter.
     *
     * @return names of the related fields
     */
    String[] value();

}
