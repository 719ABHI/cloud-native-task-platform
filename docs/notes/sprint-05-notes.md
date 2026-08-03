# Sprint 5 Notes — React Frontend

## Goal

Build a frontend that communicates with our Spring Boot backend through REST APIs.

The frontend is responsible only for:

- Displaying data
- Collecting user input
- Sending API requests
- Showing responses

Business logic stays inside Spring Boot.

---

# Final Architecture

Browser
        │
        ▼
React (Frontend)
        │
        │ HTTP (Axios)
        ▼
Spring Boot Backend
        │
        ▼
PostgreSQL

React never communicates directly with PostgreSQL.

Everything goes through Spring Boot.

---

# Folder Structure

src/

components/
Reusable UI Components

Header.jsx
Sidebar.jsx
ProtectedRoute.jsx

layouts/

DashboardLayout.jsx

pages/

Login.jsx
Register.jsx
Dashboard.jsx
Tasks.jsx

services/

authService.js
taskService.js

utils/

token.js

main.jsx

App.jsx

---

# Responsibilities

main.jsx

Entry point of React.

Starts the application.

↓

App.jsx

Contains all routes.

↓

DashboardLayout

Common layout for authenticated users.

Contains

Header

Sidebar

Outlet

Outlet renders the current page.

Example

Dashboard

Tasks

---

# Pages

Login

Calls

POST /api/auth/login

Receives

JWT

Stores

JWT
User

inside Local Storage.

---

Register

Calls

POST /api/auth/register

Creates a new user.

---

Dashboard

Displays

Total Tasks

Todo

In Progress

Done

High Priority

Overdue

Uses

GET /api/tasks

Calculates statistics inside React.

---

Tasks

Supports

Create

Update

Delete

Search

Filter

Sort

Loading

Empty State

Communicates with backend using REST APIs.

---

# Services

Services contain all API calls.

Example

taskService.js

↓

GET

POST

PUT

DELETE

using Axios.

Pages never call Axios directly.

Pages call Services.

---

# Axios Flow

User clicks

Create Task

↓

Tasks.jsx

↓

createTask()

↓

taskService.js

↓

Axios

↓

Spring Boot

↓

PostgreSQL

↓

Response

↓

React updates UI

---

# JWT Authentication Flow

Login

↓

Backend verifies user

↓

Backend returns JWT

↓

React stores

JWT

User

↓

Every API request

Authorization

Bearer <token>

↓

Spring Security

↓

JWT Filter

↓

Controller

---

# Protected Routes

Without Token

↓

Redirect Login

With Token

↓

Allow Dashboard

This is handled by

ProtectedRoute.jsx

---

# Dashboard Layout

Header

Sidebar

Content

All pages reuse the same layout.

Only the center content changes.

---

# State (useState)

Stores data that changes.

Examples

Tasks

Dialog Open

Search

Filters

Sorting

Form values

Whenever state changes

React automatically re-renders.

---

# useEffect

Runs side effects.

Used to

Load tasks

Load dashboard

Check login

Example

Component Opens

↓

useEffect()

↓

loadTasks()

↓

Backend

↓

Display Data

---

# Search

Frontend only.

Uses

filter()

No backend request.

---

# Filter

Frontend only.

Status

Priority

---

# Sort

Frontend only.

Newest

Oldest

Due Date

---

# Loading

While API is running

↓

Show CircularProgress

After response

↓

Render table

---

# Empty State

If no tasks

↓

Show

No Tasks Found

instead of empty table.

---

# Local Storage

Stores

JWT

User

Functions

saveToken()

getToken()

removeToken()

saveUser()

getUser()

removeUser()

---

# Overall Flow

Browser

↓

React Page

↓

Service

↓

Axios

↓

Spring Boot

↓

Service Layer

↓

Repository

↓

PostgreSQL

↓

Response

↓

React UI Updates

---

# Important Files

main.jsx

Application Entry

App.jsx

Routing

DashboardLayout.jsx

Common Layout

ProtectedRoute.jsx

Authentication

Header.jsx

Top Navigation

Sidebar.jsx

Side Navigation

Dashboard.jsx

Statistics

Tasks.jsx

CRUD

taskService.js

Task APIs

authService.js

Authentication APIs

token.js

JWT Storage

---

# What React Does

Displays UI

Collects User Input

Calls Backend APIs

Stores Temporary State

Updates Screen

Nothing else.

Business Logic

Validation

Security

Database

remain inside Spring Boot.

---

# Interview Summary

React

↓

Frontend

Spring Boot

↓

Business Logic

PostgreSQL

↓

Database

JWT

↓

Authentication

Axios

↓

Communication

Material UI

↓

UI Components

Protected Route

↓

Frontend Authentication

Dashboard Layout

↓

Reusable Layout

Services

↓

API Layer

React State

↓

Temporary UI Data