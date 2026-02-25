# Команди для запуску та перевірки — Lab04

Виконувати з кореня проекту `lab01`. На Windows: `.\gradlew.bat` замість `./gradlew`.

---

## Передумови

Docker (Testcontainers). Без Docker тести Lab04 з `@EnabledIfDockerAvailable` пропускаються (SKIPPED), збірка завершується успішно.

---

## Тести Lab04

**Усі тести проекту:**
```bash
./gradlew test
```

**Тільки тести Lab04 (транзакції):**
```bash
./gradlew test --tests "com.kaerna.lab01.TransactionRollbackTest" --tests "com.kaerna.lab01.TransactionSelfInvocationTest" --tests "com.kaerna.lab01.TransactionSerializableTest" --tests "com.kaerna.lab01.TransactionOperationsTest" --tests "com.kaerna.lab01.PropagationTest"
```

**Окремі групи тестів:**

| Що перевіряє | Команда |
|--------------|--------|
| Rollback для checked exception, без rollback для runtime | `./gradlew test --tests "com.kaerna.lab01.TransactionRollbackTest"` |
| Self-invocation: транзакція не відкривається при виклику всередині класу; після винесення — відкривається | `./gradlew test --tests "com.kaerna.lab01.TransactionSelfInvocationTest"` |
| Рівень ізоляції Serializable, емуляція конфлікту двох транзакцій | `./gradlew test --tests "com.kaerna.lab01.TransactionSerializableTest"` |
| Три способи транзакцій: @Transactional, TransactionTemplate, EntityManager | `./gradlew test --tests "com.kaerna.lab01.TransactionOperationsTest"` |
| Три рівні пропагації: REQUIRED, REQUIRES_NEW, NESTED | `./gradlew test --tests "com.kaerna.lab01.PropagationTest"` |

**Windows (PowerShell):** `.\gradlew.bat` замість `./gradlew`.

---

## Додатково

**Збірка без тестів:** `./gradlew build -x test`  
**Детальний вивід тестів:** `./gradlew test --info`  
**Звіт тестів:** `build/reports/tests/test/index.html`

