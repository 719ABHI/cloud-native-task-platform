# Kubernetes Architecture

## 1. Why do we need to understand Kubernetes architecture?

We already learned that Kubernetes manages our containerized applications.

But saying:

> "Kubernetes manages containers."

is not enough.

We should understand:

- Who receives our commands?
- Who stores the cluster information?
- Who decides where a Pod should run?
- Who makes sure the desired number of Pods exist?
- Who actually runs the containers?
- How does the Control Plane communicate with Worker Nodes?
- What happens when we create a Deployment?
- What happens when we scale a Deployment?
- What happens when a Pod fails?

Understanding the architecture helps us answer these questions.

---

# 2. High-level Kubernetes architecture

A Kubernetes cluster can be divided into two major parts:

```text
                    Kubernetes Cluster
                           |
             +-------------+-------------+
             |                           |
        Control Plane                Worker Node
             |                           |
       Manages cluster              Runs workloads
             |                           |
       +-----+-----+             +-----+-----+
       |     |     |             |     |     |
      API   Sched  Controller   kubelet  Pods
     Server  uler   Manager              |
       |                               Containers
      etcd
````

The easiest way to remember this is:

```text
Control Plane
     ↓
"How should the cluster look?"

Worker Node
     ↓
"Run the application workloads."
```

The Control Plane manages the cluster.

Worker Nodes run our application workloads.

---

# 3. Control Plane

The Control Plane is responsible for managing the Kubernetes cluster.

We can think of it as the **brain of Kubernetes**.

It is responsible for things such as:

* Receiving requests
* Storing cluster state
* Making scheduling decisions
* Monitoring the desired state
* Creating or replacing workloads when necessary

The important Control Plane components we need to understand are:

```text
Control Plane
│
├── API Server
├── Scheduler
├── Controller Manager
└── etcd
```

Each component has a different responsibility.

---

# 4. API Server

The Kubernetes API Server is the main communication point for the Kubernetes cluster.

Whenever we use:

```bash
kubectl
```

we normally communicate with Kubernetes through the API Server.

For example:

```bash
kubectl get pods
```

The request flow is approximately:

```text
kubectl
   |
   ↓
API Server
   |
   ↓
Kubernetes cluster state
```

We are not directly communicating with a Pod when we run:

```bash
kubectl get pods
```

Instead, `kubectl` sends a request to the Kubernetes API Server.

The API Server processes the request and returns the information.

---

# 5. Example: kubectl scale

Suppose we run:

```bash
kubectl scale deployment backend-deployment --replicas=2
```

We are not manually creating two containers.

We are telling Kubernetes:

> "I want the backend Deployment to have 2 replicas."

The request goes through the API Server.

Conceptually:

```text
kubectl
   |
   ↓
API Server
   |
   ↓
Deployment desired state
   |
   ↓
Controllers
```

The controllers then work to make the actual state match the desired state.

This is an important distinction.

We tell Kubernetes **what we want**.

Kubernetes determines the actions required to achieve it.

---

# 6. API Server is the central communication point

The API Server is important because many Kubernetes components communicate through it.

Conceptually:

```text
                 API Server
                /    |     \
               /     |      \
              ↓      ↓       ↓
          Scheduler Controllers kubectl
                         |
                         ↓
                      Cluster
```

We should therefore remember:

> The API Server is the central entry point for interacting with the Kubernetes cluster.

---

# 7. etcd

`etcd` is Kubernetes' distributed key-value data store.

Kubernetes needs somewhere to store important information about the cluster.

For example, Kubernetes needs to remember information such as:

* What Deployments exist?
* What Pods should exist?
* What configuration has been submitted?
* What resources are defined?
* What is the current cluster state?

This information is stored in `etcd`.

A simple mental model is:

```text
API Server
     |
     ↓
   etcd
     |
     ↓
Kubernetes cluster state
```

We don't normally interact directly with etcd in our project.

For our level of understanding, remember:

> etcd is the persistent key-value store used by Kubernetes to store cluster state.

---

# 8. Why does Kubernetes need etcd?

Imagine we tell Kubernetes:

> I want 2 backend Pods.

Kubernetes needs to remember that requirement.

Otherwise, if the system temporarily loses information, it would not know what the desired configuration was.

So Kubernetes stores important cluster information in etcd.

This supports the desired-state model we learned earlier.

Conceptually:

```text
User
 |
 | "I want 2 backend Pods"
 ↓
