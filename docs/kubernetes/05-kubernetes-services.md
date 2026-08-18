# Kubernetes Services

## 1. Why do we need Services?

We already learned that Kubernetes applications run inside Pods.

For example, our backend currently has multiple Pods:

```text
backend-deployment
        |
        v
   ReplicaSet
        |
   +----+----+
   |         |
   v         v
Backend    Backend
Pod 1      Pod 2
````

Each Pod gets its own IP address.

For example:

```text
Backend Pod 1 → 10.244.0.20
Backend Pod 2 → 10.244.0.21
```

At first, we might think:

> "The frontend can simply send requests to the backend Pod IP."

But there is a problem.

Pods are not permanent.

A Pod can be:

* deleted
* recreated
* restarted
* replaced during a Deployment update
* scaled up
* scaled down

When a Pod is recreated, it can receive a different IP address.

For example:

```text
Before:

Backend Pod 1 → 10.244.0.20
Backend Pod 2 → 10.244.0.21


Pod 1 is deleted.


After:

Backend Pod 2 → 10.244.0.21
New Backend Pod → 10.244.0.35
```

If the frontend was directly using:

```text
10.244.0.20
```

that address may no longer work.

This is why Kubernetes provides **Services**.

---

# 2. What is a Kubernetes Service?

A Kubernetes Service provides a stable way to access a group of Pods.

Simple definition:

> A Service is a stable network endpoint that forwards traffic to the appropriate Pods.

Instead of applications communicating directly with changing Pod IP addresses:

```text
Frontend
   |
   ↓
Pod IP
```

we use:

```text
Frontend
   |
   ↓
Backend Service
   |
   +----> Backend Pod
   |
   +----> Backend Pod
```

The Service gives us a stable endpoint even when the individual Pods change.

---

# 3. Why was Service introduced?

The main problem is:

```text
Pod IPs can change.
```

But applications need a stable way to communicate.

For example, our frontend needs to communicate with our backend.

We don't want the frontend to know:

```text
Backend Pod 1 IP
Backend Pod 2 IP
Backend Pod 3 IP
```

Instead, the frontend should communicate with:

```text
backend-service
```

Kubernetes then takes care of sending the request to an appropriate backend Pod.

So the basic idea is:

```text
Changing Pods
     ↓
     ↓
Stable Service
     ↑
     ↑
Other applications
```

---

# 4. Service provides a stable endpoint

Suppose we have:

```text
Backend Pod 1 → 10.244.0.20
Backend Pod 2 → 10.244.0.21
```

We create:

```text
backend-service
```

The Service gets its own stable ClusterIP:

```text
backend-service
        ↓
10.111.127.205
```

Now another application does not need to know the individual Pod IPs.

It communicates with:

```text
backend-service
```

The Service forwards the request to one of the backend Pods.

Conceptually:

```text
Client
  |
  ↓
backend-service
  |
  +------> Backend Pod 1
  |
  +------> Backend Pod 2
```

---

# 5. Service does not create Pods

This is an important beginner concept.

A Service does **not** create Pods.

For example:

```text
Deployment
    ↓
ReplicaSet
    ↓
Pods
```

The Service is a separate Kubernetes object:

```text
Service
    ↓
Provides networking access to Pods
```

So:

```text
Deployment → manages Pods

Service → provides stable networking to Pods
```

They have different responsibilities.

---

# 6. Our backend Service

In our project, we have:

```text
backend-service
```

We verified it using:

```bash
kubectl get services
```

and saw:

```text
backend-service   ClusterIP   10.111.127.205   <none>   8080/TCP
```

This tells us:

```text
Service name:
backend-service

Type:
ClusterIP

Cluster IP:
10.111.127.205

Service port:
8080
```

Our backend Pods also listen on:

```text
8080
```

So the basic communication path is:

```text
Frontend
    |
    ↓
backend-service:8080
    |
    ↓
Backend Pod:8080
```

---

# 7. What is ClusterIP?

`ClusterIP` is the default Kubernetes Service type.

It provides an IP address that can be accessed from inside the Kubernetes cluster.

For example:

```text
backend-service
       |
       ↓
ClusterIP
10.111.127.205
```

Other Pods inside the cluster can communicate with this Service.

For example:

```text
Frontend Pod
     |
     | request
     ↓
backend-service:8080
     |
     ↓
