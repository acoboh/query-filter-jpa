package io.github.acoboh.query.filter.jpa.domain;

import io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.jpa.annotations.QFElement;
import io.github.acoboh.query.filter.jpa.annotations.QFPredicate;
import io.github.acoboh.query.filter.jpa.model.PostBlog;

/**
 * Example for predicates on query filter definition
 *
 * @author Adrián Cobo
 */
@QFDefinitionClass(PostBlog.class)
@QFPredicate(name = FilterBlogPredicatesDef.OR_LIKES, expression = "likes OR commentLikes")
@QFPredicate(name = FilterBlogPredicatesDef.AND_LIKES, expression = "likes AND commentLikes")
@QFPredicate(name = FilterBlogPredicatesDef.OR_ONLY_AUTHORS, expression = "author OR commentAuthor", includeMissing = false)
@QFPredicate(name = FilterBlogPredicatesDef.OR_AUTHORS, expression = "author OR commentAuthor", includeMissing = true)
public class FilterBlogPredicatesDef {

	/**
	 * Or likes predicate name
	 */
	public static final String OR_LIKES = "or-likes";

	/**
	 * And likes predicate name
	 */
	public static final String AND_LIKES = "and-likes";

	/**
	 * Or only with authors predicate name
	 */
	public static final String OR_ONLY_AUTHORS = "or-only-authors";

	/**
	 * Or only authors include missing predicate name
	 */
	public static final String OR_AUTHORS = "or-authors";

	@QFElement("likes")
	private int likes;

	@QFElement("comments.likes")
	private int commentLikes;

	@QFElement("author")
	private String author;

	@QFElement("comments.author")
	private String commentAuthor;

	@QFElement("postType")
	private String postType;

}
