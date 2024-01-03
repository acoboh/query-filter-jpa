package io.github.acoboh.query.filter.jpa.processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

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
import io.github.acoboh.query.filter.jpa.exceptions.QFCollectionException;
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
import io.github.acoboh.query.filter.jpa.processor.definitions.QFDefinitionSortable;
import io.github.acoboh.query.filter.jpa.processor.definitions.traits.IDefinitionSortable;
import io.github.acoboh.query.filter.jpa.processor.match.QFCollectionMatch;
import io.github.acoboh.query.filter.jpa.processor.match.QFDiscriminatorMatch;
import io.github.acoboh.query.filter.jpa.processor.match.QFElementMatch;
import io.github.acoboh.query.filter.jpa.processor.match.QFJsonElementMatch;
import io.github.acoboh.query.filter.jpa.spel.SpelResolverContext;

/**
 * Class that implements {@linkplain Specification} from JPA library that allows the user to create automatic filters from
 * {@linkplain QFParamType#RHS_COLON} or {@linkplain QFParamType#LHS_BRACKETS} standards
 *
 * @author Adrián Cobo
 * @param <E> Entity model class
 * 
 */
/**
 * @param <E>
 */
/**
 * @param <E>
 */
public class QueryFilter<E> implements Specification<E> {

	private static final Logger LOGGER = LoggerFactory.getLogger(QueryFilter.class);

	private static final long serialVersionUID = 1L;

	private static final String REGEX_SORT = "^[a-zA-Z0-9]+=([+-]?[a-zA-Z0-9]+)(,[+-]?[a-zA-Z0-9]+)*$";
	private static final Pattern REGEX_PATTERN = Pattern.compile("([+-])([a-zA-Z0-9]+)");

	private final String initialInput;

	private final List<QFElementMatch> valueMapping = new ArrayList<>();
	private final List<QFCollectionMatch> collectionMapping = new ArrayList<>();
	private final List<QFJsonElementMatch> jsonMapping = new ArrayList<>();
	private final List<QFDiscriminatorMatch> discriminatorMapping = new ArrayList<>();
	private final transient @Nullable Map<String, PredicateProcessorResolutor> predicateMap;

	private final transient List<QFElementMatch> defaultMatches;
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
		this.defaultMatches = processor.getDefaultMatches();

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
			String[] parts = input.split("&");

