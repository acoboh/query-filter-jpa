package io.github.acoboh.query.filter.jpa.processor;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

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

import io.github.acoboh.query.filter.jpa.domain.NumericEntityFilterDef;
import io.github.acoboh.query.filter.jpa.model.extended.NumericEntity;
import io.github.acoboh.query.filter.jpa.repositories.NumericEntityRepository;
import io.github.acoboh.query.filter.jpa.spring.SpringIntegrationTestBase;

@SpringJUnitWebConfig(SpringIntegrationTestBase.Config.class)
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class NumericEntityTests {

	private static final NumericEntity NUMERIC_EXAMPLE = new NumericEntity(1L, BigDecimal.valueOf(2.5574936423));

	@Autowired
	private QFProcessor<NumericEntityFilterDef, NumericEntity> queryFilterProcessor;

	@Autowired
	private NumericEntityRepository repository;

	@Test
	@DisplayName("0, Setup")
	@Order(Ordered.HIGHEST_PRECEDENCE)
	void setup() {

		assertThat(repository).isNotNull();
		assertThat(repository.count()).isZero();

		assertThat(queryFilterProcessor).isNotNull();

		repository.save(NUMERIC_EXAMPLE);

		assertThat(repository.count()).isOne();
	}

	@Test
	@DisplayName("1. Find by value gte")
	@Order(1)
	void findByValueGte() {

		var qf = queryFilterProcessor.newQueryFilter("bigDecimal=gte:0", QFParamType.RHS_COLON);

		var list = repository.findAll(qf);

		assertThat(list).hasSize(1);
		assertThat(list.get(0)).isEqualTo(NUMERIC_EXAMPLE);
	}

	@Test
	@DisplayName("END. Test by clear BBDD")
	@Order(Ordered.LOWEST_PRECEDENCE)
	void clearBBDD() {
		repository.deleteAll();
		assertThat(repository.findAll()).isEmpty();
	}

}
