package io.github.acoboh.query.filter.jpa.processor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import io.github.acoboh.query.filter.jpa.domain.JsonFilterDef;
import io.github.acoboh.query.filter.jpa.model.jsondata.ModelJson;
import io.github.acoboh.query.filter.jpa.repositories.ModelJsonRepository;
import io.github.acoboh.query.filter.jpa.spring.SpringIntegrationTest;

/**
 * JSON tests
 * 
 * @author Adrián Cobo
 *
 */
@SpringJUnitWebConfig(SpringIntegrationTest.Config.class)
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JsonTypeTest {

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

	@Test
	@DisplayName("0. Setup")
	@Order(0)
	void setup() {

		assertThat(queryFilterProcessor).isNotNull();
		assertThat(repository).isNotNull();

		assertThat(repository.findAll()).isEmpty();

		repository.saveAndFlush(MODEL1);
		repository.saveAndFlush(MODEL2);

		assertThat(repository.findAll()).hasSize(2).containsExactlyInAnyOrder(MODEL1, MODEL2);

	}

	@Test
	@DisplayName("1. Test find by json data")
	@Order(1)
	void testFindByJsonData() {

		// Find common data
		QueryFilter<ModelJson> qf = queryFilterProcessor.newQueryFilter("jsonb=eq:{'bkey1':'value1'}",
				QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

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
	@DisplayName("2. Test by clear BBDD")
	@Order(2)
	void clearBBDD() {
		repository.deleteAll();
		assertThat(repository.findAll()).isEmpty();
	}

}
