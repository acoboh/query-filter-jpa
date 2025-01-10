package io.github.acoboh.query.filter.jpa.model.discriminators.joined;

import java.util.Objects;

import javax.persistence.Entity;

/**
 * Subclass A entity
 */
@Entity
public class SubclassAEntity extends ParentEntity {

	private String subClassField;

	private boolean flag;

	/**
	 * Get the sub class field
	 *
	 * @return the sub class field
	 */
	public String getSubClassField() {
		return subClassField;
	}

	/**
	 * Set the subclass field
	 *
	 * @param subClassField
	 */
	public void setSubClassField(String subClassField) {
		this.subClassField = subClassField;
	}

	/**
	 * Get the flag
	 *
	 * @return the flag
	 */
	public boolean isFlag() {
		return flag;
	}

	/**
	 * Set the flag
	 *
	 * @param flag
	 */
	public void setFlag(boolean flag) {
		this.flag = flag;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(flag, subClassField);
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
		SubclassAEntity other = (SubclassAEntity) obj;
		return flag == other.flag && Objects.equals(subClassField, other.subClassField);
	}

}
