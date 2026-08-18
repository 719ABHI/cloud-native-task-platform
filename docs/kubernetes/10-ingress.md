# 10 — Kubernetes Ingress

## 1. Why do we need Ingress?

We already learned that a Kubernetes **Service** provides stable networking to Pods.

For example:

```text
Frontend
   |
   ↓
backend-service
   |
   ↓
Backend Pods
```

That works for communication **inside the cluster**.

But now think about a user accessing our application from outside the Kubernetes cluster.

For example:

```text
User Browser
     |
     ↓
?????
     |
     ↓
Frontend / Backend
```

We need a way to accept external HTTP/HTTPS requests and decide where those requests should go.

For example:

```text
/api/users
      ↓
Backend

/
      ↓
Frontend
```

This is where **Ingress** comes in.

---

# 2. What is Ingress?

Simple definition:

> **Ingress is a Kubernetes API object that defines rules for routing external HTTP/HTTPS traffic to Services inside the cluster.**

The important word is:

```text
Routing
```

Ingress can inspect an incoming request and decide which Service should receive it.

For example:

```text
User
  |
  ↓
Ingress
  |
  +------ /api ------> Backend Service
  |
  +------ / ---------> Frontend Service
```

So:

```text
Ingress
→ External HTTP/HTTPS routing

Service
→ Stable access to Pods
```

---

# 3. Why was Ingress introduced?

Without Ingress, we could expose applications using Services such as NodePort or LoadBalancer.

For example:

```text
User
 |
 +----> Backend NodePort
 |
 +----> Frontend NodePort
 |
 +----> Another NodePort
```

This can become difficult to manage.

Imagine we have:

```text
Frontend
Backend
Admin
Payments
Notifications
```

We might end up exposing many ports:

```text
30001 → Frontend
30002 → Backend
30003 → Admin
30004 → Payments
30005 → Notifications
```

Instead, we can have one external HTTP entry point:

```text
                    Ingress
                       |
        +--------------+--------------+
        |              |              |
       /              /api          /admin
        |              |              |
        ↓              ↓              ↓
    Frontend        Backend          Admin
    Service         Service         Service
```

This makes HTTP routing much cleaner.

---

# 4. Ingress does not directly manage Pods

This is important.

Ingress normally routes traffic to **Services**, not directly to Pods.

The architecture is:

```text
User
  |
  ↓
Ingress
  |
  ↓
Service
  |
  ↓
Pods
```

For our project:

```text
User
  |
  ↓
application-ingress
  |
  +------------------+
  |                  |
  ↓                  ↓
Frontend Service   Backend Service
  |                  |
  ↓                  ↓
Frontend Pods      Backend Pods
```

So the responsibilities are different:

```text
Ingress
→ Routes external HTTP/HTTPS traffic

Service
→ Provides stable networking to Pods

Pod
→ Runs the application
```

---

# 5. Ingress is an API object, not the actual proxy

This is an important beginner distinction.

When we create:

```yaml
kind: Ingress
```

we are defining routing rules.

But Kubernetes needs an actual component that receives the HTTP request and implements those rules.

That component is provided by an:

> **Ingress Controller**

So:

```text
Ingress
→ Defines what should happen

Ingress Controller
→ Actually implements the routing
```

This distinction is very important.

---

# 6. Ingress Controller

An Ingress Controller watches Kubernetes Ingress resources and configures a reverse proxy/load-balancing system accordingly.

In our project, we are using the:

```text
NGINX Ingress Controller
```

Conceptually:

```text
                    User
                      |
                      ↓
             NGINX Ingress Controller
                      |
                      ↓
              Ingress rules
                      |
             +--------+--------+
             |                 |
             ↓                 ↓
      Frontend Service   Backend Service
             |                 |
             ↓                 ↓
      Frontend Pods      Backend Pods
```

---

# 7. Our Ingress

We have an Ingress named:

```text
application-ingress
```

We verified it using:

```bash
kubectl get ingress
```

and saw:

```text
NAME                  CLASS   HOSTS   ADDRESS        PORTS
application-ingress   nginx   *       192.168.49.2   80
```

This tells us that our application has an Ingress using the `nginx` class.

---

