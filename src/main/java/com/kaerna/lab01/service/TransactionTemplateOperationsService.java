package com.kaerna.lab01.service;

import com.kaerna.lab01.entity.Product;
import com.kaerna.lab01.repository.ProductRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;

@Component
public class TransactionTemplateOperationsService {

    private final ProductRepository productRepository;
    private final TransactionTemplate transactionTemplate;

    public TransactionTemplateOperationsService(ProductRepository productRepository,
                                               TransactionTemplate transactionTemplate) {
        this.productRepository = productRepository;
        this.transactionTemplate = transactionTemplate;
    }

    public Product createProduct(String name, BigDecimal price) {
        return transactionTemplate.execute(status ->
                productRepository.save(Product.builder().name(name).price(price).build()));
    }

    public Product updateProduct(Long id, String name, BigDecimal price) {
        return transactionTemplate.execute(status -> {
            Product product = productRepository.findById(id).orElseThrow();
            product.setName(name);
            product.setPrice(price);
            return productRepository.save(product);
        });
    }

    public void deleteProduct(Long id) {
        transactionTemplate.executeWithoutResult(status -> productRepository.deleteById(id));
    }
}
