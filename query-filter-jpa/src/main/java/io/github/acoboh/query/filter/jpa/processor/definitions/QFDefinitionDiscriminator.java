package io.github.acoboh.query.filter.jpa.processor.definitions;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import io.github.acoboh.query.filter.jpa.annotations.QFBlockParsing;
import io.github.acoboh.query.filter.jpa.annotations.QFDiscriminator;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QueryFilterDefinitionException;
import io.github.acoboh.query.filter.jpa.processor.QFPath;

/**
 * Definition for discriminator classes
 */
public class QFDefinitionDiscriminator extends QFAbstractDefinition {

	private final QFDiscriminator discriminatorAnnotation;
	private final List<QFPath> paths;

	QFDefinitionDiscriminator(Field filterField, Class<?> filterClass, Class<?> entityClass,
			QFBlockParsing blockParsing, QFDiscriminator discriminatorAnnotation)
			throws QueryFilterDefinitionException {
		super(filterField, filterClass, entityClass, blockParsing);

		this.discriminatorAnnotation = discriminatorAnnotation;

		if (!discriminatorAnnotation.path().isEmpty()) {

			FieldClassProcessor fieldClassProcessor = new FieldClassProcessor(entityClass,
					discriminatorAnnotation.path(), true);
			this.paths = fieldClassProcessor.getPaths();

		} else {
			this.paths = Collections.emptyList();
		}

		if (!discriminatorAnnotation.name().isEmpty()) {
			super.filterName = discriminatorAnnotation.name();
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

}
