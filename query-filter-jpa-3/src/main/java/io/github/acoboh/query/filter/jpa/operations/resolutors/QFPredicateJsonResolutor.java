package io.github.acoboh.query.filter.jpa.operations.resolutors;

import io.github.acoboh.query.filter.jpa.processor.match.QFJsonElementMatch;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

/**
 * Interface for predicate json resolutors
 */
public interface QFPredicateJsonResolutor {

	/**
	 * Generate predicate of JSON elements
	 *
	 * @param path
	 *            path of criteria builder
	 * @param cb
	 *            criteria builder
	 * @param match
	 *            element matched
	 * @return predicate resolver
	 */
	Predicate generateJsonPredicate(Path<?> path, CriteriaBuilder cb, QFJsonElementMatch match);

	/**
	 * Get the operation string value
	 *
	 * @return string operation
	 */
	String getOperation();
}
