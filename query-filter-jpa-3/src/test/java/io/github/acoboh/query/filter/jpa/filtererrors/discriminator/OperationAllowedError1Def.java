package io.github.acoboh.query.filter.jpa.filtererrors.discriminator;

import io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.jpa.annotations.QFElement;
import io.github.acoboh.query.filter.jpa.model.PostBlog;
import io.github.acoboh.query.filter.jpa.operations.QFOperationEnum;

@QFDefinitionClass(PostBlog.class)
public class OperationAllowedError1Def {

	@QFElement(value = "likes", allowedOperations = QFOperationEnum.ENDS_WITH)
	private int likes;

}
