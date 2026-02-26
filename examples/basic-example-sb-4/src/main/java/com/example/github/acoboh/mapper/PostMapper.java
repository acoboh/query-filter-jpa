package com.example.github.acoboh.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.example.github.acoboh.domain.PostDTO;
import com.example.github.acoboh.entities.PostBlog;

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
