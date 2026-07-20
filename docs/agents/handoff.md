# Handoff: Failing Integration/Service Tests

The repository tests compile and pass successfully (82/82 tests passing). However, running the full test suite (`mvnw test`) reveals failures in service/integration tests.

## 1. Failing Tests Overview

### `AuthorServiceIntegrationTest.shouldFindAuthorWithTypo` (Line 148)
* **Failure:** `java.lang.AssertionError: Expected size: 1 but was: 2`
* **Details:** The test query returned multiple authors when only one was expected. This might be due to dirty database state or incorrect setup data.

### `BookServiceIntegrationTest.shouldReturnAllLanguages` (Line 209)
* **Failure:** `java.lang.AssertionError`
  ```
  Expecting actual:
    [3L]
  to contain exactly (and in same order):
    [2L]
  ```
* **Details:** Expecting language count `[2L]` but received `[3L]`. This could be caused by database leakage or extra records.

### `LibraryBookServiceIntegrationTest.init` (Line 98)
* **Failure:** `DataIntegrityViolationException: could not execute statement [ERROR: duplicate key value violates unique constraint "users_email_uq"]`
* **Details:** Duplicate key error when inserting `User` with email `test@example.com`. Indicates that the database was not properly cleaned up before/during execution, or the test setup does not use unique emails per test class.

---

## Suggested Skills

* **diagnosing-bugs** (`C:\Users\dimam\.gemini\config\skills\diagnosing-bugs\SKILL.md`): Use this skill to isolate and debug database pollution or logic issues in these integration tests.
* **spring-boot-testing** (`C:\Users\dimam\.gemini\config\skills\spring-boot-testing\SKILL.md`): Review standard transaction boundaries and test cleanup setups for Spring Boot integration tests.
