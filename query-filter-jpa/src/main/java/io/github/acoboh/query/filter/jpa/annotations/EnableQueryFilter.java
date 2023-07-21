package io.github.acoboh.query.filter.jpa.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Class to configure packages of Filter Class
 *
 * @author Adrián Cobo
 * @version $Id: $Id
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface EnableQueryFilter {

	/**
	 * Base package names to find classes with {@linkplain QFDefinitionClass} annotations
	 * 
	 * @return list of packages to search
	 */
	String[] basePackages() default {};

	/**
	 * Base package classes to find classes with {@linkplain QFDefinitionClass} annotations
	 * 
	 * @return list of package classes to search
	 */
	Class<?>[] basePackageClasses() default {};

}
