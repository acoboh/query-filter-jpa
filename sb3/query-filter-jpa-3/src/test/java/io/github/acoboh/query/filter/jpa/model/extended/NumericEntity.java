package io.github.acoboh.query.filter.jpa.model.extended;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
public class NumericEntity {

    @Id
    private Long id;

    @Column(precision = 20, scale = 10)
    private BigDecimal bigDecimal;

    protected NumericEntity() {
        // Empty for JPA
    }

    public NumericEntity(Long id, BigDecimal bigDecimal) {
        this.id = id;
        this.bigDecimal = bigDecimal;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getBigDecimal() {
        return bigDecimal;
    }

    public void setBigDecimal(BigDecimal bigDecimal) {
        this.bigDecimal = bigDecimal;
    }

    @Override
    public int hashCode() {
        return Objects.hash(bigDecimal, id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        NumericEntity other = (NumericEntity) obj;
        return Objects.equals(bigDecimal, other.bigDecimal) && Objects.equals(id, other.id);
    }

    @Override
    public String toString() {
        return "NumericEntity [id=" + id + ", bigDecimal=" + bigDecimal + "]";
    }

}
