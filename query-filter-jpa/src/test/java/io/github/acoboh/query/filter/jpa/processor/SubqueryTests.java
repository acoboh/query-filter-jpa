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
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.context.web.WebAppConfiguration;

import io.github.acoboh.query.filter.jpa.domain.UserModelFilterDef;
import io.github.acoboh.query.filter.jpa.model.subquery.RoleModel;
import io.github.acoboh.query.filter.jpa.model.subquery.UserModel;
import io.github.acoboh.query.filter.jpa.repositories.RoleRepository;
import io.github.acoboh.query.filter.jpa.repositories.UserRepository;
import io.github.acoboh.query.filter.jpa.spring.SpringIntegrationTestBase;

/**
 * Subquery tests
 * 
 * @author Adrián Cobo
 *
 */
@SpringJUnitWebConfig(SpringIntegrationTestBase.Config.class)
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SubqueryTests {

	private static final UserModel USER1 = new UserModel();
	private static final UserModel USER2 = new UserModel();
	private static final UserModel USER3 = new UserModel();
	private static final UserModel USER4 = new UserModel();

	private static final RoleModel ROLE1 = new RoleModel();
	private static final RoleModel ROLE2 = new RoleModel();
	private static final RoleModel ROLE3 = new RoleModel();
	private static final RoleModel ROLE4 = new RoleModel();

	// USER 1 - R1 R2
	// USER 2 - R3 R4
	// USER 3 - R1 R3
	// USER 4 - R2 R3

	static {
		ROLE1.setName("ROLE1");
		ROLE2.setName("ROLE2");
		ROLE3.setName("ROLE3");
		ROLE4.setName("ROLE4");

		USER1.setUsername("USER1");
		USER1.getRoles().add(ROLE1);
		USER1.getRoles().add(ROLE2);

		USER2.setUsername("USER2");
		USER2.getRoles().add(ROLE3);
		USER2.getRoles().add(ROLE4);

		USER3.setUsername("USER3");
		USER3.getRoles().add(ROLE1);
		USER3.getRoles().add(ROLE3);

		USER4.setUsername("USER4");
		USER4.getRoles().add(ROLE2);
		USER4.getRoles().add(ROLE4);

	}

	@Autowired
	private QFProcessor<UserModelFilterDef, UserModel> queryFilterProcessor;

	@Autowired
	private UserRepository repository;

	@Autowired
	private RoleRepository roleRepository;

	@Test
	@DisplayName("0. Setup")
	@Order(0)
	void setup() {

		assertThat(queryFilterProcessor).isNotNull();
		assertThat(repository).isNotNull();

		assertThat(repository.findAll()).isEmpty();
		assertThat(roleRepository.findAll()).isEmpty();

		roleRepository.save(ROLE1);
		roleRepository.save(ROLE2);
		roleRepository.save(ROLE3);
		roleRepository.save(ROLE4);

		repository.saveAndFlush(USER1);
		repository.saveAndFlush(USER2);
		repository.saveAndFlush(USER3);
		repository.saveAndFlush(USER4);

	}

	@Test
	@DisplayName("1. Test user has no role")
	@Order(1)
	void testHasNotRole() {
		QueryFilter<UserModel> qf = queryFilterProcessor.newQueryFilter("roleNotSub=ne:ROLE1", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		// In this case, all users have one condition where the role name is not ROLE1
		List<UserModel> list = repository.findAll(qf);
		assertThat(list).hasSize(4);

		// In this case, the role name is filtered in one sub-query, and only users who do not have a role named ROLE1 are returned
		qf = queryFilterProcessor.newQueryFilter("role=ne:ROLE1", QFParamType.RHS_COLON);
		list = repository.findAll(qf);
		assertThat(list).hasSize(2).containsExactlyInAnyOrder(USER2, USER4);

	}

	@Test
	@DisplayName("2. Test user has role")
	@Order(2)
	void testHasRole() {

		// Subquery and normal query must return exactly the same users

		QueryFilter<UserModel> qf = queryFilterProcessor.newQueryFilter("roleNotSub=eq:ROLE1", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		List<UserModel> list = repository.findAll(qf);
		assertThat(list).hasSize(2).containsExactlyInAnyOrder(USER1, USER3);

		qf = queryFilterProcessor.newQueryFilter("role=eq:ROLE1", QFParamType.RHS_COLON);
		list = repository.findAll(qf);
		assertThat(list).hasSize(2).containsExactlyInAnyOrder(USER1, USER3);
	}

	@Test
	@DisplayName("10. Test by clear BBDD")
	@Order(10)
	void clearBBDD() {
		repository.deleteAll();
		assertThat(repository.findAll()).isEmpty();

		roleRepository.deleteAll();
		assertThat(roleRepository.findAll()).isEmpty();
	}

}
