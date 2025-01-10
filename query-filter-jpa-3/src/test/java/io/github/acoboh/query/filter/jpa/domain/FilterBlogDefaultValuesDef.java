package io.github.acoboh.query.filter.jpa.domain;

import io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.jpa.annotations.QFElement;
import io.github.acoboh.query.filter.jpa.model.PostBlog;

/**
 * Basic example of default values
 */
@QFDefinitionClass(PostBlog.class)
public class FilterBlogDefaultValuesDef {

    @QFElement(value = "author", defaultValues = "Author 1")
    private String author;

}
