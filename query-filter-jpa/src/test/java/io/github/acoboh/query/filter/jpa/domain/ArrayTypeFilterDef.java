package io.github.acoboh.query.filter.jpa.domain;

import io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.jpa.annotations.QFElement;
import io.github.acoboh.query.filter.jpa.model.PostBlog;

@QFDefinitionClass(PostBlog.class)
public class ArrayTypeFilterDef {

	@QFElement(value = "tags", arrayTyped = true)
	private String tags;

}
