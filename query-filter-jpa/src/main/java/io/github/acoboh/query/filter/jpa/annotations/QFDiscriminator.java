package io.github.acoboh.query.filter.jpa.annotations;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

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
	 *
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

}
