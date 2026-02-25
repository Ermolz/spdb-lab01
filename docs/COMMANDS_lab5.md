# Lab05 — кластер БД, роутинг DataSource, пул з’єднань

## Запуск кластера (primary + replica)

З кореня проєкту:

```bash
docker compose -f docker-compose.lab05.yml down -v
docker compose -f docker-compose.lab05.yml up -d
```

Після першого запуску або змін у compose обов’язково виконати `down -v`, щоб primary отримав чисту ініціалізацію (БД `lab01` створиться з `POSTGRES_DB`) та TCP (`listen_addresses=*`, `port=5432`). Зачекати 20–30 с, поки replica завершить pg_basebackup.

Перевірки після `up -d`:

- Наявність БД `lab01`:  
  `docker exec -it lab01-postgres-primary-1 psql -U postgres -d postgres -c "\l"`
- З’єднання з primary з контейнера replica:  
  `docker exec -it lab01-postgres-replica-1 pg_isready -h postgres-primary -p 5432 -U postgres -d postgres`  
  Очікується: `postgres-primary:5432 - accepting connections`.
- Якщо TCP не приймає — перевірити, що процес postgres отримав параметри:  
  `docker exec -it lab01-postgres-primary-1 ps aux`  
  У виводі має бути `listen_addresses=*`.

Primary — порт 5432, replica — 5433.

## Запуск застосунку з профілем lab05

Після підняття кластера:

```bash
./gradlew bootRun --args='--spring.profiles.active=lab05'
```

Підключення: primary — localhost:5432, replica — localhost:5433 (див. `application-lab05.properties`).

## Тести

Запуск **лише тестів Lab05** (без решти проєкту):

```bash
./gradlew lab05Test
```

Запускаються: `ReplicationSyncTest`, `RoutingDataSourceTest`, `ReplicaFallbackTest`, `PoolExhaustionTest`, `MaxConnectionsTest`. Для `ReplicationSyncTest` потрібен піднятий кластер (`docker compose -f docker-compose.lab05.yml up -d`); без нього тест буде пропущений (SKIPPED). Решта тестів використовують Testcontainers — потрібен Docker.

Альтернатива вручну:

```bash
./gradlew test --tests "com.kaerna.lab01.ReplicationSyncTest" --tests "com.kaerna.lab01.RoutingDataSourceTest" --tests "com.kaerna.lab01.ReplicaFallbackTest" --tests "com.kaerna.lab01.PoolExhaustionTest" --tests "com.kaerna.lab01.MaxConnectionsTest"
```

## Налаштування max_connections (п.5)

У тесті `MaxConnectionsTest` PostgreSQL запускається з `max_connections=3`. Для перевірки вручну можна змінити команду в `docker-compose.lab05.yml` для primary, наприклад:

```yaml
command:
  - postgres
  - -c
  - max_connections=5
  # ... інші -c
```

Або підключитися до контейнера і виконати в psql: `ALTER SYSTEM SET max_connections = 5;` та перезапустити інстанс. Наслідки: якщо сумарний розмір пулів застосунку перевищує `max_connections`, частина запитів отримає помилки з’єднання.
