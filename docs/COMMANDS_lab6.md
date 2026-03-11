# Lab06 — індекс унікальності (JPA) та MongoDB

## Що потрібно перед тестами

- **PostgreSQL** — піднімається через Testcontainers (потрібен Docker тільки для цих контейнерів).
- **MongoDB** — має бути **запущений локально** на порту 27017. Тести підключаються до `mongodb://localhost:27017/lab01_mongo`.

### Як запустити MongoDB локально

**Варіант 1 — установлений MongoDB:**

```bash
# Windows (як служба) або
mongod

# Linux/macOS
sudo systemctl start mongod
# або
mongod --dbpath /path/to/data
```

**Варіант 2 — один контейнер без Testcontainers:**

```bash
docker run -d --name mongo-lab01 -p 27017:27017 mongo:6
```

Після старту MongoDB тести підключаються до бази `lab01_mongo` (вона створиться автоматично).

Інший хост/порт — передай URI при запуску тестів:

```bash
./gradlew test -Dmongo.uri=mongodb://localhost:27018/lab01_mongo --tests "com.kaerna.lab01.ProductDocRepositoryTest"
```

## Запуск тестів Lab06

Індекс і перевірка дубля (лише Postgres Testcontainers):

```bash
./gradlew test --tests "com.kaerna.lab01.ProductUniqueIndexTest"
```

MongoDB-тести (потрібен локальний Mongo на 27017):

```bash
./gradlew test --tests "com.kaerna.lab01.ProductDocRepositoryTest"
./gradlew test --tests "com.kaerna.lab01.ProductMongoTemplateServiceTest"
```

Усі Mongo-тести разом:

```bash
./gradlew test --tests "com.kaerna.lab01.ProductDocRepositoryTest" --tests "com.kaerna.lab01.ProductMongoTemplateServiceTest"
```

Усі тести Lab06 (Postgres + Mongo):

```bash
./gradlew test --tests "com.kaerna.lab01.ProductUniqueIndexTest" --tests "com.kaerna.lab01.ProductDocRepositoryTest" --tests "com.kaerna.lab01.ProductMongoTemplateServiceTest"
```

## Запуск застосунку з MongoDB

```bash
./gradlew bootRun --args='--spring.profiles.active=mongo'
```

У профілі `application-mongo.properties` (або в `application.properties`):  
`spring.data.mongodb.uri=mongodb://localhost:27017/lab01_mongo`
