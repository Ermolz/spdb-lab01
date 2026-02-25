package com.kaerna.lab01;

import com.kaerna.lab01.repository.ProductRepository;
import com.kaerna.lab01.service.TransactionSelfInvocationService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfDockerAvailable
@Import(TestcontainersConfiguration.class)
@SpringBootTest
class TransactionSelfInvocationTest {

    @Autowired
    TransactionSelfInvocationService transactionSelfInvocationService;

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
    void caller_selfInvocation_calleeRunsWithoutTransaction() {
        AtomicBoolean transactionActive = new AtomicBoolean(true);

        transactionSelfInvocationService.caller(transactionActive);

        assertThat(transactionActive.get()).isFalse();
    }

    @Test
    void callerViaDelegate_calleeRunsInTransaction() {
        AtomicBoolean transactionActive = new AtomicBoolean(false);

        transactionSelfInvocationService.callerViaDelegate(transactionActive);

        assertThat(transactionActive.get()).isTrue();
        entityManager.clear();
        assertThat(productRepository.findByName("CalleeProduct")).isPresent();
    }
}
