package com.kaerna.lab01;

import com.kaerna.lab01.entity.Article;
import com.kaerna.lab01.entity.Book;
import com.kaerna.lab01.entity.Publication;
import com.kaerna.lab01.service.InheritanceDemoService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Transactional
class InheritanceDemoTest {

    @Autowired
    EntityManager entityManager;

    @Autowired
    InheritanceDemoService inheritanceDemoService;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Test
    void nativeSql_listsPublicationBookArticleTables() {
        List<String> tables = inheritanceDemoService.getPublicationTableNames();
        assertThat(tables).containsExactlyInAnyOrder("article", "book", "publication");
    }

    @Test
    void nativeSql_joinPublicationAndBook_returnsRows() {
        Book book = new Book();
        book.setTitle("Spring in Action");
        book.setIsbn("978-1617294945");
        book.setPageCount(500);
        entityManager.persist(book);
        entityManager.flush();
        entityManager.clear();

        List<Map<String, Object>> rows = inheritanceDemoService.getPublicationWithJoinedBooks();
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).get("title")).isEqualTo("Spring in Action");
        assertThat(rows.get(0).get("dtype")).isEqualTo("BOOK");
        assertThat(rows.get(0).get("isbn")).isEqualTo("978-1617294945");
        assertThat(rows.get(0).get("page_count")).isEqualTo(500);
    }

    @Test
    void nativeSql_joinPublicationAndArticle_returnsRows() {
        Article article = new Article();
        article.setTitle("JPA Inheritance");
        article.setJournal("Java Quarterly");
        article.setVolume(42);
        entityManager.persist(article);
        entityManager.flush();
        entityManager.clear();

        List<Map<String, Object>> rows = inheritanceDemoService.getPublicationWithJoinedArticles();
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).get("title")).isEqualTo("JPA Inheritance");
        assertThat(rows.get(0).get("dtype")).isEqualTo("ARTICLE");
        assertThat(rows.get(0).get("journal")).isEqualTo("Java Quarterly");
        assertThat(rows.get(0).get("volume")).isEqualTo(42);
    }
}
