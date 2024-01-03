package io.github.acoboh.query.filter.jpa.processor.definitions;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import org.springframework.data.util.Pair;

import io.github.acoboh.query.filter.jpa.annotations.QFBlockParsing;
import io.github.acoboh.query.filter.jpa.annotations.QFCollectionElement;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QFCollectionNotSupported;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QueryFilterDefinitionException;
import io.github.acoboh.query.filter.jpa.processor.QFPath;
import io.github.acoboh.query.filter.jpa.processor.QFPath.QueryFilterElementDefType;

/**
 * Definition for collection filter field
 */
public class QFDefinitionCollection extends QFAbstractDefinition {

	private static final List<QueryFilterElementDefType> allowedTypes = Arrays.asList(QueryFilterElementDefType.LIST,
			QueryFilterElementDefType.SET);

	private final List<QFPath> paths;

	QFDefinitionCollection(Field filterField, Class<?> filterClass, Class<?> entityClass, QFBlockParsing blockParsing,
			QFCollectionElement collectionElement) throws QueryFilterDefinitionException {
		super(filterField, filterClass, entityClass, blockParsing);

		Pair<Class<?>, List<QFPath>> pairDef = ClassUtils.getPathsFrom(collectionElement.value(), filterClass,
				entityClass, false);
		this.paths = pairDef.getSecond();

		if (!collectionElement.name().isEmpty()) {
			super.filterName = collectionElement.name();
		}

		QFPath finalPath = paths.get(paths.size() - 1);
		if (!allowedTypes.contains(finalPath.getType())) {
			throw new QFCollectionNotSupported(filterName, filterClass, finalPath.getType());
		}

		// Force final
		finalPath.setFinal(true);

	}

	/**
	 * Get definition paths of the collection
	 * 
	 * @return paths
	 */
	public List<QFPath> getPaths() {
		return paths;
	}

}
