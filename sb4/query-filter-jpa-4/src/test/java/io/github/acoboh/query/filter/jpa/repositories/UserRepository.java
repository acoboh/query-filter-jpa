package io.github.acoboh.query.filter.jpa.repositories;

import io.github.acoboh.query.filter.jpa.model.subquery.UserModel;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * User repository
 *
 * @author Adrián Cobo
 */
public interface UserRepository extends JpaSpecificationExecutor<@NonNull UserModel>, JpaRepository<@NonNull UserModel, @NonNull Long> {

}
