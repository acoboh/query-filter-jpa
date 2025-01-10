package io.github.acoboh.query.filter.jpa.annotations;

import io.github.acoboh.query.filter.jpa.processor.QFParamType;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Query filter parameter
 *
 * @author Adrián Cobo
 */
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface QFParam {

    /**
     * Filter class
     *
     * @return filter class
     */
    Class<?> value();

    /**
     * Standard of filtering
     *
     * @return Standard of filtering
     */
    QFParamType type() default QFParamType.RHS_COLON;

}
