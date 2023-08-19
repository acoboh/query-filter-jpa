package io.github.acoboh.query.filter.jpa.domain;

import io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.jpa.annotations.QFJsonElement;
import io.github.acoboh.query.filter.jpa.model.jsondata.ModelJson;

/**
 * Basic example with JSON types
 * 
 * @author Adrián Cobo
 *
 */
@QFDefinitionClass(ModelJson.class)
public class JsonFilterDef {

	@QFJsonElement(value = "jsonbData")
	private String jsonb;

	@QFJsonElement(value = "jsonbData")
	private String json;

}
