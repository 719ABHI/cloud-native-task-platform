# Sprint 02 - API Quality

## Goal

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

## Concepts Learned

## Files Modified

## API Changes

## Interview Notes

## Outcome
