package io.github.acoboh.query.filter.jpa.processor.definitions;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import io.github.acoboh.query.filter.jpa.annotations.QFBlockParsing;
import io.github.acoboh.query.filter.jpa.annotations.QFCollectionElement;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QFCollectionNotSupported;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QueryFilterDefinitionException;
import io.github.acoboh.query.filter.jpa.processor.QFAttribute;
import io.github.acoboh.query.filter.jpa.processor.QFPath;
import io.github.acoboh.query.filter.jpa.processor.QFPath.QFElementDefType;
import jakarta.persistence.metamodel.Metamodel;

/**
 * Definition for collection filter field
 */
public class QFDefinitionCollection extends QFAbstractDefinition {

	private static final List<QFElementDefType> allowedTypes = Arrays.asList(QFElementDefType.LIST,
			QFElementDefType.SET);

	private final List<QFAttribute> attributes;

	QFDefinitionCollection(Field filterField, Class<?> filterClass, Class<?> entityClass, QFBlockParsing blockParsing,
			QFCollectionElement collectionElement, Metamodel metamodel) throws QueryFilterDefinitionException {
		super(filterField, filterClass, entityClass, blockParsing);

		var fieldClassProcessor = new FieldClassProcessor(entityClass, collectionElement.value(), false,
				collectionElement.subClassMapping(), collectionElement.subClassMappingPath(), metamodel);

		this.attributes = fieldClassProcessor.getAttributes();

		if (!collectionElement.name().isEmpty()) {
			super.filterName = collectionElement.name();
		}

		if (!Collection.class.isAssignableFrom(fieldClassProcessor.getFinalClass())) {
			throw new QFCollectionNotSupported(filterName, filterClass, fieldClassProcessor.getFinalClass());
		}

	}

	/**
	 * Get definition paths of the collection
	 *
	 * @return paths
	 */
	public List<QFAttribute> getPaths() {
		return attributes;
	}

}
