# Звіт з виконання лабораторної роботи Lab01

## 1) CRUD для сутності

**Розташування:**
- Entity: [src/main/java/com/kaerna/lab01/entity/Product.java](../src/main/java/com/kaerna/lab01/entity/Product.java)
- Repository: [src/main/java/com/kaerna/lab01/repository/ProductRepository.java](../src/main/java/com/kaerna/lab01/repository/ProductRepository.java) (JpaRepository)
- DTO: [src/main/java/com/kaerna/lab01/dto/ProductRequest.java](../src/main/java/com/kaerna/lab01/dto/ProductRequest.java), [src/main/java/com/kaerna/lab01/dto/ProductResponse.java](../src/main/java/com/kaerna/lab01/dto/ProductResponse.java)
- Service: [src/main/java/com/kaerna/lab01/service/ProductService.java](../src/main/java/com/kaerna/lab01/service/ProductService.java)
- Controller: [src/main/java/com/kaerna/lab01/controller/ProductController.java](../src/main/java/com/kaerna/lab01/controller/ProductController.java)

**Деталі:** Обрано сутність Product (id, name, price). REST API: GET /api/products, GET /api/products/{id}, POST /api/products, PUT /api/products/{id}, DELETE /api/products/{id}. Валідація через Bean Validation у ProductRequest (name — не порожній, price — не null, ≥ 0).

---

## 2) Три запити через JdbcTemplate та тести

**Розташування:**
- Клас з JdbcTemplate: [src/main/java/com/kaerna/lab01/repository/ProductJdbcRepository.java](../src/main/java/com/kaerna/lab01/repository/ProductJdbcRepository.java)
- Тести: [src/test/java/com/kaerna/lab01/JdbcTemplateQueryTests.java](../src/test/java/com/kaerna/lab01/JdbcTemplateQueryTests.java)

**Деталі:** У ProductJdbcRepository реалізовано: SELECT (findById), INSERT (insert), UPDATE (updatePrice), DELETE (deleteById), COUNT (count). Тести використовують Testcontainers (PostgreSQL), перевіряють: select для неіснуючого id повертає empty; insert + count + findById повертають коректні дані; updatePrice змінює ціну; deleteById видаляє рядок. Три типи запитів (SELECT, INSERT/UPDATE, DELETE) покриті тестами.

---

## 3) Testcontainers — не менше двох тестів з контейнером БД

**Розташування:**
- Конфігурація: [src/test/java/com/kaerna/lab01/TestcontainersConfiguration.java](../src/test/java/com/kaerna/lab01/TestcontainersConfiguration.java)
- Інтеграційні тести: [src/test/java/com/kaerna/lab01/TestcontainersIntegrationTests.java](../src/test/java/com/kaerna/lab01/TestcontainersIntegrationTests.java)
- Додатково контейнер використовується в: [src/test/java/com/kaerna/lab01/Lab01ApplicationTests.java](../src/test/java/com/kaerna/lab01/Lab01ApplicationTests.java), [src/test/java/com/kaerna/lab01/JdbcTemplateQueryTests.java](../src/test/java/com/kaerna/lab01/JdbcTemplateQueryTests.java), [src/test/java/com/kaerna/lab01/InheritanceDemoTest.java](../src/test/java/com/kaerna/lab01/InheritanceDemoTest.java), [src/test/java/com/kaerna/lab01/SchemaSqlTest.java](../src/test/java/com/kaerna/lab01/SchemaSqlTest.java), [src/test/java/com/kaerna/lab01/DataSqlTest.java](../src/test/java/com/kaerna/lab01/DataSqlTest.java)

**Деталі:** TestcontainersConfiguration оголошує PostgreSQLContainer з @ServiceConnection (Spring Boot 3.1+ підставляє URL до контейнера). У TestcontainersIntegrationTests два тести: jpaRepository_saveAndFindById_returnsSavedProduct (JPA збереження та пошук Product) та jdbcTemplate_insertAndFindById_returnsInsertedRow (JdbcTemplate insert і findById). Обидва працюють з контейнеризованою PostgreSQL. Для запуску тестів із Testcontainers потрібен запущений Docker.

---

## 4) JPA Inheritance та нативний SQL для демонстрації таблиць

