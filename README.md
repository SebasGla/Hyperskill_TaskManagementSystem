# Task Management System

A multi-stage Spring Boot application developed as part of the Hyperskill Java Backend Track. The system manages tasks, assignments, comments, and secure user authentication.

---

## Project Stages & Documentation

| Stage | Title | Status | Documentation |
| :--- | :--- | :--- | :--- |
| **Stage 1** | **Registering users** | Completed | [Spring Security User Login Setup](docs/Spring%20security%20user%20login%20setup.md) |
| **Stage 2** | Creating tasks | Upcoming | Planned |
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

For the detailed step-by-step implementation guide covering all annotations and architectural layers, see the [Stage 1 Documentation](docs/Spring%20security%20user%20login%20setup.md).
