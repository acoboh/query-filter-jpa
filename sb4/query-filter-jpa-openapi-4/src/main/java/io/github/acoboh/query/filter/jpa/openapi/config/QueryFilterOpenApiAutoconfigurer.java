package io.github.acoboh.query.filter.jpa.openapi.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * Auto configuration class to enable OpenAPI customizations for Query Filter JPA.
 * @author Adrián Cobo
 */
@AutoConfiguration
@Import(OpenApiCustomiserImpl.class)
public class QueryFilterOpenApiAutoconfigurer {

}
