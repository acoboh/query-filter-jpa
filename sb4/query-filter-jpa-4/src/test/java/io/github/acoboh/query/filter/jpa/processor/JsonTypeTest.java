package io.github.acoboh.query.filter.jpa.processor;

import io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.jpa.annotations.QFJsonElement;
import io.github.acoboh.query.filter.jpa.domain.JsonFilterDef;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QueryFilterDefinitionException;
import io.github.acoboh.query.filter.jpa.model.jsondata.ModelJson;
import io.github.acoboh.query.filter.jpa.operations.QFOperationJsonEnum;
import io.github.acoboh.query.filter.jpa.repositories.ModelJsonRepository;
import io.github.acoboh.query.filter.jpa.spring.SpringIntegrationTestBase;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JSON tests
 *
 * @author Adrián Cobo
 */
@SpringJUnitWebConfig(SpringIntegrationTestBase.Config.class)
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JsonTypeTest {

    private static final ModelJson MODEL1;
    private static final ModelJson MODEL2;

    static {

        Map<String, String> m1b = new HashMap<>();
        m1b.put("bkey1", "value1");
        m1b.put("bkey2", "value2");
        m1b.put("bkey3", "value3");

        MODEL1 = new ModelJson("MODEL1", m1b);

        Map<String, String> m2b = new HashMap<>();
        m2b.put("bkey1", "value1");
        m2b.put("bkey4", "value4");
        m2b.put("bkey5", "value5");

        MODEL2 = new ModelJson("MODEL2", m2b);
    }

    @Autowired
    private ModelJsonRepository repository;

    @Autowired
    private QFProcessor<JsonFilterDef, ModelJson> queryFilterProcessor;

    @Autowired
    private ApplicationContext applicationContext;

    private static QFProcessor<DefaultValuesDef, ModelJson> qfProcessorDefaultValues;

    @Test
    @DisplayName("0. Setup")
    @Order(0)
    void setup() throws QueryFilterDefinitionException {

        assertThat(queryFilterProcessor).isNotNull();
        assertThat(repository).isNotNull();

        assertThat(repository.findAll()).isEmpty();

        repository.saveAndFlush(MODEL1);
        repository.saveAndFlush(MODEL2);

        assertThat(repository.findAll()).hasSize(2).containsExactlyInAnyOrder(MODEL1, MODEL2);

        qfProcessorDefaultValues = new QFProcessor<>(DefaultValuesDef.class, ModelJson.class, applicationContext);
        assertThat(qfProcessorDefaultValues).isNotNull();

    }

    @Test
    @DisplayName("1. Test find by json data")
    @Order(1)
    void testFindByJsonData() {

        // Find common data
        QueryFilter<ModelJson> qf = queryFilterProcessor.newQueryFilter("jsonb=eq:{'bkey1':'value1'}",
                QFParamType.RHS_COLON);
        assertThat(qf).isNotNull();

        var actual = qf.getActualJsonValue("jsonb");
        assertThat(actual).isNotNull().hasSize(1).containsEntry("bkey1", "value1");

        var pair = qf.getFirstActualJsonOperation("jsonb");
        assertThat(pair).isNotNull();
        assertThat(pair.getFirst()).isEqualTo(QFOperationJsonEnum.EQUAL);
        assertThat(pair.getSecond()).hasSize(1).containsEntry("bkey1", "value1");

        var listA = qf.getActualJsonOperation("jsonb");
        assertThat(listA).isNotNull().hasSize(1);
        assertThat(listA.get(0).getFirst()).isEqualTo(QFOperationJsonEnum.EQUAL);
        assertThat(listA.get(0).getSecond()).hasSize(1).containsEntry("bkey1", "value1");

        var allData = qf.getAllFieldValues();
        assertThat(allData).isNotNull().hasSize(1);
        var data = allData.get(0);
        assertThat(data.name()).isEqualTo("jsonb");
        assertThat(data.values()).containsExactly("{'bkey1':'value1'}");
        assertThat(data.operation()).isEqualTo(QFOperationJsonEnum.EQUAL.getOperation());

        List<ModelJson> list = repository.findAll(qf);
        assertThat(list).hasSize(2).containsExactlyInAnyOrder(MODEL1, MODEL2);

        // Find separate data
        qf = queryFilterProcessor.newQueryFilter("jsonb=eq:{'bkey2':'value2'}", QFParamType.RHS_COLON);
        assertThat(qf).isNotNull();

        list = repository.findAll(qf);
        assertThat(list).hasSize(1).containsExactly(MODEL1);

        qf = queryFilterProcessor.newQueryFilter("jsonb=eq:{'bkey4':'value4'}", QFParamType.RHS_COLON);
        assertThat(qf).isNotNull();

        list = repository.findAll(qf);
        assertThat(list).hasSize(1).containsExactly(MODEL2);

    }

    @Test
    @DisplayName("2. Test default values")
    @Order(2)
    void testDefaultValues() {

        QueryFilter<ModelJson> qf = qfProcessorDefaultValues.newQueryFilter(null, QFParamType.RHS_COLON);
        assertThat(qf).isNotNull();

        assertThat(qf.isFiltering("jsonb")).isTrue();
        assertThat(qf.getActualJsonValue("jsonb")).containsEntry("bkey4", "value4");

        var found = repository.findAll(qf);
        assertThat(found).containsExactly(MODEL2);

    }

    @Test
    @DisplayName("END. Test by clear BBDD")
    @Order(Ordered.LOWEST_PRECEDENCE)
    void clearBBDD() {
        repository.deleteAll();
        assertThat(repository.findAll()).isEmpty();
    }

    @QFDefinitionClass(ModelJson.class)
    public static class DefaultValuesDef {

        @QFJsonElement(value = "jsonbData", defaultValue = "{'bkey4':'value4'}", defaultOperation = QFOperationJsonEnum.EQUAL)
        private String jsonb;
    }

}
