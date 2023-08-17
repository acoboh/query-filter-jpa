package io.github.acoboh.query.filter.jpa.predicate;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.acoboh.query.filter.jpa.processor.QFDefinition;
import io.github.acoboh.query.filter.jpa.utils.StringParseUtils;

/**
 * Predicate level class.
 *
 * @author Adrián Cobo
 */
class PredicateLevel {

	private static final Logger LOGGER = LoggerFactory.getLogger(PredicateLevel.class);

	private final List<String> levelParts = new ArrayList<>();
	private final List<PredicatePart> parts;
	private List<PredicateLevel> nestedLevels;
	private PredicateOperation levelOperator;
	private Set<String> fieldsSet;

	/**
	 * <p>
	 * Constructor for PredicateLevel.
	 * </p>
	 *
	 * @param expression a {@link java.lang.String} object
	 * @param fullMap    a {@link java.util.Map} object
	 */
	public PredicateLevel(String expression, Map<String, QFDefinition> fullMap) {

		parts = StringParseUtils.parseParts(expression);

		if (parts.isEmpty()) {
			return;
		}

		if (parts.size() == 1) {
			LOGGER.debug("Parts has size 1, must be simplified");
			levelParts.add(parts.get(0).getPart());

			return;
		}

		for (int i = 0; i < parts.size(); i++) {

			PredicatePart part = parts.get(i);
			LOGGER.trace("Parsing parts {}", part);

			if (i % 2 == 1) {
				LOGGER.trace("Parsing part as operator {}", part.getPart());
				PredicateOperation operator = PredicateOperation.getOperator(part.getPart());
				if (operator == null) {
					LOGGER.error("Unexpected part {}. Must be an operator", part.getPart());
					throw new IllegalStateException("Unexpected part. Must be an operator");
				}

				if (levelOperator == null) {
					levelOperator = operator;
				} else if (levelOperator != operator) {
					LOGGER.error(
							"Operators mixed on same level. Found {} and {} on the same level. Review the parenthesis",
							levelOperator, operator);
					throw new IllegalStateException("Mixed operators on same level");
				}
			} else {
				if (part.isNested()) {
					LOGGER.trace("Parsing nested level as new predicate");

					PredicateLevel nestedLevel = new PredicateLevel(part.getPart(), fullMap);
					switch (nestedLevel.parts.size()) {
					case 0:
						LOGGER.trace("Nested level is empty. Ignore them");
						continue;
					case 1:
						LOGGER.trace("Nested level must be simplified");
						levelParts.addAll(nestedLevel.levelParts);
						continue;
					default:
						LOGGER.trace("Adding as nested level");
						if (nestedLevels == null) {
							nestedLevels = new ArrayList<>();
						}

						nestedLevels.add(nestedLevel);
					}

				} else {

					if (!fullMap.containsKey(part.getPart())) {
						LOGGER.error("Error missing part {} on definition map", part.getPart());
						throw new IllegalStateException("Missing part on definition map");
					}

					levelParts.add(part.getPart());

				}
			}
		}

	}

	/**
	 * <p>
	 * getFilteredFields.
	 * </p>
	 *
	 * @return a {@link java.util.Set} object
	 */
	public Set<String> getFilteredFields() {
		if (fieldsSet == null) {

			fieldsSet = new LinkedHashSet<>();

			fieldsSet.addAll(levelParts);

			if (nestedLevels != null && !nestedLevels.isEmpty()) {
				nestedLevels.forEach(e -> fieldsSet.addAll(e.getFilteredFields()));
			}

		}

		return fieldsSet;
	}

	/**
	 * <p>
	 * resolveLevel.
	 * </p>
	 *
	 * @param cb         a {@link javax.persistence.criteria.CriteriaBuilder} object
	 * @param predicates a {@link java.util.Map} object
	 * @return a {@link javax.persistence.criteria.Predicate} object
	 */
	public Predicate resolveLevel(CriteriaBuilder cb, Map<String, Predicate> predicates) {

		Predicate res = levelOperator.getPredicate(cb);

		if (!levelParts.isEmpty()) {

			for (String part : levelParts) {
				Predicate predicate = predicates.get(part);
				if (predicate != null) {
					res.getExpressions().add(predicate);
				}
			}

		}

		if (nestedLevels != null && !nestedLevels.isEmpty()) {
			for (PredicateLevel level : nestedLevels) {

				Predicate customPredicate = level.resolveLevel(cb, predicates);
				if (customPredicate.getExpressions().isEmpty()) {
					LOGGER.trace("Empty predicate nested level. Ignored");
				} else if (customPredicate.getExpressions().size() == 1) {
					LOGGER.trace("Single expression on nested level. Unwrap predicate");
					res.getExpressions().add(customPredicate.getExpressions().get(0));

				} else {
					res.getExpressions().add(customPredicate);
				}

			}
		}

		return res;

	}

}
