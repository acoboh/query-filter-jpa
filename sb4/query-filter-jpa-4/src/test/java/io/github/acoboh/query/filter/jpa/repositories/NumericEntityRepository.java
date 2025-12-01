package io.github.acoboh.query.filter.jpa.repositories;

import io.github.acoboh.query.filter.jpa.model.extended.NumericEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface NumericEntityRepository
        extends JpaRepository<@NonNull NumericEntity, @NonNull Long>, JpaSpecificationExecutor<@NonNull NumericEntity> {

}