# 8. Our project architecture

Our current architecture is:

```text
                         User
                          |
                          ↓
                  NGINX Ingress
                          |
                 application-ingress
                          |
             +------------+------------+
             |                         |
            /api                       /
             |                         |
             ↓                         ↓
      Backend Service           Frontend Service
             |                         |
             ↓                         ↓
       Backend Pods              Frontend Pods
             |
             ↓
     PostgreSQL Service
             |
             ↓
       PostgreSQL Pod
```

This is one of the most important diagrams to understand for our project.

---

# 9. Path-based routing

One of the most common things Ingress does is **path-based routing**.

For example:

```text
/api
```

can be routed to the backend.

And:

```text
/
```

can be routed to the frontend.

Conceptually:

```text
                 Ingress
                    |
        +-----------+-----------+
        |                       |
      /api                       /
        |                       |
        ↓                       ↓
Backend Service          Frontend Service
```

So when a user sends:

```text
GET /api/tasks
```

the Ingress can route it to:

```text
backend-service
```

While:

```text
GET /
```

can go to:

```text
frontend-service
```

---

# 10. Example Ingress YAML

A simplified version of our Ingress can look like:

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress

metadata:
  name: application-ingress

spec:
  ingressClassName: nginx

  rules:
    - http:
        paths:

          - path: /api
            pathType: Prefix
            backend:
              service:
                name: backend-service
                port:
                  number: 8080

          - path: /
            pathType: Prefix
            backend:
              service:
                name: frontend-service
                port:
                  number: 80
```

We don't need to memorize the entire YAML.

We need to understand what it means.

---

# 11. `ingressClassName`

Example:

```yaml
ingressClassName: nginx
```

This tells Kubernetes which Ingress Controller should handle this Ingress.

In our project:

```text
nginx
```

means the NGINX Ingress Controller handles it.

Conceptually:

```text
Ingress
   |
   ↓
ingressClassName: nginx
   |
   ↓
NGINX Ingress Controller
```

---

# 12. `rules`

Ingress rules describe how incoming requests should be routed.

For example:

```yaml
rules:
  - http:
      paths:
```

Inside the rules we define paths such as:

```text
/api
/
```

Each path can point to a different Service.

---

# 13. `path`

Example:

```yaml
path: /api
```

This means requests matching `/api` can be routed to the backend.

For example:

```text
/api/users
/api/tasks
/api/products
```

can match the `/api` path when using:

```yaml
pathType: Prefix
```

---

# 14. `pathType: Prefix`

Example:

```yaml
pathType: Prefix
```

means the path is matched based on its prefix.

For example:

```text
/api
```

can match:

```text
/api
/api/users
/api/tasks
/api/users/12
```

Conceptually:

```text
/api
 |
 +-- /api/users
 |
 +-- /api/tasks
 |
 +-- /api/products
```

They all begin with:

```text
/api
```

---

# 15. Why do we use Prefix?

Our backend has multiple API endpoints.

For example:

```text
/api/auth/register
/api/auth/login
/api/tasks
/api/tasks/1
```

We don't want to create a separate Ingress rule for every endpoint.

Instead, we can use:

```text
/api
```

with:

```text
pathType: Prefix
```

Then all those API requests can be routed to the backend Service.

---

# 16. Ingress backend

An Ingress path points to a Service.

Example:

```yaml
backend:
  service:
    name: backend-service
    port:
      number: 8080
```

This means:

```text
Request matching /api
       ↓
backend-service
       ↓
port 8080
       ↓
Backend Pods
```

Notice that the Ingress does **not** directly specify:

```text
backend Pod IP
```

It uses the Service.

---

# 17. Why does Ingress use Services?

Pods are temporary.

Their IP addresses can change.

Services provide stable networking.

Therefore:

```text
Ingress
   ↓
Service
   ↓
Pods
```

is a much better architecture than:

```text
Ingress
   ↓
Pod IP
```

The Service acts as the stable layer between the Ingress and Pods.

---

# 18. Complete request flow

Let's follow a real request from the user.

Suppose the user sends:

```text
POST /api/auth/register
```

The request flow is approximately:

```text
User Browser
      |
      ↓
Minikube IP
      |
      ↓
