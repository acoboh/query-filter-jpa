package io.github.acoboh.query.filter.jpa.processor.definitions;

import java.lang.reflect.Field;
import java.util.List;

import org.springframework.data.util.Pair;

import io.github.acoboh.query.filter.jpa.annotations.QFBlockParsing;
import io.github.acoboh.query.filter.jpa.annotations.QFSortable;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QueryFilterDefinitionException;
import io.github.acoboh.query.filter.jpa.processor.QFPath;
import io.github.acoboh.query.filter.jpa.processor.definitions.traits.IDefinitionSortable;

/**
 * Definition for sorable fields
 */
public class QFDefinitionSortable extends QFAbstractDefinition implements IDefinitionSortable {

	private final List<QFPath> paths;

	QFDefinitionSortable(Field filterField, Class<?> filterClass, Class<?> entityClass, QFBlockParsing blockParsing,
			QFSortable sortableAnnotation) throws QueryFilterDefinitionException {
		super(filterField, filterClass, entityClass, blockParsing);

		Pair<Class<?>, List<QFPath>> pairDef = ClassUtils.getPathsFrom(sortableAnnotation.value(), filterClass,
				entityClass, true);
		paths = pairDef.getSecond();

	}

	@Override
	public List<QFPath> getSortPaths() {
		return paths;
	}

	@Override
	public boolean isSortable() {
		return true;
	}

}
