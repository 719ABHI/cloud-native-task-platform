# 07 — Kubernetes Networking

## 1. Why do we need Kubernetes networking?

We already learned how our Kubernetes application is structured:

```text
                    Kubernetes Cluster
                           |
            +--------------+--------------+
            |                             |
        Frontend                       Backend
          Pods                           Pods
                                            |
                                            ↓
                                    PostgreSQL Pod
```

We also learned about Services and DNS.

For example:

```text
Frontend Pod
     |
     ↓
backend-service
     |
     ↓
Backend Pods
```

And:

```text
Backend Pod
     |
     ↓
postgres-service
     |
     ↓
PostgreSQL Pod
```

But now we need to understand an important question:

> **How does the actual network traffic move between these Pods and Services?**

For example:

```text
Frontend Pod
      |
      ↓
backend-service
      |
      ↓
Backend Pod
```

Something has to make that communication possible.

That is where **Kubernetes networking** comes in.

---

# 2. The basic Kubernetes networking idea

At a beginner level, Kubernetes networking follows a few important principles.

The most important one is:

> **Each Pod gets its own IP address, and Pods can communicate with other Pods over the cluster network.**

For example:

```text
Frontend Pod
IP: 10.244.0.10

Backend Pod
IP: 10.244.0.20
```

The frontend should be able to communicate with:

```text
10.244.0.20
```

without requiring us to manually create a connection between them.

Conceptually:

```text
Frontend Pod
10.244.0.10
      |
      | network
      ↓
Backend Pod
10.244.0.20
```

---

# 3. Why does every Pod need an IP?

A Pod is a network participant inside the Kubernetes cluster.

Giving each Pod an IP allows it to communicate over the cluster network.

For example:

```text
Backend Pod 1 → 10.244.0.20
Backend Pod 2 → 10.244.0.21
Frontend Pod  → 10.244.0.22
```

Now the Pods have network identities.

Conceptually:

```text
Pod
 |
 └── Pod IP
```

This is different from the IP of the container itself.

For our beginner understanding, think of the **Pod as the network unit**.

---

# 4. Pod IPs are temporary

Although every Pod has an IP, we should not treat the IP as permanent.

For example:

```text
Backend Pod A
10.244.0.20
```

If the Pod is deleted:

```text
Backend Pod A
10.244.0.20
      X
```

Kubernetes creates another Pod:

```text
Backend Pod B
10.244.0.35
```

The IP changed.

This is why applications should normally communicate through a Service rather than directly depending on a Pod IP.

So:

```text
Pod IP
→ useful for networking

Service
→ stable way to access the application
```

---

# 5. Pod-to-Pod communication

One of the fundamental Kubernetes networking ideas is that Pods should be able to communicate with each other.

For example:

```text
Frontend Pod
     |
     | HTTP request
     ↓
Backend Pod
```

The Kubernetes networking system provides the connectivity needed for this communication.

Conceptually:

```text
Frontend Pod
10.244.0.10
      |
      |
      ↓
Cluster Network
      |
      |
      ↓
Backend Pod
10.244.0.20
```

We don't manually configure a route for every Pod.

The Kubernetes cluster networking setup provides this connectivity.

---

# 6. Pod-to-Pod communication across nodes

This becomes more interesting when we have multiple Worker Nodes.

Imagine:

```text
Worker Node 1
      |
      └── Frontend Pod
          10.244.0.10


Worker Node 2
      |
      └── Backend Pod
          10.244.1.10
```

The frontend still needs to communicate with the backend:

```text
Frontend Pod
     |
     ↓
Backend Pod
```

even though they are running on different nodes.

Kubernetes networking is designed to provide Pod-to-Pod connectivity across the cluster.

Conceptually:

```text
Node 1                         Node 2
  |                              |
  ↓                              ↓
Frontend Pod                  Backend Pod
10.244.0.10                   10.244.1.10
       \                        /
        \                      /
         +---- Cluster -------+
              Network
```

We don't need to manually create a separate network between every pair of nodes.

---

# 7. Our Minikube environment

Our project is currently running on Minikube.

We saw:

```text
Node: minikube/192.168.49.2
```

Our current setup is essentially a single-node Kubernetes cluster.

Conceptually:

