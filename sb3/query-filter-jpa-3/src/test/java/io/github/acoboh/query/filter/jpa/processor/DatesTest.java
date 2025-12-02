package io.github.acoboh.query.filter.jpa.processor;

import io.github.acoboh.query.filter.jpa.domain.FilterBlogDatesDef;
import io.github.acoboh.query.filter.jpa.exceptions.QFDateParsingException;
import io.github.acoboh.query.filter.jpa.exceptions.QFFilterNotValid;
import io.github.acoboh.query.filter.jpa.exceptions.QFNotValuable;
import io.github.acoboh.query.filter.jpa.exceptions.QueryFilterException;
import io.github.acoboh.query.filter.jpa.model.PostBlog;
import io.github.acoboh.query.filter.jpa.operations.QFOperationEnum;
import io.github.acoboh.query.filter.jpa.repositories.PostBlogRepository;
import io.github.acoboh.query.filter.jpa.spring.SpringIntegrationTestBase;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.context.web.WebAppConfiguration;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

/**
 * Dates tests
 *
 * @author Adrián Cobo
 */
@SpringJUnitWebConfig(SpringIntegrationTestBase.Config.class)
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DatesTest {

    private static final PostBlog POST_EXAMPLE = new PostBlog();
    private static final PostBlog POST_EXAMPLE_2 = new PostBlog();

    static {

        POST_EXAMPLE.setUuid(UUID.randomUUID());
        POST_EXAMPLE.setAuthor("Author 1");
        POST_EXAMPLE.setText("Text");
        POST_EXAMPLE.setAvgNote(2.5d);
        POST_EXAMPLE.setLikes(1);
        POST_EXAMPLE.setCreateDate(LocalDateTime.of(2022, 01, 01, 12, 30, 0));
        POST_EXAMPLE.setLastTimestamp(Timestamp.valueOf(LocalDateTime.of(2022, 05, 10, 22, 13, 24)));
        POST_EXAMPLE.setInstant(LocalDateTime.of(2022, 01, 01, 12, 30, 0).toInstant(ZoneOffset.UTC));
        POST_EXAMPLE.setPublished(true);
        POST_EXAMPLE.setPostType(PostBlog.PostType.TEXT);

        POST_EXAMPLE_2.setUuid(UUID.randomUUID());
        POST_EXAMPLE_2.setAuthor("Author 2");
        POST_EXAMPLE_2.setText("Text 2");
        POST_EXAMPLE_2.setAvgNote(0.5d);
        POST_EXAMPLE_2.setLikes(2);
        POST_EXAMPLE_2.setCreateDate(LocalDateTime.of(2023, 01, 01, 12, 30, 0));
        POST_EXAMPLE_2.setLastTimestamp(Timestamp.valueOf(LocalDateTime.of(2023, 05, 10, 22, 13, 24)));
        POST_EXAMPLE_2.setLastTimestamp(Timestamp.valueOf(LocalDateTime.of(2023, 05, 10, 22, 13, 24)));
        POST_EXAMPLE_2.setPublished(false);
        POST_EXAMPLE_2.setPostType(PostBlog.PostType.VIDEO);

    }

    @Autowired
    private QFProcessor<FilterBlogDatesDef, PostBlog> queryFilterProcessor;
    @Autowired
    private PostBlogRepository repository;

    @Test
    @DisplayName("0. Setup")
    @Order(Ordered.HIGHEST_PRECEDENCE)
    void setup() {

        assertThat(queryFilterProcessor).isNotNull();
        assertThat(repository).isNotNull();

        assertThat(repository.findAll()).isEmpty();

        repository.saveAndFlush(POST_EXAMPLE);
        repository.saveAndFlush(POST_EXAMPLE_2);

        assertThat(repository.findAll()).hasSize(2).containsExactlyInAnyOrder(POST_EXAMPLE, POST_EXAMPLE_2);
    }

    @Test
    @DisplayName("1. Default format")
    @Order(1)
    void defaultFormat() throws QueryFilterException {

        QueryFilter<PostBlog> qf = queryFilterProcessor.newQueryFilter("createDateDefault=gte:2023-01-01T00:00:00Z",
                QFParamType.RHS_COLON);

        assertThat(qf).isNotNull();

        List<PostBlog> createDateResults = repository.findAll(qf);
        assertThat(createDateResults).hasSize(1).containsExactly(POST_EXAMPLE_2);

    }

    @Test
    @DisplayName("2. Only sortable element")
    @Order(2)
    void onlySortableElement() {

        QFNotValuable exception = assertThrows(QFNotValuable.class, () -> {
            queryFilterProcessor.newQueryFilter("lastTimestampSortable=gte:2023-01-01T00:00:00Z",
                    QFParamType.RHS_COLON);
        });

        assertThat(exception).isNotNull();
        assertThat(exception.getField()).isEqualTo("lastTimestampSortable");

    }

    @Test
    @DisplayName("3. Custom format local date time")
    @Order(3)
    void customFormatLocalDateTime() throws QueryFilterException {

        QueryFilter<PostBlog> qf = queryFilterProcessor.newQueryFilter("createDateCustomFormat=lt:2023-01-01 01:12:30",
                QFParamType.RHS_COLON);

        assertThat(qf).isNotNull();

        List<PostBlog> createDateResults = repository.findAll(qf);
        assertThat(createDateResults).hasSize(1).containsExactly(POST_EXAMPLE);

    }

    @Test
    @DisplayName("4. Custom format timestamp")
    @Order(4)
    void customFormatTimestamp() throws QueryFilterException {

        QueryFilter<PostBlog> qf = queryFilterProcessor.newQueryFilter("lastTimestampCustomFormat=lt:2023/01/01 00:30",
                QFParamType.RHS_COLON);

        assertThat(qf).isNotNull();

        List<PostBlog> createDateResults = repository.findAll(qf);
        assertThat(createDateResults).hasSize(1).containsExactly(POST_EXAMPLE);

    }

    @Test
    @DisplayName("5. Custom format timestamp error")
    @Order(5)
    void customFormatTimestampError() throws QueryFilterException {

        QFDateParsingException ex = assertThrows(QFDateParsingException.class, () -> queryFilterProcessor
                .newQueryFilter("lastTimestampCustomFormat=lt:2023-01-01", QFParamType.RHS_COLON));

        assertThat(ex).isNotNull();
        assertThat(ex.getField()).isEqualTo("lastTimestampCustomFormat");
        assertThat(ex.getValue()).isEqualTo("2023-01-01");
        assertThat(ex.getFormat()).isEqualTo("yyyy/MM/dd HH:mm");

    }

    @Test
    @DisplayName("6. Custom format with defaults")
    @Order(6)
    void customFormatWithDefaults() throws QueryFilterException {

        QueryFilter<PostBlog> qf = queryFilterProcessor.newQueryFilter("withDefaults=eq:2022/01/01",
                QFParamType.RHS_COLON);

        assertThat(qf).isNotNull();

        List<PostBlog> createDateResults = repository.findAll(qf);
        assertThat(createDateResults).hasSize(1).containsExactly(POST_EXAMPLE);

    }

    @Test
    @DisplayName("7. Test instant types")
    @Order(7)
    void testInstantTypes() throws QueryFilterException {

        QueryFilter<PostBlog> qf = queryFilterProcessor.newQueryFilter("instant=eq:2022-01-01T12:30:00Z",
                QFParamType.RHS_COLON);

        assertThat(qf).isNotNull();

        List<PostBlog> createDateResults = repository.findAll(qf);
        assertThat(createDateResults).hasSize(1).containsExactly(POST_EXAMPLE);

    }

    @Test
    @DisplayName("8. Test between dates")
    @Order(8)
    void testBetweenDates() throws QueryFilterException {

        QueryFilter<PostBlog> qf = queryFilterProcessor
                .newQueryFilter("instant=btw:2022-01-01T00:00:00Z,2023-01-01T00:00:00Z", QFParamType.RHS_COLON);

        assertThat(qf).isNotNull();

        List<PostBlog> createDateResults = repository.findAll(qf);
        assertThat(createDateResults).hasSize(1).containsExactly(POST_EXAMPLE);

    }

    @Test
    @DisplayName("9. Test between dates with error if only one date")
    @Order(9)
    void testBetweenDatesWithError() throws QueryFilterException {

        QFFilterNotValid ex = assertThrows(QFFilterNotValid.class,
                () -> queryFilterProcessor.newQueryFilter("instant=btw:2022-01-01T00:00:00Z", QFParamType.RHS_COLON));

        assertThat(ex).isNotNull();
        assertThat(ex.getField()).isEqualTo("instant");
        assertThat(ex.getOperation()).isEqualTo(QFOperationEnum.BETWEEN);

    }

    @Test
    @DisplayName("END. Test by clear BBDD")
    @Order(Ordered.LOWEST_PRECEDENCE)
    void clearBBDD() {
        repository.deleteAll();
        assertThat(repository.findAll()).isEmpty();
    }

}
