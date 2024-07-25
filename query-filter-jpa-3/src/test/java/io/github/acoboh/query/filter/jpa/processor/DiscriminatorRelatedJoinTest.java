package io.github.acoboh.query.filter.jpa.processor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

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

import io.github.acoboh.query.filter.jpa.domain.FilterRelatedParentEntityDef;
import io.github.acoboh.query.filter.jpa.model.discriminators.joined.ParentEntity.TypeEnum;
import io.github.acoboh.query.filter.jpa.model.discriminators.joined.RelatedParent;
import io.github.acoboh.query.filter.jpa.model.discriminators.joined.SubclassAEntity;
import io.github.acoboh.query.filter.jpa.model.discriminators.joined.SubclassBEntity;
import io.github.acoboh.query.filter.jpa.repositories.ParentEntityRepository;
import io.github.acoboh.query.filter.jpa.repositories.RelatedParentEntityRepository;
import io.github.acoboh.query.filter.jpa.spring.SpringIntegrationTestBase;

/**
 * Discriminator join tests
 */
@SpringJUnitWebConfig(SpringIntegrationTestBase.Config.class)
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DiscriminatorRelatedJoinTest {

	private static final SubclassAEntity SUBCLASS_A_EXAMPLE = new SubclassAEntity();
	private static final SubclassAEntity SUBCLASS_A_EXAMPLE_2 = new SubclassAEntity();

	private static final SubclassBEntity SUBCLASS_B_EXAMPLE = new SubclassBEntity();
	private static final SubclassBEntity SUBCLASS_B_EXAMPLE_2 = new SubclassBEntity();

	// Subclass A
	private static final RelatedParent RELATED_PARENT = new RelatedParent();
	private static final RelatedParent RELATED_PARENT_2 = new RelatedParent();

	// Subclass B
	private static final RelatedParent RELATED_PARENT_3 = new RelatedParent();
	private static final RelatedParent RELATED_PARENT_4 = new RelatedParent();

	// Repeated subclas 1 A and B
	private static final RelatedParent RELATED_PARENT_5 = new RelatedParent();
	private static final RelatedParent RELATED_PARENT_6 = new RelatedParent();

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

		// Parents A
		RELATED_PARENT.setId("1");
		RELATED_PARENT.setParent(SUBCLASS_A_EXAMPLE);

		RELATED_PARENT_2.setId("2");
		RELATED_PARENT_2.setParent(SUBCLASS_A_EXAMPLE_2);

		// Parents B
		RELATED_PARENT_3.setId("3");
		RELATED_PARENT_3.setParent(SUBCLASS_B_EXAMPLE);

		RELATED_PARENT_4.setId("4");
		RELATED_PARENT_4.setParent(SUBCLASS_B_EXAMPLE_2);

		// Parents A and B
		RELATED_PARENT_5.setId("5");
		RELATED_PARENT_5.setParent(SUBCLASS_A_EXAMPLE);

		RELATED_PARENT_6.setId("6");
		RELATED_PARENT_6.setParent(SUBCLASS_B_EXAMPLE);
	}

	@Autowired
	private QFProcessor<FilterRelatedParentEntityDef, RelatedParent> queryFilterProcessor;

	@Autowired
	private ParentEntityRepository parentRepository;

	@Autowired
	private RelatedParentEntityRepository repository;

	@Test
	@DisplayName("0. Setup")
	@Order(0)
	void setup() {

		assertThat(queryFilterProcessor).isNotNull();
		assertThat(repository).isNotNull();

		assertThat(repository.findAll()).isEmpty();

		parentRepository.save(SUBCLASS_A_EXAMPLE);
		parentRepository.save(SUBCLASS_A_EXAMPLE_2);
		parentRepository.save(SUBCLASS_B_EXAMPLE);
		parentRepository.save(SUBCLASS_B_EXAMPLE_2);
		parentRepository.flush();

		repository.save(RELATED_PARENT);
		repository.save(RELATED_PARENT_2);
		repository.save(RELATED_PARENT_3);
		repository.save(RELATED_PARENT_4);
		repository.save(RELATED_PARENT_5);
		repository.save(RELATED_PARENT_6);
		repository.flush();

		assertThat(parentRepository.findAll()).hasSize(4).containsExactlyInAnyOrder(SUBCLASS_A_EXAMPLE,
				SUBCLASS_A_EXAMPLE_2, SUBCLASS_B_EXAMPLE, SUBCLASS_B_EXAMPLE_2);

		assertThat(repository.findAll()).hasSize(6).containsExactlyInAnyOrder(RELATED_PARENT, RELATED_PARENT_2,
				RELATED_PARENT_3, RELATED_PARENT_4, RELATED_PARENT_5, RELATED_PARENT_6);

	}

	@Test
	@DisplayName("1. Filter by parent class")
	@Order(1)
	void filterByParentClass() {

		QueryFilter<RelatedParent> qf = queryFilterProcessor.newQueryFilter("type=eq:A", QFParamType.RHS_COLON);

		assertThat(qf).isNotNull();

		List<RelatedParent> list = repository.findAll(qf);
		assertThat(list).hasSize(3).containsExactlyInAnyOrder(RELATED_PARENT, RELATED_PARENT_2, RELATED_PARENT_5);

		qf = queryFilterProcessor.newQueryFilter("type=eq:B", QFParamType.RHS_COLON);

		assertThat(qf).isNotNull();

		list = repository.findAll(qf);
		assertThat(list).hasSize(3).containsExactlyInAnyOrder(RELATED_PARENT_3, RELATED_PARENT_4, RELATED_PARENT_6);

	}

	@Test
	@DisplayName("2. Filter by discriminator class")
	@Order(2)
	void filterByDiscriminatorClass() {

		QueryFilter<RelatedParent> qf = queryFilterProcessor.newQueryFilter("discriminatorType=eq:subclassA",
				QFParamType.RHS_COLON);

		assertThat(qf).isNotNull();

		List<RelatedParent> list = repository.findAll(qf);
		assertThat(list).hasSize(3).containsExactlyInAnyOrder(RELATED_PARENT, RELATED_PARENT_2, RELATED_PARENT_5);

		qf = queryFilterProcessor.newQueryFilter("discriminatorType=eq:subclassB", QFParamType.RHS_COLON);

		assertThat(qf).isNotNull();

		list = repository.findAll(qf);
		assertThat(list).hasSize(3).containsExactlyInAnyOrder(RELATED_PARENT_3, RELATED_PARENT_4, RELATED_PARENT_6);

	}

	@Test
	@DisplayName("3. Filter by subclass a field")
	@Order(3)
	void filterBySubclassAField() {

		QueryFilter<RelatedParent> qf = queryFilterProcessor.newQueryFilter("subClassAField=eq:Subclass A field",
				QFParamType.RHS_COLON);

		assertThat(qf).isNotNull();

		List<RelatedParent> list = repository.findAll(qf);
		assertThat(list).hasSize(2).containsExactlyInAnyOrder(RELATED_PARENT, RELATED_PARENT_5);

		qf = queryFilterProcessor.newQueryFilter("subClassAField=ne:Subclass A field", QFParamType.RHS_COLON);

		assertThat(qf).isNotNull();

		list = repository.findAll(qf);
		assertThat(list).hasSize(1).containsExactlyInAnyOrder(RELATED_PARENT_2);

		qf.deleteField("subClassAField");

		list = repository.findAll(qf);
		assertThat(list).hasSize(6).containsExactlyInAnyOrder(RELATED_PARENT, RELATED_PARENT_2, RELATED_PARENT_3,
				RELATED_PARENT_4, RELATED_PARENT_5, RELATED_PARENT_6);

		qf = queryFilterProcessor.newQueryFilter("sort=+subClassAField", QFParamType.RHS_COLON);

		assertThat(qf).isNotNull();

		// If the field is from nested entity, it will apply a inner join
		list = repository.findAll(qf);
		assertThat(list).hasSize(3).containsExactly(RELATED_PARENT, RELATED_PARENT_5, RELATED_PARENT_2);

	}

	@Test
	@DisplayName("END. Test by clear BBDD")
	@Order(Ordered.LOWEST_PRECEDENCE)
	void clearBBDD() {
		repository.deleteAll();
		assertThat(repository.findAll()).isEmpty();

		parentRepository.deleteAll();
		assertThat(parentRepository.findAll()).isEmpty();
	}

}
