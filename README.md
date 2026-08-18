# Cloud Native Task Management Platform

A cloud-native task management application built with **Spring Boot, React, PostgreSQL, Docker, and Kubernetes**, designed to demonstrate modern backend development, containerization, orchestration, networking, scalability, configuration management, and DevOps practices.

The project is being developed incrementally through multiple sprints, evolving from a Spring Boot REST API into a containerized and Kubernetes-orchestrated application.

---

## Project Overview

The **Cloud Native Task Management Platform** is a task management application focused on building, containerizing, deploying, scaling, and operating a modern cloud-native application.

The project progressively introduces:

- RESTful backend development
- React frontend
- PostgreSQL
- Docker containerization
- Docker Compose
- Kubernetes orchestration
- Kubernetes networking and service discovery
- Configuration and secrets management
- Application health checks
- Persistent storage
- Horizontal Pod Autoscaling
- NGINX Ingress
- CI/CD
- Cloud infrastructure
- Monitoring and centralized logging

The project is developed sprint-by-sprint, with detailed concepts, implementation notes, practical work, and troubleshooting documentation maintained under the `docs/` directory.

---

## Key Features

### Backend

- RESTful task management APIs
- Layered Spring Boot architecture
- Controller → Service → Repository structure
- PostgreSQL integration
- Spring Data JPA
- Hibernate
- DTO-based request and response handling
- Request validation
- Global exception handling
- Custom business exceptions
- Unified API error responses
- Swagger / OpenAPI documentation
- Structured logging using SLF4J
- JWT-based authentication and authorization

### Frontend

- React
- Vite
- Material UI
- Backend API integration
- Production build using NGINX
- Dockerized frontend
- Kubernetes deployment

### Cloud-Native Capabilities

- Docker containerization
- Multi-stage Docker builds
- Docker layer caching
- Docker Compose
- Kubernetes Deployments and Pods
- Kubernetes Services
- Kubernetes DNS / CoreDNS
- ConfigMaps and Secrets
- Liveness and Readiness Probes
- PersistentVolumeClaims
- Horizontal Pod Autoscaling
- NGINX Ingress
- Kubernetes self-healing and rolling updates

---

## Technology Stack

| Category | Technologies |
|---|---|
| Backend | Java 21, Spring Boot, Spring Data JPA, Hibernate |
| Database | PostgreSQL |
| Build Tool | Gradle |
| Frontend | React, Vite, Material UI |
| API Documentation | Swagger / OpenAPI |
| Containerization | Docker, Docker Compose |
| Container Registry | Docker Hub |
| Orchestration | Kubernetes, Minikube |
| Networking | Kubernetes Services, NodePort, NGINX Ingress |
| Service Discovery | Kubernetes DNS, CoreDNS |
| Configuration | ConfigMaps, Secrets |
| Health Checks | Liveness and Readiness Probes |
| Storage | PersistentVolume, PersistentVolumeClaim |
| Autoscaling | Kubernetes HPA, Metrics Server |
| Web Server | NGINX |
| CI/CD | GitHub Actions *(Planned)* |
| Package Management | Helm *(Planned)* |
| Cloud | AWS *(Planned)* |
| Infrastructure as Code | Terraform *(Planned)* |
| Monitoring | Prometheus, Grafana *(Planned)* |
| Logging | Loki *(Planned)* |
| Caching | Redis *(Planned)* |

---

## Application Architecture

The backend follows a layered architecture:

```text
                    Client
                       |
                       ▼
                REST Controller
                       |
                       ▼
                 Service Layer
                       |
                       ▼
               Repository Layer
                       |
                       ▼
                  PostgreSQL
````

The application architecture is progressively extended with Docker and Kubernetes as the project evolves.

---

## Current Cloud-Native Architecture

The application is currently deployed locally using **Minikube**.

```text
                         User / Browser
                               |
                               ▼
                        NGINX Ingress
                               |
                    +----------+----------+
                    |                     |
                  "/"                   "/api"
                    |                     |
                    ▼                     ▼
            Frontend Service       Backend Service
                    |                     |
                    ▼                     ▼
             Frontend Pods         Backend Pods
                                          |
                                          ▼
                                  PostgreSQL Service
                                          |
                                          ▼
                                  PostgreSQL Pod
                                          |
                                          ▼
                                         PVC
                                          |
                                          ▼
                                  Persistent Storage