Backend Pod
```

The ClusterIP is generally not directly accessible from outside the Kubernetes cluster.

---

# 8. Why do we need a stable ClusterIP?

Pod IPs can change.

Service ClusterIP remains stable for the lifetime of the Service.

For example:

```text
Backend Pod 1
10.244.0.20
```

may disappear.

A new Pod might become:

```text
10.244.0.35
```

But:

```text
backend-service
10.111.127.205
```

can remain the same.

So applications don't have to track changing Pod IPs.

---

# 9. Service selects Pods

Now an important question:

> How does the Service know which Pods should receive traffic?

The answer is:

**Selectors.**

Our backend Pods have a label:

```yaml
labels:
  app: backend
```

For example:

```text
Backend Pod 1
labels:
  app: backend

Backend Pod 2
labels:
  app: backend
```

Our Service can use:

```yaml
selector:
  app: backend
```

This means:

> "Send traffic to Pods having the label `app=backend`."

Conceptually:

```text
                backend-service
                       |
                selector: app=backend
                       |
             +---------+---------+
             |                   |
             ↓                   ↓
       app=backend         app=backend
       Backend Pod 1       Backend Pod 2
```

---

# 10. Labels and selectors

Labels are key-value information attached to Kubernetes objects.

For example:

```yaml
labels:
  app: backend
```

The Service can use a selector:

```yaml
selector:
  app: backend
```

The Service then finds the Pods matching that selector.

This creates the relationship:

```text
Service
   |
   | selector
   ↓
Pods with matching labels
```

This is extremely important.

A Service does not simply know:

> "These are my Pods."

It identifies the target Pods using selectors.

---

# 11. Example Service YAML

A simplified backend Service can look like:

```yaml
apiVersion: v1
kind: Service

metadata:
  name: backend-service

spec:
  selector:
    app: backend

  ports:
    - port: 8080
      targetPort: 8080

  type: ClusterIP
```

Let's understand each important part.

---

# 12. `metadata.name`

```yaml
metadata:
  name: backend-service
```

This gives the Service its name.

Other Kubernetes resources can use this name to communicate with the Service.

For example:

```text
backend-service
```

---

# 13. `selector`

```yaml
selector:
  app: backend
```

This tells the Service:

> "Find Pods that have the label `app=backend`."

Our backend Deployment creates Pods with:

```yaml
labels:
  app: backend
```

Therefore the Service can find them.

---

# 14. `port`

Consider:

```yaml
ports:
  - port: 8080
```

`port` is the port exposed by the Service.

So clients communicate with:

```text
backend-service:8080
```

---

# 15. `targetPort`

We can have:

```yaml
ports:
  - port: 8080
    targetPort: 8080
```

These two values have different meanings.

### `port`

The port on the Service.

```text
backend-service:8080
```

### `targetPort`

The port on the target Pod/container.

```text
Backend Pod:8080
```

So:

```text
Client
   |
   | backend-service:8080
   ↓
Service
   |
   | forwards to targetPort 8080
   ↓
Backend Pod:8080
```

---

# 16. Why can `port` and `targetPort` be different?

They don't have to be the same.

For example:

```yaml
ports:
  - port: 80
    targetPort: 8080
```

This means:

```text
Client
   |
   ↓
Service:80
   |
   ↓
Pod:8080
```

The Service listens on port `80`, but forwards traffic to port `8080` on the Pod.

In our backend:

```yaml
port: 8080
targetPort: 8080
```

so they happen to be the same.

---

# 17. Service and Pods — complete flow

Let's combine everything.

Suppose we have:

```text
Backend Pod 1
IP: 10.244.0.20
label: app=backend

Backend Pod 2
IP: 10.244.0.21
label: app=backend
```

And:

```text
backend-service
ClusterIP: 10.111.127.205
selector: app=backend
```

The flow becomes:

```text
Client
   |
   ↓
backend-service:8080
   |
   ↓
ClusterIP
10.111.127.205
   |
   ↓
Service selects Pods
   |
   +----------+
   |          |
   ↓          ↓
Pod 1       Pod 2
```

The Service provides the stable access point.

---

# 18. What happens if a Pod is deleted?

Suppose:

```text
backend-service
       |
       +----> Pod 1
       |
       +----> Pod 2
```

Pod 1 gets deleted.

The Deployment/ReplicaSet creates a replacement:

```text
backend-service
       |
       +----> New Pod
       |
       +----> Pod 2
