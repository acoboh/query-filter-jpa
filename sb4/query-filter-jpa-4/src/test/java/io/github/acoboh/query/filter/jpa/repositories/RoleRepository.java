package io.github.acoboh.query.filter.jpa.repositories;

import io.github.acoboh.query.filter.jpa.model.subquery.RoleModel;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Role repository
 *
 * @author Adrián Cobo
 */
public interface RoleRepository extends JpaRepository<@NonNull RoleModel, @NonNull Long> {

}
