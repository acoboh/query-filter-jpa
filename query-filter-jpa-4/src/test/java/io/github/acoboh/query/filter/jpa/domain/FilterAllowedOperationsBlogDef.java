package io.github.acoboh.query.filter.jpa.domain;

import io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.jpa.annotations.QFElement;
import io.github.acoboh.query.filter.jpa.model.PostBlog;
import io.github.acoboh.query.filter.jpa.operations.QFOperationEnum;

@QFDefinitionClass(PostBlog.class)
public class FilterAllowedOperationsBlogDef {

	@QFElement(value = "author", allowedOperations = {QFOperationEnum.EQUAL, QFOperationEnum.STARTS_WITH,
			QFOperationEnum.ENDS_WITH, QFOperationEnum.LIKE})
	private String author;

}
