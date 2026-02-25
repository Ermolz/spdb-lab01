package com.kaerna.lab01.service;

import com.kaerna.lab01.entity.Product;
import com.kaerna.lab01.repository.ProductRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
public class PropagationOuterService {

    private final ProductRepository productRepository;
    private final PropagationInnerService propagationInnerService;

    public PropagationOuterService(ProductRepository productRepository,
                                   PropagationInnerService propagationInnerService) {
        this.productRepository = productRepository;
        this.propagationInnerService = propagationInnerService;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void outerThenInnerRequiredThenThrow(String outerName, String innerName, BigDecimal price) {
        productRepository.save(Product.builder().name(outerName).price(price).build());
        propagationInnerService.saveWithRequired(innerName, price);
        throw new RuntimeException("outer rollback");
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void outerThenInnerRequiresNewThenThrow(String outerName, String innerName, BigDecimal price) {
        productRepository.save(Product.builder().name(outerName).price(price).build());
        propagationInnerService.saveWithRequiresNew(innerName, price);
        throw new RuntimeException("outer rollback");
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void outerThenInnerNestedThenThrow(String outerName, String innerName, BigDecimal price) {
        productRepository.save(Product.builder().name(outerName).price(price).build());
        propagationInnerService.saveWithNested(innerName, price);
        throw new RuntimeException("outer rollback");
    }
}
