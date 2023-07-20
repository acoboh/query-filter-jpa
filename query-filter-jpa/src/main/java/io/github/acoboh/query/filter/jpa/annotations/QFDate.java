package io.github.acoboh.query.filter.jpa.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.time.temporal.ChronoField;

/**
 * Annotation used to set any query filter element as a date
 * <p>
 * You can specify a format
 * </p>
 *
 * @author Adrián Cobo
 */
@Documented
@Retention(RUNTIME)
@Target(FIELD)
public @interface QFDate {

	/**
	 * Used to set a format time. If it is empty, a timestamp default format will be used
	 *
	 * @return Time format used
	 */
	String timeFormat() default "yyyy-MM-dd'T'HH:mm:ss'Z'";

	String zoneOffset() default "UTC";

	QFDateDefault[] parseDefaulting() default {};

	public @interface QFDateDefault {

		ChronoField chronoField();

		long value();

	}

}
