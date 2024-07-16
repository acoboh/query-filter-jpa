package io.github.acoboh.query.filter.jpa.processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.util.Pair;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.google.common.collect.Sets;

import io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.jpa.exceptions.QFBlockException;
import io.github.acoboh.query.filter.jpa.exceptions.QFDiscriminatorNotFoundException;
import io.github.acoboh.query.filter.jpa.exceptions.QFFieldNotFoundException;
import io.github.acoboh.query.filter.jpa.exceptions.QFJsonParseException;
import io.github.acoboh.query.filter.jpa.exceptions.QFMultipleSortException;
import io.github.acoboh.query.filter.jpa.exceptions.QFNotSortableException;
import io.github.acoboh.query.filter.jpa.exceptions.QFNotValuable;
import io.github.acoboh.query.filter.jpa.exceptions.QFOperationNotFoundException;
import io.github.acoboh.query.filter.jpa.exceptions.QFParseException;
import io.github.acoboh.query.filter.jpa.operations.QFCollectionOperationEnum;
import io.github.acoboh.query.filter.jpa.operations.QFOperationEnum;
import io.github.acoboh.query.filter.jpa.operations.QFOperationJsonEnum;
import io.github.acoboh.query.filter.jpa.predicate.PredicateProcessorResolutor;
import io.github.acoboh.query.filter.jpa.processor.definitions.QFAbstractDefinition;
import io.github.acoboh.query.filter.jpa.processor.definitions.QFDefinitionCollection;
import io.github.acoboh.query.filter.jpa.processor.definitions.QFDefinitionDiscriminator;
import io.github.acoboh.query.filter.jpa.processor.definitions.QFDefinitionElement;
import io.github.acoboh.query.filter.jpa.processor.definitions.QFDefinitionJson;
import io.github.acoboh.query.filter.jpa.processor.definitions.traits.IDefinitionSortable;
import io.github.acoboh.query.filter.jpa.processor.match.QFCollectionMatch;
import io.github.acoboh.query.filter.jpa.processor.match.QFDiscriminatorMatch;
import io.github.acoboh.query.filter.jpa.processor.match.QFElementMatch;
import io.github.acoboh.query.filter.jpa.processor.match.QFJsonElementMatch;
import io.github.acoboh.query.filter.jpa.spel.SpelResolverContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

/**
 * Class that implements {@linkplain Specification} from JPA library that allows the user to create automatic filters from
 * {@linkplain QFParamType#RHS_COLON} or {@linkplain QFParamType#LHS_BRACKETS} standards
 *
 * @author Adrián Cobo
 * @param <E> Entity model class
 * 
 */
public class QueryFilter<E> implements Specification<E> {

	private static final Logger LOGGER = LoggerFactory.getLogger(QueryFilter.class);

	private static final String FIELD_NOT_NULL_MESSAGE = "field cannot be null";
	private static final String OPERATION_NOT_NULL_MESSAGE = "operation cannot be null";
	private static final String VALUES_NOT_NULL_MESSAGE = "values cannot be null";

	private static final long serialVersionUID = 1L;

	private static final Pattern REGEX_PATTERN = Pattern.compile("([+-])([a-zA-Z0-9]+)");

	private final String initialInput;

	private final QFSpecificationsWarp specificationsWarp;

	private final transient @Nullable Map<String, PredicateProcessorResolutor> predicateMap;

	private final transient List<Pair<IDefinitionSortable, Direction>> defaultSorting;

	private final transient Map<String, QFAbstractDefinition> definitionMap;

	private final transient QFDefinitionClass queryFilterClassAnnotation;

	private boolean defaultSortEnabled = true;

	private final Class<E> entityClass;
	private final Class<?> predicateClass;
	private final boolean distinct;
	private final transient SpelResolverContext spelResolver;
	private final transient List<Pair<IDefinitionSortable, Direction>> sortDefinitionList = new ArrayList<>();
	private boolean isConstructor = true;

	private @Nullable String predicateName;
	private transient @Nullable PredicateProcessorResolutor predicate;

