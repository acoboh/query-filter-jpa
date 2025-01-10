package io.github.acoboh.query.filter.jpa.openapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Auto-Configuration class for OpenAPI standard
 *
 * @author Adrián Cobo
 */
@Configuration
@Import(OpenApiCustomiserImpl.class)
public class QueryFilterOpenApiAutoconfigurer {

}
