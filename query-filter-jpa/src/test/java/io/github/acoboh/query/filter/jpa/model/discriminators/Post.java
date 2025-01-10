package io.github.acoboh.query.filter.jpa.model.discriminators;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Objects;

/**
 * Post entity model
 *
 * @author Adrián Cobo
 */
@Entity(name = "Post")
@Table(name = "post")
@DiscriminatorValue("1")
public class Post extends Topic {

    private String content;

    /**
     * Default constructor
     */
    public Post() {

    }

    /**
     * Get content
     *
     * @return content
     */
    public String getContent() {
        return content;
    }

    /**
     * Set new content
     *
     * @param content content
     */
    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(content);
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
        Post other = (Post) obj;
        return Objects.equals(content, other.content);
    }

}
