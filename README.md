# Task Management System

A multi-stage Spring Boot application developed as part of the Hyperskill Java Backend Track. The system manages tasks, assignments, comments, and secure user authentication.

---

## Documentation Index

Direct links to the step-by-step implementation guides:

* **Stage 1 Guide:** [Spring Security User Login Setup](docs/Spring%20security%20user%20login%20setup.md)
  * Covers `UserEntity`, `UserRepository`, `CreateUserRequest`, `UserController`, `UserAdapter` (`UserDetails`), `UserDetailsServiceImpl` (`UserDetailsService`), and `SecurityConfiguration`.
* **Stage 2 Guide:** [Introduction of Task-Related Modules](docs/Introduction%20of%20task%20related%20modules.md)
  * Covers `TaskStatus`, `TaskEntity` (UUID & timestamping), `TaskRepository` (derived sorting queries), `TaskCreateDto`, `TaskCreateResponseDto`, `TaskService`, `TaskController` (`@AuthenticationPrincipal`), and route authentication.

---

## Project Stages

| Stage | Title | Status | Documentation Guide |
| :--- | :--- | :--- | :--- |
| **Stage 1** | **Registering users** | Completed | [Spring Security User Login Setup](docs/Spring%20security%20user%20login%20setup.md) |
| **Stage 2** | **Creating tasks** | Completed | [Introduction of Task-Related Modules](docs/Introduction%20of%20task%20related%20modules.md) |
| **Stage 3** | Authenticating with JWT | Upcoming | Planned |
| **Stage 4** | Assigning tasks | Upcoming | Planned |
| **Stage 5** | Leaving comments | Upcoming | Planned |

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
  * `TaskStatus` enum persists status as string values (`CREATED`, `INPROCESS`) via `@Enumerated(EnumType.STRING)`.
* **Persistence Layer:** `TaskRepository` defines derived queries for sorted retrieval (`findByOrderByCreatedAtDesc`, `findAllByAuthorOrderByCreatedAtDesc`).
* **DTO Layer:** `TaskCreateDto` validates incoming title and description; `TaskCreateResponseDto` decouples entity structure from client responses via a static `from` factory mapper.
* **Service Layer:** `TaskService` encapsulates business rules, assigns default `TaskStatus.CREATED`, normalizes author emails, and handles stream mappings.
* **REST API:** `TaskController` exposes:
  * `GET /api/tasks` with optional author filtering (`@RequestParam(required = false)`).
  * `POST /api/tasks` using `@AuthenticationPrincipal UserDetails` to automatically assign the task author from the logged-in principal.
* **Security:** `SecurityConfiguration` restricts `/api/tasks` so all operations require authentication.

Detailed guide: [Stage 2 Documentation](docs/Introduction%20of%20task%20related%20modules.md).
