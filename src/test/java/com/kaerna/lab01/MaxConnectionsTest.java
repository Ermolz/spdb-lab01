package com.kaerna.lab01;

import com.kaerna.lab01.service.Lab05ReplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfDockerAvailable
@Testcontainers
@SpringBootTest
@ActiveProfiles("lab05")
class MaxConnectionsTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"))
            .withDatabaseName("lab01")
            .withUsername("postgres")
            .withPassword("postgres");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("app.datasource.primary.jdbc-url", postgres::getJdbcUrl);
        registry.add("app.datasource.primary.username", postgres::getUsername);
        registry.add("app.datasource.primary.password", postgres::getPassword);
        registry.add("app.datasource.primary.maximum-pool-size", () -> "2");
        registry.add("app.datasource.primary.connection-timeout", () -> "3000");
        registry.add("app.datasource.replica.jdbc-url", postgres::getJdbcUrl);
        registry.add("app.datasource.replica.username", postgres::getUsername);
        registry.add("app.datasource.replica.password", postgres::getPassword);
        registry.add("app.datasource.replica.maximum-pool-size", () -> "2");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.flyway.enabled", () -> "false");
    }

    @Autowired
    Lab05ReplicationService lab05ReplicationService;

    @Test
    void whenDbMaxConnectionsExceeded_requestsFail() throws InterruptedException {
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);
        List<Exception> errors = new ArrayList<>();

        int threadCount = 5;
        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                try {
                    start.await(10, TimeUnit.SECONDS);
                    lab05ReplicationService.holdConnection(4000);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    synchronized (errors) {
                        errors.add(e);
                    }
                }
            });
        }
        for (Thread t : threads) {
            t.start();
        }
        start.countDown();
        for (Thread t : threads) {
            t.join(30000);
        }

        assertThat(successCount.get() + errors.size()).isEqualTo(threadCount);
        assertThat(errors).isNotEmpty();
        assertThat(errors.stream().anyMatch(e -> chainContains(e, "connection") || chainContains(e, "limit") || chainContains(e, "timeout") || chainContains(e, "timed"))).isTrue();
    }

    private static boolean chainContains(Throwable t, String sub) {
        while (t != null) {
            if (t.getMessage() != null && t.getMessage().toLowerCase().contains(sub)) {
                return true;
            }
            t = t.getCause();
        }
        return false;
    }
}
