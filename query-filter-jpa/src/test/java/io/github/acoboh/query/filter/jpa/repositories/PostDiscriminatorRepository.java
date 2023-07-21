package io.github.acoboh.query.filter.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import io.github.acoboh.query.filter.jpa.model.discriminators.Topic;

/**
 * Post Discriminator repository
 * 
 * @author Adrián Cobo
 *
 */
public interface PostDiscriminatorRepository extends JpaSpecificationExecutor<Topic>, JpaRepository<Topic, Long> {

}
