package io.github.acoboh.query.filter.jpa.exceptions.definition;

/**
 * Exception thrown when no {@link io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass} annotation is present
 *
 * @author Architecture Team
 * @version $Id: $Id
 */
public class QFClassException extends QueryFilterDefinitionException {

	private static final long serialVersionUID = 1L;
	private static final String MESSAGE = "Annotation {} is not present in {}";
	private static final String CLASS = "Unexpected class {} in annotation on class {}. Expected {}";

	/**
	 * Default constructor
	 *
	 * @param ann expected annotation
	 * @param cs  location of the expected annotation
	 */
	public QFClassException(Class<?> ann, String cs) {
		super(MESSAGE, ann, cs);
	}

	/**
	 * Annotation has unexpected class
	 *
	 * @param clazz      Unexpected class
	 * @param annotation annotation Annotation of class
	 * @param expected   expected class
	 */
	public QFClassException(Class<?> clazz, Class<?> annotation, Class<?> expected) {
		super(CLASS, clazz, annotation, expected);
	}
}
