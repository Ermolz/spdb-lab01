package com.kaerna.lab01.service;

import com.kaerna.lab01.entity.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class EntityManagerTransactionService {

    private final EntityManagerFactory entityManagerFactory;

    public EntityManagerTransactionService(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    public Product createProduct(String name, BigDecimal price) {
        EntityManager em = entityManagerFactory.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        try {
            Product product = Product.builder().name(name).price(price).build();
            em.persist(product);
            tx.commit();
            return product;
        } catch (Exception e) {
            tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public Product updateProduct(Long id, String name, BigDecimal price) {
        EntityManager em = entityManagerFactory.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        try {
            Product product = em.find(Product.class, id);
            if (product == null) {
                tx.rollback();
                throw new IllegalArgumentException("Product not found: " + id);
            }
            product.setName(name);
            product.setPrice(price);
            tx.commit();
            return product;
        } catch (Exception e) {
            tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void deleteProduct(Long id) {
        EntityManager em = entityManagerFactory.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        try {
            Product product = em.find(Product.class, id);
            if (product != null) {
                em.remove(product);
            }
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
