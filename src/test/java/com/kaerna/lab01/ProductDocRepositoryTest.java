package com.kaerna.lab01;

import com.kaerna.lab01.document.ProductDoc;
import com.kaerna.lab01.repository.ProductDocRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ProductDocRepositoryTest {

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        MongoAndPostgresTestContainers.registerProperties(registry);
    }

    @Autowired
    ProductDocRepository productDocRepository;

    @Test
    void findByName_returnsSavedDocument() {
        productDocRepository.deleteAll();
        ProductDoc doc = ProductDoc.builder().name("Alpha").price(new BigDecimal("19.99")).build();
        productDocRepository.save(doc);

        Optional<ProductDoc> found = productDocRepository.findByName("Alpha");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Alpha");
        assertThat(found.get().getPrice()).isEqualByComparingTo("19.99");
    }

    @Test
    void findByPriceGreaterThan_returnsMatchingDocuments() {
        productDocRepository.deleteAll();
        productDocRepository.save(ProductDoc.builder().name("Cheap").price(new BigDecimal("5.00")).build());
        productDocRepository.save(ProductDoc.builder().name("Dear").price(new BigDecimal("50.00")).build());

        List<ProductDoc> result = productDocRepository.findByPriceGreaterThan(new BigDecimal("10.00"));
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Dear");
    }

    @Test
    void findByNameContainingIgnoreCase_returnsMatchingDocuments() {
        productDocRepository.deleteAll();
        productDocRepository.save(ProductDoc.builder().name("Widget Pro").price(new BigDecimal("10.00")).build());
        productDocRepository.save(ProductDoc.builder().name("Gadget").price(new BigDecimal("20.00")).build());

        List<ProductDoc> result = productDocRepository.findByNameContainingIgnoreCase("widget");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Widget Pro");
    }

    @Test
    void findCustomByName_returnsMatchingDocuments() {
        productDocRepository.deleteAll();
        productDocRepository.save(ProductDoc.builder().name("Exact").price(new BigDecimal("1.00")).build());

        List<ProductDoc> result = productDocRepository.findCustomByName("Exact");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Exact");
    }

    @Test
    void findCustomByPriceRange_returnsDocumentsInRange() {
        productDocRepository.deleteAll();
        productDocRepository.save(ProductDoc.builder().name("Low").price(new BigDecimal("5.00")).build());
        productDocRepository.save(ProductDoc.builder().name("Mid").price(new BigDecimal("15.00")).build());
        productDocRepository.save(ProductDoc.builder().name("High").price(new BigDecimal("25.00")).build());

        List<ProductDoc> result = productDocRepository.findCustomByPriceRange(new BigDecimal("10.00"), new BigDecimal("20.00"));
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Mid");
    }

    @Test
    void findCustomByNameLike_returnsDocumentsMatchingRegex() {
        productDocRepository.deleteAll();
        productDocRepository.save(ProductDoc.builder().name("SuperTool").price(new BigDecimal("1.00")).build());

        List<ProductDoc> result = productDocRepository.findCustomByNameLike(".*tool.*");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("SuperTool");
    }
}
