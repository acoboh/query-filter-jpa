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
import javax.persistence.criteria.From;
import javax.persistence.criteria.JoinType;
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
import io.github.acoboh.query.filter.jpa.exceptions.QFDiscriminatorNotFoundException;
import io.github.acoboh.query.filter.jpa.exceptions.QFFieldNotFoundException;
import io.github.acoboh.query.filter.jpa.exceptions.QFJsonParseException;
import io.github.acoboh.query.filter.jpa.exceptions.QFMultipleSortException;
import io.github.acoboh.query.filter.jpa.exceptions.QFNotSortableException;
import io.github.acoboh.query.filter.jpa.exceptions.QFNotValuable;
import io.github.acoboh.query.filter.jpa.exceptions.QFOperationNotFoundException;
import io.github.acoboh.query.filter.jpa.exceptions.QFParseException;
import io.github.acoboh.query.filter.jpa.exceptions.QueryFilterException;
import io.github.acoboh.query.filter.jpa.operations.QFOperationEnum;
import io.github.acoboh.query.filter.jpa.predicate.PredicateProcessorResolutor;
import io.github.acoboh.query.filter.jpa.spel.SpelResolverInterface;

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

	private static final long serialVersionUID = 1L;

	private static final String REGEX_SORT = "[a-zA-Z0-9]+=[+-][a-zA-Z0-9]+";
	private static final Pattern REGEX_PATTERN = Pattern.compile("([a-zA-Z0-9]+)=([+-])([a-zA-Z0-9]+)");

	private final String initialInput;

	private final List<QFElementMatch> valueMapping = new ArrayList<>();
	private final List<QFJsonElementMatch> jsonMapping = new ArrayList<>();
	private final List<QFDiscriminatorMatch> discriminatorMapping = new ArrayList<>();
	private final transient @Nullable Map<String, PredicateProcessorResolutor> predicateMap;

	private final transient List<QFElementMatch> defaultMatches;
	private final transient Map<String, QFDefinition> definitionMap;
	private final transient QFDefinitionClass queryFilterClassAnnotation;

	private final Class<E> entityClass;
	private final Class<?> predicateClass;
	private final boolean distinct;
	private final transient SpelResolverInterface spelResolver;
	private final transient List<Pair<QFDefinition, Direction>> sortDefinitionList = new ArrayList<>();
	private boolean isConstructor = true;

	private @Nullable String predicateName;
	private transient @Nullable PredicateProcessorResolutor predicate;

	/**
	 * Default constructor
	 *
	 * @param input                      input string
	 * @param type                       parameter type
	 * @param entityClass                entity class
	 * @param predicateClass             predicate definition class
	 * @param definitionMap              definition map
	 * @param queryFilterClassAnnotation definition annotation
	 * @param defaultMatches             list of default matching values
	 * @param spelResolver               spel resolver
	 * @param predicateMap               predicate map
	 * @param predicateName              predicate name
	 * @param defaultPredicate           default predicate
	 * @throws io.github.acoboh.query.filter.jpa.exceptions.QueryFilterException if any parsing exception
	 */
	protected QueryFilter(String input, QFParamType type, Class<E> entityClass, Class<?> predicateClass,
			Map<String, QFDefinition> definitionMap, QFDefinitionClass queryFilterClassAnnotation,
			List<QFElementMatch> defaultMatches, SpelResolverInterface spelResolver,
			@Nullable Map<String, PredicateProcessorResolutor> predicateMap, @Nullable String predicateName,
			@Nullable PredicateProcessorResolutor defaultPredicate) throws QueryFilterException {

		Assert.notNull(type, "type cannot be null");
		Assert.notNull(entityClass, "entityClass cannot be null");
		Assert.notNull(definitionMap, "definitionMap cannot be null");
		Assert.notNull(queryFilterClassAnnotation, "queryFilterClassAnnotation cannot be null");
		Assert.notNull(defaultMatches, "defaultMatches cannot be null");

		this.definitionMap = definitionMap;
		this.queryFilterClassAnnotation = queryFilterClassAnnotation;
		this.defaultMatches = defaultMatches;
		this.entityClass = entityClass;
		this.predicateClass = predicateClass;
		this.spelResolver = spelResolver;
		this.predicateMap = predicateMap;
		this.predicateName = predicateName;
		this.predicate = defaultPredicate;
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

	private static Path<?> getObject(Root<?> root, List<QFPath> paths, Map<String, Path<?>> pathsMap) {
		String fullPath = getFullPath(paths);

		Path<?> ret = pathsMap.get(fullPath);
		if (ret != null) {
			return ret;
		}

		if (paths.size() == 1 && paths.get(0).isFinal()) {
			ret = root.get(paths.get(0).getPath());
		} else {
			ret = getJoinObject(root, paths, pathsMap);
		}

		pathsMap.put(fullPath, ret);
		return ret;

	}

	private static Path<?> getJoinObject(Root<?> root, List<QFPath> paths, Map<String, Path<?>> pathsMap) {

		From<?, ?> join = root;

		StringBuilder base = new StringBuilder();
		String prefix = "";

		for (int i = 0; i < paths.size(); i++) {

			base.append(prefix).append(paths.get(i).getPath());
			prefix = ".";

			Path<?> pathRet = pathsMap.get(base.toString());
			if (pathRet != null) {
				join = (From<?, ?>) pathRet;
				continue;
			}

			if (i + 1 == paths.size() && paths.get(i).isFinal()) { // if last element and final
				return join.get(paths.get(i).getPath());
			}

			QFPath elem = paths.get(i);
			switch (elem.getType()) {
			case LIST:
				join = join.joinList(elem.getPath(), JoinType.LEFT);
				break;

			case SET:
				join = join.joinSet(elem.getPath(), JoinType.LEFT);
				break;
			case PROPERTY:
			case ENUM:
			default:
				join = join.join(elem.getPath(), JoinType.LEFT);
				break;
			}

			// Add to pathsMap

			pathsMap.put(base.toString(), join);

		}

		return join;

	}

	private static List<Order> parseOrders(List<Pair<QFDefinition, Direction>> sortDefinitionList, CriteriaBuilder cb,
			Root<?> root, Map<String, Path<?>> pathsMap) {
		ArrayList<Order> orderList = new ArrayList<>();

		for (Pair<QFDefinition, Direction> pair : sortDefinitionList) {
			LOGGER.trace("Adding sort operation for {}", pair);
			if (pair.getSecond() == Direction.ASC) {
				orderList.add(cb.asc(getObject(root, pair.getFirst().getPaths().get(0), pathsMap)));
			} else {
				orderList.add(cb.desc(getObject(root, pair.getFirst().getPaths().get(0), pathsMap)));
			}

		}

		return orderList;
	}

	private static String getFullPath(List<QFPath> paths) {
		return paths.stream().map(QFPath::getPath).collect(Collectors.joining("."));
	}

	private void parseValuePart(String part, QFParamType type)
			throws QFParseException, QFFieldNotFoundException, QFOperationNotFoundException,
			QFDiscriminatorNotFoundException, QFBlockException, QFJsonParseException, QFNotValuable {

		Matcher matcher = type.getPattern().matcher(part);
		if (matcher.find() && matcher.groupCount() == 3) {

			String field = matcher.group(1);
			String op = matcher.group(2);
			String value = matcher.group(3);

			QFDefinition def = definitionMap.get(field);
			if (def == null) {
				throw new QFFieldNotFoundException(field);
			}

			if (def.isConstructorBlocked() && isConstructor) {
				throw new QFBlockException(field);
			}

			if (!def.isElementFilter() && !def.isDiscriminatorFilter() && !def.isJsonElementFilter()) {
				throw new QFNotValuable(field);
			}

			if (def.isElementFilter()) {
				QFElementMatch match = new QFElementMatch(Arrays.asList(value.split(",")),
						QFOperationEnum.fromValue(op), def);
				valueMapping.add(match);
			} else if (def.isDiscriminatorFilter()) {
				QFDiscriminatorMatch match = new QFDiscriminatorMatch(Arrays.asList(value.split(",")), def);
				discriminatorMapping.add(match);

			} else if (def.isJsonElementFilter()) {
				QFJsonElementMatch match = new QFJsonElementMatch(value, QFOperationEnum.fromValue(op), def);
				jsonMapping.add(match);
			}

		} else {
			LOGGER.error("Error parsing part {}. Matcher not found matches", part);
			throw new QFParseException(part, type.name());
		}

	}

	private void parseSortPart(String part)
			throws QFParseException, QFNotSortableException, QFMultipleSortException, QFFieldNotFoundException {

		Matcher matcher = REGEX_PATTERN.matcher(part);

		if (!matcher.find() || matcher.groupCount() != 3) {
			LOGGER.error("Error parsing sort part {}, Matcher not found matches", part);
			throw new QFParseException(part, "sort part");
		}

		String shortname = matcher.group(1);
		String order = matcher.group(2);
		String fieldName = matcher.group(3);

		if (!shortname.equals(queryFilterClassAnnotation.sortProperty())) {
			LOGGER.warn("Ignoring sorting property with name {}. Expected {}", shortname,
					queryFilterClassAnnotation.sortProperty());
			return;
		}

		QFDefinition def = definitionMap.get(fieldName);
		if (def == null) {
			throw new QFFieldNotFoundException(fieldName);
		}

		if (!def.isSortable()) {
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

		Pair<QFDefinition, Direction> pair = Pair.of(def, dir);
		this.sortDefinitionList.add(pair);

	}

	/**
	 * Get the input used on the constructor
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

		Assert.notNull(field, "field cannot be null");
		Assert.notNull(operation, "operation cannot be null");
		Assert.notNull(values, "values cannot be null");

		QFDefinition def = definitionMap.get(field);
		if (def == null) {
			throw new QFFieldNotFoundException(field);
		}

		if (def.isElementFilter()) {
			QFElementMatch match = new QFElementMatch(values, operation, def);
			valueMapping.add(match);
		} else if (def.isDiscriminatorFilter()) {
			QFDiscriminatorMatch match = new QFDiscriminatorMatch(values, def);
			discriminatorMapping.add(match);
		}

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

		QFDefinition def = definitionMap.get(field);
		if (def == null) {
			throw new QFFieldNotFoundException(field);
		}

		if (!def.isSortable()) {
			throw new QFNotSortableException(field);
		}

		if (this.sortDefinitionList.stream().anyMatch(e -> e.getFirst().getFilterName().equals(field))) {
			throw new QFMultipleSortException(field);
		}

		Pair<QFDefinition, Direction> pair = Pair.of(def, direction);
		this.sortDefinitionList.add(pair);

	}

	/**
	 * Remove the actual sort configuration
	 */
	public void clearSort() {
		this.sortDefinitionList.clear();
	}

	/**
	 * Get if any sort filter is applied
	 *
	 * @return true if any sort is applied, false otherwise
	 */
	public boolean isSorted() {
		return !this.sortDefinitionList.isEmpty();
	}

	/**
	 * Get all the sort fields
	 *
	 * @return list of a pair of sorting fields
	 */
	public List<Pair<String, Direction>> getSortFields() {
		return sortDefinitionList.stream().map(e -> Pair.of(e.getFirst().getFilterName(), e.getSecond()))
				.collect(Collectors.toList());
	}

	/**
	 * Get if the filter is sorted by the selected field
	 *
	 * @param field field to check
	 * @return true if is actually sorting, false otherwise
	 */
	public boolean isSortedBy(String field) {
		return sortDefinitionList.stream().anyMatch(e -> e.getFirst().getFilterName().equals(field));
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
	 * @throws io.github.acoboh.query.filter.jpa.exceptions.QFJsonParseException             if any json parse exception
	 */
	public void overrideField(String field, QFOperationEnum operation, String value)
			throws QFFieldNotFoundException, QFDiscriminatorNotFoundException, QFJsonParseException {

		Assert.notNull(field, "field cannot be null");
		Assert.notNull(operation, "operation cannot be null");
		Assert.notNull(value, "value cannot be null");

		QFDefinition def = definitionMap.get(field);
		if (def == null) {
			throw new QFFieldNotFoundException(field);
		}

		if (def.isElementFilter()) {
			valueMapping.removeIf(e -> e.getDefinition().getFilterName().equals(field));
			QFElementMatch match = new QFElementMatch(Arrays.asList(value.split(",")), operation, def);
			valueMapping.add(match);

		} else if (def.isDiscriminatorFilter()) {
			discriminatorMapping.removeIf(e -> e.getDefinition().getFilterName().equals(field));
			QFDiscriminatorMatch match = new QFDiscriminatorMatch(Arrays.asList(value.split(",")), def);
			discriminatorMapping.add(match);
		} else if (def.isJsonElementFilter()) {
			jsonMapping.removeIf(e -> e.getDefinition().getFilterName().equals(field));
			QFJsonElementMatch match = new QFJsonElementMatch(value, operation, def);
			jsonMapping.add(match);
		}

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
		QFDefinition def = definitionMap.get(field);
		if (def == null) {
			throw new QFFieldNotFoundException(field);
		}

		if (def.isElementFilter()) {
			return valueMapping.stream().filter(e -> e.getDefinition().getFilterName().equals(field))
					.map(QFElementMatch::getOriginalValues).findAny().orElse(null);

		} else if (def.isDiscriminatorFilter()) {
			return discriminatorMapping.stream().filter(e -> e.getDefinition().getFilterName().equals(field))
					.map(QFDiscriminatorMatch::getValues).findFirst().orElse(null);
		} else if (def.isJsonElementFilter()) {
			throw new UnsupportedOperationException("Unsupported get list values of Json fields");
		}

		return null;

	}

	/**
	 * Delete the field of the filter
	 *
	 * @param field field to delete
	 * @throws io.github.acoboh.query.filter.jpa.exceptions.QFFieldNotFoundException if the is not present
	 */
	public void deleteField(String field) throws QFFieldNotFoundException {

		QFDefinition def = definitionMap.get(field);
		if (def == null) {
			throw new QFFieldNotFoundException(field);
		}

		if (def.isElementFilter()) {
			valueMapping.removeIf(e -> e.getDefinition().getFilterName().equals(field));
		} else if (def.isDiscriminatorFilter()) {
			discriminatorMapping.removeIf(e -> e.getDefinition().getFilterName().equals(field));
		} else if (def.isJsonElementFilter()) {
			jsonMapping.removeIf(e -> e.getDefinition().getFilterName().equals(field));
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
		return parseOrders(sortDefinitionList, criteriaBuilder, root, new HashMap<>());
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

		List<QFElementMatch> isArrayLevel = toMatch.stream().filter(QFElementMatch::isSubquery)
				.collect(Collectors.toList());

		toMatch.removeAll(isArrayLevel);

		for (QFDiscriminatorMatch match : discriminatorMapping) {

			List<Predicate> orDiscriminators = new ArrayList<>();

			if (match.isRoot()) {
				for (Class<?> clazz : match.getMatchingClasses()) {
					orDiscriminators.add(criteriaBuilder.equal(root.type(), clazz));
					mlmap.add(match.getDefinition().getFilterName(), clazz);
				}

			} else {

				for (Class<?> clazz : match.getMatchingClasses()) {
					orDiscriminators
							.add(criteriaBuilder.equal(getObject(root, match.getPath(), pathsMap).type(), clazz));
					mlmap.add(match.getDefinition().getFilterName(), clazz);
				}

			}

			predicatesMap.computeIfAbsent(match.getDefinition().getFilterName(), k -> new ArrayList<>())
					.add(criteriaBuilder.or(orDiscriminators.toArray(new Predicate[0])));

		}

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

				Path<?> pathFinal = getObject(newRoot, paths, subSelecthMap);

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

		for (QFElementMatch match : toMatch) {

			match.initialize(spelResolver, mlmap);

			if (!match.needToEvaluate()) {
				continue;
			}

			int index = 0;

			Predicate surrondingPredicate = match.getDefinition().getPredicateOperation().getPredicate(criteriaBuilder);
			List<Expression<Boolean>> expressions = surrondingPredicate.getExpressions();

			for (List<QFPath> paths : match.getPaths()) {
				expressions.add(match.getOperation().generatePredicate(getObject(root, paths, pathsMap),
						criteriaBuilder, match, index, mlmap));
				index++;
			}

			predicatesMap.computeIfAbsent(match.getDefinition().getFilterName(), t -> new ArrayList<>())
					.add(surrondingPredicate);

		}

		for (QFJsonElementMatch match : jsonMapping) {
			predicatesMap.computeIfAbsent(match.getDefinition().getFilterName(), t -> new ArrayList<>())
					.add(match.getOperation().generateJsonPredicate(getObject(root, match.getPaths(), pathsMap),
							criteriaBuilder, match));
		}

		query.distinct(distinct);

		if (!this.sortDefinitionList.isEmpty()) {
			LOGGER.trace("Adding all sort operations");
			query.orderBy(parseOrders(sortDefinitionList, criteriaBuilder, root, pathsMap));
		}

		Predicate finalPredicate = parseFinalPredicate(criteriaBuilder, predicatesMap);

		LOGGER.debug("Predicate {}", finalPredicate);

		return finalPredicate;
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

	@Override
	public String toString() {
		return initialInput;
	}

}