```

The Kubernetes components provide workload management, networking, configuration, health checks, persistent storage, autoscaling, and external HTTP routing.

---

## Kubernetes Deployment

The current Kubernetes deployment consists of:

```text
                    Kubernetes Cluster
                           |
             +-------------+-------------+
             |                           |
        Control Plane                Worker Node
                                         |
             +---------------------------+-------------------+
             |                |             |               |
          Backend          Frontend     PostgreSQL        NGINX
             |                |             |               |
          Service          Service       Service           Ingress
```

Current application workloads include:

* Backend Deployment
* Frontend Deployment
* PostgreSQL Deployment
* NGINX Deployment
* Kubernetes Services
* ConfigMaps
* Secrets
* PersistentVolumeClaim
* Metrics Server
* Horizontal Pod Autoscaler
* NGINX Ingress

The detailed explanation of each Kubernetes concept is maintained separately in `docs/kubernetes/`.

---

## Current Kubernetes Networking

The application uses Kubernetes Services for stable communication between workloads.

```text
Frontend Pods
      |
      ▼
backend-service:8080
      |
      ▼
Backend Pods
      |
      ▼
postgres-service:5432
      |
      ▼
PostgreSQL Pod
```

Kubernetes DNS allows application components to use Service names instead of depending on changing Pod IP addresses.

External HTTP traffic is handled through the NGINX Ingress:

```text
Browser
   |
   ▼
NGINX Ingress
   |
   +------------+
   |            |
   ▼            ▼
Frontend     Backend
Service      Service
```

---

## Configuration and Secrets

Application configuration is externalized using Kubernetes ConfigMaps and Secrets.

```text
Backend Pod
    |
    +------ ConfigMap
    |          |
    |          └── Non-sensitive configuration
    |
    +------ Secret
               |
               └── Sensitive configuration
```

This allows configuration to be managed separately from the Docker image.

---

## Health Checks

The backend uses Spring Boot Actuator with Kubernetes health probes.

```text
Liveness Probe
      |
      ▼
Application health
      |
      └── Failure → Container restart

Readiness Probe
      |
      ▼
Application ready to receive traffic
      |
      └── Failure → Pod removed from normal Service traffic
```

The detailed behavior and practical testing are documented in:

```text
docs/kubernetes/09-health-probes.md
```

---

## Persistent Storage

PostgreSQL uses Kubernetes persistent storage:

```text
PostgreSQL Pod
      |
      ▼
PersistentVolumeClaim
      |
      ▼
PersistentVolume
      |
      ▼
Persistent Storage
```

This separates database storage from the lifecycle of the PostgreSQL Pod.

---

## Horizontal Pod Autoscaling

The backend uses Kubernetes Horizontal Pod Autoscaling.

Current configuration:

```text
Minimum replicas : 2
Maximum replicas : 5
CPU target       : 70%
```

The Metrics Server provides resource metrics to the HPA.

```text
Application Load
       |
       ▼
CPU Utilization
       |
       ▼
Metrics Server
       |
       ▼
HPA
       |
       ▼
Backend Deployment
       |
       ▼
