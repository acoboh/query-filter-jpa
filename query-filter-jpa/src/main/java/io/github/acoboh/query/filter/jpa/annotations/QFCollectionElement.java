package io.github.acoboh.query.filter.jpa.annotations;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation used to define a query filter element that is a collection
 * 
 * @author Adrián Cobo
 *
 */
@Documented
@Retention(RUNTIME)
@Target({ FIELD, ANNOTATION_TYPE })
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
}
