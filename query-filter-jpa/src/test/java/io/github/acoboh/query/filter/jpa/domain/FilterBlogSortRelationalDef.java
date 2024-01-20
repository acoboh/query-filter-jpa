package io.github.acoboh.query.filter.jpa.domain;

import io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.jpa.annotations.QFElement;
import io.github.acoboh.query.filter.jpa.annotations.QFSortable;
import io.github.acoboh.query.filter.jpa.model.PostBlog;

/**
 * Example for sort with relational classes
 * 
 * @author Adrián Cobo
 *
 */
@QFDefinitionClass(PostBlog.class)
public class FilterBlogSortRelationalDef {

	@QFSortable("comments.author")
	private int commentAuthorSort;

	@QFSortable(value = "comments.author", autoFetch = false)
	private int commentAuthorSortError;

	@QFElement("comments.author")
	private int commentAuthorElement;

	@QFElement(value = "comments.author", autoFetch = false)
	private int commentAuthorElementError;

}
