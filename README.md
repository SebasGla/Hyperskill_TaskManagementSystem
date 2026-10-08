# Task Management System

A multi-stage Spring Boot application developed as part of the Hyperskill Java Backend Track. The system manages tasks, assignments, comments, and secure user authentication.

---

## Documentation Index

Direct links to the step-by-step implementation guides:

* **Stage 1 Guide:** [Spring Security User Login Setup](docs/Spring%20security%20user%20login%20setup.md)
  * Covers `UserEntity`, `UserRepository`, `CreateUserRequest`, `UserController`, `UserAdapter` (`UserDetails`), `UserDetailsServiceImpl` (`UserDetailsService`), and `SecurityConfiguration`.
* **Stage 2 Guide:** [Introduction of Task-Related Modules](docs/Introduction%20of%20task%20related%20modules.md)
  * Covers `TaskStatus`, `TaskEntity` (UUID & timestamping), `TaskRepository` (derived sorting queries), `TaskCreateDto`, `TaskCreateResponseDto`, `TaskService`, `TaskController` (`@AuthenticationPrincipal`), and route authentication.
* **Stage 3 Guide:** [Adding JWT](docs/adding%20JWT.md)
  * Covers JWT architecture, 2048-bit RSA key generation, Nimbus `JwtDecoder` & `JwtEncoder`, OAuth2 Resource Server integration, `POST /api/auth/token`, and universal principal extraction with `Authentication.getName()`.
* **Stage 4 Guide:** [Assigning Tasks](docs/Assigning%20tasks.md)
  * Covers task assignment, lifecycle statuses (`CREATED`, `IN_PROGRESS`, `COMPLETED`), business authorization rules (author vs. assignee access), custom exceptions, and global exception translation (`@RestControllerAdvice`).
* **Stage 5 Guide:** [Leaving Comments](docs/Leaving%20comments.md)
  * Covers relational domain modeling, JPA associations (`@ManyToOne` & `@OneToMany`), bidirectional relationship synchronization, Hibernate `@Formula` calculated fields, and comment API endpoints.

---

## Project Stages

| Stage | Title | Status | Documentation Guide |
| :--- | :--- | :--- | :--- |
| **Stage 1** | **Registering users** | Completed | [Spring Security User Login Setup](docs/Spring%20security%20user%20login%20setup.md) |
| **Stage 2** | **Creating tasks** | Completed | [Introduction of Task-Related Modules](docs/Introduction%20of%20task%20related%20modules.md) |
| **Stage 3** | **Authenticating with JWT** | Completed | [Adding JWT](docs/adding%20JWT.md) |
| **Stage 4** | **Assigning tasks** | Completed | [Assigning Tasks](docs/Assigning%20tasks.md) |
| **Stage 5** | **Leaving comments** | Completed | [Leaving Comments](docs/Leaving%20comments.md) |

---

## Stage 1: Registering Users Overview

Stage 1 establishes the persistence layer and baseline authentication foundation:

* **Endpoint:** `POST /api/accounts` allows new users to register with an email and password.
* **Validation:** Jakarta Validation (`@Valid`, `@NotBlank`, `@Email`, `@Size(min = 6)`) rejects invalid payloads before processing.
* **Email Normalization:** Emails are converted to lowercase (`.toLowerCase()`) during registration and authentication lookups to guarantee case-insensitive uniqueness and prevent duplicate accounts.
* **Password Hashing:** Passwords are encrypted using a configured `BCryptPasswordEncoder` bean before persisting to the database.
* **Security Layer:** 
  * `UserAdapter` bridges the JPA `UserEntity` to Spring Security's `UserDetails` contract.
  * `UserDetailsServiceImpl` implements `UserDetailsService` to fetch credentials from `UserRepository` during authentication.
  * `SecurityConfiguration` establishes a stateless `SecurityFilterChain`, enables HTTP Basic authentication, exposes open routes (`/api/accounts`, `/error`, `/h2-console/**`), and disables CSRF.

Detailed guide: [Stage 1 Documentation](docs/Spring%20security%20user%20login%20setup.md).

---

## Stage 2: Creating Tasks Overview

Stage 2 introduces task management, entity relationships, business service logic, and authenticated controller endpoints:

* **Domain Model:** 
  * `TaskEntity` uses UUID-based primary keys (`GenerationType.UUID`) and an immutable UTC creation timestamp (`Instant.now()`).
  * `TaskStatus` enum persists status as string values (`CREATED`, `IN_PROGRESS`) via `@Enumerated(EnumType.STRING)`.
