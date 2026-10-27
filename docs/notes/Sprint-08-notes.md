# Sprint 08 — GitHub Actions and CI/CD Notes

## 1. What Is CI/CD?

CI/CD is a collection of practices that automate the process of building, testing, and delivering software.

**CI** stands for Continuous Integration.

**CD** commonly stands for Continuous Delivery or Continuous Deployment.

### Continuous Integration (CI)

CI automatically builds and tests code changes to identify problems early.

For our project, when we push code to GitHub, GitHub Actions can automatically:

1. Download the repository.
2. Build and test the Spring Boot backend.
3. Install frontend dependencies.
4. Build the React frontend.
5. Build Docker images for both applications.

### Continuous Delivery

Continuous Delivery prepares an application for release. Deployment to production typically requires a manual approval or action.

### Continuous Deployment

Continuous Deployment automatically releases changes to production after they pass the required checks.

**Our current implementation:** We have implemented CI. We have not yet implemented automated Docker image publishing or application deployment.

---

## 2. What Is GitHub Actions?

GitHub Actions is a GitHub automation platform that runs workflows in response to events such as code pushes and pull requests.

We use it to automate the build and testing of our application.

Our workflow file is:

```text
.github/workflows/ci.yml
```

### Why is the file inside `.github/workflows/`?

GitHub recognizes workflow YAML files stored in this directory and can execute them when their configured triggers occur.

### What does `ci.yml` mean?

- `ci` describes the purpose: Continuous Integration.
- `.yml` is a YAML file extension.

The filename can be different, but the file must be inside `.github/workflows/` for GitHub Actions to discover it as a repository workflow.

---

## 3. GitHub Actions Architecture

A workflow contains jobs, and each job contains steps.

```text
GitHub Event
     |
     v
  Workflow
     |
     v
     Job
     |
     v
   Runner
     |
     v
    Steps
```

### Workflow

A workflow is the complete automation process defined in a YAML file.

Our workflow is named `CI`.

### Job

A job groups the tasks that should run together.

Our job is named `build-and-test`.

### Runner

A runner is the machine that executes the job.

Our configuration uses:

```yaml
runs-on: ubuntu-latest
```

This tells GitHub Actions to use a GitHub-hosted Ubuntu runner.

The runner provides the environment in which our commands execute.

### Steps

Steps are individual actions or commands executed by a job.

Examples from our workflow include:

- Checking out the repository.
- Setting up Java.
- Building the backend.
- Setting up Node.js.
- Building the frontend.
- Building Docker images.

Steps in the same job run sequentially unless workflow logic changes that behavior.

If a required step fails, subsequent steps are normally skipped.

---

## 4. Understanding Workflow Triggers

Our workflow contains:

```yaml
on:
  push:
    branches:
      - main
      - feature/ci-cd

  pull_request:
    branches:
      - main
```

### Push trigger

```yaml
push:
  branches:
    - main
    - feature/ci-cd
```

This runs the workflow when code is pushed to either `main` or `feature/ci-cd`.

A push to a different branch will not trigger this workflow through this push configuration.

### Pull request trigger

```yaml
pull_request:
  branches:
    - main
```

This runs the workflow for pull requests targeting `main`.

**Important distinction:** The listed branch under `pull_request` is the target branch, not necessarily the branch containing the new changes.

### Why do we use triggers?

Triggers automate validation. We don't need to remember to run every build and test manually after each relevant push or pull request.

---

## 5. Setting Up Java

Our workflow contains:

```yaml
- name: Set up Java
  uses: actions/setup-java@v4
  with:
    java-version: '21'
    distribution: 'temurin'
    cache: 'gradle'
```

### `name`

```yaml
name: Set up Java
```

This is the human-readable label displayed in the GitHub Actions interface.

### `uses`

```yaml
uses: actions/setup-java@v4
```

This runs a reusable GitHub Action that installs and configures Java.

- `actions` identifies the action publisher.
- `setup-java` identifies the action.
- `@v4` selects its major version.

### `java-version`

```yaml
java-version: '21'
```

This configures Java 21 for the backend build.

### `distribution`

```yaml
distribution: 'temurin'
```

This selects the Eclipse Temurin distribution of Java.

### `cache`

```yaml
cache: 'gradle'
```

This enables Gradle dependency caching to reduce repeated downloads and potentially speed up subsequent builds.

---

## 6. Building the Backend

Our workflow contains:

```yaml
- name: Build backend
  working-directory: task-management-backend
  run: ./gradlew build
```

