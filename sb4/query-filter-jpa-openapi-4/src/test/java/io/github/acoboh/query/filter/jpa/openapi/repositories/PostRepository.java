package io.github.acoboh.query.filter.jpa.openapi.repositories;

import io.github.acoboh.query.filter.jpa.openapi.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Repository present only to trigger the JPA repository infrastructure (and the
 * shared {@link jakarta.persistence.EntityManager} bean it registers) needed by
 * {@link io.github.acoboh.query.filter.jpa.processor.QFProcessor}.
 *
 * @author Adrián Cobo
 */
public interface PostRepository extends JpaRepository<Post, UUID> {

}