* **Persistence Layer:** `TaskRepository` defines derived queries for sorted retrieval (`findByOrderByCreatedAtDesc`, `findAllByAuthorOrderByCreatedAtDesc`).
* **DTO Layer:** `TaskCreateDto` validates incoming title and description; `TaskCreateResponseDto` decouples entity structure from client responses via a static `from` factory mapper.
* **Service Layer:** `TaskService` encapsulates business rules, assigns default `TaskStatus.CREATED`, normalizes author emails, and handles stream mappings.
* **REST API:** `TaskController` exposes `GET /api/tasks` and `POST /api/tasks`.
* **Security:** `SecurityConfiguration` restricts `/api/tasks` so all operations require authentication.

Detailed guide: [Stage 2 Documentation](docs/Introduction%20of%20task%20related%20modules.md).

---

## Stage 3: Authenticating with JWT Overview

Stage 3 introduces stateless, token-based authentication using JSON Web Tokens (JWT) signed via asymmetric RSA cryptography:

* **Dependency:** Added `spring-boot-starter-oauth2-resource-server` to leverage Spring Security's native Bearer token verification.
* **RSA Key Pair Generation:** `RsaKeysConfig` creates a 2048-bit RSA key pair for cryptographic signing and verification.
* **Security Beans:**
  * `JwtDecoder`: Verifies incoming Bearer tokens using the RSA public key.
  * `JWKSource` & `JwtEncoder`: Signs outgoing tokens using the RSA private key.
* **OAuth2 Resource Server:** Configured `.oauth2ResourceServer(oauth2 -> oauth2.jwt(...))` on the filter chain to automatically intercept and authenticate `Authorization: Bearer <token>` requests.
* **Token Issuance:** `AuthController` exposes `POST /api/auth/token`, requiring HTTP Basic authentication to issue a signed JWT valid for 60 seconds.
* **Universal Principal Extraction:** Updated `TaskController` from `@AuthenticationPrincipal UserDetails` to `Authentication.getName()`, ensuring full compatibility with both HTTP Basic and JWT Bearer authentication.

Detailed guide: [Stage 3 Documentation](docs/adding%20JWT.md).

---

## Stage 4: Assigning Tasks Overview

Stage 4 expands task management with assignment, status transitions, authorization rules, and centralized exception translation:

* **Task Assignment:** 
  * `PUT /api/tasks/{taskId}/assign` allows assigning tasks to another registered user or unassigning using `"none"`.
  * **Authorization Rule:** Only the task **author** is permitted to assign or unassign a task (attempts by others return `403 Forbidden`).
  * **Validation:** Verified that the proposed assignee exists in `UserRepository` (unregistered emails return `404 Not Found`).
* **Status Updates:** 
  * `PUT /api/tasks/{taskId}/status` transitions tasks across `CREATED`, `IN_PROGRESS`, and `COMPLETED`.
  * **Authorization Rule:** Only the task **author** or the task **assignee** can change status (attempts by third parties return `403 Forbidden`).
* **Query Filtering:** `GET /api/tasks` supports query parameters `author` and `assignee`, allowing filtering by either or both fields combined.
* **Centralized Exception Handling:** `GlobalExceptionHandler` (`@RestControllerAdvice`) catches `TaskNotFoundException`, `AssigneeNotFoundException`, and `TaskForbiddenException`, returning clean HTTP 404 and 403 status codes.

Detailed guide: [Stage 4 Documentation](docs/Assigning%20tasks.md).

---

## Stage 5: Leaving Comments Overview

Stage 5 implements task commenting through relational domain modeling and JPA associations:

* **Relational Association:** 
  * `CommentEntity` child entity establishes `@ManyToOne(fetch = FetchType.LAZY, optional = false)` with `@JoinColumn(name = "task_id")`.
  * `TaskEntity` parent entity manages `@OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)`.
  * Helper synchronization methods (`addComment`, `removeComment`) maintain in-memory consistency between both sides.
* **Hibernate `@Formula`:** Calculates `total_Comments` dynamically via a SQL subquery without loading comment collections into memory.
* **Endpoints:**
  * `POST /api/tasks/{taskId}/comments` validates incoming text (`CommentDto`) and persists new comments associated with the target task and authenticated author.
  * `GET /api/tasks/{taskId}/comments` returns all comments for a task ordered from newest to oldest (`CommentResponseDto`).
* **Security Filter Chain:** Protected all sub-resource routes via `.requestMatchers("/api/tasks/**").authenticated()`.

Detailed guide: [Stage 5 Documentation](docs/Leaving%20comments.md).
