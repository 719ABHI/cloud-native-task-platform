# Multi-stage Builds

So far, our Dockerfile looks like this:

```dockerfile
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY build/libs/task-management-backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]
```

This works because we build the JAR outside Docker using

```bash
./gradlew bootJar
```

and then copy the generated JAR into the image.

This approach is called the **Pre-built JAR Approach**.

---

# Why Multi-stage Builds?

Suppose we want Docker to build our application itself.

Docker needs

- Source Code
- Gradle
- JDK

to compile the project.

However, after compilation, we only need

- Executable JAR
- JRE

Everything else becomes unnecessary.

Keeping build tools inside the final image makes it

- Larger
- Slower to download
- Less secure

This is why Multi-stage Builds exist.

---

# Single-stage vs Multi-stage

## Single-stage Build

```
JDK

↓

Gradle

↓

Source Code

↓

Dependencies

↓

Executable JAR

↓

Final Image
```

Everything remains inside the final image.

---

## Multi-stage Build

```
Builder Stage

↓

Gradle

↓

JDK

↓

Source Code

↓

Dependencies

↓

Build JAR

----------------------------

Runtime Stage

↓

JRE

↓

Executable JAR

↓

Final Image
```

Only the runtime files are included in the final image.

---

# Cake Analogy

Imagine you are baking a cake.

To bake the cake, you need

- Flour
- Sugar
- Eggs
- Butter
- Mixing Bowl
- Oven

Once the cake is ready,

you deliver only

```
Cake
```

You do not deliver

- Mixing Bowl
- Oven
- Flour
- Eggs

Builder Stage = Kitchen

Runtime Stage = Delivered Cake

Exactly the same idea is used in Docker Multi-stage Builds.

---

# Builder Stage

Purpose

Compile the application.

Contains

- Source Code
- Gradle
- JDK
- Dependencies

Output

```
Executable Spring Boot JAR
```

---

# Runtime Stage

Purpose

Run the application.

Contains

- JRE
- Executable JAR

Does NOT contain

- Source Code
- Gradle
- JDK
- Build Cache

---

# Multiple FROM Instructions

A Multi-stage Dockerfile contains multiple `FROM` instructions.

Example

```dockerfile
FROM gradle:8.8-jdk21 AS builder

...

FROM eclipse-temurin:21-jre
```

Every `FROM` starts a completely new build stage with its own filesystem.

The second stage does **not** automatically inherit files from the first stage.

---

# AS builder

Example

```dockerfile
FROM gradle:8.8-jdk21 AS builder
```

`builder` is simply a name (alias) given to the first build stage.

It is **not**

- an Image
- a Container

It is only a stage name that can be referenced later.

The name can be anything.

Examples

```dockerfile
AS builder

AS build

AS compile

AS stage1
```

---

# COPY --from

Example

```dockerfile
COPY --from=builder /app/build/libs/app.jar app.jar
```

Meaning

- Go to the build stage named `builder`.
- Find the file `/app/build/libs/app.jar`.
- Copy it into the current stage as `app.jar`.

Only the requested file is copied.

Gradle, source code and build tools are NOT copied.

---

# Practical Multi-stage Dockerfile

```dockerfile
FROM gradle:8.8-jdk21 AS builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

RUN chmod +x gradlew

RUN ./gradlew dependencies --no-daemon

COPY src src

RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /app/build/libs/task-management-backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]
```

---

# Understanding the Build Flow

```
docker build

↓

Stage 1 (Builder)

↓

Copy Gradle Files

↓

Download Dependencies

↓

Copy Source Code

↓

Build Executable JAR

↓

Stage 2 (Runtime)

↓

Copy Only Executable JAR

↓

Final Docker Image
```

---

# Docker Image Layers

One of Docker's most powerful features.

Every Docker instruction creates a separate image layer.

Example

```dockerfile
FROM

WORKDIR

COPY

RUN

ENTRYPOINT
```

becomes

```
Layer 5

ENTRYPOINT

↑

Layer 4

RUN

↑

Layer 3

COPY

↑

Layer 2

WORKDIR

↑

Layer 1

FROM
```

Docker stores every layer separately.

---

# Docker Layer Cache

Docker does not rebuild everything every time.

Instead,

it checks every layer.

If nothing has changed,

Docker reuses the cached layer.

This is called **Docker Layer Caching**.

---

# Cache Invalidation

Suppose only

```dockerfile
ENTRYPOINT
```

changes.

Docker rebuilds only

```
ENTRYPOINT
```

Everything before it comes from cache.

---

Suppose

```dockerfile
COPY src src
```

changes.

Docker rebuilds

```
COPY src

↓

RUN bootJar

↓

Remaining Layers
```

Every layer before that is reused.

---

# Why Dockerfile Order Matters

A well-written Dockerfile places

- Less frequently changing files at the top
- Frequently changing files near the bottom

Example

```
Gradle Files

↓

Download Dependencies

↓

Source Code

↓

Build Application
```

Now changing only a Java file does **not** force Docker to download dependencies again.

This significantly improves build performance.

---

# Advantages of Multi-stage Builds

- Smaller Images
- Better Security
- Faster Downloads
- Faster Deployments
- Cleaner Production Images
- Better Layer Caching
- No Build Tools in Runtime Image

---

# .dockerignore

Whenever Docker builds an image,

it sends the build context to Docker Engine.

Suppose your project contains

```
src/

build/

.gradle/

.git/

.idea/

logs/
```

Without a `.dockerignore` file,

everything is sent.

Even files Docker never needs.

---

# Purpose

`.dockerignore`

prevents unnecessary files from entering the build context.

This makes

