package io.github.acoboh.query.filter.jpa.processor;

import io.github.acoboh.query.filter.jpa.domain.FilterBlogSortMultiDef;
import io.github.acoboh.query.filter.jpa.model.PostBlog;
import io.github.acoboh.query.filter.jpa.repositories.PostBlogRepository;
import io.github.acoboh.query.filter.jpa.spring.SpringIntegrationTestBase;
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
 * Tests of {@link QueryFilter#deleteSortBy(String)}: the removal must be applied on the
 * instance's own copy of the default sort list, never on the processor's shared one
 *
 * @author Adrián Cobo
 */
@SpringJUnitWebConfig(SpringIntegrationTestBase.Config.class)
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DeleteSortByTest {

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
    private QFProcessor<FilterBlogSortMultiDef, PostBlog> multiSortProcessor;

    @Autowired
    private PostBlogRepository repository;

    @Test
    @DisplayName("0. Setup")
    @Order(0)
    void setup() {

        assertThat(multiSortProcessor).isNotNull();
        assertThat(repository).isNotNull();

        assertThat(repository.findAll()).isEmpty();

        repository.saveAndFlush(POST_EXAMPLE);
        repository.saveAndFlush(POST_EXAMPLE_2);

        assertThat(repository.findAll()).hasSize(2);
    }

    @Test
    @DisplayName("1. Delete a field from the default sort list")
    @Order(1)
    void deleteSortFromDefaultSort() {

        QueryFilter<PostBlog> qf = multiSortProcessor.newQueryFilter("", QFParamType.RHS_COLON);

        assertThat(qf.getSortFields())
                .containsExactly(Pair.of("author", Direction.ASC), Pair.of("likes", Direction.DESC));

        // Must not throw: the default sort list used to be the processor's shared
        // unmodifiable list, so removeIf failed with UnsupportedOperationException
        qf.deleteSortBy("likes");

        assertThat(qf.isSorted()).isTrue();
        assertThat(qf.isSortedBy("author")).isTrue();
        assertThat(qf.isSortedBy("likes")).isFalse();

        assertThat(qf.getSortFields()).containsExactly(Pair.of("author", Direction.ASC));

        var found = repository.findAll(qf);
        assertThat(found).hasSize(2).containsExactly(POST_EXAMPLE, POST_EXAMPLE_2);

    }

    @Test
    @DisplayName("2. Delete on one instance does not affect other instances")
    @Order(2)
    void deleteSortIsInstanceLocal() {

        QueryFilter<PostBlog> qf1 = multiSortProcessor.newQueryFilter("", QFParamType.RHS_COLON);
        qf1.deleteSortBy("likes");
        qf1.deleteSortBy("author");

        assertThat(qf1.isSorted()).isFalse();
        assertThat(qf1.getSortFields()).isEmpty();

        var found = repository.findAll(qf1);
        assertThat(found).hasSize(2).containsExactlyInAnyOrder(POST_EXAMPLE, POST_EXAMPLE_2);

        // A new instance of the same processor must keep the full default sorting
        QueryFilter<PostBlog> qf2 = multiSortProcessor.newQueryFilter("", QFParamType.RHS_COLON);

        assertThat(qf2.isSorted()).isTrue();
        assertThat(qf2.getSortFields())
                .containsExactly(Pair.of("author", Direction.ASC), Pair.of("likes", Direction.DESC));

        // And the processor's own shared default sort list is intact
        assertThat(multiSortProcessor.getDefaultSorting()).hasSize(2);
        assertThat(
                multiSortProcessor.getDefaultSorting().stream()
                        .map(pair -> pair.getFirst().getFilterName() + ":" + pair.getSecond())
                        .toList())
                .containsExactly("author:ASC", "likes:DESC");

    }

    @Test
    @DisplayName("3. Delete a field from a custom (parsed) sort configuration")
    @Order(3)
    void deleteSortFromCustomSort() {

        QueryFilter<PostBlog> qf = multiSortProcessor.newQueryFilter("sort=-likes,+author", QFParamType.RHS_COLON);

        assertThat(qf.getSortFields())
                .containsExactly(Pair.of("likes", Direction.DESC), Pair.of("author", Direction.ASC));

        qf.deleteSortBy("likes");

        assertThat(qf.isSorted()).isTrue();
        assertThat(qf.isSortedBy("author")).isTrue();
        assertThat(qf.isSortedBy("likes")).isFalse();

        assertThat(qf.getSortFields()).containsExactly(Pair.of("author", Direction.ASC));

        var found = repository.findAll(qf);
        assertThat(found).hasSize(2).containsExactly(POST_EXAMPLE, POST_EXAMPLE_2);

    }

    @Test
    @DisplayName("END. Test by clear BBDD")
    @Order(10)
    void clearBBDD() {
        repository.deleteAll();
        assertThat(repository.findAll()).isEmpty();
    }

}
