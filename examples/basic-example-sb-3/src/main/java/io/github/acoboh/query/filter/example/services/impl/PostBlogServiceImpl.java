package io.github.acoboh.query.filter.example.services.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.github.acoboh.query.filter.example.exceptions.ResourceNotFoundException;
import io.github.acoboh.query.filter.example.model.PostBlog;
import io.github.acoboh.query.filter.example.repositories.PostBlogRepository;
import io.github.acoboh.query.filter.example.services.PostBlogService;
import io.github.acoboh.query.filter.jpa.processor.QueryFilter;

@Service
class PostBlogServiceImpl implements PostBlogService {

	@Autowired
	private PostBlogRepository repository;

	@Override
	public List<PostBlog> getPosts(QueryFilter<PostBlog> filter) {
		return repository.findAll(filter);
	}

	@Override
	public Long createPost(PostBlog post) {
		return repository.save(post).getTsid();
	}

	@Override
	public PostBlog getPost(Long uuid) {
		return repository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException("Post not found"));
	}

	@Override
	public void updatePost(Long uuid, PostBlog post) {
		Optional<PostBlog> postOptional = repository.findById(uuid);
		PostBlog existingPost = postOptional.orElseThrow(() -> new ResourceNotFoundException("Post not found"));

		existingPost.setAuthor(post.getAuthor());
		existingPost.setText(post.getText());
		existingPost.setAvgNote(post.getAvgNote());
		existingPost.setLikes(post.getLikes());
		existingPost.setLastTimestamp(post.getLastTimestamp());
		existingPost.setPublished(post.isPublished());
		existingPost.setPostType(post.getPostType());

		repository.save(existingPost);

	}

	@Override
	public void deletePost(Long uuid) {
		repository.deleteById(uuid);
	}

}
