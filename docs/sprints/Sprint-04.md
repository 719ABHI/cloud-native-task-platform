# Sprint 4 - Docker Fundamentals & Containerization

## Sprint Goal

Learn Docker from scratch, understand containerization, create a production Dockerfile, run the backend and PostgreSQL using Docker Compose, and understand Docker internals before moving towards Kubernetes.

---

# Module 1 - Docker Fundamentals

## Topics Covered

- What is Docker?
- Why Docker is needed
- Docker Engine
- Docker Desktop
- Containers vs Virtual Machines
- Images vs Containers
- Docker Architecture

## What I Learned

- Docker is a containerization platform.
- Containers package an application along with its dependencies.
- Containers share the host operating system kernel.
- Images are read-only templates.
- Containers are running instances of images.
- One image can create multiple containers.

---

# Module 2 - Dockerfile

## Topics Covered

- Dockerfile
- FROM
- WORKDIR
- COPY
- EXPOSE
- ENTRYPOINT
- JDK vs JRE

## Dockerfile Created

```dockerfile
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY build/libs/task-management-backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]
```

## What I Learned

### FROM

- Specifies the base image.
- Our project uses Eclipse Temurin Java 21 JRE.

### WORKDIR

- Sets the working directory inside the image.
- Similar to executing:

```bash
cd /app
```

### COPY

Copies files from the host machine into the image.

```dockerfile
COPY build/libs/task-management-backend-0.0.1-SNAPSHOT.jar app.jar
```

### EXPOSE

- Documents the application port.
- Does not publish the port.

### ENTRYPOINT

Specifies the main process that starts when the container starts.

```dockerfile
ENTRYPOINT ["java","-jar","app.jar"]
```

---

# Module 3 - Docker Compose

## Topics Covered

- Docker Compose
- Services
- Build
- Image
- Environment Variables
- depends_on
- Named Volumes

## Docker Compose Created

```yaml
services:
  backend:
    build: ./task-management-backend
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/cloud_native_task_platform
      SPRING_DATASOURCE_USERNAME: cloud_native_user
      SPRING_DATASOURCE_PASSWORD: Abhi@123
    depends_on:
      - postgres

  postgres:
    image: postgres:16
    environment:
      POSTGRES_DB: cloud_native_task_platform
      POSTGRES_USER: cloud_native_user
      POSTGRES_PASSWORD: Abhi@123
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
```

## What I Learned

- Docker Compose runs multiple containers.
- One compose file can manage backend and PostgreSQL together.
- Docker automatically creates a private network.
- Each service gets its own hostname.

---

# Module 4 - Running Containers

## Commands Used

Build Gradle Project

```bash
./gradlew build
```

Build and Start Containers

```bash
docker compose up --build
```

View Running Containers

```bash
docker ps
```

Open Shell Inside Container

```bash
docker exec -it <container-name> bash
```

Stop Containers

```bash
docker compose down
```

---

# Module 5 - Container Internals

## Topics Covered

- Container Filesystem
- PID 1
- Container Lifecycle

## Commands Used

```bash
pwd

ls

java -version

ps -ef
```

## What I Learned

- The application runs inside `/app`.
- Java becomes PID 1.
- Exiting the shell does not stop the container.
- Stopping the container sends SIGTERM to PID 1.

---

# Module 6 - Docker Storage

## Topics Covered

- Named Volumes
- Bind Mounts

## Named Volume

```yaml
volumes:
  - postgres_data:/var/lib/postgresql/data
```

### Learned

- Named volumes store persistent data.
- Removing a container does not remove the volume.
- PostgreSQL data survives container recreation.

## Bind Mount

Example

```yaml
volumes:
  - ./task-management-backend:/app
```

### Learned

- Bind mounts synchronize files between host and container.
- Useful during development.
- Any changes on the host appear inside the container.
- Mounting over `/app` hides the files originally copied into the image.

---

# Module 7 - Docker Networking

## Topics Covered

- Docker Networks
- Docker DNS
- Service Names
- localhost inside containers
- Port Mapping
- Custom Networks

## What I Learned

- Docker Compose creates a private network automatically.
- Service names become DNS hostnames.
- Containers communicate using service names.
- localhost always refers to the current container.
- Publishing ports is only required for host-to-container communication.
- Container-to-container communication does not require published ports.

Example

Correct

```properties
jdbc:postgresql://postgres:5432/cloud_native_task_platform
```

Wrong

```properties
jdbc:postgresql://localhost:5432/cloud_native_task_platform
```

---

# Module 8 - Multi-stage Builds (Concept)

## Topics Covered

- Why Multi-stage Builds
- Builder Stage
- Runtime Stage
- AS builder
- COPY --from

## What I Learned

- Production images should contain only runtime files.
- Build tools and source code remain in the builder stage.
- Runtime image contains only the application JAR and JRE.
- `AS builder` names the build stage.
- `COPY --from=builder` copies files from another build stage.

---

# Module 9 - Docker Image Layers

## Topics Covered

- Docker Layers
- Docker Build Cache

## What I Learned

- Every Docker instruction creates a new layer.
- Docker caches each layer.
- If one layer changes, that layer and all following layers are rebuilt.
- Previous unchanged layers are reused from cache.
- Proper Dockerfile ordering improves build performance.

---

# Module 10 - .dockerignore (Concept)

## Topics Covered

- Docker Build Context
- .dockerignore

## What I Learned

- `COPY . .` copies everything from the build context.
- `.dockerignore` prevents unnecessary files from entering the build context.
- Similar to `.gitignore`, but used during Docker builds.

---

# Module 11 - Docker Hub (Concept)

## Topics Covered

- Docker Hub
- Repository
- Tags
- latest
- docker pull
- docker push

## What I Learned

- Docker Hub is an online registry for Docker images.
- Images are organized into repositories.
- Tags represent image versions.
- `latest` is only another tag.
- `docker pull` downloads an image.
- `docker push` uploads an image.

---


# Module 12 - Docker Build Optimization

## Topics Covered

- Docker Layer Caching
- Multi-stage Dockerfile
- Image Optimization
- Build Context Optimization

## What I Learned

- Docker caches image layers and reuses unchanged layers.
- Frequently changing files should be copied later in the Dockerfile.
- Dependency installation should be separated from source code copying to maximize cache reuse.
- Multi-stage builds separate build tools from runtime.
- Production images should contain only runtime dependencies.
- `.dockerignore` reduces the Docker build context and improves build performance.

---
# Current Project Status

## Completed

- Dockerfile
- Docker Compose
- Backend Container
- PostgreSQL Container
- Docker Networking
- Docker Volumes
- 

## Concepts Learned

- Multi-stage Builds
- Docker Layers
- Docker Cache
- .dockerignore
- Docker Hub
- Multi-stage Dockerfile
- Docker Layer Caching
- Docker Build Optimization
- .dockerignore
- Docker Hub Concepts
---

## Sprint Outcome

Successfully learned Docker fundamentals, containerization concepts, Docker Compose, networking, storage, image layers, caching, Docker Hub, and prepared the backend for future Kubernetes deployment.