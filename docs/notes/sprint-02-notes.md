# Sprint 02 Notes – API Quality

## Sprint Overview

Sprint 02 focused on improving the quality, maintainability, and usability of the REST APIs developed in Sprint 01. The application was enhanced by introducing DTOs, request validation, global exception handling, Swagger documentation, structured logging, custom exceptions, and a unified error response model.

---

# Concepts Covered

- DTO (Data Transfer Object)
- DTO Mapping
- Bean Validation
- Request Validation
- Global Exception Handling
- Custom Exceptions
- ErrorResponse DTO
- Swagger / OpenAPI
- Logging using SLF4J

---

# DTO (Data Transfer Object)

## Definition

A DTO (Data Transfer Object) is an object used to transfer data between the client and the server without exposing the internal database entity.

---

## Why use DTO?

- Prevents exposing database entities directly.
- Sends only the required fields.
- Improves security.
- Decouples the API contract from the database model.
- Makes backend changes independent of the frontend.
- Improves API maintainability.

---

## Request Flow

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
```

---

## Response Flow

```
Database
   │
   ▼
Repository
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
Controller
   │
   ▼
Client
```

---

## Entity vs DTO

| Entity | DTO |
|---------|-----|
| Represents database table | Represents API data |
| Managed by Hibernate | Not managed by Hibernate |
| Used for persistence | Used for request/response |
| May contain sensitive fields | Contains only required fields |

---

## Best Practices

- Never expose Entities through REST APIs.
- Create separate Request and Response DTOs.
- Keep DTOs independent of the database structure.

---

## Common Mistakes

- Returning Entity objects directly.
- Reusing the same DTO for every operation.
- Adding business logic inside DTO classes.

---

# DTO Mapping

## Definition

Mapping is the process of converting between Entity objects and DTOs.

```
TaskRequest
      │
      ▼
Task Entity
      │
      ▼
Database
      │
      ▼
Task Entity
      │
      ▼
TaskResponse
```

---

## Why Mapping?

- Separates persistence from API models.
- Improves flexibility.
- Makes future changes easier.

---

## Best Practice

Keep mapping methods inside the Service layer or use a dedicated mapper class.

---

# Bean Validation

## Definition

Bean Validation is a framework that validates incoming request data using annotations.

Validation occurs before the request reaches the Service layer.

---

## Common Validation Annotations

### @Valid

Triggers validation before the controller method executes.

---

### @NotBlank

Ensures a String is not null, empty, or only whitespace.

---

### @NotNull

Ensures a field is not null.

---

### @Size

Restricts the minimum and maximum length of Strings or Collections.

---

### @Future

Ensures the provided date is later than the current date.

---

## Validation Flow

```
Client
   │
   ▼
TaskRequest
   │
   ▼
@Valid
   │
   ▼
Bean Validation
   │
   ▼
Controller
   │
   ▼
Service
```

If validation fails:

```
MethodArgumentNotValidException
```

is thrown before entering the controller method.

---

## Best Practices

- Validate all client input.
- Keep validation rules inside DTOs.
- Return meaningful validation errors.

---

## Common Mistakes

- Forgetting to use `@Valid`.
- Validating inside the Service instead of the DTO.
- Returning unclear validation messages.

---

# Global Exception Handling

## Definition

Global Exception Handling centralizes exception processing for all REST controllers using `@RestControllerAdvice`.

---

## Why use it?

- Eliminates duplicate try-catch blocks.
- Keeps Controllers clean.
- Returns consistent error responses.
- Centralizes error handling logic.

---

## Important Annotations

### @RestControllerAdvice

Applies exception handling across all REST controllers.

---

### @ExceptionHandler

Handles a specific exception type.

---

## Flow

```
Client
   │
   ▼
Validation
   │
   ▼
Exception
   │
   ▼
GlobalExceptionHandler
   │
   ▼
