# Sprint 06 Notes

## Why Docker?

Docker packages

Application
+
Dependencies
+
Runtime

inside a Container.

This makes the application run the same on every machine.

---

# Multi-stage Docker Build

Instead of putting everything into one image

Builder Image

↓

Compile application

↓

Runtime Image

↓

Copy only final artifact

Benefits

- Smaller image
- Better security
- Faster deployment

---

# Why use Gradle Wrapper?

Gradle Wrapper ensures

Every developer
CI/CD
Docker
Production

all use the exact same Gradle version.

This guarantees reproducible builds.

---

# Why COPY build.gradle first?

Docker creates one layer for every instruction.

If only Java code changes

↓

Reuse dependency layer

↓

Compile only source code

This significantly speeds up builds.

---

# Docker Layer Cache

Each Docker instruction creates a layer.

If the instruction does not change

↓

Docker reuses that layer

↓

No rebuild required

This reduces build time.

---

# Docker Compose

Docker Compose starts multiple containers together.

Instead of

docker run

docker run

docker run

We write one compose file.

Then run

docker compose up

Docker automatically

- Creates Network
- Creates Containers
- Starts Services

---

# Docker DNS

Each service name becomes a hostname.

Example

backend

postgres

frontend

Backend connects using

jdbc:postgresql://postgres:5432/database

instead of localhost.

---

# Why frontend uses localhost?

React application is built inside Docker

↓

Static files served by Nginx

↓

Browser downloads JavaScript

↓

Browser executes API calls

The browser cannot resolve Docker service names.

Therefore

http://localhost:8080

is used.

---

# ARG vs ENV

ARG

Available only while building Docker image.

Used to inject

VITE_API_URL

ENV

Available while container is running.

Spring Boot uses environment variables at runtime.

---

# Docker Volumes

Without Volume

Delete Container

↓

Database Lost

With Volume

Delete Container

↓

Database remains

Volume stores PostgreSQL data outside container filesystem.

---

# docker compose build

Builds Docker Images.

Does NOT start containers.

---

# docker compose up

Starts containers.

Creates network.

Starts all services.

---

# docker compose down

Stops containers.

Removes containers.

Network removed.

Volume remains.

---

# docker compose down -v

Stops containers.

Deletes volumes.

Database data removed.

---

# Architecture

Browser

↓

React (Nginx)

↓

Spring Boot

↓

PostgreSQL

---

# Sprint Summary

Learned

- Multi-stage Builds
- Docker Layers
- Layer Caching
- Docker Compose
- Docker Networking
- Docker DNS
- Environment Variables
- Build Arguments
- Volumes
- Container Communication

Foundation for Kubernetes is complete.