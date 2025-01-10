package io.github.acoboh.query.filter.jpa.repositories;

import io.github.acoboh.query.filter.jpa.model.PostBlog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

/**
 * Post Blog repository
 *
 * @author Adrián Cobo
 */
public interface PostBlogRepository extends JpaSpecificationExecutor<PostBlog>, JpaRepository<PostBlog, UUID> {

}
