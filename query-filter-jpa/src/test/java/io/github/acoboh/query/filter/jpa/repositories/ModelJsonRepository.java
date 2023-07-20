package io.github.acoboh.query.filter.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import io.github.acoboh.query.filter.jpa.model.jsondata.ModelJson;

public interface ModelJsonRepository extends JpaSpecificationExecutor<ModelJson>, JpaRepository<ModelJson, Long> {

}