API Server
 |
 ↓
etcd
 |
 ↓
Desired state is stored
 |
 ↓
Controllers work toward that state
```

---

# 9. Scheduler

The Scheduler is responsible for deciding **which Worker Node should run a newly created Pod**.

Suppose Kubernetes needs to create a Pod.

It needs to answer:

> Where should this Pod run?

The Scheduler makes this scheduling decision.

Conceptually:

```text
New Pod
   |
   ↓
Scheduler
   |
   ↓
Select suitable Worker Node
   |
   ↓
Pod gets scheduled
```

The Scheduler considers factors such as available resources and scheduling requirements.

We don't need to go deeply into all scheduling algorithms yet.

The important idea is:

> Scheduler decides where a Pod should run.

---

# 10. Example with multiple Worker Nodes

Imagine a cluster has:

```text
Worker Node 1
Worker Node 2
Worker Node 3
```

And Kubernetes needs to create:

```text
Backend Pod
```

The Scheduler evaluates the available nodes and chooses a suitable one.

For example:

```text
                 Scheduler
                     |
          +----------+----------+
          |          |          |
          ↓          ↓          ↓
       Node 1      Node 2      Node 3
          |          |          |
          |       suitable      |
          |          ↓          |
          |      Backend Pod    |
          |                    |
```

In our Minikube setup, things are simpler because we have a single-node environment.

We saw:

```text
Node: minikube/192.168.49.2
```

when we ran:

```bash
kubectl describe pod
```

So our Pods are running on the Minikube node.

---

# 11. Controller Manager

The Controller Manager is responsible for running Kubernetes controllers.

Controllers continuously observe the cluster and try to make:

```text
Actual State
```

match:

```text
Desired State
```

This is directly connected to the most important Kubernetes concept we learned earlier.

For example:

```text
Deployment

Desired:
2 backend Pods

Actual:
1 backend Pod
```

The controller recognizes that the actual state does not match the desired state.

It works toward:

```text
Desired = 2
Actual  = 2
```

---

# 12. Controllers and self-healing

Suppose we have:

```text
backend Deployment
replicas: 2
```

Initially:

```text
Backend Pod 1
Backend Pod 2
```

Now imagine Pod 1 disappears.

The state becomes:

```text
Desired = 2
Actual  = 1
```

The controller notices the difference.

It creates another Pod.

Eventually:

```text
Backend Pod 2
Backend Pod 3
```

Now:

```text
Desired = 2
Actual  = 2
```

This is one reason Kubernetes provides self-healing behavior.

We don't manually create the replacement Pod.

Kubernetes works toward the desired state.

---

# 13. Deployment → ReplicaSet → Pods

This is an important relationship we observed in our project.

We created a Deployment:

```text
backend-deployment
```

The Deployment does not directly behave like:

```text
Deployment
    ↓
Container
```

There is another object involved:

```text
Deployment
    ↓
ReplicaSet
    ↓
Pods
    ↓
Containers
```

This hierarchy is very important.

---

# 14. What is a Deployment?

A Deployment is a Kubernetes object used to manage application Pods.

For example, our backend Deployment contains:

```yaml
spec:
  replicas: 2
```

This means:

> "I want 2 Pods for this application."

The Deployment helps manage:

* Desired number of replicas
* Updates
* Rollouts
* Rollbacks
* Pod versions

Our Deployment is:

```text
backend-deployment
```

---

# 15. What is a ReplicaSet?

A ReplicaSet's responsibility is to maintain the required number of Pods.

For example:

```text
Deployment
    |
    ↓
ReplicaSet
    |
    ├── Backend Pod
    └── Backend Pod
