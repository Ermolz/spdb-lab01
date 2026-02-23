# Звіт з виконання лабораторної роботи Lab02

## 1) @Component з операціями EntityManager

**Розташування:** [src/main/java/com/kaerna/lab01/data/ProductEntityManagerDataAccess.java](../src/main/java/com/kaerna/lab01/data/ProductEntityManagerDataAccess.java)

**Деталі:** Клас позначено @Component, EntityManager інжектується через @PersistenceContext. Слугує шаром доступу до даних для сутності Product замість (або поряд із) JpaRepository. Усі зміни виконуються в транзакційному контексті (при виклику з сервісу з @Transactional або з тестів з @Transactional).

---

## 2) Один метод — одна операція EntityManager

**Розташування:** той самий клас [src/main/java/com/kaerna/lab01/data/ProductEntityManagerDataAccess.java](../src/main/java/com/kaerna/lab01/data/ProductEntityManagerDataAccess.java).

Методи відповідають операціям EM:
- **persist(Product)** — entityManager.persist(entity)
- **detach(Product)** — entityManager.detach(entity)
- **remove(Product)** — entityManager.remove(entity)
- **refresh(Product)** — entityManager.refresh(entity)
- **merge(Product)** — повертає entityManager.merge(entity)
- **find(Long id)** — entityManager.find(Product.class, id), повертає Product або null

---

## 3) Тести методів EntityManager

**Розташування:** [src/test/java/com/kaerna/lab01/ProductEntityManagerDataAccessTest.java](../src/test/java/com/kaerna/lab01/ProductEntityManagerDataAccessTest.java)

**Деталі:** @SpringBootTest, Testcontainers, @Transactional. Тести: persist_thenFind_returnsSavedEntity (після persist об’єкт знаходиться через find); find_nonExistentId_returnsNull; remove_thenFind_returnsNull; merge_detachedEntity_updatesInDb (detached сутність змінюють, merge — перевірка через find); refresh_syncsFromDb (зміна в БД через JdbcTemplate, refresh — сутність оновлена); detach_changeNotPersisted (після detach зміна поля не потрапляє в БД).

---

## 4) Flyway і ddl-auto=validate в тестах

**Розташування:** залежність у [build.gradle](../build.gradle) (flyway-core, flyway-database-postgresql); профіль [src/test/resources/application-flyway-test.properties](../src/test/resources/application-flyway-test.properties) з `spring.jpa.hibernate.ddl-auto=validate`; тест [src/test/java/com/kaerna/lab01/FlywayMigrationTest.java](../src/test/java/com/kaerna/lab01/FlywayMigrationTest.java) з @ActiveProfiles("flyway-test").

**Деталі:** У тесті контейнер PostgreSQL запускається в static-блоці, потім програмно викликається Flyway.migrate() до старту Spring-контексту, щоб схема вже існувала; контекст стартує з ddl-auto=validate і перевіряє відповідність сутностей схемі. У профілі spring.flyway.enabled=false, оскільки міграції виконуються вручну перед контекстом.

---

## 5) Три міграції Flyway

**Каталог:** [src/main/resources/db/migration/](../src/main/resources/db/migration/)

- **[src/main/resources/db/migration/V1__create_sale_item_and_data.sql](../src/main/resources/db/migration/V1__create_sale_item_and_data.sql)** — створює таблиці product, publication, book, article (для сумісності з validate), таблицю sale_item (id, product_name, quantity), наповнює sale_item даними (INSERT).
- **[src/main/resources/db/migration/V2__create_sale_summary_aggregate.sql](../src/main/resources/db/migration/V2__create_sale_summary_aggregate.sql)** — створює таблицю sale_summary (id, product_name, total_quantity), заповнює її агрегованими даними з sale_item (GROUP BY product_name, SUM(quantity)).
- **[src/main/resources/db/migration/V3__merge_tables_into_sale_record.sql](../src/main/resources/db/migration/V3__merge_tables_into_sale_record.sql)** — створює таблицю sale_record (id, product_name, quantity, total_quantity), переносить дані з sale_item та sale_summary у sale_record, видаляє таблиці sale_summary та sale_item.

**Сутність:** [src/main/java/com/kaerna/lab01/entity/SaleRecord.java](../src/main/java/com/kaerna/lab01/entity/SaleRecord.java) — маппінг на фінальну таблицю sale_record.

---

## 6) Основні визначення

- **Entity (JPA)** — клас, позначений @Entity, який відображається на таблицю реляційної БД; екземпляр відповідає рядку таблиці.

- **Persistence Context** — контекст збереження JPA: набір managed-сутностей, які EntityManager відстежує в межах транзакції; зміни над ними синхронізуються з БД при flush/commit.

- **EntityManager** — інтерфейс JPA для операцій над сутностями: persist (додати до контексту та зберегти), find (завантажити за id), merge (приєднати detached-сутність і зберегти зміни), remove (позначити на видалення), refresh (перечитати з БД), detach (виключити з контексту).

- **Міграції (Flyway)** — версійовані SQL-скрипти у db/migration (V1__name.sql, V2__…); при старті застосунку Flyway виконує ще не застосовані міграції і веде журнал у таблиці flyway_schema_history.

- **ddl-auto (Hibernate)** — режим оновлення схеми БД: none, validate, update, create, create-drop. validate — схема не змінюється, лише перевіряється відповідність сутностей існуючій схемі.

- **persist** — додає нову сутність до контексту і зберігає її в БД (INSERT).

- **merge** — приймає detached-сутність, копіює її стан у managed-копію і повертає її; зміни зберігаються при flush.

- **detach** — виключає сутність з контексту; подальші зміни над нею не відстежуються.

- **refresh** — перечитує стан сутності з БД у контекст.

- **remove** — позначає managed-сутність на видалення (DELETE при flush/commit).
