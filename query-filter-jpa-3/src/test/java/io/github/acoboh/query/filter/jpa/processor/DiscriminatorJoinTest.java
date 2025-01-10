package io.github.acoboh.query.filter.jpa.processor;

import io.github.acoboh.query.filter.jpa.domain.FilterParentEntityDef;
import io.github.acoboh.query.filter.jpa.model.discriminators.joined.ParentEntity;
import io.github.acoboh.query.filter.jpa.model.discriminators.joined.ParentEntity.TypeEnum;
import io.github.acoboh.query.filter.jpa.model.discriminators.joined.SubclassAEntity;
import io.github.acoboh.query.filter.jpa.model.discriminators.joined.SubclassBEntity;
import io.github.acoboh.query.filter.jpa.repositories.ParentEntityRepository;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Discriminator join tests
 */
@SpringJUnitWebConfig(SpringIntegrationTestBase.Config.class)
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DiscriminatorJoinTest {

    private static final SubclassAEntity SUBCLASS_A_EXAMPLE = new SubclassAEntity();
    private static final SubclassAEntity SUBCLASS_A_EXAMPLE_2 = new SubclassAEntity();

    private static final SubclassBEntity SUBCLASS_B_EXAMPLE = new SubclassBEntity();
    private static final SubclassBEntity SUBCLASS_B_EXAMPLE_2 = new SubclassBEntity();

    static {
        SUBCLASS_A_EXAMPLE.setId("1");
        SUBCLASS_A_EXAMPLE.setActive(true);
        SUBCLASS_A_EXAMPLE.setType(TypeEnum.A);
        SUBCLASS_A_EXAMPLE.setSubClassField("Subclass A field");
        SUBCLASS_A_EXAMPLE.setFlag(true);

        SUBCLASS_A_EXAMPLE_2.setId("2");
        SUBCLASS_A_EXAMPLE_2.setActive(false);
        SUBCLASS_A_EXAMPLE_2.setType(TypeEnum.A);
        SUBCLASS_A_EXAMPLE_2.setSubClassField("Subclass A field 2");
        SUBCLASS_A_EXAMPLE_2.setFlag(false);

        SUBCLASS_B_EXAMPLE.setId("3");
        SUBCLASS_B_EXAMPLE.setActive(true);
        SUBCLASS_B_EXAMPLE.setType(TypeEnum.B);
        SUBCLASS_B_EXAMPLE.setText("Subclass B text");

        SUBCLASS_B_EXAMPLE_2.setId("4");
        SUBCLASS_B_EXAMPLE_2.setActive(false);
        SUBCLASS_B_EXAMPLE_2.setType(TypeEnum.B);
        SUBCLASS_B_EXAMPLE_2.setText("Subclass B text 2");

    }

    @Autowired
    private QFProcessor<FilterParentEntityDef, ParentEntity> queryFilterProcessor;

    @Autowired
    private ParentEntityRepository repository;

    @Test
    @DisplayName("0. Setup")
    @Order(0)
    void setup() {

        assertThat(queryFilterProcessor).isNotNull();
        assertThat(repository).isNotNull();

        assertThat(repository.findAll()).isEmpty();

        repository.save(SUBCLASS_A_EXAMPLE);
        repository.save(SUBCLASS_A_EXAMPLE_2);
        repository.save(SUBCLASS_B_EXAMPLE);
        repository.save(SUBCLASS_B_EXAMPLE_2);
        repository.flush();

        assertThat(repository.findAll()).hasSize(4).containsExactlyInAnyOrder(SUBCLASS_A_EXAMPLE, SUBCLASS_A_EXAMPLE_2,
                SUBCLASS_B_EXAMPLE, SUBCLASS_B_EXAMPLE_2);

    }

    @Test
    @DisplayName("1. Filter by parent class")
    @Order(1)
    void filterByParentClass() {

        QueryFilter<ParentEntity> qf = queryFilterProcessor.newQueryFilter("type=eq:A", QFParamType.RHS_COLON);

        assertThat(qf).isNotNull();

        List<ParentEntity> list = repository.findAll(qf);
        assertThat(list).hasSize(2).containsExactlyInAnyOrder(SUBCLASS_A_EXAMPLE, SUBCLASS_A_EXAMPLE_2);

        qf = queryFilterProcessor.newQueryFilter("type=eq:B", QFParamType.RHS_COLON);

        assertThat(qf).isNotNull();

        list = repository.findAll(qf);
        assertThat(list).hasSize(2).containsExactlyInAnyOrder(SUBCLASS_B_EXAMPLE, SUBCLASS_B_EXAMPLE_2);

    }

    @Test
    @DisplayName("2. Filter by discriminator class")
    @Order(2)
    void filterByDiscriminatorClass() {

        QueryFilter<ParentEntity> qf = queryFilterProcessor.newQueryFilter("discriminatorType=eq:subclassA",
                QFParamType.RHS_COLON);

        assertThat(qf).isNotNull();

        List<ParentEntity> list = repository.findAll(qf);
        assertThat(list).hasSize(2).containsExactlyInAnyOrder(SUBCLASS_A_EXAMPLE, SUBCLASS_A_EXAMPLE_2);

        qf = queryFilterProcessor.newQueryFilter("discriminatorType=eq:subclassB", QFParamType.RHS_COLON);

        assertThat(qf).isNotNull();

        list = repository.findAll(qf);
        assertThat(list).hasSize(2).containsExactlyInAnyOrder(SUBCLASS_B_EXAMPLE, SUBCLASS_B_EXAMPLE_2);

    }

    @Test
    @DisplayName("3. Filter by subclass a field")
    @Order(3)
    void filterBySubclassAField() {

        // On Hibernate 6, the filter will apply a Left Join on the subclassA table and all data from SubclassB will be returned matching
        // the criteria query

        QueryFilter<ParentEntity> qf = queryFilterProcessor.newQueryFilter("subClassAField=eq:Subclass A field",
                QFParamType.RHS_COLON);

        assertThat(qf).isNotNull();

        List<ParentEntity> list = repository.findAll(qf);
        assertThat(list).hasSize(1).containsExactlyInAnyOrder(SUBCLASS_A_EXAMPLE);

        qf = queryFilterProcessor.newQueryFilter("subClassAField=ne:Subclass A field", QFParamType.RHS_COLON);

        assertThat(qf).isNotNull();

        list = repository.findAll(qf);
        assertThat(list).hasSize(1).containsExactlyInAnyOrder(SUBCLASS_A_EXAMPLE_2);

        qf.deleteField("subClassAField");

        list = repository.findAll(qf);
        assertThat(list).hasSize(4).containsExactlyInAnyOrder(SUBCLASS_A_EXAMPLE, SUBCLASS_A_EXAMPLE_2,
                SUBCLASS_B_EXAMPLE, SUBCLASS_B_EXAMPLE_2);

        qf = queryFilterProcessor.newQueryFilter("sort=-subClassAField", QFParamType.RHS_COLON);

        // On Hibernate 6, the sort will only do a Left Join on the subclassA table and all data from SubclassB will be returned
        list = repository.findAll(qf);

        // Nulls are returned first
        assertThat(list).hasSize(4).containsExactly(SUBCLASS_B_EXAMPLE, SUBCLASS_B_EXAMPLE_2, SUBCLASS_A_EXAMPLE_2,
                SUBCLASS_A_EXAMPLE);

        qf = queryFilterProcessor.newQueryFilter("sort=-subClassAField&subClassAField=null:true",
                QFParamType.RHS_COLON);

        // Found also data from SubclassB
        list = repository.findAll(qf);
        assertThat(list).hasSize(2).containsExactly(SUBCLASS_B_EXAMPLE, SUBCLASS_B_EXAMPLE_2);

        qf = queryFilterProcessor.newQueryFilter("sort=-subClassAField&subClassAField=null:false",
                QFParamType.RHS_COLON);

        list = repository.findAll(qf);
        assertThat(list).hasSize(2).containsExactly(SUBCLASS_A_EXAMPLE_2, SUBCLASS_A_EXAMPLE);

    }

    @Test
    @DisplayName("END. Test by clear BBDD")
    @Order(Ordered.LOWEST_PRECEDENCE)
    void clearBBDD() {
        repository.deleteAll();
        assertThat(repository.findAll()).isEmpty();
    }

}