```

If the desired number is:

```text
replicas: 2
```

the ReplicaSet works to maintain two Pods.

This is why when we ran:

```bash
kubectl get rs -l app=backend
```

we saw ReplicaSets such as:

```text
backend-deployment-64d6ff7d46
```

Some old ReplicaSets had:

```text
DESIRED   CURRENT   READY
0         0         0
```

while the current ReplicaSet had:

```text
2         2         2
```

The old ReplicaSets remain because Deployments use them for rollout and revision history.

---

# 16. Why were there many old ReplicaSets?

During our project we repeatedly changed and restarted the Deployment.

We saw:

```text
backend-deployment-59b5c58f4b
backend-deployment-5b95999878
backend-deployment-5d57cd8986
backend-deployment-745598db8c
backend-deployment-86d59d7679
backend-deployment-f79957f9c
```

Most of them had:

```text
DESIRED = 0
CURRENT = 0
READY = 0
```

This does not mean those old Pods are still running.

They are old ReplicaSet revisions maintained by the Deployment.

The currently active ReplicaSet is the one maintaining the running Pods.

---

# 17. Pod

A Pod is the smallest deployable unit in Kubernetes.

For our application:

```text
Pod
 |
 └── Backend container
```

We can think of a Pod as the Kubernetes wrapper around one or more containers.

Our backend Pods look conceptually like:

```text
Backend Pod
     |
     └── Spring Boot container
```

We currently use one container per Pod.

---

# 18. Container inside the Pod

Our backend container runs:

```text
Spring Boot application
```

The container image is:

```text
cloud-native-task-platform-backend:latest
```

Our Pod therefore looks like:

```text
backend Pod
     |
     └── backend container
             |
             └── Spring Boot application
```

The container exposes:

```text
8080
```

---

# 19. Worker Node

Now we move from the Control Plane to the Worker Node.

A Worker Node is where application workloads actually run.

Conceptually:

```text
Worker Node
│
├── Pod
│   └── Container
│
├── Pod
│   └── Container
│
└── Pod
    └── Container
```

Our Minikube environment currently uses a node named:

```text
minikube
```

We saw this in:

```bash
kubectl describe pod
```

which showed:

```text
Node: minikube/192.168.49.2
```

---

# 20. kubelet

Every Worker Node has a kubelet.

The kubelet is responsible for making sure the Pods assigned to that node are running as instructed.

Conceptually:

```text
Control Plane
      |
      ↓
"Run this Pod"
      |
      ↓
Worker Node
      |
      ↓
    kubelet
      |
      ↓
Container Runtime
      |
      ↓
   Container
```

The kubelet communicates with the Kubernetes control plane and manages the workloads assigned to its node.

---

# 21. kubelet and health probes

This connects directly to what we recently learned.

Our backend Deployment contains:

```yaml
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8080

readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8080
```

The kubelet performs these checks for the container.

For example:

```text
kubelet
   |
   | HTTP GET
   ↓
/actuator/health/liveness
```

If the liveness probe repeatedly fails, Kubernetes can restart the container.

If the readiness probe fails, the container is not restarted just because of that failure.

Instead, Kubernetes considers the Pod not ready to receive traffic.

This explains what we observed earlier.

---

# 22. Liveness vs Readiness from the architecture perspective

We tested a deliberately incorrect readiness endpoint:

```text
/actuator/health/does-not-exist
```

The events showed:

```text
Readiness probe failed
HTTP probe failed with statuscode: 404
```

But the container remained running.

This demonstrated:

```text
Readiness failure
      ↓
Pod becomes Not Ready
      ↓
Container continues running
```

Then we tested an incorrect liveness endpoint.

The events showed:

```text
Liveness probe failed
HTTP probe failed with statuscode: 404
```

and:

```text
Killing
Container backend failed liveness probe, will be restarted
```

So:

```text
Readiness failure
→ stop sending traffic to the Pod

Liveness failure
→ restart the container
```

This is an important practical Kubernetes concept.

---

# 23. Container Runtime

The kubelet needs something to actually run the containers.

That is the responsibility of the container runtime.

Conceptually:

```text
Kubernetes
    ↓
kubelet
    ↓
Container Runtime
    ↓
Container
```

The container runtime is responsible for running the containers.

For our learning level, we don't need to go deeply into runtime internals yet.

The important thing is to understand the role:

```text
kubelet
    ↓
tells the runtime what containers need to run
```

---

# 24. Important correction about Docker and Kubernetes

We previously learned that Docker can run containers.

It is important not to think:

```text
Kubernetes directly runs Docker containers itself.
```

Instead, the architecture is conceptually:

```text
Kubernetes
     ↓
kubelet
     ↓
Container Runtime
     ↓
