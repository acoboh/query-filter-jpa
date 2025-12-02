package io.github.acoboh.query.filter.jpa.processor;

import io.github.acoboh.query.filter.jpa.domain.FilterBlogSortDef;
import io.github.acoboh.query.filter.jpa.domain.FilterBlogSortWithPredicateDef;
import io.github.acoboh.query.filter.jpa.model.PostBlog;
import io.github.acoboh.query.filter.jpa.repositories.PostBlogRepository;
import io.github.acoboh.query.filter.jpa.spring.SpringIntegrationTestBase;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.util.Pair;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.context.web.WebAppConfiguration;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Default sorting options
 *
 * @author Adrián Cobo
 */
@SpringJUnitWebConfig(SpringIntegrationTestBase.Config.class)
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SortTest {

    private static final PostBlog POST_EXAMPLE = new PostBlog();
    private static final PostBlog POST_EXAMPLE_2 = new PostBlog();

    static {
        POST_EXAMPLE.setUuid(UUID.randomUUID());
        POST_EXAMPLE.setAuthor("Author");
        POST_EXAMPLE.setText("Text");
        POST_EXAMPLE.setAvgNote(2.5d);
        POST_EXAMPLE.setLikes(0);

        // Truncated to avoid rounding issues with Java > 8 and BBDD
        POST_EXAMPLE.setCreateDate(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS));
        POST_EXAMPLE.setLastTimestamp(Timestamp.valueOf(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)));

        POST_EXAMPLE.setPublished(true);
        POST_EXAMPLE.setPostType(PostBlog.PostType.TEXT);

        POST_EXAMPLE_2.setUuid(UUID.randomUUID());
        POST_EXAMPLE_2.setAuthor("Author 2");
        POST_EXAMPLE_2.setText("Text 2");
        POST_EXAMPLE_2.setAvgNote(0.5d);
        POST_EXAMPLE_2.setLikes(100);

        // Truncated to avoid rounding issues with Java > 8 and BBDD
        POST_EXAMPLE_2.setCreateDate(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS));
        POST_EXAMPLE_2.setLastTimestamp(Timestamp.valueOf(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)));
        POST_EXAMPLE_2.setPublished(false);
        POST_EXAMPLE_2.setPostType(PostBlog.PostType.VIDEO);
    }

    @Autowired
    private QFProcessor<@NonNull FilterBlogSortDef, @NonNull PostBlog> queryFilterProcessor;

    @Autowired
    private QFProcessor<@NonNull FilterBlogSortWithPredicateDef, @NonNull PostBlog> qfProcessorWithPredicates;

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

        assertThat(repository.findAll()).hasSize(2);
    }

    @Test
    @DisplayName("1. Test default sort creation")
    @Order(1)
    void testDefaultSortCreation() {

        var qf = queryFilterProcessor.newQueryFilter("", QFParamType.RHS_COLON);
        assertThat(qf).isNotNull();

        assertThat(qf.isSorted()).isTrue();

        assertThat(qf.isSortedBy("author")).isTrue();

        assertThat(qf.getSortFields()).containsExactly(Pair.of("author", Direction.ASC));

        assertThat(qf.getSortFieldWithFullPath()).containsExactly(Pair.of("author", Direction.ASC));

        var found = repository.findAll(qf);

        assertThat(found).hasSize(2).containsExactly(POST_EXAMPLE, POST_EXAMPLE_2);

    }

    @Test
    @DisplayName("2. Test clear sort options")
    @Order(2)
    void testClearSortOptions() {
        var qf = queryFilterProcessor.newQueryFilter("", QFParamType.RHS_COLON);
        assertThat(qf).isNotNull();

        assertThat(qf.isSorted()).isTrue();

        qf.clearSort();

        assertThat(qf.isSorted()).isFalse();

        assertThat(qf.getSortFields()).isEmpty();

        assertThat(qf.getSortFieldWithFullPath()).isEmpty();

        var found = repository.findAll(qf);

        assertThat(found).hasSize(2).containsExactlyInAnyOrder(POST_EXAMPLE, POST_EXAMPLE_2);

        qf.setDefaultSortEnabled();
        assertThat(qf.isSorted()).isTrue();
        assertThat(qf.getSortFields()).containsExactly(Pair.of("author", Direction.ASC));
        assertThat(qf.getSortFieldWithFullPath()).containsExactly(Pair.of("author", Direction.ASC));
        assertThat(qf.isSortedBy("author")).isTrue();
        assertThat(qf.isSortedBy("likes")).isFalse();

        found = repository.findAll(qf);
        assertThat(found).hasSize(2).containsExactly(POST_EXAMPLE, POST_EXAMPLE_2);

    }

    @Test
    @DisplayName("3. Test clear sort options on creation")
    @Order(3)
    void testSortOptionsOnConstructor() {
        var qf = queryFilterProcessor.newQueryFilter("sort=-likes", QFParamType.RHS_COLON);
        assertThat(qf).isNotNull();

        assertThat(qf.isSorted()).isTrue();

        assertThat(qf.isSortedBy("likes")).isTrue();

        assertThat(qf.isSortedBy("author")).isFalse();

        assertThat(qf.getSortFields()).containsExactly(Pair.of("likes", Direction.DESC));

        assertThat(qf.getSortFieldWithFullPath()).containsExactly(Pair.of("likes", Direction.DESC));

        var found = repository.findAll(qf);

        assertThat(found).hasSize(2).containsExactly(POST_EXAMPLE_2, POST_EXAMPLE);

        qf.clearSort();

        assertThat(qf.isSorted()).isFalse();

        found = repository.findAll(qf);

        assertThat(found).hasSize(2).containsExactlyInAnyOrder(POST_EXAMPLE, POST_EXAMPLE_2);

    }

    @Test
    @DisplayName("4. Test sort options concat on creation")
    @Order(4)
    void testSortConcatOnConstructor() {
        var qf = queryFilterProcessor.newQueryFilter("sort=-likes,+lastTimestamp", QFParamType.RHS_COLON);
        assertThat(qf).isNotNull();

        assertThat(qf.isSorted()).isTrue();

        assertThat(qf.isSortedBy("likes")).isTrue();

        assertThat(qf.isSortedBy("author")).isFalse();

        assertThat(qf.isSortedBy("lastTimestamp")).isTrue();

        assertThat(qf.getSortFields()).containsExactly(Pair.of("likes", Direction.DESC),
                Pair.of("lastTimestamp", Direction.ASC));

        assertThat(qf.getSortFieldWithFullPath()).containsExactly(Pair.of("likes", Direction.DESC),
                Pair.of("lastTimestamp", Direction.ASC));

        var found = repository.findAll(qf);

        assertThat(found).hasSize(2).containsExactly(POST_EXAMPLE_2, POST_EXAMPLE);

        qf.clearSort();

        assertThat(qf.isSorted()).isFalse();

        found = repository.findAll(qf);

        assertThat(found).hasSize(2).containsExactlyInAnyOrder(POST_EXAMPLE, POST_EXAMPLE_2);

        qf = queryFilterProcessor.newQueryFilter("sort=-likes,+lastTimestamp,+author", QFParamType.RHS_COLON);

        assertThat(qf.isSorted()).isTrue();

        assertThat(qf.isSortedBy("likes")).isTrue();

        assertThat(qf.isSortedBy("author")).isTrue();

        assertThat(qf.isSortedBy("lastTimestamp")).isTrue();

        assertThat(qf.getSortFields()).containsExactly(Pair.of("likes", Direction.DESC),
                Pair.of("lastTimestamp", Direction.ASC), Pair.of("author", Direction.ASC));

        assertThat(qf.getSortFieldWithFullPath()).containsExactly(Pair.of("likes", Direction.DESC),
                Pair.of("lastTimestamp", Direction.ASC), Pair.of("author", Direction.ASC));

        found = repository.findAll(qf);

        assertThat(found).hasSize(2).containsExactlyInAnyOrder(POST_EXAMPLE, POST_EXAMPLE_2);

    }

    @Test
    @DisplayName("5. Test sort with predicates Issue #31")
    @Order(5)
    void testSortWithPredicates() {
        var qf = qfProcessorWithPredicates.newQueryFilter("sort=-likes", QFParamType.RHS_COLON);

        assertThat(qf).isNotNull();

        assertThat(qf.isSorted()).isTrue();

        assertThat(qf.isSortedBy("likes")).isTrue();

        assertThat(qf.isSortedBy("author")).isFalse();

        assertThat(qf.isSortedBy("lastTimestamp")).isFalse();

        assertThat(qf.getSortFields()).containsExactly(Pair.of("likes", Direction.DESC));

        assertThat(qf.getSortFieldWithFullPath()).containsExactly(Pair.of("likes", Direction.DESC));

        var found = repository.findAll(qf);

        assertThat(found).hasSize(2).containsExactly(POST_EXAMPLE_2, POST_EXAMPLE);

        qf.clearSort();

        assertThat(qf.isSorted()).isFalse();

        assertThat(found).hasSize(2).containsExactlyInAnyOrder(POST_EXAMPLE, POST_EXAMPLE_2);

    }

    @Test
    @DisplayName("END. Test by clear BBDD")
    @Order(10)
    void clearBBDD() {
        repository.deleteAll();
        assertThat(repository.findAll()).isEmpty();
    }

}