Backend Pod Count
```

The HPA was practically tested for both scale-up and scale-down behavior.

---

## Current Project Status

The project is currently under active development.

| Area                     | Status      | Focus                                           |
| ------------------------ | ----------- | ----------------------------------------------- |
| Backend Foundation       | ✅ Completed | Spring Boot REST API                            |
| API Quality              | ✅ Completed | DTOs, Validation, Exceptions, Swagger, Logging  |
| Authentication           | ✅ Completed | JWT Authentication & Authorization              |
| Docker Fundamentals      | ✅ Completed | Images, Containers, Volumes, Networking         |
| Docker Practicals        | ✅ Completed | Multi-stage Builds, Layer Caching, Optimization |
| Docker Compose           | ✅ Completed | Frontend + Backend + PostgreSQL                 |
| Kubernetes Fundamentals  | ✅ Completed | Architecture, Pods, Deployments, ReplicaSets    |
| Kubernetes Networking    | ✅ Completed | Services, ClusterIP, NodePort, DNS/CoreDNS      |
| Kubernetes Configuration | ✅ Completed | ConfigMaps & Secrets                            |
| Kubernetes Health Checks | ✅ Completed | Liveness & Readiness Probes                     |
| Kubernetes Storage       | ✅ Completed | PersistentVolumeClaim                           |
| Kubernetes Autoscaling   | ✅ Completed | Metrics Server + HPA                            |
| Kubernetes Ingress       | ✅ Completed | NGINX Ingress                                   |
| Kubernetes Application   | ✅ Completed | Frontend + Backend + PostgreSQL                 |
| CI/CD                    | ⏳ Planned   | GitHub Actions                                  |
| Helm                     | ⏳ Planned   | Kubernetes Package Management                   |
| Monitoring               | ⏳ Planned   | Prometheus + Grafana                            |
| Logging                  | ⏳ Planned   | Loki                                            |
| Caching                  | ⏳ Planned   | Redis                                           |
| Infrastructure as Code   | ⏳ Planned   | Terraform                                       |
| Cloud Deployment         | ⏳ Planned   | AWS                                             |

---

## Sprint Progress

| Sprint      | Status      | Focus                          |
| ----------- | ----------- | ------------------------------ |
| Sprint 01   | ✅ Completed | Spring Boot Backend Foundation |
| Sprint 02   | ✅ Completed | API Quality                    |
| Sprint 03   | ✅ Completed | Authentication & Authorization |
| Sprint 04   | ✅ Completed | Docker Fundamentals            |
| Sprint 04.5 | ✅ Completed | Docker Practicals              |
| Sprint 05   | ✅ Completed | React Frontend                 |
| Sprint 06   | ✅ Completed | Dockerized Application         |
| Sprint 07   | ✅ Completed | Kubernetes                     |
| Sprint 08   | ⏳ Planned   | GitHub Actions CI/CD           |
| Sprint 09   | ⏳ Planned   | Monitoring & Observability     |
| Sprint 10   | ⏳ Planned   | Helm, Terraform & AWS          |

---

## Project Structure

```text
cloud-native-task-platform/
│
├── README.md
│
├── docs/
│   ├── kubernetes/
│   │   ├── 01-kubernetes-basics.md
│   │   ├── 02-kubernetes-architecture.md
│   │   ├── 03-pods.md
│   │   ├── 04-deployments-and-replicasets.md
│   │   ├── 05-services.md
│   │   ├── 06-kubernetes-dns-and-coredns.md
│   │   ├── 07-kubernetes-networking.md
│   │   ├── 08-configmaps-and-secrets.md
│   │   ├── 09-health-probes.md
│   │   ├── 10-ingress.md
│   │   ├── 11-persistent-storage-pv-pvc.md
│   │   ├── 12-horizontal-pod-autoscaling.md
│   │   ├── 13-self-healing-and-rollouts.md
│   │   └── 14-kubernetes-practical-work.md
│   │
│   ├── sprints/
│   ├── setup/
│   ├── architecture/
│   ├── troubleshooting/
│   └── images/
│
├── kubernetes/
│   ├── backend-deployment.yaml
│   ├── backend-service.yaml
│   ├── backend-nodeport.yaml
│   ├── backend-configmap.yaml
│   ├── backend-secret.yaml
│   ├── backend-hpa.yaml
│   ├── frontend-deployment.yaml
│   ├── frontend-service.yaml
│   ├── postgres-deployment.yaml
│   ├── postgres-service.yaml
│   ├── postgres-configmap.yaml
│   ├── postgres-secret.yaml
│   ├── postgres-pvc.yaml
│   └── ingress.yaml
│
├── task-management-backend/
│
├── task-management-frontend/
│
└── docker-compose.yml
```

> The exact Kubernetes filenames may evolve as the project continues.

---

## Documentation

Detailed learning and implementation documentation is maintained under `docs/`.

### Kubernetes Documentation

```text
docs/kubernetes/

