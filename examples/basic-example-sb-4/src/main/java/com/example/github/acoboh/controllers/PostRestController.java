package com.example.github.acoboh.controllers;

import com.example.github.acoboh.domain.PostDTO;
import com.example.github.acoboh.entities.PostBlog;
import com.example.github.acoboh.filterdef.PostFilterDef;
import com.example.github.acoboh.services.PostBlogService;
import io.github.acoboh.query.filter.jpa.annotations.QFMultiParam;
import io.github.acoboh.query.filter.jpa.annotations.QFParam;
import io.github.acoboh.query.filter.jpa.processor.QueryFilter;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts")
public class PostRestController {

    private final PostBlogService service;

    PostRestController(PostBlogService service) {
        this.service = service;
    }

    @GetMapping("/single-param")
    public Page<PostDTO> getPosts(
            @QFParam(PostFilterDef.class) @RequestParam(required = false, defaultValue = "", name = "filter") QueryFilter<PostBlog> filter,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return service.getPosts(filter, page, size);
    }

    @GetMapping("/multi-param")
    public Page<PostDTO> getPostMultiFilter(@QFMultiParam(PostFilterDef.class) QueryFilter<PostBlog> filter,
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
