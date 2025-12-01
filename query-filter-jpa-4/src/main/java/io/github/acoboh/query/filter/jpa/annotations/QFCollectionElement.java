package io.github.acoboh.query.filter.jpa.annotations;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.github.acoboh.query.filter.jpa.operations.QFCollectionOperationEnum;
import jakarta.persistence.criteria.JoinType;

/**
 * Annotation used to define a query filter element that is a collection
 *
 * @author Adrián Cobo
 */
@Documented
@Retention(RUNTIME)
@Target({FIELD, ANNOTATION_TYPE})
public @interface QFCollectionElement {

	/**
	 * Path to check the input part. Is the full level access until the collection
	 * element
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
	 * Select the class of the element if it is only available on a nested
	 * Discriminator class.
	 * <p>
	 * Example: <blockquote>
	 *
	 * <pre>
	 * &#64;Entity
	 * &#64;Inheritance(strategy = InheritanceType.JOINED)
	 * public class ParentEntity {
	 *     // Base data
	 * }
	 *
	 * &#64;Entity
	 * public class SubclassAEntity extends ParentEntity {
	 *     // Subclass A data
	 *     &#64;OneToMany
	 *     private List links;
	 * }
	 *
	 * &#64;QFDefinitionClass(ParentEntity.class)
	 * public class FilterParentEntityDef {
	 *    &#64;QFCollectionElement(value = "links", subClassMapping = SubclassAEntity.class)
	 *    private int links;
	 * }
	 * }
	 * </pre>
	 *
	 * </blockquote>
	 *
	 * @return subClassMapping
	 */
	Class<?> subClassMapping() default Void.class;

	/**
	 * Select the path of the subclass level if it is only available on a nested
	 * Discriminator class.
	 * <p>
	 * Need to be used with {@linkplain #subClassMapping()}
	 *
	 * @return path to apply subclass scanning
	 */
	String subClassMappingPath() default "";

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
	JoinType[] joinTypes() default {JoinType.INNER};

	/**
	 * List of allowed operations for this element
	 * <p>
	 * If the allowed operations are not specified, all operations will be allowed.
	 *
	 * @return array of allowed operations
	 */
	QFCollectionOperationEnum[] allowedOperations() default {};

	/**
	 * Default value to use if the input is not specified
	 * <p>
	 * If the int value is {@link Integer#MIN_VALUE}, it will not be used
	 *
	 * @return default value
	 */
	int defaultValue() default Integer.MIN_VALUE;

	/**
	 * Default operation to use if the input is not specified
	 * <p>
	 * If the default operation is not specified, it will be used
	 * {@link QFCollectionOperationEnum#EQUAL}.
	 *
	 * @return default operation
	 */
	QFCollectionOperationEnum defaultOperation() default QFCollectionOperationEnum.EQUAL;

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