	/**
	 * Construtor from query filter processor
	 * 
	 * @param input     Input of filter
	 * @param type      Type of filter
	 * @param processor query filter processor
	 */
	protected QueryFilter(String input, QFParamType type, QFProcessor<?, E> processor) {
		Assert.notNull(type, "type cannot be null");

		this.definitionMap = processor.getDefinitionMap();
		this.queryFilterClassAnnotation = processor.getDefinitionClassAnnotation();

		this.specificationsWarp = new QFSpecificationsWarp(processor.getDefaultMatches());

		this.defaultSorting = processor.getDefaultSorting();
		this.entityClass = processor.getEntityClass();
		this.predicateClass = processor.getFilterClass();
		this.spelResolver = processor.getApplicationContext().getBean(SpelResolverContext.class);
		this.predicateMap = processor.getPredicateMap();
		this.predicateName = processor.getDefaultPredicate();

		if (this.predicateName != null) {
			this.predicate = predicateMap.get(this.predicateName);
		}

		this.distinct = queryFilterClassAnnotation.distinct();

		this.initialInput = input != null ? input : "";

		if (input != null && !input.isEmpty()) {

			var matcher = type.getPattern().matcher(input);

			while (matcher.find()) {
				if (LOGGER.isTraceEnabled()) {
					LOGGER.trace("Processing part {}", matcher.group());
				}

				if (matcher.group(1) != null) {
					parseValuePart(matcher.group(2), matcher.group(3), matcher.group(4));
				} else if (matcher.group(5) != null) {
					parseSortPart(matcher.group(6));
				} else {
					throw new QFParseException(matcher.group(), input);
				}

			}

		}

		isConstructor = false;
	}

	/**
	 * Get the entity class
	 *
	 * @return entity model class
	 */
	public Class<E> getEntityClass() {
		return entityClass;
	}

	/**
	 * Get the predicate definition class
	 *
	 * @return predicate definition class
	 */
	public Class<?> getPredicateClass() {
		return predicateClass;
	}

	private void parseValuePart(String field, String op, String value)
			throws QFParseException, QFFieldNotFoundException, QFOperationNotFoundException,
			QFDiscriminatorNotFoundException, QFBlockException, QFJsonParseException, QFNotValuable {

		QFAbstractDefinition def = definitionMap.get(field);
		if (def == null) {
			throw new QFFieldNotFoundException(field);
		}

		if (def.isConstructorBlocked() && isConstructor) {
			throw new QFBlockException(field);
		}

		QFSpecificationPart qfSpecificationPart;
		if (def instanceof QFDefinitionElement qdef) {
			qfSpecificationPart = new QFElementMatch(Arrays.asList(value.split(",")), QFOperationEnum.fromValue(op),
					qdef);
		} else if (def instanceof QFDefinitionDiscriminator qdef) {
			qfSpecificationPart = new QFDiscriminatorMatch(Arrays.asList(value.split(",")), qdef);

		} else if (def instanceof QFDefinitionJson qdef) {
			qfSpecificationPart = new QFJsonElementMatch(value, QFOperationJsonEnum.fromValue(op), qdef);

		} else if (def instanceof QFDefinitionCollection qdef) {
			qfSpecificationPart = new QFCollectionMatch(qdef, QFCollectionOperationEnum.fromValue(op),
					Integer.valueOf(value));
		} else {
			throw new QFNotValuable(field);
		}

		specificationsWarp.addSpecification(qfSpecificationPart);

	}

	private void parseSortPart(String values)
			throws QFParseException, QFNotSortableException, QFMultipleSortException, QFFieldNotFoundException {

		Matcher matcher = REGEX_PATTERN.matcher(values);

		while (matcher.find()) {

			String order = matcher.group(1);
			String fieldName = matcher.group(2);

			QFAbstractDefinition def = definitionMap.get(fieldName);
			if (def == null) {
				throw new QFFieldNotFoundException(fieldName);
			}

			if (!(def instanceof IDefinitionSortable)) {
				throw new QFNotSortableException(fieldName);
			}

			if (this.sortDefinitionList.stream().anyMatch(e -> e.getFirst().getFilterName().equals(fieldName))) {
				throw new QFMultipleSortException(fieldName);
			}

			Direction dir;
			if (order.equals("+")) {
				dir = Direction.ASC;
			} else {
				dir = Direction.DESC;
			}

			Pair<IDefinitionSortable, Direction> pair = Pair.of((IDefinitionSortable) def, dir);
			this.sortDefinitionList.add(pair);
			this.defaultSortEnabled = false;

		}

	}

