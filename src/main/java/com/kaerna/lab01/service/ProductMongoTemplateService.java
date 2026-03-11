package com.kaerna.lab01.service;

import com.kaerna.lab01.document.ProductDoc;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;

@Service
public class ProductMongoTemplateService {

    private static final String PRODUCTS_COLLECTION = "products";
    private static final String SALE_RECORDS_COLLECTION = "sale_records";

    private final MongoTemplate mongoTemplate;

    public ProductMongoTemplateService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public List<ProductDoc> findByName(String name) {
        Query query = Query.query(Criteria.where("name").is(name));
        return mongoTemplate.find(query, ProductDoc.class, PRODUCTS_COLLECTION);
    }

    public List<ProductDoc> findTopByPriceDesc(int limit) {
        Query query = new Query().with(Sort.by(Sort.Direction.DESC, "price")).limit(limit);
        return mongoTemplate.find(query, ProductDoc.class, PRODUCTS_COLLECTION);
    }

    public List<Map> sumQuantityByProductName() {
        Aggregation aggregation = Aggregation.newAggregation(
                group("productName").sum("quantity").as("totalQuantity"),
                project("totalQuantity").and("_id").as("productName")
        );
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, SALE_RECORDS_COLLECTION, Map.class);
        return results.getMappedResults();
    }
}
