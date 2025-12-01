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
import org.springframework.core.Ordered;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.context.web.WebAppConfiguration;

import io.github.acoboh.query.filter.jpa.domain.FilterBlogDefaultValuesDef;
import io.github.acoboh.query.filter.jpa.domain.FilterBlogOnPresentDef;
import io.github.acoboh.query.filter.jpa.model.PostBlog;
import io.github.acoboh.query.filter.jpa.operations.QFOperationEnum;
import io.github.acoboh.query.filter.jpa.repositories.PostBlogRepository;
import io.github.acoboh.query.filter.jpa.spring.SpringIntegrationTestBase;

/**
 * Default values tests
 */
@SpringJUnitWebConfig(SpringIntegrationTestBase.Config.class)
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DefaultValuesTests {

	private static final PostBlog POST_EXAMPLE = new PostBlog();
	private static final PostBlog POST_EXAMPLE_2 = new PostBlog();

	static {
		POST_EXAMPLE.setUuid(UUID.randomUUID());
		POST_EXAMPLE.setAuthor("Author 1");
		POST_EXAMPLE.setText("Text");
		POST_EXAMPLE.setAvgNote(2.5d);
		POST_EXAMPLE.setLikes(0);
		// Truncated to avoid rounding issues with Java > 8 and BBDD
		POST_EXAMPLE.setCreateDate(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS));
		POST_EXAMPLE.setLastTimestamp(Timestamp.valueOf(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)));
		POST_EXAMPLE.setPublished(true);
		POST_EXAMPLE.setPostType(PostBlog.PostType.TEXT);

		POST_EXAMPLE_2.setUuid(UUID.randomUUID());
		POST_EXAMPLE_2.setAuthor("Author 2");
		POST_EXAMPLE_2.setText("Text 2");
		POST_EXAMPLE_2.setAvgNote(0.5d);
		POST_EXAMPLE_2.setLikes(100);
		// Truncated to avoid rounding issues with Java > 8 and BBDD
		POST_EXAMPLE_2.setCreateDate(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS));
		POST_EXAMPLE_2.setLastTimestamp(Timestamp.valueOf(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)));
		POST_EXAMPLE_2.setPublished(false);
		POST_EXAMPLE_2.setPostType(PostBlog.PostType.VIDEO);
	}

	@Autowired
	private QFProcessor<FilterBlogDefaultValuesDef, PostBlog> queryFilterProcessor;

	@Autowired
	private QFProcessor<FilterBlogOnPresentDef, PostBlog> queryFilterOnPresentProcessor;

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

		assertThat(repository.findAll()).hasSize(2);
	}

	@Test
	@DisplayName("1. Default values filtering")
	@Order(1)
	void testDefaultValueFilter() {

		QueryFilter<PostBlog> qf = queryFilterProcessor.newQueryFilter(null, QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		assertThat(qf.isFiltering("author")).isTrue();
		assertThat(qf.getActualValue("author")).containsExactly("Author 1");

		var found = repository.findAll(qf);

		assertThat(found).containsExactly(POST_EXAMPLE);

	}

	@Test
	@DisplayName("2. Default values filtering remove")
	@Order(2)
	void testDefaultValuesRemoved() {
		QueryFilter<PostBlog> qf = queryFilterProcessor.newQueryFilter(null, QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		assertThat(qf.isFiltering("author")).isTrue();
		assertThat(qf.getActualValue("author")).containsExactly("Author 1");

		qf.deleteField("author");
		assertThat(qf.isFiltering("author")).isFalse();

		var found = repository.findAll(qf);

		assertThat(found).containsExactlyInAnyOrder(POST_EXAMPLE, POST_EXAMPLE_2);

	}

	@Test
	@DisplayName("4. Default values filtering override on definition")
	@Order(4)
	void testDefaultValuesOverrideOnDefinition() {
		QueryFilter<PostBlog> qf = queryFilterProcessor.newQueryFilter("author=eq:Author 2", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		assertThat(qf.isFiltering("author")).isTrue();
		assertThat(qf.getActualValue("author")).containsExactly("Author 2");

		var found = repository.findAll(qf);

		assertThat(found).containsExactly(POST_EXAMPLE_2);
	}

	@Test
	@DisplayName("5. Default values filtering override manually")
	@Order(5)
	void testDefaultValuesOverrideManually() {
		QueryFilter<PostBlog> qf = queryFilterProcessor.newQueryFilter("", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		assertThat(qf.isFiltering("author")).isTrue();
		assertThat(qf.getActualValue("author")).containsExactly("Author 1");

		qf.addNewField("author", QFOperationEnum.EQUAL, "Author 2");

		assertThat(qf.isFiltering("author")).isTrue();
		assertThat(qf.getActualValue("author")).containsExactly("Author 2");

		var found = repository.findAll(qf);

		assertThat(found).containsExactly(POST_EXAMPLE_2);
	}

	@Test
	@DisplayName("6. Test on filter present default values")
	@Order(6)
	void testOnFilterPresentDefaultValues() {
		QueryFilter<PostBlog> qf = queryFilterOnPresentProcessor.newQueryFilter(null, QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		assertThat(qf.isFilteringAny("author", "likes")).isFalse();

		var found = repository.findAll(qf);

		assertThat(found).containsExactlyInAnyOrder(POST_EXAMPLE, POST_EXAMPLE_2);
	}

	@Test
	@DisplayName("7. Test on filter present default values with author")
	@Order(7)
	void testOnFilterPresentDefaultValuesWithAuthor() {
		QueryFilter<PostBlog> qf = queryFilterOnPresentProcessor.newQueryFilter("author=eq:Author 1",
				QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		assertThat(qf.isFilteringAny("author")).isTrue();
		assertThat(qf.isFiltering("likes")).isTrue();
		assertThat(qf.getActualValue("author")).containsExactly("Author 1");
		assertThat(qf.getActualValue("likes")).containsExactly("10");

		var found = repository.findAll(qf);

		assertThat(found).isEmpty();

		qf.overrideField("author", QFOperationEnum.EQUAL, "Author 2");
		assertThat(qf.isFilteringAny("author")).isTrue();
		assertThat(qf.isFiltering("likes")).isTrue();

		assertThat(qf.getActualValue("author")).containsExactly("Author 2");
		assertThat(qf.getActualValue("likes")).containsExactly("10");

		found = repository.findAll(qf);
		assertThat(found).containsExactly(POST_EXAMPLE_2);
	}

	@Test
	@DisplayName("8. Test on filter present default values with author and text")
	@Order(8)
	void testOnFilterPresentDefaultValuesWithAuthorAndText() {
		QueryFilter<PostBlog> qf = queryFilterOnPresentProcessor.newQueryFilter("author=eq:Author 2&text=like:Text",
				QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		assertThat(qf.isFiltering("author")).isTrue();
		assertThat(qf.isFiltering("text")).isTrue();
		assertThat(qf.isFiltering("likes")).isTrue();
		assertThat(qf.getActualValue("author")).containsExactly("Author 2");
		assertThat(qf.getActualValue("text")).containsExactly("Text");
		assertThat(qf.getActualValue("likes")).containsExactly("10");

		var fieldValues = qf.getAllFieldValues();
		assertThat(fieldValues).hasSize(3).containsExactlyInAnyOrder(
				// author=eq:Author 2
				new QFFieldInfo("author", "eq", List.of("Author 2")),
				// text=like:Text
				new QFFieldInfo("text", "like", List.of("Text")),
				// likes=gt:10 (default value)
				new QFFieldInfo("likes", "gt", List.of("10")));

		var found = repository.findAll(qf);

		assertThat(found).contains(POST_EXAMPLE_2);

		qf.deleteField("text");

		fieldValues = qf.getAllFieldValues();
		assertThat(fieldValues).hasSize(2).containsExactlyInAnyOrder(
				// author=eq:Author 2
				new QFFieldInfo("author", "eq", List.of("Author 2")),
				// likes=gt:10 (default value)
				new QFFieldInfo("likes", "gt", List.of("10")));

		assertThat(qf.isFiltering("author")).isTrue();
		assertThat(qf.isFiltering("text")).isFalse();
		assertThat(qf.isFiltering("likes")).isTrue();

		found = repository.findAll(qf);
		assertThat(found).containsExactly(POST_EXAMPLE_2);

		qf.deleteField("author");

		fieldValues = qf.getAllFieldValues();
		assertThat(fieldValues).isEmpty();

		assertThat(qf.isFiltering("author")).isFalse();
		assertThat(qf.isFiltering("text")).isFalse();
		assertThat(qf.isFiltering("likes")).isFalse();

		found = repository.findAll(qf);
		assertThat(found).containsExactlyInAnyOrder(POST_EXAMPLE, POST_EXAMPLE_2);

		qf.addNewField("author", QFOperationEnum.EQUAL, "Author 1");

		// Default values can be overridden
		qf.overrideField("likes", QFOperationEnum.LESS_THAN, "5");

		assertThat(qf.isFiltering("author")).isTrue();
		assertThat(qf.isFiltering("text")).isFalse();
		assertThat(qf.isFiltering("likes")).isTrue();

		fieldValues = qf.getAllFieldValues();
		assertThat(fieldValues).hasSize(2).containsExactlyInAnyOrder(
				// author=eq:Author 1
				new QFFieldInfo("author", "eq", List.of("Author 1")),
				// likes=gt:5
				new QFFieldInfo("likes", "lt", List.of("5")));

		found = repository.findAll(qf);
		assertThat(found).containsExactly(POST_EXAMPLE);
	}

	@Test
	@DisplayName("END. Test by clear BBDD")
	@Order(Ordered.LOWEST_PRECEDENCE)
	void clearBBDD() {
		repository.deleteAll();
		assertThat(repository.findAll()).isEmpty();
	}

}