Container
```

The exact runtime used by a Kubernetes installation can vary.

For our project, the important concept is the separation of responsibilities.

---

# 25. Complete request: from kubectl to a running Pod

Let's understand what happens when we apply our backend Deployment.

Suppose we run:

```bash
kubectl apply -f backend-deployment.yaml
```

A simplified flow is:

```text
kubectl
   |
   ↓
API Server
   |
   ↓
Deployment object
   |
   ↓
Controller Manager
   |
   ↓
ReplicaSet
   |
   ↓
Pod
   |
   ↓
Scheduler
   |
   ↓
Worker Node
   |
   ↓
kubelet
   |
   ↓
Container Runtime
   |
   ↓
Backend Container
```

There are internal details and ordering nuances in the real implementation, but this is the mental model we should use initially.

---

# 26. What happens when we request 2 replicas?

Our Deployment says:

```yaml
replicas: 2
```

We are essentially saying:

> "I want 2 backend Pods."

Conceptually:

```text
User
 |
 | kubectl apply
 ↓
API Server
 |
 ↓
Deployment
 |
 ↓
ReplicaSet
 |
 | desired = 2
 ↓
Pods
 |
 +---- Pod 1
 |
 +---- Pod 2
```

Eventually:

```text
Deployment
     |
     ↓
ReplicaSet
     |
     +---------+
     |         |
     ↓         ↓
   Pod 1     Pod 2
     |         |
 Container  Container
```

We verified this in our project using:

```bash
kubectl get pods -l app=backend
```

and saw:

```text
backend-deployment-...   1/1   Running
backend-deployment-...   1/1   Running
```

---

# 27. What happens when we scale to zero?

We tested:

```bash
kubectl scale deployment backend-deployment --replicas=0
```

Then:

```bash
kubectl get pods -l app=backend
```

showed:

```text
No resources found
```

Why?

Because we changed the desired state.

Before:

```text
Desired = 2
Actual  = 2
```

After scaling:

```text
Desired = 0
Actual  = 2
```

The controller works to reconcile the difference.

Eventually:

```text
Desired = 0
Actual  = 0
```

Therefore the backend Pods disappear.

Then when we ran:

```bash
kubectl scale deployment backend-deployment --replicas=2
```

Kubernetes created two Pods again.

This is desired-state reconciliation in action.

---

# 28. What happens when a Pod crashes?

Suppose:

```text
Desired = 2
Actual  = 2
```

and one Pod crashes permanently.

Now:

```text
Desired = 2
Actual  = 1
```

The ReplicaSet/controller notices the difference.

It creates another Pod.

Eventually:

```text
Desired = 2
Actual  = 2
```

This is one part of Kubernetes' self-healing behavior.

---

# 29. What happens during a Deployment update?

This is why Deployments and ReplicaSets are useful.

Suppose our backend currently uses:

```text
Backend image version A
```

We build a new version:

```text
Backend image version B
```

and update the Deployment.

The Deployment can create a new ReplicaSet for the new version.

Conceptually:

```text
Deployment
    |
    +-------------------+
    |                   |
Old ReplicaSet      New ReplicaSet
    |                   |
Old Pods             New Pods
```

During a rollout, Kubernetes can gradually replace the old Pods with new Pods.

This is called a rolling update.

We will study deployments and rollouts more practically in a separate concept.

---

# 30. Our project's architecture

Our current project contains:

```text
                    Kubernetes Cluster
                           |
            +--------------+--------------+
            |                             |
       Control Plane                  Worker Node
                                          |
                +-------------------------+----------------------+
                |              |             |                  |
             Backend         Frontend      PostgreSQL          Nginx
               Pods            Pods           Pod               Pods
```

Our actual workloads include:

```text
Backend
  ↓
backend-deployment
  ↓
2 Backend Pods
```

```text
Frontend
  ↓
frontend-deployment
  ↓
3 Frontend Pods
```

```text
PostgreSQL
  ↓
postgres-deployment
  ↓
1 PostgreSQL Pod
```

```text
Nginx
  ↓
nginx-deployment
  ↓
multiple Nginx Pods
```

---

# 31. Our backend architecture

Our backend currently looks like:

```text
backend-deployment
        |
        ↓
ReplicaSet
        |
    +---+---+
    |       |
    ↓       ↓
 Backend  Backend
   Pod      Pod
    |        |
 Container Container
    |        |
