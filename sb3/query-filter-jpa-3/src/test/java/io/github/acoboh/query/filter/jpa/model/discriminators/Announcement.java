package io.github.acoboh.query.filter.jpa.model.discriminators;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.sql.Timestamp;
import java.util.Objects;

/**
 * Announcement entity class
 *
 * @author Adrián Cobo
 */
@Entity(name = "Announcement")
@DiscriminatorValue("2")
public class Announcement extends Topic {

    private Timestamp validUntil;

    /**
     * Valid until
     *
     * @return valid until
     */
    public Timestamp getValidUntil() {
        return validUntil;
    }

    /**
     * Set valid until
     *
     * @param validUntil new valid until
     */
    public void setValidUntil(Timestamp validUntil) {
        this.validUntil = validUntil;
    }

    /**
     * Default constructor
     */
    public Announcement() {

    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(validUntil);
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
        Announcement other = (Announcement) obj;
        return Objects.equals(validUntil, other.validUntil);
    }

}
