# Sprint 07 — Kubernetes — Interview Handy Notes

> These are short revision notes for interviews.
> The detailed concepts are documented separately in `docs/kubernetes/`.

---

## 1. What is Kubernetes?

Kubernetes is a **container orchestration platform**.

In simple words:

> Docker runs containers. Kubernetes manages containers at scale.

Kubernetes helps with:

- Running multiple application instances
- Scaling
- Self-healing
- Networking
- Service discovery
- Health checks
- Rolling updates
- Configuration and secrets
- Storage

---

## 2. Why do we need Kubernetes?

Docker is enough when we have a few containers.

But when we have many containers, manually managing them becomes difficult.

Kubernetes can automatically:

```text
Restart failed workloads
Maintain required replicas
Scale applications
Distribute traffic
Perform rolling updates
Provide service discovery
Check application health
````

---

## 3. Kubernetes Cluster

A Kubernetes cluster mainly consists of:

```text
Kubernetes Cluster
│
├── Control Plane
│
└── Worker Nodes
```

### Control Plane

The **brain/management part** of Kubernetes.

### Worker Node

The machine where application workloads run.

---

# 4. Important Control Plane Components

### API Server

Main communication entry point to Kubernetes.

```text
kubectl
   ↓
API Server
```

When we run:

```bash
kubectl get pods
```

`kubectl` communicates with the API Server.

---

### Scheduler

Decides **which Worker Node should run a newly created Pod**.

```text
New Pod
   ↓
Scheduler
   ↓
Suitable Worker Node
```

---

### Controller Manager

Runs controllers that continuously compare:

```text
Desired State
     ↓
Actual State
```

and try to make them match.

---

### etcd

Kubernetes' **key-value data store**.

It stores important cluster state and configuration.

Simple way to remember:

```text
etcd → Kubernetes cluster state
```

---

# 5. Worker Node Components

### kubelet

Agent running on each Worker Node.

It makes sure Pods assigned to the node are running as instructed.

```text
Control Plane
      ↓
   kubelet
      ↓
Container Runtime
      ↓
Container
```

### Container Runtime

Responsible for actually running containers.

---

# 6. Pod

A **Pod is the smallest deployable unit in Kubernetes**.

A Pod contains one or more containers.

Our project uses:

```text
Pod
 ↓
Spring Boot Container
```

Important:

```text
Pod ≠ Container
```

A Pod is the Kubernetes unit that contains and manages containers.

---

# 7. Deployment

A Deployment manages application Pods.

It defines things such as:

```yaml
replicas: 2
image: ...
```

In simple words:

> Deployment tells Kubernetes how we want our application Pods to run and helps manage updates.

---

# 8. ReplicaSet

A ReplicaSet maintains the required number of Pods.

```text
Deployment
    ↓
ReplicaSet
    ↓
Pods
```

For example:

```text
replicas: 2

ReplicaSet
   ├── Pod 1
   └── Pod 2
```

If one Pod disappears, the ReplicaSet helps create another.

---

# 9. Desired State

One of the **most important Kubernetes concepts**.

If we specify:

```yaml
replicas: 2
```

we are saying:

> "I want 2 Pods running."

Kubernetes continuously tries to make:

```text
Desired State = Actual State
```

Example:

```text
Desired = 2
Actual  = 1
       ↓
Kubernetes creates another Pod
       ↓
Desired = 2
Actual  = 2
```

This process is called **reconciliation**.

---

# 10. Declarative Approach

Instead of telling Kubernetes every individual action:

```text
Create Pod
Start Pod
Restart Pod
Create another Pod
```

we tell Kubernetes what we want:

```yaml
replicas: 2
```

Kubernetes decides what actions are necessary.

This is called a **declarative approach**.

---

# 11. Kubernetes Service

Pods can be recreated, so their IP addresses can change.

A Service provides a **stable network endpoint** for accessing Pods.

```text
Client
   ↓
Service
   ↓
Pods
```

Instead of:

```text
Client → Pod IP
```

we use:

```text
Client → backend-service → Backend Pods
```

---

# 12. Service Selector

A Service uses labels to identify its target Pods.

Pod:

```yaml
labels:
  app: backend
```

Service:

```yaml
selector:
  app: backend
