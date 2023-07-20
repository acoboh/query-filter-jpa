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

	String path() default "";

	String name() default "";

	Value[] value() default {};

	@Retention(RUNTIME)
	@Target({ ANNOTATION_TYPE, FIELD })
	@interface Value {

		String name();

		Class<?> type();

	}

}
