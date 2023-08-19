package io.github.acoboh.query.filter.example.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.github.acoboh.query.filter.example.filterdef.PostFilterDef;
import io.github.acoboh.query.filter.example.model.PostBlog;
import io.github.acoboh.query.filter.example.services.PostBlogService;
import io.github.acoboh.query.filter.jpa.annotations.QFParam;
import io.github.acoboh.query.filter.jpa.processor.QueryFilter;

@RestController
@RequestMapping("/posts")
public class PostRestController {

	@Autowired
	private PostBlogService service;

	@GetMapping
	public List<PostBlog> getPosts(
			@QFParam(PostFilterDef.class) @RequestParam(required = false) QueryFilter<PostBlog> filter) {
		return service.getPosts(filter);
	}

	@PostMapping
	public Long createPost(@RequestBody PostBlog post) {
		return service.createPost(post);
	}

	@GetMapping("/{uuid}")
	public PostBlog getPost(@PathVariable Long uuid) {
		return service.getPost(uuid);
	}

	@PostMapping("/{uuid}")
	public void updatePost(@PathVariable Long uuid, @RequestBody PostBlog post) {
		service.updatePost(uuid, post);
	}

	@DeleteMapping("/{uuid}")
	public void deletePost(@PathVariable Long uuid) {
		service.deletePost(uuid);
	}
}
