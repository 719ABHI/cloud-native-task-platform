# Sprint 4 Notes - Docker

---

# What is Docker?

Docker is a containerization platform that packages an application along with all its dependencies into a lightweight, portable unit called a **Container**.

It ensures that the application runs the same way on every machine.

Example:

Suppose you developed a Spring Boot application on your laptop.

It works perfectly.

You send the project to another developer.

They run it and get errors like:

- Java version mismatch
- Gradle version mismatch
- PostgreSQL not installed
- Environment variables missing

This is called **"It works on my machine"** problem.

Docker solves this problem by packaging:

- Application
- Java Runtime
- Dependencies
- Required Libraries
- Configuration

into one container.

Wherever Docker is installed, the application behaves exactly the same.

---

# Why Docker?

Before Docker

Developer Machine
↓
Works

Testing Server
↓
Fails

Production Server
↓
Different Environment

Everyone has different software versions.

---

After Docker

Docker Image
↓

Developer
↓

Testing
↓

Production

Everyone runs the exact same container.

No environment mismatch.

---

# Virtual Machine vs Docker

## Virtual Machine

A Virtual Machine contains an entire Operating System.

Example

```
Application

Java

Libraries

Ubuntu Operating System

Virtual Hardware
```

Every VM requires:

- CPU
- RAM
- Storage
- Complete Operating System

Advantages

- Strong isolation
- Can run different Operating Systems

Disadvantages

- Heavy
- Slow startup
- High memory usage

---

## Docker Container

Docker does NOT create a complete Operating System.

Instead, containers share the Host Operating System Kernel.

Example

```
Application

Java

Libraries

Docker Engine

Host Linux Kernel
```

Advantages

- Lightweight
- Starts in seconds
- Less RAM
- Less Storage
- Easy Deployment

---

# Docker Engine

Docker Engine is the software responsible for running Docker.

Think of it as the "brain" of Docker.

It performs operations like:

- Build Images
- Run Containers
- Stop Containers
- Delete Containers
- Create Networks
- Create Volumes

Without Docker Engine, Docker commands cannot work.

---

# Docker Desktop

Docker Desktop is an application mainly used on Windows and macOS.

Purpose

- Provides Docker Engine
- Provides GUI
- Downloads a lightweight Linux Virtual Machine
- Runs Docker inside that Linux VM

Reason

Docker containers require a Linux Kernel.

Windows and macOS do not have one.

Therefore Docker Desktop creates a lightweight Linux VM.

---

Linux Users

Linux already has a Linux Kernel.

Therefore Docker Engine runs directly.

Docker Desktop is optional.

---

# Docker Architecture

```
Developer

      │

Docker CLI

      │

Docker Engine

      │

-------------------------
Images
Containers
Networks
Volumes
-------------------------
```

Flow

Developer writes Docker commands.

↓

Docker CLI sends the request.

↓

Docker Engine performs the operation.

↓

Images, Containers, Networks and Volumes are managed.

---

# What is an Image?

An Image is a **read-only blueprint** used to create containers.

Think of it like a class in Java.

Java

```java
class Student {

}
```

Nothing is running yet.

It is only a blueprint.

Similarly,

Docker Image

```
postgres:16
```

is only a blueprint.

It contains:

- Files
- Libraries
- Runtime
- Configuration

But nothing is running.

---

# What is a Container?

A Container is a running instance of an Image.

Java Analogy

```java
Student s1 = new Student();
Student s2 = new Student();
```

One class

↓

Multiple Objects

Similarly

One Image

↓

Multiple Containers

Example

```
Image

postgres:16

↓

Container 1

↓

Container 2

↓

Container 3
```

Every container has:

- Its own process
- Its own filesystem
- Its own network namespace

But all are created from the same image.

---

# Image vs Container

| Image | Container |
|--------|-----------|
| Blueprint | Running Instance |
| Read Only | Read/Write |
| Cannot Execute | Executes Application |
| Used to Create Containers | Created from Images |

Remember

Image = Recipe

Container = Prepared Food

One recipe can make many dishes.

One image can create many containers.

---

# Container Lifecycle

Image

↓

Container Created

↓

Container Running

↓

Container Stopped

↓

Container Removed

The image still exists even after the container is removed.

Only the running instance is deleted.

---

# Dockerfile

A Dockerfile is a text file containing instructions to build a Docker Image.

Instead of manually installing Java every time,

Dockerfile automates everything.

Example

```dockerfile
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY build/libs/task-management-backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]
```

Docker reads this file from top to bottom.

Each instruction creates a new image layer.

---

# Understanding Our Dockerfile

Our project Dockerfile

```dockerfile
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY build/libs/task-management-backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]
```

Purpose

This Dockerfile creates an image capable of running our Spring Boot backend.

---

# FROM

Syntax

```dockerfile
FROM image-name:tag
```

Example

```dockerfile
FROM eclipse-temurin:21-jre
```

Purpose

Defines the base image.

Our application requires Java.

Instead of installing Java manually,

Docker starts from an existing image that already contains Java Runtime.

---

# Why JRE instead of JDK?

JDK

Contains

- Java Compiler
- Development Tools
- Java Runtime

Used while developing applications.

JRE

Contains only

- Java Runtime

Used to run applications.

Since our application is already compiled into a JAR,

we only need the Runtime.

Using JRE results in:

- Smaller Image
- Faster Downloads
- Better Security

---

# WORKDIR

Syntax

```dockerfile
WORKDIR /app
```

Purpose

Changes the current working directory.

Equivalent to

```bash
cd /app
```

Every command after WORKDIR executes inside `/app`.

---

# COPY

Syntax

```dockerfile
COPY source destination
```

Example

```dockerfile
COPY build/libs/task-management-backend-0.0.1-SNAPSHOT.jar app.jar
```

Meaning

Host Machine

↓

Copy JAR File

↓

Image

↓

/app/app.jar

The original file still remains on the host machine.

Docker creates a copy.

---

# EXPOSE

Syntax

```dockerfile
EXPOSE 8080
```

Purpose

Documents that the application listens on port 8080.

Important

EXPOSE does NOT publish the port.

Publishing happens using

```bash
docker run -p
```

or

Docker Compose.

---

# ENTRYPOINT

Syntax

```dockerfile
ENTRYPOINT ["java","-jar","app.jar"]
```

Purpose

Defines the main process that starts when the container starts.

When the container starts,

Docker automatically executes

```bash
java -jar app.jar
```

without us typing it manually.

---

# Summary

After Sprint 4 Part 1, you should understand:

✅ Why Docker exists

✅ Virtual Machines vs Containers

✅ Docker Engine

✅ Docker Desktop

✅ Docker Architecture

✅ Images

✅ Containers

✅ Image vs Container

✅ Container Lifecycle

✅ Dockerfile

✅ FROM

✅ WORKDIR

✅ COPY

✅ EXPOSE

✅ ENTRYPOINT

✅ Why we used JRE instead of JDK
