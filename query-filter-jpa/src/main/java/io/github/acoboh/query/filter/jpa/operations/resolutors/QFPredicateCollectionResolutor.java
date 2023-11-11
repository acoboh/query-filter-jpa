package io.github.acoboh.query.filter.jpa.operations.resolutors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

import io.github.acoboh.query.filter.jpa.processor.match.QFCollectionMatch;

public interface QFPredicateCollectionResolutor {

	/**
	 * Generate predicate of collection elements
	 *
	 * @param path  path of criteria builder
	 * @param cb    criteria builder
	 * @param match element matched
	 * @return predicate resolver
	 */
	<C extends java.util.Collection<?>> Predicate generateCollectionPredicate(Expression<C> path, CriteriaBuilder cb,
			QFCollectionMatch match);

	/**
	 * Get the operation string value
	 *
	 * @return string operation
	 */
	String getOperation();

}
