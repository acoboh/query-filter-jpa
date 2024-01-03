package io.github.acoboh.query.filter.example.entities;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

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
