package io.github.acoboh.query.filter.jpa.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

/**
 * Comments entity
 *
 * @author Adrián Cobo
 */
@Entity
public class Comments {

	@Id
	private int id;

	private String author;

	@ManyToOne
	private PostBlog postBlog;

	private String comment;

	private int likes;

	@OneToMany(mappedBy = "comment", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ExtraData> extraData = new ArrayList<>();

	/**
	 * Get ID
	 *
	 * @return ID
	 */
	public int getId() {
		return id;
	}

	/**
	 * Set new ID
	 *
	 * @param id
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Get author
	 *
	 * @return author
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * Set new author
	 *
	 * @param author
	 *            new author
	 */
	public void setAuthor(String author) {
		this.author = author;
	}

	/**
	 * Get post blog
	 *
	 * @return post blog
	 */
	public PostBlog getPostBlog() {
		return postBlog;
	}

	/**
	 * Set new post blog
	 *
	 * @param postBlog
	 *            new post blog
	 */
	public void setPostBlog(PostBlog postBlog) {
		this.postBlog = postBlog;
	}

	/**
	 * Get comment
	 *
	 * @return comment
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * Set new comment
	 *
	 * @param comment
	 *            new comment
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * Get likes
	 *
	 * @return likes
	 */
	public int getLikes() {
		return likes;
	}

	/**
	 * Set new likes
	 *
	 * @param likes
	 *            new likes
	 */
	public void setLikes(int likes) {
		this.likes = likes;
	}

	/**
	 * Get extra data
	 *
	 * @return extra data
	 */
	public List<ExtraData> getExtraData() {
		return extraData;
	}

	/**
	 * Set extra data
	 *
	 * @param extraData
	 *            extra data
	 */
	public void setExtraData(List<ExtraData> extraData) {
		this.extraData = extraData;
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

	@Override
	public String toString() {
		return "Comments [id=" + id + ", author=" + author + ", comment=" + comment + ", likes=" + likes
				+ ", extraData=" + extraData + "]";
	}

}
