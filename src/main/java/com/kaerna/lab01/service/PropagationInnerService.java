package com.kaerna.lab01.service;

import com.kaerna.lab01.entity.Product;
import com.kaerna.lab01.repository.ProductRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
public class PropagationInnerService {

    private final ProductRepository productRepository;

    public PropagationInnerService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void saveWithRequired(String name, BigDecimal price) {
        productRepository.save(Product.builder().name(name).price(price).build());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveWithRequiresNew(String name, BigDecimal price) {
        productRepository.save(Product.builder().name(name).price(price).build());
    }

    @Transactional(propagation = Propagation.NESTED)
    public void saveWithNested(String name, BigDecimal price) {
        productRepository.save(Product.builder().name(name).price(price).build());
    }
}
