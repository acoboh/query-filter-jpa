package io.github.acoboh.query.filter.jpa.processor;

import io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.jpa.annotations.QFElement;
import io.github.acoboh.query.filter.jpa.annotations.QFRequired;
import io.github.acoboh.query.filter.jpa.annotations.QFSortable;
import io.github.acoboh.query.filter.jpa.exceptions.QFRequiredException;
import io.github.acoboh.query.filter.jpa.exceptions.QueryFilterException;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QueryFilterDefinitionException;
import io.github.acoboh.query.filter.jpa.model.PostBlog;
import io.github.acoboh.query.filter.jpa.operations.QFOperationEnum;
import io.github.acoboh.query.filter.jpa.repositories.PostBlogRepository;
import io.github.acoboh.query.filter.jpa.spring.SpringIntegrationTestBase;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.context.web.WebAppConfiguration;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for required fields in query filters.
 * 
 * @author Adrián Cobo
 */
@SpringJUnitWebConfig(SpringIntegrationTestBase.Config.class)
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RequiredTest {

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

    private static QFProcessor<@NonNull RequiredAuthorDef, @NonNull PostBlog> processorAuthor;
    private static QFProcessor<@NonNull RequiredSortDef, @NonNull PostBlog> processorSort;
    private static QFProcessor<@NonNull RequiredSortDef2, @NonNull PostBlog> processorSort2;
    private static QFProcessor<@NonNull RequiredAuthorDef2, @NonNull PostBlog> processorAuthor2;
    private static QFProcessor<@NonNull RequiredAuthorDef3, @NonNull PostBlog> processorAuthor3;
    private static QFProcessor<@NonNull RequiredAuthorDef4, @NonNull PostBlog> processorAuthor4;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private PostBlogRepository repository;

    // Initialize the processors
    @Test
    @DisplayName("0. Setup Processors")
    @Order(Ordered.HIGHEST_PRECEDENCE)
    void initProcessors() throws QueryFilterDefinitionException {
        processorAuthor = new QFProcessor<>(RequiredAuthorDef.class, PostBlog.class, applicationContext);
        processorSort = new QFProcessor<>(RequiredSortDef.class, PostBlog.class, applicationContext);
        processorSort2 = new QFProcessor<>(RequiredSortDef2.class, PostBlog.class, applicationContext);
        processorAuthor2 = new QFProcessor<>(RequiredAuthorDef2.class, PostBlog.class, applicationContext);
        processorAuthor3 = new QFProcessor<>(RequiredAuthorDef3.class, PostBlog.class, applicationContext);
        processorAuthor4 = new QFProcessor<>(RequiredAuthorDef4.class, PostBlog.class, applicationContext);
        assertThat(processorAuthor).isNotNull();
        assertThat(processorSort).isNotNull();
        assertThat(processorSort2).isNotNull();
        assertThat(processorAuthor2).isNotNull();
        assertThat(processorAuthor3).isNotNull();
        assertThat(processorAuthor4).isNotNull();
    }

    // Set up the database with a sample post
    @Test
    @DisplayName("1. Setup Database")
    @Order(1)
    void setupDatabase() {
        assertThat(repository).isNotNull();
        repository.save(POST_EXAMPLE);
        assertThat(repository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("2. Test with required author throw exception")
    @Order(2)
    void testRequiredAuthor() {

        var ex = assertThrows(QFRequiredException.class, () -> processorAuthor.newQueryFilter());
        assertThat(ex.getMessage()).contains("author");

        ex = assertThrows(QFRequiredException.class,
                () -> processorAuthor.newQueryFilter("likes=gt:0", QFParamType.RHS_COLON));
        assertThat(ex.getMessage()).contains("author");

    }

    @Test
    @DisplayName("3. Test with required author and valid value")
    @Order(3)
    void testRequiredAuthorValid() throws QueryFilterException {
        var qf = processorAuthor.newQueryFilter("author=eq:author", QFParamType.RHS_COLON);
        assertThat(qf).isNotNull();

        var list = repository.findAll(qf);
        assertThat(list).hasSize(1).containsExactly(POST_EXAMPLE);
    }

    @Test
    @DisplayName("4. Test with required likes on sortable required on string but not on sort or execution")
    @Order(4)
    void testRequiredLikesOnSortable() {

        var qf = processorSort.newQueryFilter("sort=+likes", QFParamType.RHS_COLON);
        assertThat(qf).isNotNull();

        var list = repository.findAll(qf);
        assertThat(list).hasSize(1).containsExactly(POST_EXAMPLE);

        var ex = assertThrows(QFRequiredException.class,
                () -> processorSort.newQueryFilter("author=eq:author", QFParamType.RHS_COLON));
        assertThat(ex.getMessage()).contains("likes");

        qf = processorSort.newQueryFilter("author=eq:author&sort=+likes", QFParamType.RHS_COLON);
        assertThat(qf).isNotNull();

        // Clear sort to test without sorting
        qf.clearSort();

        // Now it should not throw an exception
        list = repository.findAll(qf);
        assertThat(list).hasSize(1).containsExactly(POST_EXAMPLE);

    }

    @Test
    @DisplayName("5. Test with required likes on sortable and valid value")
    @Order(5)
    void testRequiredLikesOnSortableValid() throws QueryFilterException {
        var qf = processorSort2.newQueryFilter("author=eq:author&sort=+likes", QFParamType.RHS_COLON);
        assertThat(qf).isNotNull();

        var list = repository.findAll(qf);
        assertThat(list).hasSize(1).containsExactly(POST_EXAMPLE);

        var ex = assertThrows(QFRequiredException.class, () -> processorSort2.newQueryFilter());
        assertThat(ex.getMessage()).contains("likes");

        // Clear sort to test without sorting
        qf.clearSort();

        // Now it should throw an exception
        final var finalQf = qf;
        ex = assertThrows(QFRequiredException.class, () -> repository.findAll(finalQf));
        assertThat(ex.getMessage()).contains("likes");
    }

    @Test
    @DisplayName("6. Required author not on string but on execution")
    @Order(6)
    void testRequiredAuthorNotOnStringButOnExecution() {

        // Works by default
        var qf = processorAuthor2.newQueryFilter("author=eq:author", QFParamType.RHS_COLON);
        assertThat(qf).isNotNull();

        var list = repository.findAll(qf);
        assertThat(list).hasSize(1).containsExactly(POST_EXAMPLE);

        // Works on string build
        qf = processorAuthor2.newQueryFilter();
        assertThat(qf).isNotNull();

        // Error on execution
        final var finalQf = qf;
        var ex = assertThrows(QFRequiredException.class, () -> repository.findAll(finalQf));
        assertThat(ex.getMessage()).contains("author");

        // Added and works on execution
        qf.addNewField("author", QFOperationEnum.EQUAL, "author");
        list = repository.findAll(qf);
        assertThat(list).hasSize(1).containsExactly(POST_EXAMPLE);
    }

    @Test
    @DisplayName("7. Required author on sort, not in string or execution")
    @Order(7)
    void testRequiredAuthorOnExecutionAndSortNotInString() {

        // Author with operation, not sort, throw exception
        var qf = processorAuthor3.newQueryFilter("author=eq:author", QFParamType.RHS_COLON);
        assertThat(qf).isNotNull();

        final var finalQF1 = qf;
        var ex = assertThrows(QFRequiredException.class, () -> repository.findAll(finalQF1));
        assertThat(ex.getMessage()).contains("author");
        assertThat(ex.getField()).isEqualTo("author");

        // Works on string build
        qf = processorAuthor3.newQueryFilter();
        assertThat(qf).isNotNull();

        // Error on execution
        final var finalQf2 = qf;
        ex = assertThrows(QFRequiredException.class, () -> repository.findAll(finalQf2));
        assertThat(ex.getMessage()).contains("author");

        // Added and works on execution
        qf.addSortBy("author", Sort.Direction.ASC);

        var list = repository.findAll(qf);
        assertThat(list).hasSize(1).containsExactly(POST_EXAMPLE);

        // Works on sort with string
        qf = processorAuthor3.newQueryFilter("sort=+author", QFParamType.RHS_COLON);
        assertThat(qf).isNotNull();
        list = repository.findAll(qf);
        assertThat(list).hasSize(1).containsExactly(POST_EXAMPLE);

    }

    @Test
    @DisplayName("8. Test with required author only on string")
    @Order(8)
    void testRequiredAuthorOnlyOnString() {

        // Author with operation, not sort, throw exception
        var ex = assertThrows(QFRequiredException.class, () -> processorAuthor4.newQueryFilter());
        assertThat(ex.getMessage()).contains("author");

        var qf = processorAuthor4.newQueryFilter("author=eq:author", QFParamType.RHS_COLON);
        assertThat(qf).isNotNull();

        var list = repository.findAll(qf);
        assertThat(list).hasSize(1).containsExactly(POST_EXAMPLE);

        qf.deleteField("author");

        // Now it should not throw an exception
        list = repository.findAll(qf);
        assertThat(list).hasSize(1).containsExactly(POST_EXAMPLE);

    }

    @Test
    @DisplayName("END. Test by clear BBDD")
    @Order(Ordered.LOWEST_PRECEDENCE)
    void clearBBDD() {
        repository.deleteAll();
        assertThat(repository.findAll()).isEmpty();
    }

    // Filter definitions
    @QFDefinitionClass(PostBlog.class)
    public static class RequiredAuthorDef {

        @QFElement("author")
        @QFRequired
        private String author;

        @QFElement("likes")
        private int likes;
    }

    @QFDefinitionClass(PostBlog.class)
    public static class RequiredSortDef {

        @QFElement("author")
        private String author;

        @QFSortable("likes")
        @QFRequired
        private int likes;
    }

    @QFDefinitionClass(PostBlog.class)
    public static class RequiredSortDef2 {

        @QFElement("author")
        private String author;

        @QFSortable("likes")
        @QFRequired(onSort = true)
        private int likes;
    }

    @QFDefinitionClass(PostBlog.class)
    public static class RequiredAuthorDef2 {
        @QFElement("author")
        @QFRequired(onStringFilter = false)
        private String author;
    }

    @QFDefinitionClass(PostBlog.class)
    public static class RequiredAuthorDef3 {
        @QFElement("author")
        @QFRequired(onStringFilter = false, onSort = true, onExecution = false)
        private String author;
    }

    @QFDefinitionClass(PostBlog.class)
    public static class RequiredAuthorDef4 {
        @QFElement("author")
        @QFRequired(onExecution = false)
        private String author;
    }
}
