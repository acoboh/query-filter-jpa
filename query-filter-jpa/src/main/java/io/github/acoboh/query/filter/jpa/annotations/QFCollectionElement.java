package io.github.acoboh.query.filter.jpa.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

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
     * Path to check the input part. Is the full level access until the collection element
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
     * Select the class of the element if it is only available on a nested Discriminator class.
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
     * Select the path of the subclass level if it is only available on a nested Discriminator class.
     * <p>
     * Need to be used with {@linkplain #subClassMapping()}
     *
     * @return path to apply subclass scanning
     */
    String subClassMappingPath() default "";
}
