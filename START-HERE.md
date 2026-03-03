# START HERE — Build a New Feature (Baby Steps)

This guide teaches you how to add a **new feature** in this project from scratch.

We will build: **User feature**

- Create user
- Get user by id

And we will wire all layers:

- Controller
- Advice
- DTO
- Ports
- Service
- Domain
- Infra Entity + Repository + Adapter

---

## 0) First, what is this architecture?

Think like this:

- `http` = talks with the outside world (REST API)
- `application` = business logic (rules)
- `infra` = database and technical details

Rule: **outside depends on inside**

- `http` can call `application`
- `infra` can implement `application` ports
- `application` should not know HTTP or JPA details

---

## 0.1) I never ran Spring Boot before (from zero)

Perfect. Start here.

### What you need installed

1. **Java 25** (or the project toolchain version)
2. **Git**
3. **Docker** (optional, only for PostgreSQL profile)

### Check if Java exists

```bash
java -version
```

If command is not found, install Java first.

### Run the app (dev profile, easiest)

At project root (same folder that has `gradlew`):

```bash
./gradlew bootRun
```

What happens:

1. Gradle compiles code
2. Spring Boot starts
3. Flyway runs DB migrations
4. App listens on `http://localhost:8080`

### Test if app is alive

Open browser:

- `http://localhost:8080/`

Or terminal:

```bash
curl http://localhost:8080/
```

### Stop the app

Press `Ctrl + C` in terminal where app is running.

### Run tests

```bash
./gradlew test
```

### Optional: run with PostgreSQL + Docker

```bash
docker compose up -d
SPRING_PROFILES_ACTIVE=prod ./gradlew bootRun
```

If this is your first time, use `dev` first. It is simpler.

---

## 1) Folder structure for a new `user` feature

Create these folders/files:

```text
src/main/java/com/code/bank/codebank
├── application
│   └── user
│       ├── domain
│       │   └── User.java
│       ├── port
│       │   └── out
│       │       └── UserRepositoryPort.java
│       └── service
│           ├── UserApplicationService.java
│           └── UserNotFoundException.java
├── http
│   ├── controller
│   │   └── UserController.java
│   ├── dto
│   │   ├── CreateUserRequest.java
│   │   └── UserResponse.java
│   └── advice
│       └── (update existing ApiExceptionHandler.java)
└── infra
	└── persistence
		├── entity
		│   └── UserEntity.java
		├── repository
		│   └── SpringDataUserRepository.java
		└── adapter
			└── UserRepositoryAdapter.java
```

Also add migration:

```text
src/main/resources/db/migration/V2__create_users_table.sql
```

---

## 2) Domain (the heart)

Domain is your pure business data.

`application/user/domain/User.java`

```java
package com.code.bank.codebank.application.user.domain;

import java.time.Instant;
import java.util.UUID;

public record User(
		UUID id,
		String name,
		String email,
		Instant createdAt
) {
}
```

Simple rule: Domain has **no JPA annotations**, no controller annotations.

---

## 3) Ports (application contracts)

Port = interface that says what the application needs from outside.

`application/user/port/out/UserRepositoryPort.java`

```java
package com.code.bank.codebank.application.user.port.out;

import com.code.bank.codebank.application.user.domain.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {
	User save(User user);
	Optional<User> findById(UUID id);
	Optional<User> findByEmail(String email);
}
```

---

## 4) Service (business logic)

Service uses ports and applies business rules.

`application/user/service/UserNotFoundException.java`

```java
package com.code.bank.codebank.application.user.service;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {
	public UserNotFoundException(UUID id) {
		super("User not found: " + id);
	}
}
```

`application/user/service/UserApplicationService.java`

```java
package com.code.bank.codebank.application.user.service;

import com.code.bank.codebank.application.user.domain.User;
import com.code.bank.codebank.application.user.port.out.UserRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class UserApplicationService {

	private final UserRepositoryPort userRepository;

	public UserApplicationService(UserRepositoryPort userRepository) {
		this.userRepository = userRepository;
	}

	public User create(String name, String email) {
		userRepository.findByEmail(email.trim().toLowerCase())
				.ifPresent(existing -> {
					throw new IllegalArgumentException("Email already exists");
				});

		User user = new User(
				UUID.randomUUID(),
				name.trim(),
				email.trim().toLowerCase(),
				Instant.now()
		);

		return userRepository.save(user);
	}

	public User getById(UUID id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new UserNotFoundException(id));
	}
}
```

