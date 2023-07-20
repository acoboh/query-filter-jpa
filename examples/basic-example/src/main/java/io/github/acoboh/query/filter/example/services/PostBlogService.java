package io.github.acoboh.query.filter.example.services;

import java.util.List;

import io.github.acoboh.query.filter.example.model.PostBlog;
import io.github.acoboh.query.filter.jpa.processor.QueryFilter;

public interface PostBlogService {

	public List<PostBlog> getPosts(QueryFilter<PostBlog> filter);

	public Long createPost(PostBlog post);

	public PostBlog getPost(Long uuid);

	public void updatePost(Long uuid, PostBlog post);

	public void deletePost(Long uuid);
}
