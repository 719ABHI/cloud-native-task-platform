# Sprint 5 — React Frontend

## Sprint Goal

Build a complete React frontend for the Cloud Native Task Platform and integrate it with the Spring Boot backend using JWT authentication.

---

# Objectives

- Build frontend using React + Vite
- Integrate with Spring Boot REST APIs
- Implement JWT authentication
- Create Dashboard
- Implement Task CRUD
- Protect routes
- Connect frontend with backend
- Prepare application for Dockerization

---

# Features Completed

## Authentication

- User Registration
- User Login
- JWT Authentication
- Protected Routes
- Logout
- Local Storage for JWT
- Local Storage for User Information

---

## Dashboard

- Dynamic Welcome Message
- Total Tasks
- Todo Tasks
- In Progress Tasks
- Done Tasks
- High Priority Tasks
- Overdue Tasks

---

## Task Management

- Create Task
- View Tasks
- Update Task
- Delete Task

---

## Task Features

- Search Tasks
- Filter by Status
- Filter by Priority
- Sort by
    - Newest
    - Oldest
    - Due Date

---

## User Experience

- Loading Spinner
- Empty State
- Responsive Dashboard Layout
- Header
- Sidebar Navigation

---

# Backend Improvements

- LoginResponse now returns User Information
- User-specific Task Management
- Each Task belongs to one User
- Repository updated for user-based queries
- Service layer updated to use authenticated user

---

# Project Structure

task-management-frontend/

- components/
- layouts/
- pages/
- services/
- utils/

---

# APIs Used

Authentication

POST /api/auth/register

POST /api/auth/login

Task APIs

GET /api/tasks

POST /api/tasks

PUT /api/tasks/{id}

DELETE /api/tasks/{id}

---

# Technologies Used

React

Vite

Material UI

Axios

React Router

Local Storage

JWT

Spring Boot REST API

---

# Outcome

The application is now a complete full-stack web application.

Frontend and Backend communicate using REST APIs secured with JWT authentication.

Each authenticated user can manage only their own tasks.

The project is now ready for containerization using Docker.

---

# Sprint Status

Sprint 5

✅ COMPLETED

Next Sprint

🚀 Sprint 6 — Docker