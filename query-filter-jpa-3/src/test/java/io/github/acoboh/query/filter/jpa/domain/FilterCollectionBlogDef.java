package io.github.acoboh.query.filter.jpa.domain;

import io.github.acoboh.query.filter.jpa.annotations.QFCollectionElement;
import io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.jpa.annotations.QFElement;
import io.github.acoboh.query.filter.jpa.model.PostBlog;

/**
 * Basic example of filter
 *
 * @author Adrián Cobo
 */
@QFDefinitionClass(PostBlog.class)
public class FilterCollectionBlogDef {

    @QFElement("author")
    private String author;

    @QFCollectionElement("comments")
    private int commentsSize;

    @QFElement("comments.author")
    private String authorComments;

}
