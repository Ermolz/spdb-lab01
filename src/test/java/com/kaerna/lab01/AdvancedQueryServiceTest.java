package com.kaerna.lab01;

import com.kaerna.lab01.entity.Product;
import com.kaerna.lab01.entity.SaleRecord;
import com.kaerna.lab01.repository.ProductRepository;
import com.kaerna.lab01.repository.SaleRecordRepository;
import com.kaerna.lab01.service.AdvancedQueryService;
import jakarta.persistence.EntityManager;
import com.kaerna.lab01.service.ProductAggregate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Transactional
class AdvancedQueryServiceTest {

    @Autowired
    AdvancedQueryService advancedQueryService;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    SaleRecordRepository saleRecordRepository;

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
        saleRecordRepository.deleteAll();
    }

    @Test
    void deleteByPriceLessThanJpql_removesProducts() {
        productRepository.save(Product.builder().name("A").price(new BigDecimal("50")).build());
        productRepository.save(Product.builder().name("B").price(new BigDecimal("150")).build());
        productRepository.save(Product.builder().name("C").price(new BigDecimal("25")).build());

        int deleted = advancedQueryService.deleteByPriceLessThanJpql(new BigDecimal("100"));

        assertThat(deleted).isEqualTo(2);
        assertThat(productRepository.findAll()).extracting(Product::getName).containsExactly("B");
    }

    @Test
    void deleteByPriceLessThanNamedQuery_removesProducts() {
        productRepository.save(Product.builder().name("A").price(new BigDecimal("50")).build());
        productRepository.save(Product.builder().name("B").price(new BigDecimal("150")).build());

        int deleted = advancedQueryService.deleteByPriceLessThanNamedQuery(new BigDecimal("100"));

        assertThat(deleted).isEqualTo(1);
        assertThat(productRepository.findAll()).extracting(Product::getName).containsExactly("B");
    }

    @Test
    void deleteByPriceLessThanCriteria_removesProducts() {
        productRepository.save(Product.builder().name("A").price(new BigDecimal("30")).build());
        productRepository.save(Product.builder().name("B").price(new BigDecimal("200")).build());

        int deleted = advancedQueryService.deleteByPriceLessThanCriteria(new BigDecimal("100"));

        assertThat(deleted).isEqualTo(1);
        assertThat(productRepository.findAll()).extracting(Product::getName).containsExactly("B");
    }

    @Test
    void deleteByPriceLessThanNative_removesProducts() {
        productRepository.save(Product.builder().name("A").price(new BigDecimal("40")).build());
        productRepository.save(Product.builder().name("B").price(new BigDecimal("250")).build());

        int deleted = advancedQueryService.deleteByPriceLessThanNative(new BigDecimal("100"));

        assertThat(deleted).isEqualTo(1);
        assertThat(productRepository.findAll()).extracting(Product::getName).containsExactly("B");
    }

    @Test
    void deleteByPriceLessThanJooq_removesProducts() {
        productRepository.save(Product.builder().name("A").price(new BigDecimal("60")).build());
        productRepository.save(Product.builder().name("B").price(new BigDecimal("300")).build());

        int deleted = advancedQueryService.deleteByPriceLessThanJooq(new BigDecimal("100"));

        assertThat(deleted).isEqualTo(1);
        assertThat(productRepository.findAll()).extracting(Product::getName).containsExactly("B");
    }

    @Test
    void updatePriceByNameContainsJpql_updatesMatchingProducts() {
        productRepository.save(Product.builder().name("ApplePhone").price(new BigDecimal("100")).build());
        productRepository.save(Product.builder().name("BananaPhone").price(new BigDecimal("100")).build());
        productRepository.save(Product.builder().name("Orange").price(new BigDecimal("100")).build());

        int updated = advancedQueryService.updatePriceByNameContainsJpql("Phone", new BigDecimal("200"));
        entityManager.clear();

        assertThat(updated).isEqualTo(2);
        assertThat(productRepository.findAll())
                .filteredOn(p -> p.getName().contains("Phone"))
                .allMatch(p -> p.getPrice().compareTo(new BigDecimal("200")) == 0);
        assertThat(productRepository.findByName("Orange").orElseThrow().getPrice()).isEqualByComparingTo("100");
    }

    @Test
    void updatePriceByNameContainsNamedQuery_updatesMatchingProducts() {
        productRepository.save(Product.builder().name("TestItem").price(new BigDecimal("50")).build());
        productRepository.save(Product.builder().name("Other").price(new BigDecimal("50")).build());

        int updated = advancedQueryService.updatePriceByNameContainsNamedQuery("Test", new BigDecimal("99"));
        entityManager.clear();

        assertThat(updated).isEqualTo(1);
        assertThat(productRepository.findByName("TestItem").orElseThrow().getPrice()).isEqualByComparingTo("99");
        assertThat(productRepository.findByName("Other").orElseThrow().getPrice()).isEqualByComparingTo("50");
    }

    @Test
    void updatePriceByNameContainsCriteria_updatesMatchingProducts() {
        productRepository.save(Product.builder().name("PrefixMatch").price(new BigDecimal("10")).build());
        productRepository.save(Product.builder().name("NoMatch").price(new BigDecimal("10")).build());

        int updated = advancedQueryService.updatePriceByNameContainsCriteria("Prefix", new BigDecimal("55"));
        entityManager.clear();

        assertThat(updated).isEqualTo(1);
        assertThat(productRepository.findByName("PrefixMatch").orElseThrow().getPrice()).isEqualByComparingTo("55");
    }

    @Test
    void updatePriceByNameContainsNative_updatesMatchingProducts() {
        productRepository.save(Product.builder().name("NativeTest").price(new BigDecimal("20")).build());
        productRepository.save(Product.builder().name("Other").price(new BigDecimal("20")).build());

        int updated = advancedQueryService.updatePriceByNameContainsNative("Native", new BigDecimal("77"));
        entityManager.clear();

        assertThat(updated).isEqualTo(1);
        assertThat(productRepository.findByName("NativeTest").orElseThrow().getPrice()).isEqualByComparingTo("77");
    }

    @Test
    void updatePriceByNameContainsJooq_updatesMatchingProducts() {
        productRepository.save(Product.builder().name("JooqItem").price(new BigDecimal("30")).build());
        productRepository.save(Product.builder().name("Different").price(new BigDecimal("30")).build());

        int updated = advancedQueryService.updatePriceByNameContainsJooq("Jooq", new BigDecimal("88"));
        entityManager.clear();

        assertThat(updated).isEqualTo(1);
        assertThat(productRepository.findByName("JooqItem").orElseThrow().getPrice()).isEqualByComparingTo("88");
    }

    @Test
    void findProductNamesWithTotalQuantityGreaterThanJpql_returnsFilteredNames() {
        saleRecordRepository.save(SaleRecord.builder().productName("A").quantity(5).totalQuantity(5L).build());
        saleRecordRepository.save(SaleRecord.builder().productName("A").quantity(8).totalQuantity(13L).build());
        saleRecordRepository.save(SaleRecord.builder().productName("B").quantity(3).totalQuantity(3L).build());

        List<String> result = advancedQueryService.findProductNamesWithTotalQuantityGreaterThanJpql(10L);

        assertThat(result).containsExactly("A");
    }

    @Test
    void findProductNamesWithTotalQuantityGreaterThanNamedQuery_returnsFilteredNames() {
        saleRecordRepository.save(SaleRecord.builder().productName("X").quantity(10).totalQuantity(10L).build());
        saleRecordRepository.save(SaleRecord.builder().productName("Y").quantity(2).totalQuantity(2L).build());

        List<String> result = advancedQueryService.findProductNamesWithTotalQuantityGreaterThanNamedQuery(5L);

        assertThat(result).containsExactly("X");
    }

    @Test
    void findProductNamesWithTotalQuantityGreaterThanCriteria_returnsFilteredNames() {
        saleRecordRepository.save(SaleRecord.builder().productName("C1").quantity(7).totalQuantity(7L).build());
        saleRecordRepository.save(SaleRecord.builder().productName("C2").quantity(2).totalQuantity(2L).build());

        List<String> result = advancedQueryService.findProductNamesWithTotalQuantityGreaterThanCriteria(5L);

        assertThat(result).containsExactly("C1");
    }

    @Test
    void findProductNamesWithTotalQuantityGreaterThanNative_returnsFilteredNames() {
        saleRecordRepository.save(SaleRecord.builder().productName("N1").quantity(20).totalQuantity(20L).build());
        saleRecordRepository.save(SaleRecord.builder().productName("N2").quantity(1).totalQuantity(1L).build());

        List<String> result = advancedQueryService.findProductNamesWithTotalQuantityGreaterThanNative(10L);

        assertThat(result).containsExactly("N1");
    }

    @Test
    void findProductNamesWithTotalQuantityGreaterThanJooq_returnsFilteredNames() {
        saleRecordRepository.save(SaleRecord.builder().productName("J1").quantity(15).totalQuantity(15L).build());
        saleRecordRepository.save(SaleRecord.builder().productName("J2").quantity(1).totalQuantity(1L).build());

        List<String> result = advancedQueryService.findProductNamesWithTotalQuantityGreaterThanJooq(10L);

        assertThat(result).containsExactly("J1");
    }

    @Test
    void findProductCountAndAvgPriceJpql_returnsAggregate() {
        productRepository.save(Product.builder().name("P1").price(new BigDecimal("10")).build());
        productRepository.save(Product.builder().name("P2").price(new BigDecimal("20")).build());
        productRepository.save(Product.builder().name("P3").price(new BigDecimal("30")).build());

        ProductAggregate result = advancedQueryService.findProductCountAndAvgPriceJpql();

        assertThat(result.count()).isEqualTo(3);
        assertThat(result.avgPrice()).isEqualByComparingTo("20");
    }

    @Test
    void findProductCountAndAvgPriceNamedQuery_returnsAggregate() {
        productRepository.save(Product.builder().name("Q1").price(new BigDecimal("100")).build());
        productRepository.save(Product.builder().name("Q2").price(new BigDecimal("200")).build());

        ProductAggregate result = advancedQueryService.findProductCountAndAvgPriceNamedQuery();

        assertThat(result.count()).isEqualTo(2);
        assertThat(result.avgPrice()).isEqualByComparingTo("150");
    }

    @Test
    void findProductCountAndAvgPriceCriteria_returnsAggregate() {
        productRepository.save(Product.builder().name("R1").price(new BigDecimal("5")).build());
        productRepository.save(Product.builder().name("R2").price(new BigDecimal("15")).build());

        ProductAggregate result = advancedQueryService.findProductCountAndAvgPriceCriteria();

        assertThat(result.count()).isEqualTo(2);
        assertThat(result.avgPrice()).isEqualByComparingTo("10");
    }

    @Test
    void findProductCountAndAvgPriceNative_returnsAggregate() {
        productRepository.save(Product.builder().name("S1").price(new BigDecimal("40")).build());
        productRepository.save(Product.builder().name("S2").price(new BigDecimal("60")).build());

        ProductAggregate result = advancedQueryService.findProductCountAndAvgPriceNative();

        assertThat(result.count()).isEqualTo(2);
        assertThat(result.avgPrice()).isEqualByComparingTo("50");
    }

    @Test
    void findProductCountAndAvgPriceJooq_returnsAggregate() {
        productRepository.save(Product.builder().name("T1").price(new BigDecimal("25")).build());
        productRepository.save(Product.builder().name("T2").price(new BigDecimal("75")).build());

        ProductAggregate result = advancedQueryService.findProductCountAndAvgPriceJooq();

        assertThat(result.count()).isEqualTo(2);
        assertThat(result.avgPrice()).isEqualByComparingTo("50");
    }

    @Test
    void findProductsWithSaleRecordsJpql_returnsProductsWithMatchingSales() {
        productRepository.save(Product.builder().name("Widget").price(new BigDecimal("10")).build());
        productRepository.save(Product.builder().name("Gadget").price(new BigDecimal("20")).build());
        productRepository.save(Product.builder().name("Orphan").price(new BigDecimal("30")).build());
        saleRecordRepository.save(SaleRecord.builder().productName("Widget").quantity(1).totalQuantity(1L).build());
        saleRecordRepository.save(SaleRecord.builder().productName("Gadget").quantity(1).totalQuantity(1L).build());

        List<Product> result = advancedQueryService.findProductsWithSaleRecordsJpql();

        assertThat(result).extracting(Product::getName).containsExactlyInAnyOrder("Widget", "Gadget");
    }

    @Test
    void findProductsWithSaleRecordsNamedQuery_returnsProductsWithMatchingSales() {
        productRepository.save(Product.builder().name("JoinA").price(new BigDecimal("1")).build());
        productRepository.save(Product.builder().name("JoinB").price(new BigDecimal("2")).build());
        productRepository.save(Product.builder().name("Solo").price(new BigDecimal("3")).build());
        saleRecordRepository.save(SaleRecord.builder().productName("JoinA").quantity(1).totalQuantity(1L).build());
        saleRecordRepository.save(SaleRecord.builder().productName("JoinB").quantity(1).totalQuantity(1L).build());

        List<Product> result = advancedQueryService.findProductsWithSaleRecordsNamedQuery();

        assertThat(result).extracting(Product::getName).containsExactlyInAnyOrder("JoinA", "JoinB");
    }

    @Test
    void findProductsWithSaleRecordsCriteria_returnsProductsWithMatchingSales() {
        productRepository.save(Product.builder().name("CritA").price(new BigDecimal("1")).build());
        productRepository.save(Product.builder().name("CritB").price(new BigDecimal("2")).build());
        saleRecordRepository.save(SaleRecord.builder().productName("CritA").quantity(1).totalQuantity(1L).build());
        saleRecordRepository.save(SaleRecord.builder().productName("CritB").quantity(1).totalQuantity(1L).build());

        List<Product> result = advancedQueryService.findProductsWithSaleRecordsCriteria();

        assertThat(result).extracting(Product::getName).containsExactlyInAnyOrder("CritA", "CritB");
    }

    @Test
    void findProductsWithSaleRecordsNative_returnsProductsWithMatchingSales() {
        productRepository.save(Product.builder().name("NatA").price(new BigDecimal("1")).build());
        productRepository.save(Product.builder().name("NatB").price(new BigDecimal("2")).build());
        saleRecordRepository.save(SaleRecord.builder().productName("NatA").quantity(1).totalQuantity(1L).build());
        saleRecordRepository.save(SaleRecord.builder().productName("NatB").quantity(1).totalQuantity(1L).build());

        List<Product> result = advancedQueryService.findProductsWithSaleRecordsNative();

        assertThat(result).extracting(Product::getName).containsExactlyInAnyOrder("NatA", "NatB");
    }

    @Test
    void findProductsWithSaleRecordsJooq_returnsProductsWithMatchingSales() {
        productRepository.save(Product.builder().name("JooqA").price(new BigDecimal("1")).build());
        productRepository.save(Product.builder().name("JooqB").price(new BigDecimal("2")).build());
        saleRecordRepository.save(SaleRecord.builder().productName("JooqA").quantity(1).totalQuantity(1L).build());
        saleRecordRepository.save(SaleRecord.builder().productName("JooqB").quantity(1).totalQuantity(1L).build());

        List<Product> result = advancedQueryService.findProductsWithSaleRecordsJooq();

        assertThat(result).extracting(Product::getName).containsExactlyInAnyOrder("JooqA", "JooqB");
    }

    @Test
    void findProductsDynamicJpql_filtersByOptionalParams() {
        productRepository.save(Product.builder().name("DynamicA").price(new BigDecimal("50")).build());
        productRepository.save(Product.builder().name("DynamicB").price(new BigDecimal("150")).build());
        productRepository.save(Product.builder().name("Static").price(new BigDecimal("75")).build());

        List<Product> byName = advancedQueryService.findProductsDynamicJpql("Dynamic", null);
        assertThat(byName).extracting(Product::getName).containsExactlyInAnyOrder("DynamicA", "DynamicB");

        List<Product> byPrice = advancedQueryService.findProductsDynamicJpql(null, new BigDecimal("100"));
        assertThat(byPrice).extracting(Product::getName).containsExactly("DynamicB");

        List<Product> byBoth = advancedQueryService.findProductsDynamicJpql("Dynamic", new BigDecimal("100"));
        assertThat(byBoth).extracting(Product::getName).containsExactly("DynamicB");
    }

    @Test
    void findProductsDynamicCriteria_filtersByOptionalParams() {
        productRepository.save(Product.builder().name("CritDynA").price(new BigDecimal("10")).build());
        productRepository.save(Product.builder().name("CritDynB").price(new BigDecimal("200")).build());

        List<Product> result = advancedQueryService.findProductsDynamicCriteria("CritDyn", new BigDecimal("50"));
        assertThat(result).extracting(Product::getName).containsExactly("CritDynB");
    }

    @Test
    void findProductsDynamicNative_filtersByOptionalParams() {
        productRepository.save(Product.builder().name("NativeDyn").price(new BigDecimal("300")).build());
        productRepository.save(Product.builder().name("Other").price(new BigDecimal("50")).build());

        List<Product> result = advancedQueryService.findProductsDynamicNative("Native", new BigDecimal("100"));
        assertThat(result).extracting(Product::getName).containsExactly("NativeDyn");
    }

    @Test
    void findProductsDynamicJooq_filtersByOptionalParams() {
        productRepository.save(Product.builder().name("JooqDyn").price(new BigDecimal("500")).build());
        productRepository.save(Product.builder().name("Low").price(new BigDecimal("10")).build());

        List<Product> result = advancedQueryService.findProductsDynamicJooq("Jooq", new BigDecimal("100"));
        assertThat(result).extracting(Product::getName).containsExactly("JooqDyn");
    }
}
