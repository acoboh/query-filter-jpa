package io.github.acoboh.query.filter.jpa.repositories;

import io.github.acoboh.query.filter.jpa.model.discriminators.joined.ParentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Parent entity repository
 */
public interface ParentEntityRepository
        extends JpaSpecificationExecutor<ParentEntity>, JpaRepository<ParentEntity, String> {

}
