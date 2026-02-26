package com.example.github.acoboh.services;

import com.example.github.acoboh.domain.PostDTO;
import com.example.github.acoboh.entities.PostBlog;
import com.example.github.acoboh.exceptions.ResourceNotFoundException;
import com.example.github.acoboh.mapper.PostMapper;
import com.example.github.acoboh.repositories.PostBlogRepository;
import io.github.acoboh.query.filter.jpa.processor.QueryFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class PostBlogService {

    private static final Logger log = LoggerFactory.getLogger(PostBlogService.class);

    private final PostMapper mapper;
    private final PostBlogRepository repository;

    PostBlogService(PostMapper mapper, PostBlogRepository repository) {
        this.mapper = mapper;
        this.repository = repository;
    }

    public Page<PostDTO> getPosts(QueryFilter<PostBlog> filter, int page, int size) {
        log.debug("Getting posts page {} size {} filter {}", page, size, filter);
        Page<PostBlog> pageSlice = repository.findAll(filter, PageRequest.of(page, size));
        return pageSlice.map(mapper::postToPostDTO);
    }

    public PostDTO getPost(String uuid) {

        PostBlog post = repository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id " + uuid));

        return mapper.postToPostDTO(post);
    }

    public String createPost(PostDTO post) {
        PostBlog createdPost = mapper.postDTOToPost(post);
        return repository.save(createdPost).getTsid();
    }

    public void updatePost(String uuid, PostDTO dto) {
        PostBlog postBlog = repository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id " + uuid));

        postBlog = mapper.updatePostBlog(postBlog, dto);
        repository.save(postBlog);

    }

    public void deletePost(String uuid) {
        PostBlog postBlog = repository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id " + uuid));

        repository.delete(postBlog);

    }

}
