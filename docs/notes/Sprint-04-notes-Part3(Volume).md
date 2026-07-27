# Docker Storage

When a container is created, Docker gives it its own writable filesystem.

Example

```
Container

Application Files

Temporary Files

Logs
```

This writable layer exists only as long as the container exists.

If the container is deleted,

everything stored inside it is also deleted.

Example

Suppose PostgreSQL stores data inside the container.

```
PostgreSQL Container

Database
↓

Users
↓

Tasks
↓

Orders
```

Now delete the container.

```
docker rm postgres
```

Everything is lost.

This is why Docker provides **Volumes**.

---

# Why Do We Need Volumes?

Containers are meant to be temporary.

Data is not.

Examples of persistent data

- Database
- Uploaded Images
- Configuration Files
- Logs
- Backups

Docker Volumes allow this data to survive even if the container is deleted.

---

# Named Volumes

A Named Volume is storage managed by Docker.

Example

```yaml
volumes:
  postgres_data:
```

Used inside a service

```yaml
postgres:

  volumes:
    - postgres_data:/var/lib/postgresql/data
```

Meaning

```
Container

PostgreSQL

↓

Stores Database

↓

Docker Volume

postgres_data
```

Now suppose

```
Delete PostgreSQL Container
```

The volume still exists.

Create another PostgreSQL container.

Attach the same volume.

Your database comes back.

---

# Volume Lifecycle

```
Create Volume

↓

Create Container

↓

Container Uses Volume

↓

Delete Container

↓

Volume Still Exists

↓

Create New Container

↓

Reuse Existing Volume
```

Volume and Container have different lifecycles.

Deleting a container does NOT delete the volume.

---

# Bind Mounts

A Bind Mount connects a folder on your computer directly to a folder inside the container.

Example

```yaml
volumes:
  - ./task-management-backend:/app
```

Meaning

```
Host Folder

task-management-backend

↓

Container Folder

/app
```

Both point to the same files.

---

# Named Volume vs Bind Mount

This is one of the most important concepts.

Think of it like notebooks.

## Named Volume

Imagine Docker keeps a notebook inside its own cupboard.

```
Docker Cupboard

↓

Notebook

↓

Database Data
```

Your application uses that notebook.

You normally don't edit it directly.

Docker manages it.

---

## Bind Mount

Imagine both you and Docker are writing in the same notebook.

```
Host Computer

↓

Shared Notebook

↓

Container
```

If you edit a file on your laptop,

the container immediately sees the change.

If the container edits the file,

your laptop immediately sees the change.

Both are using the same files.

---

# COPY vs Bind Mount

This confused us initially.

Remember this difference.

## COPY

```
Host

↓

Photocopy

↓

Image
```

Docker copies the files.

After copying,

Host changes

❌ Not reflected inside the image.

Image changes

❌ Not reflected on the host.

They are separate copies.

---

## Bind Mount

```
Host Folder

⇅

Container Folder
```

No copying happens.

Both use the same files.

Host changes

✅ Container sees changes.

Container changes

✅ Host sees changes.

---

# What Happens If a Bind Mount Replaces Existing Files?

Suppose our image contains

```
/app

↓

app.jar
```

Now we mount

```yaml
./backend:/app
```

Docker overlays

```
Host Folder

↓

/app
```

The original contents become hidden.

Important

The files are NOT deleted.

They are simply hidden while the bind mount exists.

This is why bind mounts are mainly used during development.

---

# Development vs Production

Development

```
Bind Mount
```

Reason

Code changes should immediately appear inside the container.

No rebuilding required.

---

Production

```
COPY
```

Reason

Application should remain fixed.

No accidental modifications.

---

# Docker Networking

Suppose we have

```
Backend

↓

PostgreSQL
```

How do they communicate?

Docker automatically creates a private network.

Both containers join that network.

---

# Docker Compose Network

When we execute

```bash
docker compose up
```

Docker automatically creates

```
Compose Network

↓

Backend

↓

PostgreSQL
```

Both containers can communicate directly.

No manual configuration required.

---

# Docker DNS

One of Docker's most useful features.

Docker automatically converts service names into hostnames.

Our compose file

```yaml
services:

  backend:

  postgres:
```

Docker creates DNS entries.

```
backend

↓

Backend Container

-------------------

postgres

↓

PostgreSQL Container
```

Instead of remembering IP addresses,

we simply use

```
postgres
```

---

# Why localhost Doesn't Work

This is probably the most common beginner mistake.

Remember

Every container has its own localhost.

Example

```
Backend Container

localhost

↓

Backend Container
```

NOT

```
PostgreSQL Container
```

Therefore

```
jdbc:postgresql://localhost:5432
```

is wrong.

Correct

```
jdbc:postgresql://postgres:5432
```

because

```
postgres
```

is the Docker service name.

---

# Container-to-Container Communication

```
Backend

↓

postgres:5432

↓

PostgreSQL
```

Notice

No published ports are required.

Containers communicate directly through the Docker network.

---

# Host-to-Container Communication

Suppose your browser needs to access the backend.

```
Browser

↓

localhost:8080

↓

Backend Container
```

Now port mapping becomes necessary.

Compose

```yaml
ports:
  - "8080:8080"
```

Without this mapping,

the browser cannot access the backend.

---

# Host vs Container Communication

| Communication | Needs Port Mapping? |
|---------------|--------------------|
| Browser → Backend | ✅ Yes |
| Backend → PostgreSQL | ❌ No |
| PostgreSQL → Backend | ❌ No |

Remember this table.

Interviewers love asking this question.

---

# Docker Networks

Every container belongs to a network.

Containers in the same network

✅ Can communicate.

Containers in different networks

❌ Cannot communicate unless connected.

---

# Custom Networks

Docker also allows creating custom networks.

Example

```bash
docker network create my-network
```

Containers connected to the same custom network can communicate.

We did not implement this in our project, but we understood the concept.

---

# Summary

After Part 3, you should understand

✅ Docker Storage

✅ Why containers lose data

✅ Named Volumes

✅ Bind Mounts

✅ COPY vs Bind Mount

✅ Development vs Production

✅ Docker Networks

✅ Docker DNS

✅ localhost inside containers

✅ Host-to-Container Communication

✅ Container-to-Container Communication

✅ Custom Networks