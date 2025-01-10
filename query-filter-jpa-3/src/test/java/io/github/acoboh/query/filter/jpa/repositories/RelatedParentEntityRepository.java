package io.github.acoboh.query.filter.jpa.repositories;

import io.github.acoboh.query.filter.jpa.model.discriminators.joined.RelatedParent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Related parent entity repository
 */
public interface RelatedParentEntityRepository
        extends JpaSpecificationExecutor<RelatedParent>, JpaRepository<RelatedParent, String> {

}
