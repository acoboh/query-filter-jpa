package io.github.acoboh.query.filter.jpa.model;

import java.sql.Timestamp;
import java.time.Instant;
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
 * Post Blog entity model
 *
 * @author acobo
 */
@Entity
@TypeDef(name = "string-array", typeClass = StringArrayType.class)
public class PostBlog {

	/**
	 * Post Type enumeration
	 *
	 * @author Adrián Cobo
	 */
	public enum PostType {
		/**
		 * Video type
		 */
		VIDEO,
		/**
		 * Text type
		 */
		TEXT
	}

	@Id
	private UUID uuid;

	private String author;

	private String text;

	private double avgNote;

	private int likes;

	private LocalDateTime createDate;

	private Timestamp lastTimestamp;

	private Instant instant;

	private boolean published;

	@Enumerated(EnumType.STRING)
	private PostType postType;

	@OneToMany(mappedBy = "postBlog", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<Comments> comments;

	@Type(type = "string-array")
	@Column(columnDefinition = "varchar[]")
	private String[] tags;

	/**
	 * Default constructor
	 */
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
				&& instant.equals(other.instant) && likes == other.likes && postType == other.postType
				&& published == other.published && Objects.equals(text, other.text) && Objects.equals(uuid, other.uuid)
				&& Arrays.equals(tags, other.tags);
	}

	/**
	 * Get UUID
	 *
	 * @return UUID
	 */
	public UUID getUuid() {
		return uuid;
	}

	/**
	 * Set new uuid
	 *
	 * @param uuid
	 *            new uuid
	 */
	public void setUuid(UUID uuid) {
		this.uuid = uuid;
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
	 * Get text
	 *
	 * @return text
	 */
	public String getText() {
		return text;
	}

	/**
	 * Set new text
	 *
	 * @param text
	 *            new text
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * Get average note
	 *
	 * @return average note
	 */
	public double getAvgNote() {
		return avgNote;
	}

	/**
	 * Set new average note
	 *
	 * @param avgNote
	 *            average note
	 */
	public void setAvgNote(double avgNote) {
		this.avgNote = avgNote;
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
	 * Set likes
	 *
	 * @param likes
	 *            new likes
	 */
	public void setLikes(int likes) {
		this.likes = likes;
	}

	/**
	 * Get create date
	 *
	 * @return create date
	 */
	public LocalDateTime getCreateDate() {
		return createDate;
	}

	/**
	 * Set new create date
	 *
	 * @param createDate
	 *            new create date
	 */
	public void setCreateDate(LocalDateTime createDate) {
		this.createDate = createDate;
	}

	/**
	 * Get last timestamp
	 *
	 * @return last timestamp
	 */
	public Timestamp getLastTimestamp() {
		return lastTimestamp;
	}

	/**
	 * Set new last timestamp
	 *
	 * @param lastTimestamp
	 *            new last timestamp
	 */
	public void setLastTimestamp(Timestamp lastTimestamp) {
		this.lastTimestamp = lastTimestamp;
	}

	/**
	 * Get if is published
	 *
	 * @return published
	 */
	public boolean isPublished() {
		return published;
	}

	/**
	 * Set published
	 *
	 * @param published
	 *            new status
	 */
	public void setPublished(boolean published) {
		this.published = published;
	}

	/**
	 * Get post type
	 *
	 * @return post type
	 */
	public PostType getPostType() {
		return postType;
	}

	/**
	 * Set new post type
	 *
	 * @param postType
	 *            new post type
	 */
	public void setPostType(PostType postType) {
		this.postType = postType;
	}

	/**
	 * Get comments
	 *
	 * @return comments
	 */
	public Set<Comments> getComments() {
		return comments;
	}

	/**
	 * Set new comments
	 *
	 * @param comments
	 *            new comments
	 */
	public void setComments(Set<Comments> comments) {
		this.comments = comments;
	}

	/**
	 * Get tags
	 *
	 * @return tags
	 */
	public String[] getTags() {
		return tags;
	}

	/**
	 * Set new tags
	 *
	 * @param tags
	 *            new tags
	 */
	public void setTags(String[] tags) {
		this.tags = tags;
	}

	@Override
	public String toString() {
		return "PostBlog [uuid=" + uuid + ", author=" + author + ", text=" + text + ", avgNote=" + avgNote + ", likes="
				+ likes + ", createDate=" + createDate + ", lastTimestamp=" + lastTimestamp + ", published=" + published
				+ ", postType=" + postType + ", comments=" + comments + ", tags=" + Arrays.toString(tags) + "]";
	}

}
