package com.kaerna.lab01.repository;

import com.kaerna.lab01.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
