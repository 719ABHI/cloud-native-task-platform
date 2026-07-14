# Sprint 03 Notes – Authentication & Authorization

> **Sprint Goal**
>
> Learn and implement authentication and authorization using Spring Security and JWT while understanding the architecture behind every component instead of simply writing code.

---

# Story 1 – Introduction to Spring Security

## What is Spring Security?

Spring Security is a framework that provides security for Spring applications.

It handles:

- Authentication
- Authorization
- Password Encryption
- Protection against common attacks
- Session Management
- JWT Authentication
- OAuth2 Authentication

Instead of writing authentication logic ourselves, Spring Security provides a complete security infrastructure.

---

# Authentication vs Authorization

This is one of the most common interview questions.

## Authentication

Authentication answers:

> **Who are you?**

Example:

```
Username
Password
```

Spring verifies the credentials.

If correct:

```
Authenticated
```

Otherwise:

```
401 Unauthorized
```

Authentication verifies identity.

---

## Authorization

Authorization answers:

> **What are you allowed to access?**

Example:

```
User
```

Allowed

```
GET /tasks
```

Not Allowed

```
DELETE /users
```

Authorization checks permissions after authentication.

---

## Easy Way to Remember

Authentication

```
Identity
```

Authorization

```
Permission
```

---

# Why do we need Spring Security?

Without Spring Security:

```
Client

↓

Controller

↓

Database
```

Anyone can access the APIs.

---

With Spring Security:

```
Client

↓

Security Filter Chain

↓

Controller

↓

Database
```

Every request is checked before reaching the controller.

---

# Story 2 – SecurityFilterChain

Spring Security uses a chain of filters.

Every request passes through these filters before reaching the controller.

```
Request

↓

Filter 1

↓

Filter 2

↓

JWT Filter

↓

Controller
```

Every filter performs a specific responsibility.

Examples:

- Authentication
- Authorization
- CSRF
- JWT Validation

---

## Why use a Filter?

Imagine writing JWT validation inside every controller.

```
TaskController

↓

Validate JWT

UserController

↓

Validate JWT

AdminController

↓

Validate JWT
```

This duplicates code.

Instead:

```
Request

↓

JWT Filter

↓

Controller
```

One filter.

Every request.

---

# Story 3 – SecurityConfig

Spring Security configuration lives inside:

```
SecurityConfig
```

Responsibilities:

- Configure authentication
- Configure authorization
- Register custom filters
- Configure PasswordEncoder
- Configure public endpoints
- Configure protected endpoints

---

## PasswordEncoder Bean

We created

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

### Why?

Passwords should never be stored as plain text.

Instead:

```
Password

↓

BCrypt

↓

Hash

↓

Database
```

---

## SecurityFilterChain Bean

We configured

```java
@Bean
public SecurityFilterChain securityFilterChain(...)
```

This method tells Spring Security:

- Which endpoints are public
- Which endpoints are protected
- Which filters should execute
- In what order they should execute

---

# Story 4 – CSRF

Initially we wrote

```java
.csrf(csrf -> csrf.disable())
```

---

## What is CSRF?

CSRF

```
Cross Site Request Forgery
```

It is an attack where another website forces a logged-in user to perform an unwanted action.

Example:

User logged into Banking App.

Visits a malicious website.

The malicious website silently submits:

```
Transfer ₹10000
```

The browser automatically sends session cookies.

The bank thinks the request came from the user.

---

## Why disable CSRF?

Our application uses:

```
JWT
```

instead of

```
Sessions
```

JWT is sent manually using

```
Authorization

Bearer <JWT>
```

No session cookie.

Therefore CSRF protection is unnecessary for our REST API.

---

# Story 5 – Public vs Protected Endpoints

Initially we configured

```java
.anyRequest().permitAll()
```

Meaning:

```
Everyone

↓

Everything
```

Useful while building the application.

---

Later we changed to

```java
.requestMatchers(
    "/api/auth/register",
    "/api/auth/login"
).permitAll()

.anyRequest().authenticated()
```

Meaning:

Public

```
Register

Login

Swagger
```

Protected

```
Tasks

Future APIs
```

---

# Story 6 – Constructor Injection

Throughout the sprint we followed Constructor Injection.

Example:

```java
private final UserRepository userRepository;

public UserServiceImpl(UserRepository userRepository) {
    this.userRepository = userRepository;
}
```

---

## Why Constructor Injection?

Advantages:

- Mandatory dependencies.
- Immutable fields.
- Easier unit testing.
- Recommended by Spring.
- Prevents NullPointerException caused by missing dependencies.

---

## Why not Field Injection?

Example:

```java
@Autowired
private UserRepository repository;
```

Problems:

- Harder to test.
- Mutable dependency.
- Hidden dependency.

Constructor Injection makes dependencies explicit.

---

# Story 7 – Project Architecture

After Sprint 3 the backend architecture became

```
Controller

↓

Service

↓

Repository

↓

Database
```

Security layer

```
Request

↓

Security Filter Chain

↓

JWT Filter

↓

Controller

↓

Service

↓

Repository

↓

Database
```

---

# Important Interview Questions

## Q1. Difference between Authentication and Authorization?

Authentication

```
Who are you?
```

Authorization

```
What are you allowed to access?
```

---

## Q2. Why use Spring Security?

- Secure REST APIs
- Authentication
- Authorization
- Password Encryption
- Security Filters
- Easy integration with JWT

---

## Q3. Why use SecurityFilterChain?

To configure Spring Security without extending deprecated adapters and to define how requests should be secured.

---

## Q4. Why disable CSRF?

Because our backend uses stateless JWT authentication instead of session-based authentication.

---

## Q5. Why Constructor Injection?

Because it provides immutable dependencies, better testing support, and follows Spring Boot best practices.

---

# Key Takeaways

- Spring Security intercepts every request.
- Authentication and Authorization are different concepts.
- SecurityFilterChain controls application security.
- JWT applications generally disable CSRF.
- Constructor Injection should be preferred over field injection.
- Public and protected endpoints should be explicitly configured.