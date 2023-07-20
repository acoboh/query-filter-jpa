package io.github.acoboh.query.filter.jpa.annotations;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.github.acoboh.query.filter.jpa.processor.QFParamType;

/**
 * Query filter parameter
 *
 * @author Adrián Cobo
 */
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface QFParam {

	Class<?> value();

	String name() default "filter";

	QFParamType type() default QFParamType.RHS_COLON;

}
