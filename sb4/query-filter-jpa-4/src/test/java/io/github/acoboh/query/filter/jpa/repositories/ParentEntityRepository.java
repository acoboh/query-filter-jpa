package io.github.acoboh.query.filter.jpa.repositories;

import io.github.acoboh.query.filter.jpa.model.discriminators.joined.ParentEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Parent entity repository
 */
public interface ParentEntityRepository
        extends JpaSpecificationExecutor<@NonNull ParentEntity>, JpaRepository<@NonNull ParentEntity, @NonNull String> {

}