```text
Minikube Cluster
      |
      ↓
minikube Node
      |
      +── Backend Pods
      +── Frontend Pods
      +── Nginx Pods
      +── PostgreSQL Pod
```

Because we currently have a single worker node, we don't experience the complexity of traffic moving between multiple worker nodes.

But understanding the multi-node model is important for Kubernetes fundamentals.

---

# 8. Pod network vs Service network

This is an important distinction.

We have:

```text
Pod network
```

and:

```text
Service networking
```

They solve different problems.

### Pod networking

Allows Pods to communicate.

```text
Pod A
  |
  ↓
Pod B
```

### Service networking

Provides a stable endpoint for a group of Pods.

```text
Client
  |
  ↓
Service
  |
  ↓
Pods
```

So:

```text
Pod networking
→ How Pods communicate

Service
→ Stable access to Pods
```

---

# 9. Why don't applications normally use Pod IPs?

Suppose our frontend directly uses:

```text
10.244.0.20
```

for the backend.

Then Kubernetes recreates the backend Pod:

```text
Old:
10.244.0.20

New:
10.244.0.35
```

The frontend would still be trying to reach:

```text
10.244.0.20
```

That creates a problem.

Instead:

```text
Frontend
   |
   ↓
backend-service
   |
   ↓
Backend Pods
```

The Service keeps the application independent from individual Pod IP changes.

---

# 10. How Services fit into networking

We already learned that a Service has a ClusterIP.

Our backend Service:

```text
backend-service
ClusterIP: 10.111.127.205
```

The frontend can communicate with:

```text
backend-service:8080
```

rather than:

```text
10.244.0.20:8080
```

The simplified flow is:

```text
Frontend Pod
      |
      ↓
backend-service
      |
      ↓
Service ClusterIP
      |
      ↓
Backend Pod
```

The Service therefore gives us a stable networking abstraction.

---

# 11. What happens to the Service IP?

The Service has a virtual/stable ClusterIP.

For example:

```text
backend-service
      |
      ↓
10.111.127.205
```

This is not the same thing as a Pod IP.

Compare:

```text
Pod IP:
10.244.0.20

Service ClusterIP:
10.111.127.205
```

The Pod IP belongs to an individual Pod.

The ClusterIP belongs to the Service.

---

# 12. Service traffic needs routing

Suppose the frontend sends:

```text
http://backend-service:8080
```

DNS helps resolve:

```text
backend-service
```

to the Service's network address.

But now the network system needs to make the actual request reach a backend Pod.

Conceptually:

```text
Frontend Pod
     |
     ↓
Service ClusterIP
     |
     ↓
Service routing
     |
     ↓
Backend Pod
```

This is where Kubernetes service networking mechanisms become important.

---

# 13. kube-proxy

One important Kubernetes networking component to know is **kube-proxy**.

At a beginner level, think of kube-proxy as a component that helps implement Kubernetes Service networking on nodes.

It observes Services and their endpoints and configures the node's networking rules so traffic sent to a Service can be directed toward appropriate backend endpoints.

Conceptually:

```text
Service
   |
   ↓
kube-proxy networking rules
   |
   ↓
Backend Pod
```

We don't need to memorize its internal implementation yet.

The important idea is:

> **kube-proxy helps implement Service networking.**

---

# 14. kube-proxy is not the same as CoreDNS

This distinction is very important.

We just learned about CoreDNS.

### CoreDNS

Responsible for DNS name resolution.

```text
backend-service
      ↓
CoreDNS
      ↓
Service address
```

### kube-proxy

Helps implement Service traffic routing.

```text
Service address
      ↓
kube-proxy networking rules
      ↓
Backend Pod
```

So:

```text
CoreDNS
→ "What address belongs to this Service name?"

kube-proxy
→ "How should traffic sent to this Service reach an endpoint?"
```

---

# 15. Simplified Service request flow

Let's combine the concepts.

Suppose the frontend wants:

```text
http://backend-service:8080/api/tasks
```

The simplified flow is:

```text
                  Frontend Pod
                       |
                       |
                "backend-service"
                       |
                       ↓
                    CoreDNS
                       |
                       ↓
                Service ClusterIP
                       |
                       ↓
                 Service routing
                       |
                       ↓
                  Backend Pod
```

The important responsibilities are:

```text
CoreDNS
→ Name resolution

Service
→ Stable endpoint

Service networking
→ Traffic routing

Backend Pod
→ Runs the application
```

