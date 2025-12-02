package io.github.acoboh.query.filter.jpa.predicate;

import io.github.acoboh.query.filter.jpa.processor.definitions.QFAbstractDefinition;
import io.github.acoboh.query.filter.jpa.utils.StringParseUtils;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

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
     * Predicate constructor
     *
     * @param expression expression of predicate
     * @param fullMap    map definition of filter to be used
     */
    public PredicateLevel(String expression, Map<String, QFAbstractDefinition> fullMap) {

        parts = StringParseUtils.parseParts(expression);

        if (parts.isEmpty()) {
            return;
        }

        if (parts.size() == 1) {
            LOGGER.debug("Parts has size 1, must be simplified");
            levelParts.add(parts.get(0).part());

            return;
        }

        for (int i = 0; i < parts.size(); i++) {

            PredicatePart part = parts.get(i);
            LOGGER.trace("Parsing parts {}", part);

            if (i % 2 == 1) {
                LOGGER.trace("Parsing part as operator {}", part.part());
                PredicateOperation operator = PredicateOperation.getOperator(part.part());
                if (operator == null) {
                    LOGGER.error("Unexpected part {}. Must be an operator", part.part());
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
                if (part.nested()) {
                    LOGGER.trace("Parsing nested level as new predicate");

                    PredicateLevel nestedLevel = new PredicateLevel(part.part(), fullMap);
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

                    if (!fullMap.containsKey(part.part())) {
                        LOGGER.error("Error missing part {} on definition map", part.part());
                        throw new IllegalStateException("Missing part on definition map");
                    }

                    levelParts.add(part.part());

                }
            }
        }

    }

    /**
     * Get the filtered fields
     *
     * @return set of filtered field
     */
    public Set<String> getFilteredFields() {
        if (fieldsSet == null) { // Cache fields

            fieldsSet = new LinkedHashSet<>();

            fieldsSet.addAll(levelParts);

            if (nestedLevels != null && !nestedLevels.isEmpty()) {
                nestedLevels.forEach(e -> fieldsSet.addAll(e.getFilteredFields()));
            }

        }

        return fieldsSet;
    }

    /**
     * Resolve the level of the predicate
     *
     * @param cb         Criteria builder
     * @param predicates Predicates to be used on nested levels for resolution
     * @return Predicate used
     */
    public Predicate resolveLevel(CriteriaBuilder cb, Map<String, Predicate> predicates) {

        List<Predicate> expresions = new ArrayList<>();

        if (!levelParts.isEmpty()) {

            for (String part : levelParts) {
                Predicate predicate = predicates.get(part);
                if (predicate != null) {
                    expresions.add(predicate);
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
                    expresions.add((Predicate) customPredicate.getExpressions().get(0));

                } else {
                    expresions.add(customPredicate);
                }

            }
        }

        if (expresions.isEmpty()) {
            return null;
        }

        return levelOperator.getPredicate(cb, expresions);

    }

}
