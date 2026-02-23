# Lab01

Spring Boot application with CRUD for the Product entity, JdbcTemplate usage, JPA inheritance (JOINED), and Testcontainers-based tests.

## Project structure

```
lab01/
├── build.gradle
├── src/
│   ├── main/
│   │   ├── java/com/kaerna/lab01/
│   │   │   ├── Lab01Application.java          # Application entry point
│   │   │   ├── controller/
│   │   │   │   └── ProductController.java     # REST API /api/products
│   │   │   ├── dto/
│   │   │   │   ├── ProductRequest.java
│   │   │   │   └── ProductResponse.java
│   │   │   ├── entity/
│   │   │   │   ├── Product.java               # Main CRUD entity
│   │   │   │   ├── Publication.java           # Base entity (JPA inheritance)
│   │   │   │   ├── Book.java
│   │   │   │   └── Article.java
│   │   │   ├── repository/
│   │   │   │   ├── ProductRepository.java     # JpaRepository
│   │   │   │   └── ProductJdbcRepository.java # JdbcTemplate queries
│   │   │   └── service/
│   │   │       ├── ProductService.java
│   │   │       └── InheritanceDemoService.java # Native SQL for table demo
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       ├── java/com/kaerna/lab01/
│       │   ├── TestcontainersConfiguration.java  # PostgreSQL container for tests
│       │   ├── Lab01ApplicationTests.java
│       │   ├── JdbcTemplateQueryTests.java        # JdbcTemplate tests (SELECT, INSERT, UPDATE, DELETE)
│       │   ├── TestcontainersIntegrationTests.java
│       │   ├── InheritanceDemoTest.java          # JPA inheritance + native SQL tests
│       │   ├── SchemaSqlTest.java                # Schema loaded from schema.sql (ddl-auto=none)
│       │   └── DataSqlTest.java                   # Data loaded from data.sql
│       └── resources/
│           ├── schema.sql
│           ├── data.sql
│           ├── application-schema-test.properties
│           └── application-data-test.properties
├── docs/
│   └── LAB01_REPORT.md
├── docker-compose.yml
└── postman/
    └── Lab01-Products-API.postman_collection.json
```

## Database (Docker)

Start PostgreSQL with Docker Compose (from the project root):

```bash
docker compose up -d
```

This runs Postgres 16 on port **5432** with database `lab01`, user `postgres`, password `postgres`. The app is configured to use these settings by default.

Stop the database:

```bash
docker compose down
```

## Running the application

A PostgreSQL database must be running (e.g. via `docker compose up -d`).

**Using Gradle (Linux/macOS — `./gradlew`, Windows — `.\gradlew.bat` or `./gradlew`):**

```bash
./gradlew bootRun
```

**With explicit database configuration** (when not using Testcontainers):

```bash
# Windows (PowerShell)
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/lab01"
$env:SPRING_DATASOURCE_USERNAME="postgres"
$env:SPRING_DATASOURCE_PASSWORD="postgres"
./gradlew bootRun

# Linux/macOS
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/lab01
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=postgres
./gradlew bootRun
```

By default the application listens on port **8080**. API base: `http://localhost:8080/api/products`.

## Running tests

**Docker** is required (tests use Testcontainers with PostgreSQL).

**All tests:**

```bash
./gradlew test
```

**Build without tests:**

```bash
./gradlew build -x test
```

**Run tests with detailed output:**

```bash
./gradlew test --info
```

**Test report:** after `./gradlew test`, open `build/reports/tests/test/index.html`.

## Postman

Collection for exercising the REST API with automated Postman tests:

- File: **`postman/Lab01-Products-API.postman_collection.json`**

Import the collection into Postman and run **Collection Runner**. The `baseUrl` variable defaults to `http://localhost:8080`; you can change it in the collection variables.

Endpoints:

| Method | URL | Description |
|--------|-----|-------------|
| GET | /api/products | List all products |
| GET | /api/products/:id | Get product by id |
| POST | /api/products | Create product (body: `name`, `price`) |
| PUT | /api/products/:id | Update product |
| DELETE | /api/products/:id | Delete product |