	/**
	 * Get the input used on the constructor
	 * 
	 * @return original input
	 */
	public String getInitialInput() {
		return initialInput;
	}

	/**
	 * Manually adds a new operation on any field
	 *
	 * @param field     field of filter
	 * @param operation operation to be applied
	 * @param values    list of values
	 * @throws io.github.acoboh.query.filter.jpa.exceptions.QFFieldNotFoundException         if the field is not found
	 * @throws io.github.acoboh.query.filter.jpa.exceptions.QFDiscriminatorNotFoundException if the discriminator is not allowed
	 * @see #addNewField(String, QFOperationEnum, String)
	 */
	public void addNewField(String field, QFOperationEnum operation, List<String> values)
			throws QFFieldNotFoundException, QFDiscriminatorNotFoundException {

		Assert.notNull(field, FIELD_NOT_NULL_MESSAGE);
		Assert.notNull(operation, OPERATION_NOT_NULL_MESSAGE);
		Assert.notNull(values, VALUES_NOT_NULL_MESSAGE);

		QFAbstractDefinition def = definitionMap.get(field);
		if (def == null) {
			throw new QFFieldNotFoundException(field);
		}

		QFSpecificationPart qfSpecificationPart;
		if (def instanceof QFDefinitionElement qdef) {
			qfSpecificationPart = new QFElementMatch(values, operation, qdef);
		} else if (def instanceof QFDefinitionDiscriminator qdef) {
			qfSpecificationPart = new QFDiscriminatorMatch(values, qdef);
		} else {
			throw new QFNotValuable(field);
		}

		specificationsWarp.addSpecification(qfSpecificationPart);

	}

	/**
	 * Manually adds a new operation on any JSON field
	 * 
	 * @param field     field to filter
	 * @param operation operation to be applied
	 * @param value     json value as string
	 */
	public void addNewField(String field, QFOperationJsonEnum operation, String value) {
		Assert.notNull(field, FIELD_NOT_NULL_MESSAGE);
		Assert.notNull(operation, OPERATION_NOT_NULL_MESSAGE);
		Assert.notNull(value, VALUES_NOT_NULL_MESSAGE);

		QFAbstractDefinition def = definitionMap.get(field);
		if (def == null) {
			throw new QFFieldNotFoundException(field);
		}

		if (!(def instanceof QFDefinitionJson)) {
			throw new QFNotValuable(field);
		}

		QFJsonElementMatch match = new QFJsonElementMatch(value, operation, (QFDefinitionJson) def);
		specificationsWarp.addSpecification(match);

	}

	/**
	 * Add a new field for collection operations
	 * 
	 * @param field     filter field name
	 * @param operation operation
	 * @param value     value
	 */
	public void addNewField(String field, QFCollectionOperationEnum operation, int value) {

		Assert.notNull(field, FIELD_NOT_NULL_MESSAGE);
		Assert.notNull(operation, OPERATION_NOT_NULL_MESSAGE);

		QFAbstractDefinition def = definitionMap.get(field);
		if (def == null) {
			throw new QFFieldNotFoundException(field);
		}

		if (!(def instanceof QFDefinitionCollection)) {
			throw new QFNotValuable(field);
		}

		QFCollectionMatch match = new QFCollectionMatch((QFDefinitionCollection) def, operation, value);
		specificationsWarp.addSpecification(match);

	}

