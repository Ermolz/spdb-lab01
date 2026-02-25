package com.kaerna.lab01;

import com.kaerna.lab01.entity.Product;
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

import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfDockerAvailable
@Testcontainers
@SpringBootTest
@ActiveProfiles("lab05")
class ReplicaFallbackTest {

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
        registry.add("app.datasource.replica.jdbc-url", () -> "jdbc:postgresql://localhost:54399/nonexistent");
        registry.add("app.datasource.replica.username", () -> "postgres");
        registry.add("app.datasource.replica.password", () -> "postgres");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.flyway.enabled", () -> "false");
    }

    @Autowired
    Lab05ReplicationService lab05ReplicationService;

    @Test
    void whenReplicaUnavailable_readOnlyFallsBackToPrimary() {
        Product created = lab05ReplicationService.createProduct("FallbackProduct", new BigDecimal("33.33"));
        assertThat(created.getId()).isNotNull();

        assertThat(lab05ReplicationService.findByNameReadOnly("FallbackProduct"))
                .isPresent()
                .get()
                .extracting(Product::getName)
                .isEqualTo("FallbackProduct");
    }
}