```

Therefore:

```text
Service
   ↓
selector: app=backend
   ↓
Pods with app=backend
```

---

# 13. ClusterIP

**ClusterIP is the default Service type.**

It provides an internal stable endpoint inside the cluster.

Example:

```text
backend-service:8080
        ↓
Backend Pods
```

Use it mainly for:

```text
Pod → Service → Pod
```

internal communication.

---

# 14. NodePort

NodePort exposes a Service through a port on the Kubernetes node.

Example:

```text
NodeIP:30081
      ↓
NodePort Service
      ↓
Backend Pod
```

Useful for external/testing access, especially in Minikube.

---

# 15. LoadBalancer

Used commonly in cloud environments.

Conceptually:

```text
Internet
   ↓
Cloud Load Balancer
   ↓
Kubernetes Service
   ↓
Pods
```

---

# 16. `port` vs `targetPort`

Example:

```yaml
ports:
  - port: 80
    targetPort: 8080
```

Means:

```text
Client
  ↓
Service:80
  ↓
Pod:8080
```

Remember:

```text
port       → Service port
targetPort → Application/Pod port
```

---

# 17. Kubernetes DNS / CoreDNS

Pods should not depend on changing Pod IPs.

Instead, applications can use the Service name:

```text
backend-service
```

Kubernetes provides DNS-based service discovery using **CoreDNS**.

Simple flow:

```text
Application
     ↓
backend-service
     ↓
CoreDNS
     ↓
Service address
     ↓
Backend Pods
```

Important distinction:

```text
CoreDNS
→ Resolves names

Service networking
→ Routes traffic
```

A full Service DNS name can look like:

```text
backend-service.default.svc.cluster.local
```

---

# 18. Kubernetes Networking

Basic communication:

```text
Frontend Pod
     ↓
backend-service
     ↓
Backend Pod
```

Backend → PostgreSQL:

```text
Backend Pod
     ↓
postgres-service
     ↓
PostgreSQL Pod
```

The important idea is:

> Applications communicate through stable Services rather than relying on individual Pod IPs.

---

# 19. ConfigMap

A ConfigMap stores **non-sensitive configuration** separately from the application image.

Examples:

```text
Application configuration
Database host
Port
Environment settings
```

Pods can consume ConfigMap values as environment variables or files.

---

# 20. Secret

A Secret is used for **sensitive configuration**.

Examples:

```text
Database password
JWT secret
API credentials
```

Simple distinction:

```text
ConfigMap → normal configuration
Secret    → sensitive configuration
```

Secrets are not automatically equivalent to strong encryption just because the object is called a Secret.

---

# 21. Liveness Probe

Liveness answers:

> "Is the application still alive?"

If liveness repeatedly fails:

```text
Liveness failure
      ↓
Container restarted
```

Example:

```yaml
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8080
```

---

# 22. Readiness Probe

Readiness answers:

> "Is the application ready to receive traffic?"

If readiness fails:

```text
Readiness failure
      ↓
Pod becomes Not Ready
      ↓
Traffic is not normally sent to it
```

The container is **not restarted just because readiness fails**.

---

# 23. Liveness vs Readiness

Very common interview question.

| Probe     | Main Question             | Failure                                |
| --------- | ------------------------- | -------------------------------------- |
| Liveness  | Is the application alive? | Container may be restarted             |
| Readiness | Can it receive traffic?   | Pod removed from ready traffic targets |

Easy way to remember:

```text
Liveness  → Restart
Readiness → Traffic
```

---

# 24. Ingress

Ingress manages **external HTTP/HTTPS routing** into the cluster.

Our architecture:

```text
User
  ↓
Ingress
  ↓
Service
  ↓
Pods
```

For our project:

```text
/api → backend-service
/    → frontend-service
```

Important distinction:

```text
Ingress
→ External HTTP/HTTPS routing

Service
→ Stable access to Pods
```

---

# 25. Persistent Volume (PV)

A PersistentVolume represents storage available to Kubernetes workloads.

It allows application data to survive beyond the lifetime of an individual Pod.

---

# 26. PersistentVolumeClaim (PVC)

A PVC is a request for storage.

Simple mental model:

```text
Pod
 ↓
PVC
 ↓
