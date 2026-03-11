package com.kaerna.lab01.repository;

import com.kaerna.lab01.document.ProductDoc;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductDocRepository extends MongoRepository<ProductDoc, String> {

    Optional<ProductDoc> findByName(String name);

    List<ProductDoc> findByPriceGreaterThan(BigDecimal price);

    List<ProductDoc> findByNameContainingIgnoreCase(String namePart);

    @Query("{ 'name': ?0 }")
    List<ProductDoc> findCustomByName(String name);

    @Query("{ 'price': { $gte: ?0, $lte: ?1 } }")
    List<ProductDoc> findCustomByPriceRange(BigDecimal min, BigDecimal max);

    @Query("{ 'name': { $regex: ?0, $options: 'i' } }")
    List<ProductDoc> findCustomByNameLike(String pattern);
}
