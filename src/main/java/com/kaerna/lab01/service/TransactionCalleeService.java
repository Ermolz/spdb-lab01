package com.kaerna.lab01.service;

import com.kaerna.lab01.entity.Product;
import com.kaerna.lab01.repository.ProductRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class TransactionCalleeService {

    private final ProductRepository productRepository;

    public TransactionCalleeService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public void callee(AtomicBoolean transactionActive) {
        transactionActive.set(TransactionSynchronizationManager.isActualTransactionActive());
        productRepository.save(Product.builder().name("CalleeProduct").price(new BigDecimal("1")).build());
    }
}
