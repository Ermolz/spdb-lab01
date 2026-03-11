package com.kaerna.lab01;

import com.kaerna.lab01.entity.Product;
import com.kaerna.lab01.repository.ProductRepository;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class ProductUniqueIndexTest {

    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
            .withDatabaseName("lab01")
            .withUsername("test")
            .withPassword("test");

    static {
        postgres.start();
        Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .locations("classpath:db/migration")
                .load()
                .migrate();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
    }

    @Autowired
    ProductRepository productRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void savingSecondProductWithSameName_throwsDataIntegrityViolationException() {
        productRepository.deleteAll();
        String sameName = "UniqueNameForDuplicateTest";
        Product first = Product.builder().name(sameName).price(new BigDecimal("10.00")).build();
        productRepository.saveAndFlush(first);

        Product second = Product.builder().name(sameName).price(new BigDecimal("20.00")).build();
        assertThatThrownBy(() -> productRepository.saveAndFlush(second))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void uniqueIndexUkProductName_existsInDatabase() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM pg_indexes WHERE tablename = 'product' AND indexname = 'uk_product_name'",
                Integer.class
        );
        assertThat(count).isGreaterThanOrEqualTo(1);
    }
}