```

The new Pod can have a completely different IP.

The application using the Service does not need to know about the change.

This is one of the biggest advantages of Services.

---

# 19. Service is not the same as load balancer

When a Service has multiple backend Pods, traffic can be distributed among those Pods.

For example:

```text
              backend-service
                    |
             +------+------+
             |             |
             ↓             ↓
         Backend Pod 1  Backend Pod 2
```

This provides a basic mechanism for distributing traffic among the selected Pods.

However, we should not think of every Kubernetes Service as a cloud-provider load balancer.

The exact networking behavior depends on the Service type and Kubernetes networking implementation.

For our current level, remember:

> A Service provides a stable endpoint and sends traffic to the selected Pods.

---

# 20. How does Kubernetes know which Pods belong to the Service?

The relationship is:

```text
Service selector
        ↓
Pod labels
```

For our backend:

```yaml
Service:

selector:
  app: backend
```

Backend Pods:

```yaml
labels:
  app: backend
```

Because they match:

```text
app=backend
```

the Service considers those Pods as its targets.

---

# 21. What if the labels don't match?

Suppose our Service says:

```yaml
selector:
  app: backend
```

but our Pods have:

```yaml
labels:
  app: frontend
```

Then the Service won't find those Pods.

Conceptually:

```text
Service
selector:
app=backend

        X

Pod
label:
app=frontend
```

Therefore there would be no matching backend endpoint.

This is a common beginner mistake when creating Services.

---

# 22. Endpoints and EndpointSlices

We don't need to go deeply into Kubernetes networking internals yet, but it is useful to know one important concept.

Kubernetes keeps track of the Pods that are selected by a Service.

Modern Kubernetes uses **EndpointSlices** for this purpose.

Conceptually:

```text
Service
   |
   ↓
EndpointSlice
   |
   +---- Pod IP 1
   |
   +---- Pod IP 2
   |
   +---- Pod IP 3
```

The EndpointSlice represents the network endpoints associated with the Service.

We don't need to manually manage these in our project.

Kubernetes updates them when Pods are added, removed, or become unavailable.

---

# 23. Readiness and Services

This connects directly to our previous learning.

Suppose we have:

```text
Backend Pod 1 → Ready
Backend Pod 2 → Not Ready
```

The Service should not send normal application traffic to a Pod that is not ready.

This is one reason readiness probes are important.

The relationship is:

```text
Readiness Probe
       ↓
Is Pod ready to receive traffic?
       ↓
Yes → eligible for Service traffic
No  → not considered ready for normal traffic
```

This connects our two concepts:

```text
Readiness Probe
       ↓
Pod readiness
       ↓
Service traffic
```

This is exactly why we learned earlier that a readiness failure does not necessarily restart the container.

---

# 24. Service discovery

Now we reach another important concept.

Suppose the frontend needs to call the backend.

We don't want to hard-code:

```text
10.244.0.20
```

because Pod IPs can change.

Instead, the frontend can use the Service name:

```text
backend-service
```

Kubernetes provides service discovery so applications inside the cluster can locate Services by name.

Conceptually:

```text
Frontend Pod
     |
     | backend-service
     ↓
Kubernetes DNS
     |
     ↓
Service IP
     |
     ↓
Backend Pods
```

---

# 25. Kubernetes DNS / CoreDNS

Kubernetes normally provides DNS-based service discovery through **CoreDNS**.

For example, inside the cluster, a Pod can resolve:

```text
backend-service
```

to the Service's network address.

So instead of remembering:

```text
10.111.127.205
```

an application can use:

```text
backend-service
```

This is much more stable.

A simplified mental model is:

```text
Application
    |
    | "backend-service"
    ↓
CoreDNS
    |
    ↓
Service IP
    |
    ↓
Backend Pods
```

CoreDNS handles the DNS resolution.

The actual packet forwarding to the selected Pods is handled by Kubernetes networking components rather than CoreDNS itself.

This distinction is important:

```text
CoreDNS
→ Resolves the Service name

Service networking
→ Sends traffic toward the Service/selected Pods
```

---

# 26. Service DNS names

Inside the same namespace, applications can usually use the short Service name:

```text
backend-service
```

A fuller DNS name can be:

```text
backend-service.default.svc.cluster.local
```

The structure is roughly:

```text
<service-name>.<namespace>.svc.cluster.local
```

For our project:

```text
backend-service.default.svc.cluster.local
```

Because our resources are in the `default` namespace, simply using:

```text
backend-service
```

is usually enough for communication within that namespace.

---

# 27. Service types

Kubernetes provides different Service types.

The important beginner-level types are:

```text
ClusterIP
NodePort
LoadBalancer
```

We should understand the purpose of each.

---

# 28. ClusterIP

ClusterIP is the default Service type.

It is mainly used for internal cluster communication.

Example:

```text
Frontend Pod
     |
     ↓
