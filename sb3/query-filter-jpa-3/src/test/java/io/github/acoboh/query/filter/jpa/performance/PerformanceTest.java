package io.github.acoboh.query.filter.jpa.performance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class PerformanceTest {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceTest.class);

    private static final String[] QUERIES = { "campo1[eq]=valor1", "campo1[eq]=valor1&campo2[gt]=valor2",
            "campo1[eq]=valor1&sort=+campo2", "sort=+campo1,-campo2,+campo3",
            "campo1[eq]=marcos & comp&campo2[ne]=valor2", "campo1[eq]=valor con espacios y caracteres !@#",
            "campo1[eq]=valor1&campo2[lt]=valor2&campo3[gte]=valor3&sort=+campo4,-campo5", "campo1[eq]=",
            "campo1[eq]=valor\\&escapado&campo2[eq]=valor\\,escapado",
            "campoLargoConNombreExtenso[operationLarga]=valorExtensoconContenidoVariado&campo2[ne]=v&sort=-campoLargo",
            "campo1[operation]=marcos & comp&campo2[operation]=valor&sort=+campo1,-campo2&campo3[operation]=example & string" };

    private static final int ITERATIONS = 100000;

    @DisplayName("Test performance simple regex")
    @Test
    void testPerformanceSimple() {
        long startTime;
        long endTime;

        // First regex
        Pattern pattern1 = Pattern.compile("([^&=]+)\\[([a-zA-Z]+)\\]=((?:[^&]|&[^a-zA-Z0-9])*[^&]*)|sort=([^&]+)");
        startTime = System.nanoTime();
        for (String query : QUERIES) {
            for (int i = 0; i < ITERATIONS; i++) {
                Matcher matcher = pattern1.matcher(query);
                while (matcher.find()) {
                    if (matcher.group(1) != null && matcher.group(2) != null && matcher.group(3) != null) {
                        String field = matcher.group(1);
                        String operation = matcher.group(2);
                        String value = matcher.group(3);
                        logger.trace("field: {}, operation: {}, value: {}", field, operation, value);
                    } else if (matcher.group(4) != null) {
                        String sortFields = matcher.group(4);
                        logger.trace("sortFields: {}", sortFields);
                    }
                }
            }
        }

        assertThat(QUERIES.length * ITERATIONS).isGreaterThan(0);

        endTime = System.nanoTime();
        logger.info("First regex duration: {} ms", (endTime - startTime) / 1000000);
    }

    @DisplayName("Test performance complex regex")
    @Test
    void testPerformance() {
        long startTime;
        long endTime;

        // Second regex
        Pattern pattern2 = Pattern.compile("(([^&=]+)\\[([a-zA-Z]+)\\]=((?:[^&]|&[^a-zA-Z0-9])*[^&]*))|(sort=([^&]+))");
        startTime = System.nanoTime();
        for (String query : QUERIES) {
            for (int i = 0; i < ITERATIONS; i++) {
                Matcher matcher = pattern2.matcher(query);
                while (matcher.find()) {
                    if (matcher.group(1) != null) {
                        String field = matcher.group(2);
                        String operation = matcher.group(3);
                        String value = matcher.group(4);
                        logger.trace("field: {}, operation: {}, value: {}", field, operation, value);
                    } else if (matcher.group(5) != null) {
                        String sortFields = matcher.group(6);
                        logger.trace("sortFields: {}", sortFields);
                    }
                }
            }
        }

        assertThat(QUERIES.length * ITERATIONS).isGreaterThan(0);

        endTime = System.nanoTime();
        logger.info("Second regex duration: {} ms", (endTime - startTime) / 1000000);
    }

}