NGINX Ingress Controller
      |
      ↓
Ingress rule
      |
      | /api
      ↓
backend-service:8080
      |
      ↓
One of the ready Backend Pods
      |
      ↓
Spring Boot
      |
      ↓
/api/auth/register
```

This connects several concepts we have already learned.

---

# 19. What happens for the frontend?

Suppose the user requests:

```text
GET /
```

The flow is:

```text
User Browser
      |
      ↓
Minikube IP
      |
      ↓
NGINX Ingress Controller
      |
      ↓
Ingress rule
      |
      | /
      ↓
frontend-service:80
      |
      ↓
Frontend Pod
      |
      ↓
Nginx serving frontend files
```

So the same external entry point can route requests to different Services.

---

# 20. Ingress vs Service

This distinction should be very clear.

### Service

A Service provides stable networking for Pods.

```text
Service
   ↓
Pods
```

### Ingress

Ingress provides HTTP/HTTPS routing from outside the cluster to Services.

```text
Ingress
   ↓
Services
   ↓
Pods
```

So:

```text
Ingress
→ Which Service should receive this HTTP request?

Service
→ Which Pods should receive this traffic?
```

---

# 21. Ingress vs NodePort

We also have NodePort Services in our project.

For example:

```text
backend-nodeport
```

NodePort exposes a Service through a port on the node.

For example:

```text
NodeIP:30081
      ↓
backend-nodeport
      ↓
Backend Pods
```

Ingress works differently:

```text
NodeIP:80
      ↓
Ingress
      ↓
Path-based routing
      ↓
Service
      ↓
Pods
```

Ingress is particularly useful when we have multiple HTTP applications.

---

# 22. Why not expose everything with NodePort?

Suppose we have:

```text
Frontend
Backend
Admin
Payments
Notifications
```

With NodePort we could have:

```text
30001 → Frontend
30002 → Backend
30003 → Admin
30004 → Payments
30005 → Notifications
```

With Ingress we can instead have:

```text
Application IP
      |
      ↓
Ingress
      |
      +---- /       → Frontend
      |
      +---- /api    → Backend
      |
      +---- /admin  → Admin
      |
      +---- /pay    → Payments
```

This is cleaner for HTTP/HTTPS applications.

---

# 23. Ingress and domain names

Ingress can also route using hostnames.

For example:

```text
example.com
api.example.com
admin.example.com
```

Conceptually:

```text
                 Ingress
                    |
       +------------+------------+
       |            |            |
       ↓            ↓            ↓
 example.com   api.example.com  admin.example.com
       |            |            |
       ↓            ↓            ↓
  Frontend      Backend        Admin
```

This is called **host-based routing**.

We currently use path-based routing in our project.

---

# 24. Path-based vs Host-based routing

### Path-based routing

```text
example.com/
      ↓
Frontend

example.com/api
      ↓
Backend
```

### Host-based routing

```text
example.com
      ↓
Frontend

api.example.com
      ↓
Backend
```

Both are common Ingress patterns.

---

# 25. HTTPS and TLS

Ingress can also be used for HTTPS.

Instead of:

```text
HTTP
```

we can configure:

```text
HTTPS
```

with TLS certificates.

Conceptually:

```text
User
  |
  | HTTPS
  ↓
Ingress
  |
  ↓
Service
  |
  ↓
Pods
```

The Ingress can terminate TLS at the edge.

We don't need to configure production TLS in our current Minikube project yet, but it is important to know that Ingress commonly handles HTTPS.

---

# 26. What is an Ingress Controller?

This is worth repeating because beginners often confuse these two.

### Ingress

A Kubernetes resource containing routing rules.

Example:

```yaml
kind: Ingress
```

It says:

```text
/api → backend-service
/    → frontend-service
```

### Ingress Controller

The actual software that watches those rules and handles traffic.

Our controller is:

```text
NGINX Ingress Controller
```

So:

```text
Ingress
→ Configuration / rules

Ingress Controller
→ Actual traffic handling
```

---

# 27. Why do we need an Ingress Controller?

Creating an Ingress object alone doesn't magically route traffic.

Something must read the rules and implement them.

That is the job of the Ingress Controller.

Conceptually:

```text
Ingress YAML
     |
     ↓
