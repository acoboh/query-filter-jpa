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
import io.github.acoboh.query.filter.jpa.processor.QFAttribute;
import io.github.acoboh.query.filter.jpa.processor.definitions.traits.IDefinitionSortable;
import io.github.acoboh.query.filter.jpa.utils.DateUtils;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.metamodel.Metamodel;

/**
 * Basic definition for filter fields
 */
public final class QFDefinitionElement extends QFAbstractDefinition implements IDefinitionSortable {

	private static final Logger LOGGER = LoggerFactory.getLogger(QFDefinitionElement.class);

	private final QFElement[] elementAnnotations;
	private final PredicateOperation defaultOperation;

	private final QFDate dateAnnotation;

	private final DateTimeFormatter dateTimeFormatter;

	private final List<List<QFAttribute>> paths;
	private final List<List<JoinType>> joinTypes;
	private final List<Class<?>> finalClasses;
	private final List<Boolean> autoFetchPaths;

	private final List<String> fullPath;

	// Extra properties
	private final boolean subQuery;
	private final boolean sortable;
	private final boolean caseSensitive;
	private final boolean arrayTyped;
	private final boolean spelExpression;
	private final boolean blankIgnore;

	private final int order;

	QFDefinitionElement(Field filterField, Class<?> filterClass, Class<?> entityClass, QFBlockParsing blockedParsing,
			QFElements elementsAnnotation, QFElement[] elementAnnotations, QFDate dateAnnotation, Metamodel metamodel)
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
		Pair<List<Class<?>>, List<List<QFAttribute>>> pairDef = setupPaths(elementAnnotations, entityClass, metamodel);
		this.paths = pairDef.getSecond();
		this.finalClasses = pairDef.getFirst();

		this.autoFetchPaths = Stream.of(elementAnnotations).map(QFElement::autoFetch).toList();

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
		order = Stream.of(elementAnnotations).mapToInt(QFElement::order).max().orElseGet(() -> 0);
		subQuery = Stream.of(elementAnnotations).allMatch(QFElement::subquery);
		joinTypes = Stream.of(elementAnnotations).map(e -> List.of(e.joinTypes())).toList();

		if (joinTypes.stream().anyMatch(List::isEmpty)) {
			LOGGER.warn("Join types not defined. Will use default join type");
		}

		if (dateAnnotation != null) {
			// Try type
			dateTimeFormatter = checkDateTimeFormatter();
		} else {
			dateTimeFormatter = null;
		}

		this.fullPath = Stream.of(elementAnnotations).map(QFElement::value).toList();
	}

	private static Pair<List<Class<?>>, List<List<QFAttribute>>> setupPaths(QFElement[] elementAnnotations,
			Class<?> entityClass, Metamodel metamodel) throws QueryFilterDefinitionException {
		LOGGER.debug("Creating paths for all element annotation. Total {}", elementAnnotations.length);

		List<Class<?>> finalClasses = new ArrayList<>();
		List<List<QFAttribute>> paths = new ArrayList<>();

		for (QFElement elem : elementAnnotations) {
			LOGGER.trace("Creating paths for element annotation {}", elem);
			var fieldClassProcessor = new FieldClassProcessor(entityClass, elem.value(), true, elem.subClassMapping(),
					elem.subClassMappingPath(), metamodel);

			List<QFAttribute> attributes = fieldClassProcessor.getAttributes();
			paths.add(attributes);

			finalClasses.add(fieldClassProcessor.getFinalClass());
		}

		long distinct = finalClasses.stream().distinct().count();
		if (distinct != 1) {
			throw new QFElementMultipleClassesException();
		}

		return Pair.of(Collections.unmodifiableList(finalClasses), Collections.unmodifiableList(paths));

	}

	private DateTimeFormatter checkDateTimeFormatter() throws QFDateClassNotSupported, QFDateParseError {
		LocalDateTime now = LocalDateTime.now();

		DateTimeFormatter formatter = DateUtils.getFormatter(dateAnnotation);
		String value = formatter.format(now);

		for (Class<?> finalClass : finalClasses) {
			try {
				Object parsed = DateUtils.parseDate(formatter, value, finalClass, dateAnnotation);
				if (parsed == null) {
					throw new QFDateClassNotSupported(finalClass, getFilterName());
				}

			} catch (DateTimeParseException e) {
				throw new QFDateParseError(dateAnnotation.timeFormat(), finalClass, e);
			}
		}

		return formatter;

	}

	@Override
	public List<List<QFAttribute>> getPaths() {
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

	@Override
	public List<JoinType> getJoinTypes(int index) {
		if (index < 0 || index >= joinTypes.size()) {
			LOGGER.warn("Index {} out of bounds. Using default join type", index);
			return List.of(JoinType.INNER);
		}
		return joinTypes.get(index);
	}

	@Override
	public List<String> getPathField() {
		return fullPath;
	}

}
