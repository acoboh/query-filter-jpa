package io.github.acoboh.query.filter.jpa.repositories;

import io.github.acoboh.query.filter.jpa.model.jsondata.ModelJson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Model JSON repository
 *
 * @author Adrián Cobo
 */
public interface ModelJsonRepository extends JpaSpecificationExecutor<ModelJson>, JpaRepository<ModelJson, Long> {

}
