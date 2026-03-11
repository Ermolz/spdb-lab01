# Звіт з виконання лабораторної роботи Lab06

## 1) Унікальний індекс для поля, що не є PK

**Розташування:**
- Міграція Flyway: [src/main/resources/db/migration/V4__add_unique_index_product_name.sql](../src/main/resources/db/migration/V4__add_unique_index_product_name.sql) — `CREATE UNIQUE INDEX uk_product_name ON product(name);`
- Сущність Product без дублювання індексу в JPA: [src/main/java/com/kaerna/lab01/entity/Product.java](../src/main/java/com/kaerna/lab01/entity/Product.java) — індекс створюється лише через Flyway.

**Деталі:** Ім'я індексу фіксоване `uk_product_name` для перевірки в тесті п.3. У класі Product не використовується `@Table(indexes = ...)`, щоб уникнути конфлікту між Hibernate і Flyway.

---

## 2) Автоматизований тест: дубль значення неможливо додати

**Розташування:**
- Тест: [src/test/java/com/kaerna/lab01/ProductUniqueIndexTest.java](../src/test/java/com/kaerna/lab01/ProductUniqueIndexTest.java) — метод `savingSecondProductWithSameName_throwsDataIntegrityViolationException`.

**Деталі:** Використовується `saveAndFlush()` для обох збережень: перший продукт з даним `name` зберігається і flush виконується; при збереженні другого з тим самим `name` очікується виняток `DataIntegrityViolationException`. Це гарантує, що порушення унікальності виникає саме при flush, а не лише при commit.

---

## 3) Автоматизований тест: індекс існує в БД

**Розташування:**
- Тест: той самий клас [ProductUniqueIndexTest.java](../src/test/java/com/kaerna/lab01/ProductUniqueIndexTest.java) — метод `uniqueIndexUkProductName_existsInDatabase`.

**Деталі:** Перевірка виконується за точним іменем індексу: запит до `pg_indexes` з умовою `tablename = 'product' AND indexname = 'uk_product_name'`. Assert — що кількість знайдених рядків ≥ 1.

---

## 4) Три класи — об'єктна модель MongoDB

**Розташування:**
- Пакет: [src/main/java/com/kaerna/lab01/document/](../src/main/java/com/kaerna/lab01/document/)
- Класи:
  - [ProductDoc.java](../src/main/java/com/kaerna/lab01/document/ProductDoc.java) — id (String), name, price; для `price` (BigDecimal) використано `@Field(targetType = FieldType.DECIMAL128)`.
  - [SaleRecordDoc.java](../src/main/java/com/kaerna/lab01/document/SaleRecordDoc.java) — id, productName, quantity.
  - [ProductSummaryDoc.java](../src/main/java/com/kaerna/lab01/document/ProductSummaryDoc.java) — id, productName, totalQuantity.

**Деталі:** Колекції: `products`, `sale_records`, `product_summaries`. БД MongoDB у тестах — з URI, що передається через Testcontainers (наприклад, база з іменем у URI).

---

## 5) Три запити за назвою методу (derived) + тести

**Розташування:**
- Репозиторій: [src/main/java/com/kaerna/lab01/repository/ProductDocRepository.java](../src/main/java/com/kaerna/lab01/repository/ProductDocRepository.java)
- Методи: `findByName(String name)`, `findByPriceGreaterThan(BigDecimal price)`, `findByNameContainingIgnoreCase(String namePart)`.
- Тести: [src/test/java/com/kaerna/lab01/ProductDocRepositoryTest.java](../src/test/java/com/kaerna/lab01/ProductDocRepositoryTest.java) — методи `findByName_returnsSavedDocument`, `findByPriceGreaterThan_returnsMatchingDocuments`, `findByNameContainingIgnoreCase_returnsMatchingDocuments`.

**Деталі:** Тести запускаються з `@SpringBootTest`, PostgreSQL та MongoDB — через Testcontainers; URI до MongoDB задається через `@DynamicPropertySource`.

---

## 6) Три запити з @Query + тести

