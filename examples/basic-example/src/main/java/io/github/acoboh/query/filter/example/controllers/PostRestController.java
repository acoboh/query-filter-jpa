package io.github.acoboh.query.filter.example.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.github.acoboh.query.filter.example.domain.PostDTO;
import io.github.acoboh.query.filter.example.entities.PostBlog;
import io.github.acoboh.query.filter.example.filterdef.PostFilterDef;
import io.github.acoboh.query.filter.example.services.PostBlogService;
import io.github.acoboh.query.filter.jpa.annotations.QFParam;
import io.github.acoboh.query.filter.jpa.processor.QueryFilter;

@RestController
@RequestMapping("/posts")
public class PostRestController {

	@Autowired
	private PostBlogService service;

	@GetMapping
	public Page<PostDTO> getPosts(
			@QFParam(PostFilterDef.class) @RequestParam(required = false, defaultValue = "", name = "filter") QueryFilter<PostBlog> filter,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
		return service.getPosts(filter, page, size);
	}

	@PostMapping
	public String createPost(@RequestBody PostDTO post) {
		return service.createPost(post);
	}

	@GetMapping("/{uuid}")
	public PostDTO getPost(@PathVariable String uuid) {
		return service.getPost(uuid);
	}

	@PostMapping("/{uuid}")
	public void updatePost(@PathVariable String uuid, @RequestBody PostDTO post) {
		service.updatePost(uuid, post);
	}

	@DeleteMapping("/{uuid}")
	public void deletePost(@PathVariable String uuid) {
		service.deletePost(uuid);
	}
}
