package io.github.acoboh.query.filter.jpa.model.subquery;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
     * @param username new user name
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
