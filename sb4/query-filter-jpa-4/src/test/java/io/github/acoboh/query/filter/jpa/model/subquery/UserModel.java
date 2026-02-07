package io.github.acoboh.query.filter.jpa.model.subquery;

import jakarta.persistence.*;

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
    public boolean equals(Object o) {
        if (!(o instanceof UserModel userModel))
            return false;
        return Objects.equals(id, userModel.id) && Objects.equals(username, userModel.username);
    }
}
