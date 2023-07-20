package io.github.acoboh.query.filter.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import io.github.acoboh.query.filter.jpa.model.subquery.UserModel;

public interface UserRepository extends JpaSpecificationExecutor<UserModel>, JpaRepository<UserModel, Long> {

}