PV
 ↓
Storage
```

In our project:

```text
PostgreSQL
    ↓
postgres-pvc
    ↓
Persistent Storage
```

This is important because database data should not depend on the lifetime of a Pod.

---

# 27. Horizontal Pod Autoscaler (HPA)

HPA automatically adjusts the number of Pods based on metrics such as CPU utilization.

Example:

```text
Low CPU
   ↓
Fewer Pods

High CPU
   ↓
More Pods
```

Our backend HPA was configured with:

```text
MINPODS = 2
MAXPODS = 5
TARGET CPU = 70%
```

So Kubernetes can scale the backend between 2 and 5 Pods based on the configured metric.

---

# 28. Self-Healing

Kubernetes can automatically recover from certain failures.

Example:

```text
Desired = 2
Actual  = 2

Pod crashes

Desired = 2
Actual  = 1

Kubernetes
    ↓
Creates replacement Pod

Desired = 2
Actual  = 2
```

This is mainly achieved through controllers such as ReplicaSets and Deployments.

---

# 29. Rolling Update

When we update an application version, Kubernetes can gradually replace old Pods with new Pods.

Conceptually:

```text
Old Pods
   ↓
New Pods
   ↓
Old Pods removed
```

This is called a **rolling update**.

It helps update applications without taking everything down at once.

---

# 30. Rollback

If a new Deployment version has a problem, Kubernetes Deployments support rolling back to a previous revision.

Simple idea:

```text
Version A
   ↓
Version B
   ↓
Problem
   ↓
Rollback
   ↓
Version A
```

---

# 31. Important Kubernetes Object Relationships

Remember these:

```text
Deployment
    ↓
ReplicaSet
    ↓
Pods
    ↓
Containers
```

Networking:

```text
Service
    ↓
Selected Pods
```

External routing:

```text
Ingress
    ↓
Service
    ↓
Pods
```

Storage:

```text
Pod
 ↓
PVC
 ↓
PV
 ↓
Storage
```

Autoscaling:

```text
HPA
 ↓
Deployment
 ↓
Pods
```

---

# 32. Our Project Architecture

Our current Kubernetes architecture is roughly:

```text
                         User
                           |
                           ↓
                        Ingress
                           |
                 +---------+---------+
                 |                   |
                 ↓                   ↓
          Frontend Service    Backend Service
                 |                   |
                 ↓                   ↓
          Frontend Pods        Backend Pods
                                     |
                                     ↓
                              PostgreSQL Service
                                     |
                                     ↓
                              PostgreSQL Pod
                                     |
                                     ↓
                                  PVC/PV
```

We also have Nginx and supporting Kubernetes resources in the project.

---

# 33. Important Commands

### View Pods

```bash
kubectl get pods
```

### View Deployments

```bash
kubectl get deployments
```

### View Services

```bash
kubectl get services
```

### View Ingress

```bash
kubectl get ingress
```

### View PVC

```bash
kubectl get pvc
```

### View everything

```bash
kubectl get all
```

### Describe a resource

```bash
kubectl describe pod <pod-name>
```

### View logs

```bash
kubectl logs <pod-name>
```

### Scale a Deployment

```bash
kubectl scale deployment backend-deployment --replicas=2
```

### Check rollout

```bash
kubectl rollout status deployment backend-deployment
```

### Restart Deployment

```bash
kubectl rollout restart deployment backend-deployment
```

---

# 34. Practical Kubernetes Concepts We Tested

In our project we actually practiced:

```text
Pods
Deployments
ReplicaSets
Services
ClusterIP
NodePort
Ingress
ConfigMaps
Secrets
Liveness probes
Readiness probes
PVC
HPA
Scaling
Rollouts
Self-healing
```

We also:

```text
Built Docker images
        ↓
Loaded images into Minikube
        ↓
Ran them through Kubernetes
        ↓
Verified image IDs
        ↓