---

## 5) Infra Entity (database model)

Entity is how data is stored in DB.

`infra/persistence/entity/UserEntity.java`

```java
package com.code.bank.codebank.infra.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserEntity {

	@Id
	private UUID id;

	@Column(nullable = false, length = 120)
	private String name;

	@Column(nullable = false, unique = true, length = 180)
	private String email;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected UserEntity() {
	}

	public UserEntity(UUID id, String name, String email, Instant createdAt) {
		this.id = id;
		this.name = name;
		this.email = email;
		this.createdAt = createdAt;
	}

	public UUID getId() { return id; }
	public String getName() { return name; }
	public String getEmail() { return email; }
	public Instant getCreatedAt() { return createdAt; }
}
```

---

## 6) Infra Repository + Adapter

### 6.1 Spring Data repository

`infra/persistence/repository/SpringDataUserRepository.java`

```java
package com.code.bank.codebank.infra.persistence.repository;

import com.code.bank.codebank.infra.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataUserRepository extends JpaRepository<UserEntity, UUID> {
	Optional<UserEntity> findByEmail(String email);
}
```

### 6.2 Adapter (implements application port)

`infra/persistence/adapter/UserRepositoryAdapter.java`

```java
package com.code.bank.codebank.infra.persistence.adapter;

import com.code.bank.codebank.application.user.domain.User;
import com.code.bank.codebank.application.user.port.out.UserRepositoryPort;
import com.code.bank.codebank.infra.persistence.entity.UserEntity;
import com.code.bank.codebank.infra.persistence.repository.SpringDataUserRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class UserRepositoryAdapter implements UserRepositoryPort {

	private final SpringDataUserRepository repository;

	public UserRepositoryAdapter(SpringDataUserRepository repository) {
		this.repository = repository;
	}

	@Override
	public User save(User user) {
		UserEntity saved = repository.save(toEntity(user));
		return toDomain(saved);
	}

	@Override
	public Optional<User> findById(UUID id) {
		return repository.findById(id).map(this::toDomain);
	}

	@Override
	public Optional<User> findByEmail(String email) {
		return repository.findByEmail(email).map(this::toDomain);
	}

	private UserEntity toEntity(User user) {
		return new UserEntity(user.id(), user.name(), user.email(), user.createdAt());
	}

	private User toDomain(UserEntity entity) {
		return new User(entity.getId(), entity.getName(), entity.getEmail(), entity.getCreatedAt());
	}
}
```

	### 6.3 Use MapStruct mapper (recommended)

	For cleaner code, avoid manual mapping methods in adapters.

	Instead of writing this in every adapter:

	- `toEntity(...)`
	- `toDomain(...)`

	Use a dedicated mapper interface and let MapStruct generate the implementation.

	#### 6.3.1 Add dependencies in `build.gradle`

	```gradle
	implementation 'org.mapstruct:mapstruct:1.6.3'
	annotationProcessor 'org.mapstruct:mapstruct-processor:1.6.3'
	testAnnotationProcessor 'org.mapstruct:mapstruct-processor:1.6.3'
	```

	#### 6.3.2 Create mapper interface

	`infra/persistence/mapper/UserPersistenceMapper.java`

	```java
	package com.code.bank.codebank.infra.persistence.mapper;

	import org.mapstruct.Mapper;

	import com.code.bank.codebank.application.user.domain.User;
	import com.code.bank.codebank.infra.persistence.entity.UserEntity;

	@Mapper(componentModel = "spring")
	public interface UserPersistenceMapper {
		UserEntity toEntity(User user);
		User toDomain(UserEntity entity);
	}
	```

	#### 6.3.3 Inject mapper into adapter

	```java
	@Component
	public class UserRepositoryAdapter implements UserRepositoryPort {

		private final SpringDataUserRepository repository;
		private final UserPersistenceMapper mapper;

		public UserRepositoryAdapter(SpringDataUserRepository repository, UserPersistenceMapper mapper) {
			this.repository = repository;
			this.mapper = mapper;
		}

		@Override
		public User save(User user) {
			UserEntity saved = repository.save(mapper.toEntity(user));
			return mapper.toDomain(saved);
		}

		@Override
		public Optional<User> findById(UUID id) {
			return repository.findById(id).map(mapper::toDomain);
		}
	}
	```

	This keeps adapters small and focused on repository calls.

