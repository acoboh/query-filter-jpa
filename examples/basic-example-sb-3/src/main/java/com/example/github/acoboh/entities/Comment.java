package com.example.github.acoboh.entities;

import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Comment extends BaseEntity {

	private String comment;

	@JoinColumn(name = "post_id")
	@ManyToOne
	private PostBlog post;

	public Comment(String comment, PostBlog post) {
		super();
		this.comment = comment;
		this.post = post;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(comment, post);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Comment other = (Comment) obj;
		return Objects.equals(comment, other.comment) && Objects.equals(post, other.post);
	}

}
