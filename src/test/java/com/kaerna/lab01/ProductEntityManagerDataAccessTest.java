package com.kaerna.lab01;

import com.kaerna.lab01.data.ProductEntityManagerDataAccess;
import com.kaerna.lab01.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Transactional
class ProductEntityManagerDataAccessTest {

    @Autowired
    ProductEntityManagerDataAccess dataAccess;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Test
    void persist_thenFind_returnsSavedEntity() {
        Product product = Product.builder()
                .name("Persisted Product")
                .price(new BigDecimal("12.50"))
                .build();
        dataAccess.persist(product);

        Product found = dataAccess.find(product.getId());
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Persisted Product");
        assertThat(found.getPrice()).isEqualByComparingTo("12.50");
    }

    @Test
    void find_nonExistentId_returnsNull() {
        Product found = dataAccess.find(99999L);
        assertThat(found).isNull();
    }

    @Test
    void remove_thenFind_returnsNull() {
        Product product = Product.builder()
                .name("To Remove")
                .price(BigDecimal.ONE)
                .build();
        dataAccess.persist(product);
        Long id = product.getId();

        dataAccess.remove(product);

        Product found = dataAccess.find(id);
        assertThat(found).isNull();
    }

    @Test
    void merge_detachedEntity_updatesInDb() {
        Product product = Product.builder()
                .name("Original")
                .price(new BigDecimal("10"))
                .build();
        dataAccess.persist(product);
        Long id = product.getId();
        dataAccess.detach(product);

        product.setName("Updated");
        product.setPrice(new BigDecimal("20"));
        dataAccess.merge(product);

        Product found = dataAccess.find(id);
        assertThat(found.getName()).isEqualTo("Updated");
        assertThat(found.getPrice()).isEqualByComparingTo("20");
    }

    @Test
    void refresh_syncsFromDb() {
        Product product = Product.builder()
                .name("Original")
                .price(new BigDecimal("5"))
                .build();
        dataAccess.persist(product);
        Long id = product.getId();

        jdbcTemplate.update("UPDATE product SET name = ?, price = ? WHERE id = ?", "ChangedInDb", 99, id);

        dataAccess.refresh(product);
        assertThat(product.getName()).isEqualTo("ChangedInDb");
        assertThat(product.getPrice()).isEqualByComparingTo("99");
    }

    @Test
    void detach_changeNotPersisted() {
        Product product = Product.builder()
                .name("Original")
                .price(BigDecimal.TEN)
                .build();
        dataAccess.persist(product);
        Long id = product.getId();

        dataAccess.detach(product);
        product.setName("DetachedChange");

        Product found = dataAccess.find(id);
        assertThat(found.getName()).isEqualTo("Original");
    }
}