### `working-directory`

```yaml
working-directory: task-management-backend
```

This tells GitHub Actions to execute the command from the backend directory.

Without this setting, the command would normally run from the repository root, where the backend's `gradlew` script is not located.

### `run`

```yaml
run: ./gradlew build
```

This executes the Gradle wrapper script.

- `.` refers to the current directory.
- `/` separates the directory from the executable name.
- `gradlew` is the Gradle wrapper script.
- `build` invokes Gradle's build lifecycle.

The build lifecycle normally compiles the code, processes resources, runs tests, and creates build artifacts.

If a test fails, the build normally fails too.

### Why use `./gradlew` instead of `gradle`?

The Gradle wrapper uses the Gradle version configured by the project. This helps maintain consistent builds across developer machines and CI environments.

---

## 7. Configuring Node.js

Our workflow contains:

```yaml
- name: Set up Node.js
  uses: actions/setup-node@v4
  with:
    node-version: '22'
    cache: 'npm'
    cache-dependency-path: task-management-frontend/package-lock.json
```

### `node-version`

```yaml
node-version: '22'
```

This configures Node.js 22 for the frontend build.

### `cache`

```yaml
cache: 'npm'
```

This enables npm dependency caching.

### `cache-dependency-path`

```yaml
cache-dependency-path: task-management-frontend/package-lock.json
```

This tells the action which lockfile to use when determining the dependency cache.

If the dependency lockfile changes, the cache can be refreshed appropriately.

**Important:** The Node.js version used by our application build is different from the Node.js runtime that GitHub Actions uses internally to execute certain actions.

---

## 8. Installing Frontend Dependencies

Our workflow contains:

```yaml
- name: Install frontend dependencies
  working-directory: task-management-frontend
  run: npm ci
```

### What is npm?

npm is the Node.js package manager. It installs and manages JavaScript packages.

### What does `npm ci` do?

`npm ci` installs the dependencies specified by the project's lockfile.

It is particularly useful in CI environments because it is designed for clean, reproducible dependency installation.

It expects a compatible lockfile and fails if the lockfile and `package.json` are inconsistent.

### Why not use `npm install`?

`npm install` can update the lockfile in certain situations. `npm ci` is better suited to automated builds where we want to install the locked dependency set.

---

## 9. Building the Frontend

Our workflow contains:

```yaml
- name: Build frontend
  working-directory: task-management-frontend
  run: npm run build
```

This runs the `build` script defined in the frontend's `package.json`.

For our React/Vite application, the production build generates static assets, normally in the `dist/` directory.

These assets are later copied into the NGINX image by the frontend Dockerfile.

---

## 10. Setting Up Docker Buildx

Our workflow contains:

```yaml
- name: Set up Docker Buildx
  uses: docker/setup-buildx-action@v3
```

Docker Buildx provides extended Docker build capabilities.

It is useful for advanced builds, multi-platform images, and build caching.

Our current Docker image builds use the standard `docker build` command. The Buildx setup prepares the environment for more advanced CI/CD improvements.

---

## 11. Building Docker Images in CI

Our workflow contains:

```yaml
- name: Build backend Docker image
  run: docker build -t task-management-backend:ci ./task-management-backend

- name: Build frontend Docker image
  run: docker build -t task-management-frontend:ci ./task-management-frontend
```

### Backend image

```bash
docker build -t task-management-backend:ci ./task-management-backend
```

This builds the backend Docker image.

- `docker build` builds an image using a Dockerfile.
- `-t` assigns a name and tag.
- `task-management-backend` is the image name.
- `ci` is the image tag.
- `./task-management-backend` is the build context.

The build context contains the files available to the Docker build, subject to `.dockerignore`.

### Frontend image

```bash
docker build -t task-management-frontend:ci ./task-management-frontend
```

This builds the frontend Docker image using its own Dockerfile and build context.

### What does the `ci` tag mean?

The tag identifies this image as a CI build.

It is a label, not a special Docker mode.

### Where are these images stored?

The images are built on the GitHub-hosted runner.

Our current workflow does not push them to Docker Hub or another container registry. They are available during the job but are not automatically published as reusable registry images.

---

## 12. PostgreSQL Service Container

Our job defines a PostgreSQL service:

```yaml
services:
  postgres:
    image: postgres:16
    env:
      POSTGRES_DB: task_platform_test
      POSTGRES_USER: ci_user
      POSTGRES_PASSWORD: ci_test_password
    ports:
      - 5432:5432
    options: >-
      --health-cmd "pg_isready -U ci_user -d task_platform_test"
      --health-interval 10s
      --health-timeout 5s
      --health-retries 5
```

