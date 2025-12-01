package io.github.acoboh.query.filter.jpa.processor.definitions;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;

import io.github.acoboh.query.filter.jpa.annotations.QFDate;
import io.github.acoboh.query.filter.jpa.annotations.QFElement;
import io.github.acoboh.query.filter.jpa.annotations.QFElements;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QFDateClassNotSupported;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QFDateParseError;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QFElementMultipleClassesException;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QueryFilterDefinitionException;
import io.github.acoboh.query.filter.jpa.operations.QFOperationEnum;
import io.github.acoboh.query.filter.jpa.predicate.PredicateOperation;
import io.github.acoboh.query.filter.jpa.processor.QFAttribute;
import io.github.acoboh.query.filter.jpa.processor.QFSpecificationPart;
import io.github.acoboh.query.filter.jpa.processor.definitions.traits.IDefinitionSortable;
import io.github.acoboh.query.filter.jpa.processor.match.QFElementMatch;
import io.github.acoboh.query.filter.jpa.utils.DateUtils;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.metamodel.Metamodel;

/**
 * Basic definition for filter fields
 *
 * @author Adrián Cobo
 */
public final class QFDefinitionElement extends QFAbstractDefinition implements IDefinitionSortable {

	private static final Logger LOGGER = LoggerFactory.getLogger(QFDefinitionElement.class);

	private final QFElement[] elementAnnotations;
	private final PredicateOperation defaultOperation;

	private final QFDate dateAnnotation;

	private final Set<String> onFilterPresentFilters;

	private final DateTimeFormatter dateTimeFormatter;

	private final List<List<QFAttribute>> paths;
	private final List<List<JoinType>> joinTypes;
	private final Set<QFOperationEnum> allowedOperations;
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

	QFDefinitionElement(FilterFieldInfo filterInfo, QFElements elementsAnnotation, QFElement[] elementAnnotations,
			QFDate dateAnnotation, Metamodel metamodel) throws QueryFilterDefinitionException {
		super(filterInfo);

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
					filterInfo.field(), getFilterName());
		}

		// Initialize paths and classes
		Pair<List<Class<?>>, List<List<QFAttribute>>> pairDef = setupPaths(elementAnnotations, filterInfo.entityClass(),
				metamodel);
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
		order = Stream.of(elementAnnotations).mapToInt(QFElement::order).max().orElse(0);
		subQuery = Stream.of(elementAnnotations).allMatch(QFElement::subquery);
		joinTypes = Stream.of(elementAnnotations).map(e -> List.of(e.joinTypes())).toList();

		if (elementsAnnotation != null) {
			allowedOperations = Set.of(elementsAnnotation.allowedOperations());
		} else {
			if (elementAnnotations.length == 1) {
				allowedOperations = Set.of(elementAnnotations[0].allowedOperations());
			} else {
				if (Stream.of(elementAnnotations).anyMatch(e -> e.allowedOperations().length > 0)) {
					LOGGER.error(
							"If multiple element annotations are used, the allowed operations must be defined in the QFElements annotation");
					throw new QueryFilterDefinitionException(
							"Allowed operations must be defined in the QFElements annotation for multiple element annotations");
				}

				LOGGER.debug("Multiple element annotations found. Will use default allowed operations");
				allowedOperations = Set.of();
			}
		}

		if (!allowedOperations.isEmpty()) {
			// Check if all operations are valid
			for (var finalClazz : finalClasses) {
				Set<QFOperationEnum> operations = QFOperationEnum.getOperationsOfClass(finalClazz, arrayTyped);
				if (allowedOperations.stream().anyMatch(e -> !operations.contains(e))) {
					LOGGER.error("Allowed operations {} not valid for class {}", allowedOperations, finalClazz);
					throw new QueryFilterDefinitionException(
							"Allowed operations " + allowedOperations + " not valid for class " + finalClazz
									+ " on field " + getFilterName() + " of filter " + filterInfo.filterClass());
				}
			}
		}

		if (joinTypes.stream().anyMatch(List::isEmpty)) {
			LOGGER.warn("Join types not defined. Will use default join type");
		}

		var onFilterPresent = filterInfo.onFilterPresent();
		if (onFilterPresent != null) {
			if (onFilterPresent.value().length == 0) {
				LOGGER.error("On filter present annotation must have at least one filter link");
				throw new QueryFilterDefinitionException(
						"On filter present annotation must have at least one filter link");
			}
			var count = Stream.of(elementAnnotations).filter(e -> e.defaultValues().length > 0).count();
			if (count == 0) {
				LOGGER.warn("On filter present annotation must have default values");
				throw new QueryFilterDefinitionException("On filter present annotation must have default values");
			}
			onFilterPresentFilters = Set.of(onFilterPresent.value());
		} else {
			onFilterPresentFilters = null;
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
			var fieldClassProcessor = new FieldClassProcessor(entityClass, elem.value(), elem.subClassMapping(),
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

	/** {@inheritDoc} */
	@Override
	public List<List<QFAttribute>> getPaths() {
		return paths;
	}

	/** {@inheritDoc} */
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
	 * Get if the field is case-sensitive
	 *
	 * @return true if the field is case-sensitive
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

	@Override
	public Set<String> getOnFilterPresentFilters() {
		return onFilterPresentFilters;
	}

	/**
	 * Get order of evaluation
	 *
	 * @return order of evaluation
	 */
	public int getOrder() {
		return order;
	}

	@Override
	public boolean hasDefaultValues() {
		return Stream.of(elementAnnotations).anyMatch(e -> e.defaultValues().length > 0);
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

	/** {@inheritDoc} */
	@Override
	public List<JoinType> getJoinTypes(int index) {
		if (index < 0 || index >= joinTypes.size()) {
			LOGGER.warn("Index {} out of bounds. Using default join type", index);
			return List.of(JoinType.INNER);
		}
		return joinTypes.get(index);
	}

	/** {@inheritDoc} */
	@Override
	public List<String> getPathField() {
		return fullPath;
	}

	/**
	 * Get if the operation is allowed
	 *
	 * @param operation
	 *            to check
	 * @return a boolean true if the operation is allowed
	 * @since 1.0.0
	 */
	public boolean isOperationAllowed(QFOperationEnum operation) {
		return allowedOperations.isEmpty() || allowedOperations.contains(operation);
	}

	/**
	 * Get the allowed operations
	 *
	 * @return a set of allowed operations
	 * @since 1.0.0
	 */
	public Set<QFOperationEnum> getRealAllowedOperations() {
		if (allowedOperations.isEmpty()) {
			return QFOperationEnum.getOperationsOfClass(finalClasses.get(0), arrayTyped);
		}
		return allowedOperations;
	}

	/** {@inheritDoc} */
	@Override
	protected List<QFSpecificationPart> getInnerDefaultValues() {

		List<QFSpecificationPart> matches = new ArrayList<>();

		for (var elem : elementAnnotations) {
			if (elem.defaultValues().length > 0) {
				LOGGER.trace("OnFilterPresent values found for element annotation {}. Will add to matches", elem);
				matches.add(new QFElementMatch(Arrays.asList(elem.defaultValues()), elem.defaultOperation(), this));
			}
		}
		LOGGER.debug("OnFilterPresent values found for element annotations {}. Will add to matches", matches.size());
		return matches;
	}

}
