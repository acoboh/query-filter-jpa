package io.github.acoboh.query.filter.jpa.processor.definitions;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;

import io.github.acoboh.query.filter.jpa.annotations.QFBlockParsing;
import io.github.acoboh.query.filter.jpa.annotations.QFDate;
import io.github.acoboh.query.filter.jpa.annotations.QFElement;
import io.github.acoboh.query.filter.jpa.annotations.QFElements;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QFDateClassNotSupported;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QFDateParseError;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QFElementMultipleClassesException;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QueryFilterDefinitionException;
import io.github.acoboh.query.filter.jpa.predicate.PredicateOperation;
import io.github.acoboh.query.filter.jpa.processor.QFPath;
import io.github.acoboh.query.filter.jpa.processor.definitions.traits.IDefinitionSortable;
import io.github.acoboh.query.filter.jpa.utils.DateUtils;

/**
 * Basic definition for filter fields
 */
public final class QFDefinitionElement extends QFAbstractDefinition implements IDefinitionSortable {

	private static final Logger LOGGER = LoggerFactory.getLogger(QFDefinitionElement.class);

	private final QFElement[] elementAnnotations;
	private final PredicateOperation defaultOperation;

	private final QFDate dateAnnotation;

	private final DateTimeFormatter dateTimeFormatter;

	private final List<List<QFPath>> paths;
	private final List<Class<?>> finalClasses;
	private final List<Boolean> autoFetchPaths;

	// Extra properties
	private final boolean subQuery;
	private final boolean sortable;
	private final boolean caseSensitive;
	private final boolean arrayTyped;
	private final boolean spelExpression;
	private final boolean blankIgnore;

	private final int order;

	QFDefinitionElement(Field filterField, Class<?> filterClass, Class<?> entityClass, QFBlockParsing blockedParsing,
			QFElements elementsAnnotation, QFElement[] elementAnnotations, QFDate dateAnnotation)
			throws QueryFilterDefinitionException {
		super(filterField, filterClass, entityClass, blockedParsing);

		// Element annotations
		this.elementAnnotations = elementAnnotations;

		// Date annotation
		this.dateAnnotation = dateAnnotation;

		if (elementsAnnotation != null) {
			this.defaultOperation = elementsAnnotation.operation();
		} else {
			this.defaultOperation = PredicateOperation.AND;
		}

		// Only if one element and has name override name
		if (elementAnnotations.length == 1 && !elementAnnotations[0].name().isEmpty()) {
			super.filterName = elementAnnotations[0].name();
		} else if (elementAnnotations.length > 1) {
			LOGGER.warn(
					"Multiple element annotations found on field {}. Will ignore name parameter on all annotations and will be the field name {}",
					filterField, getFilterName());
		}

		// Initialize paths and classes
		Pair<List<Class<?>>, List<List<QFPath>>> pairDef = setupPaths(elementAnnotations, filterClass, entityClass);
		this.paths = pairDef.getSecond();
		this.finalClasses = pairDef.getFirst();

		this.autoFetchPaths = Stream.of(elementAnnotations).map(QFElement::autoFetch).toList();

		LOGGER.debug("Checking sortable on element annotations");
		if (elementAnnotations.length != 1) {
			LOGGER.warn(
					"Multiple element annotations checked for all properties. All must be true to be consider true");
		}

		LOGGER.debug("Checking sortable on element annotations");
		if (elementAnnotations.length != 1) {
			LOGGER.warn(
					"Multiple element annotations checked for all properties. All must be true to be consider true");
		}

		sortable = Stream.of(elementAnnotations).allMatch(QFElement::sortable);
		caseSensitive = Stream.of(elementAnnotations).allMatch(QFElement::caseSensitive);
		arrayTyped = Stream.of(elementAnnotations).allMatch(QFElement::arrayTyped);
		spelExpression = Stream.of(elementAnnotations).allMatch(QFElement::isSpPELExpression);
		blankIgnore = Stream.of(elementAnnotations).allMatch(QFElement::blankIgnore);
		order = Stream.of(elementAnnotations).mapToInt(QFElement::order).max().getAsInt();
		subQuery = Stream.of(elementAnnotations).allMatch(QFElement::subquery);

		if (dateAnnotation != null) {
			// Try type
			dateTimeFormatter = checkDateTimeFormatter();
		} else {
			dateTimeFormatter = null;
		}
	}

	private static Pair<List<Class<?>>, List<List<QFPath>>> setupPaths(QFElement[] elementAnnotations,
			Class<?> filterClass, Class<?> entityClass) throws QueryFilterDefinitionException {
		LOGGER.debug("Creating paths for all element annotation. Total {}", elementAnnotations.length);

		List<Class<?>> finalClasses = new ArrayList<>();
		List<List<QFPath>> paths = new ArrayList<>();

		for (QFElement elem : elementAnnotations) {
			LOGGER.trace("Creating paths for element annotation {}", elem);
			Pair<Class<?>, List<QFPath>> pairDef = ClassUtils.getPathsFrom(elem.value(), filterClass, entityClass,
					true);
			finalClasses.add(pairDef.getFirst());
			paths.add(pairDef.getSecond());
		}

		long distinct = paths.stream().map(e -> e.get(e.size() - 1)).map(QFPath::getFieldClass).distinct().count();
		if (distinct != 1) {
			throw new QFElementMultipleClassesException();
		}

		return Pair.of(Collections.unmodifiableList(finalClasses), Collections.unmodifiableList(paths));

	}

	private DateTimeFormatter checkDateTimeFormatter() throws QFDateClassNotSupported, QFDateParseError {
		LocalDateTime now = LocalDateTime.now();

		DateTimeFormatter formatter = DateUtils.getFormatter(dateAnnotation);
		String value = formatter.format(now);

		for (int i = 0; i < finalClasses.size(); i++) {
			try {
				Object parsed = DateUtils.parseDate(formatter, value, finalClasses.get(i), dateAnnotation);
				if (parsed == null) {
					throw new QFDateClassNotSupported(finalClasses.get(i), getFilterName());
				}

			} catch (DateTimeParseException e) {
				throw new QFDateParseError(dateAnnotation.timeFormat(), finalClasses.get(i), e);
			}
		}

		return formatter;

	}

	@Override
	public List<List<QFPath>> getPaths() {
		return paths;
	}

	@Override
	public boolean isAutoFetch(int index) {
		return autoFetchPaths.get(index);
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
	 * Get if the field is case sensitive
	 *
	 * @return true if the field is case sensitive
	 */
	public boolean isCaseSensitive() {
		return caseSensitive;
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
	 * Get the date annotation
	 *
	 * @return date annotation
	 */
	public QFDate getDateAnnotation() {
		return dateAnnotation;
	}

	/**
	 * Get date time formatted created for the field
	 *
	 * @return date time formatted
	 */
	public DateTimeFormatter getDateTimeFormatter() {
		return dateTimeFormatter;
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
	 * Get if the field is array typed
	 *
	 * @return true if the field is array typed
	 */
	public boolean isArrayTyped() {
		return arrayTyped;
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
	 * Get the first final class of the definition element
	 * 
	 * @return first final class located
	 */
	public Class<?> getFirstFinalClass() {
		return finalClasses.get(0);
	}

}
