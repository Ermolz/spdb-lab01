package com.kaerna.lab01;

import com.kaerna.lab01.entity.Product;
import com.kaerna.lab01.repository.ProductJdbcRepository;
import com.kaerna.lab01.repository.ProductRepository;
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
class TestcontainersIntegrationTests {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ProductJdbcRepository productJdbcRepository;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Test
    void jpaRepository_saveAndFindById_returnsSavedProduct() {
        Product product = Product.builder()
                .name("Container Product")
                .price(new BigDecimal("99.99"))
                .build();
        product = productRepository.save(product);

        Optional<Product> found = productRepository.findById(product.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Container Product");
        assertThat(found.get().getPrice()).isEqualByComparingTo("99.99");
    }

    @Test
    void jdbcTemplate_insertAndFindById_returnsInsertedRow() {
        Long id = productJdbcRepository.insertReturningId("Jdbc Product", new BigDecimal("42.00"));
        Optional<Map<String, Object>> row = productJdbcRepository.findById(id);
        assertThat(row).isPresent();
        assertThat(row.get().get("name")).isEqualTo("Jdbc Product");
        assertThat(toBigDecimal(row.get().get("price"))).isEqualByComparingTo(new BigDecimal("42.00"));
    }

    private static java.math.BigDecimal toBigDecimal(Object value) {
        if (value == null) return null;
        if (value instanceof java.math.BigDecimal) return (java.math.BigDecimal) value;
        return new java.math.BigDecimal(value.toString());
    }
}
