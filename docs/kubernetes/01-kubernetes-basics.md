# 1. What is Kubernetes?

Before Kubernetes, let's think about how we would run our application manually.

Suppose our project has:

- Frontend
- Backend
- PostgreSQL

We could run:

```text
Frontend    → Docker container
Backend     → Docker container
PostgreSQL  → Docker container
```

That works.

But now imagine our application becomes bigger.

We might have:

```text
Frontend        → 3 containers
Backend         → 5 containers
PostgreSQL      → 1 container
Redis           → 1 container
Nginx           → 2 containers
```

Now we have many containers.

We need something to manage them.

For example:

- What if a container crashes?
- Who starts it again?
- What if we need 5 backend instances?
- How do we distribute requests between them?
- How do we expose the application?
- How do containers communicate with each other?
- How do we update the application without manually stopping everything?
- How do we check whether an application is healthy?
- How do we store data even if a container is recreated?

This is where Kubernetes comes in.

### Simple Definition

> **Kubernetes is a system that manages and coordinates containers for us.**

Instead of manually managing every container, we tell Kubernetes **what we want**, and Kubernetes continuously works to maintain that state.

---

# 2. Why Was Kubernetes Introduced?

Docker solved an important problem:

> **"How can I package and run my application consistently?"**

But Docker alone doesn't solve everything when we have many containers.

For example:

```text
Docker
   ↓
Run container
Stop container
Restart container
Manage individual containers
```

When the application becomes larger:

```text
                    ┌── Backend
                    ├── Backend
User → Application ─┼── Backend
                    ├── Frontend
                    ├── PostgreSQL
                    └── Redis
```

Managing all of these manually becomes difficult.

Kubernetes provides **orchestration**.

## Docker vs Kubernetes

Think of it like this:

### Docker

> "Run this container."

### Kubernetes

> "I want 2 backend instances running. Make sure they stay running, expose them through a service, send traffic only to healthy instances, and replace failed instances."

That distinction is important.

---

# 3. What Does "Orchestration" Mean?

Orchestration simply means:

> **Automatically managing multiple containers according to a desired configuration.**

For example, we tell Kubernetes:

```yaml
replicas: 2
```

Meaning:

> **"I want two backend Pods running."**

Kubernetes continuously checks the actual state.

If:

```text
Desired: 2
Running: 2
```

Everything is fine.

If:

```text
Desired: 2
Running: 1
```

Kubernetes tries to bring the system back to:

```text
Desired: 2
Running: 2
```

This is one of the most important ideas in Kubernetes:

## Desired State

We tell Kubernetes **what we want**, rather than manually telling it every individual action.

---

# 4. Kubernetes Works Using Desired State

Suppose our Deployment says:

```yaml
replicas: 2
```

We are essentially saying:

> **I want 2 backend Pods.**

Kubernetes continuously compares:

```text
Desired State
      ↓
    2 Pods

      VS

Actual State
      ↓
    1 Pod
```

If one Pod disappears:

```text
Desired = 2
Actual  = 1
```

Kubernetes creates another Pod.

So Kubernetes is constantly trying to make:

```text
Actual State = Desired State
```

This explains a lot of what we observed in our project.

For example, when we scaled:

```bash
kubectl scale deployment backend-deployment --replicas=0
```

Kubernetes removed the backend Pods.

Then when we did:

```bash
kubectl scale deployment backend-deployment --replicas=2
```

Kubernetes created two Pods again.

---

# 5. What Is a Kubernetes Cluster?

A **Kubernetes cluster** is the complete Kubernetes environment.

Conceptually:

```text
              Kubernetes Cluster
                     │
          ┌──────────┴──────────┐
          │                     │
     Control Plane          Worker Nodes
```

The cluster contains the components responsible for:

- Managing the cluster
- Running our applications

---

# 6. Control Plane

The **Control Plane** is basically the **brain of the Kubernetes cluster**.

It makes decisions about the cluster.

For example:

- What Pods should exist?
- Where should they run?
- Is the desired state satisfied?
- Should a new Pod be created?
- Should a failed Pod be replaced?
- What is the current state of the cluster?

Think:

```text
Control Plane
     ↓
"How should the cluster look?"
```

---

# 7. Worker Node

A **worker node** is where our application's workloads actually run.

For example:

```text
Worker Node
│
├── Backend Pod
├── Backend Pod
├── Frontend Pod
└── PostgreSQL Pod
```

The worker node provides the environment where containers run.

In our Minikube setup, we have a Kubernetes cluster with a node named:

```text
minikube
```

We saw this when running:

