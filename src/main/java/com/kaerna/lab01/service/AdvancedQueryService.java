package com.kaerna.lab01.service;

import com.kaerna.lab01.entity.Product;
import com.kaerna.lab01.entity.SaleRecord;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.sum;
import static org.jooq.impl.DSL.table;

@Component
@Transactional
public class AdvancedQueryService {

    private final EntityManager entityManager;
    private final DSLContext dsl;

    public AdvancedQueryService(EntityManager entityManager, DSLContext dsl) {
        this.entityManager = entityManager;
        this.dsl = dsl;
    }

    public int deleteByPriceLessThanJpql(BigDecimal maxPrice) {
        return entityManager.createQuery("DELETE FROM Product p WHERE p.price < :maxPrice")
                .setParameter("maxPrice", maxPrice)
                .executeUpdate();
    }

    public int deleteByPriceLessThanNamedQuery(BigDecimal maxPrice) {
        return entityManager.createNamedQuery("Product.deleteByPriceLessThan")
                .setParameter("maxPrice", maxPrice)
                .executeUpdate();
    }

    public int deleteByPriceLessThanCriteria(BigDecimal maxPrice) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaDelete<Product> delete = cb.createCriteriaDelete(Product.class);
        Root<Product> root = delete.from(Product.class);
        delete.where(cb.lessThan(root.get("price"), maxPrice));
        return entityManager.createQuery(delete).executeUpdate();
    }

    public int deleteByPriceLessThanNative(BigDecimal maxPrice) {
        return entityManager.createNativeQuery("DELETE FROM product WHERE price < ?1")
                .setParameter(1, maxPrice)
                .executeUpdate();
    }

    public int deleteByPriceLessThanJooq(BigDecimal maxPrice) {
        return dsl.deleteFrom(table("product"))
                .where(field("price").lessThan(maxPrice))
                .execute();
    }

    public int updatePriceByNameContainsJpql(String substring, BigDecimal newPrice) {
        String pattern = "%" + substring + "%";
        return entityManager.createQuery("UPDATE Product p SET p.price = :newPrice WHERE p.name LIKE :pattern")
                .setParameter("newPrice", newPrice)
                .setParameter("pattern", pattern)
                .executeUpdate();
    }

    public int updatePriceByNameContainsNamedQuery(String substring, BigDecimal newPrice) {
        String pattern = "%" + substring + "%";
        return entityManager.createNamedQuery("Product.updatePriceByNameContains")
                .setParameter("newPrice", newPrice)
                .setParameter("pattern", pattern)
                .executeUpdate();
    }

    public int updatePriceByNameContainsCriteria(String substring, BigDecimal newPrice) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaUpdate<Product> update = cb.createCriteriaUpdate(Product.class);
        Root<Product> root = update.from(Product.class);
        update.set("price", newPrice);
        update.where(cb.like(root.get("name"), "%" + substring + "%"));
        return entityManager.createQuery(update).executeUpdate();
    }

    public int updatePriceByNameContainsNative(String substring, BigDecimal newPrice) {
        String pattern = "%" + substring + "%";
        return entityManager.createNativeQuery("UPDATE product SET price = ?1 WHERE name LIKE ?2")
                .setParameter(1, newPrice)
                .setParameter(2, pattern)
                .executeUpdate();
    }

    public int updatePriceByNameContainsJooq(String substring, BigDecimal newPrice) {
        String pattern = "%" + substring + "%";
        return dsl.update(table("product"))
                .set(field("price"), newPrice)
                .where(field("name").like(pattern))
                .execute();
    }

    @SuppressWarnings("unchecked")
    public List<String> findProductNamesWithTotalQuantityGreaterThanJpql(Long minTotal) {
        return entityManager.createQuery(
                        "SELECT s.productName FROM SaleRecord s GROUP BY s.productName HAVING SUM(s.quantity) > :minTotal")
                .setParameter("minTotal", minTotal)
                .getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<String> findProductNamesWithTotalQuantityGreaterThanNamedQuery(Long minTotal) {
        return entityManager.createNamedQuery("SaleRecord.findProductNamesWithTotalQuantityGreaterThan")
                .setParameter("minTotal", minTotal)
                .getResultList();
    }

    public List<String> findProductNamesWithTotalQuantityGreaterThanCriteria(Long minTotal) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<String> query = cb.createQuery(String.class);
        Root<SaleRecord> root = query.from(SaleRecord.class);
        query.select(root.get("productName"));
        query.groupBy(root.get("productName"));
        query.having(cb.gt(cb.sum(root.get("quantity")), minTotal));
        return entityManager.createQuery(query).getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<String> findProductNamesWithTotalQuantityGreaterThanNative(Long minTotal) {
        return entityManager.createNativeQuery(
                        "SELECT product_name FROM sale_record GROUP BY product_name HAVING SUM(quantity) > ?1")
                .setParameter(1, minTotal)
                .getResultList();
    }

    public List<String> findProductNamesWithTotalQuantityGreaterThanJooq(Long minTotal) {
        return dsl.select(field("product_name"))
                .from(table("sale_record"))
                .groupBy(field("product_name"))
                .having(sum(field("quantity", Integer.class)).gt(BigDecimal.valueOf(minTotal)))
                .fetch()
                .map(r -> r.get("product_name", String.class));
    }

    public ProductAggregate findProductCountAndAvgPriceJpql() {
        Object[] row = (Object[]) entityManager.createQuery(
                        "SELECT COUNT(p), AVG(p.price) FROM Product p")
                .getSingleResult();
        long count = ((Number) row[0]).longValue();
        BigDecimal avgPrice = toBigDecimal(row[1]);
        return new ProductAggregate(count, avgPrice);
    }

    public ProductAggregate findProductCountAndAvgPriceNamedQuery() {
        Object[] row = (Object[]) entityManager.createNamedQuery("Product.findProductCountAndAvgPrice")
                .getSingleResult();
        long count = ((Number) row[0]).longValue();
        BigDecimal avgPrice = toBigDecimal(row[1]);
        return new ProductAggregate(count, avgPrice);
    }

    public ProductAggregate findProductCountAndAvgPriceCriteria() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<Product> root = query.from(Product.class);
        query.select(cb.array(cb.count(root), cb.avg(root.get("price"))));
        Object[] row = entityManager.createQuery(query).getSingleResult();
        long count = ((Number) row[0]).longValue();
        BigDecimal avgPrice = toBigDecimal(row[1]);
        return new ProductAggregate(count, avgPrice);
    }

    private static BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal bd) return bd;
        if (value instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        return BigDecimal.ZERO;
    }

    public ProductAggregate findProductCountAndAvgPriceNative() {
        Object[] row = (Object[]) entityManager.createNativeQuery(
                        "SELECT COUNT(*), AVG(price) FROM product")
                .getSingleResult();
        long count = ((Number) row[0]).longValue();
        BigDecimal avgPrice = toBigDecimal(row[1]);
        return new ProductAggregate(count, avgPrice);
    }

    public ProductAggregate findProductCountAndAvgPriceJooq() {
        var result = dsl.select(
                        field("count(*)").cast(Long.class),
                        field("avg(price)"))
                .from(table("product"))
                .fetchOne();
        long count = result != null && result.value1() != null ? result.value1() : 0L;
        BigDecimal avgPrice = result != null && result.value2() != null
                ? BigDecimal.valueOf(((Number) result.value2()).doubleValue()) : BigDecimal.ZERO;
        return new ProductAggregate(count, avgPrice);
    }

    public List<Product> findProductsWithSaleRecordsJpql() {
        return entityManager.createQuery(
                        "SELECT DISTINCT p FROM Product p JOIN SaleRecord s ON p.name = s.productName",
                        Product.class)
                .getResultList();
    }

    public List<Product> findProductsWithSaleRecordsNamedQuery() {
        return entityManager.createNamedQuery("Product.findProductsWithSaleRecords", Product.class)
                .getResultList();
    }

    public List<Product> findProductsWithSaleRecordsCriteria() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Product> query = cb.createQuery(Product.class);
        Root<Product> pRoot = query.from(Product.class);
        Root<SaleRecord> sRoot = query.from(SaleRecord.class);
        query.select(pRoot).distinct(true);
        query.where(cb.equal(pRoot.get("name"), sRoot.get("productName")));
        return entityManager.createQuery(query).getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Product> findProductsWithSaleRecordsNative() {
        List<Object[]> rows = entityManager.createNativeQuery(
                        "SELECT DISTINCT p.id, p.name, p.price FROM product p " +
                                "INNER JOIN sale_record s ON p.name = s.product_name")
                .getResultList();
        return rows.stream()
                .map(row -> Product.builder()
                        .id(((Number) row[0]).longValue())
                        .name((String) row[1])
                        .price(row[2] != null ? new BigDecimal(row[2].toString()) : null)
                        .build())
                .toList();
    }

    public List<Product> findProductsWithSaleRecordsJooq() {
        return dsl.selectDistinct(
                        field("p.id"),
                        field("p.name"),
                        field("p.price"))
                .from(table("product").as("p"))
                .innerJoin(table("sale_record").as("s"))
                .on(field("p.name").eq(field("s.product_name")))
                .fetch()
                .map(r -> Product.builder()
                        .id(r.get(0, Long.class))
                        .name(r.get(1, String.class))
                        .price(r.get(2) != null ? new BigDecimal(r.get(2).toString()) : null)
                        .build());
    }

    public List<Product> findProductsDynamicJpql(String nameSubstring, BigDecimal minPrice) {
        StringBuilder jpql = new StringBuilder("SELECT p FROM Product p WHERE 1=1");
        if (nameSubstring != null && !nameSubstring.isBlank()) {
            jpql.append(" AND p.name LIKE :name");
        }
        if (minPrice != null) {
            jpql.append(" AND p.price > :minPrice");
        }
        TypedQuery<Product> query = entityManager.createQuery(jpql.toString(), Product.class);
        if (nameSubstring != null && !nameSubstring.isBlank()) {
            query.setParameter("name", "%" + nameSubstring + "%");
        }
        if (minPrice != null) {
            query.setParameter("minPrice", minPrice);
        }
        return query.getResultList();
    }

    public List<Product> findProductsDynamicCriteria(String nameSubstring, BigDecimal minPrice) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Product> query = cb.createQuery(Product.class);
        Root<Product> root = query.from(Product.class);
        List<Predicate> predicates = new ArrayList<>();
        if (nameSubstring != null && !nameSubstring.isBlank()) {
            predicates.add(cb.like(root.get("name"), "%" + nameSubstring + "%"));
        }
        if (minPrice != null) {
            predicates.add(cb.greaterThan(root.get("price"), minPrice));
        }
        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(Predicate[]::new)));
        }
        return entityManager.createQuery(query).getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Product> findProductsDynamicNative(String nameSubstring, BigDecimal minPrice) {
        StringBuilder sql = new StringBuilder("SELECT id, name, price FROM product WHERE 1=1");
        List<Object> params = new ArrayList<>();
        int paramIndex = 1;
        if (nameSubstring != null && !nameSubstring.isBlank()) {
            sql.append(" AND name LIKE ?").append(paramIndex++);
            params.add("%" + nameSubstring + "%");
        }
        if (minPrice != null) {
            sql.append(" AND price > ?").append(paramIndex);
            params.add(minPrice);
        }
        var query = entityManager.createNativeQuery(sql.toString());
        for (int i = 0; i < params.size(); i++) {
            query.setParameter(i + 1, params.get(i));
        }
        List<Object[]> rows = query.getResultList();
        return rows.stream()
                .map(row -> Product.builder()
                        .id(((Number) row[0]).longValue())
                        .name((String) row[1])
                        .price(row[2] != null ? new BigDecimal(row[2].toString()) : null)
                        .build())
                .toList();
    }

    public List<Product> findProductsDynamicJooq(String nameSubstring, BigDecimal minPrice) {
        List<Condition> conditions = new ArrayList<>();
        if (nameSubstring != null && !nameSubstring.isBlank()) {
            conditions.add(field("name", String.class).like("%" + nameSubstring + "%"));
        }
        if (minPrice != null) {
            conditions.add(field("price").greaterThan(minPrice));
        }
        var fromProduct = dsl.selectFrom(table("product"));
        var withCondition = conditions.isEmpty()
                ? fromProduct
                : fromProduct.where(conditions.stream().reduce(Condition::and).orElseThrow());
        return withCondition.fetch()
                .map(r -> Product.builder()
                        .id(r.get("id", Long.class))
                        .name(r.get("name", String.class))
                        .price(r.get("price") != null ? new BigDecimal(Objects.requireNonNull(r.get("price")).toString()) : null)
                        .build());
    }
}
