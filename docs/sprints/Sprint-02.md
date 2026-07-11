[//]: # (# Sprint 02 - API Quality)

[//]: # (## Goal)

[//]: # ()
[//]: # (Improve the API architecture by introducing DTOs, validation, exception handling, API documentation, and logging.)

[//]: # ()
[//]: # (# Story 1 - DTO Layer)

[//]: # ()
[//]: # (## DTO &#40;Data Transfer Object&#41;)

[//]: # ()
[//]: # (### Definition)

[//]: # ()
[//]: # (A DTO &#40;Data Transfer Object&#41; is an object used to transfer data between the client and the server.)

[//]: # ()
[//]: # (### Why use DTO?)

[//]: # ()
[//]: # (- Prevents exposing the database entity directly.)

[//]: # (- Sends only the required fields to the client.)

[//]: # (- Improves security.)

[//]: # (- Decouples the API contract from the database model.)

[//]: # (- Makes future database changes independent of the frontend.)

[//]: # ()
[//]: # (### Request Flow)

[//]: # ()
[//]: # (Client)

[//]: # (↓)

[//]: # (TaskRequest)

[//]: # (↓)

[//]: # (Controller)

[//]: # (↓)

[//]: # (Service)

[//]: # (↓)

[//]: # (Task Entity)

[//]: # (↓)

[//]: # (Repository)

[//]: # (↓)

[//]: # (Database)

[//]: # ()
[//]: # (### Response Flow)

[//]: # ()
[//]: # (Database)

[//]: # (↓)

[//]: # (Repository)

[//]: # (↓)

[//]: # (Task Entity)

[//]: # (↓)

[//]: # (Service)

[//]: # (↓)

[//]: # (TaskResponse)

[//]: # (↓)

[//]: # (Controller)

[//]: # (↓)

[//]: # (Client)

[//]: # ()
[//]: # (## API Changes)

[//]: # ()
[//]: # (### POST /api/tasks)

[//]: # ()
[//]: # (Before)

[//]: # ()
[//]: # (Task Entity)

[//]: # ()
[//]: # (After)

[//]: # ()
[//]: # (TaskRequest -> TaskResponse)

[//]: # ()
[//]: # (---)

[//]: # ()
[//]: # (### GET /api/tasks)

[//]: # ()
[//]: # (Before)

[//]: # ()
[//]: # (List<Task>)

[//]: # ()
[//]: # (After)

[//]: # ()
[//]: # (List<TaskResponse>)

[//]: # ()
[//]: # (---)

[//]: # ()
[//]: # (### GET /api/tasks/{id})

[//]: # ()
[//]: # (Before)

[//]: # ()
[//]: # (Task)

[//]: # ()
[//]: # (After)

[//]: # ()
[//]: # (TaskResponse)

[//]: # ()
[//]: # (---)

[//]: # ()
[//]: # (### PUT /api/tasks/{id})

[//]: # ()
[//]: # (Before)

[//]: # ()
[//]: # (Task)

[//]: # ()
[//]: # (After)

[//]: # ()
[//]: # (TaskRequest -> TaskResponse)

[//]: # ()
[//]: # (## Completed)

[//]: # (- ✅ Introduced TaskRequest DTO)

[//]: # (- ✅ Introduced TaskResponse DTO)

[//]: # (- ✅ Refactored Create Task API to use DTOs)

[//]: # (- ✅ Refactored Get All Tasks API to use DTOs)

[//]: # (- ✅ Refactored Get Task By ID API)

[//]: # (- ✅ Refactored Update Task API to use DTOs)

[//]: # (- ✅ Introduced reusable mapping methods)

[//]: # ()
[//]: # (## Concepts Learned)

[//]: # (- DTO &#40;Data Transfer Object&#41;)

[//]: # (- Entity vs DTO)

[//]: # (- Mapping)

[//]: # (- Why DTO improves API design)

[//]: # ()
[//]: # ()
[//]: # (## Files Modified)

[//]: # (- TaskController.java)

[//]: # (- TaskService.java)

[//]: # (- TaskServiceImpl.java)

[//]: # (- TaskRequest.java)

[//]: # (- TaskResponse.java)

[//]: # ()
[//]: # (### Key Takeaway)

[//]: # ()
[//]: # (Never expose Entity classes directly through REST APIs.)

[//]: # (Use DTOs to define the API contract.)

[//]: # ()
[//]: # (## Outcome)

[//]: # (Successfully introduced a DTO layer, improving API design, security, and maintainability by separating the API contract from the database model.)

[//]: # (## Story 2 - Request Validation)

[//]: # ()
[//]: # (### Concepts Learned)

[//]: # ()
[//]: # (#### Bean Validation)

[//]: # (A framework used to validate incoming request data using annotations.)

[//]: # ()
[//]: # (#### @Valid)

[//]: # (Triggers validation before the controller method executes.)

[//]: # ()
[//]: # (#### @NotBlank)

[//]: # (Ensures a String is neither null, empty, nor contains only whitespace.)

[//]: # ()
[//]: # (#### @NotNull)

[//]: # (Ensures a field is not null.)

[//]: # ()
[//]: # (#### @Size)

[//]: # (Restricts the minimum and maximum length of a String or Collection.)

[//]: # ()
[//]: # (#### @Future)

[//]: # (Ensures the provided date is later than the current date.)

[//]: # ()
[//]: # (### Why Validation?)

[//]: # ()
[//]: # (Validation ensures invalid requests are rejected before reaching the Service layer.)

[//]: # ()
[//]: # (### Flow)

[//]: # ()
[//]: # (Client)

[//]: # (↓)

[//]: # (TaskRequest)

[//]: # (↓)

[//]: # (@Valid)

[//]: # (↓)

[//]: # (Bean Validation)

[//]: # (↓)

[//]: # (Controller)

[//]: # (↓)

[//]: # (Service)

[//]: # ()
[//]: # (If validation fails, Spring throws a)

[//]: # (MethodArgumentNotValidException)

[//]: # (before entering the Controller.)

[//]: # ()
[//]: # (Current Response)

[//]: # ()
[//]: # (- HTTP 400 Bad Request)

[//]: # (- Default Spring Boot validation response)

[//]: # ()
[//]: # (Next Story)

[//]: # ()
[//]: # (Customize validation errors using)

[//]: # (Global Exception Handler.)

[//]: # ()
[//]: # (### Key Takeaway)

[//]: # ()
[//]: # (Validation should happen before the request reaches the Service layer.)

[//]: # ()
[//]: # (## Story 3 - Global Exception Handling)

[//]: # (### Concepts Learned)

[//]: # ()
[//]: # (#### @RestControllerAdvice)

[//]: # (A global class that handles exceptions thrown by all REST controllers.)

[//]: # ()
[//]: # (#### @ExceptionHandler)

[//]: # (Maps a specific exception to a custom handler method.)

[//]: # ()
[//]: # (#### MethodArgumentNotValidException)

[//]: # (Thrown automatically by Spring when Bean Validation fails.)

[//]: # ()
[//]: # (### Why Global Exception Handling?)

[//]: # ()
[//]: # (Without a global handler, every controller would need duplicate error handling code.)

[//]: # ()
[//]: # (A centralized handler provides consistent API responses across the application.)

[//]: # ()
[//]: # ()
[//]: # (### Flow)

[//]: # (Client)

[//]: # ()
[//]: # (↓)

[//]: # ()
[//]: # (@Valid)

[//]: # ()
[//]: # (↓)

[//]: # ()
[//]: # (Validation Failed)

[//]: # ()
[//]: # (↓)

[//]: # ()
[//]: # (MethodArgumentNotValidException)

[//]: # ()
[//]: # (↓)

[//]: # ()
[//]: # (GlobalExceptionHandler)

[//]: # ()
[//]: # (↓)

[//]: # ()
[//]: # (Custom JSON Response)

[//]: # ()
[//]: # (### Key Takeaway)

[//]: # ()
[//]: # (A centralized exception handler keeps error responses consistent and avoids duplicate code.)

[//]: # ()
[//]: # (## Interview Notes)

[//]: # ()
[//]: # (Q. Why use DTO?)

[//]: # ()
[//]: # (A.)

[//]: # (- Prevent exposing Entity.)

[//]: # (- Improve security.)

[//]: # (- Decouple API from database.)

[//]: # (- Send only required fields.)

[//]: # ()
[//]: # (---)

[//]: # ()
[//]: # (Q. When does validation happen?)

[//]: # ()
[//]: # (A.)

[//]: # ()
[//]: # (Validation happens before the controller method executes.)

[//]: # (If validation fails,)

[//]: # (Spring throws MethodArgumentNotValidException.)

[//]: # ()
[//]: # (---)

[//]: # ()
[//]: # (Q. Why make mapping methods private?)

[//]: # ()
[//]: # (A.)

[//]: # ()
[//]: # (They are helper methods used only inside the Service implementation.)

[//]: # ()
[//]: # ()
[//]: # (## Story 4 - Swagger / OpenAPI)

[//]: # ()
[//]: # (### Definition)

[//]: # ()
[//]: # (Swagger is an API documentation tool that automatically generates interactive documentation for REST APIs.)

[//]: # ()
[//]: # (Q. What is Swagger?)

[//]: # ()
[//]: # (A.)

[//]: # ()
[//]: # (Swagger &#40;OpenAPI&#41; automatically generates interactive documentation for REST APIs, allowing developers to understand and test APIs directly from the browser.)

[//]: # ()
[//]: # (### Why Swagger?)

[//]: # ()
[//]: # (- Documents APIs automatically.)

[//]: # (- Allows frontend developers to test APIs.)

[//]: # (- Eliminates manual API documentation.)

[//]: # (- Improves collaboration between frontend and backend teams.)

[//]: # ()
[//]: # (### Completed)

[//]: # ()
[//]: # (- ✅ Integrated Swagger UI)

[//]: # (- ✅ Documented REST APIs)

[//]: # (- ✅ Added @Tag)

[//]: # (- ✅ Added @Operation)

[//]: # ()
[//]: # (---)

[//]: # ()
[//]: # (# Story 5 - Logging)

[//]: # ()
[//]: # (## Concepts Learned)

[//]: # ()
[//]: # (### Logging)

[//]: # ()
[//]: # (Records important application events.)

[//]: # ()
[//]: # (### Logger)

[//]: # ()
[//]: # (Used instead of System.out.println&#40;&#41;.)

[//]: # ()
[//]: # (### Log Levels)

[//]: # ()
[//]: # (- INFO)

[//]: # (- WARN)

[//]: # (- ERROR)

[//]: # ()
[//]: # (### Completed)

[//]: # ()
[//]: # (- ✅ Added Logger)

[//]: # (- ✅ Logged CRUD operations)

[//]: # (- ✅ Learned SLF4J logging)

[//]: # ()
[//]: # (---)

[//]: # ()
[//]: # (# Story 6 - Custom Exceptions)

[//]: # ()
[//]: # (## Concepts Learned)

[//]: # ()
[//]: # (### Custom Exception)

[//]: # ()
[//]: # (Represents business-specific exceptions.)

[//]: # ()
[//]: # (### TaskNotFoundException)

[//]: # ()
[//]: # (Thrown when a task does not exist.)

[//]: # ()
[//]: # (### Completed)

[//]: # ()
[//]: # (- ✅ Created TaskNotFoundException)

[//]: # (- ✅ Returned HTTP 404)

[//]: # ()
[//]: # ()
[//]: # (# Story 7 - Unified Error Response)

[//]: # ()
[//]: # (## Concepts Learned)

[//]: # ()
[//]: # (### ErrorResponse DTO)

[//]: # ()
[//]: # (Provides a consistent structure for all API errors.)

[//]: # ()
[//]: # (### Error Response Structure)

[//]: # ()
[//]: # (- timestamp)

[//]: # (- status)

[//]: # (- message)

[//]: # (- errors)

[//]: # ()
[//]: # (### Completed)

[//]: # ()
[//]: # (- ✅ Created ErrorResponse DTO)

[//]: # (- ✅ Unified validation errors)

[//]: # (- ✅ Unified not-found errors)

[//]: # ()
[//]: # (---)

[//]: # ()
[//]: # (# Files Added)

[//]: # ()
[//]: # (- TaskRequest.java)

[//]: # (- TaskResponse.java)

[//]: # (- ErrorResponse.java)

[//]: # (- TaskNotFoundException.java)

[//]: # (- GlobalExceptionHandler.java)

[//]: # ()
[//]: # (# Files Modified)

[//]: # (- TaskController.java)

[//]: # (- TaskService.java)

[//]: # (- TaskServiceImpl.java)

[//]: # (- build.gradle)

[//]: # ()
[//]: # (---)

[//]: # ()
[//]: # (# Interview Questions)

[//]: # ()
[//]: # (### Why use DTO?)

[//]: # ()
[//]: # (To separate the API contract from the database model.)

[//]: # ()
[//]: # (---)

[//]: # ()
[//]: # (### Why Bean Validation?)

[//]: # ()
[//]: # (To reject invalid requests before reaching the Service layer.)

[//]: # ()
[//]: # (---)

[//]: # ()
[//]: # (### Why Global Exception Handler?)

[//]: # ()
[//]: # (To centralize exception handling and avoid duplicate code.)

[//]: # ()
[//]: # (---)

[//]: # ()
[//]: # (### Why Swagger?)

[//]: # ()
[//]: # (To automatically generate API documentation.)

[//]: # ()
[//]: # (---)

[//]: # ()
[//]: # (### Why Logging?)

[//]: # ()
[//]: # (To record application events for monitoring and debugging.)

[//]: # ()
[//]: # (---)

[//]: # ()
[//]: # (### Why Custom Exceptions?)

[//]: # ()
[//]: # (To represent business-specific errors clearly.)

[//]: # ()
[//]: # (---)

[//]: # ()
[//]: # (### Why ErrorResponse DTO?)

[//]: # ()
[//]: # (To provide a consistent error response structure across all APIs.)

[//]: # ()
[//]: # (---)

[//]: # ()
[//]: # (# Outcome)

[//]: # ()
[//]: # (Successfully improved the backend architecture by introducing DTOs, validation, global exception handling, Swagger documentation, logging, custom exceptions, and a unified error response model.)


# Sprint 02 – API Quality

| Item | Details |
|------|---------|
| **Sprint** | Sprint 02 |
| **Status** | ✅ Completed |
| **Focus Area** | API Quality & Backend Best Practices |
| **Technologies** | Spring Boot, Bean Validation, Spring Data JPA, Swagger/OpenAPI, SLF4J |

---

## Objective

Improve the quality, maintainability, and usability of the REST APIs by introducing DTOs, request validation, centralized exception handling, API documentation, structured logging, and consistent error responses while following backend development best practices.

---

## Features Implemented

During this sprint, the following improvements were made to the backend:

- Introduced DTOs for request and response handling.
- Refactored REST APIs to use DTOs instead of exposing entities.
- Added Bean Validation for validating incoming requests.
- Implemented centralized exception handling using `@RestControllerAdvice`.
- Added Swagger/OpenAPI documentation for REST APIs.
- Integrated structured logging using SLF4J.
- Created custom business exceptions.
- Introduced a unified error response model for consistent API responses.

---

## Implementation Summary

The initial CRUD application built in Sprint 01 exposed entity objects directly through the REST API. In this sprint, the API architecture was enhanced by introducing Data Transfer Objects (DTOs), separating the API contract from the persistence layer.

Request validation was added to reject invalid input before it reached the business logic. A global exception handling mechanism was implemented to ensure consistent and meaningful error responses across the application.

To improve API usability and developer experience, Swagger/OpenAPI documentation was integrated. Structured logging replaced console output, making application events easier to trace and debug.

These enhancements significantly improved the maintainability, security, and scalability of the backend.

---

## Architecture Improvements

### Before Sprint 02

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

---

### After Sprint 02

```
Client
   │
   ▼
TaskRequest DTO
   │
   ▼
Controller
   │
   ▼
Service
   │
   ▼
Task Entity
   │
   ▼
Repository
   │
   ▼
Database
   │
   ▼
Task Entity
   │
   ▼
Service
   │
   ▼
TaskResponse DTO
   │
   ▼
Client
```

Additional components introduced during request processing:

- Bean Validation
- Global Exception Handler
- Logging
- Swagger/OpenAPI

---

## Project Structure Updates

The following packages and classes were introduced during this sprint:

```
task-management-backend/
│
├── dto/
│   ├── TaskRequest.java
│   ├── TaskResponse.java
│   └── ErrorResponse.java
│
├── exception/
│   ├── GlobalExceptionHandler.java
│   └── TaskNotFoundException.java
```

Existing Controller and Service layers were updated to support the new architecture.

---

## Files Added

- TaskRequest.java
- TaskResponse.java
- ErrorResponse.java
- TaskNotFoundException.java
- GlobalExceptionHandler.java

---

## Files Modified

- TaskController.java
- TaskService.java
- TaskServiceImpl.java
- build.gradle

---

## API Improvements

The existing CRUD APIs were enhanced with the following improvements:

- Request DTOs
- Response DTOs
- Bean Validation
- Structured validation error messages
- Custom exception handling
- Swagger documentation
- Structured logging

No endpoint URLs changed during this sprint.

---

## Challenges Faced

Several architectural improvements required refactoring the existing codebase.

Key challenges included:

- Refactoring CRUD APIs without changing their functionality.
- Mapping between Entity and DTO objects.
- Understanding Spring's validation lifecycle.
- Designing centralized exception handling.
- Standardizing API error responses.
- Integrating Swagger documentation.
- Replacing console output with structured logging.

---

## Lessons Learned

- Importance of separating API models from persistence models.
- Benefits of request validation in REST APIs.
- Centralizing exception handling for cleaner controller code.
- Designing consistent API responses.
- Documenting APIs using Swagger/OpenAPI.
- Using structured logging for debugging and monitoring.
- Improving backend maintainability through clean architecture principles.

---

## Outcome

Sprint 02 transformed the basic CRUD application into a more robust and production-oriented backend by applying industry-standard backend development practices.

The APIs became more secure, maintainable, and developer-friendly through the introduction of DTOs, validation, centralized exception handling, logging, API documentation, custom exceptions, and standardized error responses.

---

## Next Sprint

Sprint 03 focuses on implementing authentication and authorization, including:

- User Entity
- Spring Security
- JWT Authentication
- Login & Registration APIs
- Role-Based Authorization
- Task Ownership