---

# 16. What is a CNI?

Another important Kubernetes networking term is **CNI**.

CNI stands for:

**Container Network Interface**

CNI is a standard used to configure networking for containers and Pods.

Kubernetes itself does not implement every low-level networking detail.

Instead, Kubernetes can use a CNI plugin to provide Pod networking.

Conceptually:

```text
Kubernetes
     |
     ↓
CNI plugin
     |
     ↓
Pod networking
```

The exact CNI plugin depends on the Kubernetes environment.

---

# 17. Why does Kubernetes need CNI?

When a new Pod is created, it needs networking.

For example:

```text
New Backend Pod
       |
       ↓
Needs:
- network interface
- IP address
- connectivity
```

The CNI networking implementation helps configure that Pod's network.

Conceptually:

```text
Pod created
    |
    ↓
CNI
    |
    +── Network interface
    +── Pod IP
    +── Network connectivity
```

We don't need to learn the implementation of a specific CNI plugin yet.

For beginner understanding, remember:

> **CNI provides the mechanism for configuring Pod networking.**

---

# 18. CNI vs kube-proxy

These two are easy to confuse.

### CNI

Mainly concerned with **Pod networking**.

```text
Pod
 ↓
Network interface
 ↓
Pod IP
 ↓
Connectivity
```

### kube-proxy

Mainly concerned with **Service networking**.

```text
Service
 ↓
Service routing rules
 ↓
Pod endpoint
```

So:

```text
CNI
→ Pod network

kube-proxy
→ Service network/routing
```

They work at different parts of the networking system.

---

# 19. Do Pods communicate through Services?

Not necessarily.

This is an important clarification.

Pods can communicate directly with other Pods using their Pod IPs.

For example:

```text
Pod A
  |
  ↓
Pod B
```

But applications generally use Services when they need a stable endpoint.

So:

```text
Pod-to-Pod
→ possible directly

Application-to-application
→ usually use a Service
```

For our project:

```text
Frontend
    ↓
backend-service
    ↓
Backend Pods
```

is preferable to directly calling a backend Pod IP.

---

# 20. Why does the Service need selectors?

We already learned this in the Services note.

Suppose:

```text
Backend Pod 1
label: app=backend

Backend Pod 2
label: app=backend
```

Service:

```yaml
selector:
  app: backend
```

The Service can identify the backend Pods.

Conceptually:

```text
backend-service
       |
       | selector
       ↓
   app=backend
       |
   +---+---+
   |       |
   ↓       ↓
 Pod 1   Pod 2
```

This information is used to maintain the Service's endpoints.

---

# 21. EndpointSlices in networking

Kubernetes needs to know which actual Pod endpoints belong to a Service.

Modern Kubernetes uses **EndpointSlices**.

Conceptually:

```text
Service
   |
   ↓
EndpointSlice
   |
   +---- 10.244.0.20:8080
   |
   +---- 10.244.0.21:8080
```

When Pods change, Kubernetes updates the endpoint information.

For example:

```text
Pod A deleted
       ↓
EndpointSlice updated
       ↓
Pod A removed from endpoints
```

A new Pod:

```text
Pod C created
       ↓
EndpointSlice updated
       ↓
Pod C added
```

This allows Service networking to know which endpoints are currently available.

---

# 22. Readiness and networking

Readiness probes connect strongly with networking.

Suppose:

```text
Backend Pod 1 → Ready
Backend Pod 2 → Not Ready
```

The not-ready Pod should not normally receive application traffic from the Service.

Conceptually:

```text
                    backend-service
                           |
                    +------+------+
                    |             |
                    ↓             ↓
               Ready Pod      Not Ready Pod
                    ↑
               receives traffic
```

This is why readiness probes are important for networking.

Remember:

```text
Liveness
→ Is the application alive?

Readiness
→ Is the application ready to receive traffic?
```

---

# 23. Pod-to-Pod communication model

A useful beginner mental model is:

```text
Pod A
IP: 10.244.0.10
      |
      | network
      ↓
Pod B
IP: 10.244.0.20
```

The cluster networking implementation provides the connectivity.

If the Pods are on different nodes:

```text
Node 1                         Node 2
  |                              |
Pod A                          Pod B
  |                              |
10.244.0.10                  10.244.1.10
       \                       /
        \                     /
         +--- Cluster Net ---+
```

