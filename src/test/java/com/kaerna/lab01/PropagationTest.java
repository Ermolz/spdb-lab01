package com.kaerna.lab01;

import com.kaerna.lab01.repository.ProductRepository;
import com.kaerna.lab01.service.PropagationOuterService;
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
class PropagationTest {

    @Autowired
    PropagationOuterService propagationOuterService;

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
    void required_innerJoinsOuter_bothRolledBack() {
        assertThatThrownBy(() -> propagationOuterService.outerThenInnerRequiredThenThrow(
                "OuterRequired", "InnerRequired", new BigDecimal("1")))
                .isInstanceOf(RuntimeException.class);

        entityManager.clear();
        assertThat(productRepository.findByName("OuterRequired")).isEmpty();
        assertThat(productRepository.findByName("InnerRequired")).isEmpty();
    }

    @Test
    void requiresNew_innerCommitsOuterRollsBack_innerSaved() {
        assertThatThrownBy(() -> propagationOuterService.outerThenInnerRequiresNewThenThrow(
                "OuterRequiresNew", "InnerRequiresNew", new BigDecimal("2")))
                .isInstanceOf(RuntimeException.class);

        entityManager.clear();
        assertThat(productRepository.findByName("OuterRequiresNew")).isEmpty();
        assertThat(productRepository.findByName("InnerRequiresNew")).isPresent();
    }

    @Test
    void nested_outerRollback_rollsBackNested() {
        assertThatThrownBy(() -> propagationOuterService.outerThenInnerNestedThenThrow(
                "OuterNested", "InnerNested", new BigDecimal("3")))
                .isInstanceOf(RuntimeException.class);

        entityManager.clear();
        assertThat(productRepository.findByName("OuterNested")).isEmpty();
        assertThat(productRepository.findByName("InnerNested")).isEmpty();
    }
}
