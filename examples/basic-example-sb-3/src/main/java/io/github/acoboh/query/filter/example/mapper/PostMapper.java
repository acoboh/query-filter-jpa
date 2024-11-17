package io.github.acoboh.query.filter.example.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import io.github.acoboh.query.filter.example.domain.PostDTO;
import io.github.acoboh.query.filter.example.entities.PostBlog;

@Mapper(componentModel = "spring")
public interface PostMapper {

	PostDTO postToPostDTO(PostBlog post);

	@Mapping(target = "comments", ignore = true)
	@Mapping(target = "media", ignore = true)
	PostBlog postDTOToPost(PostDTO post);

	@Mapping(target = "comments", ignore = true)
	@Mapping(target = "media", ignore = true)
	PostBlog updatePostBlog(@MappingTarget PostBlog postBlog, PostDTO dto);

}