Spring Boot Spring Boot
```

Both Pods use:

```text
cloud-native-task-platform-backend:latest
```

We verified their image IDs using:

```bash
kubectl get pods -l app=backend \
-o jsonpath='{range .items[*]}{.metadata.name}{" -> "}{.status.containerStatuses[0].imageID}{"\n"}{end}'
```

Both Pods had the same image ID.

That confirms both Pods are running the same image version.

---

# 32. Important distinction: Pod vs Container

A common beginner mistake is to treat Pods and containers as the same thing.

They are not exactly the same.

### Container

```text
Runs the application
```

### Pod

```text
Kubernetes unit that contains one or more containers
```

For our backend:

```text
Pod
 |
 └── Container
       |
       └── Spring Boot application
```

We currently use one container per Pod, which is common for simple applications.

A Pod can technically contain multiple tightly coupled containers.

We don't need to use multiple containers in one Pod for our current project.

---

# 33. Complete Kubernetes architecture mental model

The entire architecture can be remembered as:

```text
                         KUBERNETES CLUSTER
                                |
                +---------------+---------------+
                |                               |
          CONTROL PLANE                    WORKER NODE
                |                               |
        +-------+-------+                 +-----+------+
        |       |       |                 |            |
     API     Scheduler Controllers      kubelet       Pods
    Server                              |              |
        |                               ↓              ↓
       etcd                       Container Runtime  Containers
```

The responsibilities are:

```text
API Server
→ Communication entry point

etcd
→ Stores cluster state

Scheduler
→ Decides where Pods should run

Controllers
→ Reconcile desired state with actual state

kubelet
→ Manages Pods assigned to the node

Container Runtime
→ Runs containers

Pod
→ Kubernetes unit containing containers
```

---

# 34. The most important flow to remember

If you forget everything else, remember this:

```text
              WHAT DO WE WANT?
                     |
                     ↓
               API Server
                     |
                     ↓
             Desired State
                     |
                     ↓
              Controllers
                     |
                     ↓
          Deployment / ReplicaSet
                     |
                     ↓
                   Pods
                     |
                     ↓
                Worker Node
                     |
                   kubelet
                     |
                     ↓
             Container Runtime
                     |
                     ↓
                Containers
```

And Kubernetes continuously checks:

```text
Desired State
      |
      ↓
   Compare
      ↑
      |
Actual State
```

If they are different:

```text
Kubernetes takes action
```

until they match.

---

# 35. What we should understand instead of memorizing

We don't need to memorize long definitions.

Instead, understand the responsibility of each component.

Think of it like a company:

```text
Control Plane
     ↓
Management
```

```text
API Server
     ↓
Reception / communication point
```

```text
Scheduler
     ↓
Decides where work should go
```

```text
Controllers
     ↓
Continuously make sure requirements are satisfied
```

```text
etcd
     ↓
Stores important cluster information
```

```text
Worker Node
     ↓
Where the actual work happens
```

```text
kubelet
     ↓
Node-level manager
```

```text
Container Runtime
     ↓
Actually runs containers
```

```text
Pod
     ↓
Kubernetes workload unit
```

This mental model is much easier to remember.

---

# 36. Interview explanations

## What is the Kubernetes Control Plane?

Simple answer:

> "The Control Plane is the management part of Kubernetes. It receives requests through the API Server, stores cluster state, makes scheduling decisions, and uses controllers to make sure the actual state matches the desired state."

## What is the API Server?

> "The API Server is the main communication entry point to Kubernetes. Tools like kubectl communicate with the cluster through the API Server."

## What is etcd?

> "etcd is Kubernetes' key-value data store where important cluster state and configuration are stored."

## What does the Scheduler do?

> "The Scheduler decides which Worker Node should run a newly created Pod based on the available resources and scheduling requirements."

## What does the Controller Manager do?

> "It runs controllers that continuously compare the desired state with the actual state and take action to bring them back into sync."

## What is a Worker Node?

> "A Worker Node is a machine in the Kubernetes cluster where application workloads such as Pods actually run."

## What is kubelet?

> "kubelet is the agent running on each Worker Node. It makes sure the Pods assigned to that node are running as instructed and performs health checks such as liveness and readiness probes."

## What is a Pod?

> "A Pod is the smallest deployable unit in Kubernetes. It contains one or more containers that are managed together. In our project, each backend Pod contains one Spring Boot container."

## What is the relationship between Deployment, ReplicaSet and Pod?

> "A Deployment manages the desired state and updates of an application. It creates and manages ReplicaSets, and the ReplicaSet maintains the required number of Pods."

The simple hierarchy is:

```text
Deployment
    ↓
