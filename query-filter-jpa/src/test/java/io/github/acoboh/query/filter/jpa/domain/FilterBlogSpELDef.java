package io.github.acoboh.query.filter.jpa.domain;

import io.github.acoboh.query.filter.jpa.annotations.QFBlockParsing;
import io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.jpa.annotations.QFElement;
import io.github.acoboh.query.filter.jpa.model.PostBlog;
import io.github.acoboh.query.filter.jpa.operations.QFOperationEnum;

@QFDefinitionClass(PostBlog.class)
public class FilterBlogSpELDef {

	@QFElement("likes")
	private int likes;

	@QFElement(value = "comments.likes", isSpPELExpression = true, defaultValues = "#likes * 10", defaultOperation = QFOperationEnum.GREATER_THAN, order = 0)
	@QFBlockParsing
	private int commentLikes;

}
