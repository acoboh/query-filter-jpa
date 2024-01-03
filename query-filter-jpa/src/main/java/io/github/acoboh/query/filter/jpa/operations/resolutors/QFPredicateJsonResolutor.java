package io.github.acoboh.query.filter.jpa.operations.resolutors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.github.acoboh.query.filter.jpa.processor.match.QFJsonElementMatch;

/**
 * Interface for predicate json resolutors
 */
public interface QFPredicateJsonResolutor {

	/**
	 * Generate predicate of JSON elements
	 *
	 * @param path  path of criteria builder
	 * @param cb    criteria builder
	 * @param match element matched
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