Tested the backend API
```

---

# 35. Most Important Interview Questions

### 1. What is Kubernetes?

> Kubernetes is a container orchestration platform used to manage containerized applications, including scaling, networking, health checks, self-healing and deployments.

### 2. Why Kubernetes when we already have Docker?

> Docker helps us build and run containers, while Kubernetes manages many containers and provides features such as scaling, self-healing, service discovery and rolling updates.

### 3. What is a Pod?

> A Pod is the smallest deployable unit in Kubernetes and contains one or more containers.

### 4. What is a Deployment?

> A Deployment manages the desired state, replicas and updates of application Pods.

### 5. What is a ReplicaSet?

> A ReplicaSet maintains the required number of Pod replicas.

### 6. What is a Service?

> A Service provides a stable network endpoint for accessing a group of Pods whose IP addresses may change.

### 7. ClusterIP vs NodePort?

> ClusterIP is mainly for internal cluster communication, while NodePort exposes a Service through a port on the Kubernetes node.

### 8. What is Ingress?

> Ingress provides HTTP/HTTPS routing from outside the cluster to Kubernetes Services.

### 9. What is the difference between liveness and readiness?

> Liveness checks whether the application is alive and can cause a restart. Readiness checks whether the application is ready to receive traffic.

### 10. What is ConfigMap vs Secret?

> ConfigMap stores normal configuration, while Secret is intended for sensitive configuration such as passwords and credentials.

### 11. What is HPA?

> HPA automatically adjusts the number of Pods based on metrics such as CPU utilization.

### 12. What is Kubernetes self-healing?

> Kubernetes continuously works toward the desired state, so if a managed Pod fails, controllers can create a replacement.

### 13. What is CoreDNS?

> CoreDNS provides DNS-based service discovery inside the Kubernetes cluster, allowing applications to find Services by name.

### 14. What is PVC?

> A PersistentVolumeClaim is a request for persistent storage by a workload.

### 15. What is the Kubernetes Control Plane?

> The Control Plane manages the Kubernetes cluster. It includes components such as the API Server, Scheduler, Controller Manager and etcd.

---

# 36. The 10 Things to Remember First

If you have very little time before an interview, remember these:

```text
1. Kubernetes = container orchestration

2. Cluster = Control Plane + Worker Nodes

3. Pod = smallest deployable unit

4. Deployment → ReplicaSet → Pods

5. Desired State = Actual State

6. Service = stable networking for Pods

7. Ingress = external HTTP/HTTPS routing

8. Liveness = restart
   Readiness = traffic

9. HPA = automatic Pod scaling

10. CoreDNS = Service name resolution
```

---

# 37. One-Minute Kubernetes Explanation

If an interviewer asks:

> "Explain Kubernetes."

You can explain it naturally:

> "Kubernetes is a container orchestration platform. Docker helps us package and run containers, but when an application has many containers, manually managing them becomes difficult. Kubernetes manages those workloads using objects like Deployments and Pods. A Deployment maintains the desired number of Pods, Services provide stable networking to those Pods, Ingress handles external HTTP routing, and Kubernetes can automatically scale, restart and update workloads. The main idea is that we define the desired state and Kubernetes continuously works to keep the actual state matching it."

---

# 38. Final Mental Model

Don't try to memorize every Kubernetes command.

Understand this:

```text
                KUBERNETES
                     |
              Desired State
                     |
                     ↓
              Control Plane
                     |
        +------------+------------+
        |            |            |
    API Server    Scheduler   Controllers
        |                         |
       etcd                       ↓
                              Deployment
                                  ↓
                              ReplicaSet
                                  ↓
                                Pods
                                  ↓
                              Containers
                                  |
                              Worker Node
```

Networking:

```text
User
 ↓
Ingress
 ↓
Service
 ↓
Pods
```

Internal service discovery:

```text
Application
 ↓
Service Name
 ↓
CoreDNS
 ↓
Service
 ↓
Pods
```

Storage:

```text
Pod
 ↓
PVC
 ↓
PV
 ↓
Persistent Storage
```

Scaling:

```text
HPA
 ↓
Deployment
 ↓
More/Fewer Pods
```

Health:

```text
Liveness  → Is it alive? → Restart if necessary

Readiness → Can it receive traffic? → Include/exclude from traffic
```

The single most important Kubernetes idea is:

```text
Desired State
      ↓
Kubernetes
      ↓
Actual State
      ↓
Reconciliation
      ↓
Keep them matching
```

That understanding is more valuable than memorizing Kubernetes definitions.

```
```
