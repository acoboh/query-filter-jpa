package io.github.acoboh.query.filter.example.entities;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;

@Entity
public class PostBlog extends BaseEntity {

	public enum PostType {
		EDUCATIONAL, INFO, SOCIAL, VIDEO
	}

	private String title;

	private String content;

	private int likes;

	@CreatedDate
	private LocalDateTime createDate;

	@LastModifiedBy
	private Timestamp lastTimestamp;

	private boolean published;

	@Enumerated(EnumType.STRING)
	private PostType postType;

	@OneToMany(mappedBy = "post", orphanRemoval = true)
	private List<Comment> comments;

	@OneToMany(mappedBy = "post", orphanRemoval = true)
	private List<Media> media;

	protected PostBlog() {
		// Empty for JPA
	}

	public PostBlog(String title, String content, int likes, PostType postType) {
		super();
		this.title = title;
		this.content = content;
		this.likes = likes;
		this.postType = postType;
	}

	public String getTitle() {
		return title;
	}

	public String getContent() {
		return content;
	}

	public int getLikes() {
		return likes;
	}

	public LocalDateTime getCreateDate() {
		return createDate;
	}

	public Timestamp getLastTimestamp() {
		return lastTimestamp;
	}

	public void setPublished(boolean published) {
		this.published = published;
	}

	public boolean isPublished() {
		return published;
	}

	public PostType getPostType() {
		return postType;
	}

	public List<Comment> getComments() {
		return comments;
	}

	public List<Media> getMedia() {
		return media;
	}

}
