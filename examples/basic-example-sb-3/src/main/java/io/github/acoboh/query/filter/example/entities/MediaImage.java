package io.github.acoboh.query.filter.example.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("IMAGE")
public class MediaImage extends Media {

	private int width;

	private int height;

	protected MediaImage() {
		// Empty for JPA
	}

	public MediaImage(MediaType mediaType, String url, PostBlog post, int width, int height) {
		super(mediaType, url, post);
		this.width = width;
		this.height = height;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

}
