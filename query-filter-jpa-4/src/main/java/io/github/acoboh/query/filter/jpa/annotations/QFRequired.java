package io.github.acoboh.query.filter.jpa.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Used to force the usage of a field before a query filter execution.
 * <p>
 * This annotation is used to mark fields that are required for the query filter
 * to be present on the execution of the query.
 * </p>
 * <p>
 * If the field is not present, the query filter will throw an
 * QFRequiredException.
 * </p>
 * <p>
 * If the field is annotated with QFElement or QFSortable, the onSort() method
 * will be used to determine if the field is required on the sort. The
 * onExecution() will only be used for field with any operation
 * </p>
 */
@Documented
@Retention(RUNTIME)
@Target(FIELD)
public @interface QFRequired {

	/**
	 * Defines if the field is required on the string filter phase.
	 * 
	 * @return true if the field is required on the string filter phase,
	 */
	boolean onStringFilter() default true;

	/**
	 * Defines if the field is required on the execution phase.
	 * 
	 * @return true if the field is required on the execution phase,
	 */
	boolean onExecution() default true;

	/**
	 * Defines if the field is required on the sort phase. Only applicable if the
	 * field annotated with QFElement or QFSortable
	 * 
	 * @return true if the field is required on the sort phase,
	 */
	boolean onSort() default false;

}
