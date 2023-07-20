package io.github.acoboh.query.filter.jpa.domain;

import io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.jpa.annotations.QFElement;
import io.github.acoboh.query.filter.jpa.annotations.QFElements;
import io.github.acoboh.query.filter.jpa.model.PostBlog;
import io.github.acoboh.query.filter.jpa.predicate.PredicateOperation;

@QFDefinitionClass(PostBlog.class)
public class FilterBlogMultipleElementsDef {

	// @formatter:off
	@QFElements(operation = PredicateOperation.OR, value = { 
			@QFElement("author"),
			@QFElement("text") 
		}
	)
	// @formatter:on
	private String multipleOr;

	// @formatter:off
	@QFElements(operation = PredicateOperation.AND, value = { 
			@QFElement("author"),
			@QFElement("text") 
		}
	)
	// @formatter:on
	private String multipleAnd;

	@QFElement("author")
	@QFElement("text")
	private String multipleDefault;

}
