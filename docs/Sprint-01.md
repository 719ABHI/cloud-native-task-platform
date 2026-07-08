# Sprint 01 - Backend Foundation

## Goal

Build the foundation of the Cloud Native Task Management Platform.

---

## Completed

- Spring Boot project setup using Gradle
- PostgreSQL integration
- Hibernate and Spring Data JPA configuration
- Created Task entity
- Created Task Repository
- Created Service layer
- Created Controller layer
- Implemented complete CRUD APIs
- Tested APIs using Postman
- Verified database operations in PostgreSQL

---

## Concepts Learned

### Layered Architecture

Client
↓
Controller
↓
Service
↓
Repository
↓
Database

### Entity

- Represents a database table.
- Managed by Hibernate.
- Used for database operations.

### Repository

- Communicates with the database.
- Hibernate generates SQL queries from repository methods.

### Service

- Contains business logic.
- Acts as a bridge between Controller and Repository.

### Controller

- Handles HTTP requests and responses.
- Delegates business logic to the Service layer.

### CRUD APIs

- POST Create Task
- GET All Tasks
- GET Task By ID
- PUT Update Task
- DELETE Task

---

## Interview Notes

- Hibernate converts Entity operations into SQL queries.
- Spring Data JPA provides methods such as:
  - save()
  - findAll()
  - findById()
  - delete()

---

## Outcome

Successfully built and tested a complete Spring Boot CRUD application with PostgreSQL.
