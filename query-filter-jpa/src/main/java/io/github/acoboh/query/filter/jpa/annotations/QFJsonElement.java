package io.github.acoboh.query.filter.jpa.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Special query filter element used on JSON elements
 *
 * @author Adrián Cobo
 */
@Documented
@Retention(RUNTIME)
@Target({FIELD, ANNOTATION_TYPE})
public @interface QFJsonElement {

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
     * If True, only case-sensitive string will be match. Otherwise, all strings will match.
     *
     * <p>
     * Example:
     * <p>
     * true -- "Example" will match only "Example"
     * <p>
     * false -- "Example" will match "EXAMPLE", "example", "eXAMple"...
     * </p>
     *
     * @return true if case-sensitive
     */
    boolean caseSensitive() default false;

}
