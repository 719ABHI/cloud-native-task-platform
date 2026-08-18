# 06 — Kubernetes DNS and CoreDNS

## 1. Why do we need DNS in Kubernetes?

We already learned that Pods have IP addresses.

For example:

```text
Backend Pod 1 → 10.244.0.20
Backend Pod 2 → 10.244.0.21
```

But Pod IP addresses are not permanent.

If a Pod is deleted and Kubernetes creates a replacement:

```text
Before:

Backend Pod → 10.244.0.20


After:

New Backend Pod → 10.244.0.35
```

The IP changed.

This creates a problem if another application directly uses the Pod IP.

For example:

```text
Frontend
   |
   ↓
10.244.0.20
```

If that Pod disappears, the frontend's destination is no longer valid.

We already solved the **stable endpoint** problem using a Service:

```text
Frontend
   |
   ↓
backend-service
   |
   ↓
Backend Pods
```

But now another question appears:

> How does the frontend know where `backend-service` is?

We don't want to manually remember the Service's ClusterIP either.

For example:

```text
backend-service → 10.111.127.205
```

Instead, Kubernetes provides **DNS-based service discovery**.

---

# 2. What is DNS?

Before understanding Kubernetes DNS, we should understand normal DNS.

DNS stands for:

**Domain Name System**

Its basic purpose is to translate names into IP addresses.

For example, instead of remembering an IP address, we use a name.

Conceptually:

```text
example.com
     |
     ↓
   DNS
     |
     ↓
IP address
```

So DNS provides a way to use names instead of manually remembering IP addresses.

---

# 3. Why is DNS useful in Kubernetes?

The same idea is useful inside a Kubernetes cluster.

Suppose our backend Service has:

```text
backend-service
ClusterIP: 10.111.127.205
```

A frontend Pod does not need to know:

```text
10.111.127.205
```

Instead, it can use:

```text
backend-service
```

Kubernetes DNS resolves the Service name to the Service's network address.

Conceptually:

```text
Frontend Pod
      |
      | backend-service
      ↓
 Kubernetes DNS
      |
      ↓
10.111.127.205
      |
      ↓
Backend Service
```

This is called **service discovery**.

---

# 4. What is service discovery?

Service discovery means:

> Finding another application or service by its name instead of manually knowing its network address.

In our project:

```text
Frontend
    |
    | "backend-service"
    ↓
Kubernetes DNS
    |
    ↓
Backend Service
```

The frontend does not need to know:

```text
Backend Pod 1 IP
Backend Pod 2 IP
```

or even manually remember the Service ClusterIP.

It can use the Service name.

---

# 5. Kubernetes provides DNS automatically

Kubernetes normally provides DNS-based service discovery inside the cluster.

When a Kubernetes cluster is created, a DNS service is installed for the cluster.

In modern Kubernetes installations, this is usually **CoreDNS**.

So we can think of:

```text
Kubernetes
     |
     ↓
CoreDNS
     |
     ↓
DNS-based service discovery
```

CoreDNS is responsible for answering DNS queries for Kubernetes resources such as Services.

---

# 6. What is CoreDNS?

CoreDNS is the DNS server used by Kubernetes to provide DNS-based service discovery.

Simple definition:

> CoreDNS is the DNS component that allows applications inside the Kubernetes cluster to resolve Kubernetes Service names into network addresses.

For our project, the important idea is:

```text
Pod asks:

"What is backend-service?"

        ↓

CoreDNS

        ↓

Returns the Service's DNS result
```

We don't need to learn CoreDNS's internal implementation yet.

For our level, remember:

```text
CoreDNS
    ↓
Kubernetes DNS
    ↓
Service name resolution
```

---

# 7. The complete idea

Let's combine what we already learned.

We have:

```text
Frontend Pod
      |
      ↓
backend-service
      |
      ↓
Backend Pods
```

But internally, the frontend needs to resolve:

```text
backend-service
```

Kubernetes DNS helps with this.

The simplified flow is:

```text
Frontend Pod
      |
      | DNS query:
      | "backend-service"
      ↓
    CoreDNS
      |
      ↓
Service IP
      |
      ↓
backend-service
      |
      ↓
Backend Pods
```

---

# 8. CoreDNS does NOT forward application traffic

This is a very important distinction.

CoreDNS's job is **name resolution**.

It does not take the application's HTTP request and forward it to the backend Pod.

For example:

```text
Frontend
   |
   | "What IP is backend-service?"
   ↓
CoreDNS
   |
   ↓
Service IP
```

After the frontend knows the Service address, the actual application request travels through Kubernetes networking toward the Service and its selected Pods.

So:

```text
CoreDNS
→ Resolves the name

Service
→ Provides the stable endpoint

Kubernetes networking
→ Routes the actual traffic
```

This distinction will become important when we study Kubernetes networking in `07-kubernetes-networking.md`.

---

# 9. Service name inside the cluster

Suppose we have:

```text
backend-service
```

inside the `default` namespace.

A Pod in the same namespace can generally use:

```text
backend-service
```

as the hostname.

For example:

```text
http://backend-service:8080
```

The application asks the cluster DNS system to resolve:

```text
backend-service
```

CoreDNS provides the DNS response.

---

# 10. Why don't we use the ClusterIP directly?

Our backend Service currently has:

```text
backend-service
ClusterIP: 10.111.127.205
```

We could technically communicate using:

```text
10.111.127.205:8080
```

But using the Service name is better.

Instead:

```text
backend-service:8080
```

Why?

Because the name is easier to understand.

Compare:

```text
http://10.111.127.205:8080
```

with:

```text
http://backend-service:8080
```

The second one immediately tells us what we are communicating with.

It also avoids putting Kubernetes-generated IP addresses directly into application configuration.

---

# 11. Service DNS name

Kubernetes provides DNS names for Services.

The full DNS name follows this general structure:

```text
<service-name>.<namespace>.svc.cluster.local
```

For our backend:

```text
backend-service.default.svc.cluster.local
```

Breaking it down:

```text
backend-service
       |
       ↓
Service name

default
       |
       ↓
Namespace

svc
       |
       ↓
Indicates a Kubernetes Service

cluster.local
       |
       ↓
Cluster DNS domain
```

So:

```text
backend-service.default.svc.cluster.local
```

is the fully qualified DNS name of our Service.

---

# 12. Why can we simply use `backend-service`?

We don't normally need to write the full DNS name.

Inside the same namespace, Kubernetes DNS allows us to use the shorter name:

```text
backend-service
```

For example:

```text
http://backend-service:8080
```

This is convenient for applications communicating inside the same namespace.

Our project resources are currently in:

```text
default
```

namespace.

Therefore:

```text
backend-service
```

is sufficient for our internal communication.

---

# 13. What is a namespace?

We need a basic understanding of namespaces because they appear in Kubernetes DNS names.

A namespace provides a logical separation inside a Kubernetes cluster.

For example, a cluster could have:

```text
development
production
testing
```

Each namespace can contain its own Kubernetes resources.

For example:

```text
development
    |
    ├── backend-service
    └── frontend-service


production
    |
    ├── backend-service
    └── frontend-service
```

Both namespaces could have a Service named:

```text
backend-service
```

The namespace helps distinguish them.

That is why the full DNS name contains:

```text
backend-service.default.svc.cluster.local
```

---

# 14. Same namespace communication

Suppose we have:

```text
Frontend Pod
namespace: default
```

and:

```text
backend-service
namespace: default
```

The frontend can use:

```text
backend-service
```

Conceptually:

```text
Frontend Pod
    |
    | backend-service
    ↓
CoreDNS
    |
    ↓
backend-service in default namespace
```

This is the simplest case.

---

# 15. Cross-namespace communication

Suppose the backend Service is in another namespace:

```text
backend-service
namespace: production
```

Then simply using:

```text
backend-service
```

from another namespace may not refer to that Service.

We can use a namespace-qualified name:

```text
backend-service.production
```

Or the full DNS name:

```text
backend-service.production.svc.cluster.local
```

