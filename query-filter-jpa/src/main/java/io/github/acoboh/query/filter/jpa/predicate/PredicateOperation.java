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
 */
public enum PredicateOperation {

	AND("AND") {
		@Override
		public Predicate getPredicate(CriteriaBuilder cb) {
			return cb.conjunction();
		}
	},
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

	public static PredicateOperation getOperator(String value) {
		return map.get(value);
	}

	public abstract Predicate getPredicate(CriteriaBuilder cb);

	public String getValue() {
		return value;
	}
}
