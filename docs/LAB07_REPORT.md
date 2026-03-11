# Звіт з виконання лабораторної роботи Lab07

## 1) CRUD профілю користувача (JSON-стрічка)

**Розташування:**
- Репозиторій: [src/main/java/com/kaerna/lab01/redis/UserProfileRedisRepository.java](../src/main/java/com/kaerna/lab01/redis/UserProfileRedisRepository.java)
- Модель: [src/main/java/com/kaerna/lab01/redis/UserProfile.java](../src/main/java/com/kaerna/lab01/redis/UserProfile.java)
- Тести: [src/test/java/com/kaerna/lab01/UserProfileRedisRepositoryTest.java](../src/test/java/com/kaerna/lab01/UserProfileRedisRepositoryTest.java)

**Деталі:** Зберігання за схемою один ключ → одна JSON-стрічка: ключ `profile:{userId}`, значення — рядок JSON. Використовується **StringRedisTemplate** та **ValueOperations** (set, get, delete). Серіалізація через Jackson ObjectMapper. Для тестів — `@SpringBootTest`, Redis у Docker через Testcontainers (GenericContainer `redis:7-alpine`), параметри підключення через `@DynamicPropertySource`.

---

## 2) Rate limit за user id (Redis + TTL)

**Розташування:**
- Сервіс: [src/main/java/com/kaerna/lab01/redis/RateLimitService.java](../src/main/java/com/kaerna/lab01/redis/RateLimitService.java)
- Тести: [src/test/java/com/kaerna/lab01/RateLimitServiceTest.java](../src/test/java/com/kaerna/lab01/RateLimitServiceTest.java)

**Деталі:** Ключ `ratelimit:{userId}`. Атомарна схема: **INCR** ключа; якщо результат дорівнює **1**, встановлюється **EXPIRE** з заданим TTL. Так уникнуто гонок при паралельних запитах. Перевищення ліміту — якщо після INCR значення > N, запит відхиляється. У тестах перевіряється: до N запитів дозволено, (N+1)-й відхиляється; після закінчення TTL лічильник скидається.

---

## 3) CRUD бізнес-об'єкта (Redis Hash)

**Розташування:**
- Репозиторій: [src/main/java/com/kaerna/lab01/redis/BusinessEntityRedisRepository.java](../src/main/java/com/kaerna/lab01/redis/BusinessEntityRedisRepository.java)
- Модель: [src/main/java/com/kaerna/lab01/redis/BusinessEntity.java](../src/main/java/com/kaerna/lab01/redis/BusinessEntity.java)
- Тести: [src/test/java/com/kaerna/lab01/BusinessEntityRedisRepositoryTest.java](../src/test/java/com/kaerna/lab01/BusinessEntityRedisRepositoryTest.java)

**Деталі:** Ручний спосіб роботи з Redis через **RedisTemplate** (StringRedisTemplate) та **HashOperations**. Ключ `business:entity:{id}`, поля — field-value (String–String). Методи: save (putAll), findById (entries), deleteById (delete).

---

## 4) Часткове оновлення (patch)

**Розташування:**
- Той самий клас [BusinessEntityRedisRepository.java](../src/main/java/com/kaerna/lab01/redis/BusinessEntityRedisRepository.java) — метод `updatePartial(String id, Map<String, String> patch)`.
- Тести: у [BusinessEntityRedisRepositoryTest.java](../src/test/java/com/kaerna/lab01/BusinessEntityRedisRepositoryTest.java) — `updatePartial_updatesOnlyGivenFields`, `updatePartial_onNonExistent_createsOnlyThoseFields`.

**Деталі:** Для кожного ключа в `patch` викликається `hashOps.put(key, field, value)`. Якщо об'єкта немає, створюються лише зазначені поля (новий hash).

---

## 5) Redis Sentinel

**Розташування:**
- Docker Compose: [docker-compose.lab07-redis-sentinel.yml](../docker-compose.lab07-redis-sentinel.yml) — redis-master, redis-replica1, redis-replica2, redis-sentinel1..3.
- Конфіг Sentinel: [docker/lab07/sentinel.conf](../docker/lab07/sentinel.conf)
- Профіль Spring: [src/main/resources/application-sentinel.properties](../src/main/resources/application-sentinel.properties)