			for (String part : parts) {

				if (part.matches(type.getFullRegex())) {
					parseValuePart(part, type);
				} else if (part.matches(REGEX_SORT)) {
					parseSortPart(part);
				} else {
					throw new QFParseException(part, input);
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

	private void parseValuePart(String part, QFParamType type)
			throws QFParseException, QFFieldNotFoundException, QFOperationNotFoundException,
			QFDiscriminatorNotFoundException, QFBlockException, QFJsonParseException, QFNotValuable {

		Matcher matcher = type.getPattern().matcher(part);
		if (!(matcher.find() && matcher.groupCount() == 3)) {
			LOGGER.error("Error parsing part {}. Matcher not found matches", part);
			throw new QFParseException(part, type.name());
		}

		String field = matcher.group(1);
		String op = matcher.group(2);
		String value = matcher.group(3);

		QFAbstractDefinition def = definitionMap.get(field);
		if (def == null) {
			throw new QFFieldNotFoundException(field);
		}

		if (def.isConstructorBlocked() && isConstructor) {
			throw new QFBlockException(field);
		}

		if (def instanceof QFDefinitionElement) {
			QFElementMatch match = new QFElementMatch(Arrays.asList(value.split(",")), QFOperationEnum.fromValue(op),
					(QFDefinitionElement) def);
			valueMapping.add(match);
		} else if (def instanceof QFDefinitionDiscriminator) {
			QFDiscriminatorMatch match = new QFDiscriminatorMatch(Arrays.asList(value.split(",")),
					(QFDefinitionDiscriminator) def);
			discriminatorMapping.add(match);
		} else if (def instanceof QFDefinitionJson) {
			QFJsonElementMatch match = new QFJsonElementMatch(value, QFOperationJsonEnum.fromValue(op),
					(QFDefinitionJson) def);
			jsonMapping.add(match);
		} else if (def instanceof QFDefinitionCollection) {
			QFCollectionMatch match = new QFCollectionMatch((QFDefinitionCollection) def,
					QFCollectionOperationEnum.fromValue(op), Integer.valueOf(value));
			collectionMapping.add(match);
		} else {
			throw new QFNotValuable(field);
		}

	}

	private void parseSortPart(String part)
			throws QFParseException, QFNotSortableException, QFMultipleSortException, QFFieldNotFoundException {

		if (!part.startsWith(queryFilterClassAnnotation.sortProperty() + "=")) {
			throw new QFParseException(part, "sort part");
		}

		String partPostEqual = part.substring(part.indexOf('='));

		String[] parts = partPostEqual.split(",");

		for (String orderPart : parts) {

			Matcher matcher = REGEX_PATTERN.matcher(orderPart);
			if (!matcher.find() || matcher.groupCount() != 2) {
				LOGGER.error("Error parsing sort part {}, Matcher not found matches", orderPart);
				throw new QFParseException(orderPart, "sort part");
			}

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
	 * <p>
	 * Only works with element and discriminators. Otherwise will throw {@linkplain QFNotValuable} exception
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

		Assert.notNull(field, "field cannot be null");
		Assert.notNull(operation, "operation cannot be null");
		Assert.notNull(values, "values cannot be null");

		QFAbstractDefinition def = definitionMap.get(field);
		if (def == null) {
			throw new QFFieldNotFoundException(field);
		}

		if (def instanceof QFDefinitionElement) {
			QFElementMatch match = new QFElementMatch(values, operation, (QFDefinitionElement) def);
			valueMapping.add(match);
		} else if (def instanceof QFDefinitionDiscriminator) {
			QFDiscriminatorMatch match = new QFDiscriminatorMatch(values, (QFDefinitionDiscriminator) def);
			discriminatorMapping.add(match);
		} else {
			throw new QFNotValuable(field);
		}

	}

	/**
	 * Manually adds a new operation on any JSON field
	 * 
	 * @param field     field to filter
	 * @param operation operation to be applied
	 * @param value     json value as string
	 */
	public void addNewField(String field, QFOperationJsonEnum operation, String value) {
		Assert.notNull(field, "field cannot be null");
		Assert.notNull(operation, "operation cannot be null");
		Assert.notNull(value, "value cannot be null");

		QFAbstractDefinition def = definitionMap.get(field);
		if (def == null) {
			throw new QFFieldNotFoundException(field);
		}

		if (!(def instanceof QFDefinitionJson)) {
			throw new QFNotValuable(field);
		}

		QFJsonElementMatch match = new QFJsonElementMatch(value, operation, (QFDefinitionJson) def);
		jsonMapping.add(match);

	}

	/**
	 * Add a new field for collection operations
	 * 
	 * @param field     filter field name
	 * @param operation operation
	 * @param value     value
	 */
	public void addNewField(String field, QFCollectionOperationEnum operation, int value) {

		Assert.notNull(field, "field cannot be null");
		Assert.notNull(operation, "operation cannot be null");

		QFAbstractDefinition def = definitionMap.get(field);
		if (def == null) {
			throw new QFFieldNotFoundException(field);
		}

		if (!(def instanceof QFDefinitionCollection)) {
			throw new QFNotValuable(field);
		}

		QFCollectionMatch match = new QFCollectionMatch((QFDefinitionCollection) def, operation, value);
		collectionMapping.add(match);

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

		Assert.notNull(field, "field cannot be null");
		Assert.notNull(operation, "operation cannot be null");
		Assert.notNull(value, "value cannot be null");

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
		List<Pair<IDefinitionSortable, Direction>> list = defaultSortEnabled ? defaultSorting : sortDefinitionList;
		return !list.isEmpty();
	}

	/**
	 * Get all the sort fields
	 *
	 * @return list of a pair of sorting fields
	 */
	public List<Pair<String, Direction>> getSortFields() {
		List<Pair<IDefinitionSortable, Direction>> list = defaultSortEnabled ? defaultSorting : sortDefinitionList;
		return list.stream().map(e -> Pair.of(e.getFirst().getFilterName(), e.getSecond()))
				.collect(Collectors.toList());
	}

	/**
	 * Get if the filter is sorted by the selected field
	 *
	 * @param field field to check
	 * @return true if is actually sorting, false otherwise
	 */
	public boolean isSortedBy(String field) {
		List<Pair<IDefinitionSortable, Direction>> list = defaultSortEnabled ? defaultSorting : sortDefinitionList;
		return list.stream().anyMatch(e -> e.getFirst().getFilterName().equals(field));
	}

	/**
	 * Check if the field is currently used for filtering
	 *
	 * @param field Filter name to check
	 * @return true if the field is present, false otherwise
	 */
	public boolean isFiltering(String field) {
		return valueMapping.stream().anyMatch(e -> e.getDefinition().getFilterName().equals(field));
	}

	/**
	 * Check if any of the fields is are currently used for filtering
	 *
	 * @param fields Filter names to be checked
	 * @return true, if any of the fields are present, false is all of them are actually missing
	 */
	public boolean isFilteringAny(String... fields) {
		Set<String> set = Sets.newHashSet(fields);
		return valueMapping.stream().anyMatch(e -> set.contains(e.getDefinition().getFilterName()));
	}

	/**
	 * Override any field. If not present, a new field will be created
	 *
	 * @param field     Field filter name
	 * @param operation Operation to apply
	 * @param value     value of filter
	 * @throws io.github.acoboh.query.filter.jpa.exceptions.QFFieldNotFoundException         Missing field exception
	 * @throws io.github.acoboh.query.filter.jpa.exceptions.QFDiscriminatorNotFoundException if the discriminator value is not allowed
	 * @throws io.github.acoboh.query.filter.jpa.exceptions.QFNotValuable                    if the field is not valuable or type
	 *                                                                                       compatible
	 */
	public void overrideField(String field, QFOperationEnum operation, String value)
			throws QFFieldNotFoundException, QFDiscriminatorNotFoundException, QFNotValuable {

		Assert.notNull(field, "field cannot be null");
		Assert.notNull(operation, "operation cannot be null");
		Assert.notNull(value, "value cannot be null");

		QFAbstractDefinition def = definitionMap.get(field);
		if (def == null) {
			throw new QFFieldNotFoundException(field);
		}

		if (def instanceof QFDefinitionElement) {
			valueMapping.removeIf(e -> e.getDefinition().getFilterName().equals(field));
			QFElementMatch match = new QFElementMatch(Arrays.asList(value.split(",")), operation,
					(QFDefinitionElement) def);
			valueMapping.add(match);
		} else if (def instanceof QFDefinitionDiscriminator) {
			discriminatorMapping.removeIf(e -> e.getDefinition().getFilterName().equals(field));
			QFDiscriminatorMatch match = new QFDiscriminatorMatch(Arrays.asList(value.split(",")),
					(QFDefinitionDiscriminator) def);
			discriminatorMapping.add(match);
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

		Assert.notNull(field, "field cannot be null");
		Assert.notNull(operationJson, "operation cannot be null");
		Assert.notNull(value, "value cannot be null");

		QFAbstractDefinition def = definitionMap.get(field);
		if (def == null) {
			throw new QFFieldNotFoundException(field);
		}

		if (!(def instanceof QFDefinitionJson)) {
			throw new QFNotValuable(field);
		}

		jsonMapping.removeIf(e -> e.getDefinition().getFilterName().equals(field));
		QFJsonElementMatch match = new QFJsonElementMatch(value, operationJson, (QFDefinitionJson) def);
		jsonMapping.add(match);

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

		Assert.notNull(field, "field cannot be null");
		Assert.notNull(operationCollection, "operation cannot be null");

		QFAbstractDefinition def = definitionMap.get(field);
		if (def == null) {
			throw new QFFieldNotFoundException(field);
		}

		if (!(def instanceof QFDefinitionCollection)) {
			throw new QFNotValuable(field);
		}

		collectionMapping.removeIf(e -> e.getDefinition().getFilterName().equals(field));
		QFCollectionMatch match = new QFCollectionMatch((QFDefinitionCollection) def, operationCollection, value);
		collectionMapping.add(match);
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

		if (def instanceof QFDefinitionElement) {
			return valueMapping.stream().filter(e -> e.getDefinition().getFilterName().equals(field))
					.map(QFElementMatch::getOriginalValues).findAny().orElse(null);
		} else if (def instanceof QFDefinitionDiscriminator) {
			return discriminatorMapping.stream().filter(e -> e.getDefinition().getFilterName().equals(field))
					.map(QFDiscriminatorMatch::getValues).findFirst().orElse(null);
		} else if (def instanceof QFDefinitionJson) {
			throw new UnsupportedOperationException("Unsupported get list values of Json fields");
		}

		return null;

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

		return jsonMapping.stream().filter(e -> e.getDefinition().getFilterName().equals(field))
				.map(QFJsonElementMatch::getMapValues).findFirst().orElse(null);
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

		return collectionMapping.stream().filter(e -> e.getDefinition().getFilterName().equals(field))
				.map(QFCollectionMatch::getValue).findFirst().orElse(null);
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

		if (def instanceof QFDefinitionElement) {
			valueMapping.removeIf(e -> e.getDefinition().getFilterName().equals(field));
		} else if (def instanceof QFDefinitionDiscriminator) {
			discriminatorMapping.removeIf(e -> e.getDefinition().getFilterName().equals(field));
		} else if (def instanceof QFDefinitionJson) {
			jsonMapping.removeIf(e -> e.getDefinition().getFilterName().equals(field));
		} else if (def instanceof QFDefinitionSortable) {
			LOGGER.trace("Disabled default sorting");
			this.defaultSortEnabled = false;
			this.sortDefinitionList.removeIf(e -> e.getFirst().getFilterName().equals(field));
		} else if (def instanceof QFDefinitionCollection) {
			collectionMapping.removeIf(e -> e.getDefinition().getFilterName().equals(field));
		}

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

		List<QFElementMatch> toMatch = new ArrayList<>();
		LOGGER.trace("Checking default values");
		for (QFElementMatch match : defaultMatches) {

			if (valueMapping.stream().noneMatch(e -> e.getDefinition().equals(match.getDefinition()))) {
				toMatch.add(match);
			}

		}

		toMatch.addAll(valueMapping);
		toMatch.sort(Comparator.comparingInt(o -> o.getDefinition().getOrder()));

		MultiValueMap<String, Object> mlmap = new LinkedMultiValueMap<>(toMatch.size() + discriminatorMapping.size());

		List<QFElementMatch> isArrayLevel = toMatch.stream().filter(e -> e.getDefinition().isSubQuery())
				.collect(Collectors.toList());

		toMatch.removeAll(isArrayLevel);

		// Discriminators
		processDiscriminators(root, criteriaBuilder, predicatesMap, pathsMap, mlmap);

		// ArrayLevel
		processArrayLevel(root, query, criteriaBuilder, predicatesMap, mlmap, isArrayLevel);

		// Elements
		processElements(root, criteriaBuilder, predicatesMap, pathsMap, toMatch, mlmap);

		// JSON elements
		processJsonElements(root, criteriaBuilder, predicatesMap, pathsMap);

		// Collections
		processCollections(root, criteriaBuilder, predicatesMap, pathsMap);

		query.distinct(distinct);

		List<Pair<IDefinitionSortable, Direction>> list = defaultSortEnabled ? defaultSorting : sortDefinitionList;
		if (!list.isEmpty()) {
			LOGGER.trace("Adding all sort operations");
			query.orderBy(QueryUtils.parseOrders(list, criteriaBuilder, root, pathsMap));
		}

		Predicate finalPredicate = parseFinalPredicate(criteriaBuilder, predicatesMap);

		LOGGER.debug("Predicate {}", finalPredicate);

		return finalPredicate;
	}

	private void processDiscriminators(Root<E> root, CriteriaBuilder criteriaBuilder,
			Map<String, List<Predicate>> predicatesMap, Map<String, Path<?>> pathsMap,
			MultiValueMap<String, Object> mlmap) {
		for (QFDiscriminatorMatch match : discriminatorMapping) {

			List<Predicate> orDiscriminators = new ArrayList<>();

			if (match.isRoot()) {
				for (Class<?> clazz : match.getMatchingClasses()) {
					orDiscriminators.add(criteriaBuilder.equal(root.type(), clazz));
					mlmap.add(match.getDefinition().getFilterName(), clazz);
				}

			} else {

				for (Class<?> clazz : match.getMatchingClasses()) {
					orDiscriminators.add(criteriaBuilder
							.equal(QueryUtils.getObject(root, match.getPath(), pathsMap, false).type(), clazz));
					mlmap.add(match.getDefinition().getFilterName(), clazz);
				}

			}

			predicatesMap.computeIfAbsent(match.getDefinition().getFilterName(), k -> new ArrayList<>())
					.add(criteriaBuilder.or(orDiscriminators.toArray(new Predicate[0])));

		}
	}

	private void processArrayLevel(Root<E> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder,
			Map<String, List<Predicate>> predicatesMap, MultiValueMap<String, Object> mlmap,
			List<QFElementMatch> isArrayLevel) {
		for (QFElementMatch arrayLevelMatch : isArrayLevel) {

			Map<String, Path<?>> subSelecthMap = new HashMap<>();

			arrayLevelMatch.initialize(spelResolver, mlmap);

			if (!arrayLevelMatch.needToEvaluate()) {
				continue;
			}

			int index = 0;
			for (List<QFPath> paths : arrayLevelMatch.getPaths()) {

				Subquery<E> subquery = query.subquery(entityClass);
				Root<E> newRoot = subquery.from(entityClass);

				subquery.select(newRoot.as(entityClass));

				Path<?> pathFinal = QueryUtils.getObject(newRoot, paths, subSelecthMap, false);

				QFOperationEnum op = arrayLevelMatch.getOperation();
				if (op == QFOperationEnum.NOT_EQUAL) {
					op = QFOperationEnum.EQUAL;
				}

				subquery.where(op.generatePredicate(pathFinal, criteriaBuilder, arrayLevelMatch, index, mlmap));

				Predicate finalPredicate = criteriaBuilder.in(root).value(subquery);

				if (arrayLevelMatch.getOperation() == QFOperationEnum.NOT_EQUAL) {
					finalPredicate = criteriaBuilder.not(finalPredicate);
				}

				predicatesMap.computeIfAbsent(arrayLevelMatch.getDefinition().getFilterName(), k -> new ArrayList<>())
						.add(finalPredicate);

				index++;
			}

		}
	}

	private void processElements(Root<E> root, CriteriaBuilder criteriaBuilder,
			Map<String, List<Predicate>> predicatesMap, Map<String, Path<?>> pathsMap, List<QFElementMatch> toMatch,
			MultiValueMap<String, Object> mlmap) {
		for (QFElementMatch match : toMatch) {

			match.initialize(spelResolver, mlmap);

			if (!match.needToEvaluate()) {
				continue;
			}

			int index = 0;

			Predicate surrondingPredicate = match.getDefinition().getPredicateOperation().getPredicate(criteriaBuilder);
			List<Expression<Boolean>> expressions = surrondingPredicate.getExpressions();

			for (List<QFPath> paths : match.getPaths()) {
				expressions.add(match.getOperation().generatePredicate(
						QueryUtils.getObject(root, paths, pathsMap, false), criteriaBuilder, match, index, mlmap));
				index++;
			}

			predicatesMap.computeIfAbsent(match.getDefinition().getFilterName(), t -> new ArrayList<>())
					.add(surrondingPredicate);

		}
	}

	private void processJsonElements(Root<E> root, CriteriaBuilder criteriaBuilder,
			Map<String, List<Predicate>> predicatesMap, Map<String, Path<?>> pathsMap) {
		for (QFJsonElementMatch match : jsonMapping) {
			predicatesMap.computeIfAbsent(match.getDefinition().getFilterName(), t -> new ArrayList<>())
					.add(match.getOperation().generateJsonPredicate(
							QueryUtils.getObject(root, match.getDefinition().getPaths(), pathsMap, true),
							criteriaBuilder, match));
		}
	}

	@SuppressWarnings("unchecked")
	private void processCollections(Root<E> root, CriteriaBuilder criteriaBuilder,
			Map<String, List<Predicate>> predicatesMap, Map<String, Path<?>> pathsMap) {
		LOGGER.trace("Creating all collection matching elements on filter");
		for (QFCollectionMatch match : collectionMapping) {

			Path<?> expressionPath = QueryUtils.getObject(root, match.getDefinition().getPaths(), pathsMap, true);

			Expression<? extends java.util.Collection<?>> expression;

			try {
				expression = (Expression<? extends java.util.Collection<?>>) expressionPath;
			} catch (ClassCastException e) {
				throw new QFCollectionException(match.getDefinition().getFilterName(), e.getMessage());
			}

			predicatesMap.computeIfAbsent(match.getDefinition().getFilterName(), t -> new ArrayList<>())
					.add(match.getOperation().generateCollectionPredicate(expression, criteriaBuilder, match));

		}
	}

	private Predicate parseFinalPredicate(CriteriaBuilder cb, Map<String, List<Predicate>> predicatesMap) {

		Map<String, Predicate> simplifiedPredicate = predicatesMap.entrySet().stream()
				.collect(Collectors.toMap(Entry::getKey, e -> {
					Predicate ret = cb.conjunction();
					ret.getExpressions().addAll(e.getValue());
					return ret;
				}));

		Predicate toRet;

		final PredicateProcessorResolutor localPredicate = predicate;

		if (localPredicate == null) {
			Predicate finalPredicate = cb.conjunction();
			finalPredicate.getExpressions().addAll(simplifiedPredicate.values());
			toRet = finalPredicate;
		} else {
			toRet = localPredicate.resolvePredicate(cb, simplifiedPredicate);
		}

		if (toRet.getExpressions().isEmpty()) {
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
