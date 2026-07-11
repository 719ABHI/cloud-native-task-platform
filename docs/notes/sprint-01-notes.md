# Sprint 01 Notes – Backend Foundation

## Sprint Overview

Sprint 01 focused on building the backend foundation of the Cloud Native Task Management Platform. The primary objective was to understand the basic architecture of a Spring Boot application, connect it to PostgreSQL, and implement RESTful CRUD APIs using a layered architecture.

---

# Concepts Covered

- Spring Boot
- Gradle
- PostgreSQL
- Spring Data JPA
- Hibernate
- Layered Architecture
- Entity
- Repository
- Service
- Controller
- REST APIs
- CRUD Operations
- Postman Testing

---

# Layered Architecture

## Definition

Layered Architecture is a software design pattern that separates an application into multiple layers, where each layer has a single responsibility.

```
Client
   │
   ▼
Controller
   │
   ▼
Service
   │
   ▼
Repository
   │
   ▼
Database
```

## Responsibilities

### Controller

- Handles incoming HTTP requests.
- Validates request parameters.
- Calls the appropriate service methods.
- Returns HTTP responses.

---

### Service

- Contains business logic.
- Processes data before interacting with the database.
- Acts as a bridge between the Controller and Repository.

---

### Repository

- Responsible for database operations.
- Uses Spring Data JPA to perform CRUD operations.
- Eliminates the need to write SQL for common database operations.

---

### Entity

- Represents a database table.
- Maps Java objects to database records.
- Managed by Hibernate.

---

## Why Layered Architecture?

- Separation of responsibilities
- Easier maintenance
- Better scalability
- Improved code readability
- Easier testing

---

# Spring Data JPA

## Definition

Spring Data JPA simplifies database access by providing repository interfaces that automatically generate SQL queries.

Example:

```java
taskRepository.save(task);

taskRepository.findAll();

taskRepository.findById(id);

taskRepository.delete(task);
```

## Benefits

- Less boilerplate code
- Automatic query generation
- Easy integration with Hibernate
- Supports custom queries

---

# Hibernate

## Definition

Hibernate is the JPA implementation used by Spring Boot.

It converts Java objects into SQL queries and maps database records back into Java objects.

Example Flow

```
Task Entity

↓

Hibernate

↓

SQL Query

↓

PostgreSQL
```

---

# Entity

## Definition

An Entity is a Java class that represents a table in the database.

Example

```
Task

↓

tasks table
```

Each object represents one row in the table.

---

## Important Annotations

### @Entity

Marks the class as a JPA Entity.

---

### @Table

Maps the Entity to a database table.

---

### @Id

Represents the primary key.

Every Entity must contain one.

---

### @GeneratedValue

Automatically generates primary key values.

---

## Best Practices

- Keep Entities focused on persistence.
- Always define a primary key.
- Avoid exposing Entities directly through REST APIs.

---

# Repository

## Definition

Repository is responsible for communicating with the database.

It provides methods for:

- Create
- Read
- Update
- Delete

without writing SQL manually.

---

## Common Methods

```java
save()

findAll()

findById()

delete()

existsById()
```

---

## Benefits

- Less code
- Easier maintenance
- Automatic SQL generation
- Database abstraction

---

# Service Layer

## Definition

The Service layer contains the application's business logic.

It receives requests from the Controller and interacts with the Repository.

---

## Why Service Layer?

Instead of placing business logic inside Controllers, it is centralized inside Services.

Benefits

- Cleaner Controllers
- Reusable business logic
- Better maintainability
- Easier testing

---

# Controller

## Definition

The Controller is responsible for handling HTTP requests and returning HTTP responses.

Example Flow

```
Client

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

## Responsibilities

- Receive HTTP requests
- Extract request data
- Call Service methods
- Return responses

---

# CRUD Operations

CRUD stands for

- Create
- Read
- Update
- Delete

Implemented APIs

| Method | Purpose |
|---------|---------|
| POST | Create Task |
| GET | Get All Tasks |
| GET | Get Task By ID |
| PUT | Update Task |
| DELETE | Delete Task |

---

# REST APIs

REST (Representational State Transfer) is an architectural style used to build web services.

Characteristics

- Stateless
- Uses HTTP methods
- Client-Server architecture
- Resource-based URLs

---

# PostgreSQL Integration

Spring Boot connects to PostgreSQL using configuration properties.

Flow

```
Spring Boot

↓

Spring Data JPA

↓

Hibernate

↓

PostgreSQL
```

---

# API Testing

Postman was used to verify:

- Create Task
- Get All Tasks
- Get Task By ID
- Update Task
- Delete Task

Database changes were verified directly in PostgreSQL.

---

# Best Practices Learned

- Follow layered architecture.
- Keep Controllers lightweight.
- Place business logic inside Services.
- Use Repository only for database operations.
- Test APIs before moving to the next feature.
- Verify database changes after API testing.

---

# Common Mistakes

### Forgetting @Id

Every Entity requires a primary key.

---

### Placing business logic inside Controllers

Business logic should belong in the Service layer.

---

### Exposing Entity directly through APIs

Use DTOs instead.

(This will be implemented in Sprint 02.)

---

### Skipping API testing

Always verify API responses using Postman.

---

# Interview Questions

## What is Spring Boot?

Spring Boot is a framework that simplifies the development of Spring applications by providing auto-configuration, embedded servers, and production-ready features.

---

## What is JPA?

JPA (Java Persistence API) is a specification for mapping Java objects to relational databases.

---

## What is Hibernate?

Hibernate is the most commonly used implementation of JPA.

---

## What is an Entity?

A Java class mapped to a database table.

---

## Why use Repository?

To abstract database operations and eliminate boilerplate SQL.

---

## Why use Service Layer?

To separate business logic from request handling.

---

## What is Layered Architecture?

A design pattern that separates an application into Controller, Service, Repository, and Database layers.

---

## What are CRUD operations?

Create, Read, Update, Delete.

---

## Why use Postman?

To test REST APIs independently of the frontend.

---

# Key Takeaways

- Learned the structure of a Spring Boot application.
- Understood layered architecture and the responsibility of each layer.
- Connected Spring Boot with PostgreSQL.
- Used Spring Data JPA and Hibernate for persistence.
- Built and tested RESTful CRUD APIs.
- Established a solid backend foundation for future development.
