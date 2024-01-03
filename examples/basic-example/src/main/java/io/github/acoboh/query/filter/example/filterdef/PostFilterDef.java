package io.github.acoboh.query.filter.example.filterdef;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import io.github.acoboh.query.filter.example.entities.PostBlog;
import io.github.acoboh.query.filter.jpa.annotations.QFBlockParsing;
import io.github.acoboh.query.filter.jpa.annotations.QFDate;
import io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.jpa.annotations.QFElement;
import io.github.acoboh.query.filter.jpa.annotations.QFSortable;

@QFDefinitionClass(PostBlog.class)
public class PostFilterDef {

	@QFElement("title")
	private String title;

	@QFElement("likes")
	private int likes;

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

}