	/**
	 * Manually adds a new operation on any field
	 *
	 * @param field     field of filter
	 * @param operation operation to be applied
	 * @param value     value to match. Can be multiple values joined by a ',' (comma)
	 * @throws io.github.acoboh.query.filter.jpa.exceptions.QFFieldNotFoundException         if the field is not found
	 * @throws io.github.acoboh.query.filter.jpa.exceptions.QFDiscriminatorNotFoundException if the discriminator is not allowed
	 */
	public void addNewField(String field, QFOperationEnum operation, String value)
			throws QFFieldNotFoundException, QFDiscriminatorNotFoundException {

		Assert.notNull(field, FIELD_NOT_NULL_MESSAGE);
		Assert.notNull(operation, OPERATION_NOT_NULL_MESSAGE);
		Assert.notNull(value, VALUES_NOT_NULL_MESSAGE);

		List<String> values = Arrays.asList(value.split(","));
		addNewField(field, operation, values);

	}

	/**
	 * Override sort configuration
	 *
	 * @param field     Field name of sorting
	 * @param direction Direction of sorting
	 * @throws io.github.acoboh.query.filter.jpa.exceptions.QFNotSortableException   not sortable
	 * @throws io.github.acoboh.query.filter.jpa.exceptions.QFFieldNotFoundException if field does not exist
	 * @throws io.github.acoboh.query.filter.jpa.exceptions.QFMultipleSortException  if multiple sort exists on the same field
	 */
	public void addSortBy(String field, Direction direction)
			throws QFFieldNotFoundException, QFNotSortableException, QFMultipleSortException {

		QFAbstractDefinition def = definitionMap.get(field);
		if (def == null) {
			throw new QFFieldNotFoundException(field);
		}

		if (!(def instanceof IDefinitionSortable)) {
			throw new QFNotSortableException(field);
		}

		if (this.sortDefinitionList.stream().anyMatch(e -> e.getFirst().getFilterName().equals(field))) {
			throw new QFMultipleSortException(field);
		}

		Pair<IDefinitionSortable, Direction> pair = Pair.of((IDefinitionSortable) def, direction);
		this.sortDefinitionList.add(pair);
		this.defaultSortEnabled = false;

	}

	/**
	 * Remove the actual sort configuration
	 */
	public void clearSort() {
		this.defaultSortEnabled = false;
		this.sortDefinitionList.clear();
	}

	/**
	 * Get if any sort filter is applied
	 *
	 * @return true if any sort is applied, false otherwise
	 */
	public boolean isSorted() {
		var list = defaultSortEnabled ? defaultSorting : sortDefinitionList;
		return !list.isEmpty();
	}

	/**
	 * Get all the sort fields
	 *
	 * @return list of a pair of sorting fields
	 */
	public List<Pair<String, Direction>> getSortFields() {
		var sortList = defaultSortEnabled ? defaultSorting : sortDefinitionList;
		return sortList.stream().map(e -> Pair.of(e.getFirst().getFilterName(), e.getSecond())).toList();
	}

	/**
	 * Get all the sort fields with full path
	 * <p>
	 * This is useful to use {@linkplain org.springframework.data.domain.Pageable} with
	 * {@linkplain org.springframework.data.domain.Sort}
	 *
	 * @return list of a pair of sorting fields
	 */
	public List<Pair<String, Direction>> getSortFieldWithFullPath() {
		List<Pair<IDefinitionSortable, Direction>> list = defaultSortEnabled ? defaultSorting : sortDefinitionList;

		List<Pair<String, Direction>> ret = new ArrayList<>();
		for (var pair : list) {
			for (var path : pair.getFirst().getPathField()) {
				ret.add(Pair.of(path, pair.getSecond()));
			}
		}

		return ret;

	}

	/**
	 * Get if the filter is sorted by the selected field
	 *
	 * @param field field to check
	 * @return true if is actually sorting, false otherwise
	 */
	public boolean isSortedBy(String field) {
		var sortList = defaultSortEnabled ? defaultSorting : sortDefinitionList;
		return sortList.stream().anyMatch(e -> e.getFirst().getFilterName().equals(field));
	}

	/**
	 * Check if the field is currently used for filtering
	 *
	 * @param field Filter name to check
	 * @return true if the field is present, false otherwise
	 */
	public boolean isFiltering(String field) {
		return specificationsWarp.getAllParts().stream().anyMatch(e -> e.getDefinition().getFilterName().equals(field));
	}

