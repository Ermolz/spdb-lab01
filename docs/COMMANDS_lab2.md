# Команди для запуску та перевірки — Lab02

Виконувати з кореня проекту `lab01`. На Windows: `.\gradlew.bat` замість `./gradlew`.

---

## 1. База даних

```bash
docker compose up -d
```

Зупинити: `docker compose down`

---

## 2. Запуск застосунку

Перед цим БД має бути запущена.

```bash
./gradlew bootRun
```

**Windows (PowerShell):**
```powershell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/lab01"
$env:SPRING_DATASOURCE_USERNAME="postgres"
$env:SPRING_DATASOURCE_PASSWORD="postgres"
.\gradlew.bat bootRun
```

**Linux/macOS:**
```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/lab01
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=postgres
./gradlew bootRun
```

API: `http://localhost:8080/api/products`

---

## 3. Тести (Lab02)

Потрібен **Docker** (Testcontainers).

**Усі тести:**
```bash
./gradlew test
```

**Flyway + validate (профіль flyway-test, міграції в static-блоці):**
```bash
./gradlew test --tests "com.kaerna.lab01.FlywayMigrationTest"
```

**EntityManager (persist, find, remove, merge, refresh, detach):**
```bash
./gradlew test --tests "com.kaerna.lab01.ProductEntityManagerDataAccessTest"
```

**Збірка без тестів:** `./gradlew build -x test`  
**Детальний вивід:** `./gradlew test --info`  
**Звіт:** `build/reports/tests/test/index.html`

---

## Швидка перевірка Lab02

1. `docker compose up -d`
2. `./gradlew test`
3. `./gradlew bootRun` (в іншому терміналі)
4. `docker compose down`
