package io.github.acoboh.query.filter.jpa.exceptions.definition;

import java.lang.reflect.Field;

/**
 * Exception thrown when the same element contains multiple type annotations
 *
 * @author Adrián Cobo
 * 
 */
public class QFAnnotationsException extends QueryFilterDefinitionException {

	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor
	 */
	public QFAnnotationsException(Field field, Class<?> filterClass, boolean isQFElement, boolean isQFJson,
			boolean isQFDiscriminator, boolean isQFCollection, boolean isQFSortable) {
		super("Can not define different element annotations on the same field {} on class {}. QFElement? {}, QFJsonElement? {}, QFDiscriminator? {}, QFCollectionElement? {}, QFSortable? {}",
				field, filterClass, isQFElement, isQFJson, isQFDiscriminator, isQFCollection, isQFSortable);
	}

}
