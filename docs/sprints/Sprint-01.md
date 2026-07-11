[//]: # (# Sprint 01 - Backend Foundation)

[//]: # ()
[//]: # (## Goal)

[//]: # ()
[//]: # (Build the foundation of the Cloud Native Task Management Platform.)

[//]: # ()
[//]: # (---)

[//]: # ()
[//]: # (## Completed)

[//]: # ()
[//]: # (- Spring Boot project setup using Gradle)

[//]: # (- PostgreSQL integration)

[//]: # (- Hibernate and Spring Data JPA configuration)

[//]: # (- Created Task entity)

[//]: # (- Created Task Repository)

[//]: # (- Created Service layer)

[//]: # (- Created Controller layer)

[//]: # (- Implemented complete CRUD APIs)

[//]: # (- Tested APIs using Postman)

[//]: # (- Verified database operations in PostgreSQL)

[//]: # ()
[//]: # (---)

[//]: # ()
[//]: # (## Concepts Learned)

[//]: # ()
[//]: # (### Layered Architecture)

[//]: # ()
[//]: # (Client)

[//]: # (↓)

[//]: # (Controller)

[//]: # (↓)

[//]: # (Service)

[//]: # (↓)

[//]: # (Repository)

[//]: # (↓)

[//]: # (Database)

[//]: # ()
[//]: # (### Entity)

[//]: # ()
[//]: # (- Represents a database table.)

[//]: # (- Managed by Hibernate.)

[//]: # (- Used for database operations.)

[//]: # ()
[//]: # (### Repository)

[//]: # ()
[//]: # (- Communicates with the database.)

[//]: # (- Hibernate generates SQL queries from repository methods.)

[//]: # ()
[//]: # (### Service)

[//]: # ()
[//]: # (- Contains business logic.)

[//]: # (- Acts as a bridge between Controller and Repository.)

[//]: # ()
[//]: # (### Controller)

[//]: # ()
[//]: # (- Handles HTTP requests and responses.)

[//]: # (- Delegates business logic to the Service layer.)

[//]: # ()
[//]: # (### CRUD APIs)

[//]: # ()
[//]: # (- POST Create Task)

[//]: # (- GET All Tasks)

[//]: # (- GET Task By ID)

[//]: # (- PUT Update Task)

[//]: # (- DELETE Task)

[//]: # ()
[//]: # (---)

[//]: # ()
[//]: # (## Interview Notes)

[//]: # ()
[//]: # (- Hibernate converts Entity operations into SQL queries.)

[//]: # (- Spring Data JPA provides methods such as:)

[//]: # (  - save&#40;&#41;)

[//]: # (  - findAll&#40;&#41;)

[//]: # (  - findById&#40;&#41;)

[//]: # (  - delete&#40;&#41;)

[//]: # ()
[//]: # (---)

[//]: # ()
[//]: # (## Outcome)

[//]: # ()
[//]: # (Successfully built and tested a complete Spring Boot CRUD application with PostgreSQL.)


# Sprint 01 – Backend Foundation

| Item | Details |
|------|---------|
| **Sprint** | Sprint 01 |
| **Status** | ✅ Completed |
| **Focus Area** | Backend Foundation |
| **Technologies** | Java 21, Spring Boot, Spring Data JPA, Hibernate, PostgreSQL, Gradle |

---

## Objective

Establish the backend foundation of the Cloud Native Task Management Platform by setting up the development environment, configuring the database, implementing a layered architecture, and developing the first version of the Task Management REST API.

---

## Features Implemented

During this sprint, the following features were implemented:

- Initialized the Spring Boot project using Gradle.
- Configured PostgreSQL as the primary database.
- Integrated Spring Data JPA and Hibernate for data persistence.
- Implemented a layered architecture consisting of Controller, Service, Repository, and Entity layers.
- Created the `Task` entity and mapped it to the database.
- Implemented the repository layer using Spring Data JPA.
- Developed the service layer to encapsulate business logic.
- Created REST controllers to expose CRUD endpoints.
- Implemented Create, Read, Update, and Delete (CRUD) operations for tasks.
- Tested all APIs using Postman.
- Verified database persistence using PostgreSQL.

---

## Implementation Summary

The backend project was initialized using Spring Boot with Gradle as the build tool. PostgreSQL was configured as the relational database, while Spring Data JPA and Hibernate were used to simplify database interactions.

A layered architecture was adopted to separate responsibilities across different application layers:

- **Controller Layer** – Handles HTTP requests and responses.
- **Service Layer** – Contains business logic.
- **Repository Layer** – Performs database operations.
- **Entity Layer** – Represents database tables.

This architecture improves maintainability, readability, and scalability by ensuring each layer has a single responsibility.

The sprint concluded with a fully functional REST API supporting CRUD operations for task management.

---

## Architecture Overview

```
                Client
                   │
                   ▼
            REST Controller
                   │
                   ▼
             Service Layer
                   │
                   ▼
           Repository Layer
                   │
                   ▼
              PostgreSQL
```

---

## Project Structure Updates

The following backend packages were introduced during this sprint:

```
task-management-backend/
│
├── controller/
├── entity/
├── repository/
├── service/
│   └── impl/
├── config/
└── enums/
```

---

## Files Created

- TaskManagementBackendApplication.java
- Task.java
- TaskRepository.java
- TaskService.java
- TaskServiceImpl.java
- TaskController.java
- application.properties
- build.gradle

---

## APIs Implemented

| Method | Endpoint | Description |
|----------|----------------|---------------------------|
| POST | `/api/tasks` | Create a new task |
| GET | `/api/tasks` | Retrieve all tasks |
| GET | `/api/tasks/{id}` | Retrieve a task by ID |
| PUT | `/api/tasks/{id}` | Update an existing task |
| DELETE | `/api/tasks/{id}` | Delete a task |

---

## Challenges Faced

During development, several configuration and implementation issues were encountered and resolved.

- Configuring the PostgreSQL datasource correctly.
- Understanding the responsibilities of each layer in the Spring Boot architecture.
- Resolving the JPA entity configuration issue caused by a missing `@Id` annotation.
- Verifying Hibernate-generated SQL and ensuring successful database persistence.

These issues helped establish a stronger understanding of Spring Boot fundamentals and JPA entity mapping.

---

## Lessons Learned

- How to initialize a Spring Boot project using Gradle.
- How Spring Boot integrates with PostgreSQL using Spring Data JPA.
- Benefits of layered architecture in backend applications.
- How Hibernate maps Java entities to relational database tables.
- Implementation of RESTful CRUD APIs using Spring Boot.
- Testing REST APIs using Postman.
- Verifying application data directly in PostgreSQL.

---

## Outcome

Sprint 01 established the core backend foundation of the Cloud Native Task Management Platform.

At the end of this sprint, the application successfully exposed RESTful CRUD APIs backed by PostgreSQL, following a layered architecture that provides a solid foundation for future enhancements such as DTOs, validation, exception handling, authentication, and cloud-native deployment.

---

## Next Sprint

Sprint 02 focuses on improving API quality by introducing:

- DTO-based API design
- Request validation
- Global exception handling
- Swagger/OpenAPI documentation
- Structured logging
- Custom exceptions
- Unified API error responses