```bash
kubectl describe pod ...
```

and got:

```text
Node: minikube/192.168.49.2
```

So our Pods are running on the Minikube node.

---

# 8. Important Kubernetes Architecture

At a high level:

```text
                    Kubernetes Cluster
                           │
             ┌─────────────┴─────────────┐
             │                           │
       Control Plane                 Worker Node
             │                           │
             │                    ┌──────┴──────┐
             │                    │             │
             │                  Pod           Pod
             │                    │             │
             │                Container     Container
             │
             ├── API Server
             ├── Scheduler
             ├── Controller Manager
             └── etcd
```

There are other components too, but these are the important architectural pieces to understand first.

---

# 9. API Server

The **API Server** is the main communication point with Kubernetes.

When we run:

```bash
kubectl get pods
```

we are not directly asking the Pod.

The flow is roughly:

```text
kubectl
   ↓
Kubernetes API Server
   ↓
Kubernetes cluster state
```

Similarly, when we run:

```bash
kubectl scale deployment backend-deployment --replicas=2
```

we are telling the Kubernetes API Server:

> **"Change the desired state of this Deployment to 2 replicas."**

The API Server is therefore a very important entry point into Kubernetes.

---

# 10. Scheduler

Suppose Kubernetes needs to create a new Pod.

It needs to decide:

> **Which worker node should run this Pod?**

The **Scheduler** makes this scheduling decision.

Conceptually:

```text
New Pod
   ↓
Scheduler
   ↓
Choose suitable Worker Node
   ↓
Pod gets scheduled there
```

In our Minikube environment, we currently have a simple setup, so the workload is scheduled onto the Minikube node.

---

# 11. Controller Manager

This is closely related to the **desired state** concept.

Controllers continuously observe the cluster and try to make the actual state match the desired state.

For example:

```text
Deployment says:
"I want 2 backend Pods."

        ↓

Actual:
"I currently have 1."

        ↓

Controller:
"I need to create another Pod."
```

This is why Kubernetes can automatically recover from certain failures.

---

# 12. etcd

`etcd` is Kubernetes' key-value data store.

It stores important cluster state and configuration.

You don't normally interact with `etcd` directly in our project.

For now, understand it as:

> **The database where Kubernetes stores its cluster state.**

Conceptually:

```text
API Server
     ↓
   etcd
     ↓
Cluster state
```

---

# 13. kubelet

Now we move to the worker node.

Each worker node has a component called **kubelet**.

Its responsibility is basically:

> **Make sure the Pods assigned to this node are actually running as instructed.**

For example:

```text
Control Plane:
"Run this Pod on the node."

        ↓

      kubelet

        ↓

Creates/manages the container
```

The kubelet also performs things such as health-probe checks for containers.

This connects directly to what we recently learned about:

- Liveness Probe
- Readiness Probe

---

# 14. Container Runtime

The containers themselves need a runtime.

In our Minikube setup, Kubernetes is using Docker as the container runtime environment.

The important conceptual distinction is:

```text
Kubernetes
    ↓
kubelet
    ↓
Container Runtime
    ↓
Container
```

We don't need to go deeply into container runtime internals yet.

---

# 15. Kubernetes Architecture — Simple Mental Model

You can remember the architecture like this:

```text
                 KUBERNETES CLUSTER
                        │
            ┌───────────┴───────────┐
            │                       │
       CONTROL PLANE            WORKER NODE
            │                       │
            │                  ┌────┴────┐
            │                  │         │
            │                 Pod       Pod
            │                  │         │
            │              Container  Container
            │
       API Server
       Scheduler
       Controllers
       etcd
```

And the overall idea is:

```text
             "What should happen?"
                      │
                 Control Plane
                      │
             "Run these workloads"
                      ↓
                 Worker Node
                      │
                    Pods
                      │
                 Containers
```

---

# 16. How This Relates to Our Project

Our project currently has:

```text
                    User
                      │
                      ↓
                 Ingress
                      │
             ┌────────┴────────┐
             │                 │
             ↓                 ↓
       Frontend Service   Backend Service
             │                 │
       Frontend Pods      Backend Pods
                               │
                               ↓
                       PostgreSQL Service
                               │
                               ↓
                       PostgreSQL Pod
```

We also have:

```text
Backend Pods
     │
     ├── Pod 1
     └── Pod 2
```

and Kubernetes manages these through:

```text
backend-deployment
```

Our Deployment currently maintains:

```text
replicas = 2
```

and we verified:

```text
backend-deployment-...   1/1 Running
backend-deployment-...   1/1 Running
```

