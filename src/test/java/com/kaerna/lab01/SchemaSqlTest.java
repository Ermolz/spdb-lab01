package com.kaerna.lab01;

import com.kaerna.lab01.repository.ProductJdbcRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("schema-test")
class SchemaSqlTest {

    @Autowired
    ProductJdbcRepository productJdbcRepository;

    @Test
    void schemaLoadedFromSql_file_tableExistsAndInsertSucceeds() {
        Long id = productJdbcRepository.insertReturningId("Schema Test Product", new BigDecimal("99.99"));
        assertThat(id).isNotNull();

        Optional<Map<String, Object>> row = productJdbcRepository.findById(id);
        assertThat(row).isPresent();
        assertThat(row.get().get("name")).isEqualTo("Schema Test Product");
    }
}
