# Sprint 03 – Authentication & Authorization

| Item | Details |
|------|---------|
| **Sprint** | Sprint 03 |
| **Status** | ✅ Completed |
| **Focus Area** | Authentication & Authorization |
| **Technologies** | Spring Boot, Spring Security, Spring Data JPA, JWT, BCrypt, PostgreSQL |

---

# Objective

Implement a secure authentication and authorization system for the Cloud Native Task Management Platform using Spring Security and JSON Web Tokens (JWT).

The primary goal of this sprint was to establish a production-style authentication mechanism that securely manages user registration, login, password encryption, JWT generation, request authentication, and endpoint protection while providing a scalable foundation for future role-based authorization.

---

# Features Implemented

During this sprint, the following features were implemented:

- Integrated Spring Security into the application.
- Configured Spring Security using `SecurityFilterChain`.
- Implemented secure user registration.
- Implemented user authentication (login).
- Added BCrypt password hashing.
- Added JWT generation after successful login.
- Implemented JWT validation.
- Created a custom JWT authentication filter.
- Integrated Spring Security with JWT authentication.
- Protected REST APIs using JWT.
- Configured public and protected endpoints.
- Added custom authentication exception handling.
- Established the foundation for future role-based authorization.

---

# Implementation Summary

Sprint 03 transformed the application from an open REST API into a secure backend capable of authenticating users using JWT-based authentication.

A dedicated User module was introduced to manage user registration and login. User passwords are securely hashed using BCrypt before being stored in PostgreSQL. During authentication, user credentials are verified, and a JWT access token is generated upon successful login.

Every incoming request to protected endpoints is intercepted by a custom JWT authentication filter. The filter validates the JWT, extracts the authenticated user's identity, loads the latest user details from the database, and stores the authentication information inside Spring Security's `SecurityContextHolder`.

With these enhancements, the backend now supports secure stateless authentication and is prepared for future authorization features.

---

# Authentication Architecture

## User Registration Flow

```text
Client
   │
   ▼
POST /api/auth/register
   │
   ▼
UserController
   │
   ▼
UserService
   │
   ▼
BCrypt Password Encoding
   │
   ▼
UserRepository
   │
   ▼
PostgreSQL
```

---

## User Login Flow

```text
Client
   │
   ▼
POST /api/auth/login
   │
   ▼
UserController
   │
   ▼
UserService
   │
   ▼
Verify Password (BCrypt)
   │
   ▼
JwtService
   │
   ▼
Generate JWT
   │
   ▼
Return Access Token
```

---

## Protected Request Flow

```text
Client
   │
Authorization: Bearer <JWT>
   │
   ▼
JwtAuthenticationFilter
   │
   ▼
Extract JWT
   │
   ▼
Extract Username
   │
   ▼
CustomUserDetailsService
   │
   ▼
Load User From Database
   │
   ▼
Validate JWT
   │
   ▼
UsernamePasswordAuthenticationToken
   │
   ▼
SecurityContextHolder
   │
   ▼
Protected Controller
```

---

# Project Structure Updates

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
├── repository/
│   └── UserRepository.java
│
├── security/
│   └── JwtAuthenticationFilter.java
│
├── service/
│   ├── UserService.java
│   ├── JwtService.java
│   ├── CustomUserDetailsService.java
│   └── impl/
│       ├── UserServiceImpl.java
│       └── JwtServiceImpl.java
```

---

# APIs Added

| Method | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Authenticate a user and return a JWT access token |

---

# Security Configuration

## Public Endpoints

- `POST /api/auth/register`
- `POST /api/auth/login`
- `/swagger-ui/**`
- `/v3/api-docs/**`

## Protected Endpoints

- All Task Management APIs
- Every endpoint requiring authenticated access

---

# Dependencies Added

## Spring Security

```gradle
implementation 'org.springframework.boot:spring-boot-starter-security'
```

## JWT

```gradle
implementation 'io.jsonwebtoken:jjwt-api:0.12.6'

runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.6'

runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.6'
```

---

# Files Added

## Configuration

- SecurityConfig.java

## Controller

- UserController.java

## DTO

- RegisterRequest.java
- LoginRequest.java
- LoginResponse.java
- UserResponse.java

## Entity

- User.java
- Role.java

## Repository

- UserRepository.java

## Service

- UserService.java
- UserServiceImpl.java
- JwtService.java
- JwtServiceImpl.java
- CustomUserDetailsService.java

## Security

- JwtAuthenticationFilter.java

## Exception

- InvalidCredentialsException.java

---

# Files Modified

- build.gradle
- application.properties
- UserService.java
- UserServiceImpl.java
- LoginResponse.java
- SecurityConfig.java

---

# Challenges Faced

During the implementation of authentication and authorization, the following challenges were encountered:

- Understanding Spring Security's authentication lifecycle.
- Configuring stateless authentication using JWT.
- Implementing BCrypt password hashing.
- Integrating JWT validation into every incoming request.
- Registering a custom authentication filter.
- Managing authenticated users through `SecurityContextHolder`.
- Configuring public and protected endpoints.
- Resolving dependency injection and constructor configuration issues.
- Handling authentication failures using custom exceptions.

---

# Outcome

Sprint 03 successfully introduced a complete authentication system into the Cloud Native Task Management Platform.

Users can securely register and authenticate using email and password credentials. Passwords are stored using BCrypt hashing, while authenticated users receive JWT access tokens. Every protected API now requires a valid JWT before accessing application resources.

The backend is now equipped with a production-style authentication architecture and provides a solid foundation for implementing role-based authorization and additional security features in future sprints.

---

# Next Sprint

Sprint 04 focuses on containerizing the application and preparing it for cloud-native deployment.

Planned topics include:

- Docker
- Dockerfile
- Multi-stage builds
- Docker Compose
- PostgreSQL containerization
- Docker networking
- Docker volumes
- Containerized application deployment