# PostgreSQL Setup

## Objective

This guide explains how to install, configure, and connect PostgreSQL for the **Cloud Native Task Management Platform**. PostgreSQL serves as the primary relational database for storing application data during development.

---

## Prerequisites

Before proceeding, ensure the following are installed:

- Ubuntu 22.04 LTS (or compatible Linux distribution)
- Java 21
- Gradle
- Git

---

## Install PostgreSQL

Update the package index:

```bash
sudo apt update
```

Install PostgreSQL:

```bash
sudo apt install postgresql postgresql-contrib
```

---

## Verify Installation

Check the installed PostgreSQL version:

```bash
psql --version
```

Example output:

```text
psql (PostgreSQL) 14.x
```

---

## Check PostgreSQL Service

Verify that PostgreSQL is running:

```bash
sudo systemctl status postgresql
```

If the service is not running:

```bash
sudo systemctl start postgresql
```

Enable PostgreSQL to start automatically after system reboot:

```bash
sudo systemctl enable postgresql
```

---

## Access PostgreSQL

Switch to the PostgreSQL user:

```bash
sudo -i -u postgres
```

Open the PostgreSQL interactive terminal:

```bash
psql
```

---

## Create the Database

Create the project database:

```sql
CREATE DATABASE cloud_native_task_platform;
```

Verify the database has been created:

```sql
\l
```

---

## Connect to the Database

```sql
\c cloud_native_task_platform
```

Expected output:

```text
You are now connected to database "cloud_native_task_platform".
```

---

## Configure Spring Boot

Update the datasource configuration in `application.properties`.

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/cloud_native_task_platform
spring.datasource.username=postgres
spring.datasource.password=<your-password>

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

> **Note:** Never commit real database credentials to version control. Use environment variables or external configuration for production environments.

---

## Verify the Connection

Start the Spring Boot application:

```bash
./gradlew bootRun
```

If the configuration is correct, the application should start successfully and connect to PostgreSQL without errors.

---

## Verify Database Tables

Connect to PostgreSQL:

```bash
sudo -i -u postgres
psql
```

Connect to the project database:

```sql
\c cloud_native_task_platform
```

List all tables:

```sql
\dt
```

Describe a table:

```sql
\d task
```

View table data:

```sql
SELECT * FROM task;
```

---

## Useful PostgreSQL Commands

| Command | Description |
|---------|-------------|
| `\l` | List all databases |
| `\c database_name` | Connect to a database |
| `\dt` | List all tables |
| `\d table_name` | Describe a table |
| `SELECT * FROM table_name;` | View table data |
| `\q` | Exit PostgreSQL |

---

## Common Issues

### PostgreSQL Service Not Running

Check the service status:

```bash
sudo systemctl status postgresql
```

Start the service:

```bash
sudo systemctl start postgresql
```

---

### Database Does Not Exist

Verify available databases:

```sql
\l
```

Create the database if necessary:

```sql
CREATE DATABASE cloud_native_task_platform;
```

---

### Authentication Failed

Verify:

- Username
- Password
- Database name
- Port number

Also check the datasource configuration in `application.properties`.

---

### Connection Refused

Ensure:

- PostgreSQL service is running.
- Port `5432` is available.
- Firewall settings are not blocking connections.

---

## References

- PostgreSQL Official Documentation: https://www.postgresql.org/docs/
- Spring Boot Reference Documentation: https://docs.spring.io/spring-boot/docs/current/reference/html/

---

## Summary

At the end of this setup:

- PostgreSQL is installed and running.
- The project database has been created.
- Spring Boot is successfully connected to PostgreSQL.
- Database tables are automatically managed using Hibernate.
- The backend is ready for development and testing.