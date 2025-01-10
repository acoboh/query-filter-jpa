package io.github.acoboh.query.filter.jpa.openapi.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * @author Adrián Cobo
 */
@AutoConfiguration
@Import(OpenApiCustomiserImpl.class)
public class QueryFilterOpenApiAutoconfigurer {

}
