# Звіт з виконання лабораторної роботи Lab04

## 1) Rollback для checked exception

**Розташування:**
- Виняток: [src/main/java/com/kaerna/lab01/exception/Lab04CheckedException.java](../src/main/java/com/kaerna/lab01/exception/Lab04CheckedException.java)
- Сервіс: [src/main/java/com/kaerna/lab01/service/TransactionRollbackService.java](../src/main/java/com/kaerna/lab01/service/TransactionRollbackService.java) — метод `saveAndThrowChecked(Product)`
- Тест: [src/test/java/com/kaerna/lab01/TransactionRollbackTest.java](../src/test/java/com/kaerna/lab01/TransactionRollbackTest.java) — `saveAndThrowChecked_rollsBackTransaction`

**Деталі:** За замовчуванням Spring відкочує транзакцію лише при unchecked exception. Для checked exception потрібно вказати `@Transactional(rollbackFor = Lab04CheckedException.class)`. Тест без @Transactional на класі: сервіс відкриває власну транзакцію, при винятку вона відкочується; після entityManager.clear() перевіряється, що продукт не з’явився в БД.

---

## 2) Без rollback для runtime exception

**Розташування:**
- Виняток: [src/main/java/com/kaerna/lab01/exception/Lab04NoRollbackException.java](../src/main/java/com/kaerna/lab01/exception/Lab04NoRollbackException.java)
- Сервіс: той самий [TransactionRollbackService](../src/main/java/com/kaerna/lab01/service/TransactionRollbackService.java) — метод `saveAndThrowNoRollback(Product)`
- Тест: [TransactionRollbackTest](../src/test/java/com/kaerna/lab01/TransactionRollbackTest.java) — `saveAndThrowNoRollback_commitsTransaction`

**Деталі:** `@Transactional(noRollbackFor = Lab04NoRollbackException.class)` — при цьому винятку транзакція комітиться перед поширенням винятку. Тест перевіряє наявність продукту в БД після виклику.

---

## 3) Self-invocation: виклик @Transactional всередині класу не відкриває транзакцію

**Розташування:**
- Клас з self-invocation: [src/main/java/com/kaerna/lab01/service/TransactionSelfInvocationService.java](../src/main/java/com/kaerna/lab01/service/TransactionSelfInvocationService.java) — `caller(AtomicBoolean)` викликає `this.callee(...)`; `callee` позначено @Transactional
- Винесений компонент: [src/main/java/com/kaerna/lab01/service/TransactionCalleeService.java](../src/main/java/com/kaerna/lab01/service/TransactionCalleeService.java) — метод `callee(AtomicBoolean)` з @Transactional
- Тест: [src/test/java/com/kaerna/lab01/TransactionSelfInvocationTest.java](../src/test/java/com/kaerna/lab01/TransactionSelfInvocationTest.java)

**Деталі:** При виклику методу всередині того самого класу (this.callee()) проксі Spring не залучається, тому @Transactional на callee не виконується. Перевірка через `TransactionSynchronizationManager.isActualTransactionActive()` у callee; результат передається в тест через AtomicBoolean. Після винесення в окремий компонент виклик `transactionCalleeService.callee()` йде через проксі — транзакція відкривається, тест перевіряє активність транзакції та збереження продукту.

---

## 4) Serializable isolation та емуляція конфлікту

**Розташування:**
- Сервіс: [src/main/java/com/kaerna/lab01/service/TransactionSerializableService.java](../src/main/java/com/kaerna/lab01/service/TransactionSerializableService.java) — метод `readIncrementAndSave(Long)` з `@Transactional(isolation = Isolation.SERIALIZABLE)`
- Тест: [src/test/java/com/kaerna/lab01/TransactionSerializableTest.java](../src/test/java/com/kaerna/lab01/TransactionSerializableTest.java) — `twoConcurrentSerializableTransactions_oneSucceedsOneFailsOrBothRetry`

**Деталі:** Два потоки одночасно викликають метод для одного й того ж product id (читання, збільшення price на 1, збереження). При рівні SERIALIZABLE один виклик може завершитись успішно, інший — винятком serialization failure (PostgreSQL), або обидва успішно після retry. Тест перевіряє, що сумарно виконано два виклики і фінальне значення price в межах очікуваного діапазону.

---

## 5) Три транзакції через @Transactional

**Розташування:** [src/main/java/com/kaerna/lab01/service/TransactionalOperationsService.java](../src/main/java/com/kaerna/lab01/service/TransactionalOperationsService.java)

