# Sprint 08 — GitHub Actions / CI

**Status: 🚧 In Progress**

## Goal

Implement a Continuous Integration (CI) pipeline using GitHub Actions to automatically build and test the Cloud Native Task Platform and build Docker images for its backend and frontend.

## What We Learned

* What Continuous Integration (CI) means and why it is useful
* GitHub Actions fundamentals
* Workflow files and YAML syntax
* Workflow triggers:
  * Push events
  * Pull request events
* GitHub Actions components:
  * Workflows
  * Jobs
  * Runners
  * Steps
* Running a Spring Boot backend build using Gradle
* Setting up Java 21 in GitHub Actions
* Setting up Node.js 22 in GitHub Actions
* Installing frontend dependencies using `npm ci`
* Building a React/Vite frontend
* Building Docker images in a CI pipeline
* Configuring a PostgreSQL service container for backend tests
* Passing database connection settings through environment variables

## Practical Work

Implemented and tested:

* Created `.github/workflows/ci.yml`
* Configured workflow triggers for pushes to `main` and `feature/ci-cd`
* Configured pull request triggers targeting `main`
* Added a GitHub-hosted Ubuntu runner
* Configured Java 21 using `actions/setup-java`
* Built and tested the Spring Boot backend using `./gradlew build`
* Configured Node.js 22 using `actions/setup-node`
* Installed frontend dependencies using `npm ci`
* Built the frontend using `npm run build`
* Added a PostgreSQL 16 service container for backend tests
* Configured test database connection variables
* Set up Docker Buildx
* Built the backend Docker image
* Built the frontend Docker image
* Successfully executed the GitHub Actions CI workflow

## CI Workflow

The current workflow follows this sequence:

```text
Code Push or Pull Request
          |
          v
   GitHub Actions
          |
          v
   Ubuntu Runner
          |
          v
   Checkout Repository
          |
          v
   Build and Test Backend
          |
          v
 Install Frontend Dependencies
          |
          v
    Build Frontend
          |
          v
   Set Up Docker Buildx
          |
          v
 Build Backend Docker Image
          |
          v
 Build Frontend Docker Image
          |
          v
       CI Passed
```

The PostgreSQL service is started for the backend build and test job.

## Important Learning

The backend tests initially failed because the Spring Boot application context could not initialize Hibernate without the required database connection configuration.

We configured a PostgreSQL service container and supplied these environment variables:

```text
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
```

This allows the backend tests to connect to the PostgreSQL database provided by the CI job.

The database credentials are intended only for the CI test environment, not production.

## Current Limitations

The current pipeline:

* Builds and tests the backend.
* Builds the frontend.
* Builds Docker images for both applications.
* Does not yet publish Docker images to Docker Hub.
* Does not yet deploy the application to Kubernetes or a cloud environment.

## Key CI Mental Model

```text
Code Change
     |
     v
Automated Build
     |
     v
Automated Tests
     |
     v
Frontend Build
     |
     v
Docker Image Builds
     |
     v
Feedback on Code Quality and Build Success
```

Continuous Integration helps detect build and integration problems early.

## Documentation Created

```text
.github/
└── workflows/
    └── ci.yml
```

## Outcome

The initial GitHub Actions CI pipeline has been implemented and successfully executed.

**Sprint 08 is in progress. 🚧**

**Next:** Continue improving the CI/CD pipeline, including Docker image publishing and subsequent deployment automation.
