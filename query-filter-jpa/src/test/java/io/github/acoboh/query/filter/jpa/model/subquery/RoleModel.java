package io.github.acoboh.query.filter.jpa.model.subquery;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import java.util.ArrayList;
import java.util.List;

/**
 * Role entity model
 *
 * @author Adrián Cobo
 */
@Entity
public class RoleModel {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @ManyToMany
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "role_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<UserModel> users = new ArrayList<>();

    /**
     * Get ID
     *
     * @return ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Get list of users
     *
     * @return list of users
     */
    public List<UserModel> getUsers() {
        return users;
    }

    /**
     * Get role name
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Set new name
     *
     * @param name new name
     */
    public void setName(String name) {
        this.name = name;
    }

}
