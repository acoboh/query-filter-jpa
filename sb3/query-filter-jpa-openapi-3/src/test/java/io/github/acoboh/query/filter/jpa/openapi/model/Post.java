package io.github.acoboh.query.filter.jpa.openapi.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Minimal entity used to exercise the OpenAPI customizer against a real JPA
 * metamodel.
 *
 * @author Adrián Cobo
 */
@Entity
public class Post {

    /**
     * Post type enumeration
     */
    public enum PostType {
        TEXT, VIDEO
    }

    @Id
    private UUID uuid;

    private String title;

    private LocalDateTime lastUpdate;

    @Enumerated(EnumType.STRING)
    private PostType postType;

    private boolean published;

    /**
     * Get uuid
     *
     * @return uuid
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * Set uuid
     *
     * @param uuid new uuid
     */
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * Get title
     *
     * @return title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set title
     *
     * @param title new title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Get last update
     *
     * @return last update
     */
    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    /**
     * Set last update
     *
     * @param lastUpdate new last update
     */
    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    /**
     * Get post type
     *
     * @return post type
     */
    public PostType getPostType() {
        return postType;
    }

    /**
     * Set post type
     *
     * @param postType new post type
     */
    public void setPostType(PostType postType) {
        this.postType = postType;
    }

    /**
     * Get if published
     *
     * @return published
     */
    public boolean isPublished() {
        return published;
    }

    /**
     * Set published
     *
     * @param published new published
     */
    public void setPublished(boolean published) {
        this.published = published;
    }

}
