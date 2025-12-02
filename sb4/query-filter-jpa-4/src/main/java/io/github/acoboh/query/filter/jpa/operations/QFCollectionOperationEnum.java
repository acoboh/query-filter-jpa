package io.github.acoboh.query.filter.jpa.operations;

import io.github.acoboh.query.filter.jpa.exceptions.QFOperationNotFoundException;
import io.github.acoboh.query.filter.jpa.operations.resolutors.QFPredicateCollectionResolutor;
import io.github.acoboh.query.filter.jpa.processor.match.QFCollectionMatch;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import org.springframework.util.MultiValueMap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Enumeration with all the collection operations
 *
 * @author Adrián Cobo
 */
public enum QFCollectionOperationEnum implements QFPredicateCollectionResolutor {
    /**
     * Equal operation
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
     * Not equal operation
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
     * Greater than operation
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
     * Greater equal than operation
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
     * Less than operation
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
     * Less equal than operation
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

    /** {@inheritDoc} */
    @Override
    public String getOperation() {
        return operation;
    }

    /**
     * Find operation from the parameter value
     *
     * @param value parameter value
     * @return operation found
     * @throws io.github.acoboh.query.filter.jpa.exceptions.QFOperationNotFoundException if
     *                                                                                   the
     *                                                                                   operation
     *                                                                                   is
     *                                                                                   not
     *                                                                                   found
     */
    public static QFCollectionOperationEnum fromValue(String value) throws QFOperationNotFoundException {
        QFCollectionOperationEnum constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new QFOperationNotFoundException(value);
        }
        return constant;
    }

}
