package io.github.acoboh.query.filter.jpa.domain;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import io.github.acoboh.query.filter.jpa.annotations.QFBlockParsing;
import io.github.acoboh.query.filter.jpa.annotations.QFDate;
import io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.jpa.annotations.QFElement;
import io.github.acoboh.query.filter.jpa.annotations.QFSortable;
import io.github.acoboh.query.filter.jpa.model.PostBlog;

/**
 * Basic example of filter
 *
 * @author Adrián Cobo
 */
@QFDefinitionClass(PostBlog.class)
public class FilterBlogDef {

	@QFElement("author")
	private String author;

	@QFElement("likes")
	private int likes;

	@QFElement("avgNote")
	private double avgNote;

	@QFDate
	@QFElement("createDate")
	private LocalDateTime createDate;

	@QFSortable("lastTimestamp")
	private Timestamp lastTimestamp;

	@QFElement("postType")
	private String postType;

	@QFElement("published")
	@QFBlockParsing
	private boolean published;

	@QFElement("comments.author")
	private String commentAuthor;

	@QFElement("comments.likes")
	private String commentLikes;

}
