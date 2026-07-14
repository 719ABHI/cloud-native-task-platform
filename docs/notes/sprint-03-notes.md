# Sprint 03 – Authentication & Authorization

| Item | Details |
|------|---------|
| **Sprint** | Sprint 03 |
| **Status** | ✅ Completed |
| **Focus Area** | Authentication & Authorization |
| **Technologies** | Spring Security, JWT, BCrypt, Spring Boot, Spring Data JPA |

---

## Objective

Implement a secure authentication and authorization system for the Cloud Native Task Management Platform using Spring Security and JWT. This sprint focused on protecting REST APIs, securing user credentials, and establishing the foundation for role-based access control.

---

## Features Implemented

During this sprint, the following features were implemented:

- Integrated Spring Security into the application.
- Configured Spring Security using `SecurityFilterChain`.
- Disabled CSRF for REST API communication.
- Configured public and protected endpoints.
- Implemented user registration.
- Implemented user authentication (login).
- Secured passwords using BCrypt hashing.
- Generated JWT access tokens after successful authentication.
- Validated JWT tokens for protected requests.
- Implemented JWT authentication filter.
- Protected all Task Management APIs using JWT authentication.
- Established the foundation for role-based authorization.

---

## Implementation Summary

The backend application was enhanced with a complete authentication system based on Spring Security and JSON Web Tokens (JWT).

A new User module was introduced to manage user registration and authentication. During registration, passwords are securely hashed using BCrypt before being stored in PostgreSQL. During login, credentials are validated and a JWT access token is generated upon successful authentication.

Every protected request is intercepted by a custom JWT authentication filter, which validates the token, loads the authenticated user, and stores the authentication information in the Spring Security context.

With these enhancements, the application now supports secure authentication while providing a scalable foundation for future authorization features.

---

## Architecture Changes

### Authentication Flow

```
Client
   │
   ▼
Register/Login Request
   │
   ▼
REST Controller
   │
   ▼
Service Layer
   │
   ▼
BCrypt Password Encoding / Verification
   │
   ▼
User Repository
   │
   ▼
PostgreSQL
```

---

### JWT Authentication Flow

```
Client Login
   │
   ▼
Authenticate User
   │
   ▼
Generate JWT
   │
   ▼
Return JWT
   │
   ▼
Client Stores Token
```

---

### Protected Request Flow

```
Client
   │
Authorization: Bearer <JWT>
   │
   ▼
JWT Authentication Filter
   │
   ▼
Validate JWT
   │
   ▼
Load User Details
   │
   ▼
SecurityContextHolder
   │
   ▼
Protected Controller
```

---

## Project Structure Updates

The following packages and classes were introduced during this sprint:

```
task-management-backend/
│
├── config/
│   └── SecurityConfig.java
│
├── controller/
│   └── UserController.java
│
├── dto/
│   ├── RegisterRequest.java
│   ├── LoginRequest.java
│   ├── LoginResponse.java
│   └── UserResponse.java
│
├── entity/
│   ├── User.java
│   └── Role.java
│
├── exception/
│   └── InvalidCredentialsException.java
│
├── filter/
│   └── JwtAuthenticationFilter.java
│
├── repository/
│   └── UserRepository.java
│
├── security/
│   └── CustomUserDetailsService.java
│
├── service/
│   ├── UserService.java
│   ├── JwtService.java
│   └── impl/
│       ├── UserServiceImpl.java
│       └── JwtServiceImpl.java
```

---

## Files Added

- User.java
- Role.java
- RegisterRequest.java
- LoginRequest.java
- LoginResponse.java
- UserResponse.java
- UserRepository.java
- UserService.java
- UserServiceImpl.java
- JwtService.java
- JwtServiceImpl.java
- UserController.java
- SecurityConfig.java
- JwtAuthenticationFilter.java
- CustomUserDetailsService.java
- InvalidCredentialsException.java

---

## Files Modified

- build.gradle
- application.properties

---

## APIs Added

| Method | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Authenticate a user and return a JWT access token |

---

## Security Configuration

The following security features were configured during this sprint:

- Spring Security integration.
- Password hashing using BCrypt.
- Stateless authentication using JWT.
- Public endpoint configuration.
- Protected endpoint configuration.
- JWT validation for every authenticated request.
- Custom authentication filter registration.
- Authentication context management using Spring Security.

Public endpoints:

- `POST /api/auth/register`
- `POST /api/auth/login`
- Swagger UI
- OpenAPI documentation

Protected endpoints:

- All Task Management APIs.
- Any endpoint requiring authenticated access.

---

## Dependencies Added

### Spring Security

```gradle
implementation 'org.springframework.boot:spring-boot-starter-security'
```

### JWT

```gradle
implementation 'io.jsonwebtoken:jjwt-api:0.12.6'

runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.6'

runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.6'
```

---

## Challenges Faced

During implementation, several security-related challenges were encountered:

- Understanding the Spring Security authentication lifecycle.
- Configuring stateless authentication using JWT.
- Designing secure user registration and login workflows.
- Implementing password hashing using BCrypt.
- Protecting REST APIs without affecting public endpoints.
- Integrating JWT validation into every incoming request.
- Managing authentication using the Spring Security context.

These challenges provided a deeper understanding of authentication and authorization mechanisms in Spring Boot.

---

## Lessons Learned

- Fundamentals of Spring Security.
- Difference between authentication and authorization.
- Password hashing using BCrypt.
- JWT generation and validation.
- Stateless authentication.
- Request filtering using `OncePerRequestFilter`.
- Loading authenticated users using `UserDetailsService`.
- Managing authenticated users through `SecurityContextHolder`.
- Securing REST APIs using JWT authentication.

---

## Outcome

Sprint 03 introduced a complete authentication system into the Cloud Native Task Management Platform.

Users can now securely register and authenticate using email and password credentials. Passwords are stored securely using BCrypt hashing, while JWT access tokens provide stateless authentication for protected APIs.

The backend is now prepared for advanced authorization features, user-specific resource access, refresh tokens, and role-based access control in future sprints.

---

## Next Sprint

Sprint 04 focuses on improving application functionality and preparing the backend for production-ready features, including:

- Task ownership and user-specific access
- Refresh token implementation
- Role-based authorization
- Pagination and sorting
- Search and filtering
- API enhancements
- Unit and integration testing