ReplicaSet
    ↓
Pods
    ↓
Containers
```

---

# 37. Questions that naturally arise from this architecture

These are concepts we should study separately rather than making this architecture note unnecessarily deep.

### Question 1

If Pods have IP addresses, why do we need Services?

→ This leads to **Kubernetes Services**.

### Question 2

How does one Service send traffic to the correct Pods?

→ This leads to **Service selectors and Endpoints/EndpointSlices**.

### Question 3

How do Pods communicate using names instead of IP addresses?

→ This leads to **Kubernetes DNS / CoreDNS**.

### Question 4

How does traffic from outside the cluster reach our application?

→ This leads to **NodePort and Ingress**.

### Question 5

How does Kubernetes know whether an application is healthy?

→ This leads to **Liveness and Readiness Probes**.

### Question 6

How does Kubernetes automatically create more Pods?

→ This leads to **Deployments, ReplicaSets and HPA**.

### Question 7

Where does PostgreSQL data survive when the Pod is recreated?

→ This leads to **Persistent Volumes and PVCs**.

These are separate concepts, so we will document them separately.

---

# 38. What we actually practiced in our project

We didn't only study Kubernetes theoretically.

We actually worked with the architecture.

We:

* Created Deployments
* Created Pods through Deployments
* Used ReplicaSets
* Scaled backend replicas
* Observed Pods being created and removed
* Rebuilt Docker images
* Loaded images into Minikube
* Used `imagePullPolicy: Never`
* Verified image IDs running inside Pods
* Used Services
* Used Ingress
* Used ConfigMaps and Secrets
* Added liveness probes
* Added readiness probes
* Intentionally broke a readiness endpoint
* Observed that readiness failure did not restart the container
* Intentionally broke a liveness endpoint
* Observed that liveness failure caused the container to restart
* Observed `CrashLoopBackOff`
* Restored the correct probe configuration
* Verified the Pods became `1/1 Running`

These practical observations are important because they connect the architecture to actual Kubernetes behavior.

---

# 39. Final mental model

Think about Kubernetes like this:

```text
                    KUBERNETES
                         |
                "I want this state"
                         |
                         ↓
                    API SERVER
                         |
                         ↓
                  CONTROL PLANE
             +-----------+-----------+
             |           |           |
           etcd      Scheduler   Controllers
                                     |
                                     ↓
                                Deployments
                                     |
                                     ↓
                                ReplicaSets
                                     |
                                     ↓
                                   Pods
                                     |
                                     ↓
                               Worker Node
                                     |
                                   kubelet
                                     |
                              Container Runtime
                                     |
                                     ↓
                                Containers
```

The most important idea remains:

```text
             DESIRED STATE
                   |
                   ↓
              Kubernetes
                   |
                   ↓
              ACTUAL STATE
                   |
                   ↓
         Keep them synchronized
```

That is the foundation for understanding the rest of Kubernetes.

---

# 40. Summary

At this point, we should have the following mental model:

```text
Kubernetes Cluster
│
├── Control Plane
│   │
│   ├── API Server
│   │     └── Main communication entry point
│   │
│   ├── Scheduler
│   │     └── Decides where Pods should run
│   │
│   ├── Controller Manager
│   │     └── Reconciles desired state with actual state
│   │
│   └── etcd
│         └── Stores cluster state
│
└── Worker Node
    │
    ├── kubelet
    │     └── Manages Pods assigned to the node
    │
    ├── Container Runtime
    │     └── Runs containers
    │
    └── Pods
          └── Containers
```

And for our application:

```text
Deployment
    ↓
ReplicaSet
    ↓
Backend Pods
    ↓
Spring Boot Containers
```

The central Kubernetes principle is:

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

This desired-state and reconciliation model is the foundation for understanding:

* Deployments
* ReplicaSets
* Services
* Self-healing
* Health probes
* HPA
* Rolling updates
* Service discovery
* Kubernetes networking
* Persistent storage
* And much of Kubernetes itself.

```
```
