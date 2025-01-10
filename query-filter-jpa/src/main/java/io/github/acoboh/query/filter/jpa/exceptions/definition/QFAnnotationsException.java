package io.github.acoboh.query.filter.jpa.exceptions.definition;

import java.lang.reflect.Field;

/**
 * Exception thrown when the same element contains multiple type annotations
 *
 * @author Adrián Cobo
 */
public class QFAnnotationsException extends QueryFilterDefinitionException {

	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor
	 *
	 * @param field
	 *            Field with multiple annotations
	 * @param filterClass
	 *            Filter class
	 * @param isQFElement
	 *            true if is QFElement
	 * @param isQFJson
	 *            true if is QFJson
	 * @param isQFDiscriminator
	 *            true if is QFDiscriminator
	 * @param isQFCollection
	 *            true if is QFCollection
	 * @param isQFSortable
	 *            true if is QFSortable
	 */
	public QFAnnotationsException(Field field, Class<?> filterClass, boolean isQFElement, boolean isQFJson,
			boolean isQFDiscriminator, boolean isQFCollection, boolean isQFSortable) {
		super("Can not define different element annotations on the same field {} on class {}. QFElement? {}, QFJsonElement? {}, QFDiscriminator? {}, QFCollectionElement? {}, QFSortable? {}",
				field, filterClass, isQFElement, isQFJson, isQFDiscriminator, isQFCollection, isQFSortable);
	}

}
