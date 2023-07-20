package io.github.acoboh.query.filter.jpa.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import io.github.acoboh.query.filter.jpa.model.PostBlog;

public interface PostBlogRepository extends JpaSpecificationExecutor<PostBlog>, JpaRepository<PostBlog, UUID> {

}
