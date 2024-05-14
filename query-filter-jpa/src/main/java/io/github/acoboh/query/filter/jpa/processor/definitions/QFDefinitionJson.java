package io.github.acoboh.query.filter.jpa.processor.definitions;

import java.lang.reflect.Field;
import java.util.List;

import javax.persistence.Column;

import io.github.acoboh.query.filter.jpa.annotations.QFBlockParsing;
import io.github.acoboh.query.filter.jpa.annotations.QFJsonElement;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QFJsonException;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QueryFilterDefinitionException;
import io.github.acoboh.query.filter.jpa.processor.QFPath;

/**
 * Definition for JSON filter fields
 */
public class QFDefinitionJson extends QFAbstractDefinition {

	private final QFJsonElement jsonAnnotation;
	private final List<QFPath> paths;

	QFDefinitionJson(Field filterField, Class<?> filterClass, Class<?> entityClass, QFBlockParsing blockParsing,
			QFJsonElement jsonAnnotation) throws QueryFilterDefinitionException {
		super(filterField, filterClass, entityClass, blockParsing);
		this.jsonAnnotation = jsonAnnotation;

		// Add json filter name
		if (!jsonAnnotation.name().isEmpty()) {
			super.filterName = jsonAnnotation.name();
		}

		FieldClassProcessor fieldClassProcessor = new FieldClassProcessor(entityClass, jsonAnnotation.value(), false);

		this.paths = fieldClassProcessor.getPaths();

		QFPath last = paths.get(paths.size() - 1);
		if (!last.getField().isAnnotationPresent(Column.class)) {
			throw new QFJsonException("@Column annotation not found on the json element field {}", last.getField());
		}

		Column column = last.getField().getAnnotation(Column.class);
		if (!column.columnDefinition().toLowerCase().startsWith("jsonb")) {
			throw new QFJsonException(
					"QFJsonElement annotations are only supported on colums of type 'jsonb'. Actual type is {}",
					column.columnDefinition());
		}

		last.setFinal(true);

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
	public List<QFPath> getPaths() {
		return paths;
	}

}
