package io.github.acoboh.query.filter.jpa.domain;

import java.sql.Timestamp;

import io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.jpa.annotations.QFElement;
import io.github.acoboh.query.filter.jpa.annotations.QFPredicate;
import io.github.acoboh.query.filter.jpa.annotations.QFSortable;
import io.github.acoboh.query.filter.jpa.model.PostBlog;

/**
 * Basic example of default sorting with a predicate
 *
 * @author Adrián Cobo
 */
@QFDefinitionClass(value = PostBlog.class, defaultPredicate = "default-predicate")
@QFPredicate(expression = "author OR likes", name = "default-predicate")
public class FilterBlogSortWithPredicateDef {

	@QFElement("author")
	private String author;

	@QFElement("likes")
	private int likes;

	@QFSortable("lastTimestamp")
	private Timestamp lastTimestamp;

}