	/**
	 * Check if any of the fields is are currently used for filtering
	 *
	 * @param fields Filter names to be checked
	 * @return true, if any of the fields are present, false is all of them are actually missing
	 */
	public boolean isFilteringAny(String... fields) {
		Set<String> set = Sets.newHashSet(fields);
		return specificationsWarp.getAllParts().stream().anyMatch(e -> set.contains(e.getDefinition().getFilterName()));
	}

	/**
	 * Override any field. If not present, a new field will be created
	 *
	 * @param field     Field filter name
	 * @param operation Operation to apply
	 * @param value     value of filter
	 * @throws io.github.acoboh.query.filter.jpa.exceptions.QFFieldNotFoundException         Missing field exception
	 * @throws io.github.acoboh.query.filter.jpa.exceptions.QFDiscriminatorNotFoundException if the discriminator value is not allowed
	 * @throws io.github.acoboh.query.filter.jpa.exceptions.QFJsonParseException             if any json parse exception
	 */
	public void overrideField(String field, QFOperationEnum operation, String value)
			throws QFFieldNotFoundException, QFDiscriminatorNotFoundException, QFJsonParseException {

		Assert.notNull(field, FIELD_NOT_NULL_MESSAGE);
		Assert.notNull(operation, OPERATION_NOT_NULL_MESSAGE);
		Assert.notNull(value, VALUES_NOT_NULL_MESSAGE);

		QFAbstractDefinition def = definitionMap.get(field);
		if (def == null) {
			throw new QFFieldNotFoundException(field);
		}

		if (def instanceof QFDefinitionElement qdef) {
			specificationsWarp.deleteSpecificationField(field);
			QFElementMatch match = new QFElementMatch(Arrays.asList(value.split(",")), operation, qdef);
			specificationsWarp.addSpecification(match);
		} else if (def instanceof QFDefinitionDiscriminator qdef) {
			specificationsWarp.deleteSpecificationField(field);
			QFDiscriminatorMatch match = new QFDiscriminatorMatch(Arrays.asList(value.split(",")), qdef);
			specificationsWarp.addSpecification(match);
		} else {
			throw new QFNotValuable(field);
		}

	}

	/**
	 * Override any JSON field. If not present, a new field will be created
	 *
	 * @param field         Field filter name
	 * @param operationJson Operation to apply
	 * @param value         value of filter
	 * @throws io.github.acoboh.query.filter.jpa.exceptions.QFFieldNotFoundException Missing field exception
	 * @throws io.github.acoboh.query.filter.jpa.exceptions.QFJsonParseException     if any json parse exception
	 * @throws io.github.acoboh.query.filter.jpa.exceptions.QFNotValuable            if the field is not valuable or type compatible
	 */
	public void overrideField(String field, QFOperationJsonEnum operationJson, String value)
			throws QFFieldNotFoundException, QFNotValuable {

		Assert.notNull(field, FIELD_NOT_NULL_MESSAGE);
		Assert.notNull(operationJson, OPERATION_NOT_NULL_MESSAGE);
		Assert.notNull(value, VALUES_NOT_NULL_MESSAGE);

		QFAbstractDefinition def = definitionMap.get(field);
		if (def == null) {
			throw new QFFieldNotFoundException(field);
		}

		if (!(def instanceof QFDefinitionJson)) {
			throw new QFNotValuable(field);
		}

		specificationsWarp.deleteSpecificationField(field);
		QFJsonElementMatch match = new QFJsonElementMatch(value, operationJson, (QFDefinitionJson) def);
		specificationsWarp.addSpecification(match);

	}

