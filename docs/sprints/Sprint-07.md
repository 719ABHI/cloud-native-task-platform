# Sprint 07 — Kubernetes

**Status: ✅ Completed**

## Goal

Learn the fundamentals of Kubernetes and deploy our Cloud Native Task Platform on a local Kubernetes cluster using Minikube.

## What We Learned

* Kubernetes basics and why container orchestration is needed
* Kubernetes architecture

    * Control Plane
    * Worker Node
    * API Server
    * Scheduler
    * Controller Manager
    * etcd
    * kubelet
    * Container Runtime
* Pods
* Deployments and ReplicaSets
* Desired State and reconciliation
* Kubernetes Services

    * ClusterIP
    * NodePort
    * Service selectors
    * `port` vs `targetPort`
* Kubernetes DNS and CoreDNS
* Kubernetes networking basics
* ConfigMaps and Secrets
* Liveness and Readiness probes
* Ingress and external HTTP routing
* Persistent storage with PV/PVC
* Horizontal Pod Autoscaling
* Self-healing and rollouts

## Practical Work

Implemented and tested:

* Backend, frontend, PostgreSQL and Nginx Deployments
* Multiple backend replicas
* ClusterIP and NodePort Services
* Ingress routing
* ConfigMap and Secret configuration
* PostgreSQL PVC
* Backend HPA
* Liveness and Readiness probes
* Docker image loading into Minikube
* Image verification using image IDs
* Deployment scaling
* Rollouts and ReplicaSets
* Kubernetes debugging using `describe`, `logs` and events
* Tested the actual backend API through the Kubernetes/Ingress path

## Important Practical Learning

We intentionally broke the health probes to understand their behavior:

```text
Readiness failure
        ↓
Pod becomes Not Ready
        ↓
Container is NOT restarted
```

```text
Liveness failure
        ↓
Container is restarted
        ↓
Repeated failures
        ↓
CrashLoopBackOff
```

We also verified the complete application path:

```text
User
  ↓
Ingress
  ↓
Backend Service
  ↓
Backend Pod
  ↓
Spring Boot
  ↓
PostgreSQL Service
  ↓
PostgreSQL Pod
  ↓
Persistent Storage
```

## Key Kubernetes Mental Model

```text
Desired State
      ↓
Kubernetes
      ↓
Actual State
      ↓
Reconciliation
      ↓
Keep them synchronized
```

And:

```text
Deployment
    ↓
ReplicaSet
    ↓
Pods
    ↓
Containers
```

```text
Service
    ↓
Stable networking
    ↓
Pods
```

## Documentation Created

```text
docs/kubernetes/
├── 01-kubernetes-basics.md
├── 02-kubernetes-architecture.md
├── 03-pods.md
├── 04-deployments-and-replicasets.md
├── 05-services.md
├── 06-kubernetes-dns-and-coredns.md
├── 07-kubernetes-networking.md
├── 08-configmaps-and-secrets.md
├── 09-health-probes.md
├── 10-ingress.md
├── 11-persistent-storage-pv-pvc.md
├── 12-horizontal-pod-autoscaling.md
├── 13-self-healing-and-rollouts.md
└── 14-kubernetes-practical-work.md
```

## Outcome

**Sprint 07 completed. ✅**

We now have a working Kubernetes deployment of the application on Minikube, along with the core Kubernetes concepts and practical behavior documented.

**Next:** Sprint 08 → **GitHub Actions / CI/CD**.