### Why do we need PostgreSQL in CI?

Our Spring Boot application uses PostgreSQL. Some tests load the complete Spring application context, which initializes Hibernate and needs database connection configuration.

The service container provides a temporary PostgreSQL database for the CI job.

### `image: postgres:16`

This selects PostgreSQL version 16 as the database container image.

### Database environment variables

```yaml
POSTGRES_DB: task_platform_test
POSTGRES_USER: ci_user
POSTGRES_PASSWORD: ci_test_password
```

These configure the test database created by the PostgreSQL container.

They are test-only values, not production credentials.

### Port mapping

```yaml
ports:
  - 5432:5432
```

This maps port 5432 on the runner to port 5432 in the PostgreSQL container.

Because our job runs directly on the Ubuntu runner, the application can connect through `localhost:5432`.

### Health check

The health-check options use `pg_isready` to check whether PostgreSQL is ready to accept connections.

The health-check settings specify the interval, timeout, and number of retries.

This helps prevent the job from trying to use the database before it is ready.

---

## 13. Environment Variables for Spring Boot

Our workflow defines:

```yaml
env:
  SPRING_DATASOURCE_URL: jdbc:postgresql://localhost:5432/task_platform_test
  SPRING_DATASOURCE_USERNAME: ci_user
  SPRING_DATASOURCE_PASSWORD: ci_test_password
```

These variables match the placeholders in the backend's `application.yaml`:

```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
```

### How does the connection work?

```text
GitHub Actions Job
        |
        v
Environment Variables
        |
        v
Spring Boot Configuration
        |
        v
JDBC PostgreSQL Driver
        |
        v
PostgreSQL Service Container
```

Spring Boot reads the environment variables and uses them to configure the database connection.

### Why define `env` at the job level?

The variables become available to the job's steps, including the backend build and test step.

This avoids repeating the same configuration for each relevant step.

### Security reminder

The current credentials are for an isolated CI test database. Never put real production passwords, tokens, or other sensitive values directly in workflow files.

For workflows that need real credentials, use GitHub Actions secrets or an appropriate secret-management system.

---

## 14. Understanding CI Failures

During our first workflow run, the backend test failed while initializing the Spring application context and Hibernate.

The error indicated that the database configuration required for initialization was not available.

We addressed this by:

1. Adding a PostgreSQL service container.
2. Configuring a test database, username, and password.
3. Providing the Spring datasource environment variables.
4. Pushing the updated workflow.
5. Confirming that the CI workflow completed successfully.

### Important lesson

A successful build on a developer's machine does not guarantee that the same build will pass in CI.

CI runs in its own environment, so dependencies, configuration, services, and environment variables must be set up appropriately.

---

## 15. Node.js Deprecation Warning

Our successful workflow displayed a warning that some GitHub Actions were being forced to run on Node.js 24 even though those action versions target Node.js 20.

This warning concerns the internal runtime used by the GitHub Actions themselves.

It is different from the Node.js 22 version configured for our frontend build.

The workflow succeeded despite the warning. We should check compatible action versions and update them when appropriate.

---

## 16. Complete CI Mental Model

```text
Developer
   |
   | Push code
   v
GitHub Repository
   |
   | Trigger
   v
GitHub Actions Workflow
   |
   v
Ubuntu Runner
   |
   +--> Checkout Repository
   |
   +--> Configure Java
   |       |
   |       v
   |    Build and Test Backend
   |       |
   |       v
   |    PostgreSQL Service
   |
   +--> Configure Node.js
   |       |
   |       v
   |    Install Dependencies
   |       |
   |       v
   |    Build Frontend
   |
   +--> Configure Docker Buildx
   |
   +--> Build Backend Image
   |
   +--> Build Frontend Image
   |
   v
CI Result
```

---

## 17. Current Status and Next Steps

### Completed

- GitHub Actions workflow created.
- Push and pull request triggers configured.
- Backend build and tests integrated.
- PostgreSQL service configured for backend tests.
- Frontend dependency installation and production build integrated.
- Docker images built for the backend and frontend.
- CI workflow successfully executed.

### Planned

- Publish Docker images to a container registry.
- Automate deployment in a later stage.
- Consider additional quality checks and CI improvements.

**Current status:** The initial CI pipeline is implemented and working. Sprint 08 remains in progress while we continue building the CI/CD pipeline.
