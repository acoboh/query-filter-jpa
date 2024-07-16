package io.github.acoboh.query.filter.jpa.operations;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

import org.springframework.util.MultiValueMap;

import io.github.acoboh.query.filter.jpa.exceptions.QFOperationNotFoundException;
import io.github.acoboh.query.filter.jpa.operations.resolutors.QFPredicateCollectionResolutor;
import io.github.acoboh.query.filter.jpa.processor.match.QFCollectionMatch;

/**
 * Enumeration with all the collection operations
 * 
 * @author Adrián Cobo
 *
 */
public enum QFCollectionOperationEnum implements QFPredicateCollectionResolutor {
	/**
	 * 
	 */
	EQUAL("eq") {
		@Override
		public <C extends Collection<?>> Predicate generateCollectionPredicate(Expression<C> path, CriteriaBuilder cb,
				QFCollectionMatch match, MultiValueMap<String, Object> mlContext) {
			mlContext.add(match.getDefinition().getFilterName(), match.getValue());
			return cb.equal(cb.size(path), match.getValue());
		}
	},
	/**
	* 
	*/
	NOT_EQUAL("ne") {
		@Override
		public <C extends Collection<?>> Predicate generateCollectionPredicate(Expression<C> path, CriteriaBuilder cb,
				QFCollectionMatch match, MultiValueMap<String, Object> mlContext) {
			mlContext.add(match.getDefinition().getFilterName(), match.getValue());
			return cb.notEqual(cb.size(path), match.getValue());
		}
	},
	/**
	* 
	*/
	GREATER_THAN("gt") {
		@Override
		public <C extends Collection<?>> Predicate generateCollectionPredicate(Expression<C> path, CriteriaBuilder cb,
				QFCollectionMatch match, MultiValueMap<String, Object> mlContext) {
			mlContext.add(match.getDefinition().getFilterName(), match.getValue());
			return cb.greaterThan(cb.size(path), match.getValue());
		}
	},
	/**
	* 
	*/
	GREATER_EQUAL_THAN("gte") {
		@Override
		public <C extends Collection<?>> Predicate generateCollectionPredicate(Expression<C> path, CriteriaBuilder cb,
				QFCollectionMatch match, MultiValueMap<String, Object> mlContext) {
			mlContext.add(match.getDefinition().getFilterName(), match.getValue());
			return cb.greaterThanOrEqualTo(cb.size(path), match.getValue());
		}
	},
	/**
	* 
	*/
	LESS_THAN("lt") {
		@Override
		public <C extends Collection<?>> Predicate generateCollectionPredicate(Expression<C> path, CriteriaBuilder cb,
				QFCollectionMatch match, MultiValueMap<String, Object> mlContext) {
			mlContext.add(match.getDefinition().getFilterName(), match.getValue());
			return cb.lessThan(cb.size(path), match.getValue());
		}
	},
	/**
	* 
	*/
	LESS_EQUAL_THAN("lte") {
		@Override
		public <C extends Collection<?>> Predicate generateCollectionPredicate(Expression<C> path, CriteriaBuilder cb,
				QFCollectionMatch match, MultiValueMap<String, Object> mlContext) {
			mlContext.add(match.getDefinition().getFilterName(), match.getValue());
			return cb.lessThanOrEqualTo(cb.size(path), match.getValue());
		}
	};

	private static final Map<String, QFCollectionOperationEnum> CONSTANTS = new HashMap<>();

	static {
		for (QFCollectionOperationEnum c : values()) {
			CONSTANTS.put(c.operation, c);
		}
	}

	private final String operation;

	QFCollectionOperationEnum(String operation) {
		this.operation = operation;
	}

	@Override
	public String getOperation() {
		return operation;
	}

	/**
	 * Find operation from the parameter value
	 *
	 * @param value parameter value
	 * @return operation found
	 * @throws io.github.acoboh.query.filter.jpa.exceptions.QFOperationNotFoundException if the operation is not found
	 */
	public static QFCollectionOperationEnum fromValue(String value) throws QFOperationNotFoundException {
		QFCollectionOperationEnum constant = CONSTANTS.get(value);
		if (constant == null) {
			throw new QFOperationNotFoundException(value);
		}
		return constant;
	}

}