---

## 7) DTOs (HTTP input/output)

DTO is what API receives/returns.

Think of DTO like a **delivery box**:

- Request DTO = box that comes from client to your API
- Response DTO = box your API sends back

Why we use DTO:

1. Protect internal domain objects
2. Validate incoming data (`@NotBlank`, `@Email`, etc.)
3. Keep API format stable even if domain changes

### Domain vs DTO (super important)

- **Domain (`User`)**: business meaning, used in application logic
- **DTO (`CreateUserRequest`, `UserResponse`)**: transport format for HTTP only

Never expose JPA entity directly in controller response.

Bad (don’t do):

```java
return userEntity;
```

Good (do this):

```java
return UserResponse.from(userDomain);
```

`http/dto/CreateUserRequest.java`

```java
package com.code.bank.codebank.http.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
		@NotBlank @Size(max = 120) String name,
		@NotBlank @Email @Size(max = 180) String email
) {
}
```

`http/dto/UserResponse.java`

```java
package com.code.bank.codebank.http.dto;

import com.code.bank.codebank.application.user.domain.User;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
		UUID id,
		String name,
		String email,
		Instant createdAt
) {
	public static UserResponse from(User user) {
		return new UserResponse(user.id(), user.name(), user.email(), user.createdAt());
	}
}
```

---

## 8) Controller (HTTP endpoint)

Controller calls service.

`http/controller/UserController.java`

```java
package com.code.bank.codebank.http.controller;

import com.code.bank.codebank.application.user.domain.User;
import com.code.bank.codebank.application.user.service.UserApplicationService;
import com.code.bank.codebank.http.dto.CreateUserRequest;
import com.code.bank.codebank.http.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

	private final UserApplicationService userApplicationService;

	public UserController(UserApplicationService userApplicationService) {
		this.userApplicationService = userApplicationService;
	}

	@PostMapping
	public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
		User user = userApplicationService.create(request.name(), request.email());
		return ResponseEntity
				.created(URI.create("/api/users/" + user.id()))
				.body(UserResponse.from(user));
	}

	@GetMapping("/{id}")
	public ResponseEntity<UserResponse> getById(@PathVariable UUID id) {
		return ResponseEntity.ok(UserResponse.from(userApplicationService.getById(id)));
	}
}
```

---

## 9) Advice (centralized error handling)

Update existing `http/advice/ApiExceptionHandler.java`:

```java
@ExceptionHandler(UserNotFoundException.class)
public ResponseEntity<ApiError> handleUserNotFound(UserNotFoundException exception) {
	ApiError error = new ApiError("USER_NOT_FOUND", exception.getMessage(), Instant.now());
	return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
}

@ExceptionHandler(IllegalArgumentException.class)
public ResponseEntity<ApiError> handleBusinessError(IllegalArgumentException exception) {
	ApiError error = new ApiError("BUSINESS_RULE_ERROR", exception.getMessage(), Instant.now());
	return ResponseEntity.badRequest().body(error);
}
```

---

## 10) Migration (database table)

Create file `src/main/resources/db/migration/V2__create_users_table.sql`

```sql
CREATE TABLE users (
	id UUID PRIMARY KEY,
	name VARCHAR(120) NOT NULL,
	email VARCHAR(180) NOT NULL UNIQUE,
	created_at TIMESTAMP NOT NULL
);
```

### What is a migration? (baby explanation)

A migration is a **small history file** that teaches your database how to evolve.

- V1 creates first tables
- V2 adds new table/column
- V3 fixes structure, and so on

Why this is good:

- Everyone in the team gets the same DB structure
- CI and production are reproducible
- You can see "what changed" and "when"

Migration files are code for your database.

### Migration naming rules (Flyway)

Format:

```text
V{version}__{description}.sql
```

Examples:

- `V1__create_snippets_table.sql`
- `V2__create_users_table.sql`
- `V3__add_unique_index_to_users_email.sql`

Rules:

- Version must increase (`V1`, `V2`, `V3`...)
- Never edit a migration that already ran in shared/prod DB
- Create a new migration instead of rewriting history

---

## 10.1) How auto-run migrations work

In this project, Flyway auto-runs on app startup.

Current config:

`src/main/resources/application.properties`

```properties
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
```

Also relevant common properties in this project:

```properties
spring.profiles.default=dev
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.open-in-view=false
```

Profile-specific datasource properties:

`src/main/resources/application-dev.properties`

```properties
spring.datasource.url=jdbc:h2:mem:codebank;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
```

`src/main/resources/application-prod.properties`

```properties
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/codebank}
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.username=${DB_USER:codebank}
spring.datasource.password=${DB_PASSWORD:codebank}
```

This means:

1. App starts
2. Flyway checks database history table (`flyway_schema_history`)
3. Finds migrations not applied yet
4. Applies them in order (`V1`, then `V2`, ...)
5. Then Spring app continues startup

So yes: **you usually do not run migration manually** in dev.

### How to see if it worked

Look in startup logs for Flyway messages (migrated, schema up to date), or check DB table:

```sql
SELECT * FROM flyway_schema_history ORDER BY installed_rank;
```

Typical log lines you may see:

- `Successfully validated X migrations`
- `Current version of schema ...`
- `Migrating schema ... to version ...`
- `Successfully applied X migrations`

If you see errors, read the first SQL error line. It usually points to the exact migration file name.

---

## 10.2) Auto-run with profiles (dev/test/prod)

This project uses profiles:

- `dev` (default): H2 memory DB
- `test`: H2 for tests
- `prod`: PostgreSQL

Flyway can run in all profiles.

### Run dev (default)

```bash
./gradlew bootRun
```

### Run prod profile (PostgreSQL)

```bash
SPRING_PROFILES_ACTIVE=prod ./gradlew bootRun
```

If using Docker DB:

```bash
docker compose up -d
SPRING_PROFILES_ACTIVE=prod ./gradlew bootRun
```

---

## 10.3) Common migration mistakes (and fixes)

1. **Mistake:** Two files with same version (`V2` twice)  
	**Fix:** Use next version number.

2. **Mistake:** Editing old migration already applied in prod  
	**Fix:** Create new migration (`V3...`) with corrective SQL.

3. **Mistake:** Works in H2 but fails in PostgreSQL  
	**Fix:** Use PostgreSQL-compatible SQL when possible and test in `prod` profile locally.

4. **Mistake:** App fails because table exists but Flyway history is broken  
	**Fix:** Recreate local DB for study, or repair carefully in non-prod environments.

---

## 10.4) When creating a new feature, DB checklist

- Added new `Vx__...sql` migration file
- Entity table/columns match SQL
- Domain model matches entity mapping
- Service rules align with DB constraints (e.g., unique email)
- App starts without Flyway errors

---

## 11) How to test quickly (manual)

Create user:

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Mario","email":"mario@nintendo.com"}'
```

Get user by id:

```bash
curl http://localhost:8080/api/users/{id}
```

---

## 12) Important beginner rules (very important)

1. **Domain first** (clean model)
2. **Port in application** (contract)
3. **Adapter in infra** (implementation)
4. **Controller + DTO in http**
5. **Never put JPA annotations in domain**
6. **Never put HTTP logic inside application service**
7. **Always create migration for DB changes**
8. **Validate request in DTO, not in controller if possible**

---

## 12.1) How data flows in your app (end-to-end)

Let’s say client calls `POST /api/users`:

1. Controller receives JSON
2. Spring converts JSON -> `CreateUserRequest` DTO
3. Validation runs (`@NotBlank`, `@Email`)
4. Controller calls `UserApplicationService`
5. Service applies business rules
6. Service calls `UserRepositoryPort`
7. Infra adapter uses JPA repository and DB
8. Service returns domain `User`
9. Controller maps to `UserResponse`
10. Client receives JSON response

That is clean architecture in action.

---

## 13) Your requested order (Controller -> Advice -> DTO -> Ports -> Services -> Domain)

You can code in that order if you want, but for fewer mistakes beginners usually do:

1. Domain
2. Port
3. Service
4. Infra (entity/repository/adapter)
5. DTO
6. Controller
7. Advice

Both are valid. The key is keeping dependencies clean.

---

## 14) Tiny checklist before commit

- App starts
- Flyway migration runs
- `POST /api/users` works
- `GET /api/users/{id}` works
- Validation errors return `400`
- Not found returns `404`

That’s it. You just created a clean feature from scratch 🎉
