package io.github.acoboh.query.filter.jpa.model.discriminators.joined;

import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

/**
 * Parent entity
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class ParentEntity {

	/**
	 * Type enum
	 */
	public enum TypeEnum {
		/**
		 * A type
		 */
		A,
		/**
		 * B type
		 */
		B
	}

	@Id
	private String id;

	private TypeEnum type;

	private boolean active = false;

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
	 * Get the type
	 * 
	 * @return the type
	 */
	public TypeEnum getType() {
		return type;
	}

	/**
	 * Set the type
	 * 
	 * @param type
	 */
	public void setType(TypeEnum type) {
		this.type = type;
	}

	/**
	 * Get the active
	 * 
	 * @return the active
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * Set the active
	 * 
	 * @param active
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	@Override
	public int hashCode() {
		return Objects.hash(active, id, type);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ParentEntity other = (ParentEntity) obj;
		return active == other.active && Objects.equals(id, other.id) && type == other.type;
	}

}
