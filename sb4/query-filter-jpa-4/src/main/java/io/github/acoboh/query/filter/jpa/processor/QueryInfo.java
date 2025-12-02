package io.github.acoboh.query.filter.jpa.processor;

import jakarta.annotation.Nullable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

/**
 * Query information for the {@link QFProcessor} class.
 *
 * @author Adrián Cobo
 * @since 1.0.0
 */
public record QueryInfo<E>(Root<E> root, @Nullable CriteriaQuery<?> query, CriteriaBuilder cb, boolean isCount) {
}
