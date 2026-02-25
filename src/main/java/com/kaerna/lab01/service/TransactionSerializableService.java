package com.kaerna.lab01.service;

import com.kaerna.lab01.entity.Product;
import com.kaerna.lab01.repository.ProductRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
public class TransactionSerializableService {

    private final ProductRepository productRepository;

    public TransactionSerializableService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void readIncrementAndSave(Long productId) {
        Product product = productRepository.findById(productId).orElseThrow();
        product.setPrice(product.getPrice().add(BigDecimal.ONE));
        productRepository.save(product);
    }
}
