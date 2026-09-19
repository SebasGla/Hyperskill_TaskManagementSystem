# Task Management System: Architecture & Registration Deep Dive

This guide documents the architecture, component roles, and runtime execution flow for user registration and authentication using Spring Boot 3 and Spring Security.

## 1. Architecture & Component Roles

The application follows a layered design pattern. Each component has a single, clear responsibility:

| **Component** | **Layer / Stereotype** | **Primary Role** | 
| `CreateUserRequest` | DTO (`taskmanagement.dto`) | Input contract and validation barrier. Prevents malformed or unsafe inputs from reaching business logic. | 
| `UserEntity` | Domain / Persistence (`taskmanagement.user`) | Relational mapping to the underlying `users` database table via JPA/Hibernate. | 
| `UserRepository` | Data Access (`taskmanagement.user`) | Database interaction abstraction powered by Spring Data JPA. | 
| `UserController` | Transport / Web (`taskmanagement.controller`) | HTTP request routing, input unmarshaling, orchestrating registration steps, and formulating HTTP responses. | 
| `SecurityConfiguration` | Configuration (`taskmanagement.security`) | Sets the security filter chain, declares open vs. protected routes, and exposes shared beans like `PasswordEncoder`. | 
| `UserAdapter` | Security Bridge (`taskmanagement.security`) | Translates the domain entity (`UserEntity`) into Spring Security's native identity model (`UserDetails`). | 
| `UserDetailsServiceImpl` | Security Lookup (`taskmanagement.security`) | Strategy bean used by Spring Security during authentication to fetch user records by username/email. | 
---
## 2. Core Security Concepts: `UserDetails` vs. `UserDetailsService`

Spring Security decouples **how your application stores data** from **how the framework authenticates a principal**. It accomplishes this via two core contracts:

### A. What is `UserDetails`? (The Identity Contract)

Spring Security does not know what your `UserEntity` looks like—it does not care whether your table is called `users` or `accounts`, or whether your identifier is `email`, `username`, or a UUID.

Instead, Spring Security requires an object implementing `org.springframework.security.core.userdetails.UserDetails`. This interface provides the security framework with standard properties:

* `getUsername()`: The unique credential identifier (in your app: the email).

* `getPassword()`: The stored hashed password.

* `getAuthorities()`: The granted roles and permissions (e.g., `ROLE_USER`, `ROLE_ADMIN`).

* Account status flags: `isAccountNonExpired()`, `isAccountNonLocked()`, `isCredentialsNonExpired()`, `isEnabled()`.

#### How `UserAdapter` implements this:

Instead of polluting the JPA entity `UserEntity` with Spring Security-specific interfaces, the **Adapter Pattern** is used:

```
public class UserAdapter implements UserDetails {
    private final UserEntity user;

    public UserAdapter(UserEntity user) {
        this.user = user;
    }

    @Override
    public String getPassword() {
        return this.user.getPassword(); // supplies hashed password to Spring Security
    }

    @Override
    public String getUsername() {
        return this.user.getEmail(); // supplies email as the principal identifier
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(); // empty list means default user permissions (no specific roles yet)
    }
    // ... remaining boolean flags return true
}

```

**Why this is clean:** Your persistence layer (`UserEntity`) remains completely decoupled from Spring Security classes.

### B. What is `UserDetailsService`? (The Identity Lookup Strategy)

`UserDetailsService` is a core functional interface in Spring Security with a single method:

```
UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;

```

Whenever a client attempts to log in (e.g., via HTTP Basic authentication), Spring Security's authentication manager calls this method.

#### How `UserDetailsServiceImpl` implements this:

```
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository repository;

    public UserDetailsServiceImpl(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return repository.findUserByEmail(email)
                .map(UserAdapter::new)
                .orElseThrow(() -> new UsernameNotFoundException("Email not found: " + email));
    }
}

```

1. It queries the database using `repository.findUserByEmail(email)`.

2. If found, it wraps `UserEntity` in a `UserAdapter` via `.map(UserAdapter::new)`.

3. If not found, it throws a `UsernameNotFoundException`, signaling authentication failure.

## 3. High-Level Dependency Graph (Mermaid)

```
classDiagram
    direction TB

    class UserDetails {
        <<interface>>
        +getUsername() String
        +getPassword() String
        +getAuthorities() Collection
    }

    class UserDetailsService {
        <<interface>>
        +loadUserByUsername(String) UserDetails
    }

    class PasswordEncoder {
        <<interface>>
        +encode(CharSequence) String
        +matches(CharSequence, String) boolean
    }

    class CreateUserRequest {
        <<record>>
        +String email
        +String password
    }

    class UserEntity {
        -long id
        -String email
        -String password
    }

    class UserRepository {
        <<interface>>
        +findUserByEmail(String) Optional~UserEntity~
        +existsByEmail(String) boolean
    }

    class UserController {
        -UserRepository repository
        -PasswordEncoder encoder
        +registerUser(CreateUserRequest) ResponseEntity
    }

    class UserAdapter {
        -UserEntity user
        +getUsername() String
        +getPassword() String
    }

    class UserDetailsServiceImpl {
        -UserRepository repository
        +loadUserByUsername(String) UserDetails
    }

    UserDetails <|.. UserAdapter : implements
    UserDetailsService <|.. UserDetailsServiceImpl : implements

    UserAdapter o-- UserEntity : adapts
    UserDetailsServiceImpl --> UserRepository : queries
    UserDetailsServiceImpl ..> UserAdapter : creates

    UserController --> UserRepository : checks & saves
    UserController --> PasswordEncoder : hashes
    UserController ..> CreateUserRequest : receives
    UserController ..> UserEntity : instantiates

```

