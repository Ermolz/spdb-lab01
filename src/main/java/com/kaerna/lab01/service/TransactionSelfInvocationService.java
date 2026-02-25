package com.kaerna.lab01.service;

import com.kaerna.lab01.entity.Product;
import com.kaerna.lab01.repository.ProductRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class TransactionSelfInvocationService {

    private final ProductRepository productRepository;
    private final TransactionCalleeService transactionCalleeService;

    public TransactionSelfInvocationService(ProductRepository productRepository,
                                            TransactionCalleeService transactionCalleeService) {
        this.productRepository = productRepository;
        this.transactionCalleeService = transactionCalleeService;
    }

    public void caller(AtomicBoolean transactionActiveInCallee) {
        this.callee(transactionActiveInCallee);
    }

    @Transactional
    public void callee(AtomicBoolean transactionActive) {
        transactionActive.set(TransactionSynchronizationManager.isActualTransactionActive());
        productRepository.save(Product.builder().name("SelfInvocationProduct").price(new BigDecimal("1")).build());
    }

    public void callerViaDelegate(AtomicBoolean transactionActiveInCallee) {
        transactionCalleeService.callee(transactionActiveInCallee);
    }
}