The important idea is:

```text
Service name
+
Namespace
```

helps Kubernetes identify the correct Service.

We don't need cross-namespace networking for our current project, but understanding the concept is useful.

---

# 16. DNS resolution flow in our project

Let's imagine our frontend needs to call the backend.

The frontend application wants to send:

```text
GET /api/tasks
```

It uses:

```text
backend-service:8080
```

The simplified process is:

```text
Step 1

Frontend Pod
    |
    | "What is backend-service?"
    ↓
CoreDNS
```

Then:

```text
Step 2

CoreDNS
    |
    ↓
Service DNS information
```

Then:

```text
Step 3

Frontend
    |
    ↓
Backend Service
    |
    ↓
Backend Pod
```

The important point is that DNS helps the frontend **find the Service**.

It does not replace the Service.

---

# 17. Service DNS vs Pod DNS

We should distinguish these concepts.

Our application normally communicates with a Service:

```text
backend-service
```

rather than directly with a Pod.

Why?

Because Pods are temporary.

For example:

```text
Pod A → 10.244.0.20
Pod B → 10.244.0.21
```

can become:

```text
Pod C → 10.244.0.35
Pod B → 10.244.0.21
```

The Service remains the stable abstraction:

```text
backend-service
```

Therefore, normal application communication should generally be:

```text
Application
     ↓
Service name
     ↓
Service
     ↓
Pods
```

rather than:

```text
Application
     ↓
Pod IP
```

---

# 18. How does CoreDNS know about Services?

CoreDNS is integrated with Kubernetes.

It can obtain information about Kubernetes resources through the Kubernetes API.

Conceptually:

```text
Kubernetes API
      |
      ↓
   CoreDNS
      |
      ↓
DNS records for Kubernetes Services
```

Therefore, when Services are created or changed, CoreDNS can provide the appropriate DNS information.

We don't need to manually create DNS records for normal Kubernetes Services.

---

# 19. What happens when a Service is created?

Suppose we create:

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
```

Kubernetes creates the Service.

The Service gets a ClusterIP.

For example:

```text
backend-service
      |
      ↓
10.111.127.205
```

Kubernetes DNS makes that Service discoverable by name.

Therefore applications inside the cluster can use:

```text
backend-service
```

instead of:

```text
10.111.127.205
```

---

# 20. What happens when the backend Pod changes?

Suppose:

```text
backend-service
       |
       +----> Backend Pod A
       |
       +----> Backend Pod B
```

Pod A is deleted.

The Deployment creates Pod C:

```text
backend-service
       |
       +----> Backend Pod B
       |
       +----> Backend Pod C
```

The frontend still uses:

```text
backend-service
```

Nothing needs to change in the frontend configuration.

This works because:

```text
Service
    ↓
tracks appropriate Pods
```

while:

```text
CoreDNS
    ↓
provides stable DNS-based discovery of the Service
```

This is the combination that makes Service-based communication useful.

---

# 21. DNS and readiness

We already learned about readiness probes.

Suppose we have:

```text
Backend Pod 1 → Ready
Backend Pod 2 → Not Ready
```

The Service should only consider appropriate ready endpoints for normal traffic.

DNS does not decide whether a Pod is ready.

This is important.

```text
CoreDNS
→ Resolves the Service name

Service / EndpointSlice mechanism
→ Determines usable endpoints

Readiness
→ Helps determine whether a Pod should receive traffic
```

So DNS and readiness have different responsibilities.

---

# 22. What happens if DNS fails?

Suppose the frontend tries:

```text
backend-service
```

but the DNS system cannot resolve the name.

The frontend may not be able to determine where to send the request.

This could result in a DNS resolution error.

For example, conceptually:

```text
Frontend
    |
    | backend-service
    ↓
CoreDNS
    X
