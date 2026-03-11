# Lab07 — Redis (профіль, rate limit, Hash, Sentinel, звіт)

## Що потрібно для тестів

- **Docker** — для Testcontainers (Redis контейнер `redis:7-alpine` піднімається автоматично в тестах пп.1–4, 6).
- **PostgreSQL і MongoDB** — тести використовують повний контекст застосунку; потрібні Postgres (Testcontainers) і MongoDB (локально або через змінну `mongo.uri`), як у Lab06.

## Запуск тестів Lab07

Усі Redis-тести (профіль, rate limit, бізнес-сущність, CrudRepository) підключаються до Redis у Docker через Testcontainers. Додатково використовуються Postgres (Testcontainers) та MongoDB (згідно з Lab06).

```bash
.\gradlew test --tests "com.kaerna.lab01.UserProfileRedisRepositoryTest"
.\gradlew test --tests "com.kaerna.lab01.RateLimitServiceTest"
.\gradlew test --tests "com.kaerna.lab01.BusinessEntityRedisRepositoryTest"
.\gradlew test --tests "com.kaerna.lab01.CacheableEntityRedisRepositoryTest"
```

Або всі тести проєкту:

```bash
.\gradlew test
```

## Redis локально (опційно)

Якщо потрібен один Redis для розробки без Testcontainers:

```bash
docker run -d --name redis-lab01 -p 6379:6379 redis:7-alpine
```

У `application.properties` (або профіль) можна задати:

```properties
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

## Redis Sentinel (окремий профіль)

Сценарій Sentinel — окремий integration-профіль. Потрібно підняти docker-compose з master, replica та sentinel-вузлами.

### 1. Запуск кластера Sentinel

У корені проєкту:

```bash
docker compose -f docker-compose.lab07-redis-sentinel.yml up -d
```

Сервіси: `redis-master` (порт 6379), `redis-replica1` (6380), `redis-replica2` (6381), `redis-sentinel1` (26379), `redis-sentinel2` (26380), `redis-sentinel3` (26381).

### 2. Перевірка Sentinel

Поточний master:

```bash
docker compose -f docker-compose.lab07-redis-sentinel.yml exec redis-sentinel1 redis-cli -p 26379 SENTINEL get-master-addr-by-name mymaster
```

### 3. Запуск застосунку з профілем sentinel

```bash
.\gradlew bootRun --args='--spring.profiles.active=sentinel'
```

Застосунок підключається до Redis через Sentinel (mymaster, localhost:26379,26380,26381).

### 4. Демонстрація failover (покроково)

1. Запустити compose і застосунок з профілем `sentinel`.
2. Виконати запис у Redis (наприклад, зберегти профіль через API або тестовий скрипт).
3. Зупинити master: `docker compose -f docker-compose.lab07-redis-sentinel.yml stop redis-master`
4. Зачекати 10–30 с — Sentinel вибере новий master з replica.
5. Перевірити новий master: `docker compose -f docker-compose.lab07-redis-sentinel.yml exec redis-sentinel1 redis-cli -p 26379 SENTINEL get-master-addr-by-name mymaster`
6. Повторити запис/читання — клієнт (Lettuce) отримає нову адресу master від Sentinel і підключиться до нього.

### 5. Зупинка compose

```bash
docker compose -f docker-compose.lab07-redis-sentinel.yml down
```