**Розташування:**
- Той самий репозиторій [ProductDocRepository.java](../src/main/java/com/kaerna/lab01/repository/ProductDocRepository.java): `findCustomByName`, `findCustomByPriceRange`, `findCustomByNameLike`.
- Приклади: точне поле `name`, діапазон `$gte`/`$lte` для price, пошук по частині рядка через `$regex` з `$options: 'i'`.
- Тести: у [ProductDocRepositoryTest.java](../src/test/java/com/kaerna/lab01/ProductDocRepositoryTest.java) — `findCustomByName_returnsMatchingDocuments`, `findCustomByPriceRange_returnsDocumentsInRange`, `findCustomByNameLike_returnsDocumentsMatchingRegex`.

---

## 7) Три запити через MongoTemplate + тести

**Розташування:**
- Сервіс: [src/main/java/com/kaerna/lab01/service/ProductMongoTemplateService.java](../src/main/java/com/kaerna/lab01/service/ProductMongoTemplateService.java)
- Методи: пошук по імені (`findByName`), пошук з сортуванням і лімітом (`findTopByPriceDesc`), агрегація по колекції `sale_records` — сума `quantity` по `productName` (`sumQuantityByProductName`).
- Тести: [src/test/java/com/kaerna/lab01/ProductMongoTemplateServiceTest.java](../src/test/java/com/kaerna/lab01/ProductMongoTemplateServiceTest.java).

**Деталі:** Агрегація використовує `Aggregation.group("productName").sum("quantity").as("totalQuantity")` та project для виводу productName і totalQuantity.

---

## Як запускати тести

- **Усі тести проєкту:** `./gradlew test` (або `.\gradlew test` у Windows).
- **PostgreSQL:** для тестів п.1–3 і для контексту Mongo-тестів використовується Testcontainers (контейнер `postgres:16-alpine`). Потрібен Docker.
- **MongoDB:** тести п.5–7 підключаються до **локального** MongoDB на `localhost:27017`, база `lab01_mongo`. Перед запуском ProductDocRepositoryTest і ProductMongoTemplateServiceTest потрібно запустити MongoDB (служба, `mongod` або `docker run -p 27017:27017 mongo:6`). URI можна змінити: `-Dmongo.uri=mongodb://localhost:27018/lab01_mongo`.
- **Окремо тести Lab06:**
  - Індекс і дубль: `./gradlew test --tests "com.kaerna.lab01.ProductUniqueIndexTest"`
  - Репозиторій MongoDB: `./gradlew test --tests "com.kaerna.lab01.ProductDocRepositoryTest"`
  - MongoTemplate: `./gradlew test --tests "com.kaerna.lab01.ProductMongoTemplateServiceTest"`

Детальніші команди та запуск MongoDB — у [COMMANDS_lab6.md](COMMANDS_lab6.md).

---

## Основні визначення

- **Індекс** — структура даних у БД для прискорення пошуку та/або забезпечення обмежень (наприклад, унікальність). У реляційних БД індекс будується по одній або кількох колонках.

- **Унікальний індекс** — індекс, що забороняє дублювання значень у індексованих колонках. У PostgreSQL створюється через `CREATE UNIQUE INDEX ... ON table(column)`.

- **MongoDB** — документоорієнтована NoSQL БД. Дані зберігаються у вигляді документів (зазвичай BSON/JSON) у колекціях.

- **Колекція** — аналог таблиці в MongoDB; група документів. У проєкті: `products`, `sale_records`, `product_summaries`.

- **Документ** — запис у колекції MongoDB; у Spring Data MongoDB відповідає класу з аннотацією `@Document(collection = "...")`.

- **MongoTemplate** — клас Spring Data MongoDB для виконання операцій (find, aggregate, insert тощо) з явним побудовою запитів і агрегацій.

- **Derived query** — метод репозиторію, ім'я якого визначає умову запиту (наприклад, `findByName` — пошук по полю `name`). Spring Data генерує запит за назвою методу.

- **@Query** — аннотація для визначення власного запиту (у MongoDB — JSON-подібний фільтр з плейсхолдерами `?0`, `?1` та операторами `$gte`, `$lte`, `$regex` тощо).
