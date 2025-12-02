package io.github.acoboh.query.filter.jpa.model.discriminators.joined;

import jakarta.persistence.Entity;

import java.util.Objects;

/**
 * Subclass B entity
 */
@Entity
public class SubclassBEntity extends ParentEntity {

    private String text;

    /**
     * Get the text
     *
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * Set the text
     *
     */
    public void setText(String text) {
        this.text = text;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(text);
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
        SubclassBEntity other = (SubclassBEntity) obj;
        return Objects.equals(text, other.text);
    }

}
