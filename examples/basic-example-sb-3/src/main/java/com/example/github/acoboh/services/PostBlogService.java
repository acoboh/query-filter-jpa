package com.example.github.acoboh.services;

import org.springframework.data.domain.Page;

import com.example.github.acoboh.domain.PostDTO;
import com.example.github.acoboh.entities.PostBlog;
import io.github.acoboh.query.filter.jpa.processor.QueryFilter;

public interface PostBlogService {

	public Page<PostDTO> getPosts(QueryFilter<PostBlog> filter, int page, int size);

	public String createPost(PostDTO post);

	public PostDTO getPost(String uuid);

	public void updatePost(String uuid, PostDTO post);

	public void deletePost(String uuid);
}
