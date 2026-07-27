# Docker Compose

So far we have learned how to create an image using a Dockerfile.

Now imagine our project.

Our Cloud Native Task Platform requires:

- Spring Boot Backend
- PostgreSQL Database

Can we start them separately?

Yes.

Example

Terminal 1

```bash
docker run backend
```

Terminal 2

```bash
docker run postgres
```

Now imagine a real application.

```
Backend
↓

PostgreSQL
↓

Redis
↓

Frontend
↓

Nginx
```

Starting every container manually becomes difficult.

Docker Compose solves this problem.

---

# What is Docker Compose?

Docker Compose is a tool used to define and run multiple Docker containers using a single YAML file.

Instead of writing multiple docker run commands,

we write one compose file.

Then simply execute

```bash
docker compose up
```

Docker automatically starts every required container.

---

# Why Docker Compose?

Without Docker Compose

```
docker run postgres

docker run backend

docker run redis

docker run nginx
```

Everything must be managed manually.

---

With Docker Compose

```
docker compose up
```

Everything starts automatically.

---

# Docker Compose File

Our project

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

---

# Compose Structure

A compose file mainly consists of

```
services

volumes

networks (optional)
```

In our project we used

- services
- volumes

Docker automatically created the network.

---

# Services

Every container is defined as a service.

Example

```
services:

backend

postgres
```

Think of a service as a blueprint describing how a container should run.

Each service has

- Image
- Build
- Ports
- Environment Variables
- Volumes
- Dependencies

---

# build

Example

```yaml
build: ./task-management-backend
```

Meaning

Docker should build an image using the Dockerfile present inside

```
task-management-backend/
```

before creating the container.

Use build when

- You have source code.
- You need Docker to build your image.

---

# image

Example

```yaml
image: postgres:16
```

Meaning

Do not build anything.

Simply download the image from Docker Hub if it is not already available.

Use image when

The application already exists on Docker Hub.

Example

```
postgres

mysql

redis

nginx
```

---

# build vs image

| build | image |
|--------|--------|
| Builds an image | Downloads an existing image |
| Uses Dockerfile | Uses Docker Hub |
| Used for our backend | Used for PostgreSQL |

---

# ports

Example

```yaml
ports:
  - "8080:8080"
```

Format

```
Host Port : Container Port
```

Meaning

```
Laptop

localhost:8080

↓

Backend Container

8080
```

The left side belongs to the Host.

The right side belongs to the Container.

---

# Why Port Mapping?

Suppose the backend listens on port 8080 inside the container.

Without publishing the port,

your browser cannot access it.

Port Mapping creates a bridge.

```
Browser

↓

localhost:8080

↓

Backend Container

8080
```

---

# environment

Example

```yaml
environment:
  SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/cloud_native_task_platform
```

Purpose

Pass environment variables into the container.

Instead of modifying application.yml,

Docker provides these values while starting the application.

Spring Boot automatically reads them.

---

# Why Did We Use "postgres" Instead of "localhost"?

One of the most important concepts in Docker.

Many beginners write

```
localhost
```

inside the backend container.

This is WRONG.

Remember

Every container has its own localhost.

Example

```
Backend Container

localhost

↓

Backend Container itself
```

NOT

```
PostgreSQL Container
```

Therefore

```
localhost
```

inside the backend container

≠

PostgreSQL.

Instead,

Docker Compose automatically creates DNS entries.

The backend reaches PostgreSQL using

```
postgres
```

because

```
postgres
```

is the service name.

Correct

```
jdbc:postgresql://postgres:5432/cloud_native_task_platform
```

Wrong

```
jdbc:postgresql://localhost:5432/cloud_native_task_platform
```

This is one of the most frequently asked Docker interview questions.

---

# depends_on

Example

```yaml
depends_on:
  - postgres
```

Purpose

Docker starts PostgreSQL before starting the backend.

Important

depends_on only controls the startup order.

It DOES NOT guarantee that PostgreSQL is ready to accept connections.

The database may still be initializing.

---

# Named Volume

Example

```yaml
volumes:

postgres_data:
```

Used by

```yaml
volumes:
  - postgres_data:/var/lib/postgresql/data
```

Purpose

Store PostgreSQL data permanently.

Even if the container is deleted,

the data remains inside the volume.

---

# Docker Compose Commands

Build Gradle Project

```bash
./gradlew build
```

Build Images and Start Containers

```bash
docker compose up --build
```

Start Existing Containers

```bash
docker compose up
```

Stop Containers

```bash
docker compose down
```

View Running Containers

```bash
docker ps
```

View Logs

```bash
docker logs <container-name>
```

Execute Commands Inside a Container

```bash
docker exec -it <container-name> bash
```

---

# Container Internals

We also explored what happens inside a running container.

Using

```bash
docker exec -it <container-name> bash
```

we entered the backend container.

Inside the container we executed

```bash
pwd

ls

java -version

ps -ef
```

---

# What Did We Observe?

Current Directory

```bash
pwd
```

returned

```
/app
```

because of

```dockerfile
WORKDIR /app
```

---

Listing Files

```bash
ls
```

showed

```
app.jar
```

which came from

```dockerfile
COPY
```

---

Java Version

```bash
java -version
```

confirmed that Java Runtime exists inside the container.

This came from

```dockerfile
FROM eclipse-temurin:21-jre
```

---

# PID 1

One of the most important Docker concepts.

When the container starts,

Docker executes

```dockerfile
ENTRYPOINT ["java","-jar","app.jar"]
```

Therefore

```
java
```

becomes

```
PID 1
```

inside the container.

PID 1 is the first process.

It controls the lifecycle of the container.

If PID 1 exits,

the container stops.

---

# Exiting the Shell

Suppose you execute

```bash
exit
```

after entering the container.

Does the container stop?

No.

Because

```
bash
```

is NOT PID 1.

The Java process continues running.

---

# Stopping the Container

When you execute

```bash
docker stop
```

Docker sends a SIGTERM signal to PID 1.

In our project,

PID 1 is

```
java -jar app.jar
```

Java shuts down.

↓

Container stops.

---

# Summary

After Part 2, you should understand

✅ Docker Compose

✅ Services

✅ build

✅ image

✅ build vs image

✅ Port Mapping

✅ Environment Variables

✅ Docker DNS

✅ Why localhost does not work inside containers

✅ depends_on

✅ Named Volumes

✅ Compose Commands

✅ Container Filesystem

✅ docker exec

✅ PID 1

✅ Container Lifecycle