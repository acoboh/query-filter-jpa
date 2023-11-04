package io.github.acoboh.query.filter.jpa.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

import io.github.acoboh.query.filter.jpa.advisor.QFExceptionAdvisor;
import io.github.acoboh.query.filter.jpa.properties.QueryFilterProperties;
import io.github.acoboh.query.filter.jpa.spel.SpelResolverBeanConfig;

/**
 * Autoconfigure class of the query filter library
 *
 * @author Adrián Cobo
 * 
 */
@AutoConfiguration
@Import({ QFExceptionAdvisor.class, SpelResolverBeanConfig.class, QFBeanFactoryPostProcessor.class,
		QFWebMvcConfigurer.class, QueryFilterProperties.class })
public class QueryFilterAutoconfigure {

}
