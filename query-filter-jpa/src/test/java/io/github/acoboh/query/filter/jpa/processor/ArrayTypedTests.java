package io.github.acoboh.query.filter.jpa.processor;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

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

import io.github.acoboh.query.filter.jpa.domain.ArrayTypeFilterDef;
import io.github.acoboh.query.filter.jpa.exceptions.QueryFilterException;
import io.github.acoboh.query.filter.jpa.model.PostBlog;
import io.github.acoboh.query.filter.jpa.repositories.PostBlogRepository;
import io.github.acoboh.query.filter.jpa.spring.SpringIntegrationTest;

/**
 * Array type tests
 * 
 * @author Adrián Cobo
 *
 */
@SpringJUnitWebConfig(SpringIntegrationTest.Config.class)
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ArrayTypedTests {

	private static final PostBlog POST_EXAMPLE = new PostBlog();
	private static final PostBlog POST_EXAMPLE_2 = new PostBlog();

	static {
		POST_EXAMPLE.setUuid(UUID.randomUUID());
		POST_EXAMPLE.setAuthor("author");
		POST_EXAMPLE.setText("text");
		POST_EXAMPLE.setAvgNote(2.5d);
		POST_EXAMPLE.setLikes(100);
		POST_EXAMPLE.setCreateDate(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)); // Truncated to avoid rounding issues with Java > 8 and BBDD
		POST_EXAMPLE.setLastTimestamp(Timestamp.valueOf(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS))); // Truncated to avoid rounding issues with Java > 8 and BBDD
		POST_EXAMPLE.setPublished(true);
		POST_EXAMPLE.setPostType(PostBlog.PostType.TEXT);
		POST_EXAMPLE.setTags(new String[] { "TAG1", "TAG2", "TAG3", "TAG4" });

		POST_EXAMPLE_2.setUuid(UUID.randomUUID());
		POST_EXAMPLE_2.setAuthor("Author 2");
		POST_EXAMPLE_2.setText("text");
		POST_EXAMPLE_2.setAvgNote(0.5d);
		POST_EXAMPLE_2.setLikes(0);
		POST_EXAMPLE_2.setCreateDate(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)); // Truncated to avoid rounding issues with Java > 8 and BBDD
		POST_EXAMPLE_2.setLastTimestamp(Timestamp.valueOf(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS))); // Truncated to avoid rounding issues with Java > 8 and BBDD
		POST_EXAMPLE_2.setPublished(false);
		POST_EXAMPLE_2.setPostType(PostBlog.PostType.VIDEO);
		POST_EXAMPLE_2.setTags(new String[] { "TAG1", "TAG4", "TAG5" });
	}

	@Autowired
	private QFProcessor<ArrayTypeFilterDef, PostBlog> queryFilterProcessor;

	@Autowired
	private PostBlogRepository repository;

	@Test
	@DisplayName("0. Setup")
	@Order(0)
	void setup() {

		assertThat(queryFilterProcessor).isNotNull();
		assertThat(repository).isNotNull();

		assertThat(repository.findAll()).isEmpty();

		repository.saveAndFlush(POST_EXAMPLE);
		repository.saveAndFlush(POST_EXAMPLE_2);

		assertThat(repository.findAll()).hasSize(2).containsExactlyInAnyOrder(POST_EXAMPLE, POST_EXAMPLE_2);
	}

	@Test
	@DisplayName("1. Test Overlap")
	@Order(1)
	void testOverlap() throws QueryFilterException {

		QueryFilter<PostBlog> qf = queryFilterProcessor.newQueryFilter("tags=ovlp:TAG1,TAG4", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		List<PostBlog> list = repository.findAll(qf);
		assertThat(list).hasSize(2).containsExactlyInAnyOrder(POST_EXAMPLE, POST_EXAMPLE_2);

		qf = queryFilterProcessor.newQueryFilter("tags=ovlp:TAG3", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		list = repository.findAll(qf);
		assertThat(list).hasSize(1).containsExactlyInAnyOrder(POST_EXAMPLE);

		qf = queryFilterProcessor.newQueryFilter("tags=ovlp:TAG5", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		list = repository.findAll(qf);
		assertThat(list).hasSize(1).containsExactlyInAnyOrder(POST_EXAMPLE_2);

	}

	@Test
	@DisplayName("2. Test IN")
	@Order(2)
	void testIN() {
		QueryFilter<PostBlog> qf = queryFilterProcessor.newQueryFilter("tags=in:TAG1,TAG3", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		List<PostBlog> list = repository.findAll(qf);
		assertThat(list).hasSize(1).containsExactlyInAnyOrder(POST_EXAMPLE);

		qf = queryFilterProcessor.newQueryFilter("tags=in:TAG1,TAG4", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		list = repository.findAll(qf);
		assertThat(list).hasSize(2).containsExactlyInAnyOrder(POST_EXAMPLE, POST_EXAMPLE_2);

		qf = queryFilterProcessor.newQueryFilter("tags=in:TAG5", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		list = repository.findAll(qf);
		assertThat(list).hasSize(1).containsExactlyInAnyOrder(POST_EXAMPLE_2);

	}

	@Test
	@DisplayName("3. Test EQ")
	@Order(3)
	void testEqual() {

		QueryFilter<PostBlog> qf = queryFilterProcessor.newQueryFilter("tags=eq:TAG1,TAG2,TAG3,TAG4",
				QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		List<PostBlog> list = repository.findAll(qf);
		assertThat(list).hasSize(1).containsExactlyInAnyOrder(POST_EXAMPLE);

		qf = queryFilterProcessor.newQueryFilter("tags=eq:TAG1,TAG4,TAG5", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		list = repository.findAll(qf);
		assertThat(list).hasSize(1).containsExactlyInAnyOrder(POST_EXAMPLE_2);

		qf = queryFilterProcessor.newQueryFilter("tags=eq:TAG5,TAG4,TAG1", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		list = repository.findAll(qf);
		assertThat(list).isEmpty();

	}

	@Test
	@DisplayName("4. Test NOT EQ")
	@Order(4)
	void testNotEqual() {

		QueryFilter<PostBlog> qf = queryFilterProcessor.newQueryFilter("tags=ne:TAG1,TAG2,TAG3,TAG4",
				QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		List<PostBlog> list = repository.findAll(qf);
		assertThat(list).hasSize(1).containsExactlyInAnyOrder(POST_EXAMPLE_2);

		qf = queryFilterProcessor.newQueryFilter("tags=ne:TAG1,TAG4,TAG5", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		list = repository.findAll(qf);
		assertThat(list).hasSize(1).containsExactlyInAnyOrder(POST_EXAMPLE);

		qf = queryFilterProcessor.newQueryFilter("tags=ne:TAG5,TAG4,TAG1", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		list = repository.findAll(qf);
		assertThat(list).hasSize(2).containsExactlyInAnyOrder(POST_EXAMPLE, POST_EXAMPLE_2);

	}

	@Test
	@DisplayName("5. Test is contained by")
	@Order(5)
	void testIsContained() {
		QueryFilter<PostBlog> qf = queryFilterProcessor.newQueryFilter("tags=containedBy:TAG1,TAG2,TAG3,TAG4,TAG5",
				QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		List<PostBlog> list = repository.findAll(qf);
		assertThat(list).hasSize(2).containsExactlyInAnyOrder(POST_EXAMPLE, POST_EXAMPLE_2);

		qf = queryFilterProcessor.newQueryFilter("tags=containedBy:TAG1,TAG4,TAG5", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		list = repository.findAll(qf);
		assertThat(list).hasSize(1).containsExactlyInAnyOrder(POST_EXAMPLE_2);

		qf = queryFilterProcessor.newQueryFilter("tags=containedBy:TAG4,TAG5,TAG1", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		list = repository.findAll(qf);
		assertThat(list).hasSize(1).containsExactlyInAnyOrder(POST_EXAMPLE_2);

		qf = queryFilterProcessor.newQueryFilter("tags=containedBy:TAG4,TAG1,TAG2,TAG6,TAG3", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		list = repository.findAll(qf);
		assertThat(list).hasSize(1).containsExactlyInAnyOrder(POST_EXAMPLE);

	}

	@Test
	@DisplayName("6. Test greater than")
	@Order(6)
	void testGreaterThan() {
		QueryFilter<PostBlog> qf = queryFilterProcessor.newQueryFilter("tags=gt:TAG1,TAG4", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		List<PostBlog> list = repository.findAll(qf);
		assertThat(list).hasSize(1).containsExactlyInAnyOrder(POST_EXAMPLE_2);

		qf = queryFilterProcessor.newQueryFilter("tags=gt:TAG1", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		list = repository.findAll(qf);
		assertThat(list).hasSize(2).containsExactlyInAnyOrder(POST_EXAMPLE, POST_EXAMPLE_2);

	}

	@Test
	@DisplayName("7. Test less than")
	@Order(7)
	void testLessThan() {
		QueryFilter<PostBlog> qf = queryFilterProcessor.newQueryFilter("tags=lt:TAG2,TAG3", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		List<PostBlog> list = repository.findAll(qf);
		assertThat(list).hasSize(2).containsExactlyInAnyOrder(POST_EXAMPLE, POST_EXAMPLE_2);

		qf = queryFilterProcessor.newQueryFilter("tags=lt:TAG1", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		list = repository.findAll(qf);
		assertThat(list).isEmpty();

	}

	@Test
	@DisplayName("8. Test greater equal than")
	@Order(8)
	void testGreaterEqualThan() {
		QueryFilter<PostBlog> qf = queryFilterProcessor.newQueryFilter("tags=gte:TAG1,TAG4,TAG5",
				QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		List<PostBlog> list = repository.findAll(qf);
		assertThat(list).hasSize(1).containsExactlyInAnyOrder(POST_EXAMPLE_2);

		qf = queryFilterProcessor.newQueryFilter("tags=gte:TAG1,TAG4", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		list = repository.findAll(qf);
		assertThat(list).hasSize(1).containsExactlyInAnyOrder(POST_EXAMPLE_2);

	}

	@Test
	@DisplayName("9. Test less equal than")
	@Order(9)
	void testLessEqualThan() {
		QueryFilter<PostBlog> qf = queryFilterProcessor.newQueryFilter("tags=lte:TAG1,TAG4,TAG5",
				QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		List<PostBlog> list = repository.findAll(qf);
		assertThat(list).hasSize(2).containsExactlyInAnyOrder(POST_EXAMPLE, POST_EXAMPLE_2);

		qf = queryFilterProcessor.newQueryFilter("tags=lte:TAG1,TAG4", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		list = repository.findAll(qf);
		assertThat(list).hasSize(1).containsExactlyInAnyOrder(POST_EXAMPLE);

	}

	@Test
	@DisplayName("10. Clear BBDD")
	@Order(10)
	void clearBBDD() {
		repository.deleteAll();
		assertThat(repository.findAll()).isEmpty();
	}

}