backend-service
     |
     ↓
Backend Pods
```

Our project has:

```text
backend-service
```

with:

```text
TYPE: ClusterIP
```

We use this for internal backend communication.

---

# 29. NodePort

NodePort exposes a Service through a port on the Kubernetes node.

For example, our project has:

```text
backend-nodeport
```

We saw:

```text
backend-nodeport   NodePort   10.102.9.135   <none>   8080:30081/TCP
```

The important part is:

```text
8080:30081
```

This means:

```text
Service port = 8080
NodePort = 30081
```

Conceptually:

```text
External Client
      |
      ↓
Node IP:30081
      |
      ↓
NodePort Service
      |
      ↓
Backend Pod:8080
```

---

# 30. Why did we have a NodePort?

NodePort is useful when we want to make a Service reachable through a port on the Kubernetes node.

In Minikube, this can be useful for testing.

For example:

```text
minikube node IP
      |
      ↓
port 30081
      |
      ↓
backend-nodeport
      |
      ↓
backend Pods
```

However, in our current application architecture, we are using **Ingress** as the main external entry point.

So we don't need to expose every application using NodePort.

---

# 31. LoadBalancer

`LoadBalancer` is commonly used in cloud environments.

It asks the underlying cloud provider to provision an external load balancer.

Conceptually:

```text
Internet
    |
    ↓
Cloud Load Balancer
    |
    ↓
Kubernetes Service
    |
    ↓
Pods
```

We don't need to use LoadBalancer for our current Minikube architecture.

We will encounter this concept again when we move toward AWS.

---

# 32. Our project's Services

We currently have:

```text
backend-nodeport
backend-service

frontend-service

nginx-nodeport
nginx-service

postgres-service
```

We verified them using:

```bash
kubectl get services
```

Our important internal Services are:

```text
backend-service
postgres-service
nginx-service
```

These use ClusterIP.

We also have NodePort Services for some workloads, mainly useful for external/testing access.

---

# 33. Backend Service

Our backend Service:

```text
backend-service
```

has:

```text
TYPE:
ClusterIP

CLUSTER-IP:
10.111.127.205

PORT:
8080
```

Its job is to provide stable internal access to our backend Pods.

Conceptually:

```text
Frontend
   |
   ↓
backend-service:8080
   |
   +------> Backend Pod
   |
   +------> Backend Pod
```

---

# 34. PostgreSQL Service

Our PostgreSQL Service is:

```text
postgres-service
```

We saw:

```text
postgres-service   ClusterIP   10.99.144.206   <none>   5432/TCP
```

PostgreSQL listens on:

```text
5432
```

So the backend can communicate with PostgreSQL through:

```text
postgres-service:5432
```

instead of using the PostgreSQL Pod's IP.

The architecture becomes:

```text
Backend Pod
     |
     ↓
postgres-service:5432
     |
     ↓
PostgreSQL Pod
```

This is much more reliable than directly using the Pod IP.

---

# 35. Frontend → Backend

Our project has:

```text
Frontend Pods
      |
      ↓
Backend Service
      |
      ↓
Backend Pods
```

The frontend does not need to know which backend Pod is currently running.

The Service provides the stable endpoint.

Conceptually:

```text
Frontend Pod
     |
     | request
     ↓
backend-service
     |
     +------------+
     |            |
     ↓            ↓
Backend Pod 1  Backend Pod 2
```

---

# 36. Backend → PostgreSQL

Similarly:

```text
Backend Pod
     |
     ↓
postgres-service
     |
     ↓
PostgreSQL Pod
```

The backend uses the Service rather than directly depending on the PostgreSQL Pod IP.

This is an important example of Service-based communication inside our project.

---

# 37. Service and Ingress are different

Another important beginner distinction:

A Service and an Ingress are not the same thing.

Our architecture is:

```text
User
 |
 ↓
Ingress
 |
 +------------------+
 |                  |
 ↓                  ↓
Frontend Service   Backend Service
 |                  |
 ↓                  ↓
