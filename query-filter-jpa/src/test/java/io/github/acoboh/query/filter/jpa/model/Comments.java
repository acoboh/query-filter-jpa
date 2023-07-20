package io.github.acoboh.query.filter.jpa.model;

import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class Comments {

	@Id
	private int id;

	private String author;

	@ManyToOne
	private PostBlog postBlog;

	private String comment;

	private int likes;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public PostBlog getPostBlog() {
		return postBlog;
	}

	public void setPostBlog(PostBlog postBlog) {
		this.postBlog = postBlog;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public int getLikes() {
		return likes;
	}

	public void setLikes(int likes) {
		this.likes = likes;
	}

	@Override
	public int hashCode() {
		return Objects.hash(author, comment, id, likes, postBlog);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Comments other = (Comments) obj;
		return Objects.equals(author, other.author) && Objects.equals(comment, other.comment) && id == other.id
				&& likes == other.likes && Objects.equals(postBlog, other.postBlog);
	}

}
