package io.github.acoboh.query.filter.jpa.processor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.util.Pair;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.context.web.WebAppConfiguration;

import io.github.acoboh.query.filter.jpa.domain.FilterBlogSortRelationalDef;
import io.github.acoboh.query.filter.jpa.model.Comments;
import io.github.acoboh.query.filter.jpa.model.ExtraData;
import io.github.acoboh.query.filter.jpa.model.PostBlog;
import io.github.acoboh.query.filter.jpa.repositories.PostBlogRepository;
import io.github.acoboh.query.filter.jpa.spring.SpringIntegrationTestBase;

@SpringJUnitWebConfig(SpringIntegrationTestBase.Config.class)
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AutoFetchSortTest {

	private static final PostBlog POST_EXAMPLE = new PostBlog();
	private static final PostBlog POST_EXAMPLE_2 = new PostBlog();

	static {
		POST_EXAMPLE.setUuid(UUID.randomUUID());
		POST_EXAMPLE.setAuthor("Author");
		POST_EXAMPLE.setText("Text");
		POST_EXAMPLE.setAvgNote(2.5d);
		POST_EXAMPLE.setLikes(100);

		// Truncated to avoid rounding issues with Java > 8 and BBDD
		POST_EXAMPLE.setCreateDate(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS));
		POST_EXAMPLE.setLastTimestamp(Timestamp.valueOf(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)));

		POST_EXAMPLE.setPublished(true);
		POST_EXAMPLE.setPostType(PostBlog.PostType.TEXT);

		Set<Comments> commentsList = new HashSet<>();

		Comments comment = new Comments();
		comment.setId(1);
		comment.setAuthor("Author 2");
		comment.setComment("Comment 1");
		comment.setLikes(1);
		comment.setPostBlog(POST_EXAMPLE);

		Comments comment2 = new Comments();
		comment2.setId(2);
		comment2.setAuthor("Author 4");
		comment2.setComment("Comment 2");
		comment2.setLikes(2);
		comment2.setPostBlog(POST_EXAMPLE);

		commentsList.add(comment);
		commentsList.add(comment2);

		POST_EXAMPLE.setComments(commentsList);

		List<ExtraData> extraDataList = new ArrayList<>();
		extraDataList.add(new ExtraData(2, comment));
		extraDataList.add(new ExtraData(4, comment));

		comment.setExtraData(extraDataList);

		List<ExtraData> extraDataList2 = new ArrayList<>();
		extraDataList2.add(new ExtraData(6, comment2));
		extraDataList2.add(new ExtraData(8, comment2));

		comment2.setExtraData(extraDataList2);

		POST_EXAMPLE_2.setUuid(UUID.randomUUID());
		POST_EXAMPLE_2.setAuthor("Author 2");
		POST_EXAMPLE_2.setText("Text 2");
		POST_EXAMPLE_2.setAvgNote(0.5d);
		POST_EXAMPLE_2.setLikes(0);

		// Truncated to avoid rounding issues with Java > 8 and BBDD
		POST_EXAMPLE_2.setCreateDate(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS));
		POST_EXAMPLE_2.setLastTimestamp(Timestamp.valueOf(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)));

		POST_EXAMPLE_2.setPublished(false);
		POST_EXAMPLE_2.setPostType(PostBlog.PostType.VIDEO);

		Set<Comments> commentsList2 = new HashSet<>();

		Comments comment3 = new Comments();
		comment3.setId(3);
		comment3.setAuthor("Author 1");
		comment3.setComment("Comment 3");
		comment3.setLikes(3);
		comment3.setPostBlog(POST_EXAMPLE_2);

		Comments comment4 = new Comments();
		comment4.setId(4);
		comment4.setAuthor(null);
		comment4.setComment("Comment 4");
		comment4.setLikes(4);
		comment4.setPostBlog(POST_EXAMPLE_2);

		commentsList2.add(comment3);
		commentsList2.add(comment4);

		POST_EXAMPLE_2.setComments(commentsList2);

		List<ExtraData> extraDataList3 = new ArrayList<>();
		extraDataList3.add(new ExtraData(5, comment3));
		extraDataList3.add(new ExtraData(3, comment3));

		comment3.setExtraData(extraDataList3);

		List<ExtraData> extraDataList4 = new ArrayList<>();
		extraDataList4.add(new ExtraData(12, comment4));
		extraDataList4.add(new ExtraData(15, comment4));

		comment4.setExtraData(extraDataList4);
	}

	@Autowired
	private QFProcessor<FilterBlogSortRelationalDef, PostBlog> queryFilterProcessor;

	@Autowired
	private PostBlogRepository repository;

	@Test
	@DisplayName("0. Setup")
	@Order(Ordered.HIGHEST_PRECEDENCE)
	void setup() {

		assertThat(queryFilterProcessor).isNotNull();
		assertThat(repository).isNotNull();

		assertThat(repository.findAll()).isEmpty();

		repository.saveAndFlush(POST_EXAMPLE);
		repository.saveAndFlush(POST_EXAMPLE_2);

		assertThat(repository.findAll()).hasSize(2).containsExactlyInAnyOrder(POST_EXAMPLE, POST_EXAMPLE_2);

	}

	@Test
	@DisplayName("1. Test sort by only sort")
	@Order(1)
	void testSortBase() {

		QueryFilter<PostBlog> qf = queryFilterProcessor.newQueryFilter("sort=+commentAuthorSort",
				QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		assertThat(qf.isSorted()).isTrue();

		assertThat(qf.isSortedBy("commentAuthorSort")).isTrue();

		assertThat(qf.getSortFields()).containsExactly(Pair.of("commentAuthorSort", Direction.ASC));

		assertThat(qf.getSortFieldWithFullPath()).containsExactly(Pair.of("comments.author", Direction.ASC));

		List<PostBlog> found = repository.findAll(qf);

		assertThat(found).hasSize(2).containsExactly(POST_EXAMPLE_2, POST_EXAMPLE);

	}

	@Test
	@DisplayName("2. Test sort by element sort")
	@Order(2)
	void testSortElement() {

		QueryFilter<PostBlog> qf = queryFilterProcessor.newQueryFilter("sort=-commentAuthorElement",
				QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		assertThat(qf.isSorted()).isTrue();

		assertThat(qf.isSortedBy("commentAuthorElement")).isTrue();

		assertThat(qf.getSortFields()).containsExactly(Pair.of("commentAuthorElement", Direction.DESC));

		assertThat(qf.getSortFieldWithFullPath()).containsExactly(Pair.of("comments.author", Direction.DESC));

		List<PostBlog> found = repository.findAll(qf);

		assertThat(found).hasSize(2).containsExactly(POST_EXAMPLE_2, POST_EXAMPLE);

	}

	@Test
	@DisplayName("3. Test sort error without autofetch on sort")
	@Order(3)
	void testSortErrorSort() {

		QueryFilter<PostBlog> qf = queryFilterProcessor.newQueryFilter("sort=-commentAuthorSortError",
				QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		assertThat(qf.isSorted()).isTrue();

		assertThat(qf.isSortedBy("commentAuthorSortError")).isTrue();

		assertThat(qf.getSortFields()).containsExactly(Pair.of("commentAuthorSortError", Direction.DESC));

		assertThat(qf.getSortFieldWithFullPath()).containsExactly(Pair.of("comments.author", Direction.DESC));

		InvalidDataAccessResourceUsageException ex = assertThrows(InvalidDataAccessResourceUsageException.class,
				() -> repository.findAll(qf));

		assertThat(ex.getCause().getCause().getMessage())
				.startsWith("ERROR: for SELECT DISTINCT, ORDER BY expressions must appear in select list");

	}

	@Test
	@DisplayName("4. Test sort error without autofetch on element")
	@Order(4)
	void testSortErrorElement() {

		QueryFilter<PostBlog> qf = queryFilterProcessor.newQueryFilter("sort=-commentAuthorElementError",
				QFParamType.RHS_COLON);
		assertThat(qf).isNotNull();

		assertThat(qf.isSorted()).isTrue();

		assertThat(qf.isSortedBy("commentAuthorElementError")).isTrue();

		assertThat(qf.getSortFields()).containsExactly(Pair.of("commentAuthorElementError", Direction.DESC));

		assertThat(qf.getSortFieldWithFullPath()).containsExactly(Pair.of("comments.author", Direction.DESC));

		InvalidDataAccessResourceUsageException ex = assertThrows(InvalidDataAccessResourceUsageException.class,
				() -> repository.findAll(qf));

		assertThat(ex.getCause().getCause().getMessage())
				.startsWith("ERROR: for SELECT DISTINCT, ORDER BY expressions must appear in select list");

	}

	@Test
	@DisplayName("5. Test 3 level joins auto-fetch")
	@Order(5)
	void testLevelJoinsAutoFetch() {
		QueryFilter<PostBlog> qf = queryFilterProcessor.newQueryFilter("sort=+extraDataSort", QFParamType.RHS_COLON);

		assertThat(qf).isNotNull();

		assertThat(qf.isSorted()).isTrue();

		assertThat(qf.isSortedBy("extraDataSort")).isTrue();

		assertThat(qf.getSortFields()).containsExactly(Pair.of("extraDataSort", Direction.ASC));

		assertThat(qf.getSortFieldWithFullPath()).containsExactly(Pair.of("comments.extraData.id", Direction.ASC));

		List<PostBlog> found = repository.findAll(qf);

		assertThat(found).hasSize(2).containsExactly(POST_EXAMPLE, POST_EXAMPLE_2);
	}

	@Test
	@DisplayName("6. Test paginated sort")
	@Order(6)
	void testPaginatedSort() {
		QueryFilter<PostBlog> qf = queryFilterProcessor.newQueryFilter("sort=+extraDataSort", QFParamType.RHS_COLON);

		assertThat(qf).isNotNull();

		assertThat(qf.isSorted()).isTrue();

		assertThat(qf.isSortedBy("extraDataSort")).isTrue();

		assertThat(qf.getSortFields()).containsExactly(Pair.of("extraDataSort", Direction.ASC));

		assertThat(qf.getSortFieldWithFullPath()).containsExactly(Pair.of("comments.extraData.id", Direction.ASC));

		// find all
		List<PostBlog> foundAll = repository.findAll(qf);
		assertThat(foundAll).hasSize(2).containsExactly(POST_EXAMPLE, POST_EXAMPLE_2);

		Page<PostBlog> found = repository.findAll(qf, PageRequest.of(0, 1));

		assertThat(found).hasSize(1).containsExactly(POST_EXAMPLE);

		assertThat(found.getTotalElements()).isEqualTo(2);

		// Clear sort
		qf.clearSort();

		found = repository.findAll(qf, PageRequest.of(0, 1));

		assertThat(found).hasSize(1).containsAnyOf(POST_EXAMPLE, POST_EXAMPLE_2);

		assertThat(found.getTotalElements()).isEqualTo(2);

	}

	@Test
	@DisplayName("7. Test sort with fetch inner on null columns")
	@Order(7)
	void testSortWithFetchInnerOnNull() {

		QueryFilter<PostBlog> qf = queryFilterProcessor.newQueryFilter("sort=+commentAuthorElement",
				QFParamType.RHS_COLON);

		assertThat(qf).isNotNull();

		assertThat(qf.isSorted()).isTrue();

		assertThat(qf.isSortedBy("commentAuthorElement")).isTrue();

		assertThat(qf.getSortFields()).containsExactly(Pair.of("commentAuthorElement", Direction.ASC));

		assertThat(qf.getSortFieldWithFullPath()).containsExactly(Pair.of("comments.author", Direction.ASC));

		var countQuery = repository.count(qf);
		assertThat(countQuery).isEqualTo(2);

		var page0 = repository.findAll(qf, PageRequest.of(0, 1));
		assertThat(page0).hasSize(1).containsExactly(POST_EXAMPLE_2);
		assertThat(page0.getTotalElements()).isEqualTo(2);
		assertThat(page0.getTotalPages()).isEqualTo(2);
		assertThat(page0.getNumber()).isZero();
		assertThat(page0.getSize()).isEqualTo(1);

		var page1 = repository.findAll(qf, PageRequest.of(1, 1));
		assertThat(page1).hasSize(1).containsExactly(POST_EXAMPLE);
		assertThat(page1.getTotalElements()).isEqualTo(2);
		assertThat(page1.getTotalPages()).isEqualTo(2);
		assertThat(page1.getNumber()).isEqualTo(1);
		assertThat(page1.getSize()).isEqualTo(1);

	}

	@Test
	@DisplayName("END. Test by clear BBDD")
	@Order(Ordered.LOWEST_PRECEDENCE)
	void clearBBDD() {
		repository.deleteAll();
		assertThat(repository.findAll()).isEmpty();
	}

}