**Розташування:**
- Базова сутність: [src/main/java/com/kaerna/lab01/entity/Publication.java](../src/main/java/com/kaerna/lab01/entity/Publication.java)
- Підтипи: [src/main/java/com/kaerna/lab01/entity/Book.java](../src/main/java/com/kaerna/lab01/entity/Book.java), [src/main/java/com/kaerna/lab01/entity/Article.java](../src/main/java/com/kaerna/lab01/entity/Article.java)
- Сервіс з нативними запитами: [src/main/java/com/kaerna/lab01/service/InheritanceDemoService.java](../src/main/java/com/kaerna/lab01/service/InheritanceDemoService.java)
- Тести: [src/test/java/com/kaerna/lab01/InheritanceDemoTest.java](../src/test/java/com/kaerna/lab01/InheritanceDemoTest.java)

**Деталі:** Обрано стратегію JOINED: таблиця publication (id, title, dtype), book (id FK → publication.id, isbn, page_count), article (id FK → publication.id, journal, volume). InheritanceDemoService через JdbcTemplate виконує нативні SQL: getPublicationTableNames() — перелік таблиць publication, book, article з information_schema; getPublicationWithJoinedBooks() та getPublicationWithJoinedArticles() — JOIN базової таблиці з дочірніми. Тести перевіряють наявність трьох таблиць і коректність даних після persist Book/Article.

---

## 5) Тест із завантаженням схеми БД з SQL-файлу (без ddl-auto)

**Розташування:**
- SQL-схема: [src/test/resources/schema.sql](../src/test/resources/schema.sql)
- Профіль: [src/test/resources/application-schema-test.properties](../src/test/resources/application-schema-test.properties)
- Тест: [src/test/java/com/kaerna/lab01/SchemaSqlTest.java](../src/test/java/com/kaerna/lab01/SchemaSqlTest.java)

**Деталі:** У профілі schema-test встановлено spring.jpa.hibernate.ddl-auto=none, spring.sql.init.mode=always, spring.sql.init.schema-locations=classpath:schema.sql. Схема не створюється Hibernate — лише з schema.sql. У SchemaSqlTest використовується @ActiveProfiles("schema-test") та Testcontainers; тест перевіряє, що після завантаження схеми можна виконати INSERT через ProductJdbcRepository і знайти запис.

---

## 6) Тест із завантаженням даних з SQL-файлу

**Розташування:**
- Дані: [src/test/resources/data.sql](../src/test/resources/data.sql)
- Профіль: [src/test/resources/application-data-test.properties](../src/test/resources/application-data-test.properties)
- Тест: [src/test/java/com/kaerna/lab01/DataSqlTest.java](../src/test/java/com/kaerna/lab01/DataSqlTest.java)

**Деталі:** Профіль data-test має ddl-auto=none, schema.sql та data.sql (spring.sql.init.data-locations=classpath:data.sql). Спочатку виконується schema.sql, потім data.sql. У DataSqlTest перевіряється наявність двох записів із data.sql (Test Product A, Test Product B) через ProductJdbcRepository.findAll().

---

## 7) Основні визначення

- **Entity (JPA)** — клас, позначений @Entity, який відображається на таблицю реляційної БД; екземпляр відповідає рядку таблиці.

- **JPA (Java Persistence API)** — стандарт Java для ORM (Object-Relational Mapping): збереження, завантаження та оновлення об’єктів у реляційній БД через провайдерів (наприклад, Hibernate).

- **JdbcTemplate** — клас Spring для роботи з JDBC: виконує SQL запити, обробляє з’єднання та винятки, зменшує boilerplate код порівняно з чистим JDBC.

- **Testcontainers** — бібліотека для запуску сервісів (БД, черги тощо) у Docker-контейнерах під час тестів; забезпечує реальне середовище замість моків.

- **CRUD** — набір операцій: Create (створення), Read (читання), Update (оновлення), Delete (видалення) записів.

- **ddl-auto (Hibernate)** — режим оновлення схеми БД: none, validate, update, create, create-drop. none/validate — схема не змінюється; create-drop — створення при старті, видалення при завершенні.

- **Inheritance (JPA)** — відображення ієрархії класів на таблиці: SINGLE_TABLE (одна таблиця з дискримінатором), JOINED (базова таблиця + таблиці підтипів з FK), TABLE_PER_CLASS (окрема таблиця на кожен клас).
