# Звіт з виконання лабораторної роботи Lab03

## 1) @Component клас AdvancedQueryService

**Розташування:** [src/main/java/com/kaerna/lab01/service/AdvancedQueryService.java](../src/main/java/com/kaerna/lab01/service/AdvancedQueryService.java)

**Деталі:** Клас позначено @Component. Використовує EntityManager для JPQL, NamedQuery, Criteria API, Native Query та DSLContext (JOOQ) для запитів JOOQ. Усі 29 методів груповані за типами: delete за умовою, update за умовою, агрегація в умові (HAVING), агрегація в результаті (projection), JOIN, динамічний запит (без NamedQuery, оскільки NamedQuery статичний).

---

## 2) Методи по категоріях

### 2.1 Видалення за умовою

Видалення Product з `price < maxPrice`:
- `deleteByPriceLessThanJpql(BigDecimal maxPrice)` — [AdvancedQueryService.java](../src/main/java/com/kaerna/lab01/service/AdvancedQueryService.java)
- `deleteByPriceLessThanNamedQuery(BigDecimal maxPrice)` — використовує @NamedQuery на Product
- `deleteByPriceLessThanCriteria(BigDecimal maxPrice)` — CriteriaDelete
- `deleteByPriceLessThanNative(BigDecimal maxPrice)` — native SQL DELETE
- `deleteByPriceLessThanJooq(BigDecimal maxPrice)` — JOOQ deleteFrom

### 2.2 Оновлення за умовою

Оновлення price для Product де `name LIKE '%substring%`:
- `updatePriceByNameContainsJpql(String substring, BigDecimal newPrice)`
- `updatePriceByNameContainsNamedQuery(String substring, BigDecimal newPrice)`
- `updatePriceByNameContainsCriteria(String substring, BigDecimal newPrice)`
- `updatePriceByNameContainsNative(String substring, BigDecimal newPrice)`
- `updatePriceByNameContainsJooq(String substring, BigDecimal newPrice)`

### 2.3 Агрегація в умові (HAVING)

Знаходження productName з SaleRecord, де `SUM(quantity) > minTotal`:
- `findProductNamesWithTotalQuantityGreaterThanJpql(Long minTotal)`
- `findProductNamesWithTotalQuantityGreaterThanNamedQuery(Long minTotal)`
- `findProductNamesWithTotalQuantityGreaterThanCriteria(Long minTotal)`
- `findProductNamesWithTotalQuantityGreaterThanNative(Long minTotal)`
- `findProductNamesWithTotalQuantityGreaterThanJooq(Long minTotal)`

### 2.4 Агрегація в результаті (projection)

COUNT і AVG(price) для Product:
- `findProductCountAndAvgPriceJpql()` — повертає ProductAggregate
- `findProductCountAndAvgPriceNamedQuery()`
- `findProductCountAndAvgPriceCriteria()`
- `findProductCountAndAvgPriceNative()`
- `findProductCountAndAvgPriceJooq()`

**Деталі:** ProductAggregate — record [src/main/java/com/kaerna/lab01/service/ProductAggregate.java](../src/main/java/com/kaerna/lab01/service/ProductAggregate.java). Native Query повертає Number для AVG — необхідно перетворення на BigDecimal.

### 2.5 Пошук з JOIN

Product JOIN SaleRecord ON product.name = sale_record.product_name (Products, що мають записи в sale_record):
- `findProductsWithSaleRecordsJpql()` — JPQL з JOIN ON (JPA 2.1+)
- `findProductsWithSaleRecordsNamedQuery()`
- `findProductsWithSaleRecordsCriteria()` — два Root, умова WHERE p.name = s.productName
- `findProductsWithSaleRecordsNative()` — ручне маппінгу Object[] на Product
- `findProductsWithSaleRecordsJooq()` — selectDistinct з innerJoin

**Деталі:** Між Product і SaleRecord немає FK; JOIN виконується за логічною умовою name = product_name.

