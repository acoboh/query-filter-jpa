package io.github.acoboh.query.filter.jpa.domain;

import io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.jpa.annotations.QFElement;
import io.github.acoboh.query.filter.jpa.model.extended.NumericEntity;

import java.math.BigDecimal;

@QFDefinitionClass(NumericEntity.class)
public class NumericEntityFilterDef {

    @QFElement("bigDecimal")
    private BigDecimal bigDecimal;

}
