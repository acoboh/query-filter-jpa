package io.github.acoboh.query.filter.jpa.processor.definitions;

import io.github.acoboh.query.filter.jpa.annotations.QFBlockParsing;
import io.github.acoboh.query.filter.jpa.annotations.QFOnFilterPresent;
import io.github.acoboh.query.filter.jpa.annotations.QFRequired;
import org.springframework.util.Assert;

import java.lang.reflect.Field;

public record FilterFieldInfo(Field field, Class<?> filterClass, Class<?> entityClass, QFBlockParsing blockParsing,
        QFRequired required, QFOnFilterPresent onFilterPresent) {

    public FilterFieldInfo {
        Assert.notNull(field, "Filter field must not be null");
        Assert.notNull(filterClass, "Filter class must not be null");
        Assert.notNull(entityClass, "Entity class must not be null");
    }
}
