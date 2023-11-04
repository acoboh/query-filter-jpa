package io.github.acoboh.query.filter.jpa.processor;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import io.github.acoboh.query.filter.jpa.annotations.QFBlockParsing;
import io.github.acoboh.query.filter.jpa.annotations.QFDate;
import io.github.acoboh.query.filter.jpa.annotations.QFDiscriminator;
import io.github.acoboh.query.filter.jpa.annotations.QFElement;
import io.github.acoboh.query.filter.jpa.annotations.QFElements;
import io.github.acoboh.query.filter.jpa.annotations.QFJsonElement;
import io.github.acoboh.query.filter.jpa.annotations.QFSortable;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QFAnnotationsException;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QFDateClassNotSupported;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QFDateParseError;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QFDiscriminatorException;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QFElementException;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QFElementMultipleClassesException;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QFFieldLevelException;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QFJsonException;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QFMissingFieldException;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QueryFilterDefinitionException;
import io.github.acoboh.query.filter.jpa.predicate.PredicateOperation;
import io.github.acoboh.query.filter.jpa.processor.QFPath.QueryFilterElementDefType;
import io.github.acoboh.query.filter.jpa.utils.DateUtils;

/**
 * Class with all the information of any field to be filtered
 *
 * @author Adrián Cobo
 * 
 */
public class QFDefinition {

	private static final Logger LOGGER = LoggerFactory.getLogger(QFDefinition.class);

	private final Class<?> filterClass;

	private final Class<?> entityClass;
	private final QFBlockParsing blockParsingAnnotation;

	private final QFElement[] elementAnnotations;
	private final QFElements elementsAnnotation;
	private PredicateOperation defaultOperation;

	private final QFDate dateAnnotation;
	private DateTimeFormatter dateTimeFormatter;

	private final QFDiscriminator discriminatorAnnotation;
	private final QFSortable sortableAnnotation;
	private final QFJsonElement jsonAnnotation;

	private final List<List<QFPath>> paths = new ArrayList<>();

	private Class<?> finalClass;

	private String filterName;

	private boolean subQuery = false;

	private boolean sortable = false;

	private boolean caseSensitive = false;

	private boolean arrayTyped = false;

	private boolean spelExpression = false;

	private boolean blankIgnore = true;

	private int order = Integer.MIN_VALUE;

	QFDefinition(Field filterField, Class<?> filterClass, Class<?> entityClass) throws QueryFilterDefinitionException {

		Assert.notNull(filterField, "Field must not be null");

		this.filterClass = filterClass;
		this.entityClass = entityClass;

		filterName = filterField.getName();

		blockParsingAnnotation = filterField.getAnnotation(QFBlockParsing.class);
		elementAnnotations = filterField.getAnnotationsByType(QFElement.class);
		dateAnnotation = filterField.getAnnotation(QFDate.class);
		discriminatorAnnotation = filterField.getAnnotation(QFDiscriminator.class);
		jsonAnnotation = filterField.getAnnotation(QFJsonElement.class);
		elementsAnnotation = filterField.getAnnotation(QFElements.class);
		sortableAnnotation = filterField.getAnnotation(QFSortable.class);

		if (elementsAnnotation != null) {
			this.defaultOperation = elementsAnnotation.operation();
		} else {
			this.defaultOperation = PredicateOperation.AND;
		}

		// Only if one element and has name override name
		if (elementAnnotations.length == 1 && !elementAnnotations[0].name().isEmpty()) {
			filterName = elementAnnotations[0].name();
		} else if (elementAnnotations.length > 1) {
			LOGGER.warn(
					"Multiple element annotations found on field {}. Will ignore name parameter on all annotations and will be the field name {}",
					filterField, filterName);
		}

		if (elementAnnotations.length > 1 && Stream.of(elementAnnotations).anyMatch(QFElement::subquery)) {
			LOGGER.warn("Multiple annotations with array levels. All array levels will be ignored");
		} else if (elementAnnotations.length > 0) {
			subQuery = elementAnnotations[0].subquery();
		}

		// Check only discriminator
		if (discriminatorAnnotation != null && elementAnnotations.length > 0) {
			throw new QFAnnotationsException();
		}

		// Add discriminator filter name
		if (discriminatorAnnotation != null && !discriminatorAnnotation.name().isEmpty()) {
			this.filterName = discriminatorAnnotation.name();
		}

		// Check only json
		if (jsonAnnotation != null && (discriminatorAnnotation != null || elementAnnotations.length > 0)) {
			throw new QFAnnotationsException();
		}

		// Add json filter name
		if (jsonAnnotation != null && !jsonAnnotation.name().isEmpty()) {
			filterName = jsonAnnotation.name();
		}

		setupPaths(entityClass);

		if (arrayTyped) {
			sortable = false;
		}

		if (sortable) {
			LOGGER.trace("Checking sortable on collections"); // Limited by JPA
			if (paths.stream().flatMap(Collection::stream).anyMatch(e -> e.getType() == QueryFilterElementDefType.LIST
					|| e.getType() == QueryFilterElementDefType.SET)) {
				LOGGER.warn("Disabled sortable on field {} for class {}", filterName, filterClass);
				sortable = false;
			}
		}

		if (dateAnnotation != null) {
			// Try type
			dateTimeFormatter = checkDateTimeFormatter();
		}

	}

