# Звіт з виконання лабораторної роботи Lab05

## 1) Кластер БД і синхронність даних між вузлами

**Розташування:**
- Docker Compose: [docker-compose.lab05.yml](../docker-compose.lab05.yml) — сервіси `postgres-primary` (порт 5432) та `postgres-replica` (порт 5433).
- Ініціалізація реплікації: [docker/lab05/00_init.sql](../docker/lab05/00_init.sql) — користувач `replicator` та фізичний replication slot.

**Деталі:** Primary з `listen_addresses=*`, `port=5432`, `wal_level=replica`; реплікація асинхронна (без `synchronous_standby_names`), щоб уникнути блокування при старті, коли replica ще не підключена. Replica стартує через `pg_basebackup` з primary, потім postgres у standby. У 00_init.sql також умовне створення БД `lab01`. Синхронність перевіряється в тесті: запис на primary, читання з replica (п.3); при асинхронній реплікації можлива невелика затримка.

---

## 2) AbstractRoutingDataSource і два DataSource (write / read-only, fallback)

**Розташування:**
- Роутер: [src/main/java/com/kaerna/lab01/config/lab05/ReadWriteRoutingDataSource.java](../src/main/java/com/kaerna/lab01/config/lab05/ReadWriteRoutingDataSource.java) — розширює `AbstractRoutingDataSource`, ключ з `DataSourceContextHolder`.
- Контекст: [src/main/java/com/kaerna/lab01/config/lab05/DataSourceContextHolder.java](../src/main/java/com/kaerna/lab01/config/lab05/DataSourceContextHolder.java), [DataSourceType.java](../src/main/java/com/kaerna/lab01/config/lab05/DataSourceType.java).
- Fallback: [src/main/java/com/kaerna/lab01/config/lab05/FallbackDataSource.java](../src/main/java/com/kaerna/lab01/config/lab05/FallbackDataSource.java) — при недоступності replica повертає з’єднання з primary.
- AOP: [src/main/java/com/kaerna/lab01/config/lab05/ReadWriteRoutingAspect.java](../src/main/java/com/kaerna/lab01/config/lab05/ReadWriteRoutingAspect.java) — перед виконанням методів з `@Transactional` встановлює ключ REPLICA для `readOnly=true`, інакше PRIMARY.
- Конфігурація: [src/main/java/com/kaerna/lab01/config/lab05/Lab05DataSourceConfig.java](../src/main/java/com/kaerna/lab01/config/lab05/Lab05DataSourceConfig.java) — профіль `lab05`, біни primary, replica (з fallback), routing DataSource як `@Primary`.
- Профіль: [src/main/resources/application-lab05.properties](../src/main/resources/application-lab05.properties) — `app.datasource.primary.*` та `app.datasource.replica.*`.

**Деталі:** Аспект має `@Order(Ordered.HIGHEST_PRECEDENCE)`, щоб ключ був встановлений до відкриття транзакції та отримання з’єднання. Replica в роутері — це `FallbackDataSource(replicaRaw, primary)`.

---

## 3) Демонстрація синхронності (п.1) в автоматизованих тестах

**Розташування:**
- Тест: [src/test/java/com/kaerna/lab01/ReplicationSyncTest.java](../src/test/java/com/kaerna/lab01/ReplicationSyncTest.java) — `writeOnPrimary_isVisibleOnReplica`.
- Сервіс: [src/main/java/com/kaerna/lab01/service/Lab05ReplicationService.java](../src/main/java/com/kaerna/lab01/service/Lab05ReplicationService.java) — `createProduct` (write), `findByNameReadOnly` (read-only).

**Деталі:** Тест активний лише якщо кластер доступний (`@EnabledIf("...isReplicationClusterUp")` — перевірка портів 5432, 5433). Перед запуском тесту потрібно підняти кластер: `docker compose -f docker-compose.lab05.yml up -d`. Тест пише продукт через primary, читає через read-only (replica) і перевіряє наявність даних.

---

## 4) Поведінка при недоступності пулу з’єднань Hikari

**Розташування:**
- Тест: [src/test/java/com/kaerna/lab01/PoolExhaustionTest.java](../src/test/java/com/kaerna/lab01/PoolExhaustionTest.java) — `whenPoolExhausted_newRequestBlocksThenTimesOut`.

**Деталі:** Primary DataSource з `maximum-pool-size=1` та `connection-timeout=2000`. Один поток тримає транзакцію 5 с (`holdConnection`), інший через 300 мс намагається виконати запис — отримує з’єднання з того ж пулу. Другий запит блокується і після таймауту отримує виняток із "timeout" у повідомленні.

---

## 5) Зміна максимальних з’єднань з боку БД і наслідки для застосунку

**Розташування:**
- Тест: [src/test/java/com/kaerna/lab01/MaxConnectionsTest.java](../src/test/java/com/kaerna/lab01/MaxConnectionsTest.java) — `whenDbMaxConnectionsExceeded_requestsFail`.

**Деталі:** Тест емулює обмеження з’єднань: пули primary/replica по 2 з’єднання, connection-timeout 3 с, п’ять потоків одночасно тримають транзакції. Частина потоків отримує помилку (таймаут або connection limit). Аналогічна поведінка виникає, коли на стороні БД знижують `max_connections` (наприклад, у docker-compose.lab05.yml або в конфігурації PostgreSQL): застосунок намагається відкрити більше з’єднань, ніж дозволяє БД, і отримує помилки. Див. COMMANDS_lab5.md щодо зміни `max_connections` вручну.

---

## 6) Основні визначення

- **Реплікація** — механізм копіювання даних з primary на один або кілька replica. У PostgreSQL streaming replication — передача WAL з primary на standby; replica застосовує зміни і залишається read-only.

- **DataSource** — абстракція Java (javax.sql.DataSource) для отримання з’єднань з БД. Spring і застосунок використовують DataSource для доступу до БД; реалізації (наприклад, HikariCP) керують пулом з’єднань.

- **Connection Pool (пул з’єднань)** — набір з’єднань до БД, що підтримуються відкритими і перевикористовуються. HikariCP обмежує їх кількість (`maximum-pool-size`) і час очікування з’єднання (`connection-timeout`). Якщо всі з’єднання зайняті, новий запит чекає або отримує помилку таймауту.

- **AbstractRoutingDataSource** — базовий клас Spring для DataSource, який вибирає один з кількох цільових DataSource за ключем (наприклад, primary/replica). Метод `determineCurrentLookupKey()` повертає поточний ключ (часто з ThreadLocal).

- **Primary / Replica** — primary приймає запис і реплікує зміни; replica зазвичай тільки для читання. Розподіл навантаження: запис — на primary, читання — на replica (з fallback на primary при недоступності replica).

- **Synchronous replication** — commit на primary не завершується, поки зміни не підтверджені replica (`synchronous_commit`, `synchronous_standby_names` у PostgreSQL), що гарантує консистентність при збоях.
