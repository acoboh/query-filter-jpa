package io.github.acoboh.query.filter.example.entities;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "media_type", discriminatorType = DiscriminatorType.STRING)
public abstract class Media extends BaseEntity {

	@Enumerated(EnumType.STRING)
	private MediaType mediaType;

	private String url;

	@JoinColumn(name = "post_id")
	private PostBlog post;

	protected Media() {
		// Empty for JPA
	}

	public Media(MediaType mediaType, String url, PostBlog post) {
		super();
		this.mediaType = mediaType;
		this.url = url;
		this.post = post;
	}

	public MediaType getMediaType() {
		return mediaType;
	}

	public String getUrl() {
		return url;
	}

	public PostBlog getPost() {
		return post;
	}

}
