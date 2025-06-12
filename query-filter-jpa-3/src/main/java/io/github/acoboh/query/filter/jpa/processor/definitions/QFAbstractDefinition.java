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
import io.github.acoboh.query.filter.jpa.annotations.QFOnFilterPresent;
import io.github.acoboh.query.filter.jpa.annotations.QFRequired;
import io.github.acoboh.query.filter.jpa.annotations.QFSortable;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QFAnnotationsException;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QueryFilterDefinitionException;
import jakarta.persistence.metamodel.Metamodel;

/**
 * Abstract class for base definition
 *
 * @author Adrián Cobo
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
	private final boolean isRequiredStringFilter;
	private final boolean isRequiredExecution;
	private final boolean isRequiredSort;

	QFAbstractDefinition(Field filterField, Class<?> filterClass, Class<?> entityClass, QFBlockParsing blockParsing,
			QFRequired required) {
		Assert.notNull(filterField, "Filter field must not be null");
		Assert.notNull(filterClass, "Filter class must not be null");
		Assert.notNull(entityClass, "Entity class must not be null");

		this.field = filterField;
		this.filterClass = filterClass;
		this.entityClass = entityClass;

		this.filterName = filterField.getName(); // Default value

		this.isBlocked = blockParsing != null;

		this.isRequiredStringFilter = required != null && required.onStringFilter();
		this.isRequiredExecution = required != null && required.onExecution();
		this.isRequiredSort = required != null && required.onSort();

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
	 * Get if the field is required on string filter phase
	 * 
	 * @return true if the field is required on string filter phase
	 */
	public boolean isRequiredStringFilter() {
		return isRequiredStringFilter;
	}

	/**
	 * Get if the field is required on execution phase
	 *
	 * @return true if the field is required on execution phase
	 */
	public boolean isRequiredExecution() {
		return isRequiredExecution;
	}

	/**
	 * Get if the field is required on sort phase
	 *
	 * @return true if the field is required on sort phase
	 */
	public boolean isRequiredSort() {
		return isRequiredSort;
	}

	/**
	 * Create a new base definition based on annotations of the field
	 *
	 * @param filterField
	 *            filter field
	 * @param filterClass
	 *            filter class
	 * @param entityClass
	 *            entity class
	 * @return abstract definition
	 * @throws io.github.acoboh.query.filter.jpa.exceptions.definition.QueryFilterDefinitionException
	 *             if any error happens creating the definition
	 * @param metamodel
	 *            a {@link jakarta.persistence.metamodel.Metamodel} object
	 */
	public static QFAbstractDefinition buildDefinition(Field filterField, Class<?> filterClass, Class<?> entityClass,
			Metamodel metamodel) throws QueryFilterDefinitionException {

		boolean isQFElement = filterField.isAnnotationPresent(QFElement.class)
				|| filterField.isAnnotationPresent(QFElements.class);
		boolean isQFJson = filterField.isAnnotationPresent(QFJsonElement.class);
		boolean isQFDiscriminator = filterField.isAnnotationPresent(QFDiscriminator.class);
		boolean isQFCollection = filterField.isAnnotationPresent(QFCollectionElement.class);
		boolean isQFSortable = filterField.isAnnotationPresent(QFSortable.class);

		long count = Stream.of(isQFElement, isQFJson, isQFDiscriminator, isQFCollection, isQFSortable).filter(e -> e)
				.count();

		if (count == 0) {
			LOGGER.debug("Ignored field {} because it doesn't has annotations", filterField);
			return null;
		} else if (count > 1) {
			throw new QFAnnotationsException(filterField, filterClass, isQFElement, isQFJson, isQFDiscriminator,
					isQFCollection, isQFSortable);
		}

		QFBlockParsing blockParsing = filterField.getAnnotation(QFBlockParsing.class);
		QFRequired required = filterField.getAnnotation(QFRequired.class);

		if (isQFElement) {
			// Create element definition

			QFElement[] elementAnnotations = filterField.getAnnotationsByType(QFElement.class);
			QFElements elementsAnnotation = filterField.getAnnotation(QFElements.class);
			QFDate dateAnnotation = filterField.getAnnotation(QFDate.class);
			QFOnFilterPresent onFilterPresentAnnotation = filterField.getAnnotation(QFOnFilterPresent.class);

			return new QFDefinitionElement(filterField, filterClass, entityClass, blockParsing, required,
					elementsAnnotation, elementAnnotations, dateAnnotation, onFilterPresentAnnotation, metamodel);

		} else if (isQFJson) {
			// Create json definition
			QFJsonElement jsonAnnotation = filterField.getAnnotation(QFJsonElement.class);
			return new QFDefinitionJson(filterField, filterClass, entityClass, blockParsing, required, jsonAnnotation,
					metamodel);

		} else if (isQFDiscriminator) {
			// Create discriminator definition
			QFDiscriminator discriminatorAnnotation = filterField.getAnnotation(QFDiscriminator.class);
			return new QFDefinitionDiscriminator(filterField, filterClass, entityClass, blockParsing, required,
					discriminatorAnnotation, metamodel);

		} else if (isQFCollection) {
			// Create collection definition
			QFCollectionElement collectionAnnotation = filterField.getAnnotation(QFCollectionElement.class);
			return new QFDefinitionCollection(filterField, filterClass, entityClass, blockParsing, required,
					collectionAnnotation, metamodel);

		} else if (isQFSortable) {
			QFSortable sortableAnnotation = filterField.getAnnotation(QFSortable.class);
			return new QFDefinitionSortable(filterField, filterClass, entityClass, blockParsing, required,
					sortableAnnotation, metamodel);
		}

		return null;
	}

}