DNS resolution fails
```

The application cannot successfully discover the Service.

This is why Kubernetes DNS is an important cluster component.

---

# 23. How can we test Kubernetes DNS?

We can test DNS from inside a Pod.

For example, we can first check our Services:

```bash
kubectl get services
```

We should see:

```text
backend-service
postgres-service
frontend-service
...
```

Then we can run a temporary Pod that has DNS utilities installed.

For example:

```bash
kubectl run dns-test \
  --image=busybox:1.36 \
  --restart=Never \
  --rm -it \
  -- nslookup backend-service
```

This asks the DNS system:

```text
"What IP address belongs to backend-service?"
```

The response should contain the Service's ClusterIP.

For our project, that should correspond to:

```text
backend-service
→ 10.111.127.205
```

The exact IP can change if the Service itself is recreated, so we should always verify with:

```bash
kubectl get service backend-service
```

---

# 24. Testing the full DNS path

We can also test whether a Service name can actually be used from inside the cluster.

Conceptually:

```text
Temporary Pod
     |
     | backend-service
     ↓
CoreDNS
     |
     ↓
Backend Service
     |
     ↓
Backend Pod
```

A simple test can be:

```bash
kubectl run curl-test \
  --image=curlimages/curl:8.10.1 \
  --restart=Never \
  --rm -it \
  -- curl http://backend-service:8080/actuator/health
```

If the backend is healthy and the Service is configured correctly, we should receive the application's health response.

This tests more than DNS alone:

```text
DNS resolution
      +
Service connectivity
      +
Backend application
```

---

# 25. DNS troubleshooting

If:

```bash
curl http://backend-service:8080
```

doesn't work, we shouldn't immediately assume the application is broken.

We can troubleshoot layer by layer.

### Step 1 — Does the Service exist?

```bash
kubectl get service backend-service
```

### Step 2 — Does the Service have endpoints?

```bash
kubectl get endpoints backend-service
```

or:

```bash
kubectl get endpointslices
```

### Step 3 — Are the backend Pods running?

```bash
kubectl get pods -l app=backend
```

### Step 4 — Are the labels correct?

```bash
kubectl get pods -l app=backend --show-labels
```

### Step 5 — Can DNS resolve the Service?

```bash
kubectl run dns-test \
  --image=busybox:1.36 \
  --restart=Never \
  --rm -it \
  -- nslookup backend-service
```

This gives us a structured way to troubleshoot.

---

# 26. CoreDNS Pods

CoreDNS itself runs as Pods inside the Kubernetes cluster.

We can inspect them using:

```bash
kubectl get pods -n kube-system
```

In a typical Kubernetes cluster, we may see Pods with names similar to:

```text
coredns-xxxxxxxxx-xxxxx
```

The exact names depend on the cluster.

In Minikube, CoreDNS is also part of the cluster's system components.

We don't normally manage CoreDNS manually for our application.

---

# 27. Why do we not create DNS records manually?

Without Kubernetes DNS, we might imagine having to manually configure:

```text
backend-service → 10.111.127.205
postgres-service → 10.99.144.206
frontend-service → 10.104.148.221
```

That would be inconvenient.

Kubernetes provides service discovery automatically.

So when we create Services, applications can discover them through Kubernetes DNS.

This is one of the conveniences Kubernetes provides.

---

# 28. DNS name vs IP address

Let's compare the two approaches.

### Using an IP

```text
http://10.111.127.205:8080
```

Problems:

* Harder to understand
* Application configuration contains infrastructure-specific IPs
* Less convenient when working across environments

### Using a Service name

```text
http://backend-service:8080
```

Advantages:

* Human-readable
* Stable abstraction
* Kubernetes handles discovery
* Applications don't need to know the Service IP

Therefore, inside Kubernetes, Service names are usually preferred for service-to-service communication.

---

# 29. Our project example

Our current Kubernetes Services include:

```text
backend-service
postgres-service
frontend-service
nginx-service
```

For example:

```text
Backend
    |
    | postgres-service:5432
    ↓
PostgreSQL
```

And:

```text
Frontend
    |
    | backend-service:8080
    ↓
