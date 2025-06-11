package io.github.acoboh.query.filter.jpa.processor.definitions;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.acoboh.query.filter.jpa.annotations.QFBlockParsing;
import io.github.acoboh.query.filter.jpa.annotations.QFRequired;
import io.github.acoboh.query.filter.jpa.annotations.QFSortable;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QueryFilterDefinitionException;
import io.github.acoboh.query.filter.jpa.processor.QFAttribute;
import io.github.acoboh.query.filter.jpa.processor.definitions.traits.IDefinitionSortable;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.metamodel.Metamodel;

/**
 * Definition for sorable fields
 *
 * @author Adrián Cobo
 */
public class QFDefinitionSortable extends QFAbstractDefinition implements IDefinitionSortable {

	private static final Logger LOGGER = LoggerFactory.getLogger(QFDefinitionSortable.class);

	private final List<List<QFAttribute>> attributes;

	private final boolean autoFetch;

	private final List<String> fullPath;
	private final List<JoinType> joinTypes;

	QFDefinitionSortable(Field filterField, Class<?> filterClass, Class<?> entityClass, QFBlockParsing blockParsing,
			QFRequired required, QFSortable sortableAnnotation, Metamodel metamodel)
			throws QueryFilterDefinitionException {
		super(filterField, filterClass, entityClass, blockParsing, required);

		attributes = new ArrayList<>(1); // Only one path

		FieldClassProcessor fieldClassProcessor = new FieldClassProcessor(entityClass, sortableAnnotation.value(), null,
				null, metamodel);
		attributes.add(fieldClassProcessor.getAttributes());

		autoFetch = sortableAnnotation.autoFetch();

		this.fullPath = Collections.singletonList(sortableAnnotation.value());

		if (sortableAnnotation.joinTypes().length == 0) {
			LOGGER.warn("Join types are empty for field {}. Defaulting to INNER", filterField.getName());
			joinTypes = List.of(JoinType.INNER);
		} else {
			joinTypes = List.of(sortableAnnotation.joinTypes());
		}

	}

	/** {@inheritDoc} */
	@Override
	public List<List<QFAttribute>> getPaths() {
		return attributes;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isSortable() {
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isAutoFetch(int index) {
		return autoFetch;
	}

	/** {@inheritDoc} */
	@Override
	public List<String> getPathField() {
		return fullPath;
	}

	/** {@inheritDoc} */
	@Override
	public List<JoinType> getJoinTypes(int index) {
		return joinTypes;
	}
}
