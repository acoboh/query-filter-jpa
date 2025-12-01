package io.github.acoboh.query.filter.jpa.processor;

import io.github.acoboh.query.filter.jpa.domain.FilterBlogPredicatesDef;
import io.github.acoboh.query.filter.jpa.exceptions.QueryFilterException;
import io.github.acoboh.query.filter.jpa.model.Comments;
import io.github.acoboh.query.filter.jpa.model.PostBlog;
import io.github.acoboh.query.filter.jpa.operations.QFOperationEnum;
import io.github.acoboh.query.filter.jpa.repositories.PostBlogRepository;
import io.github.acoboh.query.filter.jpa.spring.SpringIntegrationTestBase;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.context.web.WebAppConfiguration;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Custom predicate tests
 *
 * @author Adrián Cobo
 */
@SpringJUnitWebConfig(SpringIntegrationTestBase.Config.class)
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CustomPredicatesTest {

    private static final PostBlog POST_EXAMPLE = new PostBlog();
    private static final PostBlog POST_EXAMPLE_2 = new PostBlog();

    static {

        /*
         * PostBlog 1 -> Author 1 -> Likes 1 -> Type TEXT Comment 1 -> Author 1 -> Likes
         * 100 Comment 2 -> Author 2 -> Likes 200 PostBlog 2 -> Author 2 -> Likes 2
         * Comment 3 -> Author 3 -> Likes 300 Comment 4 -> Author 4 -> Likes 400
         *
         */

        POST_EXAMPLE.setUuid(UUID.randomUUID());
        POST_EXAMPLE.setAuthor("Author 1");
        POST_EXAMPLE.setText("Text");
        POST_EXAMPLE.setAvgNote(2.5d);
        POST_EXAMPLE.setLikes(1);
        POST_EXAMPLE.setCreateDate(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)); // Truncated to avoid rounding
                                                                                        // issues with Java > 8 and BBDD
        POST_EXAMPLE.setLastTimestamp(Timestamp.valueOf(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS))); // Truncated
                                                                                                              // to
                                                                                                              // avoid
                                                                                                              // rounding
                                                                                                              // issues
                                                                                                              // with
                                                                                                              // Java
                                                                                                              // > 8
                                                                                                              // and
                                                                                                              // BBDD
        POST_EXAMPLE.setPublished(true);
        POST_EXAMPLE.setPostType(PostBlog.PostType.TEXT);

        Set<Comments> commentsList = new HashSet<>();

        Comments comment = new Comments();
        comment.setId(1);
        comment.setAuthor("Author 1");
        comment.setComment("Comment 1");
        comment.setLikes(100);
        comment.setPostBlog(POST_EXAMPLE);

        Comments comment2 = new Comments();
        comment2.setId(2);
        comment2.setAuthor("Author 2");
        comment2.setComment("Comment 2");
        comment2.setLikes(200);
        comment2.setPostBlog(POST_EXAMPLE);

        commentsList.add(comment);
        commentsList.add(comment2);

        POST_EXAMPLE.setComments(commentsList);

        POST_EXAMPLE_2.setUuid(UUID.randomUUID());
        POST_EXAMPLE_2.setAuthor("Author 2");
        POST_EXAMPLE_2.setText("Text 2");
        POST_EXAMPLE_2.setAvgNote(0.5d);
        POST_EXAMPLE_2.setLikes(2);
        POST_EXAMPLE_2.setCreateDate(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)); // Truncated to avoid rounding
                                                                                          // issues with Java > 8 and
                                                                                          // BBDD
        POST_EXAMPLE_2.setLastTimestamp(Timestamp.valueOf(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS))); // Truncated
                                                                                                                // to
                                                                                                                // avoid
                                                                                                                // rounding
                                                                                                                // issues
                                                                                                                // with
                                                                                                                // Java
                                                                                                                // > 8
                                                                                                                // and
                                                                                                                // BBDD
        POST_EXAMPLE_2.setPublished(false);
        POST_EXAMPLE_2.setPostType(PostBlog.PostType.VIDEO);

        Set<Comments> commentsList2 = new HashSet<>();

        Comments comment3 = new Comments();
        comment3.setId(3);
        comment3.setAuthor("Author 3");
        comment3.setComment("Comment 3");
        comment3.setLikes(300);
        comment3.setPostBlog(POST_EXAMPLE_2);

        Comments comment4 = new Comments();
        comment4.setId(4);
        comment4.setAuthor("Author 4");
        comment4.setComment("Comment 4");
        comment4.setLikes(400);
        comment4.setPostBlog(POST_EXAMPLE_2);

        commentsList2.add(comment3);
        commentsList2.add(comment4);

        POST_EXAMPLE_2.setComments(commentsList2);
    }

    @Autowired
    private QFProcessor<@NonNull FilterBlogPredicatesDef, @NonNull PostBlog> queryFilterProcessor;
    @Autowired
    private PostBlogRepository repository;

    @Test
    @DisplayName("0. Setup")
    @Order(0)
    void setup() {

        assertThat(queryFilterProcessor).isNotNull();
        assertThat(repository).isNotNull();

        assertThat(repository.findAll()).isEmpty();

        repository.saveAndFlush(POST_EXAMPLE);
        repository.saveAndFlush(POST_EXAMPLE_2);

        assertThat(repository.findAll()).hasSize(2).containsExactlyInAnyOrder(POST_EXAMPLE, POST_EXAMPLE_2);
    }

    @Test
    @DisplayName("1. Custom predicate selection")
    @Order(1)
    void customPredicateSelection() throws QueryFilterException {

        var qf = queryFilterProcessor.newQueryFilter("likes=eq:1&commentLikes=eq:300", QFParamType.RHS_COLON);
        assertThat(qf).isNotNull();

        List<PostBlog> list = repository.findAll(qf);
        assertThat(list).isEmpty(); // Default predicate is AND

        qf.setPredicate(FilterBlogPredicatesDef.OR_LIKES);
        list = repository.findAll(qf);

        assertThat(list).hasSize(2).containsExactlyInAnyOrder(POST_EXAMPLE, POST_EXAMPLE_2);

        qf.setPredicate(FilterBlogPredicatesDef.AND_LIKES);
        list = repository.findAll(qf);

        assertThat(list).isEmpty();

        // For LHS

        qf = queryFilterProcessor.newQueryFilter("likes[eq]=1&commentLikes[eq]=300", QFParamType.LHS_BRACKETS);
        assertThat(qf).isNotNull();

        list = repository.findAll(qf);
        assertThat(list).isEmpty(); // Default predicate is AND

        qf.setPredicate(FilterBlogPredicatesDef.OR_LIKES);
        list = repository.findAll(qf);

        assertThat(list).hasSize(2).containsExactlyInAnyOrder(POST_EXAMPLE, POST_EXAMPLE_2);

        qf.setPredicate(FilterBlogPredicatesDef.AND_LIKES);
        list = repository.findAll(qf);

        assertThat(list).isEmpty();

    }

    @Test
    @DisplayName("2. Custom predicate with include missing")
    @Order(2)
    void customPredicateWithIncludeMissing() throws QueryFilterException {

        var qf = queryFilterProcessor.newQueryFilter(
                "author=in:Author 1,Author 2&commentAuthor=in:Author 1,Author 2&likes=eq:1", QFParamType.RHS_COLON);
        assertThat(qf).isNotNull();

        qf.setPredicate(FilterBlogPredicatesDef.OR_ONLY_AUTHORS);
        assertThat(qf.getPredicateName()).isEqualTo(FilterBlogPredicatesDef.OR_ONLY_AUTHORS);

        // Find data authors in 1,2 and likes eq 1 (likes must be ignored)
        List<PostBlog> list = repository.findAll(qf);
        assertThat(list).hasSize(2).containsExactlyInAnyOrder(POST_EXAMPLE, POST_EXAMPLE_2);

        qf.setPredicate(FilterBlogPredicatesDef.OR_AUTHORS);
        assertThat(qf.getPredicateName()).isEqualTo(FilterBlogPredicatesDef.OR_AUTHORS);

        // Find data authors in 1,2 and likes eq 1 (likes must not be ignored)
        list = repository.findAll(qf);

        assertThat(list).hasSize(1).containsExactlyInAnyOrder(POST_EXAMPLE);

        // Add type filter on ONLY_AUTHORS predicate to check ignored
        qf = queryFilterProcessor.newQueryFilter("author=in:Author 1,Author 2", QFParamType.RHS_COLON);
        qf.setPredicate(FilterBlogPredicatesDef.OR_ONLY_AUTHORS);

        // FOR LHS

        qf = queryFilterProcessor.newQueryFilter(
                "author[in]=Author 1,Author 2&commentAuthor[in]=Author 1,Author 2&likes[eq]=1",
                QFParamType.LHS_BRACKETS);
        assertThat(qf).isNotNull();

        qf.setPredicate(FilterBlogPredicatesDef.OR_ONLY_AUTHORS);
        assertThat(qf.getPredicateName()).isEqualTo(FilterBlogPredicatesDef.OR_ONLY_AUTHORS);

        // Find data authors in 1,2 and likes eq 1 (likes must be ignored)
        list = repository.findAll(qf);
        assertThat(list).hasSize(2).containsExactlyInAnyOrder(POST_EXAMPLE, POST_EXAMPLE_2);

        qf.setPredicate(FilterBlogPredicatesDef.OR_AUTHORS);
        assertThat(qf.getPredicateName()).isEqualTo(FilterBlogPredicatesDef.OR_AUTHORS);

        // Find data authors in 1,2 and likes eq 1 (likes must not be ignored)
        list = repository.findAll(qf);

        assertThat(list).hasSize(1).containsExactlyInAnyOrder(POST_EXAMPLE);

        // Add type filter on ONLY_AUTHORS predicate to check ignored
        qf = queryFilterProcessor.newQueryFilter("author[in]=Author 1,Author 2", QFParamType.LHS_BRACKETS);
        qf.setPredicate(FilterBlogPredicatesDef.OR_ONLY_AUTHORS);

    }

    @Test
    @DisplayName("3. Custom predicate with missing fields and ignore new ones")
    @Order(3)
    void customPredicateWithMissingFieldsAndIgnoreNew() throws QueryFilterException {
        var qf = queryFilterProcessor.newQueryFilter("author=in:Author 1,Author 2", QFParamType.RHS_COLON);
        qf.setPredicate(FilterBlogPredicatesDef.OR_ONLY_AUTHORS); // Additional fields must be ignored

        List<PostBlog> list = repository.findAll(qf);
        assertThat(list).hasSize(2).containsExactlyInAnyOrder(POST_EXAMPLE, POST_EXAMPLE_2);

        qf.addNewField("postType", QFOperationEnum.EQUAL, "VIDEO");

        list = repository.findAll(qf);
        assertThat(list).hasSize(2).containsExactlyInAnyOrder(POST_EXAMPLE, POST_EXAMPLE_2);

        qf.setPredicate(FilterBlogPredicatesDef.OR_AUTHORS); // Additional fields must not be ignored
        list = repository.findAll(qf);

        assertThat(list).hasSize(1).containsExactlyInAnyOrder(POST_EXAMPLE_2);

    }

    @Test
    @DisplayName("5. Test by clear BBDD")
    @Order(10)
    void clearBBDD() {
        repository.deleteAll();
        assertThat(repository.findAll()).isEmpty();
    }
}
