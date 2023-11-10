package io.github.acoboh.query.filter.example.services.impl;

import org.mapstruct.factory.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import io.github.acoboh.query.filter.example.domain.PostDTO;
import io.github.acoboh.query.filter.example.entities.PostBlog;
import io.github.acoboh.query.filter.example.exceptions.ResourceNotFoundException;
import io.github.acoboh.query.filter.example.mapper.PostMapper;
import io.github.acoboh.query.filter.example.repositories.PostBlogRepository;
import io.github.acoboh.query.filter.example.services.PostBlogService;
import io.github.acoboh.query.filter.jpa.processor.QueryFilter;

@Service
class PostBlogServiceImpl implements PostBlogService {

	private static final Logger log = LoggerFactory.getLogger(PostBlogServiceImpl.class);
	private static final PostMapper mapper = Mappers.getMapper(PostMapper.class);

	@Autowired
	private PostBlogRepository repository;

	@Override
	public Page<PostDTO> getPosts(QueryFilter<PostBlog> filter, int page, int size) {
		log.debug("Getting posts page {} size {} filter {}", page, size, filter);
		Page<PostBlog> pageSlice = repository.findAll(filter, PageRequest.of(page, size));
		return pageSlice.map(mapper::postToPostDTO);
	}

	@Override
	public PostDTO getPost(String uuid) {

		PostBlog post = repository.findById(uuid)
				.orElseThrow(() -> new ResourceNotFoundException("Post not found with id " + uuid));

		return mapper.postToPostDTO(post);
	}

	@Override
	public String createPost(PostDTO post) {
		PostBlog createdPost = mapper.postDTOToPost(post);
		return repository.save(createdPost).getTsid();
	}

	@Override
	public void updatePost(String uuid, PostDTO dto) {
		PostBlog postBlog = repository.findById(uuid)
				.orElseThrow(() -> new ResourceNotFoundException("Post not found with id " + uuid));

		postBlog = mapper.updatePostBlog(postBlog, dto);
		repository.save(postBlog);

	}

	@Override
	public void deletePost(String uuid) {
		PostBlog postBlog = repository.findById(uuid)
				.orElseThrow(() -> new ResourceNotFoundException("Post not found with id " + uuid));

		repository.delete(postBlog);

	}

}
