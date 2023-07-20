package io.github.acoboh.query.filter.jpa.exceptions.definition;

import io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass;

/**
 * Exception thrown when no {@link QFDefinitionClass} annotation is present
 *
 * @author Architecture Team
 */
public class QFClassException extends QueryFilterDefinitionException {

	private static final long serialVersionUID = 1L;
	private static final String MESSAGE = "Annotation {} is not present in {}";
	private static final String CLASS = "Unexpected class {} in annotation on class {}. Expected {}";

	public QFClassException(Class<?> ann, String cs) {
		super(MESSAGE, ann, cs);
	}

	public QFClassException(Class<?> clazz, Class<?> annotation, Class<?> expected) {
		super(CLASS, clazz, annotation, expected);
	}
}
