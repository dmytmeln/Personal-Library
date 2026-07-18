# Library API

Spring Boot backend for Personal Library. Provides authentication, book and author management, collections, notes,
quotes, reading goals, statistics, and recommendations.

## Prerequisites

* JDK 17
* Docker (PostgreSQL and Testcontainers-based tests)
* Maven 3.9+ or included Maven Wrapper

## Configuration

| Variable         | Required | Default                                     | Purpose                     |
|------------------|----------|---------------------------------------------|-----------------------------|
| `JWT_SECRET_KEY` | Yes      | —                                           | JWT signing secret          |
| `DB_URL`         | No       | `jdbc:postgresql://localhost:5432/postgres` | PostgreSQL JDBC URL         |
| `DB_USERNAME`    | No       | `postgres`                                  | PostgreSQL username         |
| `DB_PASSWORD`    | No       | `postgres`                                  | PostgreSQL password         |
| `SERVER_PORT`    | No       | `8080`                                      | HTTP port                   |
| `GROQ_API_KEY`   | No       | Empty                                       | Voice transcription API key |
| `GEMINI_API_KEY` | No       | Empty                                       | AI recommendation API key   |

Liquibase applies database migrations during startup. Hibernate validates the resulting schema.

## Run Locally

Start PostgreSQL from the repository root:

```bash
docker compose up -d
```

From `library-app`:

```bash
JWT_SECRET_KEY=replace-with-a-secure-secret ./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
$env:JWT_SECRET_KEY = "replace-with-a-secure-secret"
.\mvnw.cmd spring-boot:run
```

The API starts at `http://localhost:8080` by default.

## Build

```bash
./mvnw clean package
```

## Run Tests

Docker must be running for Testcontainers-based tests.

```bash
./mvnw test
```

## Code Style

Apply deterministic Java formatting:

```bash
./mvnw spotless:apply
```

Check formatting and structural style without changing files:

```bash
./mvnw spotless:check checkstyle:check
```

Check one file (path relative to `library-app`):

```bash
./mvnw spotless:check -DspotlessFiles=src/main/java/org/example/library/user/service/UserService.java
./mvnw checkstyle:check -Dcheckstyle.includes=**/UserService.java
```

Use `spotless:apply` instead of `spotless:check` to format that file. Spotless accepts a relative path; Checkstyle's
`includes` value is an Ant-style path pattern.

Both checks also run during `./mvnw verify`. Formatter settings live in `config/java-formatter.xml`; Checkstyle rules
live in `config/checkstyle/checkstyle.xml`.

## Package Structure

Backend code follows package-by-feature under `src/main/java/org/example/library`. Cross-cutting code belongs in focused
shared packages such as `common` and `security`.

## Operations

Spring Boot Actuator exposes `health`, `info`, and Prometheus metrics endpoints.