- Faster Builds
- Smaller Build Context
- Cleaner Images

---

# Difference Between .gitignore and .dockerignore

| .gitignore | .dockerignore |
|------------|---------------|
| Used by Git | Used by Docker |
| Prevents files from being committed | Prevents files from entering build context |

They solve different problems.

---

# Docker Hub

Docker Hub is an online registry used to store Docker Images.

Think of it as

GitHub

↓

for Docker Images.

---

# Why Docker Hub?

Instead of every developer building images,

one image is uploaded.

Everyone downloads the same image.

Example

Developer

↓

Build Image

↓

Push to Docker Hub

↓

Team Members

↓

Pull Image

---

# Repository

A repository stores related Docker Images.

Example

```
postgres
```

contains

```
postgres:14

postgres:15

postgres:16

postgres:latest
```

Repository

↓

Collection of tagged images.

---

# Tags

A Tag identifies a particular version of an image.

Example

```
postgres:16
```

Repository

```
postgres
```

Tag

```
16
```

---

# latest Tag

Many beginners think

```
latest
```

always means

```
Newest Version
```

This is incorrect.

latest is simply another tag.

Example

```
Repository

task-management-backend

v1

v2

v3

latest
```

Suppose

```
latest
```

points to

```
v2
```

Then

```bash
docker pull task-management-backend:latest
```

downloads

```
v2
```

NOT

```
v3
```

Always use explicit version tags in production.

Example

```
postgres:16

backend:v1.2.0
```

instead of

```
latest
```

---

# docker pull

Downloads an image from Docker Hub.

Example

```bash
docker pull postgres:16
```

---

# docker push

Uploads an image to Docker Hub.

Example

```bash
docker push username/backend:v1
```

---

# docker tag

Creates another tag for an existing image.

Example

```bash
docker tag backend:v1 backend:latest
```

Important

Docker DOES NOT create another image.

It simply creates another reference.

Think of it like

```
Image

↑

v1

↑

latest
```

Both tags point to the same image.

---

# Docker Best Practices

- Use official base images.
- Prefer JRE instead of JDK for runtime images.
- Use Multi-stage Builds for production.
- Use `.dockerignore`.
- Keep images as small as possible.
- Use explicit image versions instead of `latest`.
- Keep secrets outside Dockerfiles.
- Use named volumes for persistent data.
- Use bind mounts only during development.

---

# Common Mistakes

❌ Using localhost between containers.

Use service names instead.

---

❌ Using latest in production.

Use version tags.

---

❌ Storing database inside container filesystem.

Use Docker Volumes.

---

❌ Building huge images.

Use Multi-stage Builds.

---

❌ Forgetting `.dockerignore`.

Leads to larger build contexts.

---

# Frequently Asked Interview Questions

### What is Docker?

A containerization platform used to package applications and their dependencies into portable containers.

---

### Difference between Image and Container?

Image is a blueprint.

Container is a running instance of an image.

---

### Why Docker instead of Virtual Machines?

Containers share the host kernel, making them lightweight and faster than virtual machines.

---

### Difference between COPY and Bind Mount?

COPY creates a separate copy inside the image.

Bind Mount shares the same files between host and container.

---

### Why doesn't localhost work between containers?

Each container has its own localhost.

Containers communicate using Docker DNS and service names.

---

### Difference between build and image in Docker Compose?

build creates an image using a Dockerfile.

image downloads an existing image from Docker Hub.

---

### What is PID 1?

The main process running inside the container.

If PID 1 exits, the container stops.

---

### What is a Named Volume?

Docker-managed persistent storage.

---

### What is Multi-stage Build?

A technique where one stage builds the application and another stage runs it, resulting in a smaller production image.

---

### What is Docker Cache?

Docker reuses unchanged image layers to speed up builds.

---

### What is Docker Hub?

An online registry for storing and sharing Docker images.

---

# Docker Commands Cheat Sheet

Check Docker Version

```bash
docker --version
```

Build Image

```bash
docker build -t image-name .
```

List Images

```bash
docker images
```

Run Container

```bash
docker run image-name
```

List Running Containers

```bash
docker ps
```

Stop Container

```bash
docker stop <container-id>
```

Delete Container

```bash
docker rm <container-id>
```

Delete Image

```bash
docker rmi <image-id>
```

Execute Commands Inside Container

```bash
docker exec -it <container-name> bash
```

View Logs

```bash
docker logs <container-name>
```

Start Docker Compose

```bash
docker compose up
```

Build and Start Compose

```bash
docker compose up --build
```

Stop Compose

```bash
docker compose down
```

Pull Image

```bash
docker pull postgres:16
```

Push Image

```bash
docker push username/backend:v1
```

Tag Image

```bash
docker tag backend:v1 backend:latest
```

---

# Sprint 4 Completion Checklist

After completing Sprint 4, you should be able to explain and understand:

✅ What Docker is and why it is used

✅ Docker Engine and Docker Desktop

✅ Images and Containers

✅ Dockerfile and all instructions (`FROM`, `WORKDIR`, `COPY`, `EXPOSE`, `ENTRYPOINT`)

✅ JDK vs JRE

✅ Docker Compose

✅ Services, `build`, `image`, `ports`, `environment`, `depends_on`

✅ Container Internals (Filesystem, PID 1, Lifecycle)

✅ Docker Storage (Named Volumes and Bind Mounts)

✅ Docker Networking (Docker DNS, Service Names, Port Mapping, `localhost`)

✅ Multi-stage Builds

✅ Docker Image Layers

✅ Docker Cache

✅ `.dockerignore`

✅ Docker Hub, Repositories, Tags, `latest`

✅ Essential Docker Commands
