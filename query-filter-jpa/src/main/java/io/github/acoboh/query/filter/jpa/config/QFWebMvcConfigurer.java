package io.github.acoboh.query.filter.jpa.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.github.acoboh.query.filter.jpa.converters.QFCustomConverter;
import io.github.acoboh.query.filter.jpa.processor.QFProcessor;

/**
 * Class to enable custom converters for Spring Boot Controllers via {@linkplain WebMvcConfigurer} converters
 *
 * @author Adrián Cobo
 * @version $Id: $Id
 */
@Configuration
@EnableWebMvc
public class QFWebMvcConfigurer implements WebMvcConfigurer {

	private static final Logger LOGGER = LoggerFactory.getLogger(QFWebMvcConfigurer.class);

	@Autowired
	private List<QFProcessor<?, ?>> processors;

	/** {@inheritDoc} */
	@Override
	public void addFormatters(FormatterRegistry registry) {
		LOGGER.info("Using QueryFilterWebMvcConfigurer. Registering custom formatters");
		registry.addConverter(new QFCustomConverter(processors));
	}
}
