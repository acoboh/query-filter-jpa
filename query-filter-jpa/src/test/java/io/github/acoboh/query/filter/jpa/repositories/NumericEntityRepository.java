package io.github.acoboh.query.filter.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import io.github.acoboh.query.filter.jpa.model.extended.NumericEntity;

public interface NumericEntityRepository
		extends
			JpaRepository<NumericEntity, Long>,
			JpaSpecificationExecutor<NumericEntity> {

}
