package io.github.acoboh.query.filter.jpa.processor.definitions;

import io.github.acoboh.query.filter.jpa.annotations.QFBlockParsing;
import io.github.acoboh.query.filter.jpa.annotations.QFOnFilterPresent;
import io.github.acoboh.query.filter.jpa.annotations.QFRequired;
import jakarta.annotation.Nullable;
import org.springframework.util.Assert;

import java.lang.reflect.Field;

/**
 * Immutable holder for the field-level metadata common to every
 * {@link QFAbstractDefinition}: the reflected {@link Field}, the filter and
 * entity classes it belongs to, and the optional cross-cutting annotations that
 * apply regardless of the concrete filter element type.
 *
 * @param field           reflected field of the filter definition class
 * @param filterClass     filter definition class the field belongs to
 * @param entityClass     entity class targeted by the filter definition
 * @param blockParsing    {@link QFBlockParsing} annotation present on the
 *                        field, or {@code null} if not present
 * @param required        {@link QFRequired} annotation present on the field, or
 *                        {@code null} if not present
 * @param onFilterPresent {@link QFOnFilterPresent} annotation present on the
 *                        field, or {@code null} if not present
 * @author Adrián Cobo
 */
public record FilterFieldInfo(Field field, Class<?> filterClass, Class<?> entityClass,
        @Nullable QFBlockParsing blockParsing, @Nullable QFRequired required,
        @Nullable QFOnFilterPresent onFilterPresent) {

    /**
     * Compact constructor validating that the mandatory fields are not
     * {@code null}.
     */
    public FilterFieldInfo {
        Assert.notNull(field, "Filter field must not be null");
        Assert.notNull(filterClass, "Filter class must not be null");
        Assert.notNull(entityClass, "Entity class must not be null");
    }
}
