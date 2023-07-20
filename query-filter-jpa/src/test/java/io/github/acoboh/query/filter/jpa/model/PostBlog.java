package io.github.acoboh.query.filter.jpa.model;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import io.hypersistence.utils.hibernate.type.array.StringArrayType;

/**
 * @author acobo
 *
 */
@Entity
@TypeDef(name = "string-array", typeClass = StringArrayType.class)
public class PostBlog {

	public enum PostType {
		VIDEO, TEXT
	}

	@Id
	private UUID uuid;

	private String author;

	private String text;

	private double avgNote;

	private int likes;

	private LocalDateTime createDate;

	private Timestamp lastTimestamp;

	private boolean published;

	@Enumerated(EnumType.STRING)
	private PostType postType;

	@OneToMany(mappedBy = "postBlog", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<Comments> comments;

	@Type(type = "string-array")
	@Column(columnDefinition = "varchar[]")
	private String[] tags;

	public PostBlog() {
		super();
	}

	@Override
	public int hashCode() {
		return Objects.hash(author, avgNote, createDate, lastTimestamp, likes, postType, published, text, uuid);
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
				&& Objects.equals(text, other.text) && Objects.equals(uuid, other.uuid)
				&& Arrays.equals(tags, other.tags);
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
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

	public Set<Comments> getComments() {
		return comments;
	}

	public void setComments(Set<Comments> comments) {
		this.comments = comments;
	}

	public String[] getTags() {
		return tags;
	}

	public void setTags(String[] tags) {
		this.tags = tags;
	}

	@Override
	public String toString() {
		return "PostBlog [uuid=" + uuid + ", author=" + author + ", text=" + text + ", avgNote=" + avgNote + ", likes="
				+ likes + ", createDate=" + createDate + ", lastTimestamp=" + lastTimestamp + ", published=" + published
				+ ", postType=" + postType + ", tags=" + Arrays.toString(tags) + "]";
	}

}
