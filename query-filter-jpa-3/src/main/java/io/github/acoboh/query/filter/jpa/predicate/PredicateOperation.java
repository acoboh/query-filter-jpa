package io.github.acoboh.query.filter.jpa.predicate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;

/**
 * Predicate operations for custom predicates enumeration
 *
 * @author Adrián Cobo
 * 
 */
public enum PredicateOperation {

	/**
	 * And operator
	 */
	AND("AND") {
		@Override
		public Predicate getPredicate(CriteriaBuilder cb, List<Predicate> predicates) {
			return cb.and(predicates.toArray(new Predicate[predicates.size()]));
		}
	},

	/**
	 * Or operator
	 */
	OR("OR") {
		@Override
		public Predicate getPredicate(CriteriaBuilder cb, List<Predicate> predicates) {
			return cb.or(predicates.toArray(new Predicate[predicates.size()]));
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
	 * @param cb         Criteria builder
	 * @param predicates Predicates to be used on operation
	 *
	 * @return the predicate
	 */
	public abstract Predicate getPredicate(CriteriaBuilder cb, List<Predicate> predicates);

	/**
	 * Get the value of the predicate
	 * 
	 * @return value of the predicate
	 */
	public String getValue() {
		return value;
	}
}