Kubernetes API Server
     |
     ↓
Ingress Controller watches it
     |
     ↓
Controller configures proxy/routing
     |
     ↓
Incoming traffic is routed
```

Without a suitable Ingress Controller, our Ingress rules would not provide the expected traffic routing.

---

# 28. NGINX Ingress Controller in our project

Our Ingress shows:

```text
CLASS: nginx
```

This indicates that the NGINX Ingress Controller is associated with our Ingress.

The high-level flow is:

```text
User
  |
  ↓
NGINX Ingress Controller
  |
  ↓
application-ingress rules
  |
  +------------+------------+
  |                         |
  ↓                         ↓
backend-service       frontend-service
```

---

# 29. Ingress and Services work together

This is the key relationship:

```text
                    INGRESS
                       |
              "Where should this
               HTTP request go?"
                       |
              +--------+--------+
              |                 |
              ↓                 ↓
       Backend Service    Frontend Service
              |                 |
       "Which Pods?"       "Which Pods?"
              |                 |
              ↓                 ↓
        Backend Pods       Frontend Pods
```

Ingress and Service solve different networking problems.

---

# 30. What happens when a backend Pod changes?

Suppose:

```text
Backend Pod 1
Backend Pod 2
```

are behind:

```text
backend-service
```

and Pod 1 is replaced.

The Service updates its available endpoints.

The Ingress still points to:

```text
backend-service
```

Nothing about the Ingress needs to change.

So:

```text
Pod changes
   ↓
Service updates endpoints
   ↓
Ingress continues pointing to Service
```

This is another example of Kubernetes abstraction.

---

# 31. Ingress does not replace a Service

A common beginner misunderstanding is:

> "If I have Ingress, do I still need Services?"

Yes.

Normally:

```text
Ingress
   ↓
Service
   ↓
Pods
```

Ingress and Service have different responsibilities.

Ingress provides routing.

Service provides stable access to the Pods.

---

# 32. Our current external architecture

Our project currently looks like:

```text
                         USER
                           |
                           ↓
                  Minikube IP / Host
                           |
                           ↓
             NGINX Ingress Controller
                           |
                           ↓
                 application-ingress
                           |
                +----------+----------+
                |                     |
              /api                    /
                |                     |
                ↓                     ↓
        backend-service        frontend-service
                |                     |
          +-----+-----+         +-----+-----+
          |           |         |           |
          ↓           ↓         ↓           ↓
       Backend     Backend   Frontend    Frontend
        Pod          Pod       Pod         Pod
          |
          ↓
   postgres-service
          |
          ↓
    PostgreSQL Pod
```

This is the architecture we should understand for our current project.

---

# 33. How we tested Ingress

We tested our backend through the Minikube IP.

We used:

```bash
curl -i -X POST http://192.168.49.2/api/auth/register \
-H "Content-Type: application/json" \
-d '{"username":"testUser","email":"testUser@example.com","password":"User@123"}'
```

The request returned:

```text
HTTP/1.1 200
```

and the backend returned:

```json
{
  "createdAt": "...",
  "email": "testUser@example.com",
  "id": 12,
  "role": "USER",
  "username": "testUser"
}
```

This was important because the request did not directly target a backend Pod IP.

It went through our Kubernetes networking architecture.

Conceptually:

```text
curl
  |
  ↓
192.168.49.2
  |
  ↓
Ingress
  |
  ↓
backend-service
  |
  ↓
Backend Pod
  |
  ↓
Spring Boot
```

---

# 34. Why this test was important

This single test verified multiple parts of our system:

```text
Ingress
   ↓
Service
   ↓
Backend Pods
   ↓
Spring Boot
   ↓
PostgreSQL if required
```

Because registration successfully returned:

```text
HTTP 200
```

we know the external request successfully reached the backend application.

---

# 35. Ingress and readiness

Ingress sends traffic toward Services.

Services should normally use Pods that are ready.

Therefore the overall flow is:

```text
External Request
      ↓
Ingress
      ↓
Backend Service
      ↓
Ready Backend Pod
      ↓
Application
```

If a backend Pod fails its readiness probe:

```text
Backend Pod
     ↓
