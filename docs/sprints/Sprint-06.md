# Sprint 06 – Dockerizing the Cloud Native Task Platform

## Objective

Containerize the complete Cloud Native Task Platform so that the entire application can be started using a single Docker Compose command.

---

# Goals

- Dockerize Spring Boot Backend
- Dockerize React Frontend
- Create Multi-stage Dockerfiles
- Optimize image size
- Configure Docker Compose
- Connect containers using Docker Network
- Configure environment variables
- Persist PostgreSQL data using Docker Volumes

---

# Backend

Implemented a multi-stage Docker build.

Stage 1
- Gradle Builder Image
- Download Dependencies
- Build Spring Boot Executable JAR

Stage 2
- Eclipse Temurin JRE Image
- Copy only executable JAR
- Run application

Benefits

- Small runtime image
- Faster deployment
- No Gradle present in production image

---

# Frontend

Implemented a multi-stage Docker build.

Stage 1

- Node 22 Alpine
- Install npm dependencies
- Build React application

Stage 2

- Nginx Alpine
- Copy React build files
- Serve static application

Benefits

- Small image
- Production-ready web server
- Fast static file serving

---

# Docker Compose

Created docker-compose.yml containing

- PostgreSQL
- Spring Boot Backend
- React Frontend

Configured

- Networks
- Volumes
- Environment Variables
- Container Dependencies

---

# Docker Networking

Backend communicates with PostgreSQL using Docker DNS.

Example

postgres:5432

instead of

localhost:5432

Frontend communicates with Backend using

http://localhost:8080

because API calls originate from the browser.

---

# Docker Volumes

Configured persistent PostgreSQL storage.

Named Volume

postgres_data

Benefits

- Data persists after container restart
- Safe database storage

---

# Environment Variables

Backend

- SPRING_DATASOURCE_URL
- SPRING_DATASOURCE_USERNAME
- SPRING_DATASOURCE_PASSWORD

Frontend

- VITE_API_URL

Injected during Docker image build.

---

# Problems Solved

- Docker networking
- localhost vs service names
- CORS configuration
- React Build Arguments
- Environment Variables
- Docker Layer Caching
- Multi-stage Docker Builds
- Docker Compose orchestration

---

# Result

Application successfully runs using

docker compose up

Components

Browser

↓

React (Nginx)

↓

Spring Boot

↓

PostgreSQL

All services communicate successfully.

---

# Sprint Outcome

Completed Dockerization of the Cloud Native Task Platform.

Ready for Sprint 07 – Kubernetes.