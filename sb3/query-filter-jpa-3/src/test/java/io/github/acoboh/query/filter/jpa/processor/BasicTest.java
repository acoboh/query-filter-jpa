package io.github.acoboh.query.filter.jpa.processor;

import io.github.acoboh.query.filter.jpa.domain.FilterBlogDef;
import io.github.acoboh.query.filter.jpa.exceptions.QFBlockException;
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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Basic tests
 *
 * @author Adrián Cobo
 */
@SpringJUnitWebConfig(SpringIntegrationTestBase.Config.class)
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BasicTest {

    private static final PostBlog POST_EXAMPLE = new PostBlog();

    static {
        POST_EXAMPLE.setUuid(UUID.randomUUID());
        POST_EXAMPLE.setAuthor("author");
        POST_EXAMPLE.setText("text");
        POST_EXAMPLE.setAvgNote(2.5d);
        POST_EXAMPLE.setLikes(100);
        // Truncated to avoid rounding issues with Java > 8 and BBDD
        POST_EXAMPLE.setCreateDate(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS));
        POST_EXAMPLE.setLastTimestamp(Timestamp.valueOf(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)));

        POST_EXAMPLE.setPublished(true);
        POST_EXAMPLE.setPostType(PostBlog.PostType.TEXT);
    }

    @Autowired
    private QFProcessor<FilterBlogDef, PostBlog> queryFilterProcessor;

    @Autowired
    private PostBlogRepository repository;

    private static void assertPostEqual(PostBlog actual) {
        assertThat(POST_EXAMPLE.getAuthor()).isEqualTo(actual.getAuthor());
        assertThat(POST_EXAMPLE.getText()).isEqualTo(actual.getText());
        assertThat(POST_EXAMPLE.getAvgNote()).isEqualTo(actual.getAvgNote());
        assertThat(POST_EXAMPLE.getLikes()).isEqualTo(actual.getLikes());
        assertThat(POST_EXAMPLE.getCreateDate()).isEqualTo(actual.getCreateDate());
        assertThat(POST_EXAMPLE.getLastTimestamp()).isEqualTo(actual.getLastTimestamp());
        assertThat(POST_EXAMPLE.isPublished()).isEqualTo(actual.isPublished());
        assertThat(POST_EXAMPLE.getPostType()).isEqualTo(actual.getPostType());
        assertThat(POST_EXAMPLE.getUuid()).isEqualTo(actual.getUuid());
    }

    @Test
    @DisplayName("0. Setup")
    @Order(Ordered.HIGHEST_PRECEDENCE)
    void setup() {

        assertThat(queryFilterProcessor).isNotNull();
        assertThat(repository).isNotNull();

        assertThat(repository.findAll()).isEmpty();

        repository.saveAndFlush(POST_EXAMPLE);
    }

    @Test
    @DisplayName("1. Test empty query")
    @Order(1)
    void testQueryByName() throws QueryFilterException {

        QueryFilter<PostBlog> qf = queryFilterProcessor.newQueryFilter("", QFParamType.RHS_COLON);
        assertThat(qf).isNotNull();

        List<PostBlog> list = repository.findAll(qf);
        assertThat(list).hasSize(1);

        PostBlog postBlog = list.get(0);
        assertPostEqual(postBlog);

        qf = queryFilterProcessor.newQueryFilter("", QFParamType.LHS_BRACKETS);
        assertThat(qf).isNotNull();

        list = repository.findAll(qf);
        assertThat(list).hasSize(1);

        list.get(0);
        assertPostEqual(postBlog);
    }

    @Test
    @DisplayName("2. Test by author")
    @Order(2)
    void testQueryByAuthor() throws QueryFilterException {

        QueryFilter<PostBlog> qf = queryFilterProcessor.newQueryFilter("author=like:auth", QFParamType.RHS_COLON);
        assertThat(qf).isNotNull();

        assertThat(qf.getInitialInput()).isEqualTo("author=like:auth");

        assertThat(qf.isFiltering("author")).isTrue();
        assertThat(qf.getActualValue("author")).containsExactly("auth");
        var pairOp = qf.getFirstActualElementOperation("author");
        assertThat(pairOp).isNotNull();
        assertThat(pairOp.getFirst()).isEqualTo(QFOperationEnum.LIKE);
        assertThat(pairOp.getSecond()).containsExactly("auth");

        var fieldFields = qf.getActualElementOperation("author");
        assertThat(fieldFields).hasSize(1);
        var fieldField = fieldFields.get(0);
        assertThat(fieldField.getFirst()).isEqualTo(QFOperationEnum.LIKE);
        assertThat(fieldField.getSecond()).containsExactly("auth");

        var allFields = qf.getAllFieldValues();
        assertThat(allFields).hasSize(1);
        var firstField = allFields.get(0);
        assertThat(firstField.name()).isEqualTo("author");
        assertThat(firstField.operation()).isEqualTo(QFOperationEnum.LIKE.getValue());
        assertThat(firstField.values()).containsExactly("auth");

        List<PostBlog> list = repository.findAll(qf);
        assertThat(list).hasSize(1);

        PostBlog postBlog = list.get(0);
        assertPostEqual(postBlog);

        qf = queryFilterProcessor.newQueryFilter("author[like]=auth", QFParamType.LHS_BRACKETS);
        assertThat(qf).isNotNull();

        list = repository.findAll(qf);
        assertThat(list).hasSize(1);

        postBlog = list.get(0);
        assertPostEqual(postBlog);

    }

    @Test
    @DisplayName("3. Test by not existing author")
    @Order(3)
    void testQueryByMissingAuthor() throws QueryFilterException {
        QueryFilter<PostBlog> qf = queryFilterProcessor.newQueryFilter("author=like:example", QFParamType.RHS_COLON);
        assertThat(qf).isNotNull();

        List<PostBlog> list = repository.findAll(qf);
        assertThat(list).isEmpty();

        qf = queryFilterProcessor.newQueryFilter("author[like]=example", QFParamType.LHS_BRACKETS);
        assertThat(qf).isNotNull();

        list = repository.findAll(qf);
        assertThat(list).isEmpty();
    }

    @Test
    @DisplayName("4. Test by avgNote")
    @Order(4)
    void testQueryByAvgNote() throws QueryFilterException {
        QueryFilter<PostBlog> qf = queryFilterProcessor.newQueryFilter("avgNote=gt:1.2", QFParamType.RHS_COLON);
        assertThat(qf).isNotNull();

        List<PostBlog> list = repository.findAll(qf);
        assertThat(list).hasSize(1);

        PostBlog postBlog = list.get(0);
        assertPostEqual(postBlog);
    }

    @Test
    @DisplayName("5. Test by avgNote less than 1")
    @Order(5)
    void testQueryByAvgNoteLessThan1() throws QueryFilterException {
        QueryFilter<PostBlog> qf = queryFilterProcessor.newQueryFilter("avgNote=lt:1.2", QFParamType.RHS_COLON);
        assertThat(qf).isNotNull();

        List<PostBlog> list = repository.findAll(qf);
        assertThat(list).isEmpty();
    }

    @Test
    @DisplayName("6. Test by create date")
    @Order(6)
    void testQueryByCreateDate() throws QueryFilterException {
        QueryFilter<PostBlog> qf = queryFilterProcessor.newQueryFilter("createDate=gt:2020-01-01T00:00:00Z",
                QFParamType.RHS_COLON);
        assertThat(qf).isNotNull();

        List<PostBlog> list = repository.findAll(qf);
        assertThat(list).hasSize(1);

        PostBlog postBlog = list.get(0);
        assertPostEqual(postBlog);
    }

    @Test
    @DisplayName("7. Test by last timestamp")
    @Order(7)
    void testQueryByLastTimestamp() throws QueryFilterException {

        QFNotValuable ex = assertThrows(QFNotValuable.class, () -> queryFilterProcessor
                .newQueryFilter("lastTimestamp=gt:2020-01-01T00:00:00Z", QFParamType.RHS_COLON));

        assertThat(ex).isNotNull();

        assertThat(ex.getField()).isEqualTo("lastTimestamp");
    }

    @Test
    @DisplayName("8. Test by published is blocked. Assert throws QueryFilterException")
    @Order(8)
    void testQueryByPublished() {

        QueryFilterException qfException = assertThrows(QueryFilterException.class, () -> {
            queryFilterProcessor.newQueryFilter("published=eq:true", QFParamType.RHS_COLON);
        });

        assertThat(qfException.getClass()).isAssignableFrom(QFBlockException.class);

        QFBlockException qfBlockException = (QFBlockException) qfException;
        assertThat(qfBlockException.getField()).isEqualTo("published");

    }

    @Test
    @DisplayName("9. Test by post type")
    @Order(9)
    void testQueryByPostType() throws QueryFilterException {
        QueryFilter<PostBlog> qf = queryFilterProcessor.newQueryFilter("postType=eq:TEXT", QFParamType.RHS_COLON);
        assertThat(qf).isNotNull();

        List<PostBlog> list = repository.findAll(qf);
        assertThat(list).hasSize(1);

        PostBlog postBlog = list.get(0);
        assertPostEqual(postBlog);
    }

    @Test
    @DisplayName("10. Test by published is allowed manually")
    @Order(10)
    void testQueryByPublishedManually() throws QueryFilterException {
        QueryFilter<PostBlog> qf = queryFilterProcessor.newQueryFilter(null, QFParamType.RHS_COLON);
        assertThat(qf).isNotNull();

        qf.addNewField("published", QFOperationEnum.EQUAL, "true");

        List<PostBlog> list = repository.findAll(qf);
        assertThat(list).hasSize(1);

        PostBlog postBlog = list.get(0);
        assertPostEqual(postBlog);

    }

    @Test
    @DisplayName("11. Test author between")
    @Order(11)
    void testQueryByAuthorBetween() throws QueryFilterException {
        QueryFilter<PostBlog> qf = queryFilterProcessor.newQueryFilter("author=btw:authoa,authoz",
                QFParamType.RHS_COLON);
        assertThat(qf).isNotNull();

        List<PostBlog> list = repository.findAll(qf);
        assertThat(list).hasSize(1);

        PostBlog postBlog = list.get(0);
        assertPostEqual(postBlog);
    }

    @Test
    @DisplayName("12. Test regular like")
    @Order(12)
    void testQueryByAuthorLike() throws QueryFilterException {
        QueryFilter<PostBlog> qf = queryFilterProcessor.newQueryFilter("author=rlike:autho_", QFParamType.RHS_COLON);
        assertThat(qf).isNotNull();

        List<PostBlog> list = repository.findAll(qf);
        assertThat(list).hasSize(1);

        PostBlog postBlog = list.get(0);
        assertPostEqual(postBlog);
    }

    @Test
    @DisplayName("END. Test by clear BBDD")
    @Order(Ordered.LOWEST_PRECEDENCE)
    void clearBBDD() {
        repository.deleteAll();
        assertThat(repository.findAll()).isEmpty();
    }
}
