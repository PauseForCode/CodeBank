# codeBank — Architecture

## 1) Architecture Style

Use **Clean/Hexagonal-inspired layered architecture** with clear dependency direction:

- `http` → accepts requests/responses (controllers, DTOs)
- `application` (business rules) → use cases/services
- `infra` → database, migrations, external adapters
- `tests` → e2e/integration tests

## 2) Dependency Rule (important)

Dependencies must point inward:

- `http` depends on `application`
- `infra` depends on `application`
- `application` depends on **nothing framework-specific** (or minimal shared kernel)
- `tests` depends on `http` + `infra` + `application`

> `application` must not depend on `http` or `infra`.

## 3) Suggested Gradle Multi-Module

- `:app` (Spring Boot entrypoint)
- `:http`
- `:application`
- `:infra`
- `:tests:e2e`

`app` wires beans and runs the application.

## 4) Module Responsibilities

### `http`
- REST controllers
- request/response contracts
- validation
- exception handlers
- security entry mapping (if needed)

### `application`
- use cases (e.g., `CreateAccountUseCase`)
- business services/domain rules
- interfaces/ports for repositories and gateways
- transaction boundaries (optional, or in app layer orchestration)

### `infra`
- JPA entities/repositories/adapters
- Flyway migrations (`db/migration`)
- PostgreSQL datasource config
- technical implementations for ports

### `tests/e2e`
- full API flow tests
- Testcontainers PostgreSQL (recommended)
- optional H2 for lightweight test scenarios

## 5) Runtime Profiles

- `application.yml` → common
- `application-dev.yml` → local dev
- `application-test.yml` → tests (H2 or Testcontainers setup)
- `application-prod.yml` → PostgreSQL + production settings

## 6) Database Strategy

- **Production:** PostgreSQL
- **Tests:**  
  - fast tests: H2 (in-memory)  
  - realistic integration/e2e: PostgreSQL via Testcontainers
- **Migrations:** Flyway only (single source of truth)

## 7) Security

- Add Spring Security in `http`/`app` config
- If JWT is used: OAuth2 Resource Server
- Keep auth/role checks close to entrypoints and use-case policies

## 8) High-Level Request Flow

1. `http` controller receives request
2. validates DTO
3. calls `application` use case
4. use case calls repository port
5. `infra` adapter persists/reads data
6. use case returns result
7. controller maps to response

## 9) Minimal Package Example

```text
com.codebank
├── app
├── http
│   ├── controller
│   ├── dto
│   └── advice
├── application
│   ├── usecase
│   ├── service
│   ├── domain
│   └── port
│       ├── in
│       └── out
├── infra
│   ├── persistence
│   │   ├── entity
│   │   ├── repository
│   │   └── adapter
│   └── config
└── tests
    └── e2e
```

## 10) Initializr Dependencies (base)

- Spring Web
- Validation
- Spring Data JPA
- PostgreSQL Driver
- H2 Database
- Flyway Migration
- Spring Security
- (optional) OAuth2 Resource Server
- Spring Boot Test

Additional Gradle test deps:
- `spring-security-test`
- `testcontainers-postgresql`
- `testcontainers-junit-jupiter`
- `spring-boot-testcontainers`