Backend
```

The applications can use the Service names instead of hard-coded Pod IP addresses.

---

# 30. Important distinction: DNS does not mean HTTP routing

This is another common beginner confusion.

Suppose the frontend sends:

```text
http://backend-service:8080/api/tasks
```

There are multiple things happening.

### DNS

Resolves:

```text
backend-service
```

to the Service's network address.

### Service networking

Uses the Service to reach an appropriate backend endpoint.

### HTTP

The application sends:

```text
GET /api/tasks
```

to the backend.

So:

```text
DNS
→ Where is the Service?

Service
→ Which backend endpoint should receive traffic?

HTTP
→ What application request are we sending?
```

These are different layers of the process.

---

# 31. DNS and Ingress are also different

We already have an Ingress:

```text
application-ingress
```

It handles external HTTP routing.

For example:

```text
User
  |
  ↓
Ingress
  |
  +---- /api ----> backend-service
  |
  +---- / -------> frontend-service
```

DNS has a different role.

For internal service communication:

```text
Frontend Pod
     |
     ↓
backend-service
     |
     ↓
CoreDNS
     |
     ↓
Service address
```

So:

```text
CoreDNS
→ Service name resolution

Ingress
→ External HTTP/HTTPS routing
```

We will study Ingress properly later in:

```text
10-ingress.md
```

---

# 32. DNS and Kubernetes networking

DNS is also not the entire Kubernetes networking system.

Think of the process as:

```text
Application
     |
     | "backend-service"
     ↓
CoreDNS
     |
     | resolves name
     ↓
Service
     |
     | routes traffic
     ↓
Backend Pod
```

Therefore:

```text
DNS
→ Name resolution

Networking
→ Packet connectivity/routing

Service
→ Stable endpoint and Pod selection
```

We will study the networking part in:

```text
07-kubernetes-networking.md
```

---

# 33. What we actually learned through our project

Our project already contains:

```text
backend-service
postgres-service
frontend-service
nginx-service
```

We learned that applications should communicate through Service names rather than directly using temporary Pod IP addresses.

For example:

```text
backend-service
```

can represent our backend even though the backend Pods may change.

We also learned that:

```text
backend-service.default.svc.cluster.local
```

is the full DNS name for our backend Service in the `default` namespace.

This gives us a stable service-discovery mechanism inside the cluster.

---

# 34. Important beginner mistakes

### Mistake 1 — Using Pod IPs everywhere

Avoid:

```text
10.244.0.20
```

for normal application-to-application communication.

Prefer:

```text
backend-service
```

---

### Mistake 2 — Thinking CoreDNS forwards traffic

CoreDNS does not forward our HTTP request.

It resolves names.

```text
CoreDNS
→ DNS resolution
```

---

### Mistake 3 — Thinking DNS replaces Services

DNS helps applications find the Service.

It doesn't replace the Service.

```text
DNS
 ↓
Service
 ↓
Pods
```

---

### Mistake 4 — Thinking Service DNS points directly to one Pod

A Service represents a group of appropriate endpoints.

For example:

```text
backend-service
      |
      +---- Backend Pod 1
      |
      +---- Backend Pod 2
```

It is not simply:

```text
backend-service → one permanent Pod
```

---

### Mistake 5 — Confusing Ingress with DNS

Ingress routes external HTTP/HTTPS requests.

CoreDNS provides DNS-based name resolution.

They solve different problems.

---

# 35. Simple interview explanation

If an interviewer asks:

## What is Kubernetes DNS?

You can say:

> "Kubernetes provides DNS-based service discovery so that applications inside the cluster can communicate with Services using names instead of hard-coded IP addresses."

---

## What is CoreDNS?

You can say:

> "CoreDNS is the DNS component commonly used by Kubernetes to resolve Kubernetes Service names into their corresponding network addresses."

---

## Why do we need CoreDNS?

You can say:

> "Pods and their IP addresses can change, so instead of hard-coding IP addresses, applications can use stable Service names. CoreDNS resolves those names inside the Kubernetes cluster."

---

## How does a Pod communicate with another Service?

You can say:

> "The Pod uses the Service name, such as `backend-service`. Kubernetes DNS, through CoreDNS, resolves that name to the Service's address. The request then goes through the Service networking layer to an appropriate backend Pod."

---

## What is the full DNS name of a Kubernetes Service?

You can say:

> "The general format is `<service-name>.<namespace>.svc.cluster.local`. For example, our backend Service in the default namespace can be addressed as `backend-service.default.svc.cluster.local`."

---

## Does CoreDNS route traffic to Pods?

You can say:

> "No. CoreDNS is responsible for DNS name resolution. The actual application traffic is handled by the Kubernetes Service and networking layer."

---

# 36. The mental model we should remember

Don't memorize the implementation details.

Remember this:

```text
Pod IPs can change
       |
       ↓
