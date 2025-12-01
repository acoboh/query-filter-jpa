package io.github.acoboh.query.filter.jpa.domain;

import io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.jpa.annotations.QFElement;
import io.github.acoboh.query.filter.jpa.model.PostBlog;
import jakarta.persistence.criteria.JoinType;

@QFDefinitionClass(PostBlog.class)
public class FilterBlogJoinType {

	@QFElement(value = "comments.author", joinTypes = JoinType.LEFT)
	private String commentAuthor;
}
