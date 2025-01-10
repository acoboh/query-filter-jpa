package io.github.acoboh.query.filter.jpa.domain;

import io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass.QFDefaultSort;
import io.github.acoboh.query.filter.jpa.annotations.QFElement;
import io.github.acoboh.query.filter.jpa.annotations.QFSortable;
import io.github.acoboh.query.filter.jpa.model.PostBlog;

import java.sql.Timestamp;

/**
 * Basic example of default sorting
 *
 * @author Adrián Cobo
 */
@QFDefinitionClass(value = PostBlog.class, defaultSort = @QFDefaultSort("author"))
public class FilterBlogDefaulltSortADef {

    @QFElement("author")
    private String author;

    @QFElement("likes")
    private int likes;

    @QFSortable("lastTimestamp")
    private Timestamp lastTimestamp;

}
