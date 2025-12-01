package io.github.acoboh.query.filter.jpa.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Class to configure packages of Filter Class
 *
 * @author Adrián Cobo
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface EnableQueryFilter {

    /**
     * Base package names to find classes with {@linkplain QFDefinitionClass}
     * annotations
     *
     * @return array of packages to search
     */
    String[] basePackages() default {};

    /**
     * Base package classes to find classes with {@linkplain QFDefinitionClass}
     * annotations
     *
     * @return array of package classes to search
     */
    Class<?>[] basePackageClasses() default {};

}
