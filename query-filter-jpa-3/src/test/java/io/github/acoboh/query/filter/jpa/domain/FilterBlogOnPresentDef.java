package io.github.acoboh.query.filter.jpa.domain;

import io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.jpa.annotations.QFElement;
import io.github.acoboh.query.filter.jpa.annotations.QFOnFilterPresent;
import io.github.acoboh.query.filter.jpa.model.PostBlog;
import io.github.acoboh.query.filter.jpa.operations.QFOperationEnum;

@QFDefinitionClass(PostBlog.class)
public class FilterBlogOnPresentDef {

	@QFElement("author")
	private String author;

	@QFElement("text")
	private String text;

	@QFOnFilterPresent(value = {"author", "text"})
	@QFElement(value = "likes", defaultValues = "10", defaultOperation = QFOperationEnum.GREATER_THAN)
	private int likes;

}
