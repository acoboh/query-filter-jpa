package io.github.acoboh.query.filter.jpa.model.subquery;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

/**
 * User entity model
 *
 * @author Adrián Cobo
 */
@Entity
public class UserModel {

	@Id
	@GeneratedValue
	private Long id;

	private String username;

	@ManyToMany
	@JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
	private List<RoleModel> roles = new ArrayList<>();

	/**
	 * Get ID
	 *
	 * @return ID
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Get Username
	 *
	 * @return username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Set new user name
	 *
	 * @param username
	 *            new user name
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Get roles
	 *
	 * @return get roles
	 */
	public List<RoleModel> getRoles() {
		return roles;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, username);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserModel other = (UserModel) obj;
		return Objects.equals(id, other.id) && Objects.equals(username, other.username);
	}

}
