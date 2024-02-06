package io.github.acoboh.query.filter.jpa.model;

import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * Extra data entity
 */
@Entity
public class ExtraData {

	@Id
	private int id;

	@ManyToOne
	private Comments comment;

	/**
	 * Default constructor
	 */
	public ExtraData() {
		super();
	}

	/**
	 * All fields constructor
	 * 
	 * @param id      id
	 * @param comment comment
	 */
	public ExtraData(int id, Comments comment) {
		super();
		this.id = id;
		this.comment = comment;
	}

	/**
	 * Get ID
	 * 
	 * @return ID
	 */
	public int getId() {
		return id;
	}

	/**
	 * Set ID
	 * 
	 * @param id New ID
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Get comment
	 * 
	 * @return comment
	 */
	public Comments getComment() {
		return comment;
	}

	/**
	 * Set new comment
	 * 
	 * @param comment new comment
	 */
	public void setComment(Comments comment) {
		this.comment = comment;
	}

	@Override
	public int hashCode() {
		return Objects.hash(comment, id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExtraData other = (ExtraData) obj;
		return Objects.equals(comment, other.comment) && Objects.equals(id, other.id);
	}

	@Override
	public String toString() {
		return "ExtraData [id=" + id + "]";
	}

}