Not Ready
     ↓
Service does not normally send traffic to it
```

This connects:

```text
Ingress
+
Service
+
Readiness Probe
```

into one complete traffic flow.

---

# 36. Ingress and liveness

Liveness has a different role.

If the backend container becomes unhealthy:

```text
Liveness fails
      ↓
Container restarted
      ↓
Application starts again
      ↓
Readiness succeeds
      ↓
Service can use the Pod again
```

So:

```text
Liveness
→ Helps recover the application

Readiness
→ Controls whether traffic should reach it

Service
→ Provides stable networking

Ingress
→ Routes external HTTP/HTTPS requests
```

---

# 37. Common beginner mistakes

## Mistake 1 — Thinking Ingress is a Service

They are different.

```text
Ingress
→ HTTP/HTTPS routing

Service
→ Stable Pod networking
```

---

## Mistake 2 — Thinking Ingress directly sends traffic to Pods

Normally:

```text
Ingress
   ↓
Service
   ↓
Pods
```

---

## Mistake 3 — Thinking an Ingress object alone handles traffic

We need an Ingress Controller.

```text
Ingress
→ Rules

Ingress Controller
→ Implements rules
```

---

## Mistake 4 — Thinking Ingress is required for internal Pod communication

It isn't.

For example:

```text
Backend Pod
   ↓
postgres-service
   ↓
PostgreSQL Pod
```

This is internal Service-based communication.

Ingress is mainly for external HTTP/HTTPS routing.

---

## Mistake 5 — Thinking NodePort and Ingress are the same

They are not.

```text
NodePort
→ Exposes Service through a node port

Ingress
→ HTTP/HTTPS routing to Services
```

---

# 38. Useful commands

### View Ingress

```bash
kubectl get ingress
```

---

### Get detailed Ingress information

```bash
kubectl describe ingress application-ingress
```

---

### View Ingress YAML

```bash
kubectl get ingress application-ingress -o yaml
```

---

### View Services

```bash
kubectl get services
```

---

### View Pods

```bash
kubectl get pods
```

---

### Test the application through the Ingress

For our Minikube setup:

```bash
curl -i http://192.168.49.2/api/...
```

For example, our registration test:

```bash
curl -i -X POST http://192.168.49.2/api/auth/register \
-H "Content-Type: application/json" \
-d '{"username":"testUser","email":"testUser@example.com","password":"User@123"}'
```

---

# 39. How to debug an Ingress problem

If the application doesn't work through Ingress, don't immediately assume Ingress itself is broken.

Check the layers one by one.

### Step 1 — Check Pods

```bash
kubectl get pods
```

Are the application Pods:

```text
1/1 Running
```

?

---

### Step 2 — Check Services

```bash
kubectl get services
```

Does the Service exist?

---

### Step 3 — Check Service selectors

```bash
kubectl describe service backend-service
```

Make sure the selector matches the Pod labels.

---

### Step 4 — Check endpoints

Check whether the Service has usable endpoints.

For example:

```bash
kubectl get endpoints backend-service
```

or, on modern Kubernetes:

```bash
kubectl get endpointslice
```

If there are no endpoints, the Service isn't finding usable Pods.

---

### Step 5 — Check Ingress

```bash
kubectl describe ingress application-ingress
```

Check:

```text
Rules
Backends
Events
```

---

### Step 6 — Test the external route

```bash
curl -i http://192.168.49.2/api/...
```

This helps identify which layer is failing.

---

# 40. Ingress request flow — complete mental model

Remember this:

```text
                 EXTERNAL USER
                       |
                       ↓
                 HTTP / HTTPS
                       |
                       ↓
             Ingress Controller
                       |
                       ↓
                Ingress Rules
                       |
             +---------+---------+
             |                   |
           /api                  /
             |                   |
             ↓                   ↓
      Backend Service      Frontend Service
             |                   |
             ↓                   ↓
      Ready Backend Pods   Ready Frontend Pods
             |
             ↓
      PostgreSQL Service
             |
             ↓
       PostgreSQL Pod
