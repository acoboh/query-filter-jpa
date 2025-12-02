package io.github.acoboh.query.filter.jpa.annotations;

import io.github.acoboh.query.filter.jpa.operations.QFOperationDiscriminatorEnum;
import jakarta.persistence.criteria.JoinType;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Special query filter element to filter between Discriminator types
 *
 * @author Adrián Cobo
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface QFDiscriminator {

    /**
     * Path of the discriminator entity model
     *
     * @return path of the discriminator entity model
     */
    String path() default "";

    /**
     * New name of the filter
     *
     * @return new name of the filter
     */
    String name() default "";

    /**
     * List of possible values
     *
     * @return possible values
     */
    Value[] value() default {};

    /**
     * Possible values of the discriminator
     *
     * @author Adrián Cobo
     */
    @Retention(RUNTIME)
    @Target({ ANNOTATION_TYPE, FIELD })
    @interface Value {

        /**
         * Name of the discriminator type
         *
         * @return name of the discriminator type
         */
        String name();

        /**
         * Class of the discriminator entity model
         *
         * @return class of the discriminator entity model
         */
        Class<?> type();

    }

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
    QFOperationDiscriminatorEnum[] allowedOperations() default {};

    /**
     * Default value to use if the input is not specified
     * <p>
     * If the default value is not specified, it will be used
     *
     * @return default value
     */
    String[] defaultValues() default {};

    /**
     * Default operation to use if the input is not specified
     * <p>
     * If the default operation is not specified, it will be used
     * {@link QFOperationDiscriminatorEnum#EQUAL}.
     *
     * @return default operation
     */
    QFOperationDiscriminatorEnum defaultOperation() default QFOperationDiscriminatorEnum.EQUAL;

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
