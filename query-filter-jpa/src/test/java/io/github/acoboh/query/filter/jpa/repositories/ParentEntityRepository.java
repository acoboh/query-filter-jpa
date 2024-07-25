package io.github.acoboh.query.filter.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import io.github.acoboh.query.filter.jpa.model.discriminators.joined.ParentEntity;

/**
 * Parent entity repository
 */
public interface ParentEntityRepository
		extends JpaSpecificationExecutor<ParentEntity>, JpaRepository<ParentEntity, String> {

}