	private void setupPaths(Class<?> entityClass) throws QueryFilterDefinitionException {

		// Setup all the paths of elements annotations
		if (elementAnnotations != null && elementAnnotations.length > 0) {
			LOGGER.debug("Creating paths for all element annotation. Total {}", elementAnnotations.length);
			for (QFElement elem : elementAnnotations) {
				LOGGER.trace("Creating paths for element annotation {}", elem);
				paths.add(getPathsFrom(elem.value(), entityClass, true));
			}

			long distinct = paths.stream().map(e -> e.get(e.size() - 1)).map(QFPath::getFieldClass).distinct().count();
			if (distinct != 1) {
				throw new QFElementMultipleClassesException();
			}

			LOGGER.debug("Checking sortable on element annotations");
			if (elementAnnotations.length != 1) {
				LOGGER.warn(
						"Multiple element annotations checked for sortable annotations. If any is false, all will be false");
			}
			sortable = Stream.of(elementAnnotations).allMatch(QFElement::sortable);

			if (elementAnnotations.length != 1) {
				LOGGER.warn(
						"Multiple element annotations checked for isCaseSensitive. All must be true to be consider true");
			}

			caseSensitive = Stream.of(elementAnnotations).allMatch(QFElement::caseSensitive);

			arrayTyped = Stream.of(elementAnnotations).allMatch(QFElement::arrayTyped);

			spelExpression = Stream.of(elementAnnotations).allMatch(QFElement::isSpPELExpression);

			blankIgnore = Stream.of(elementAnnotations).allMatch(QFElement::blankIgnore);

			order = Stream.of(elementAnnotations).mapToInt(QFElement::order).max().getAsInt();

			return;
		}

		// Check paths for only sortable annotation
		if (sortableAnnotation != null) {
			paths.add(getPathsFrom(sortableAnnotation.value(), entityClass, true));
			sortable = true;
			return;
		}

		// Discriminator annotations
		if (discriminatorAnnotation != null && !discriminatorAnnotation.path().isEmpty()) {
			paths.add(getPathsFrom(discriminatorAnnotation.path(), entityClass, false));
		} else if (discriminatorAnnotation != null) {
			finalClass = entityClass;
		}

		if (discriminatorAnnotation != null) {

			// Check class is discriminator
			if (finalClass.getAnnotation(DiscriminatorColumn.class) == null) {
				throw new QFDiscriminatorException("Discriminator annotation not found on class {}", finalClass);
			}

			// Check all discriminator annotations type
			for (QFDiscriminator.Value discriminatorValue : discriminatorAnnotation.value()) {
				if (!finalClass.isAssignableFrom(discriminatorValue.type())) {
					throw new QFDiscriminatorException(
							"Discriminator option {} is not subclass of discriminator type {}",
							discriminatorValue.type(), finalClass);
				}

				// Check discriminator type
				if (discriminatorValue.type().getAnnotation(DiscriminatorValue.class) == null) {
					throw new QFDiscriminatorException("Discriminator option {} without DiscriminatorValue annotation",
							discriminatorValue.type());
				}
			}
		}

		// Json annotation path
		if (jsonAnnotation != null) {
			LOGGER.debug("Creating paths for JSON element");
			List<QFPath> pathsJson = getPathsFrom(jsonAnnotation.value(), entityClass, false);

			QFPath last = pathsJson.get(pathsJson.size() - 1);
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
			paths.add(pathsJson);
			caseSensitive = jsonAnnotation.caseSensitive();
		}

	}

