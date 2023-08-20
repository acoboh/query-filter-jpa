package io.github.acoboh.query.filter.example.model;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Objects;

import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;

@Entity
public class PostBlog {

	public enum PostType {
		VIDEO, TEXT
	}

	@Id
	@Tsid
	private Long tsid;

	private String author;

	private String text;

	private double avgNote;

	private int likes;

	private LocalDateTime createDate;

	private Timestamp lastTimestamp;

	private boolean published;

	@Enumerated(EnumType.STRING)
	private PostType postType;

	public PostBlog() {
		super();
	}

	@Override
	public int hashCode() {
		return Objects.hash(author, avgNote, createDate, lastTimestamp, likes, postType, published, text, tsid);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PostBlog other = (PostBlog) obj;
		return Objects.equals(author, other.author)
				&& Double.doubleToLongBits(avgNote) == Double.doubleToLongBits(other.avgNote)
				&& createDate.equals(other.createDate) && lastTimestamp.equals(other.lastTimestamp)
				&& likes == other.likes && postType == other.postType && published == other.published
				&& Objects.equals(text, other.text) && Objects.equals(tsid, other.tsid);
	}

	public Long getTsid() {
		return tsid;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public double getAvgNote() {
		return avgNote;
	}

	public void setAvgNote(double avgNote) {
		this.avgNote = avgNote;
	}

	public int getLikes() {
		return likes;
	}

	public void setLikes(int likes) {
		this.likes = likes;
	}

	public LocalDateTime getCreateDate() {
		return createDate;
	}

	public void setCreateDate(LocalDateTime createDate) {
		this.createDate = createDate;
	}

	public Timestamp getLastTimestamp() {
		return lastTimestamp;
	}

	public void setLastTimestamp(Timestamp lastTimestamp) {
		this.lastTimestamp = lastTimestamp;
	}

	public boolean isPublished() {
		return published;
	}

	public void setPublished(boolean published) {
		this.published = published;
	}

	public PostType getPostType() {
		return postType;
	}

	public void setPostType(PostType postType) {
		this.postType = postType;
	}

}
