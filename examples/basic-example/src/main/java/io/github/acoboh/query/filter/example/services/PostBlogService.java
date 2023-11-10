package io.github.acoboh.query.filter.example.services;

import org.springframework.data.domain.Page;

import io.github.acoboh.query.filter.example.domain.PostDTO;
import io.github.acoboh.query.filter.example.entities.PostBlog;
import io.github.acoboh.query.filter.jpa.processor.QueryFilter;

public interface PostBlogService {

	public Page<PostDTO> getPosts(QueryFilter<PostBlog> filter, int page, int size);

	public String createPost(PostDTO post);

	public PostDTO getPost(String uuid);

	public void updatePost(String uuid, PostDTO post);

	public void deletePost(String uuid);
}
