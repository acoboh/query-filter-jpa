package io.github.acoboh.query.filter.jpa.domain;

import io.github.acoboh.query.filter.jpa.annotations.QFElement;

/**
 * Base filter
 */
public class BaseFilterDef {

    @QFElement("uuid")
    private String uuid;

}
