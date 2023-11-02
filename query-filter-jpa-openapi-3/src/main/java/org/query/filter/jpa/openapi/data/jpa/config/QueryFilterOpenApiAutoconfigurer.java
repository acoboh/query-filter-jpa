package org.query.filter.jpa.openapi.data.jpa.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * @author Adrián Cobo
 *
 */
@AutoConfiguration
@Import(OpenApiCustomiserImpl.class)
public class QueryFilterOpenApiAutoconfigurer {

}
