package io.github.acoboh.query.filter.example.entities;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

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
