package com.kaerna.lab01.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public Optional<Map<String, Object>> findById(Long id) {
        String sql = "SELECT id, name, price FROM product WHERE id = ?";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, id);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    public int insert(String name, BigDecimal price) {
        String sql = "INSERT INTO product (name, price) VALUES (?, ?)";
        return jdbcTemplate.update(sql, name, price);
    }

    public Long insertReturningId(String name, BigDecimal price) {
        String sql = "INSERT INTO product (name, price) VALUES (?, ?) RETURNING id";
        return jdbcTemplate.queryForObject(sql, Long.class, name, price);
    }

    public int updatePrice(Long id, BigDecimal price) {
        String sql = "UPDATE product SET price = ? WHERE id = ?";
        return jdbcTemplate.update(sql, price, id);
    }

    public int deleteById(Long id) {
        String sql = "DELETE FROM product WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }

    public long count() {
        String sql = "SELECT COUNT(*) FROM product";
        Long result = jdbcTemplate.queryForObject(sql, Long.class);
        return result != null ? result : 0L;
    }

    public List<Map<String, Object>> findAll() {
        String sql = "SELECT id, name, price FROM product ORDER BY id";
        return jdbcTemplate.queryForList(sql);
    }
}