---

# 17. Kubernetes Is NOT the Same Thing as Docker

This distinction is important for interviews.

## Docker

Docker primarily gives us:

```text
Image
   ↓
Container
   ↓
Application
```

It helps us package and run applications in containers.

## Kubernetes

Kubernetes manages those workloads:

```text
                    Kubernetes
                        │
        ┌───────────────┼────────────────┐
        ↓               ↓                ↓
     Backend         Frontend         Database
       Pods            Pods              Pod
        │               │                │
   Containers       Containers       Container
```

So:

> **Docker helps us create/run containers. Kubernetes helps us manage containers at scale.**

---

# 18. Why Don't We Directly Manage Containers in Kubernetes?

This is another important mental shift.

We generally don't tell Kubernetes:

```text
"Create this exact container."

"Restart this exact container."

"Create another copy of this container."
```

Instead, we describe what we want:

```yaml
replicas: 2
```

or:

```yaml
image: cloud-native-task-platform-backend:latest
```

or:

```yaml
livenessProbe:
  ...
```

Then Kubernetes continuously works toward that desired configuration.

This is called the **declarative approach**.

---

# 19. Declarative vs Imperative

## Imperative

You tell the system exactly what action to perform:

```text
Create container.
Start container.
Restart container.
Create another container.
```

## Declarative

You say:

```text
I want 2 backend Pods running.
```

Kubernetes figures out what actions are necessary.

This is one of the fundamental ideas behind Kubernetes.

---

# 20. Interview Explanation

If an interviewer asks:

### "What is Kubernetes?"

Don't try to memorize a complicated definition.

You can say:

> **"Kubernetes is a container orchestration platform. It helps us manage containerized applications by maintaining the desired state of the application, handling things like scaling, restarting failed workloads, networking, service discovery, and deployment."**

If they ask:

### "Why do we need Kubernetes if we already have Docker?"

Say:

> **"Docker helps us package and run containers, but when we have many containers, managing them manually becomes difficult. Kubernetes automates that management—for example, maintaining multiple replicas, restarting failed containers, distributing traffic, and scaling the application."**

If they ask:

### "What is a Kubernetes cluster?"

Say:

> **"A Kubernetes cluster is the complete Kubernetes environment consisting of the control plane, which manages the cluster, and worker nodes, where the application workloads run."**

If they ask:

### "What is the control plane?"

Say:

> **"The control plane is like the brain of Kubernetes. It receives requests through the API server, decides where workloads should run, and continuously makes sure the actual state matches the desired state."**

---

# 21. What We Should Remember

Don't memorize all the component descriptions individually yet.

Build this mental picture:

```text
                     KUBERNETES
                         │
              "I want this state"
                         │
                         ↓
                  CONTROL PLANE
                         │
              manages / decides
                         │
                         ↓
                   WORKER NODE
                         │
                        Pods
                         │
                    Containers
```

And the most important principle:

```text
        DESIRED STATE
             │
             ↓
        Kubernetes
             │
             ↓
        ACTUAL STATE
             │
             ↓
     Keep them matching
```

That **desired-state/reconciliation idea** is the foundation for understanding:

- Deployments
- ReplicaSets
- Services
- HPA
- Self-healing
- Much of Kubernetes

---

# Questions We Had While Learning — Concepts Captured

We don't need to put our conversational questions verbatim into the notes, but the concepts behind them belong here.

- Why does Kubernetes restart something?
- What happens if the application's main process stops?
- What happens if the process is running but the application itself isn't healthy?
- Why do we need health probes?
- Why does readiness failure not restart a container?
- How does Kubernetes know how many Pods should exist?
- Why can we have multiple Pods for the same backend?
- How does traffic reach one of those Pods?

These will be answered in their **respective concept notes**, rather than making this first note unnecessarily deep.

---

# Quick Mental Summary

If we reduce everything in this note to one flow:

```text
                    Kubernetes Cluster
                           │
                           │
                  ┌────────┴────────┐
                  │                 │
             Control Plane      Worker Node
                  │                 │
                  │                 ├── Pod
                  │                 │    └── Container
                  │                 │
                  │                 └── Pod
                  │                      └── Container
                  │
        ┌─────────┼──────────┐
        │         │          │
    API Server  Scheduler  Controllers
        │
       etcd
```

The control plane manages the cluster, while worker nodes run our application workloads.

The most important idea is:

```text
We declare what we want
          ↓
     Kubernetes
          ↓
Kubernetes continuously works
to make actual state match
desired state
```

That is the foundation on which the rest of Kubernetes is built.