package com.kaerna.lab01;

import com.kaerna.lab01.entity.Product;
import com.kaerna.lab01.service.Lab05ReplicationService;
import org.junit.jupiter.api.BeforeEach;
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
class RoutingDataSourceTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"))
            .withDatabaseName("lab01")
            .withUsername("postgres")
            .withPassword("postgres");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        String url = postgres.getJdbcUrl();
        registry.add("app.datasource.primary.jdbc-url", () -> url);
        registry.add("app.datasource.primary.username", postgres::getUsername);
        registry.add("app.datasource.primary.password", postgres::getPassword);
        registry.add("app.datasource.replica.jdbc-url", () -> url);
        registry.add("app.datasource.replica.username", postgres::getUsername);
        registry.add("app.datasource.replica.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.flyway.enabled", () -> "false");
    }

    @Autowired
    Lab05ReplicationService lab05ReplicationService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void writeAndReadOnly_useRouting() {
        Product created = lab05ReplicationService.createProduct("RouteProduct", new BigDecimal("11.11"));
        assertThat(created.getId()).isNotNull();

        assertThat(lab05ReplicationService.findByNameReadOnly("RouteProduct"))
                .isPresent()
                .get()
                .extracting(Product::getName)
                .isEqualTo("RouteProduct");
    }
}
