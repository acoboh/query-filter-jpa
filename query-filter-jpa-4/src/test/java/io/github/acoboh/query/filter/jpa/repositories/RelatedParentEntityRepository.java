package io.github.acoboh.query.filter.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import io.github.acoboh.query.filter.jpa.model.discriminators.joined.RelatedParent;

/**
 * Related parent entity repository
 */
public interface RelatedParentEntityRepository
		extends
			JpaSpecificationExecutor<RelatedParent>,
			JpaRepository<RelatedParent, String> {

}