Frontend Pods      Backend Pods
```

The **Ingress** controls external HTTP/HTTPS routing.

The **Service** provides stable access to Pods.

So:

```text
Ingress
→ Routes external HTTP requests

Service
→ Provides stable networking to Pods
```

---

# 38. Our Ingress

We have:

```text
application-ingress
```

We verified:

```text
kubectl get ingress
```

and saw:

```text
application-ingress
CLASS: nginx
ADDRESS: 192.168.49.2
PORTS: 80
```

Our Ingress rules route:

```text
/api
    ↓
backend-service:8080

/
    ↓
frontend-service:80
```

So the complete external flow is:

```text
User
 |
 ↓
192.168.49.2
 |
 ↓
Ingress
 |
 +-------------------+
 |                   |
 /api                /
 |                   |
 ↓                   ↓
Backend Service   Frontend Service
 |                   |
 ↓                   ↓
Backend Pods       Frontend Pods
```

---

# 39. Service communication in our project

Putting the important pieces together:

```text
                    User
                      |
                      ↓
                Ingress
                      |
             +--------+--------+
             |                 |
            /api               /
             |                 |
             ↓                 ↓
      Backend Service     Frontend Service
             |                 |
             ↓                 ↓
       Backend Pods       Frontend Pods
             |
             ↓
     PostgreSQL Service
             |
             ↓
      PostgreSQL Pod
```

This is the networking architecture we currently have.

---

# 40. What happens when a backend Pod changes?

Suppose our backend initially has:

```text
Backend Pod A
Backend Pod B
```

and the Service points to both.

Now Kubernetes replaces Pod A.

We get:

```text
Backend Pod C
Backend Pod B
```

The Service automatically updates its set of usable endpoints based on the matching Pods.

The frontend still uses:

```text
backend-service
```

It doesn't need to change anything.

This is the main reason Services are useful.

---

# 41. Service does not give Pods permanent IPs

This is another common misunderstanding.

A Service gives us a stable network endpoint.

It does **not** make Pod IPs permanent.

For example:

```text
Service:
10.111.127.205   ← stable Service IP

Pod:
10.244.0.20      ← can change
```

So:

```text
Service IP = stable endpoint

Pod IP = temporary workload address
```

---

# 42. ClusterIP vs Pod IP

It is useful to clearly distinguish them.

### Pod IP

Assigned to an individual Pod.

Example:

```text
10.244.0.20
```

It can change when the Pod is recreated.

### ClusterIP

Assigned to a Service.

Example:

```text
10.111.127.205
```

It provides a stable endpoint for accessing the selected Pods.

So:

```text
Pod IP
→ Individual Pod

ClusterIP
→ Service
```

---

# 43. Service vs Deployment

These objects also have different jobs.

### Deployment

Answers:

> "How many Pods should exist and how should they be updated?"

### Service

Answers:

> "How can other applications reliably communicate with these Pods?"

So:

```text
Deployment
   ↓
Manages Pods

Service
   ↓
Provides networking to Pods
```

They work together.

---

# 44. Service vs ReplicaSet

Similarly:

### ReplicaSet

Maintains the required number of Pods.

### Service

Provides a stable endpoint for reaching those Pods.

Conceptually:

```text
Deployment
    ↓
ReplicaSet
    ↓
Pods
    ↑
    |
Service
```

The Service does not manage the number of Pods.

The ReplicaSet does.

---

# 45. A complete mental model

Think of the architecture this way:

```text
Deployment
    |
    | "I want 2 Pods"
    ↓
ReplicaSet
    |
    ↓
+-------+-------+
|               |
Pod 1           Pod 2
|               |
+-------+-------+
        ↑
        |
     Service
        |
        |
