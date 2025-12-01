package io.github.acoboh.query.filter.jpa.processor.definitions;

import io.github.acoboh.query.filter.jpa.annotations.QFBlockParsing;
import io.github.acoboh.query.filter.jpa.annotations.QFOnFilterPresent;
import io.github.acoboh.query.filter.jpa.annotations.QFRequired;
import jakarta.annotation.Nullable;
import org.springframework.util.Assert;

import java.lang.reflect.Field;

public record FilterFieldInfo(Field field, Class<?> filterClass, Class<?> entityClass,
        @Nullable QFBlockParsing blockParsing, @Nullable QFRequired required,
        @Nullable QFOnFilterPresent onFilterPresent) {

    public FilterFieldInfo {
        Assert.notNull(field, "Filter field must not be null");
        Assert.notNull(filterClass, "Filter class must not be null");
        Assert.notNull(entityClass, "Entity class must not be null");
    }
}
