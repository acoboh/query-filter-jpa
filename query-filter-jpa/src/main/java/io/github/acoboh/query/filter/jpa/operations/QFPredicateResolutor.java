package io.github.acoboh.query.filter.jpa.operations;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import org.springframework.util.MultiValueMap;

import io.github.acoboh.query.filter.jpa.processor.QFElementMatch;
import io.github.acoboh.query.filter.jpa.processor.QFJsonElementMatch;

public interface QFPredicateResolutor {

	Predicate generatePredicate(Path<?> path, CriteriaBuilder cb, QFElementMatch match, int index,
			MultiValueMap<String, Object> mlContext);

	Predicate generateJsonPredicate(Path<?> path, CriteriaBuilder cb, QFJsonElementMatch match);

	String getOperation();

}