Stable endpoint
```

The Deployment/ReplicaSet handles **workload management**.

The Service handles **network access**.

---

# 46. What we actually practiced

In our project, we:

* Created ClusterIP Services
* Created NodePort Services
* Checked Services using `kubectl get services`
* Observed ClusterIP addresses
* Observed Service ports
* Connected backend Pods through `backend-service`
* Connected PostgreSQL through `postgres-service`
* Used Services behind our Ingress
* Used Pod labels such as `app=backend`
* Used Service selectors
* Observed multiple Pods behind one Service
* Learned that Pod IPs can change
* Learned why Services provide stable access
* Connected readiness probes with Service traffic
* Used Ingress to route `/api` to `backend-service`
* Used `/` to route to `frontend-service`

These practical observations connect the Service concept to our actual Kubernetes architecture.

---

# 47. Questions that naturally arise

Now that we understand Services, several questions naturally appear.

### Question 1

How does Kubernetes actually implement the Service IP?

This leads to Kubernetes networking and kube-proxy/service routing.

### Question 2

How does the Service know exactly which Pods are available?

This leads to EndpointSlices.

### Question 3

How can the frontend use `backend-service` instead of an IP address?

This leads to Kubernetes DNS and CoreDNS.

### Question 4

How does traffic from the user's browser reach the Service?

This leads to Ingress and NodePort.

### Question 5

What happens if all backend Pods are unhealthy?

This connects Services with readiness probes.

These concepts can be studied separately rather than making this note unnecessarily deep.

---

# 48. Simple interview explanation

If an interviewer asks:

## What is a Kubernetes Service?

You can say:

> "A Kubernetes Service provides a stable network endpoint for a group of Pods. Since Pod IP addresses can change when Pods are recreated, applications communicate with the Service instead. The Service uses selectors to identify the appropriate Pods and forwards traffic to them."

---

## Why do we need Services?

You can say:

> "Pods are temporary and their IP addresses can change. A Service gives us a stable endpoint so applications don't need to track individual Pod IPs."

---

## What is ClusterIP?

You can say:

> "ClusterIP is the default Kubernetes Service type. It provides an internal stable IP that can be used by applications inside the cluster to communicate with the selected Pods."

---

## What is a Service selector?

You can say:

> "A Service selector defines which Pods should receive traffic. For example, if the selector is `app=backend`, the Service targets Pods having that label."

---

## What is the difference between port and targetPort?

You can say:

> "`port` is the port exposed by the Service, while `targetPort` is the port on the target Pod where the application is running."

For example:

```text
Service:80
    ↓
Pod:8080
```

---

## What is NodePort?

You can say:

> "NodePort exposes a Service through a specific port on each Kubernetes node, allowing external clients to access the Service through the node's IP and that port."

---

## How does a Service communicate with Pods?

You can say:

> "A Service uses selectors to identify matching Pods. Kubernetes maintains the endpoints for those Pods, and traffic sent to the Service is routed to the available matching Pods."

---

## How does Service discovery work?

You can say:

> "Kubernetes provides DNS-based service discovery through CoreDNS. Instead of using a changing Pod IP, applications can use the Service name, such as `backend-service`, and Kubernetes DNS resolves it to the Service's network address."

---

# 49. What we should remember

Don't memorize all the networking details.

Remember this core idea:

```text
Pods can change
     ↓
Pod IPs can change
     ↓
We need a stable endpoint
     ↓
Kubernetes Service
     ↓
Service selects Pods
     ↓
Traffic reaches available Pods
```

For our project:

```text
Frontend
    |
    ↓
backend-service
    |
    +------> Backend Pod
    |
    +------> Backend Pod
```

And for PostgreSQL:

```text
Backend
    |
    ↓
postgres-service
    |
    ↓
PostgreSQL Pod
```

The most important distinction is:

```text
Deployment
→ manages Pods

ReplicaSet
→ maintains number of Pods

Service
→ provides stable networking to Pods

Ingress
→ routes external HTTP/HTTPS traffic to Services
```

---

# 50. Final mental model

The Kubernetes networking model we currently understand is:

```text
                         USER
                           |
                           ↓
                        INGRESS
                           |
                 +---------+---------+
                 |                   |
                /api                 /
                 |                   |
                 ↓                   ↓
          BACKEND SERVICE      FRONTEND SERVICE
                 |                   |
                 ↓                   ↓
          +------+------+       +----+----+
          |             |       |         |
          ↓             ↓       ↓         ↓
       Backend        Backend  Frontend  Frontend
         Pod            Pod      Pod       Pod
          |
          ↓
   POSTGRES SERVICE
          |
          ↓
    POSTGRES POD
```

The core idea is:

```text
                    SERVICE
                       |
                Stable endpoint
                       |
                       ↓
                Select matching Pods
                       |
             +---------+---------+
             |                   |
             ↓                   ↓
           Pod 1               Pod 2
```

And because Pods are temporary:

```text
Pod IP can change
       ↓
Service remains the stable endpoint
       ↓
Applications continue communicating
```

This is the fundamental reason Kubernetes Services exist.

```
```
