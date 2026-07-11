# Local Development Setup

## Objective

This guide explains how to set up the local development environment required to build and run the **Cloud Native Task Management Platform**.

Following this guide will prepare your machine with the necessary tools, configure the project dependencies, and run the backend application locally.

---

# Prerequisites

Ensure the following software is installed before starting:

| Software | Version |
|----------|---------|
| Java | 21 |
| Gradle | Latest Compatible Version |
| Git | Latest |
| PostgreSQL | 14+ |
| IntelliJ IDEA | Community or Ultimate |

---

# Clone the Repository

Clone the project from GitHub.

```bash
git clone <repository-url>
```

Navigate into the project directory.

```bash
cd cloud-native-task-platform
```

---

# Project Structure

```
cloud-native-task-platform/
│
├── docs/
├── task-management-backend/
└── README.md
```

---

# Java Setup

Verify Java installation.

```bash
java --version
```

Expected output:

```
openjdk 21
```

If Java is not installed, install Java 21 before proceeding.

---

# Gradle Setup

Verify Gradle installation.

```bash
gradle --version
```

If Gradle is not installed globally, the project can also be built using the Gradle Wrapper.

```bash
./gradlew --version
```

---

# PostgreSQL Setup

PostgreSQL is the primary database used by this project.

Complete the PostgreSQL installation and database configuration by following:

**`docs/setup/postgresql-setup.md`**

Ensure the following database has been created:

```
cloud_native_task_platform
```

---

# Backend Configuration

Navigate to the backend project.

```bash
cd task-management-backend
```

Configure the database connection in:

```
src/main/resources/application.properties
```

Example configuration:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/cloud_native_task_platform
spring.datasource.username=postgres
spring.datasource.password=<your-password>

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

> Do not commit real passwords or sensitive configuration values to version control.

---

# Build the Project

Build the project using the Gradle Wrapper.

```bash
./gradlew build
```

A successful build indicates that all project dependencies have been downloaded and compiled.

---

# Run the Application

Start the backend application.

```bash
./gradlew bootRun
```

If everything is configured correctly, Spring Boot will start successfully.

Default application URL:

```
http://localhost:8080
```

---

# Verify the Application

Open your browser.

Swagger UI (available after Sprint 02):

```
http://localhost:8080/swagger-ui/index.html
```

Verify that:

- Spring Boot starts successfully.
- PostgreSQL connection is established.
- Swagger UI loads correctly.
- CRUD APIs are available.

---

# Verify Database Connection

Open PostgreSQL.

```bash
sudo -i -u postgres
psql
```

Connect to the project database.

```sql
\c cloud_native_task_platform
```

View all tables.

```sql
\dt
```

If Hibernate has created the schema successfully, the project tables should be visible.

---

# Useful Commands

### Build

```bash
./gradlew build
```

---

### Run

```bash
./gradlew bootRun
```

---

### Clean

```bash
./gradlew clean
```

---

### Test

```bash
./gradlew test
```

---

# Troubleshooting

If you encounter issues during setup, refer to:

```
docs/troubleshooting/common-errors.md
```

Common issues include:

- Java version mismatch
- PostgreSQL connection failure
- Database authentication errors
- Gradle dependency download issues
- Port 8080 already in use

---

# Additional Setup Guides

Detailed setup documentation is available for individual components.

| Guide | Description |
|--------|-------------|
| PostgreSQL Setup | Configure PostgreSQL for local development |
| Docker Setup *(Coming Soon)* | Containerize the application |
| Kubernetes Setup *(Coming Soon)* | Deploy locally using Kubernetes |
| AWS Setup *(Coming Soon)* | Cloud deployment |
| Redis Setup *(Coming Soon)* | Configure Redis caching |

---

# Next Steps

After successfully completing the local setup:

- Explore the project structure.
- Review the sprint documentation in `docs/sprints/`.
- Read the learning notes in `docs/notes/`.
- Start the backend application.
- Test the REST APIs using Swagger UI or Postman.

---

# Summary

At the end of this guide, you will have:

- Installed the required development tools.
- Configured PostgreSQL.
- Cloned the project repository.
- Built the Spring Boot application.
- Successfully started the backend locally.
- Verified database connectivity and API availability.

Your local development environment is now ready for further development.