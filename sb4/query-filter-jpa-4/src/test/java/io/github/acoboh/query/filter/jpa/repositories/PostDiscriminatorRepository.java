package io.github.acoboh.query.filter.jpa.repositories;

import io.github.acoboh.query.filter.jpa.model.discriminators.Topic;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Post Discriminator repository
 *
 * @author Adrián Cobo
 */
public interface PostDiscriminatorRepository extends JpaSpecificationExecutor<@NonNull Topic>, JpaRepository<@NonNull Topic, @NonNull Long> {

}
