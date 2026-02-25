package com.kaerna.lab01;

import com.kaerna.lab01.entity.Product;
import com.kaerna.lab01.repository.ProductRepository;
import com.kaerna.lab01.service.EntityManagerTransactionService;
import com.kaerna.lab01.service.TransactionalOperationsService;
import com.kaerna.lab01.service.TransactionTemplateOperationsService;
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

@EnabledIfDockerAvailable
@Import(TestcontainersConfiguration.class)
@SpringBootTest
class TransactionOperationsTest {

    @Autowired
    TransactionalOperationsService transactionalOperationsService;

    @Autowired
    TransactionTemplateOperationsService transactionTemplateOperationsService;

    @Autowired
    EntityManagerTransactionService entityManagerTransactionService;

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
    void transactionalOperationsService_createUpdateDelete() {
        Product created = transactionalOperationsService.createProduct("TxnProduct", new BigDecimal("10"));
        assertThat(created.getId()).isNotNull();
        entityManager.clear();
        assertThat(productRepository.findByName("TxnProduct")).isPresent();

        transactionalOperationsService.updateProduct(created.getId(), "TxnProductUpdated", new BigDecimal("20"));
        entityManager.clear();
        Product updated = productRepository.findById(created.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("TxnProductUpdated");
        assertThat(updated.getPrice()).isEqualByComparingTo("20");

        transactionalOperationsService.deleteProduct(created.getId());
        entityManager.clear();
        assertThat(productRepository.findById(created.getId())).isEmpty();
    }

    @Test
    void transactionTemplateOperationsService_createUpdateDelete() {
        Product created = transactionTemplateOperationsService.createProduct("TplProduct", new BigDecimal("15"));
        assertThat(created.getId()).isNotNull();
        entityManager.clear();
        assertThat(productRepository.findByName("TplProduct")).isPresent();

        transactionTemplateOperationsService.updateProduct(created.getId(), "TplProductUpdated", new BigDecimal("25"));
        entityManager.clear();
        Product updated = productRepository.findById(created.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("TplProductUpdated");

        transactionTemplateOperationsService.deleteProduct(created.getId());
        entityManager.clear();
        assertThat(productRepository.findById(created.getId())).isEmpty();
    }

    @Test
    void entityManagerTransactionService_createUpdateDelete() {
        Product created = entityManagerTransactionService.createProduct("EmProduct", new BigDecimal("30"));
        assertThat(created.getId()).isNotNull();
        entityManager.clear();
        assertThat(productRepository.findByName("EmProduct")).isPresent();

        entityManagerTransactionService.updateProduct(created.getId(), "EmProductUpdated", new BigDecimal("40"));
        entityManager.clear();
        Product updated = productRepository.findById(created.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("EmProductUpdated");

        entityManagerTransactionService.deleteProduct(created.getId());
        entityManager.clear();
        assertThat(productRepository.findById(created.getId())).isEmpty();
    }
}
