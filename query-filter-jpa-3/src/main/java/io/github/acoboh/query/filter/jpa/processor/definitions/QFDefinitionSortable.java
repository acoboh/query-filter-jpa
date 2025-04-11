package io.github.acoboh.query.filter.jpa.processor.definitions;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.acoboh.query.filter.jpa.annotations.QFBlockParsing;
import io.github.acoboh.query.filter.jpa.annotations.QFSortable;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QueryFilterDefinitionException;
import io.github.acoboh.query.filter.jpa.processor.QFAttribute;
import io.github.acoboh.query.filter.jpa.processor.definitions.traits.IDefinitionSortable;
import jakarta.persistence.metamodel.Metamodel;

/**
 * Definition for sorable fields
 */
public class QFDefinitionSortable extends QFAbstractDefinition implements IDefinitionSortable {

	private final List<List<QFAttribute>> attributes;

	private final boolean autoFetch;

	private final List<String> fullPath;

	QFDefinitionSortable(Field filterField, Class<?> filterClass, Class<?> entityClass, QFBlockParsing blockParsing,
			QFSortable sortableAnnotation, Metamodel metamodel) throws QueryFilterDefinitionException {
		super(filterField, filterClass, entityClass, blockParsing);

		attributes = new ArrayList<>(1); // Only one path

		FieldClassProcessor fieldClassProcessor = new FieldClassProcessor(entityClass, sortableAnnotation.value(), true,
				null, null, metamodel);
		attributes.add(fieldClassProcessor.getAttributes());

		autoFetch = sortableAnnotation.autoFetch();

		this.fullPath = Collections.singletonList(sortableAnnotation.value());

	}

	@Override
	public List<List<QFAttribute>> getPaths() {
		return attributes;
	}

	@Override
	public boolean isSortable() {
		return true;
	}

	@Override
	public boolean isAutoFetch(int index) {
		return autoFetch;
	}

	@Override
	public List<String> getPathField() {
		return fullPath;
	}
}
