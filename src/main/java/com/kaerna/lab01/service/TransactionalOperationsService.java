package com.kaerna.lab01.service;

import com.kaerna.lab01.entity.Product;
import com.kaerna.lab01.repository.ProductRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
public class TransactionalOperationsService {

    private final ProductRepository productRepository;

    public TransactionalOperationsService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public Product createProduct(String name, BigDecimal price) {
        return productRepository.save(Product.builder().name(name).price(price).build());
    }

    @Transactional
    public Product updateProduct(Long id, String name, BigDecimal price) {
        Product product = productRepository.findById(id).orElseThrow();
        product.setName(name);
        product.setPrice(price);
        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}
