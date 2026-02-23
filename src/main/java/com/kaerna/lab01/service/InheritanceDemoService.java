package com.kaerna.lab01.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InheritanceDemoService {

    private final JdbcTemplate jdbcTemplate;

    public List<String> getPublicationTableNames() {
        String sql = "SELECT table_name FROM information_schema.tables " +
                "WHERE table_schema = 'public' AND table_name IN ('publication', 'book', 'article') " +
                "ORDER BY table_name";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        return rows.stream()
                .map(r -> (String) r.get("table_name"))
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getPublicationWithJoinedBooks() {
        String sql = "SELECT p.id, p.title, p.dtype, b.isbn, b.page_count " +
                "FROM publication p LEFT JOIN book b ON p.id = b.id ORDER BY p.id";
        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> getPublicationWithJoinedArticles() {
        String sql = "SELECT p.id, p.title, p.dtype, a.journal, a.volume " +
                "FROM publication p LEFT JOIN article a ON p.id = a.id ORDER BY p.id";
        return jdbcTemplate.queryForList(sql);
    }
}
