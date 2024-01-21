package io.github.acoboh.query.filter.jpa.operations;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.github.acoboh.query.filter.jpa.exceptions.QFOperationNotFoundException;
import io.github.acoboh.query.filter.jpa.operations.resolutors.QFPredicateJsonResolutor;
import io.github.acoboh.query.filter.jpa.processor.match.QFJsonElementMatch;

/**
 * Enum with all JSON operations availables
 */
public enum QFOperationJsonEnum implements QFPredicateJsonResolutor {

	/**
	 * 
	 */
	EQUAL("eq") {
		@Override
		public Predicate generateJsonPredicate(Path<?> path, CriteriaBuilder cb, QFJsonElementMatch match) {
			Predicate finalPredicate = cb.conjunction();

			for (Map.Entry<String, String> nodeEntry : match.getMapValues().entrySet()) {

				finalPredicate.getExpressions()
						.add(cb.equal(cb.function(EXTRACT_FUNCTION, String.class, path, cb.literal(nodeEntry.getKey())),
								nodeEntry.getValue()));
			}

			return finalPredicate;
		}
	},
	/**
	* 
	*/
	NOT_EQUAL("ne") {
		@Override
		public Predicate generateJsonPredicate(Path<?> path, CriteriaBuilder cb, QFJsonElementMatch match) {
			Predicate finalPredicate = cb.conjunction();

			for (Map.Entry<String, String> nodeEntry : match.getMapValues().entrySet()) {

				finalPredicate.getExpressions()
						.add(cb.notEqual(
								cb.function(EXTRACT_FUNCTION, String.class, path, cb.literal(nodeEntry.getKey())),
								nodeEntry.getValue()));
			}

			return finalPredicate;
		}
	},
	/**
	* 
	*/
	LIKE("like") {
		@Override
		public Predicate generateJsonPredicate(Path<?> path, CriteriaBuilder cb, QFJsonElementMatch match) {
			Predicate finalPredicate = cb.conjunction();

			for (Map.Entry<String, String> nodeEntry : match.getMapValues().entrySet()) {

				finalPredicate.getExpressions()
						.add(PredicateUtils.parseLikePredicate(cb,
								cb.function(EXTRACT_FUNCTION, String.class, path, cb.literal(nodeEntry.getKey())),
								nodeEntry.getValue(), match.getDefinition().isCaseSensitive()));
			}

			return finalPredicate;
		}
	},
	/**
	* 
	*/
	STARTS_WITH("starts") {
		@Override
		public Predicate generateJsonPredicate(Path<?> path, CriteriaBuilder cb, QFJsonElementMatch match) {
			Predicate finalPredicate = cb.conjunction();

			for (Map.Entry<String, String> nodeEntry : match.getMapValues().entrySet()) {

				finalPredicate.getExpressions()
						.add(PredicateUtils.parseStartsPredicate(cb,
								cb.function(EXTRACT_FUNCTION, String.class, path, cb.literal(nodeEntry.getKey())),
								nodeEntry.getValue(), match.getDefinition().isCaseSensitive()));
			}

			return finalPredicate;
		}
	},
	/**
	* 
	*/
	ENDS_WITH("ends") {
		@Override
		public Predicate generateJsonPredicate(Path<?> path, CriteriaBuilder cb, QFJsonElementMatch match) {
			Predicate finalPredicate = cb.conjunction();

			for (Map.Entry<String, String> nodeEntry : match.getMapValues().entrySet()) {

				finalPredicate.getExpressions()
						.add(PredicateUtils.parseEndsPredicate(cb,
								cb.function(EXTRACT_FUNCTION, String.class, path, cb.literal(nodeEntry.getKey())),
								nodeEntry.getValue(), match.getDefinition().isCaseSensitive()));
			}

			return finalPredicate;
		}
	};

	private static final Map<String, QFOperationJsonEnum> CONSTANTS = new HashMap<>();
	private static final String EXTRACT_FUNCTION = "jsonb_extract_path_text";

	static {
		for (QFOperationJsonEnum c : values()) {
			CONSTANTS.put(c.operation, c);
		}
	}

	private final String operation;

	QFOperationJsonEnum(String operation) {
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
	public static QFOperationJsonEnum fromValue(String value) throws QFOperationNotFoundException {
		QFOperationJsonEnum constant = CONSTANTS.get(value);
		if (constant == null) {
			throw new QFOperationNotFoundException(value);
		}
		return constant;
	}

}
