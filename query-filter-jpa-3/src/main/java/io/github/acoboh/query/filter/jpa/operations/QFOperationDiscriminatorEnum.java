package io.github.acoboh.query.filter.jpa.operations;

import java.util.HashMap;
import java.util.Map;

import org.springframework.util.MultiValueMap;

import io.github.acoboh.query.filter.jpa.exceptions.QFOperationNotFoundException;
import io.github.acoboh.query.filter.jpa.operations.resolutors.QFPredicateDiscriminatorResolutor;
import io.github.acoboh.query.filter.jpa.processor.match.QFDiscriminatorMatch;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;

/**
 * Enumeration with all the discriminator operations
 *
 * @author Adrián Cobo
 */
public enum QFOperationDiscriminatorEnum implements QFPredicateDiscriminatorResolutor {

	/**
	 * Equal operation
	 */
	EQUAL("eq") {
		@Override
		public Predicate generateDiscriminatorPredicate(Expression<Class<?>> expression, CriteriaBuilder cb,
				QFDiscriminatorMatch match, MultiValueMap<String, Object> mlContext) {
			mlContext.add(match.getDefinition().getFilterName(), match.getMatchingClasses().get(0));
			return cb.equal(expression, match.getMatchingClasses().get(0));
		}
	},
	/**
	 * Not equal operation
	 */
	NOT_EQUAL("ne") {
		@Override
		public Predicate generateDiscriminatorPredicate(Expression<Class<?>> expression, CriteriaBuilder cb,
				QFDiscriminatorMatch match, MultiValueMap<String, Object> mlContext) {
			mlContext.add(match.getDefinition().getFilterName(), match.getMatchingClasses().get(0));
			return cb.notEqual(expression, match.getMatchingClasses().get(0));
		}
	},
	/**
	 * In operation
	 */
	IN("in") {
		@Override
		public Predicate generateDiscriminatorPredicate(Expression<Class<?>> expression, CriteriaBuilder cb,
				QFDiscriminatorMatch match, MultiValueMap<String, Object> mlContext) {
			mlContext.add(match.getDefinition().getFilterName(), match.getMatchingClasses());
			return expression.in(match.getMatchingClasses());
		}
	},
	/**
	 * Not in operation
	 */
	NOT_IN("nin") {
		@Override
		public Predicate generateDiscriminatorPredicate(Expression<Class<?>> expression, CriteriaBuilder cb,
				QFDiscriminatorMatch match, MultiValueMap<String, Object> mlContext) {
			mlContext.add(match.getDefinition().getFilterName(), match.getMatchingClasses());
			return cb.not(expression.in(match.getMatchingClasses()));
		}
	};

	private static final Map<String, QFOperationDiscriminatorEnum> operationMap = new HashMap<>();

	static {
		for (QFOperationDiscriminatorEnum operation : values()) {
			operationMap.put(operation.getOperation(), operation);
		}
	}

	private final String operation;

	QFOperationDiscriminatorEnum(String operation) {
		this.operation = operation;
	}

	/** {@inheritDoc} */
	@Override
	public String getOperation() {
		return operation;
	}

	/**
	 * Get the operation from the value
	 *
	 * @param value
	 *            value of the operation
	 * @return operation
	 */
	public static QFOperationDiscriminatorEnum fromValue(String value) {
		QFOperationDiscriminatorEnum operationEnum = operationMap.get(value);
		if (operationEnum == null) {
			throw new QFOperationNotFoundException(value);
		}
		return operationEnum;
	}

}