**Деталі:** Сценарій Sentinel винесено в окремий integration-профіль `sentinel`. Spring Data Redis підтримує Sentinel-конфігурацію окремо: `spring.data.redis.sentinel.master=mymaster`, `spring.data.redis.sentinel.nodes=localhost:26379,localhost:26380,localhost:26381`. Звичайні тести пп.1–4,6 працюють з одним Redis-контейнером (Testcontainers) без Sentinel. Демонстрація failover — покрокова інструкція в [COMMANDS_lab7.md](COMMANDS_lab7.md).

---

## 6) Мінімум 3 методи CrudRepository + 3 методи RedisTemplate

**Розташування:**
- Сущність: [src/main/java/com/kaerna/lab01/redis/CacheableEntity.java](../src/main/java/com/kaerna/lab01/redis/CacheableEntity.java) — `@RedisHash("cacheable")`, поле `@Id String id`.
- Репозиторій: [src/main/java/com/kaerna/lab01/repository/CacheableEntityRedisRepository.java](../src/main/java/com/kaerna/lab01/repository/CacheableEntityRedisRepository.java) — `CrudRepository<CacheableEntity, String>`.
- Тести: [src/test/java/com/kaerna/lab01/CacheableEntityRedisRepositoryTest.java](../src/test/java/com/kaerna/lab01/CacheableEntityRedisRepositoryTest.java) — викликаються та перевіряються **save**, **findById**, **deleteById** (та existsById).

**Деталі:** Два підходи: (1) ручна робота через RedisTemplate / HashOperations / ValueOperations (пп.1–3, 2); (2) декларативна робота через Spring Data Redis **CrudRepository** + **@RedisHash**. У тестах обов'язково викликаються щонайменше три методи репозиторію. RedisTemplate-методи, використані в проєкті: ValueOperations set/get (п.1), increment/get/expire (п.2), HashOperations putAll/entries/put/delete (п.3).

---

## Як запускати тести

- **Усі тести:** `.\gradlew test` (Windows) або `./gradlew test`.
- **Redis у тестах:** Testcontainers — GenericContainer з образом `redis:7-alpine`. Параметри підключення передаються через `@DynamicPropertySource`. Потрібен Docker.
- **Окремо тести Lab07:**
  - Профіль: `.\gradlew test --tests "com.kaerna.lab01.UserProfileRedisRepositoryTest"`
  - Rate limit: `.\gradlew test --tests "com.kaerna.lab01.RateLimitServiceTest"`
  - Бізнес-сущність і patch: `.\gradlew test --tests "com.kaerna.lab01.BusinessEntityRedisRepositoryTest"`
  - CrudRepository: `.\gradlew test --tests "com.kaerna.lab01.CacheableEntityRedisRepositoryTest"`
- **Sentinel:** окремий профіль `sentinel`, docker-compose вручну — див. [COMMANDS_lab7.md](COMMANDS_lab7.md).

---

## Основні визначення

- **Redis** — in-memory store даних (key-value), часто використовується як кеш або брокер повідомлень. Підтримує рядки, хеші, списки, множини тощо.

- **Ключ (key)** — унікальний ідентифікатор запису в Redis. Значення зберігається за ключем. У проєкті: `profile:{userId}`, `ratelimit:{userId}`, `business:entity:{id}`.

- **TTL (Time To Live)** — час життя ключа в секундах. Після закінчення TTL ключ автоматично видаляється. Команда Redis: `EXPIRE key seconds`.

- **Redis Hash** — структура даних Redis: один ключ зі множиною полів (field–value). Команди: HSET, HGET, HGETALL, HDEL. У Spring — HashOperations.

- **Sentinel** — компонент Redis для моніторингу master і replica та автоматичного виявлення відмов і перемикання (failover).

- **Master / Replica** — у режимі реплікації один вузол Redis є master (приймає запис), інші — replica (копії даних, за замовчуванням read-only). Sentinel моніторить master; при падінні master один з replica обирається новим master.

- **Failover** — автоматичне перемикання на новий master після виявлення відмови поточного master. Sentinel керує цим процесом.

- **CrudRepository (Spring Data Redis)** — інтерфейс репозиторію з методами save, findById, deleteById, count, existsById тощо. Працює з сущностями, позначеними `@RedisHash`; зберігає їх як Redis Hash під капотом.

- **RedisTemplate** — клас Spring Data Redis для програмного доступу до Redis (ValueOperations, HashOperations, ListOperations тощо). StringRedisTemplate — варіант з типізацією ключів і значень як String.