## 4. End-to-End Registration Control Flow (`POST /api/accounts`)

Here is the exact step-by-step sequence of events when an external client sends a registration request.

```
sequenceDiagram
    autonumber
    actor Client
    participant FilterChain as Spring Security FilterChain
    participant Dispatcher as DispatcherServlet & Validator
    participant Controller as UserController
    participant Repo as UserRepository
    participant Encoder as PasswordEncoder (BCrypt)
    participant DB as H2 / PostgreSQL

    Client->>FilterChain: POST /api/accounts (JSON payload)
    Note over FilterChain: Evaluates SecurityFilterChain rules
    FilterChain->>Dispatcher: Route allowed (.requestMatchers(POST, "/api/accounts").permitAll())
    
    Dispatcher->>Dispatcher: Validate CreateUserRequest (@Valid)
    alt Validation fails (e.g. short password / invalid email)
        Dispatcher-->>Client: 400 Bad Request
    else Validation succeeds
        Dispatcher->>Controller: registerUser(newUserDto)
        
        Controller->>Repo: existsByEmail(newUserDto.email())
        Repo->>DB: SELECT COUNT(*) > 0 FROM users WHERE email = ?
        DB-->>Repo: boolean result
        Repo-->>Controller: true / false
        
        alt Email already exists
            Controller-->>Client: 409 Conflict
        else Email is unique
            Controller->>Encoder: encode(newUserDto.password())
            Note over Encoder: Generates random salt & BCrypt hash
            Encoder-->>Controller: "$2a$10$e7..."
            
            Controller->>Controller: new UserEntity() + setEmail() + setPassword(hash)
            Controller->>Repo: save(newUser)
            Repo->>DB: INSERT INTO users (email, password) VALUES (?, ?)
            DB-->>Repo: Persisted entity (generated ID)
            Repo-->>Controller: saved UserEntity
            
            Controller-->>Client: 200 OK
        end
    end

```

## 5. Step-by-Step Code Walkthrough

### Step 1: Request Interception in the Security Filter Chain

When the HTTP `POST /api/accounts` arrives, it hits Spring Security's `SecurityFilterChain` before reaching any controller:

```
// SecurityConfiguration.java
.authorizeHttpRequests(auth -> auth
    .requestMatchers(HttpMethod.POST, "/api/accounts").permitAll()
    // ...
    .anyRequest().denyAll()
)

```

* The framework checks the incoming method and URI.

* Because `POST /api/accounts` is marked as `.permitAll()`, the security interceptor permits the request without requiring authentication headers.

* CSRF protection is disabled (`.csrf(AbstractHttpConfigurer::disable)`), allowing POST requests without a CSRF token.

### Step 2: Deserialization & Declarative Validation

Spring's `DispatcherServlet` converts the raw JSON body into the `CreateUserRequest` record. The `@Valid` annotation triggers Jakarta Bean Validation:

```
// CreateUserRequest.java
public record CreateUserRequest(
    @NotBlank(message = "no email given")
    @Email(message = "invalid email format")
    String email,

    @NotBlank(message = "password can't be empty")
    @Size(min = 6, message = "Password must have at least 6 characters")
    String password) {
}

```

* **If `email` is invalid or blank:** Spring interrupts execution and returns an HTTP `400 Bad Request`.

* **If `password` is shorter than 6 characters:** Execution stops before running any controller code.

### Step 3: Duplicate Verification

Inside `UserController.registerUser(...)`:

```
if (repository.existsByEmail(newUserDto.email())){
    return ResponseEntity.status(HttpStatus.CONFLICT).build();
}

```

* The controller queries `UserRepository.existsByEmail(email)`.

* Spring Data JPA executes a lightweight existence query (`SELECT count(*) > 0 FROM users WHERE email = ?`).

* If `true`, the controller immediately halts execution and returns HTTP `409 Conflict`.

### Step 4: Password Hashing (BCrypt)

```
String encodedPassword = encoder.encode(newUserDto.password());

```

* Raw passwords must never be stored in plain text.

* `PasswordEncoder` (configured as `BCryptPasswordEncoder` in `SecurityConfiguration`) generates a cryptographic salt and creates an irreversible hash (e.g., `$2a$10$vI8aWBnW3fID.ZQ4/zo1G.q1lR5...`).

### Step 5: Entity Instantiation & Database Persistence

```
UserEntity newUser = new UserEntity();
newUser.setEmail(newUserDto.email());
newUser.setPassword(encodedPassword);
repository.save(newUser);

```

* A new `UserEntity` is instantiated.

* The sanitized email and hashed password are set.

* `repository.save(newUser)` instructs JPA/Hibernate to issue an `INSERT INTO users (email, password) VALUES (?, ?)` statement and generates a primary key ID.

### Step 6: Response Dispatch

```
return ResponseEntity.ok().build();

```

* An HTTP `200 OK` response with an empty body is returned to the client, confirming successful account creation.

## 6. Suggested Improvements for Production

1. **Extract Business Logic into a Service Layer (`UserService`)**:
   Moving existence checks, encoding, and entity mapping from `UserController` to a `@Service` class keeps controllers clean and makes business logic unit-testable without HTTP overhead.

2. **Clean up `UserDetailsServiceImpl` Constructor**:
   Remove the unused `PasswordEncoder encoder` parameter from `UserDetailsServiceImpl`'s constructor.

3. **Use Protected No-Arg Constructor**:
   In `UserEntity`, change `public UserEntity()` to `protected UserEntity()`. Hibernate requires it for reflection, but making it `protected` prevents other application classes from instantiating incomplete entities without required fields.