### 2.6 Динамічний запит (без NamedQuery)

Опційні параметри: name (LIKE), price > minPrice. Реалізовано тільки JPQL, Criteria, Native, JOOQ:
- `findProductsDynamicJpql(String nameSubstring, BigDecimal minPrice)` — збір JPQL через StringBuilder
- `findProductsDynamicCriteria(String nameSubstring, BigDecimal minPrice)` — динамічні Predicate
- `findProductsDynamicNative(String nameSubstring, BigDecimal minPrice)` — динамічний SQL і параметри
- `findProductsDynamicJooq(String nameSubstring, BigDecimal minPrice)` — умови через stream().reduce(Condition::and)

---

## 3) NamedQuery на сутностях

**Розташування:**
- [src/main/java/com/kaerna/lab01/entity/Product.java](../src/main/java/com/kaerna/lab01/entity/Product.java) — @NamedQuery для delete, update, findProductCountAndAvgPrice, findProductsWithSaleRecords
- [src/main/java/com/kaerna/lab01/entity/SaleRecord.java](../src/main/java/com/kaerna/lab01/entity/SaleRecord.java) — @NamedQuery findProductNamesWithTotalQuantityGreaterThan

**Деталі:** JPQL DELETE/UPDATE підтримуються в @NamedQuery. HAVING у JPQL використовується у запиті на SaleRecord.

---

## 4) Додаткові компоненти

- **SaleRecordRepository:** [src/main/java/com/kaerna/lab01/repository/SaleRecordRepository.java](../src/main/java/com/kaerna/lab01/repository/SaleRecordRepository.java) — для setUp тестів
- **ProductRepository.findByName:** [src/main/java/com/kaerna/lab01/repository/ProductRepository.java](../src/main/java/com/kaerna/lab01/repository/ProductRepository.java) — використовується в тестах

---

## 5) Автоматизовані тести

**Розташування:** [src/test/java/com/kaerna/lab01/AdvancedQueryServiceTest.java](../src/test/java/com/kaerna/lab01/AdvancedQueryServiceTest.java)

**Деталі:** @SpringBootTest, @Import(TestcontainersConfiguration), @Transactional. ddl-auto=create-drop, flyway.enabled=false — схему створює Hibernate. entityManager.clear() після bulk update для коректної перевірки. @BeforeEach очищує product і sale_record. Для кожного з 29 методів — окремий тест з перевіркою результату (кількість видалених/оновлених, вміст списків, агрегати). Потрібен Docker для Testcontainers.

---

## 6) Основні визначення

- **JPQL (Java Persistence Query Language)** — об'єктно-орієнтована мова запитів JPA; оперує сутностями та їх полями, а не таблицями; незалежна від БД.

- **Named Query** — запит (JPQL або native), збережений під іменем на сутності через @NamedQuery/@NamedNativeQuery; викликається через createNamedQuery; переважно статичний.

- **Criteria API** — програмний (type-safe) спосіб побудови JPA-запитів через Java API; CriteriaBuilder, CriteriaQuery, Root, Predicate тощо; зручний для динамічних запитів.

- **Native Query** — звичайний SQL, виконуваний через createNativeQuery; залежить від діалекту БД; повертає Object[] або потребує результатного маппінгу.

- **JOOQ** — бібліотека type-safe SQL для Java; DSL для SELECT, INSERT, UPDATE, DELETE; підтримує агрегації, JOIN, HAVING; може використовувати codegen або ручні посилання на таблиці/колонки.

- **Projection** — частина SELECT, що визначає, які дані повертаються (наприклад, окремі поля, агрегати COUNT, AVG замість повних сутностей).

- **HAVING** — умова, що застосовується після GROUP BY для фільтрації груп; зазвичай містить агрегатні функції (SUM, COUNT тощо).

- **Dynamic query** — запит, структура якого формується в runtime залежно від наявності вхідних параметрів; Criteria API та JOOQ добре підходять; Named Query — ні, оскільки він статичний.