```

Each layer has a different job.

---

# 41. Responsibility of each component

### Ingress

```text
"What should happen to this external HTTP request?"
```

### Ingress Controller

```text
"Actually implement the routing."
```

### Service

```text
"Which application Pods should receive this traffic?"
```

### Readiness Probe

```text
"Is this Pod currently ready to receive traffic?"
```

### Pod

```text
"Run the application."
```

This is a very useful mental model.

---

# 42. Interview explanations

## What is Ingress?

> "Ingress is a Kubernetes resource used to define rules for routing external HTTP or HTTPS traffic to Services inside the cluster."

---

## Why do we need Ingress?

> "Ingress provides a centralized way to route external HTTP or HTTPS traffic to different Services. Instead of exposing every application separately through different NodePorts, we can route requests based on paths or hostnames."

---

## What is an Ingress Controller?

> "An Ingress Controller is the component that actually implements the routing rules defined by Ingress resources. In our project, we use the NGINX Ingress Controller."

---

## What is the difference between Ingress and Service?

> "A Service provides stable networking to Pods, while Ingress routes external HTTP or HTTPS requests to Services."

---

## Does Ingress route directly to Pods?

> "Normally, Ingress routes traffic to a Service, and the Service then routes it to the appropriate Pods."

---

## What is path-based routing?

> "Path-based routing means routing requests to different Services based on the URL path. For example, `/api` can route to the backend Service while `/` routes to the frontend Service."

---

## What is host-based routing?

> "Host-based routing routes traffic based on the hostname. For example, `example.com` can route to the frontend while `api.example.com` routes to the backend."

---

## Why do we need an Ingress Controller?

> "The Ingress resource only defines routing rules. The Ingress Controller watches those rules and actually handles the incoming traffic."

---

# 43. What we should understand instead of memorizing

Don't memorize the entire Ingress YAML.

Understand this:

```text
External User
      ↓
Ingress Controller
      ↓
Ingress rules
      ↓
Choose Service
      ↓
Service
      ↓
Choose ready Pod
      ↓
Application
```

For our project:

```text
/api
 ↓
backend-service
 ↓
Backend Pods


/
 ↓
frontend-service
 ↓
Frontend Pods
```

The key distinction is:

```text
Ingress
→ External HTTP/HTTPS routing

Service
→ Stable networking to Pods
```

---

# 44. Final mental model

Think of Ingress as the **front door** of our Kubernetes application.

```text
                       INTERNET / USER
                              |
                              ↓
                       INGRESS CONTROLLER
                              |
                              ↓
                         INGRESS RULES
                              |
                +-------------+-------------+
                |                           |
              /api                          /
                |                           |
                ↓                           ↓
        BACKEND SERVICE              FRONTEND SERVICE
                |                           |
          +-----+-----+                 +---+---+
          |           |                 |       |
          ↓           ↓                 ↓       ↓
       Backend      Backend          Frontend Frontend
         Pod          Pod              Pod       Pod
```

The Services then provide stable access to the Pods.

---

# 45. Summary

The important concepts we learned are:

```text
Ingress
→ Kubernetes resource that defines external HTTP/HTTPS routing rules


Ingress Controller
→ Component that actually implements those routing rules


NGINX Ingress Controller
→ The controller used in our project


Path-based routing
→ Route requests based on URL paths


Host-based routing
→ Route requests based on hostnames


ingressClassName
→ Identifies which Ingress Controller should handle the Ingress


Service
→ Stable networking layer between Ingress and Pods


NodePort
→ Exposes a Service through a port on the Kubernetes node
```

Our project's main external flow is:

```text
User
 ↓
192.168.49.2
 ↓
NGINX Ingress Controller
 ↓
application-ingress
 ↓
+-------------------+
|                   |
/api                /
|                   |
↓                   ↓
backend-service   frontend-service
|                   |
↓                   ↓
Backend Pods      Frontend Pods
```

And we successfully tested:

```text
curl
 ↓
192.168.49.2/api/auth/register
 ↓
Ingress
 ↓
backend-service
 ↓
Backend Pod
 ↓
Spring Boot
 ↓
HTTP 200
```

The most important thing to remember is:

> **Ingress decides where an external HTTP/HTTPS request should go, Service provides stable access to the application Pods, and the Pods actually run the application.**
