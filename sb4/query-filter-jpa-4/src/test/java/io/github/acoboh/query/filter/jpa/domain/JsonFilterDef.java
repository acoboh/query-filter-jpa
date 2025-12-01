package io.github.acoboh.query.filter.jpa.domain;

import io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.jpa.annotations.QFJsonElement;
import io.github.acoboh.query.filter.jpa.model.jsondata.ModelJson;

/**
 * Basic example with JSON types
 *
 * @author Adrián Cobo
 */
@QFDefinitionClass(ModelJson.class)
public class JsonFilterDef {

    @QFJsonElement("jsonbData")
    private String jsonb;

    @QFJsonElement("jsonbData")
    private String json;

}