	/**
	 * Override any collection operation on a filter field
	 * 
	 * @param field               filter field name
	 * @param operationCollection operation
	 * @param value               value
	 * @throws QFFieldNotFoundException if the field is not present
	 * @throws QFNotValuable            if the field is not a valid collection filter field
	 */
	public void overrideField(String field, QFCollectionOperationEnum operationCollection, int value)
			throws QFFieldNotFoundException, QFNotValuable {

		Assert.notNull(field, FIELD_NOT_NULL_MESSAGE);
		Assert.notNull(operationCollection, OPERATION_NOT_NULL_MESSAGE);

		QFAbstractDefinition def = definitionMap.get(field);
		if (def == null) {
			throw new QFFieldNotFoundException(field);
		}

		if (!(def instanceof QFDefinitionCollection)) {
			throw new QFNotValuable(field);
		}

		specificationsWarp.deleteSpecificationField(field);
		QFCollectionMatch match = new QFCollectionMatch((QFDefinitionCollection) def, operationCollection, value);
		specificationsWarp.addSpecification(match);
	}

	/**
	 * Get the actual values of the field
	 *
	 * @param field Field to check
	 * @return Values of the field
	 * @throws io.github.acoboh.query.filter.jpa.exceptions.QFFieldNotFoundException if the field is not present
	 * @throws java.lang.UnsupportedOperationException                               if the field is JSON type
	 */
	public @Nullable List<String> getActualValue(String field) throws QFFieldNotFoundException {
		QFAbstractDefinition def = definitionMap.get(field);
		if (def == null) {
			throw new QFFieldNotFoundException(field);
		}

		QFSpecificationPart qfSpec = specificationsWarp.getAllParts().stream()
				.filter(e -> e.getDefinition().getFilterName().equals(field)).findFirst().orElse(null);
		if (qfSpec == null) {
			return null;
		} else if (qfSpec instanceof QFElementMatch qfEMatch) {
			return qfEMatch.getOriginalValues();
		} else if (qfSpec instanceof QFDiscriminatorMatch qfDMatch) {
			return qfDMatch.getValues();
		}

		throw new UnsupportedOperationException(
				"Unsupported get list values for non QFElementMatch or QFDiscriminatorMatch classes");

	}

	/**
	 * Get the value of the json field
	 * 
	 * @param field filter field
	 * @return value of null if the field is not JSON type
	 * @throws QFFieldNotFoundException if the field is not found
	 */
	public @Nullable Map<String, String> getActualJsonValue(String field) throws QFFieldNotFoundException {
		QFAbstractDefinition def = definitionMap.get(field);
		if (def == null) {
			throw new QFFieldNotFoundException(field);
		}

		if (!(def instanceof QFDefinitionJson)) {
			return null;
		}

		QFSpecificationPart qfSpec = specificationsWarp.getAllParts().stream()
				.filter(e -> e.getDefinition().getFilterName().equals(field)).findFirst().orElse(null);

		if (qfSpec == null) {
			return null;
		} else if (qfSpec instanceof QFJsonElementMatch json) {
			return json.getMapValues();
		}

		throw new UnsupportedOperationException("Unsupported get map values of non QFJsonElementMatch classes");

	}

	/**
	 * Return the value used on a collection filter field
	 * 
	 * @param field filter field name
	 * @return the value of null if the field is not Collection type
	 * @throws QFFieldNotFoundException if the field is not found
	 */
	public Integer getActualCollectionValue(String field) throws QFFieldNotFoundException {
		QFAbstractDefinition def = definitionMap.get(field);
		if (def == null) {
			throw new QFFieldNotFoundException(field);
		}

		if (!(def instanceof QFDefinitionCollection)) {
			return null;
		}

		QFSpecificationPart qfSpec = specificationsWarp.getAllParts().stream()
				.filter(e -> e.getDefinition().getFilterName().equals(field)).findFirst().orElse(null);

		if (qfSpec == null) {
			return null;
		} else if (qfSpec instanceof QFCollectionMatch match) {
			return match.getValue();
		}

		throw new UnsupportedOperationException(
				"Unsupported get actual collection value non QFCollectionMatch classes");
	}

	/**
	 * Delete the field of the filter
	 *
	 * @param field field to delete
	 * @throws io.github.acoboh.query.filter.jpa.exceptions.QFFieldNotFoundException if the is not present
	 */
	public void deleteField(String field) throws QFFieldNotFoundException {

		QFAbstractDefinition def = definitionMap.get(field);
		if (def == null) {
			throw new QFFieldNotFoundException(field);
		}

		specificationsWarp.deleteSpecificationField(field);

	}

