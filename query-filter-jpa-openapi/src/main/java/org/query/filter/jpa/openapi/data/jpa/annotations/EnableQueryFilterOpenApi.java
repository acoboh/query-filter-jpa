package org.query.filter.jpa.openapi.data.jpa.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Class to configure packages of Filter Class
 *
 * @author Adrián Cobo
 
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface EnableQueryFilterOpenApi {

	/**
	 * Base packages to find controllers
	 * 
	 * @return packages to find controllers
	 */
	String[] basePackages() default {};

	/**
	 * Base package classes to find controllers
	 * 
	 * @return package classes to find controllers
	 */
	Class<?>[] basePackageClasses() default {};

}
