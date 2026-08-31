package io.github.acoboh.query.filter.jpa.openapi.domain;

import io.github.acoboh.query.filter.jpa.annotations.QFBlockParsing;
import io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.jpa.annotations.QFElement;
import io.github.acoboh.query.filter.jpa.annotations.QFSortable;
import io.github.acoboh.query.filter.jpa.openapi.model.Post;
import io.github.acoboh.query.filter.jpa.operations.QFOperationEnum;

import java.time.LocalDateTime;

/**
 * Filter definition used to exercise the OpenAPI documentation customizer: a
 * plain element with restricted operations, an enum element, a sortable-only
 * element, and a blocked element that must never appear in the generated
 * documentation.
 *
 * @author Adrián Cobo
 */
@QFDefinitionClass(Post.class)
public class PostFilterDef {

    @QFElement(value = "title", allowedOperations = { QFOperationEnum.EQUAL, QFOperationEnum.LIKE })
    private String title;

    @QFElement("postType")
    private Post.PostType postType;

    @QFSortable("lastUpdate")
    private LocalDateTime lastUpdateSortable;

    @QFElement("published")
    @QFBlockParsing
    private boolean published;

}
