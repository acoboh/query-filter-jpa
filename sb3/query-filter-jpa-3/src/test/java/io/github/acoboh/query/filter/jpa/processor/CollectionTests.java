package io.github.acoboh.query.filter.jpa.processor;

import io.github.acoboh.query.filter.jpa.annotations.QFCollectionElement;
import io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.jpa.domain.FilterCollectionBlogDef;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QueryFilterDefinitionException;
import io.github.acoboh.query.filter.jpa.model.Comments;
import io.github.acoboh.query.filter.jpa.model.PostBlog;
import io.github.acoboh.query.filter.jpa.operations.QFCollectionOperationEnum;
import io.github.acoboh.query.filter.jpa.repositories.PostBlogRepository;
import io.github.acoboh.query.filter.jpa.spring.SpringIntegrationTestBase;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
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

@SpringJUnitWebConfig(SpringIntegrationTestBase.Config.class)
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CollectionTests {

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

        // Truncated to avoid rounding issues with Java > 8 and BBDD
        POST_EXAMPLE.setCreateDate(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS));
        POST_EXAMPLE.setLastTimestamp(Timestamp.valueOf(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)));
        POST_EXAMPLE.setPublished(true);
        POST_EXAMPLE.setPostType(PostBlog.PostType.TEXT);

        Set<Comments> commentsList = new HashSet<>();

        Comments comment = new Comments();
        comment.setId(1);
        comment.setAuthor("Author 1");
        comment.setComment("Comment 1");
        comment.setLikes(100);
        comment.setPostBlog(POST_EXAMPLE);

        commentsList.add(comment);

        POST_EXAMPLE.setComments(commentsList);

        POST_EXAMPLE_2.setUuid(UUID.randomUUID());
        POST_EXAMPLE_2.setAuthor("Author 2");
        POST_EXAMPLE_2.setText("Text 2");
        POST_EXAMPLE_2.setAvgNote(0.5d);
        POST_EXAMPLE_2.setLikes(2);

        // Truncated to avoid rounding issues with Java > 8 and BBDD
        POST_EXAMPLE_2.setCreateDate(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS));
        POST_EXAMPLE_2.setLastTimestamp(Timestamp.valueOf(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)));
        POST_EXAMPLE_2.setPublished(false);
        POST_EXAMPLE_2.setPostType(PostBlog.PostType.VIDEO);

        Set<Comments> commentsList2 = new HashSet<>();

        Comments comment2 = new Comments();
        comment2.setId(2);
        comment2.setAuthor("Author 2");
        comment2.setComment("Comment 2");
        comment2.setLikes(200);
        comment2.setPostBlog(POST_EXAMPLE_2);

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

        commentsList2.add(comment2);
        commentsList2.add(comment3);
        commentsList2.add(comment4);

        POST_EXAMPLE_2.setComments(commentsList2);
    }

    @Autowired
    private QFProcessor<FilterCollectionBlogDef, PostBlog> queryFilterProcessor;

    private static QFProcessor<DefaultValuesDef, PostBlog> qfProcessorDefaultValues;

    @Autowired
    private PostBlogRepository repository;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("0. Setup")
    @Order(0)
    void setup() throws QueryFilterDefinitionException {

        assertThat(queryFilterProcessor).isNotNull();
        assertThat(repository).isNotNull();

        assertThat(repository.findAll()).isEmpty();

        repository.saveAndFlush(POST_EXAMPLE);
        repository.saveAndFlush(POST_EXAMPLE_2);

        assertThat(repository.findAll()).hasSize(2).containsExactlyInAnyOrder(POST_EXAMPLE, POST_EXAMPLE_2);

        qfProcessorDefaultValues = new QFProcessor<>(DefaultValuesDef.class, PostBlog.class, applicationContext);
        assertThat(qfProcessorDefaultValues).isNotNull();
    }

    @Test
    @DisplayName("1. Test by method")
    @Order(1)
    void testCollectionFilterByMethods() {

        QueryFilter<PostBlog> qf = queryFilterProcessor.newQueryFilter("", QFParamType.RHS_COLON);
        qf.addNewField("commentsSize", QFCollectionOperationEnum.GREATER_THAN, 1);

        var actualV = qf.getActualCollectionValue("commentsSize");
        assertThat(actualV).isNotNull().isEqualTo(1);

        var pairActual = qf.getFirstActualCollectionOperation("commentsSize");
        assertThat(pairActual).isNotNull();
        assertThat(pairActual.getFirst()).isEqualTo(QFCollectionOperationEnum.GREATER_THAN);
        assertThat(pairActual.getSecond()).isEqualTo(1);

        var listActual = qf.getActualCollectionOperation("commentsSize");
        assertThat(listActual).isNotNull().hasSize(1);
        assertThat(listActual.get(0).getFirst()).isEqualTo(QFCollectionOperationEnum.GREATER_THAN);
        assertThat(listActual.get(0).getSecond()).isEqualTo(1);

        var allData = qf.getAllFieldValues();
        assertThat(allData).isNotNull().hasSize(1);
        var data = allData.get(0);
        assertThat(data.name()).isEqualTo("commentsSize");
        assertThat(data.values()).containsExactly("1");
        assertThat(data.operation()).isEqualTo(QFCollectionOperationEnum.GREATER_THAN.getOperation());

        List<PostBlog> list = repository.findAll(qf);
        assertThat(list).hasSize(1).containsExactly(POST_EXAMPLE_2);

        qf.deleteField("commentsSize");
        assertThat(qf.isFiltering("commentsSize")).isFalse();
        list = repository.findAll(qf);
        assertThat(list).hasSize(2).containsExactlyInAnyOrder(POST_EXAMPLE, POST_EXAMPLE_2);

        qf.overrideField("commentsSize", QFCollectionOperationEnum.LESS_THAN, 2);
        list = repository.findAll(qf);
        assertThat(list).hasSize(1).containsExactly(POST_EXAMPLE);

    }

    @Test
    @DisplayName("2. Test by string filter")
    @Order(2)
    void testCollectionFilterString() {

        QueryFilter<PostBlog> qf = queryFilterProcessor.newQueryFilter("commentsSize=eq:1", QFParamType.RHS_COLON);

        List<PostBlog> list = repository.findAll(qf);
        assertThat(list).hasSize(1).containsExactly(POST_EXAMPLE);

        qf = queryFilterProcessor.newQueryFilter("commentsSize=gt:1", QFParamType.RHS_COLON);
        list = repository.findAll(qf);
        assertThat(list).hasSize(1).containsExactly(POST_EXAMPLE_2);

        qf = queryFilterProcessor.newQueryFilter("commentsSize[gt]=1", QFParamType.LHS_BRACKETS);
        list = repository.findAll(qf);
        assertThat(list).hasSize(1).containsExactly(POST_EXAMPLE_2);

        qf = queryFilterProcessor.newQueryFilter("commentsSize[ne]=1&author[eq]=Author 2", QFParamType.LHS_BRACKETS);
        list = repository.findAll(qf);
        assertThat(list).hasSize(1).containsExactly(POST_EXAMPLE_2);

        qf = queryFilterProcessor.newQueryFilter("commentsSize[ne]=3&author[eq]=Author 2", QFParamType.LHS_BRACKETS);
        list = repository.findAll(qf);
        assertThat(list).isEmpty();

    }

    @Test
    @DisplayName("2. Test multiple fields")
    @Order(3)
    void testCollectionFilterMultiple() {

        QueryFilter<PostBlog> qf = queryFilterProcessor.newQueryFilter("commentsSize=eq:1&authorComments=eq:Author 1",
                QFParamType.RHS_COLON);

        List<PostBlog> list = repository.findAll(qf);
        assertThat(list).hasSize(1).containsExactly(POST_EXAMPLE);

        qf = queryFilterProcessor.newQueryFilter("commentsSize=eq:1&authorComments=ne:Author 1", QFParamType.RHS_COLON);
        list = repository.findAll(qf);
        assertThat(list).isEmpty();

        qf = queryFilterProcessor.newQueryFilter("commentsSize[gte]=1&authorComments[eq]=Author 1",
                QFParamType.LHS_BRACKETS);
        list = repository.findAll(qf);
        assertThat(list).hasSize(1).containsExactly(POST_EXAMPLE);

        qf = queryFilterProcessor.newQueryFilter("commentsSize[gte]=1&authorComments[ne]=Author 1",
                QFParamType.LHS_BRACKETS);
        list = repository.findAll(qf);
        assertThat(list).hasSize(1).containsExactly(POST_EXAMPLE_2);

    }

    @Test
    @DisplayName("Test default value")
    @Order(4)
    void testDefaultValues() {

        QueryFilter<PostBlog> qf = qfProcessorDefaultValues.newQueryFilter();
        assertThat(qf).isNotNull();

        assertThat(qf.isFiltering("commentsSize")).isTrue();
        assertThat(qf.getActualCollectionValue("commentsSize")).isEqualTo(2);

        var fieldValues = qf.getAllFieldValues();
        assertThat(fieldValues).hasSize(1).containsExactlyInAnyOrder(
                // commentsSize=gt:10 (default value)
                new QFFieldInfo("commentsSize", "lt", List.of("2")));

        var found = repository.findAll(qf);
        assertThat(found).containsExactly(POST_EXAMPLE);

    }

    @Test
    @DisplayName("5. Test by clear BBDD")
    @Order(Ordered.LOWEST_PRECEDENCE)
    void clearBBDD() {
        repository.deleteAll();
        assertThat(repository.findAll()).isEmpty();
    }

    @QFDefinitionClass(PostBlog.class)
    public static class DefaultValuesDef {

        @QFCollectionElement(value = "comments", defaultValue = 2, defaultOperation = QFCollectionOperationEnum.LESS_THAN)
        private int commentsSize;

    }

}
