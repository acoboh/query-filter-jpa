package com.example.github.acoboh.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("VIDEO")
public class MediaVideo extends Media {

	private int duration;

	protected MediaVideo() {
		// Emtpy for JPA
	}

	public MediaVideo(MediaType mediaType, String url, PostBlog post, int duration) {
		super(mediaType, url, post);
		this.duration = duration;
	}

	public int getDuration() {
		return duration;
	}

}
