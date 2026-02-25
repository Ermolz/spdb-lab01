package com.kaerna.lab01.service;

import com.kaerna.lab01.entity.Product;
import com.kaerna.lab01.repository.ProductRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@Profile("lab05")
public class Lab05ReplicationService {

    private final ProductRepository productRepository;

    public Lab05ReplicationService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public Product createProduct(String name, BigDecimal price) {
        return productRepository.save(Product.builder().name(name).price(price).build());
    }

    @Transactional(readOnly = true)
    public Optional<Product> findByNameReadOnly(String name) {
        return productRepository.findByName(name);
    }

    @Transactional
    public void holdConnection(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
