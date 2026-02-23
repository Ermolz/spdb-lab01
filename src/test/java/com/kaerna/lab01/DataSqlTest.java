package com.kaerna.lab01;

import com.kaerna.lab01.repository.ProductJdbcRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("data-test")
class DataSqlTest {

    @Autowired
    ProductJdbcRepository productJdbcRepository;

    @Test
    void dataLoadedFromSql_file_insertedRowsPresent() {
        List<Map<String, Object>> rows = productJdbcRepository.findAll();
        assertThat(rows).hasSize(2);

        assertThat(rows).extracting("name").containsExactlyInAnyOrder("Test Product A", "Test Product B");
        java.util.Set<java.math.BigDecimal> prices = rows.stream()
                .map(r -> toBigDecimal(r.get("price")))
                .filter(p -> p != null)
                .collect(java.util.stream.Collectors.toSet());
        assertThat(prices).hasSize(2);
        assertThat(prices.stream().map(java.math.BigDecimal::doubleValue).toList())
                .containsExactlyInAnyOrder(10.5, 25.0);
    }

    private static java.math.BigDecimal toBigDecimal(Object value) {
        if (value == null) return null;
        if (value instanceof java.math.BigDecimal) return (java.math.BigDecimal) value;
        if (value instanceof Number n) return java.math.BigDecimal.valueOf(n.doubleValue());
        return new java.math.BigDecimal(value.toString());
    }
}
