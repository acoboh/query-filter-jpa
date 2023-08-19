package io.github.acoboh.query.filter.jpa.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import io.github.acoboh.query.filter.jpa.advisor.QFExceptionAdvisor;
import io.github.acoboh.query.filter.jpa.properties.QueryFilterProperties;
import io.github.acoboh.query.filter.jpa.spel.SecuritySpelContextResolver;
import io.github.acoboh.query.filter.jpa.spel.SpelContextResolver;

/**
 * Autoconfigure class of the query filter library
 *
 * @author Adrián Cobo
 * 
 */
@Configuration
@Import({ QFExceptionAdvisor.class, SecuritySpelContextResolver.class, SpelContextResolver.class,
		QFBeanFactoryPostProcessor.class, QFWebMvcConfigurer.class, QueryFilterProperties.class })
public class QueryFilterAutoconfigure {

}