	private List<QFPath> getPathsFrom(String pathField, Class<?> entityClass, boolean isEndObject)
			throws QueryFilterDefinitionException {

		Assert.notNull(pathField, "Path field cannot be null");

		List<QFPath> paths = new ArrayList<>();

		String[] splitLevel = pathField.split("\\.");
		if (splitLevel.length == 0) {
			throw new QFElementException(pathField, entityClass);
		}

		Field fieldObject = ClassUtils.getDeclaredFieldSuperclass(entityClass, splitLevel[0]);
		if (fieldObject == null) {
			throw new QFMissingFieldException(pathField, filterClass);
		}

		int firstDot = pathField.indexOf('.');
		String nextLevel = firstDot == -1 ? "" : pathField.substring(firstDot + 1);

		finalClass = ClassUtils.checkAbstractObject(pathField, splitLevel[0], nextLevel, fieldObject,
				fieldObject.getType(), paths, isEndObject);

		if (!paths.get(paths.size() - 1).isFinal() && discriminatorAnnotation == null && jsonAnnotation == null) {
			throw new QFFieldLevelException(pathField, nextLevel);
		}

		return paths;

	}

	private DateTimeFormatter checkDateTimeFormatter() throws QFDateClassNotSupported, QFDateParseError {
		LocalDateTime now = LocalDateTime.now();

		DateTimeFormatter formatter = DateUtils.getFormatter(dateAnnotation);

		String value = formatter.format(now);

		try {

			Object parsed = DateUtils.parseDate(formatter, value, finalClass, dateAnnotation);
			if (parsed == null) {
				throw new QFDateClassNotSupported(finalClass, filterName);
			}

		} catch (DateTimeParseException e) {
			throw new QFDateParseError(dateAnnotation.timeFormat(), finalClass, e);
		}

		return formatter;

	}

	/**
	 * Get if the element annotations are present
	 *
	 * @return true if the element has element annotations, false otherwise
	 */
	public boolean isElementFilter() {
		return elementAnnotations != null && elementAnnotations.length > 0;
	}

	/**
	 * Get if the discriminator annotation is present
	 *
	 * @return true if the discriminator annotation is present
	 */
	public boolean isDiscriminatorFilter() {
		return discriminatorAnnotation != null;
	}

	/**
	 * Get if the field is blocked
	 *
	 * @return true if the field is blocked
	 */
	public boolean isConstructorBlocked() {
		return blockParsingAnnotation != null;
	}

	/**
	 * Get if the field is json type
	 *
	 * @return true if the field is json type
	 */
	public boolean isJsonElementFilter() {
		return jsonAnnotation != null;
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
	 * Get the entity class
	 *
	 * @return entity class
	 */
	public Class<?> getEntityClass() {
		return entityClass;
	}

	/**
	 * Get all the element annotations
	 *
	 * @return element annotations
	 */
	public QFElement[] getElementAnnotations() {
		return elementAnnotations;
	}

	/**
	 * Get the date annotation
	 *
	 * @return date annotation
	 */
	public QFDate getDateAnnotation() {
		return dateAnnotation;
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
	 * Get list of all paths for each element annotation
	 *
	 * @return list of all paths for each element annotation
	 */
	public List<List<QFPath>> getPaths() {
		return paths;
	}

	/**
	 * Get final class of last instrospection analysis
	 *
	 * @return final class
	 */
	public Class<?> getFinalClass() {
		return finalClass;
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
	 * Get if the field must be subqueried
	 *
	 * @return true if the field is subquery
	 */
	public boolean isSubQuery() {
		return subQuery;
	}

	/**
	 * Get if the field is sortable
	 *
	 * @return true if sortable
	 */
	public boolean isSortable() {
		return sortable;
	}

	/**
	 * Get if the field is case sensitive
	 *
	 * @return true if the field is case sensitive
	 */
	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	/**
	 * Get if the field is array typed
	 *
	 * @return true if the field is array typed
	 */
	public boolean isArrayTyped() {
		return arrayTyped;
	}

	/**
	 * Get if the field has spel expressions
	 *
	 * @return true if the field has spel
	 */
	public boolean isSpelExpression() {
		return spelExpression;
	}

	/**
	 * Get if with blank values must be ignored
	 *
	 * @return true if with blank values must be ignored
	 */
	public boolean isBlankIgnore() {
		return blankIgnore;
	}

	/**
	 * Get order of evaluation
	 *
	 * @return order of evaluation
	 */
	public int getOrder() {
		return order;
	}

	/**
	 * Get predicate operation with multiple element annotations
	 *
	 * @return predicate operation
	 */
	public PredicateOperation getPredicateOperation() {
		return defaultOperation;
	}

	/**
	 * Get date time formatted created for the field
	 *
	 * @return date time formatted
	 */
	public DateTimeFormatter getDateTimeFormatter() {
		return dateTimeFormatter;
	}

}
