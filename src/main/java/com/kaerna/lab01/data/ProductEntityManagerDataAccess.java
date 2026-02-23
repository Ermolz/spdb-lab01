package com.kaerna.lab01.data;

import com.kaerna.lab01.entity.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;

@Component
public class ProductEntityManagerDataAccess {

    @PersistenceContext
    private EntityManager entityManager;

    public void persist(Product entity) {
        entityManager.persist(entity);
    }

    public void detach(Product entity) {
        entityManager.detach(entity);
    }

    public void remove(Product entity) {
        entityManager.remove(entity);
    }

    public void refresh(Product entity) {
        entityManager.refresh(entity);
    }

    public Product merge(Product entity) {
        return entityManager.merge(entity);
    }

    public Product find(Long id) {
        return entityManager.find(Product.class, id);
    }
}
