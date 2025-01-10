package io.github.acoboh.query.filter.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import io.github.acoboh.query.filter.jpa.model.subquery.RoleModel;

/**
 * Role repository
 *
 * @author Adrián Cobo
 */
public interface RoleRepository extends JpaRepository<RoleModel, Long> {

}
