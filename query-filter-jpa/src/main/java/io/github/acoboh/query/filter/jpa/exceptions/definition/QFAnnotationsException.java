package io.github.acoboh.query.filter.jpa.exceptions.definition;

public class QFAnnotationsException extends QueryFilterDefinitionException {

	private static final long serialVersionUID = 1L;

	public QFAnnotationsException() {
		super("Can not define different element annotations on the same field");
	}

}
