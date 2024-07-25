package io.github.acoboh.query.filter.jpa.model.discriminators.joined;

import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/**
 * Related parent entity
 */
@Entity
public class RelatedParent {

	@Id
	private String id;

	@ManyToOne
	@JoinColumn(name = "parent_id")
	private ParentEntity parent;

	/**
	 * Get the id
	 * 
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Set the id
	 * 
	 * @param id
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Get the parent
	 * 
	 * @return the parent
	 */
	public ParentEntity getParent() {
		return parent;
	}

	/**
	 * Set the parent
	 * 
	 * @param parent
	 */
	public void setParent(ParentEntity parent) {
		this.parent = parent;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, parent);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RelatedParent other = (RelatedParent) obj;
		return Objects.equals(id, other.id) && Objects.equals(parent, other.parent);
	}

}
