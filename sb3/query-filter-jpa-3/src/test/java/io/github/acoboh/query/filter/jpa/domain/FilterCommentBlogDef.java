package io.github.acoboh.query.filter.jpa.domain;

import io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.jpa.annotations.QFElement;
import io.github.acoboh.query.filter.jpa.annotations.QFSortable;
import io.github.acoboh.query.filter.jpa.model.Comments;

@QFDefinitionClass(Comments.class)
public class FilterCommentBlogDef {

    @QFSortable("postBlog.author")
    private String blogAuthor;

    @QFElement("postBlog.author")
    private String blogAuthorElem;

}
