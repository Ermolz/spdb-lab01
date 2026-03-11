package com.kaerna.lab01;

import com.kaerna.lab01.document.ProductDoc;
import com.kaerna.lab01.document.SaleRecordDoc;
import com.kaerna.lab01.service.ProductMongoTemplateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ProductMongoTemplateServiceTest {

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        MongoAndPostgresTestContainers.registerProperties(registry);
    }

    @Autowired
    ProductMongoTemplateService productMongoTemplateService;

    @Autowired
    MongoTemplate mongoTemplate;

    @BeforeEach
    void clearMongoCollections() {
        mongoTemplate.dropCollection("products");
        mongoTemplate.dropCollection("sale_records");
    }

    @Test
    void findByName_returnsMatchingDocuments() {
        mongoTemplate.save(ProductDoc.builder().name("Tool").price(new BigDecimal("9.99")).build(), "products");

        List<ProductDoc> result = productMongoTemplateService.findByName("Tool");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Tool");
    }

    @Test
    void findTopByPriceDesc_returnsSortedAndLimited() {
        mongoTemplate.save(ProductDoc.builder().name("A").price(new BigDecimal("1")).build(), "products");
        mongoTemplate.save(ProductDoc.builder().name("B").price(new BigDecimal("3")).build(), "products");
        mongoTemplate.save(ProductDoc.builder().name("C").price(new BigDecimal("2")).build(), "products");

        List<ProductDoc> result = productMongoTemplateService.findTopByPriceDesc(2);
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("B");
        assertThat(result.get(1).getName()).isEqualTo("C");
    }

    @Test
    void sumQuantityByProductName_returnsAggregatedTotals() {
        mongoTemplate.save(SaleRecordDoc.builder().productName("X").quantity(10).build(), "sale_records");
        mongoTemplate.save(SaleRecordDoc.builder().productName("X").quantity(5).build(), "sale_records");
        mongoTemplate.save(SaleRecordDoc.builder().productName("Y").quantity(2).build(), "sale_records");

        List<Map> result = productMongoTemplateService.sumQuantityByProductName();
        assertThat(result).hasSize(2);
        Map x = result.stream().filter(m -> "X".equals(m.get("productName"))).findFirst().orElseThrow();
        assertThat(x.get("totalQuantity")).isEqualTo(15);
        Map y = result.stream().filter(m -> "Y".equals(m.get("productName"))).findFirst().orElseThrow();
        assertThat(y.get("totalQuantity")).isEqualTo(2);
    }
}
