package io.github.acoboh.query.filter.example.entities;

import java.util.Objects;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.GenericGenerator;

@MappedSuperclass
public class BaseEntity {

	@Id
	@GeneratedValue(generator = "tsid")
	@GenericGenerator(name = "tsid", strategy = "io.hypersistence.utils.hibernate.id.TsidGenerator")
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
