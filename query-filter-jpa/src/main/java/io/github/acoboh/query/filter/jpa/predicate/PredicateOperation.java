package io.github.acoboh.query.filter.jpa.predicate;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;

/**
 * Predicate operations for custom predicates enumeration
 *
 * @author Adrián Cobo
 * @version $Id: $Id
 */
public enum PredicateOperation {

	/**
	 * And operator
	 */
	AND("AND") {
		@Override
		public Predicate getPredicate(CriteriaBuilder cb) {
			return cb.conjunction();
		}
	},

	/**
	 * Or operator
	 */
	OR("OR") {
		@Override
		public Predicate getPredicate(CriteriaBuilder cb) {
			return cb.disjunction();
		}
	};

	private static final Map<String, PredicateOperation> map;

	static {
		map = Stream.of(PredicateOperation.values()).collect(Collectors.toMap(PredicateOperation::getValue, e -> e));
	}

	private final String value;

	PredicateOperation(String value) {
		this.value = value;
	}

	/**
	 * Get the enumerated from the string value
	 *
	 * @param value String value of operation to be found
	 * @return predicate operation found. Null if the operation is not found
	 */
	public static PredicateOperation getOperator(String value) {
		return map.get(value);
	}

	/**
	 * Resolve the predicate with criteria builder
	 * 
	 * @param cb criteria builder
	 *
	 * @return {@link java.lang.String} object
	 */
	public abstract Predicate getPredicate(CriteriaBuilder cb);

	/**
	 * Get the value of the predicate
	 * 
	 * @return value of the predicate
	 */
	public String getValue() {
		return value;
	}
}