We don't want to hard-code them
       |
       ↓
Create a Service
       |
       ↓
Service has a stable identity
       |
       ↓
Kubernetes DNS provides name-based discovery
       |
       ↓
CoreDNS resolves the Service name
       |
       ↓
Service networking sends traffic
       |
       ↓
Selected Backend Pods
```

For our project:

```text
Frontend Pod
      |
      | backend-service:8080
      ↓
    CoreDNS
      |
      ↓
Backend Service
      |
      +------------+
      |            |
      ↓            ↓
Backend Pod 1  Backend Pod 2
```

And for PostgreSQL:

```text
Backend Pod
      |
      | postgres-service:5432
      ↓
    CoreDNS
      |
      ↓
PostgreSQL Service
      |
      ↓
PostgreSQL Pod
```

---

# 37. What we should understand, not memorize

The important chain is:

```text
Pod
 ↓
needs to communicate with another application
 ↓
uses a Service name
 ↓
CoreDNS resolves the Service name
 ↓
Service provides stable endpoint
 ↓
Service networking reaches selected Pods
```

Remember the responsibilities:

```text
Pod
→ Runs the application


Service
→ Provides stable access to Pods


CoreDNS
→ Resolves Service names


Kubernetes networking
→ Provides the actual network connectivity


Ingress
→ Handles external HTTP/HTTPS routing
```

---

# 38. How this connects to the next concept

We now understand:

```text
Frontend
   |
   | backend-service
   ↓
CoreDNS
   |
   ↓
Service
   |
   ↓
Backend Pods
```

But there is still an important question:

> How does the actual network traffic move from one Pod to another?

For example:

```text
Frontend Pod
      |
      ↓
Service IP
      |
      ↓
Backend Pod IP
```

What actually makes this packet movement possible?

What is the role of:

* Pod networking?
* Cluster networking?
* Service networking?
* kube-proxy?
* CNI?
* Pod-to-Pod communication?
* Node-to-Node communication?

These are the concepts we will study next in:

```text
07-kubernetes-networking.md
```

---

# 39. Final summary

The main problem was:

```text
Pod IPs can change
```

We solved stable access using:

```text
Service
```

But applications still need a convenient way to find the Service.

Kubernetes provides:

```text
DNS-based service discovery
```

and CoreDNS provides the DNS functionality.

The complete mental model is:

```text
                     Kubernetes Cluster

Frontend Pod
     |
     | "backend-service"
     ↓
   CoreDNS
     |
     | resolves Service name
     ↓
Backend Service
     |
     | selects appropriate endpoints
     ↓
+------------+------------+
|                         |
↓                         ↓
Backend Pod 1          Backend Pod 2
```

The most important distinction is:

```text
CoreDNS
    ↓
"Where is backend-service?"

Service
    ↓
"Which backend Pods should receive traffic?"

Kubernetes networking
    ↓
"How does the traffic actually reach them?"
```

And our overall application communication is:

```text
Frontend
    |
    ↓
backend-service
    |
    ↓
Backend Pods
    |
    ↓
postgres-service
    |
    ↓
PostgreSQL Pod
```

So the progression is now:

```text
03 Pods
      ↓
04 Deployments & ReplicaSets
      ↓
05 Services
      ↓
06 DNS & CoreDNS
      ↓
07 Kubernetes Networking
```

That gives us a clean foundation before moving into the actual networking mechanisms.
