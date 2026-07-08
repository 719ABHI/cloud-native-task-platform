# Sprint 02 - API Quality

## Goal

Improve the API architecture by introducing DTOs.

## DTO (Data Transfer Object)

### Definition

A DTO (Data Transfer Object) is an object used to transfer data between the client and the server.

### Why use DTO?

- Prevents exposing the database entity directly.
- Sends only the required fields to the client.
- Improves security.
- Decouples the API contract from the database model.
- Makes future database changes independent of the frontend.

### Request Flow

Client
↓
TaskRequest
↓
Controller
↓
Service
↓
Task Entity
↓
Repository
↓
Database

### Response Flow

Database
↓
Repository
↓
Task Entity
↓
Service
↓
TaskResponse
↓
Controller
↓
Client

## Completed
- ✅ Introduced TaskRequest DTO
- ✅ Introduced TaskResponse DTO
- ✅ Refactored Create Task API to use DTOs
- ✅ Refactored Get All Tasks API to use DTOs
- ✅ Refactored Get Task By ID API
- ✅ Refactored Update Task API to use DTOs
- ✅ Introduced reusable mapping methods

## Concepts Learned
- DTO (Data Transfer Object)
- Entity vs DTO
- Mapping
- Why DTO improves API design


## Files Modified
- TaskController.java
- TaskService.java
- TaskServiceImpl.java
- TaskRequest.java
- TaskResponse.java

## API Changes

## Interview Notes

## Outcome
Successfully separated the API contract from the database model using DTOs.