Custom JSON Response
```

---

## Best Practices

- Handle exceptions centrally.
- Return appropriate HTTP status codes.
- Provide meaningful error messages.

---

## Common Mistakes

- Catching every exception inside Controllers.
- Returning inconsistent error responses.
- Exposing internal exception details.

---

# Custom Exceptions

## Definition

A Custom Exception represents business-specific errors instead of generic Java exceptions.

Example:

```
TaskNotFoundException
```

---

## Why use Custom Exceptions?

- Improves readability.
- Represents business rules clearly.
- Makes debugging easier.
- Provides meaningful API responses.

---

## Best Practices

- Create one exception per business scenario.
- Name exceptions clearly.
- Handle them using Global Exception Handler.

---

# ErrorResponse DTO

## Definition

A standardized object used to return error information to API clients.

---

## Structure

```
timestamp

status

message

errors
```

---

## Benefits

- Consistent API responses.
- Easier frontend integration.
- Better debugging.
- Improved maintainability.

---

# Swagger / OpenAPI

## Definition

Swagger automatically generates interactive documentation for REST APIs.

---

## Why use Swagger?

- Documents APIs automatically.
- Allows API testing from the browser.
- Helps frontend developers understand APIs.
- Reduces manual documentation effort.

---

## Common Annotations

### @Tag

Groups related APIs.

---

### @Operation

Documents an API endpoint.

---

## Best Practices

- Document every public endpoint.
- Keep API descriptions clear.
- Update documentation when APIs change.

---

# Logging

## Definition

Logging records important application events during execution.

Instead of:

```java
System.out.println();
```

use:

```java
Logger
```

---

## Log Levels

- INFO
- WARN
- ERROR
- DEBUG
- TRACE

---

## Why Logging?

- Debugging
- Monitoring
- Auditing
- Production troubleshooting

---

## Best Practices

- Log meaningful events.
- Never log passwords or sensitive information.
- Use appropriate log levels.

---

## Common Mistakes

- Excessive logging.
- Logging sensitive data.
- Using `System.out.println()` in production.

---

# API Improvements Introduced

Compared to Sprint 01:

- DTO-based API design
- Request validation
- Global exception handling
- Swagger documentation
- Structured logging
- Custom business exceptions
- Unified API error responses

---

# Best Practices Learned

- Never expose Entities directly.
- Validate input before processing.
- Centralize exception handling.
- Standardize error responses.
- Document APIs.
- Replace console output with structured logging.
- Keep Controllers lightweight.

---

# Common Mistakes

- Returning Entity objects directly.
- Skipping validation.
- Duplicating exception handling.
- Using generic exceptions everywhere.
- Mixing API models with persistence models.
- Logging sensitive information.

---

# Interview Questions

## What is a DTO?

A DTO is an object used to transfer data between the client and server without exposing database entities.

---

## Why use DTO?

- Security
- Loose coupling
- API flexibility
- Better maintainability

---

## Why use Bean Validation?

To reject invalid requests before reaching the business logic.

---

## What does `@Valid` do?

It triggers Bean Validation before the controller method executes.

---

## What is `MethodArgumentNotValidException`?

It is thrown automatically when Bean Validation fails.

---

## What is `@RestControllerAdvice`?

A global exception handling mechanism for REST controllers.

---

## Why use Custom Exceptions?

To represent business-specific errors clearly.

---

## Why use an ErrorResponse DTO?

To provide a consistent structure for all API error responses.

---

## Why use Swagger?

To automatically generate interactive API documentation.

---

## Why use Logging?

To monitor application behavior, debug issues, and troubleshoot production systems.

---

## Key Takeaways

- Understood the importance of separating API models from persistence models.
- Learned DTO-based API design.
- Implemented request validation using Bean Validation.
- Centralized exception handling using `@RestControllerAdvice`.
- Created business-specific custom exceptions.
- Standardized API error responses.
- Integrated Swagger/OpenAPI documentation.
- Implemented structured logging using SLF4J.
- Improved the overall quality, maintainability, and scalability of the backend.
