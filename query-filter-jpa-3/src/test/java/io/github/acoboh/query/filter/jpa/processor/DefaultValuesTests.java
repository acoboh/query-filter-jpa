package io.github.acoboh.query.filter.jpa.processor;

import io.github.acoboh.query.filter.jpa.domain.FilterBlogDefaultValuesDef;
import io.github.acoboh.query.filter.jpa.model.PostBlog;
import io.github.acoboh.query.filter.jpa.operations.QFOperationEnum;
import io.github.acoboh.query.filter.jpa.repositories.PostBlogRepository;
import io.github.acoboh.query.filter.jpa.spring.SpringIntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.context.web.WebAppConfiguration;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Default values tests
 */
@SpringJUnitWebConfig(SpringIntegrationTestBase.Config.class)
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DefaultValuesTests {

    private static final PostBlog POST_EXAMPLE = new PostBlog();
    private static final PostBlog POST_EXAMPLE_2 = new PostBlog();

    static {
        POST_EXAMPLE.setUuid(UUID.randomUUID());
        POST_EXAMPLE.setAuthor("Author 1");
        POST_EXAMPLE.setText("Text");
        POST_EXAMPLE.setAvgNote(2.5d);
        POST_EXAMPLE.setLikes(0);
        POST_EXAMPLE.setCreateDate(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)); // Truncated to avoid rounding issues with Java > 8 and BBDD
        POST_EXAMPLE.setLastTimestamp(Timestamp.valueOf(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS))); // Truncated to avoid rounding issues with Java > 8 and BBDD
        POST_EXAMPLE.setPublished(true);
        POST_EXAMPLE.setPostType(PostBlog.PostType.TEXT);

        POST_EXAMPLE_2.setUuid(UUID.randomUUID());
        POST_EXAMPLE_2.setAuthor("Author 2");
        POST_EXAMPLE_2.setText("Text 2");
        POST_EXAMPLE_2.setAvgNote(0.5d);
        POST_EXAMPLE_2.setLikes(100);
        POST_EXAMPLE_2.setCreateDate(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)); // Truncated to avoid rounding issues with Java > 8 and BBDD
        POST_EXAMPLE_2.setLastTimestamp(Timestamp.valueOf(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS))); // Truncated to avoid rounding issues with Java > 8 and BBDD
        POST_EXAMPLE_2.setPublished(false);
        POST_EXAMPLE_2.setPostType(PostBlog.PostType.VIDEO);
    }

    @Autowired
    private QFProcessor<FilterBlogDefaultValuesDef, PostBlog> queryFilterProcessor;

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
    @DisplayName("1. Default values filtering")
    @Order(1)
    void testDefaultValueFilter() {

        QueryFilter<PostBlog> qf = queryFilterProcessor.newQueryFilter(null, QFParamType.RHS_COLON);
        assertThat(qf).isNotNull();

        assertThat(qf.isFiltering("author")).isTrue();
        assertThat(qf.getActualValue("author")).containsExactly("Author 1");

        var found = repository.findAll(qf);

        assertThat(found).containsExactly(POST_EXAMPLE);

    }

    @Test
    @DisplayName("2. Default values filtering remove")
    @Order(2)
    void testDefaultValuesRemoved() {
        QueryFilter<PostBlog> qf = queryFilterProcessor.newQueryFilter(null, QFParamType.RHS_COLON);
        assertThat(qf).isNotNull();

        assertThat(qf.isFiltering("author")).isTrue();
        assertThat(qf.getActualValue("author")).containsExactly("Author 1");

        qf.deleteField("author");
        assertThat(qf.isFiltering("author")).isFalse();

        var found = repository.findAll(qf);

        assertThat(found).containsExactlyInAnyOrder(POST_EXAMPLE, POST_EXAMPLE_2);

    }

    @Test
    @DisplayName("4. Default values filtering override on definition")
    @Order(4)
    void testDefaultValuesOverrideOnDefinition() {
        QueryFilter<PostBlog> qf = queryFilterProcessor.newQueryFilter("author=eq:Author 2", QFParamType.RHS_COLON);
        assertThat(qf).isNotNull();

        assertThat(qf.isFiltering("author")).isTrue();
        assertThat(qf.getActualValue("author")).containsExactly("Author 2");

        var found = repository.findAll(qf);

        assertThat(found).containsExactly(POST_EXAMPLE_2);
    }

    @Test
    @DisplayName("5. Default values filtering override manually")
    @Order(5)
    void testDefaultValuesOverrideManually() {
        QueryFilter<PostBlog> qf = queryFilterProcessor.newQueryFilter("", QFParamType.RHS_COLON);
        assertThat(qf).isNotNull();

        assertThat(qf.isFiltering("author")).isTrue();
        assertThat(qf.getActualValue("author")).containsExactly("Author 1");

        qf.addNewField("author", QFOperationEnum.EQUAL, "Author 2");

        assertThat(qf.isFiltering("author")).isTrue();
        assertThat(qf.getActualValue("author")).containsExactly("Author 2");

        var found = repository.findAll(qf);

        assertThat(found).containsExactly(POST_EXAMPLE_2);
    }

    @Test
    @DisplayName("END. Test by clear BBDD")
    @Order(Ordered.LOWEST_PRECEDENCE)
    void clearBBDD() {
        repository.deleteAll();
        assertThat(repository.findAll()).isEmpty();
    }

}