01 → Kubernetes Basics
02 → Kubernetes Architecture
03 → Pods
04 → Deployments & ReplicaSets
05 → Services
06 → DNS & CoreDNS
07 → Kubernetes Networking
08 → ConfigMaps & Secrets
09 → Health Probes
10 → Ingress
11 → Persistent Storage
12 → Horizontal Pod Autoscaling
13 → Self-Healing & Rollouts
14 → Kubernetes Practical Work
```

The detailed notes focus on:

* What each concept is
* Why it exists
* How it works
* Important beginner-level concepts
* How it relates to this project
* Practical examples
* Interview-oriented explanations

The README intentionally provides only a high-level overview; detailed technical explanations are maintained in the documentation.

---

## Getting Started

### Prerequisites

* Java 21
* Gradle
* Node.js
* npm
* PostgreSQL
* Docker
* kubectl
* Minikube

---

### Clone the Repository

```bash
git clone <repository-url>
cd cloud-native-task-platform
```

---

### Run the Backend Locally

```bash
cd task-management-backend
./gradlew bootRun
```

The backend runs on:

```text
http://localhost:8080
```

---

### Run the Frontend Locally

```bash
cd task-management-frontend
npm install
npm run dev
```

The frontend runs on:

```text
http://localhost:5173
```

---

### Run with Docker Compose

From the project root:

```bash
docker compose up --build
```

Stop the application:

```bash
docker compose down
```

---

### Run with Kubernetes

Start Minikube:

```bash
minikube start --driver=docker
```

Verify the cluster:

```bash
kubectl get nodes
```

Apply the Kubernetes resources:

```bash
kubectl apply -f kubernetes/
```

Check the deployed resources:

```bash
kubectl get pods
kubectl get deployments
kubectl get services
kubectl get ingress
kubectl get pvc
kubectl get hpa
```

---

## Useful Kubernetes Commands

### View Pods

```bash
kubectl get pods
```

### View Deployments

```bash
kubectl get deployments
```

### View ReplicaSets

```bash
kubectl get replicasets
```

### View Services

```bash
kubectl get services
```

### View Ingress

```bash
kubectl get ingress
```

### View PersistentVolumeClaims

```bash
kubectl get pvc
```

### View HPA

```bash
kubectl get hpa
```

### View all major resources

```bash
kubectl get all
```

### Describe a Pod

```bash
kubectl describe pod <pod-name>
```

### View Pod logs

```bash
kubectl logs <pod-name>
```

### Check Deployment rollout

```bash
kubectl rollout status deployment backend-deployment
```

---

## Development Roadmap

### Completed

* [x] Spring Boot backend foundation
* [x] PostgreSQL integration
* [x] Layered architecture
* [x] RESTful CRUD APIs
* [x] DTOs
* [x] Request validation
* [x] Global exception handling
* [x] Swagger / OpenAPI
* [x] Structured logging
* [x] JWT authentication and authorization
* [x] Docker fundamentals
* [x] Docker images
* [x] Docker layer caching
* [x] Docker multi-stage builds
* [x] Docker image optimization
* [x] Docker Compose
* [x] React frontend
* [x] Frontend containerization
* [x] Kubernetes Pods
* [x] Kubernetes Deployments
* [x] Kubernetes ReplicaSets
* [x] Kubernetes Services
* [x] ClusterIP
* [x] NodePort
* [x] Kubernetes DNS
* [x] CoreDNS
* [x] Kubernetes networking
* [x] ConfigMaps
* [x] Secrets
* [x] Liveness probes
* [x] Readiness probes
* [x] PersistentVolumeClaim
* [x] Metrics Server
* [x] Resource requests and limits
* [x] Horizontal Pod Autoscaling
* [x] HPA scale-up testing
* [x] HPA scale-down testing
* [x] NGINX Ingress
* [x] End-to-end Kubernetes application testing

### Planned

* [ ] Helm
* [ ] GitHub Actions CI/CD
* [ ] Automated Docker image builds
* [ ] Automated Docker image publishing
* [ ] Automated Kubernetes deployment
* [ ] Prometheus
* [ ] Grafana
* [ ] Loki
* [ ] Redis caching
* [ ] Terraform
* [ ] AWS infrastructure
* [ ] AWS deployment
* [ ] Production-oriented deployment configuration

---

## Future Architecture

The project will progressively evolve toward a production-oriented cloud-native architecture.

```text
                         Internet
                            |
                            ▼
                         AWS
                            |
                            ▼
                   Kubernetes Cluster
                            |
              +-------------+-------------+
              |                           |
          Frontend                     Backend
              |                           |
           Service                     Service
                                          |
                                          ▼
                                     PostgreSQL
                                          |
                                          ▼
                                    Persistent
                                     Storage
```

Additional DevOps and cloud components will be introduced in future sprints:

```text
GitHub
   |
   ▼
GitHub Actions
   |
   ▼
Docker Build
   |
   ▼
Container Registry
   |
   ▼
Kubernetes
   |
   +── Helm
   |
   +── Terraform
   |
   +── AWS
   |
   +── Prometheus
   |
   +── Grafana
   |
   +── Loki
   |
   └── Redis
```

---

## Author

**Abhishek Angadi**

Cloud Native Task Management Platform — a hands-on project focused on building practical backend, cloud-native, Kubernetes, and DevOps skills through incremental implementation.

```
```
