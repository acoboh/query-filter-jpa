package com.example.github.acoboh.entities;

import java.util.Objects;

import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public class BaseEntity {

	@Id
	@Tsid
	private String tsid;

	protected BaseEntity() {
		// Empty for JPA
	}

	public String getTsid() {
		return tsid;
	}

	@Override
	public int hashCode() {
		return Objects.hash(tsid);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BaseEntity other = (BaseEntity) obj;
		return Objects.equals(tsid, other.tsid);
	}

	@Override
	public String toString() {
		return "BaseEntity [tsid=" + tsid + "]";
	}

}
