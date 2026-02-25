package com.kaerna.lab01;

import com.kaerna.lab01.entity.Product;
import com.kaerna.lab01.repository.ProductRepository;
import com.kaerna.lab01.service.TransactionSerializableService;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfDockerAvailable
@Import(TestcontainersConfiguration.class)
@SpringBootTest
class TransactionSerializableTest {

    @Autowired
    TransactionSerializableService transactionSerializableService;

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
    void twoConcurrentSerializableTransactions_oneSucceedsOneFailsOrBothRetry() throws InterruptedException {
        Product product = productRepository.save(Product.builder()
                .name("SerializableProduct")
                .price(BigDecimal.ZERO)
                .build());
        Long id = product.getId();

        CountDownLatch startLatch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            executor.submit(() -> {
                try {
                    startLatch.await(5, TimeUnit.SECONDS);
                    transactionSerializableService.readIncrementAndSave(id);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                }
            });
            executor.submit(() -> {
                try {
                    startLatch.await(5, TimeUnit.SECONDS);
                    transactionSerializableService.readIncrementAndSave(id);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                }
            });
            startLatch.countDown();
        } finally {
            executor.shutdown();
            assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();
        }

        assertThat(successCount.get() + failureCount.get()).isEqualTo(2);
        entityManager.clear();
        Product updated = productRepository.findById(id).orElseThrow();
        assertThat(updated.getPrice().intValue()).isBetween(1, 2);
    }
}
