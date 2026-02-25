package com.kaerna.lab01;

import com.kaerna.lab01.entity.Product;
import com.kaerna.lab01.service.Lab05ReplicationService;

import java.math.BigDecimal;
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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfDockerAvailable
@Testcontainers
@SpringBootTest
@ActiveProfiles("lab05")
class PoolExhaustionTest {

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
        registry.add("app.datasource.primary.maximum-pool-size", () -> "1");
        registry.add("app.datasource.primary.connection-timeout", () -> "2000");
        registry.add("app.datasource.replica.jdbc-url", postgres::getJdbcUrl);
        registry.add("app.datasource.replica.username", postgres::getUsername);
        registry.add("app.datasource.replica.password", postgres::getPassword);
        registry.add("app.datasource.replica.maximum-pool-size", () -> "1");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.flyway.enabled", () -> "false");
    }

    @Autowired
    Lab05ReplicationService lab05ReplicationService;

    @Test
    void whenPoolExhausted_newRequestBlocksThenTimesOut() throws InterruptedException {
        CountDownLatch holderStarted = new CountDownLatch(1);
        AtomicReference<Exception> secondThreadError = new AtomicReference<>();

        Thread holder = new Thread(() -> {
            lab05ReplicationService.holdConnection(5000);
        });
        Thread requester = new Thread(() -> {
            try {
                holderStarted.await(10, TimeUnit.SECONDS);
                Thread.sleep(300);
                lab05ReplicationService.createProduct("ExhaustProduct", BigDecimal.ONE);
            } catch (Exception e) {
                secondThreadError.set(e);
            }
        });

        holder.start();
        holderStarted.countDown();
        requester.start();

        requester.join(15000);
        holder.join(10000);

        assertThat(secondThreadError.get()).isNotNull();
        assertThat(exceptionChainContains(secondThreadError.get(), "timeout")
                || exceptionChainContains(secondThreadError.get(), "timed out")
                || exceptionChainContains(secondThreadError.get(), "connection")).isTrue();
    }

    private static boolean exceptionChainContains(Throwable t, String sub) {
        while (t != null) {
            if (t.getMessage() != null && t.getMessage().toLowerCase().contains(sub)) {
                return true;
            }
            t = t.getCause();
        }
        return false;
    }
}
