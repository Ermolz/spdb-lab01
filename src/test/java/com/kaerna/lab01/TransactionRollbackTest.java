package com.kaerna.lab01;

import com.kaerna.lab01.entity.Product;
import com.kaerna.lab01.exception.Lab04CheckedException;
import com.kaerna.lab01.exception.Lab04NoRollbackException;
import com.kaerna.lab01.repository.ProductRepository;
import com.kaerna.lab01.service.TransactionRollbackService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@EnabledIfDockerAvailable
@Import(TestcontainersConfiguration.class)
@SpringBootTest
class TransactionRollbackTest {

    @Autowired
    TransactionRollbackService transactionRollbackService;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    EntityManager entityManager;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.flyway.enabled", () -> "false");
    }

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
    }

    @Test
    void saveAndThrowChecked_rollsBackTransaction() throws Lab04CheckedException {
        Product product = Product.builder().name("RollbackProduct").price(new BigDecimal("10")).build();

        assertThatThrownBy(() -> transactionRollbackService.saveAndThrowChecked(product))
                .isInstanceOf(Lab04CheckedException.class);

        entityManager.clear();
        assertThat(productRepository.count()).isZero();
    }

    @Test
    void saveAndThrowNoRollback_commitsTransaction() {
        Product product = Product.builder().name("NoRollbackProduct").price(new BigDecimal("20")).build();

        assertThatThrownBy(() -> transactionRollbackService.saveAndThrowNoRollback(product))
                .isInstanceOf(Lab04NoRollbackException.class);

        entityManager.clear();
        assertThat(productRepository.findByName("NoRollbackProduct")).isPresent();
    }
}
