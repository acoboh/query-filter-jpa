package io.github.acoboh.query.filter.jpa.model.jsondata;

import java.util.Map;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import io.hypersistence.utils.hibernate.type.json.JsonType;

/**
 * Entity with JSON data types
 *
 * @author Adrián Cobo
 */
@Entity
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@TypeDef(name = "json", typeClass = JsonType.class)
public class ModelJson {

	@Id
	@GeneratedValue
	private Long id;

	private String descriptor;

	@Type(type = "jsonb")
	@Column(columnDefinition = "jsonb")
	private Map<String, String> jsonbData;

	/**
	 * Empty for JPA
	 */
	protected ModelJson() {
		// Empty for JPA
	}

	/**
	 * Default constructor
	 *
	 * @param descriptor
	 *            descriptor
	 * @param jsonbData
	 *            json data
	 */
	public ModelJson(String descriptor, Map<String, String> jsonbData) {
		this.jsonbData = jsonbData;
	}

	/**
	 * Get ID
	 *
	 * @return ID
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Get descriptor
	 *
	 * @return descriptor
	 */
	public String getDescriptor() {
		return descriptor;
	}

	/**
	 * Get JSON data
	 *
	 * @return JSON data
	 */
	public Map<String, String> getJsonbData() {
		return jsonbData;
	}

	@Override
	public int hashCode() {
		return Objects.hash(descriptor, id, jsonbData);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ModelJson other = (ModelJson) obj;
		return Objects.equals(descriptor, other.descriptor) && Objects.equals(id, other.id)
				&& Objects.equals(jsonbData, other.jsonbData);
	}

}
