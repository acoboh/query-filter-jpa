package io.github.acoboh.query.filter.jpa.processor.definitions;

import java.lang.reflect.Field;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import io.github.acoboh.query.filter.jpa.annotations.QFBlockParsing;
import io.github.acoboh.query.filter.jpa.annotations.QFCollectionElement;
import io.github.acoboh.query.filter.jpa.annotations.QFDate;
import io.github.acoboh.query.filter.jpa.annotations.QFDiscriminator;
import io.github.acoboh.query.filter.jpa.annotations.QFElement;
import io.github.acoboh.query.filter.jpa.annotations.QFElements;
import io.github.acoboh.query.filter.jpa.annotations.QFJsonElement;
import io.github.acoboh.query.filter.jpa.annotations.QFSortable;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QFAnnotationsException;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QueryFilterDefinitionException;

/**
 * Abstract class for base definition
 */
public abstract class QFAbstractDefinition {

	private static final Logger LOGGER = LoggerFactory.getLogger(QFAbstractDefinition.class);

	/**
	 * Field to be filtered
	 */
	protected final Field field;

	/**
	 * Filter class
	 */
	protected final Class<?> filterClass;

	/**
	 * Entity class
	 */
	protected final Class<?> entityClass;

	/**
	 * Filter name
	 */
	protected String filterName;

	private final boolean isBlocked;

	QFAbstractDefinition(Field filterField, Class<?> filterClass, Class<?> entityClass, QFBlockParsing blockParsing) {
		Assert.notNull(filterField, "Filter field must not be null");
		Assert.notNull(filterClass, "Filter class must not be null");
		Assert.notNull(entityClass, "Entity class must not be null");

		this.field = filterField;
		this.filterClass = filterClass;
		this.entityClass = entityClass;

		this.filterName = filterField.getName(); // Default value

		this.isBlocked = blockParsing != null;

	}

	/**
	 * Get filter name
	 *
	 * @return filter name
	 */
	public String getFilterName() {
		return filterName;
	}

	/**
	 * Get the entity class
	 *
	 * @return entity class
	 */
	public Class<?> getEntityClass() {
		return entityClass;
	}

	/**
	 * Get the filter class
	 *
	 * @return filter class
	 */
	public Class<?> getFilterClass() {
		return filterClass;
	}

	/**
	 * Get if the field is blocked
	 *
	 * @return true if the field is blocked
	 */
	public boolean isConstructorBlocked() {
		return isBlocked;
	}

	/**
	 * Create a new base definition based on annotations of the field
	 * 
	 * @param filterField filter field
	 * @param filterClass filter class
	 * @param entityClass entity class
	 * @return abstract definition
	 * @throws QueryFilterDefinitionException if any error happens creating the definition
	 */
	public static QFAbstractDefinition buildDefinition(Field filterField, Class<?> filterClass, Class<?> entityClass)
			throws QueryFilterDefinitionException {

		boolean isQFElement = filterField.isAnnotationPresent(QFElement.class)
				|| filterField.isAnnotationPresent(QFElements.class);
		boolean isQFJson = filterField.isAnnotationPresent(QFJsonElement.class);
		boolean isQFDiscriminator = filterField.isAnnotationPresent(QFDiscriminator.class);
		boolean isQFCollection = filterField.isAnnotationPresent(QFCollectionElement.class);
		boolean isQFSortable = filterField.isAnnotationPresent(QFSortable.class);

		long count = Stream.of(isQFElement, isQFJson, isQFDiscriminator, isQFCollection, isQFSortable).filter(e -> e)
				.count();

		if (count == 0) {
			LOGGER.debug("Ignored field {} because it doesn't has annotations");
			return null;
		} else if (count > 1) {
			throw new QFAnnotationsException(filterField, filterClass, isQFElement, isQFJson, isQFDiscriminator,
					isQFCollection, isQFSortable);
		}

		QFBlockParsing blockParsing = filterField.getAnnotation(QFBlockParsing.class);

		if (isQFElement) {
			// Create element definition

			QFElement[] elementAnnotations = filterField.getAnnotationsByType(QFElement.class);
			QFElements elementsAnnotation = filterField.getAnnotation(QFElements.class);
			QFDate dateAnnotation = filterField.getAnnotation(QFDate.class);

			return new QFDefinitionElement(filterField, filterClass, entityClass, blockParsing, elementsAnnotation,
					elementAnnotations, dateAnnotation);

		} else if (isQFJson) {
			// Create json definition
			QFJsonElement jsonAnnotation = filterField.getAnnotation(QFJsonElement.class);
			return new QFDefinitionJson(filterField, filterClass, entityClass, blockParsing, jsonAnnotation);

		} else if (isQFDiscriminator) {
			// Create discriminator definition
			QFDiscriminator discriminatorAnnotation = filterField.getAnnotation(QFDiscriminator.class);
			return new QFDefinitionDiscriminator(filterField, filterClass, entityClass, blockParsing,
					discriminatorAnnotation);

		} else if (isQFCollection) {
			// Create collection definition
			QFCollectionElement collectionAnnotation = filterField.getAnnotation(QFCollectionElement.class);
			return new QFDefinitionCollection(filterField, filterClass, entityClass, blockParsing,
					collectionAnnotation);

		} else if (isQFSortable) {
			QFSortable sortableAnnotation = filterField.getAnnotation(QFSortable.class);
			return new QFDefinitionSortable(filterField, filterClass, entityClass, blockParsing, sortableAnnotation);
		}

		return null;
	}

}
