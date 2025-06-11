package io.github.acoboh.query.filter.jpa.processor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.context.web.WebAppConfiguration;

import io.github.acoboh.query.filter.jpa.annotations.QFDefinitionClass;
import io.github.acoboh.query.filter.jpa.annotations.QFElement;
import io.github.acoboh.query.filter.jpa.annotations.QFRequired;
import io.github.acoboh.query.filter.jpa.annotations.QFSortable;
import io.github.acoboh.query.filter.jpa.exceptions.QFRequiredException;
import io.github.acoboh.query.filter.jpa.exceptions.QueryFilterException;
import io.github.acoboh.query.filter.jpa.exceptions.definition.QueryFilterDefinitionException;
import io.github.acoboh.query.filter.jpa.model.PostBlog;
import io.github.acoboh.query.filter.jpa.operations.QFOperationEnum;
import io.github.acoboh.query.filter.jpa.repositories.PostBlogRepository;
import io.github.acoboh.query.filter.jpa.spring.SpringIntegrationTestBase;

/**
 * Tests for required fields in query filters.
 * 
 * @author Adrián Cobo
 */
@SpringJUnitWebConfig(SpringIntegrationTestBase.Config.class)
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RequiredTest {

	private static final PostBlog POST_EXAMPLE = new PostBlog();

	static {
		POST_EXAMPLE.setUuid(UUID.randomUUID());
		POST_EXAMPLE.setAuthor("author");
		POST_EXAMPLE.setText("text");
		POST_EXAMPLE.setAvgNote(2.5d);
		POST_EXAMPLE.setLikes(100);
		// Truncated to avoid rounding issues with Java > 8 and BBDD
		POST_EXAMPLE.setCreateDate(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS));
		POST_EXAMPLE.setLastTimestamp(Timestamp.valueOf(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)));

		POST_EXAMPLE.setPublished(true);
		POST_EXAMPLE.setPostType(PostBlog.PostType.TEXT);
	}

	private static QFProcessor<RequiredAuthorDef, PostBlog> processorAuthor;
	private static QFProcessor<RequiredSortDef, PostBlog> processorSort;
	private static QFProcessor<RequiredSortDef2, PostBlog> processorSort2;
	private static QFProcessor<RequiredAuthorDef2, PostBlog> processorAuthor2;
	private static QFProcessor<RequiredAuthorDef3, PostBlog> processorAuthor3;

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private PostBlogRepository repository;

	// Initialize the processors
	@Test
	@DisplayName("0. Setup Processors")
	@Order(Ordered.HIGHEST_PRECEDENCE)
	void initProcessors() throws QueryFilterDefinitionException {
		processorAuthor = new QFProcessor<>(RequiredAuthorDef.class, PostBlog.class, applicationContext);
		processorSort = new QFProcessor<>(RequiredSortDef.class, PostBlog.class, applicationContext);
		processorSort2 = new QFProcessor<>(RequiredSortDef2.class, PostBlog.class, applicationContext);
		processorAuthor2 = new QFProcessor<>(RequiredAuthorDef2.class, PostBlog.class, applicationContext);
		processorAuthor3 = new QFProcessor<>(RequiredAuthorDef3.class, PostBlog.class, applicationContext);
		assertThat(processorAuthor).isNotNull();
		assertThat(processorSort).isNotNull();
		assertThat(processorSort2).isNotNull();
		assertThat(processorAuthor2).isNotNull();
		assertThat(processorAuthor3).isNotNull();
	}

	// Set up the database with a sample post
	@Test
	@DisplayName("1. Setup Database")
	@Order(1)
	void setupDatabase() {
		assertThat(repository).isNotNull();
		repository.save(POST_EXAMPLE);
		assertThat(repository.findAll()).hasSize(1);
	}

	@Test
	@DisplayName("2. Test with required author throw exception")
	@Order(2)
	void testRequiredAuthor() {

		var ex = assertThrows(QFRequiredException.class,
				() -> processorAuthor.newQueryFilter(null, QFParamType.RHS_COLON));
		assertThat(ex.getMessage()).contains("author");

		ex = assertThrows(QFRequiredException.class,
				() -> processorAuthor.newQueryFilter("likes=gt:0", QFParamType.RHS_COLON));
		assertThat(ex.getMessage()).contains("author");

	}

	@Test
	@DisplayName("3. Test with required author and valid value")
	@Order(3)
	void testRequiredAuthorValid() throws QueryFilterException {
		var qf = processorAuthor.newQueryFilter("author=eq:author", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		var list = repository.findAll(qf);
		assertThat(list).hasSize(1).containsExactly(POST_EXAMPLE);
	}

	@Test
	@DisplayName("4. Test with required likes on sortable required on string but not on sort or execution")
	@Order(4)
	void testRequiredLikesOnSortable() {

		var qf = processorSort.newQueryFilter("sort=+likes", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		var list = repository.findAll(qf);
		assertThat(list).hasSize(1).containsExactly(POST_EXAMPLE);

		var ex = assertThrows(QFRequiredException.class,
				() -> processorSort.newQueryFilter("author=eq:author", QFParamType.RHS_COLON));
		assertThat(ex.getMessage()).contains("likes");

		qf = processorSort.newQueryFilter("author=eq:author&sort=+likes", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		// Clear sort to test without sorting
		qf.clearSort();

		// Now it should not throw an exception
		list = repository.findAll(qf);
		assertThat(list).hasSize(1).containsExactly(POST_EXAMPLE);

	}

	@Test
	@DisplayName("5. Test with required likes on sortable and valid value")
	@Order(5)
	void testRequiredLikesOnSortableValid() throws QueryFilterException {
		var qf = processorSort2.newQueryFilter("author=eq:author&sort=+likes", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		var list = repository.findAll(qf);
		assertThat(list).hasSize(1).containsExactly(POST_EXAMPLE);

		var ex = assertThrows(QFRequiredException.class,
				() -> processorSort2.newQueryFilter(null, QFParamType.RHS_COLON));
		assertThat(ex.getMessage()).contains("likes");

		// Clear sort to test without sorting
		qf.clearSort();

		// Now it should throw an exception
		final var finalQf = qf;
		ex = assertThrows(QFRequiredException.class, () -> repository.findAll(finalQf));
		assertThat(ex.getMessage()).contains("likes");
	}

	@Test
	@DisplayName("6. Required author not on string but on execution")
	@Order(6)
	void testRequiredAuthorNotOnStringButOnExecution() {

		var qf = processorAuthor2.newQueryFilter("author=eq:author", QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		var list = repository.findAll(qf);
		assertThat(list).hasSize(1).containsExactly(POST_EXAMPLE);

		qf = processorAuthor2.newQueryFilter(null, QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		final var finalQf = qf;
		var ex = assertThrows(QFRequiredException.class, () -> repository.findAll(finalQf));
		assertThat(ex.getMessage()).contains("author");

		qf.addNewField("author", QFOperationEnum.EQUAL, "author");
		list = repository.findAll(qf);
		assertThat(list).hasSize(1).containsExactly(POST_EXAMPLE);
	}

	@Test
	@DisplayName("11. Test by clear BBDD")
	@Order(Ordered.LOWEST_PRECEDENCE)
	void clearBBDD() {
		repository.deleteAll();
		assertThat(repository.findAll()).isEmpty();
	}

	// Filter definitions
	@QFDefinitionClass(PostBlog.class)
	public static class RequiredAuthorDef {

		@QFElement("author")
		@QFRequired
		private String author;

		@QFElement("likes")
		private int likes;
	}

	@QFDefinitionClass(PostBlog.class)
	public static class RequiredSortDef {

		@QFElement("author")
		private String author;

		@QFSortable("likes")
		@QFRequired
		private int likes;
	}

	@QFDefinitionClass(PostBlog.class)
	public static class RequiredSortDef2 {

		@QFElement("author")
		private String author;

		@QFSortable("likes")
		@QFRequired(onSort = true)
		private int likes;
	}

	@QFDefinitionClass(PostBlog.class)
	public static class RequiredAuthorDef2 {
		@QFElement("author")
		@QFRequired(onStringFilter = false)
		private String author;
	}

	@QFDefinitionClass(PostBlog.class)
	public static class RequiredAuthorDef3 {
		@QFElement("author")
		@QFRequired(onStringFilter = false, onSort = true)
		private String author;
	}

	@QFDefinitionClass(PostBlog.class)
	public static class RequiredAuthorDef4 {
		@QFElement("author")
		@QFRequired(onExecution = false)
		private String author;
	}
}
