package io.github.acoboh.query.filter.jpa.processor;

import io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.jpa.annotations.QFDiscriminator;
import io.github.acoboh.query.filter.jpa.domain.DiscriminatorFilterDef;
import io.github.acoboh.query.filter.jpa.exceptions.QFDiscriminatorNotFoundException;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QueryFilterDefinitionException;
import io.github.acoboh.query.filter.jpa.model.discriminators.Announcement;
import io.github.acoboh.query.filter.jpa.model.discriminators.Post;
import io.github.acoboh.query.filter.jpa.model.discriminators.Topic;
import io.github.acoboh.query.filter.jpa.operations.QFOperationDiscriminatorEnum;
import io.github.acoboh.query.filter.jpa.repositories.PostDiscriminatorRepository;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

/**
 * Discriminator tests
 *
 * @author Adrián Cobo
 */
@SpringJUnitWebConfig(SpringIntegrationTestBase.Config.class)
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DiscriminatorTest {

    @Autowired
    private QFProcessor<DiscriminatorFilterDef, Topic> queryFilterProcessor;

    @Autowired
    private PostDiscriminatorRepository repository;

    @Autowired
    private ApplicationContext applicationContext;

    private static QFProcessor<DefaultValuesDef, Topic> qfProcessorDefaultValues;

    private static final Post POST_EXAMPLE = new Post();
    private static final Announcement ANN_EXAMPLE = new Announcement();

    static {

        POST_EXAMPLE.setOwner("Owner 1");
        POST_EXAMPLE.setTitle("Title 1");
        POST_EXAMPLE.setContent("Content 1");

        ANN_EXAMPLE.setOwner("Owner 2");
        ANN_EXAMPLE.setTitle("Title 2");
        ANN_EXAMPLE.setValidUntil(Timestamp.valueOf(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)));

    }

    @Test
    @DisplayName("0. Setup")
    @Order(0)
    void setup() throws QueryFilterDefinitionException {

        assertThat(queryFilterProcessor).isNotNull();
        assertThat(repository).isNotNull();

        assertThat(repository.findAll()).isEmpty();

        repository.saveAndFlush(POST_EXAMPLE);
        repository.saveAndFlush(ANN_EXAMPLE);

        assertThat(repository.findAll()).hasSize(2).containsExactlyInAnyOrder(POST_EXAMPLE, ANN_EXAMPLE);

        qfProcessorDefaultValues = new QFProcessor<>(DefaultValuesDef.class, Topic.class, applicationContext);
        assertThat(qfProcessorDefaultValues).isNotNull();
    }

    @Test
    @DisplayName("1. Announcement discriminator")
    @Order(1)
    void announcementDiscriminator() {

        QueryFilter<Topic> qf = queryFilterProcessor.newQueryFilter("type=eq:ANNOUNCEMENT", QFParamType.RHS_COLON);

        assertThat(qf).isNotNull();

        List<Topic> list = repository.findAll(qf);
        assertThat(list).hasSize(1).containsExactly(ANN_EXAMPLE);

    }

    @Test
    @DisplayName("2. Post discriminator")
    @Order(2)
    void postDiscriminator() {

        QueryFilter<Topic> qf = queryFilterProcessor.newQueryFilter("type=eq:POST", QFParamType.RHS_COLON);

        assertThat(qf).isNotNull();

        List<Topic> list = repository.findAll(qf);
        assertThat(list).hasSize(1).containsExactly(POST_EXAMPLE);

    }

    @Test
    @DisplayName("3. All discriminator")
    @Order(3)
    void allDiscriminator() {

        QueryFilter<Topic> qf = queryFilterProcessor.newQueryFilter("type=in:ANNOUNCEMENT,POST", QFParamType.RHS_COLON);

        assertThat(qf).isNotNull();

        List<Topic> list = repository.findAll(qf);
        assertThat(list).hasSize(2).containsExactlyInAnyOrder(POST_EXAMPLE, ANN_EXAMPLE);

    }

    @Test
    @DisplayName("4. Error type discriminator")
    @Order(4)
    void errorDiscriminator() {

        QFDiscriminatorNotFoundException ex = assertThrows(QFDiscriminatorNotFoundException.class,
                () -> queryFilterProcessor.newQueryFilter("type=in:POST_ERROR", QFParamType.RHS_COLON));

        assertThat(ex).isNotNull();
        assertThat(ex.getValue()).isEqualTo("POST_ERROR");
        assertThat(ex.getField()).isEqualTo("type");

    }

    @Test
    @DisplayName("5. Not equal discriminator")
    @Order(5)
    void testNotEqual() {

        QueryFilter<Topic> qf = queryFilterProcessor.newQueryFilter("type=ne:POST", QFParamType.RHS_COLON);

        assertThat(qf).isNotNull();

        List<Topic> list = repository.findAll(qf);
        assertThat(list).hasSize(1).containsExactly(ANN_EXAMPLE);

    }

    @Test
    @DisplayName("6. Not in discriminator")
    @Order(6)
    void testNotIn() {

        QueryFilter<Topic> qf = queryFilterProcessor.newQueryFilter("type=nin:POST,ANNOUNCEMENT",
                QFParamType.RHS_COLON);

        assertThat(qf).isNotNull();

        List<Topic> list = repository.findAll(qf);
        assertThat(list).isEmpty();

    }

    @Test
    @DisplayName("7. Manual discriminator")
    @Order(7)
    void testManualDiscriminator() {

        QueryFilter<Topic> qf = queryFilterProcessor.newQueryFilter(null, QFParamType.RHS_COLON);
        assertThat(qf).isNotNull();
        assertThat(qf.getInitialInput()).isEmpty();

        qf.addNewField("type", QFOperationDiscriminatorEnum.EQUAL, List.of("POST"));
        assertThat(qf.getInitialInput()).isEmpty();

        assertThat(qf.isFiltering("type")).isTrue();

        List<String> actualV = qf.getActualValue("type");
        assertThat(actualV).hasSize(1).containsExactly("POST");

        var pair = qf.getFirstActualDiscriminatorOperation("type");
        assertThat(pair).isNotNull();
        assertThat(pair.getFirst()).isEqualTo(QFOperationDiscriminatorEnum.EQUAL);
        assertThat(pair.getSecond()).containsExactly("POST");

        var listActual = qf.getActualDiscriminatorOperation("type");
        assertThat(listActual).isNotNull().hasSize(1);
        var pair2 = listActual.get(0);
        assertThat(pair2.getFirst()).isEqualTo(QFOperationDiscriminatorEnum.EQUAL);
        assertThat(pair2.getSecond()).containsExactly("POST");

        var fullList = qf.getAllFieldValues();
        assertThat(fullList).isNotNull().hasSize(1);
        var entry = fullList.get(0);
        assertThat(entry.name()).isEqualTo("type");
        assertThat(entry.values()).containsExactly("POST");
        assertThat(entry.operation()).isEqualTo(QFOperationDiscriminatorEnum.EQUAL.getOperation());

        List<Topic> list = repository.findAll(qf);
        assertThat(list).hasSize(1).containsExactly(POST_EXAMPLE);

        qf.deleteField("type");
        assertThat(qf.isFiltering("type")).isFalse();
        assertThat(qf.getActualValue("type")).isNull();

        list = repository.findAll(qf);
        assertThat(list).hasSize(2).containsExactlyInAnyOrder(POST_EXAMPLE, ANN_EXAMPLE);

    }

    @Test
    @DisplayName("8. Test default values")
    @Order(8)
    void testDefaultValues() {

        QueryFilter<Topic> qf = qfProcessorDefaultValues.newQueryFilter();
        assertThat(qf).isNotNull();

        assertThat(qf.isFiltering("type")).isTrue();
        assertThat(qf.getActualValue("type")).containsExactly("ANNOUNCEMENT");

        var fields = qf.getAllFieldValues();
        assertThat(fields).isNotNull().hasSize(1);
        var field = fields.get(0);
        assertThat(field.name()).isEqualTo("type");
        assertThat(field.values()).containsExactly("ANNOUNCEMENT");
        assertThat(field.operation()).isEqualTo(QFOperationDiscriminatorEnum.NOT_EQUAL.getOperation());

        List<Topic> found = repository.findAll(qf);
        assertThat(found).containsExactly(POST_EXAMPLE);

    }

    @Test
    @DisplayName("END. Test by clear BBDD")
    @Order(Ordered.LOWEST_PRECEDENCE)
    void clearBBDD() {
        repository.deleteAll();
        assertThat(repository.findAll()).isEmpty();
    }

    @QFDefinitionClass(Topic.class)
    public static class DefaultValuesDef {

        @QFDiscriminator(value = { @QFDiscriminator.Value(name = "ANNOUNCEMENT", type = Announcement.class),
                @QFDiscriminator.Value(name = "POST", type = Post.class) }, defaultValues = "ANNOUNCEMENT", defaultOperation = QFOperationDiscriminatorEnum.NOT_EQUAL)
        private String type;
    }

}
