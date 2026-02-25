package com.kaerna.lab01;

import com.kaerna.lab01.entity.Product;
import com.kaerna.lab01.service.Lab05ReplicationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;
import java.net.Socket;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("lab05")
@EnabledIf("com.kaerna.lab01.ReplicationSyncTest#isReplicationClusterUp")
class ReplicationSyncTest {

    static boolean isReplicationClusterUp() {
        try (Socket p = new Socket("localhost", 5432); Socket r = new Socket("localhost", 5433)) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Autowired
    Lab05ReplicationService lab05ReplicationService;

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("app.datasource.primary.jdbc-url", () -> "jdbc:postgresql://localhost:5432/lab01");
        registry.add("app.datasource.primary.username", () -> "postgres");
        registry.add("app.datasource.primary.password", () -> "postgres");
        registry.add("app.datasource.replica.jdbc-url", () -> "jdbc:postgresql://localhost:5433/lab01");
        registry.add("app.datasource.replica.username", () -> "postgres");
        registry.add("app.datasource.replica.password", () -> "postgres");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.flyway.enabled", () -> "false");
    }

    @Test
    void writeOnPrimary_isVisibleOnReplica() {
        Product created = lab05ReplicationService.createProduct("SyncProduct", new BigDecimal("99.99"));
        assertThat(created.getId()).isNotNull();

        assertThat(lab05ReplicationService.findByNameReadOnly("SyncProduct"))
                .isPresent()
                .get()
                .extracting(Product::getName, Product::getPrice)
                .containsExactly("SyncProduct", new BigDecimal("99.99"));
    }
}
