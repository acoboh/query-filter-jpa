package io.github.acoboh.query.filter.jpa.processor;

import io.github.acoboh.query.filter.jpa.domain.FilterBlogDef;
import io.github.acoboh.query.filter.jpa.model.PostBlog;
import io.github.acoboh.query.filter.jpa.repositories.PostBlogRepository;
import io.github.acoboh.query.filter.jpa.spring.SpringIntegrationTestBase;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.context.web.WebAppConfiguration;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests of the escaping of LIKE wildcards in the like, nlike, starts and ends
 * operations: the user value must be matched literally, while rlike keeps the
 * raw LIKE pattern
 *
 * @author Adrián Cobo
 */
@SpringJUnitWebConfig(SpringIntegrationTestBase.Config.class)
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LikeEscapeTest {

    private static final PostBlog PCT = new PostBlog();
    private static final PostBlog PLAIN = new PostBlog();
    private static final PostBlog UNDER = new PostBlog();
    private static final PostBlog AXB = new PostBlog();
    private static final PostBlog SLASH = new PostBlog();
    private static final PostBlog AB = new PostBlog();

    static {
        PCT.setUuid(UUID.randomUUID());
        PCT.setAuthor("100% cotton");
        initCommon(PCT, "pct");

        PLAIN.setUuid(UUID.randomUUID());
        PLAIN.setAuthor("100 cotton");
        initCommon(PLAIN, "plain");

        UNDER.setUuid(UUID.randomUUID());
        UNDER.setAuthor("100_cotton");
        initCommon(UNDER, "under");

        AXB.setUuid(UUID.randomUUID());
        AXB.setAuthor("100x cotton");
        initCommon(AXB, "axb");

        SLASH.setUuid(UUID.randomUUID());
        SLASH.setAuthor("a\\b backslash");
        initCommon(SLASH, "slash");

        AB.setUuid(UUID.randomUUID());
        AB.setAuthor("ab backslash");
        initCommon(AB, "ab");
    }

    private static void initCommon(PostBlog post, String prefix) {
        post.setText(prefix);
        post.setAvgNote(1.0d);
        post.setLikes(0);

        // Truncated to avoid rounding issues with Java > 8 and BBDD
        post.setCreateDate(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS));
        post.setLastTimestamp(Timestamp.valueOf(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)));

        post.setPublished(true);
        post.setPostType(PostBlog.PostType.TEXT);
    }

    @Autowired
    private QFProcessor<FilterBlogDef, PostBlog> queryFilterProcessor;

    @Autowired
    private PostBlogRepository repository;

    @Test
    @DisplayName("0. Setup")
    @Order(0)
    void setup() {

        assertThat(queryFilterProcessor).isNotNull();
        assertThat(repository).isNotNull();

        assertThat(repository.findAll()).isEmpty();

        repository.saveAndFlush(PCT);
        repository.saveAndFlush(PLAIN);
        repository.saveAndFlush(UNDER);
        repository.saveAndFlush(AXB);
        repository.saveAndFlush(SLASH);
        repository.saveAndFlush(AB);

        assertThat(repository.findAll()).hasSize(6);
    }

    @Test
    @DisplayName("1. Like with a literal percent in the value")
    @Order(1)
    void likeLiteralPercent() {

        // The value "100%" must be matched literally: before the escaping fix the
        // pattern was %100%% and matched every row containing "100"
        List<PostBlog> found = repository
                .findAll(queryFilterProcessor.newQueryFilter("author=like:100%", QFParamType.RHS_COLON));

        assertThat(found).hasSize(1).containsExactly(PCT);

        // A plain value without special characters keeps the substring behavior
        found = repository.findAll(queryFilterProcessor.newQueryFilter("author=like:100", QFParamType.RHS_COLON));

        assertThat(found).hasSize(4).containsExactlyInAnyOrder(PCT, PLAIN, UNDER, AXB);

    }

    @Test
    @DisplayName("2. Like with a literal underscore in the value")
    @Order(2)
    void likeLiteralUnderscore() {

        // The value "100_c" must be matched literally: before the escaping fix the
        // pattern was %100_c% and the underscore also matched "100 cotton"
        var found = repository.findAll(queryFilterProcessor.newQueryFilter("author=like:100_c", QFParamType.RHS_COLON));

        assertThat(found).hasSize(1).containsExactly(UNDER);

    }

    @Test
    @DisplayName("3. Like with a literal backslash in the value")
    @Order(3)
    void likeLiteralBackslash() {

        // The value "a\b" must be matched literally: before the escaping fix the
        // pattern was %a\b% (a lone backslash means the next char literally, i.e.
        // "ab") and it matched the "ab backslash" row instead
        var found = repository.findAll(queryFilterProcessor.newQueryFilter("author=like:a\\b", QFParamType.RHS_COLON));

        assertThat(found).hasSize(1).containsExactly(SLASH);

    }

    @Test
    @DisplayName("4. Regular like keeps the raw LIKE pattern")
    @Order(4)
    void rlikeKeepsRawPattern() {

        // rlike must not escape the user value: "100%" is the raw pattern %100%%
        var found = repository.findAll(queryFilterProcessor.newQueryFilter("author=rlike:100%", QFParamType.RHS_COLON));

        assertThat(found).hasSize(4).containsExactlyInAnyOrder(PCT, PLAIN, UNDER, AXB);

        // ... and the user can still escape on his own in rlike: the raw pattern
        // %100\%% matches strings containing a literal "100%"
        found = repository.findAll(queryFilterProcessor.newQueryFilter("author=rlike:%100\\%%", QFParamType.RHS_COLON));

        assertThat(found).hasSize(1).containsExactly(PCT);

    }

    @Test
    @DisplayName("5. Starts with with special characters in the value")
    @Order(5)
    void startsWithEscapes() {

        // The value "100%" must be matched literally at the start
        var found = repository
                .findAll(queryFilterProcessor.newQueryFilter("author=starts:100%", QFParamType.RHS_COLON));

        assertThat(found).hasSize(1).containsExactly(PCT);

        // Plain value keeps the starts-with behavior
        found = repository.findAll(queryFilterProcessor.newQueryFilter("author=starts:100", QFParamType.RHS_COLON));

        assertThat(found).hasSize(4).containsExactlyInAnyOrder(PCT, PLAIN, UNDER, AXB);

    }

    @Test
    @DisplayName("6. Ends with with special characters in the value")
    @Order(6)
    void endsWithEscapes() {

        // The value "_cotton" must be matched literally at the end
        var found = repository
                .findAll(queryFilterProcessor.newQueryFilter("author=ends:_cotton", QFParamType.RHS_COLON));

        assertThat(found).hasSize(1).containsExactly(UNDER);

        // "%cotton" is a literal too: no row ends with "%cotton"
        found = repository.findAll(queryFilterProcessor.newQueryFilter("author=ends:%cotton", QFParamType.RHS_COLON));

        assertThat(found).isEmpty();

    }

    @Test
    @DisplayName("7. Not like with a literal percent in the value")
    @Order(7)
    void notLikeLiteralPercent() {

        // Everything that does not contain the literal "100%"
        var found = repository.findAll(queryFilterProcessor.newQueryFilter("author=nlike:100%", QFParamType.RHS_COLON));

        assertThat(found).hasSize(5).containsExactlyInAnyOrder(PLAIN, UNDER, AXB, SLASH, AB);

    }

    @Test
    @DisplayName("END. Test by clear BBDD")
    @Order(10)
    void clearBBDD() {
        repository.deleteAll();
        assertThat(repository.findAll()).isEmpty();
    }

}