The actual implementation can involve routing, overlays, encapsulation, or other mechanisms depending on the CNI.

We don't need those details yet.

---

# 24. Why do we not study the exact CNI implementation now?

Because our goal is to understand Kubernetes fundamentals first.

At this point, it is enough to know:

```text
Kubernetes
    |
    ↓
CNI
    |
    ↓
Pod networking
```

Later, if we use a specific CNI such as Calico or Cilium, we can study its implementation separately.

For our current project, knowing every packet-level detail would add complexity without helping us understand the architecture.

---

# 25. Kubernetes networking layers

We can now build a simple mental model.

```text
                 Kubernetes Networking
                         |
          +--------------+--------------+
          |                             |
      Pod Network                  Service Network
          |                             |
         CNI                         Service
          |                             |
      Pod IPs                     ClusterIP
                                        |
                                   kube-proxy
                                        |
                                   Pod endpoints
```

And DNS works alongside these:

```text
Application
     |
     ↓
CoreDNS
     |
     ↓
Service name → Service address
```

---

# 26. Complete communication flow

Let's follow an actual request from our project.

Suppose the frontend needs to call:

```text
backend-service:8080
```

### Step 1 — Application uses the Service name

```text
Frontend Pod
      |
      ↓
backend-service
```

### Step 2 — DNS resolution

```text
backend-service
      |
      ↓
CoreDNS
      |
      ↓
Service ClusterIP
```

### Step 3 — Service networking

```text
Service ClusterIP
      |
      ↓
Service routing
      |
      ↓
Endpoint
```

### Step 4 — Backend Pod

```text
Endpoint
    |
    ↓
Backend Pod
    |
    ↓
Spring Boot application
```

Complete simplified flow:

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
Service ClusterIP
      |
      ↓
Service routing
      |
      ↓
Backend Pod
      |
      ↓
Spring Boot
```

---

# 27. External traffic is different

So far we have discussed:

```text
Pod → Service → Pod
```

But what about a user outside the cluster?

For example:

```text
Browser
   |
   ↓
Minikube
   |
   ↓
Ingress
   |
   ↓
Service
   |
   ↓
Pod
```

This involves additional networking mechanisms such as:

* NodePort
* Ingress
* Ingress Controller

We already have an Ingress in our project.

We will study it separately in:

```text
10-ingress.md
```

So don't mix internal Pod networking with external traffic routing.

---

# 28. NodePort and networking

We have:

```text
backend-nodeport
```

which exposes:

```text
8080:30081
```

The simplified path is:

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
Backend Pod
```

This is different from:

```text
Frontend Pod
      |
      ↓
backend-service:8080
      |
      ↓
Backend Pod
```

NodePort is mainly an external-access mechanism.

ClusterIP is mainly internal.

---

# 29. Our project's networking architecture

Our current architecture can be represented as:

```text
                         User
                           |
                           ↓
                        Ingress
                           |
             +-------------+-------------+
             |                           |
             ↓                           ↓
      Frontend Service            Backend Service
             |                           |
             ↓                           ↓
      Frontend Pods               Backend Pods
                                         |
                                         ↓
                                PostgreSQL Service
                                         |
                                         ↓
                                  PostgreSQL Pod
```

Inside the cluster:

```text
Frontend Pod
     |
     ↓
backend-service
     |
     ↓
Backend Pod
```

and:

```text
Backend Pod
     |
     ↓
postgres-service
     |
     ↓
PostgreSQL Pod
```

DNS helps discover the Services.

CNI provides Pod networking.

Service networking provides Service-to-Pod routing.

---

# 30. Important distinction between all the components

At this point, we should clearly distinguish these components:

### CoreDNS

```text
"Where is backend-service?"
```

Provides DNS name resolution.

### Service

```text
"Give me a stable endpoint for the backend."
```

Provides stable access to selected Pods.

### EndpointSlice

```text
"Which actual endpoints currently belong to this Service?"
```

Tracks Service endpoints.

### kube-proxy

```text
"How should traffic to the Service be routed?"
```

Helps implement Service networking.

### CNI

```text
"How do Pods get network connectivity?"
```

Provides/configures Pod networking.

### Pod

```text
"Where is the application actually running?"
```

