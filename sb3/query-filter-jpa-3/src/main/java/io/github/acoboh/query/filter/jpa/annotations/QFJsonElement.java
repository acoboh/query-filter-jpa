package io.github.acoboh.query.filter.jpa.annotations;

import io.github.acoboh.query.filter.jpa.operations.QFOperationJsonEnum;
import jakarta.persistence.criteria.JoinType;

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
@Target({ FIELD, ANNOTATION_TYPE })
public @interface QFJsonElement {

    /**
     * Path to check the input part. Is the full level access until the variable you
     * want to filter
     *
     * @return value
     */
    String value();

    /**
     * Name to use in the input filter. If it is not specified, it will be used the
     * name of the variable associated
     *
     * @return name
     */
    String name() default "";

    /**
     * If True, only case-sensitive string will be match. Otherwise, all strings
     * will match.
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
     * Example: If you specify {@code {LEFT, INNER}}, the first join will be a LEFT
     * join, the second will be an INNER join, and all the rest will be INNER joins.
     *
     * @return join type to use
     */
    JoinType[] joinTypes() default { JoinType.INNER };

    /**
     * List of allowed operations for this element
     * <p>
     * If the allowed operations are not specified, all operations will be allowed.
     *
     * @return array of allowed operations
     */
    QFOperationJsonEnum[] allowedOperations() default {};

    /**
     * Default value to use if the input is not specified
     * <p>
     * If the default value is not specified, it will be ignored.
     *
     * @return default value
     */
    String defaultValue() default "";

    /**
     * Default operation to use if the input is not specified
     * <p>
     * If the default operation is not specified, it will be used
     * {@link QFOperationJsonEnum#EQUAL}.
     *
     * @return default operation
     */
    QFOperationJsonEnum defaultOperation() default QFOperationJsonEnum.EQUAL;

    /**
     * Order of the element in the query
     * <p>
     * If the order is not specified, it will be used 0.
     * <p>
     * If you want to change the order of the elements, you can use this attribute.
     * <p>
     * The lower the number, the higher the priority.
     *
     * @return order of the element in the query
     */
    int order() default 0;

}
