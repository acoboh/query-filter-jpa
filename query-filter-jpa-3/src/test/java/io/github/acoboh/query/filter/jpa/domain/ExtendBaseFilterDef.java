package io.github.acoboh.query.filter.jpa.domain;

import io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.jpa.annotations.QFElement;
import io.github.acoboh.query.filter.jpa.model.PostBlog;

/**
 * Extended class
 */
@QFDefinitionClass(PostBlog.class)
public class ExtendBaseFilterDef extends BaseFilterDef {

    @QFElement("author")
    private String author;

}
