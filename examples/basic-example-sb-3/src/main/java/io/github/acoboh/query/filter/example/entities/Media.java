package io.github.acoboh.query.filter.example.entities;

import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "media_type", discriminatorType = DiscriminatorType.STRING)
public abstract class Media extends BaseEntity {

	private String url;

	@JoinColumn(name = "post_id")
	@ManyToOne
	private PostBlog post;

	protected Media() {
		// Empty for JPA
	}

	public Media(MediaType mediaType, String url, PostBlog post) {
		super();
		this.url = url;
		this.post = post;
	}

	public String getUrl() {
		return url;
	}

	public PostBlog getPost() {
		return post;
	}

}
