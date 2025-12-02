package io.github.acoboh.query.filter.jpa.config;

import io.github.acoboh.query.filter.jpa.converters.QFCustomConverter;
import io.github.acoboh.query.filter.jpa.processor.QFProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Class to enable custom converters for Spring Boot Controllers via
 * {@linkplain WebMvcConfigurer} converters
 *
 * @author Adrián Cobo
 */
@Configuration
@EnableWebMvc
@DependsOn("entityManagerFactory")
public class QFWebMvcConfigurer implements WebMvcConfigurer {

    private static final Logger LOGGER = LoggerFactory.getLogger(QFWebMvcConfigurer.class);

    private final List<QFProcessor<?, ?>> processors;

    QFWebMvcConfigurer(List<QFProcessor<?, ?>> processors) {
        this.processors = processors;
    }

    /** {@inheritDoc} */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        LOGGER.info("Using QueryFilterWebMvcConfigurer. Registering custom formatters");
        registry.addConverter(new QFCustomConverter(processors));
    }

}
