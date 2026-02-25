package com.kaerna.lab01.service;

import com.kaerna.lab01.entity.Product;
import com.kaerna.lab01.exception.Lab04CheckedException;
import com.kaerna.lab01.exception.Lab04NoRollbackException;
import com.kaerna.lab01.repository.ProductRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
public class TransactionRollbackService {

    private final ProductRepository productRepository;

    public TransactionRollbackService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(rollbackFor = Lab04CheckedException.class)
    public void saveAndThrowChecked(Product product) throws Lab04CheckedException {
        productRepository.save(product);
        throw new Lab04CheckedException("Lab04 checked");
    }

    @Transactional(noRollbackFor = Lab04NoRollbackException.class)
    public void saveAndThrowNoRollback(Product product) {
        productRepository.save(product);
        throw new Lab04NoRollbackException("Lab04 no rollback");
    }
}
