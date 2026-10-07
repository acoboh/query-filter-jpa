package io.github.acoboh.query.filter.jpa.domain;

import io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass.QFDefaultSort;
import io.github.acoboh.query.filter.jpa.annotations.QFElement;
import io.github.acoboh.query.filter.jpa.model.PostBlog;
import org.springframework.data.domain.Sort.Direction;

/**
 * Example of multiple default sorting options
 *
 * @author Adrián Cobo
 */
@QFDefinitionClass(value = PostBlog.class,
        defaultSort = {
                @QFDefaultSort("author"),
                @QFDefaultSort(value = "likes", direction = Direction.DESC)})
public class FilterBlogSortMultiDef {

    @QFElement("author")
    private String author;

    @QFElement("likes")
    private int likes;

}