Runs the application workload.

---

# 31. Common beginner confusion

## Confusion 1: CoreDNS sends traffic to the Pod

Not exactly.

```text
CoreDNS
→ resolves names
```

It does not act as the HTTP traffic router.

---

## Confusion 2: Service is the same as a Pod

No.

```text
Pod
→ runs application

Service
→ stable network access to Pods
```

---

## Confusion 3: Service IP is a Pod IP

No.

```text
Pod IP
→ belongs to Pod

ClusterIP
→ belongs to Service
```

---

## Confusion 4: CNI is the Service

No.

```text
CNI
→ Pod networking

Service
→ stable endpoint + traffic distribution
```

---

## Confusion 5: kube-proxy creates Pods

No.

Pods are managed through controllers such as ReplicaSets/Deployments.

kube-proxy helps implement Service networking.

---

# 32. Useful commands

### See Pod IPs

```bash
kubectl get pods -o wide
```

Example:

```text
NAME                  IP
backend-xxx           10.244.0.20
backend-yyy           10.244.0.21
```

---

### See Services

```bash
kubectl get services
```

---

### See Service details

```bash
kubectl describe service backend-service
```

---

### See EndpointSlices

```bash
kubectl get endpointslices
```

---

### See backend endpoints

```bash
kubectl get endpoints backend-service
```

---

### See nodes

```bash
kubectl get nodes -o wide
```

---

### See system Pods

```bash
kubectl get pods -n kube-system
```

This can help us identify components such as CoreDNS.

---

# 33. A useful practical experiment

We can observe the Pod networking directly.

Run:

```bash
kubectl get pods -l app=backend -o wide
```

You will see something like:

```text
NAME                         READY   STATUS    IP
backend-deployment-xxx       1/1     Running   10.244.0.20
backend-deployment-yyy       1/1     Running   10.244.0.21
```

Notice that each Pod has its own IP.

Now scale the backend:

```bash
kubectl scale deployment backend-deployment --replicas=3
```

Then:

```bash
kubectl get pods -l app=backend -o wide
```

You should see a third Pod with its own IP.

This demonstrates:

```text
One Deployment
      ↓
Multiple Pods
      ↓
Multiple Pod IPs
```

The Service hides this changing Pod set from applications.

---

# 34. Another useful experiment

Check the backend Service:

```bash
kubectl get service backend-service
```

Then check the backend Pods:

```bash
kubectl get pods -l app=backend -o wide
```

You will notice:

```text
Service IP
```

is different from:

```text
Pod IPs
```

For example:

```text
backend-service
ClusterIP: 10.111.127.205


Backend Pods:
10.244.0.20
10.244.0.21
10.244.0.22
```

This makes the difference between Service networking and Pod networking concrete.

---

# 35. What happens when a backend Pod is replaced?

Suppose we currently have:

```text
Service
  |
  +---- 10.244.0.20
  |
  +---- 10.244.0.21
```

One Pod is replaced:

```text
Old:
10.244.0.20
      X

New:
10.244.0.35
```

Kubernetes updates the endpoint information.

The Service continues to provide:

```text
backend-service
```

The application doesn't need to know about the IP change.

This demonstrates why the architecture is built around:

```text
Service
+
Endpoint management
+
Pod networking
```

---

# 36. Why Kubernetes networking is important

Without networking, our application components would be isolated.

We need:

```text
Frontend
   ↓
Backend
   ↓
PostgreSQL
```

to communicate.

Kubernetes networking provides the foundation for these connections.

Our application depends on:

```text
Pod networking
+
Service networking
+
DNS
+
Ingress/external networking
```

Each solves a different part of the problem.

---

# 37. Interview explanation — Kubernetes networking

If an interviewer asks:

## How does networking work in Kubernetes?

A simple answer is:

> "Kubernetes gives Pods network connectivity and an IP address so they can communicate within the cluster. Services provide stable endpoints for accessing groups of Pods, while Kubernetes DNS allows applications to discover those Services by name."

If they ask about Service routing:

> "A Service selects the appropriate Pods using labels. Kubernetes maintains endpoint information for those Pods, and the Service networking layer routes traffic sent to the Service toward an appropriate endpoint."

If they ask about CNI:

> "CNI stands for Container Network Interface. Kubernetes uses CNI plugins to configure networking for Pods, including their network interfaces, IP addresses, and connectivity."

