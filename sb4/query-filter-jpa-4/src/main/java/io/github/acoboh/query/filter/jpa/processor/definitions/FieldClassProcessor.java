package io.github.acoboh.query.filter.jpa.processor.definitions;

import com.google.common.base.Splitter;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QFElementException;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QueryFilterDefinitionException;
import io.github.acoboh.query.filter.jpa.processor.QFAttribute;
import jakarta.persistence.metamodel.*;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

class FieldClassProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(FieldClassProcessor.class);

    private final Class<?> rootClass;
    private final String pathField;
    private final Metamodel metamodel;
    private @Nullable List<QFAttribute> attributes;
    private @Nullable Class<?> finalClass;

    private final @Nullable Class<?> subclassMapping;
    private final @Nullable String subClassMappingPath;

    FieldClassProcessor(Class<?> rootClass, String pathField, @Nullable Class<?> subclassMapping,
            @Nullable String subClassMappingPath, Metamodel metamodel) {
        Assert.notNull(pathField, "Path field cannot be null");
        this.rootClass = rootClass;
        this.pathField = pathField;
        this.subclassMapping = subclassMapping;
        this.subClassMappingPath = subClassMappingPath;
        this.metamodel = metamodel;
    }

    /**
     * Get the attributes of the path field
     *
     * @return list of attributes
     * @throws io.github.acoboh.query.filter.jpa.exceptions.definition.QueryFilterDefinitionException if
     *                                                                                                any.
     */
    public List<QFAttribute> getAttributes() throws QueryFilterDefinitionException {

        if (attributes != null) {
            return attributes;
        }

        LOGGER.debug("Parsing with metamodel");

        var splitLevel = Splitter.on('.').splitToList(pathField);
        attributes = new ArrayList<>(splitLevel.size());

        var entity = metamodel.entity(rootClass);

        // Level subclass
        @Nullable
        String[] levelsSubClass = getLevelSubClassIfAvailable();

        Attribute<?, ?> prevAttribute = null;

        int actualLevel = -1;
        for (String level : splitLevel) {
            LOGGER.trace("Processing level {}", level);
            actualLevel++;

            Class<?> treatClass;

            if (levelsSubClass != null && levelsSubClass.length == actualLevel && subclassMapping != null) {
                // Check levelClass is parent of subclassMapping
                if ((actualLevel == 0 && !entity.getJavaType().isAssignableFrom(subclassMapping)) || (actualLevel > 0
                        && prevAttribute != null && !prevAttribute.getJavaType().isAssignableFrom(subclassMapping))) {
                    throw new QFElementException(pathField, subclassMapping);
                }
                treatClass = subclassMapping;
                entity = metamodel.entity(subclassMapping);
                prevAttribute = entity.getAttribute(level);
                attributes.add(new QFAttribute(prevAttribute, treatClass));
                continue;
            }

            prevAttribute = getAttribute(level, prevAttribute, entity);
            attributes.add(new QFAttribute(prevAttribute, null));

        }

        finalClass = attributes.get(attributes.size() - 1).getAttribute().getJavaType();

        return attributes;
    }

    private Attribute<?, ?> getAttribute(String level, @Nullable Attribute<?, ?> prevAttribute, EntityType<?> entity)
            throws QueryFilterDefinitionException {
        try {
            if (prevAttribute == null) {
                prevAttribute = entity.getAttribute(level);
            } else {
                prevAttribute = getAttribute(prevAttribute, level);
            }

        } catch (Exception e) {
            throw new QueryFilterDefinitionException("Error processing level {}. Exception", e, level);
        }
        return prevAttribute;
    }

    private String @Nullable [] getLevelSubClassIfAvailable() throws QFElementException {
        String[] levelsSubClass = null;

        if (subclassMapping != null && !Void.class.equals(subclassMapping)) {
            LOGGER.trace("Processing subclass mapping {}", subclassMapping);
            if (subClassMappingPath != null && !subClassMappingPath.isEmpty()
                    && !pathField.startsWith(subClassMappingPath)) {
                LOGGER.trace("Subclass mapping path '{}' not present in path field '{}'", subClassMappingPath,
                        pathField);
                throw new QFElementException(pathField, subclassMapping);
            }
            if (subClassMappingPath == null || subClassMappingPath.isEmpty()) {
                levelsSubClass = new String[0];
            } else {
                levelsSubClass = subClassMappingPath.split("\\.");
            }

        }
        return levelsSubClass;
    }

    private Attribute<?, ?> getAttribute(Attribute<?, ?> prevAttribute, String level) {

        if (prevAttribute instanceof PluralAttribute<?, ?, ?> pluralAttribute) {
            LOGGER.trace("Processing list attribute {}", prevAttribute);
            var elemType = pluralAttribute.getElementType();
            return processType(elemType, level);

        } else if (prevAttribute instanceof SingularAttribute<?, ?> singularAttribute) {
            LOGGER.trace("Processing singular attribute {}", prevAttribute);
            return processType(singularAttribute.getType(), level);
        }

        throw new IllegalArgumentException("Attribute type not supported");

    }

    private Attribute<?, ?> processType(Type<?> type, String level) {
        if (type instanceof ManagedType<?> managedType) {
            return managedType.getAttribute(level);
        }
        throw new IllegalArgumentException("Type is not a managed type");
    }

    /**
     * Get final class
     *
     * @return final class
     */
    public @Nullable Class<?> getFinalClass() {
        return finalClass;
    }

}
