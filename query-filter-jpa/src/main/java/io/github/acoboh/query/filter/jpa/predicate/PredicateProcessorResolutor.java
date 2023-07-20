package io.github.acoboh.query.filter.jpa.predicate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.acoboh.query.filter.jpa.processor.QFDefinition;

/**
 * Predicate recursive processor resolver
 *
 * @author Adrián Cobo
 */

public class PredicateProcessorResolutor {

	private static final Logger LOGGER = LoggerFactory.getLogger(PredicateProcessorResolutor.class);

	private final PredicateLevel predicateLevel;
	private final boolean includeMissing;
	private final PredicateOperation defaultOperation;

	private Set<String> fieldsFiltered;

	public PredicateProcessorResolutor(String predicateExp, Map<String, QFDefinition> definitionMap,
			boolean includeMissing, PredicateOperation defaultOperation) {

		LOGGER.debug("Initializing predicate {}", predicateExp);

		predicateExp = predicateExp.replaceAll(" +", " ").trim();

		// Create predicate Expression resolver
		predicateLevel = new PredicateLevel(predicateExp, definitionMap);

		this.includeMissing = includeMissing;

		if (includeMissing) {
			LOGGER.debug("Include missing flag active");
			fieldsFiltered = predicateLevel.getFilteredFields();
		}

		LOGGER.debug("Initialized predicate {}", predicateExp);

		this.defaultOperation = defaultOperation;

	}

	public Predicate resolvePredicate(CriteriaBuilder criteriaBuilder, Map<String, Predicate> predicates) {

		Predicate res = predicateLevel.resolveLevel(criteriaBuilder, predicates);
		if (includeMissing) {

			List<Predicate> toAdd = new ArrayList<>();

			for (Map.Entry<String, Predicate> entry : predicates.entrySet()) {

				if (!fieldsFiltered.contains(entry.getKey())) {
					LOGGER.trace("Added filter field '{}' by default on predicate", entry.getKey());
					toAdd.add(entry.getValue());

				}

			}

			if (!toAdd.isEmpty()) {
				LOGGER.trace("Adding all missing fields on expression");

				if (res.getExpressions().isEmpty()) {
					LOGGER.trace("Using only missing level as final filter");

					res = defaultOperation.getPredicate(criteriaBuilder);
					res.getExpressions().addAll(toAdd);

				} else {
					LOGGER.trace("Using surrounding level as final filter");

					Predicate surrounding = defaultOperation.getPredicate(criteriaBuilder);
					surrounding.getExpressions().add(res);
					res = surrounding;
					res.getExpressions().addAll(toAdd);

				}

			}

		}

		return res;
	}
}