**Деталі:** Один @Component з трьома методами, кожен з @Transactional: `createProduct`, `updateProduct`, `deleteProduct`. Кожен виклик ззовні виконується в окремій транзакції. Перевірка в [TransactionOperationsTest](../src/test/java/com/kaerna/lab01/TransactionOperationsTest.java) — `transactionalOperationsService_createUpdateDelete`.

---

## 6) Три транзакції через TransactionTemplate

**Розташування:** [src/main/java/com/kaerna/lab01/service/TransactionTemplateOperationsService.java](../src/main/java/com/kaerna/lab01/service/TransactionTemplateOperationsService.java)

**Деталі:** Інжектується TransactionTemplate; три методи виконують логіку в `transactionTemplate.execute(...)` або `executeWithoutResult(...)`. Транзакція керується програмно в межах callback. Тест: `transactionTemplateOperationsService_createUpdateDelete` у [TransactionOperationsTest](../src/test/java/com/kaerna/lab01/TransactionOperationsTest.java).

---

## 7) Три транзакції через EntityManager

**Розташування:** [src/main/java/com/kaerna/lab01/service/EntityManagerTransactionService.java](../src/main/java/com/kaerna/lab01/service/EntityManagerTransactionService.java)

**Деталі:** Використовується EntityManagerFactory: у кожному методі створюється власний EntityManager через `createEntityManager()`, далі `getTransaction().begin()`, операції (persist/find/merge/remove), commit/rollback, у `finally` — `em.close()`. Інжектований у Spring EntityManager не підтримує програмний getTransaction(), тому потрібен application-managed EM з фабрики. Тест: `entityManagerTransactionService_createUpdateDelete` у [TransactionOperationsTest](../src/test/java/com/kaerna/lab01/TransactionOperationsTest.java).

---

## 8) Три рівні пропагації

**Розташування:**
- Зовнішній сервіс: [src/main/java/com/kaerna/lab01/service/PropagationOuterService.java](../src/main/java/com/kaerna/lab01/service/PropagationOuterService.java) — методи `outerThenInnerRequiredThenThrow`, `outerThenInnerRequiresNewThenThrow`, `outerThenInnerNestedThenThrow`
- Внутрішній сервіс: [src/main/java/com/kaerna/lab01/service/PropagationInnerService.java](../src/main/java/com/kaerna/lab01/service/PropagationInnerService.java) — методи з `Propagation.REQUIRED`, `REQUIRES_NEW`, `NESTED`
- Тест: [src/test/java/com/kaerna/lab01/PropagationTest.java](../src/test/java/com/kaerna/lab01/PropagationTest.java)

**Деталі:**
- **REQUIRED:** внутрішній метод приєднується до зовнішньої транзакції; після винятку в зовнішньому обидва запису відкочуються.
- **REQUIRES_NEW:** внутрішній метод виконується в новій транзакції, яка комітиться до винятку зовні; продукт із внутрішнього методу залишається в БД.
- **NESTED:** внутрішній метод у вкладеній транзакції (savepoint); при rollback зовнішньої відкочуються й зміни внутрішнього.

---

## 9) Основні визначення

- **Propagation** — правило участі методу в транзакції: REQUIRED (приєднатися або створити), REQUIRES_NEW (завжди нова транзакція), NESTED (вкладена через savepoint), SUPPORTS, NOT_SUPPORTED, MANDATORY, NEVER.

- **Isolation Level** — рівень ізоляції транзакції: READ_UNCOMMITTED, READ_COMMITTED, REPEATABLE_READ, SERIALIZABLE. SERIALIZABLE гарантує серіалізоване виконання; при конфлікті БД може повертати помилку serialization failure.

- **ACID** — Atomicity (все або нічого), Consistency (стан БД залишається коректним), Isolation (паралельні транзакції не впливають одна на одну неправильно), Durability (закомічені зміни зберігаються).

- **rollbackFor** — у @Transactional: перелік винятків (зокрема checked), при яких транзакція відкочується.

- **noRollbackFor** — у @Transactional: перелік винятків, при яких транзакція не відкочується (зміни комітяться).

- **Self-invocation** — виклик методу з @Transactional з іншого методу того самого біна (this.method()) обходить проксі Spring, тому нова транзакція не створюється. Потрібне винесення в окремий бін і виклик через нього.

- **TransactionTemplate** — програмний спосіб виконання коду в транзакції через callback (execute / executeWithoutResult); зручно для імперативного коду без анотацій.

- **EntityManager.getTransaction()** — програмне керування JPA-транзакцією (begin, commit, rollback); використовується при resource-local persistence без контейнерних транзакцій або в спеціальних сценаріях.
