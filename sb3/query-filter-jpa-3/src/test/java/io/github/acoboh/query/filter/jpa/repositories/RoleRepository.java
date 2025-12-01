package io.github.acoboh.query.filter.jpa.repositories;

import io.github.acoboh.query.filter.jpa.model.subquery.RoleModel;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Role repository
 *
 * @author Adrián Cobo
 */
public interface RoleRepository extends JpaRepository<RoleModel, Long> {

}