	/**
	 * Set a new predicate to be applied
	 *
	 * @param predicateName new predicate name
	 */
	public void setPredicate(String predicateName) {

		final Map<String, PredicateProcessorResolutor> locaPredMap = predicateMap;

		if (locaPredMap == null) {
			throw new UnsupportedOperationException("The class has no predicates");
		}

		PredicateProcessorResolutor found = locaPredMap.get(predicateName);
		if (found == null) {
			throw new IllegalStateException("Unable to found filter");
		}

		this.predicate = found;
		this.predicateName = predicateName;

	}

	/**
	 * Clear selected predicate
	 */
	public void clearPredicate() {
		predicate = null;
		predicateName = null;
	}

	/**
	 * Get actual predicate name. Can be null
	 *
	 * @return predicate name.
	 */
	public @Nullable String getPredicateName() {
		return predicateName;
	}

	/**
	 * Get the orders as criteria builder
	 *
	 * @param root            root of criteria builder
	 * @param criteriaBuilder criteria build
	 * @return orders parsed
	 */
	public List<Order> getOrderAsCriteriaBuilder(Root<E> root, CriteriaBuilder criteriaBuilder) {
		List<Pair<IDefinitionSortable, Direction>> sortList = defaultSortEnabled ? defaultSorting : sortDefinitionList;
		return QueryUtils.parseOrders(sortList, criteriaBuilder, root, new HashMap<>());
	}

	/** {@inheritDoc} */
	@Override
	public Predicate toPredicate(Root<E> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {

		Map<String, List<Predicate>> predicatesMap = new HashMap<>();
		Map<String, Path<?>> pathsMap = new HashMap<>();

		// Query distinct
		query.distinct(distinct);

		// Process sort
		if (query.getResultType().equals(entityClass)) {
			processSort(root, criteriaBuilder, query, pathsMap);
		} else if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Final class is not the entity class so all sorts will be ignored. Final class {}",
					query.getResultType());
		}

		List<QFSpecificationPart> sortedParts = specificationsWarp.getAllPartsSorted();

		MultiValueMap<String, Object> mlmap = new LinkedMultiValueMap<>(sortedParts.size());

		for (QFSpecificationPart part : sortedParts) {
			part.processPart(root, query, criteriaBuilder, predicatesMap, pathsMap, mlmap, spelResolver, entityClass);
		}

		Predicate finalPredicate = parseFinalPredicate(criteriaBuilder, predicatesMap);

		LOGGER.debug("Predicate {}", finalPredicate);

		return finalPredicate;
	}

	private void processSort(Root<E> root, CriteriaBuilder criteriaBuilder, CriteriaQuery<?> query,
			Map<String, Path<?>> pathsMap) {
		var sortList = defaultSortEnabled ? defaultSorting : sortDefinitionList;
		if (!sortList.isEmpty()) {
			LOGGER.trace("Adding all sort operations");
			query.orderBy(QueryUtils.parseOrders(sortList, criteriaBuilder, root, pathsMap));
		}
	}

	private Predicate parseFinalPredicate(CriteriaBuilder cb, Map<String, List<Predicate>> predicatesMap) {

		Map<String, Predicate> simplifiedPredicate = predicatesMap.entrySet().stream().collect(
				Collectors.toMap(Entry::getKey, e -> cb.and(e.getValue().toArray(new Predicate[e.getValue().size()]))));

		Predicate toRet;

		final PredicateProcessorResolutor localPredicate = predicate;

		if (localPredicate == null) {
			Predicate finalPredicate = cb
					.and(simplifiedPredicate.values().toArray(new Predicate[simplifiedPredicate.values().size()]));
			toRet = finalPredicate;
		} else {
			toRet = localPredicate.resolvePredicate(cb, simplifiedPredicate);
		}

		if (toRet == null || (toRet.isCompoundSelection() && toRet.getExpressions().isEmpty())) {
			return null;
		} else if (toRet.getExpressions().size() == 1) {
			return (Predicate) toRet.getExpressions().get(0);
		}

		return toRet;

	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return initialInput;
	}

}
