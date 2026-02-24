package com.kaerna.lab01.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "product")
@NamedQuery(name = "Product.deleteByPriceLessThan", query = "DELETE FROM Product p WHERE p.price < :maxPrice")
@NamedQuery(name = "Product.updatePriceByNameContains", query = "UPDATE Product p SET p.price = :newPrice WHERE p.name LIKE :pattern")
@NamedQuery(name = "Product.findProductCountAndAvgPrice", query = "SELECT COUNT(p), AVG(p.price) FROM Product p")
@NamedQuery(name = "Product.findProductsWithSaleRecords", query = "SELECT DISTINCT p FROM Product p JOIN SaleRecord s ON p.name = s.productName")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private java.math.BigDecimal price;
}
