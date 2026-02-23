package com.kaerna.lab01;

import com.kaerna.lab01.repository.ProductJdbcRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class JdbcTemplateQueryTests {

    @Autowired
    ProductJdbcRepository productJdbcRepository;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Test
    void select_findById_returnsEmptyWhenNotFound() {
        Optional<Map<String, Object>> result = productJdbcRepository.findById(999L);
        assertThat(result).isEmpty();
    }

    @Test
    void insert_and_select_findById_returnsInsertedRow() {
        Long id = productJdbcRepository.insertReturningId("Widget", new BigDecimal("19.99"));
        assertThat(id).isNotNull();

        long count = productJdbcRepository.count();
        assertThat(count).isEqualTo(1);

        Optional<Map<String, Object>> row = productJdbcRepository.findById(id);
        assertThat(row).isPresent();
        assertThat(row.get().get("name")).isEqualTo("Widget");
        assertThat(toBigDecimal(row.get().get("price"))).isEqualByComparingTo(new BigDecimal("19.99"));
    }

    @Test
    void updatePrice_changesPrice() {
        Long id = productJdbcRepository.insertReturningId("Item", new BigDecimal("10.00"));
        int updated = productJdbcRepository.updatePrice(id, new BigDecimal("15.50"));
        assertThat(updated).isEqualTo(1);

        Optional<Map<String, Object>> row = productJdbcRepository.findById(id);
        assertThat(row).isPresent();
        assertThat(toBigDecimal(row.get().get("price"))).isEqualByComparingTo(new BigDecimal("15.50"));
    }

    @Test
    void deleteById_removesRow() {
        Long id = productJdbcRepository.insertReturningId("ToDelete", new BigDecimal("1.00"));
        assertThat(productJdbcRepository.count()).isEqualTo(1);

        int deleted = productJdbcRepository.deleteById(id);
        assertThat(deleted).isEqualTo(1);
        assertThat(productJdbcRepository.count()).isEqualTo(0);
        assertThat(productJdbcRepository.findById(id)).isEmpty();
    }

    private static java.math.BigDecimal toBigDecimal(Object value) {
        if (value == null) return null;
        if (value instanceof java.math.BigDecimal) return (java.math.BigDecimal) value;
        return new java.math.BigDecimal(value.toString());
    }
}
