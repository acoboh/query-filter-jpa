package io.github.acoboh.query.filter.jpa.processor.definitions;

import java.lang.reflect.Field;
import java.util.List;

import io.github.acoboh.query.filter.jpa.annotations.QFBlockParsing;
import io.github.acoboh.query.filter.jpa.annotations.QFJsonElement;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QFJsonException;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QueryFilterDefinitionException;
import io.github.acoboh.query.filter.jpa.processor.QFAttribute;
import io.github.acoboh.query.filter.jpa.processor.QFPath;
import jakarta.persistence.Column;
import jakarta.persistence.metamodel.Metamodel;

/**
 * Definition for JSON filter fields
 */
public class QFDefinitionJson extends QFAbstractDefinition {

	private final QFJsonElement jsonAnnotation;
	private final List<QFAttribute> attributes;

	QFDefinitionJson(Field filterField, Class<?> filterClass, Class<?> entityClass, QFBlockParsing blockParsing,
			QFJsonElement jsonAnnotation, Metamodel metamodel) throws QueryFilterDefinitionException {
		super(filterField, filterClass, entityClass, blockParsing);
		this.jsonAnnotation = jsonAnnotation;

		// Add json filter name
		if (!jsonAnnotation.name().isEmpty()) {
			super.filterName = jsonAnnotation.name();
		}

		var fieldClassProcessor = new FieldClassProcessor(entityClass, jsonAnnotation.value(), false, null, null,
				metamodel);

		this.attributes = fieldClassProcessor.getAttributes();

		var last = attributes.get(attributes.size() - 1);
		// TODO Fix
//		if (!last.getField().isAnnotationPresent(Column.class)) {
//			throw new QFJsonException("@Column annotation not found on the json element field {}", last.getField());
//		}

//		Column column = last.getField().getAnnotation(Column.class);
//		if (!column.columnDefinition().toLowerCase().startsWith("jsonb")) {
//			throw new QFJsonException(
//					"QFJsonElement annotations are only supported on colums of type 'jsonb'. Actual type is {}",
//					column.columnDefinition());
//		}

//		last.setFinal(true);

	}

	/**
	 * Get if the field is case sensitive
	 *
	 * @return true if the field is case sensitive
	 */
	public boolean isCaseSensitive() {
		return jsonAnnotation.caseSensitive();
	}

	/**
	 * Get definition paths
	 *
	 * @return paths
	 */
	public List<QFAttribute> getAttributes() {
		return attributes;
	}

}
