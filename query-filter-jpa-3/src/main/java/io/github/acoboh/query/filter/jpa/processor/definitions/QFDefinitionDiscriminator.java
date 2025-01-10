package io.github.acoboh.query.filter.jpa.processor.definitions;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.acoboh.query.filter.jpa.annotations.QFBlockParsing;
import io.github.acoboh.query.filter.jpa.annotations.QFDiscriminator;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QFDiscriminatorException;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QueryFilterDefinitionException;
import io.github.acoboh.query.filter.jpa.processor.QFPath;

/**
 * Definition for discriminator classes
 */
public class QFDefinitionDiscriminator extends QFAbstractDefinition {

	private final QFDiscriminator discriminatorAnnotation;
	private final List<QFPath> paths;
	private final Class<?> finalClass;

	private final Map<String, Class<?>> discriminatorMap = new HashMap<>();

	QFDefinitionDiscriminator(Field filterField, Class<?> filterClass, Class<?> entityClass,
			QFBlockParsing blockParsing, QFDiscriminator discriminatorAnnotation)
			throws QueryFilterDefinitionException {
		super(filterField, filterClass, entityClass, blockParsing);

		this.discriminatorAnnotation = discriminatorAnnotation;

		if (!discriminatorAnnotation.path().isEmpty()) {
			var fieldClassProcessor = new FieldClassProcessor(entityClass, discriminatorAnnotation.path(), false, null,
					null);
			this.paths = fieldClassProcessor.getPaths();
			this.finalClass = fieldClassProcessor.getFinalClass();
		} else {
			this.paths = Collections.emptyList();
			this.finalClass = entityClass;
		}

		if (!discriminatorAnnotation.name().isEmpty()) {
			super.filterName = discriminatorAnnotation.name();
		}

		for (var value : discriminatorAnnotation.value()) {
			if (discriminatorMap.containsKey(value.name())) {
				throw new QFDiscriminatorException("Duplicate discriminator value name {}", value.name());
			}

			if (!finalClass.isAssignableFrom(value.type())) {
				throw new QFDiscriminatorException("Entity class '{}' is not assignable from value class '{}'",
						finalClass, value.type());
			}

			discriminatorMap.put(value.name(), value.type());
		}

	}

	/**
	 * Get the discriminator annotation
	 *
	 * @return discriminator annotation
	 */
	public QFDiscriminator getDiscriminatorAnnotation() {
		return discriminatorAnnotation;
	}

	/**
	 * Get paths of the discriminator field
	 *
	 * @return paths
	 */
	public List<QFPath> getPaths() {
		return paths;
	}

	/**
	 * Get final entity class
	 *
	 * @return final entity class
	 */
	public Class<?> getFinalClass() {
		return finalClass;
	}

	/**
	 * Get the discriminator map
	 *
	 * @return discriminator map
	 */
	public Map<String, Class<?>> getDiscriminatorMap() {
		return discriminatorMap;
	}

}