If they ask about kube-proxy:

> "kube-proxy is a node-level component that helps implement Kubernetes Service networking by configuring networking rules so traffic to a Service can reach its backend endpoints."

If they ask about CoreDNS:

> "CoreDNS provides DNS-based service discovery. It resolves Kubernetes Service names such as `backend-service` so applications don't need to hard-code Service IP addresses."

---

# 38. The most important mental model

Don't memorize packet-level implementation details.

Remember:

```text
                    Kubernetes Networking

                         Application
                              |
                              ↓
                         Service name
                              |
                              ↓
                           CoreDNS
                              |
                              ↓
                        Service address
                              |
                              ↓
                       Service routing
                              |
                              ↓
                       Endpoint / Pod
                              |
                              ↓
                     Pod network / CNI
```

And remember the responsibilities:

```text
CNI
↓
Provides Pod networking


CoreDNS
↓
Resolves Service names


Service
↓
Provides stable endpoint


EndpointSlice
↓
Tracks Service endpoints


kube-proxy
↓
Helps implement Service routing


Pod
↓
Runs the application
```

---

# 39. What we should understand instead of memorizing

The most important question to be able to answer is:

> **"If my frontend wants to communicate with my backend, what happens?"**

Your own explanation should be roughly:

```text
The frontend doesn't directly depend on a backend Pod IP
because Pod IPs can change.

Instead, it uses the backend Service name.

Kubernetes DNS/CoreDNS resolves that Service name.

The request then goes to the Service.

The Service has a selector that identifies the backend Pods,
and Kubernetes maintains the available endpoints.

The Service networking layer routes the request to an
appropriate backend Pod.

The backend Pod then processes the request.
```

That explanation demonstrates actual understanding rather than memorization.

---

# 40. Final architecture

Our current networking model is:

```text
                         USER
                           |
                           ↓
                        INGRESS
                           |
             +-------------+-------------+
             |                           |
             ↓                           ↓
      FRONTEND SERVICE            BACKEND SERVICE
             |                           |
             ↓                           ↓
       FRONTEND PODS               BACKEND PODS
                                         |
                                         ↓
                                POSTGRES SERVICE
                                         |
                                         ↓
                                  POSTGRES POD
```

For internal communication:

```text
Frontend Pod
     |
     | backend-service
     ↓
   CoreDNS
     |
     ↓
Backend Service
     |
     ↓
EndpointSlice
     |
     ↓
Backend Pod
```

For Pod networking:

```text
Pod
 |
 ↓
CNI
 |
 ↓
Pod network
 |
 ↓
Other Pod
```

For Service networking:

```text
Service
 |
 ↓
Service networking
 |
 ↓
Backend endpoint
```

---

# 41. Final summary

The main concepts we should remember are:

### Pod networking

```text
Pod
 ↓
Pod IP
 ↓
Cluster network
 ↓
Other Pod
```

### Service networking

```text
Client
 ↓
Service
 ↓
Selected Pod
```

### DNS

```text
backend-service
 ↓
CoreDNS
 ↓
Service address
```

### Endpoint management

```text
Service
 ↓
Selector
 ↓
Matching Pods
 ↓
EndpointSlice
```

### CNI

```text
CNI
 ↓
Pod network configuration
 ↓
Pod connectivity
```

### kube-proxy

```text
Service
 ↓
kube-proxy/networking rules
 ↓
Backend endpoint
```

And the complete mental model is:

```text
                         FRONTEND POD
                              |
                              |
                     backend-service
                              |
                              ↓
                           CoreDNS
                              |
                              ↓
                       Service ClusterIP
                              |
                              ↓
                         Service routing
                              |
                              ↓
                        EndpointSlice
                              |
                     +--------+--------+
                     |                 |
                     ↓                 ↓
                BACKEND POD 1     BACKEND POD 2
                     |                 |
                     +--------+--------+
                              |
                         Pod Network
                              |
                            CNI
```

The key idea is:

> **Kubernetes networking provides the connectivity between workloads, while Services provide stable access to changing Pods, CoreDNS provides name-based discovery, and CNI provides the underlying Pod networking.**

This gives us the foundation we need before moving to the next concept:

```text
07 Kubernetes Networking
        ↓
08 ConfigMaps and Secrets
```
