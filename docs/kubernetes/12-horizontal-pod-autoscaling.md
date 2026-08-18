# Horizontal Pod Autoscaling (HPA)

## 1. Why do we need autoscaling?

We already learned how Kubernetes can run multiple replicas of our application.

For example, our backend currently has:

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
````

We can manually decide:

```yaml
replicas: 2
```

This means:

> "I want 2 backend Pods."

But there is a problem.

What if the application suddenly receives a lot of traffic?

For example:

```text
Normal traffic

Backend Pod 1
Backend Pod 2
```

Everything may be fine.

But during high traffic:

```text
                 Many Requests
                      |
                      ↓
               Backend Pods
                ↑         ↑
             High CPU   High CPU
```

Two Pods may not be enough.

We could manually scale:

```bash
kubectl scale deployment backend-deployment --replicas=5
```

But this requires a person to monitor the application and change the replica count.

What if traffic changes frequently?

We would need:

```text
Monitor traffic
     ↓
Check resource usage
     ↓
Increase replicas
     ↓
Traffic decreases
     ↓
Decrease replicas
```

Doing this manually is inefficient.

This is where **Horizontal Pod Autoscaling (HPA)** comes in.

---

# 2. What is Horizontal Pod Autoscaling?

Horizontal Pod Autoscaling, commonly called **HPA**, automatically changes the number of Pods running for a workload based on resource usage or other supported metrics.

Simple definition:

> **HPA automatically increases or decreases the number of Pods based on defined scaling conditions.**

For example:

```text
Normal load
    ↓
2 Pods

High load
    ↓
3 Pods
    ↓
4 Pods
    ↓
5 Pods
```

When the load decreases:

```text
Low load
    ↓
4 Pods
    ↓
3 Pods
    ↓
2 Pods
```

So:

```text
HPA
 ↓
Automatically adjusts replicas
```

---

# 3. Why is it called "Horizontal" scaling?

There are two common ways to scale an application.

## Vertical scaling

Increase the resources of an existing Pod.

For example:

```text
Before:

Pod
CPU:    500m
Memory: 512Mi


After:

Pod
CPU:    1 CPU
Memory: 1Gi
```

We make the existing workload bigger.

This is called:

> **Vertical scaling**

---

## Horizontal scaling

Create more Pods.

For example:

```text
Before:

Backend Pod 1
Backend Pod 2


After:

Backend Pod 1
Backend Pod 2
Backend Pod 3
Backend Pod 4
```

We increase the number of application instances.

This is called:

> **Horizontal scaling**

HPA performs horizontal scaling.

---

# 4. Horizontal vs vertical scaling

The easiest way to remember:

```text
Vertical scaling
→ Make the Pod bigger

Horizontal scaling
→ Create more Pods
```

Example:

```text
Vertical:

      Bigger Pod
         ↑
    More CPU/RAM


Horizontal:

Pod 1   Pod 2   Pod 3
  ↑       ↑       ↑
More application instances
```

HPA is concerned with the second approach.

---

# 5. What does HPA actually scale?

HPA normally works with scalable workloads such as:

```text
Deployment
StatefulSet
```

In our project, HPA targets:

```text
backend-deployment
```

We verified this using:

```bash
kubectl get hpa
```

and saw:

```text
horizontalpodautoscaler.autoscaling/backend-hpa
REFERENCE:
Deployment/backend-deployment
```

So our architecture is:

```text
HPA
 ↓
backend-deployment
 ↓
ReplicaSet
 ↓
Backend Pods
```

HPA does not directly create containers.

It changes the desired replica count of the target workload.

---

# 6. HPA and Deployment

Suppose our Deployment says:

```yaml
replicas: 2
```

Then normally:

```text
Deployment
     |
     ↓
Desired replicas = 2
     |
     ↓
ReplicaSet
     |
     +--------+
     |        |
     ↓        ↓
   Pod 1    Pod 2
```

Now HPA decides that 3 Pods are required.

Conceptually:

```text
HPA
 ↓
Deployment desired replicas = 3
 ↓
ReplicaSet
 ↓
Pod 1
Pod 2
Pod 3
```

So the relationship is:

```text
HPA
 ↓
Deployment
 ↓
ReplicaSet
 ↓
Pods
```

---

# 7. Why does HPA change replicas?

HPA continuously checks the metrics configured for the autoscaler.

A common metric is:

```text
CPU utilization
```

For example:

```text
Target CPU utilization = 70%
```

Suppose:

```text
Current CPU = 90%
```

HPA may decide that more Pods are needed.

Conceptually:

```text
CPU usage
   ↓
Above target
   ↓
Increase replicas
```

When CPU usage decreases:

```text
CPU usage
   ↓
Below target
   ↓
Decrease replicas
```

The exact scaling decision is controlled by Kubernetes' autoscaling algorithm and configuration.

---

# 8. Our HPA

Our project already has an HPA:

```text
backend-hpa
```

We verified:

```bash
kubectl get hpa
```

and saw:

```text
NAME          REFERENCE                       TARGETS   MINPODS   MAXPODS   REPLICAS
backend-hpa   Deployment/backend-deployment   cpu: 3%/70%    2         5         2
```

Let's understand this.

```text
NAME:
backend-hpa
```

The name of our Horizontal Pod Autoscaler.

```text
REFERENCE:
Deployment/backend-deployment
```

The workload controlled by the HPA.

```text
TARGETS:
cpu: 3%/70%
```

Current CPU usage is approximately:

```text
3%
```

and our target is:

```text
70%
```

```text
MINPODS:
2
```

The HPA should not scale below 2 Pods.

```text
MAXPODS:
5
```

The HPA should not scale above 5 Pods.

```text
REPLICAS:
2
```

Currently, the target workload has 2 replicas.

---

# 9. HPA minimum and maximum replicas

An HPA usually defines:

```yaml
minReplicas: 2
maxReplicas: 5
```

This means:

```text
Minimum:
2 Pods

Maximum:
5 Pods
```

So HPA can adjust the backend between:

```text
2 → 3 → 4 → 5
```

and back down:

```text
5 → 4 → 3 → 2
```

It cannot go below:

```text
2
```

or above:

```text
5
```

---

# 10. Why do we use `minReplicas`?

Suppose our application normally needs at least two backend instances.

We can define:

```yaml
minReplicas: 2
```

Then even when traffic is low:

```text
Low traffic
    ↓
HPA
    ↓
Minimum = 2
    ↓
Backend Pod 1
Backend Pod 2
```

The HPA will not scale the workload below the configured minimum.

---

# 11. Why do we use `maxReplicas`?

We also don't want Kubernetes to create an unlimited number of Pods.

For example, if something goes wrong and CPU usage remains extremely high, we don't want:

```text
2
↓
10
↓
100
↓
1000
```

Instead:

```yaml
maxReplicas: 5
```

limits the maximum number of replicas.

So:

```text
HPA
 |
 +---- minimum = 2
 |
 +---- maximum = 5
```

---

# 12. What metric does HPA use?

HPA can use different types of metrics.

For our beginner-level understanding, the most important one is:

```text
CPU utilization
```

For example:

```yaml
metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
```

This means:

> "Try to maintain average CPU utilization around 70% across the Pods."

HPA can also work with memory and other supported/custom metrics, but CPU is the simplest place to start.

---

# 13. Why does HPA need resource requests?

This is a very important concept.

Our backend Deployment has:

```yaml
resources:
  requests:
    cpu: "100m"
    memory: "256Mi"

  limits:
    cpu: "500m"
    memory: "512Mi"
```

The CPU request is:

```text
100m
```

which means:

```text
0.1 CPU
```

If HPA uses CPU utilization as a percentage, Kubernetes needs a CPU request to calculate that utilization.

For example, conceptually:

```text
CPU usage
   ÷
CPU request
   × 100
   =
CPU utilization
```

Suppose:

```text
CPU request = 100m
CPU usage   = 70m
```

Then approximately:

```text
70m / 100m × 100
= 70%
```

So the HPA sees:

```text
CPU utilization ≈ 70%
```

This is why resource requests are important when using CPU-based HPA.

---

# 14. CPU request vs CPU limit

We should not confuse these.

Our backend has:

```yaml
requests:
  cpu: "100m"

limits:
  cpu: "500m"
```

### CPU request

The amount of CPU Kubernetes uses when scheduling the Pod and, for utilization-based HPA, the baseline against which usage is measured.

```text
request = 100m
```

### CPU limit

The maximum CPU amount the container is allowed to use according to the configured limit.

```text
limit = 500m
```

So:

```text
Request
→ Used as a resource baseline

Limit
→ Maximum allowed resource usage
```

---

# 15. How HPA gets metrics

HPA needs current resource metrics.

For CPU and memory usage, Kubernetes commonly gets these metrics through the **Metrics Server**.

Our Minikube environment includes:

```text
metrics-server
```

We previously saw it in our Minikube environment.

Conceptually:

```text
Backend Pods
     |
     ↓
Metrics
     |
     ↓
Metrics Server
     |
     ↓
HPA
     |
     ↓
Deployment
     |
     ↓
Pods
```

The Metrics Server provides resource usage information that HPA can use for resource-based autoscaling.

---

# 16. What is Metrics Server?

Metrics Server is a Kubernetes component that collects resource usage metrics from nodes and Pods.

For example:

```text
Backend Pod 1
CPU: 40m

Backend Pod 2
CPU: 60m
```

Metrics Server makes this information available to Kubernetes components such as HPA.

Simple definition:

> **Metrics Server provides current CPU and memory usage metrics for Kubernetes workloads.**

We don't need to study its internal implementation deeply right now.

---

# 17. How does HPA work?

The simplified flow is:

```text
Backend Pods
     |
     ↓
CPU / Memory usage
     |
     ↓
Metrics Server
     |
     ↓
HPA
     |
     ↓
Compare current usage with target
     |
     ↓
Calculate required replicas
     |
     ↓
Deployment
     |
     ↓
ReplicaSet
     |
     ↓
Pods
```

The process repeats continuously.

This is another example of Kubernetes continuously observing the system and adjusting it.

---

# 18. Example: CPU is low

Suppose:

```text
Minimum replicas = 2
Maximum replicas = 5
Target CPU = 70%
Current CPU = 20%
```

The HPA may determine that fewer replicas are sufficient.

But because:

```text
minReplicas = 2
```

the application remains at:

```text
2 Pods
```

So:

```text
Low CPU
   ↓
HPA wants fewer replicas
   ↓
Minimum limit = 2
   ↓
2 Pods remain
```

---

# 19. Example: CPU increases

Suppose:

```text
Current replicas = 2
Target CPU = 70%
Current CPU = 90%
```

The HPA detects that the workload is above the target.

It can increase the desired replica count.

For example:

```text
2 Pods
   ↓
HPA detects high CPU
   ↓
3 Pods
```

If the load continues:

```text
3 Pods
   ↓
4 Pods
```

and potentially:

```text
5 Pods
```

because:

```text
maxReplicas = 5
```

---

# 20. Example: traffic decreases

Suppose we have:

```text
5 Pods
```

and traffic decreases.

CPU usage may fall:

```text
90%
 ↓
70%
 ↓
40%
```

HPA can reduce the number of replicas.

For example:

```text
5
↓
4
↓
3
↓
2
```

until it reaches:

```text
minReplicas = 2
```

---

# 21. HPA does not directly create containers

This is important.

We don't have:

```text
HPA
 ↓
Container
```

Instead:

```text
HPA
 ↓
Deployment
 ↓
ReplicaSet
 ↓
Pods
 ↓
Containers
```

HPA changes the desired number of replicas of the target workload.

The Deployment/ReplicaSet machinery handles creating or removing Pods.

---

# 22. HPA and desired state

This connects directly to our first Kubernetes concept.

We learned:

```text
Desired State
      ↓
Kubernetes
      ↓
Actual State
```

With HPA, the desired replica count can change dynamically.

For example:

```text
Initially:

Desired replicas = 2
```

HPA observes high CPU:

```text
HPA
 ↓
Desired replicas = 3
```

Then the Deployment/ReplicaSet works toward:

```text
Actual replicas = 3
```

So:

```text
HPA
→ Dynamically influences desired replica count
```

This is an important connection.

---

# 23. HPA and the Deployment

Our architecture is:

```text
                    HPA
                     |
                     ↓
            backend-deployment
                     |
                     ↓
                 ReplicaSet
                     |
             +-------+-------+
             |               |
             ↓               ↓
          Backend         Backend
            Pod             Pod
```

When HPA scales:

```text
HPA
 ↓
Deployment replicas
 ↓
ReplicaSet
 ↓
Pods
```

This is how autoscaling fits into the architecture we already learned.

---

# 24. HPA does not change the Docker image

Suppose the backend uses:

```text
cloud-native-task-platform-backend:latest
```

HPA does not rebuild or change the image.

It only changes:

```text
Number of Pods
```

For example:

```text
Same image:

cloud-native-task-platform-backend:latest

Pod 1
Pod 2
Pod 3
Pod 4
```

All these Pods can run the same application image.

---

# 25. HPA and Services

Now connect HPA with Services.

Suppose:

```text
Backend Service
       |
       +----> Backend Pod 1
       |
       +----> Backend Pod 2
```

HPA increases replicas:

```text
Backend Service
       |
       +----> Backend Pod 1
       |
       +----> Backend Pod 2
       |
       +----> Backend Pod 3
       |
       +----> Backend Pod 4
```

The Service continues providing the stable endpoint.

The additional Pods become available as appropriate targets when they are ready.

So:

```text
HPA
→ Changes number of Pods

Service
→ Provides stable access to those Pods
```

These concepts work together.

---

# 26. HPA and readiness probes

This also connects to what we learned earlier.

Suppose HPA creates a new backend Pod.

Initially:

```text
New Pod
   ↓
Application starting
   ↓
Not Ready
```

The readiness probe checks:

```text
/actuator/health/readiness
```

Once the application becomes ready:

```text
Readiness = successful
   ↓
Pod becomes Ready
   ↓
Service can send normal traffic to it
```

So the concepts connect like this:

```text
HPA
 ↓
Creates additional Pod
 ↓
Pod starts
 ↓
Readiness probe
 ↓
Pod becomes Ready
 ↓
Service can use the Pod
```

This is an important real-world flow.

---

# 27. HPA and liveness probes

Liveness and HPA have different responsibilities.

### Liveness

Answers:

> "Is this container still healthy enough to keep running?"

If liveness repeatedly fails:

```text
Liveness failure
      ↓
Container restart
```

### HPA

Answers:

> "Do we need more or fewer replicas?"

For example:

```text
High CPU
   ↓
HPA
   ↓
More Pods
```

So:

```text
Liveness
→ Health/restart

HPA
→ Scaling
```

Do not confuse them.

---

# 28. HPA and resource limits

Our backend has:

```yaml
requests:
  cpu: "100m"
  memory: "256Mi"

limits:
  cpu: "500m"
  memory: "512Mi"
```

This gives Kubernetes information about the resources the application requests and the maximum resources it can use.

For CPU utilization-based HPA, the CPU request is particularly important.

A simple mental model is:

```text
CPU request
    ↓
Baseline
    ↓
Current CPU usage
    ↓
HPA calculates utilization
    ↓
Scaling decision
```

---

# 29. How to view HPA

We can run:

```bash
kubectl get hpa
```

Example:

```text
NAME          REFERENCE                       TARGETS   MINPODS   MAXPODS   REPLICAS
backend-hpa   Deployment/backend-deployment   cpu: 3%/70%    2         5         2
```

This is one of the most useful commands when troubleshooting HPA.

---

# 30. How to get more HPA information

Use:

```bash
kubectl describe hpa backend-hpa
```

This can show information such as:

```text
Target
Current metrics
Desired replicas
Minimum replicas
Maximum replicas
Scaling conditions
Events
```

If HPA is not behaving as expected, this is one of the first commands to check.

---

# 31. Check resource metrics

We can check current Pod metrics using:

```bash
kubectl top pods
```

For nodes:

```bash
kubectl top nodes
```

If these commands return CPU and memory usage, the Metrics Server is providing metrics correctly.

For example:

```text
NAME                         CPU(cores)   MEMORY(bytes)
backend-deployment-...       5m           250Mi
backend-deployment-...       4m           245Mi
```

The exact values will change over time.

---

# 32. Our current HPA state

We previously observed:

```text
backend-hpa
```

with:

```text
Target:
70% CPU

Minimum:
2 Pods

Maximum:
5 Pods

Current:
2 Pods
```

And:

```text
cpu: 3%/70%
```

This means approximately:

```text
Current CPU = 3%
Target CPU  = 70%
```

The application is currently using much less CPU than the target.

Therefore,  the HPA remains at its minimum:

```text
2 Pods
```

---

# 33. Why didn't HPA reduce our backend to 1 Pod?

Because our configuration says:

```yaml
minReplicas: 2
```

So even if CPU usage becomes:

```text
1%
```

HPA cannot reduce the Deployment below:

```text
2 Pods
```

Therefore:

```text
Low CPU
   ↓
HPA wants fewer Pods
   ↓
Minimum = 2
   ↓
2 Pods remain
```

---

# 34. Why doesn't HPA immediately scale on every CPU change?

Autoscaling should not react to every tiny metric fluctuation.

Imagine CPU changes like:

```text
69%
71%
68%
72%
70%
```

If Kubernetes immediately scaled up and down for every tiny change, the system could continuously oscillate.

Therefore, Kubernetes uses its autoscaling algorithm and timing behavior to make scaling decisions more stable.

The important beginner-level idea is:

> **HPA does not blindly change replicas for every small metric fluctuation.**

---

# 35. HPA scale-up vs scale-down

The two directions are:

### Scale up

Increase replicas:

```text
2 → 3 → 4 → 5
```

Usually because workload demand increases.

### Scale down

Decrease replicas:

```text
5 → 4 → 3 → 2
```

Usually because workload demand decreases.

The HPA configuration defines limits and Kubernetes applies autoscaling behavior around those limits.

---

# 36. HPA is not the same as Cluster Autoscaler

This distinction is important.

### HPA

Scales:

```text
Pods
```

For example:

```text
2 Pods → 5 Pods
```

### Cluster Autoscaler

Scales:

```text
Worker Nodes
```

For example:

```text
2 Worker Nodes → 3 Worker Nodes
```

So:

```text
HPA
→ More/fewer Pods

Cluster Autoscaler
→ More/fewer Nodes
```

They can work together.

---

# 37. What if HPA wants 5 Pods but there is not enough node capacity?

Suppose:

```text
HPA
 ↓
Wants 5 Pods
```

but the available Worker Node does not have enough resources.

Then Kubernetes may not be able to schedule all the Pods.

Conceptually:

```text
HPA
 ↓
5 Pods required
 ↓
Insufficient node resources
 ↓
Some Pods cannot be scheduled
```

This is where cluster-level scaling can become useful.

For example:

```text
HPA
 ↓
More Pods required
 ↓
Cluster has insufficient capacity
 ↓
Cluster Autoscaler
 ↓
More Worker Nodes
 ↓
Pods can be scheduled
```

We don't need to implement Cluster Autoscaler in Minikube right now.

---

# 38. HPA does not automatically mean more performance

Adding more Pods can help when the application is designed to scale horizontally.

For example:

```text
Request
 ↓
Backend Pod 1
Backend Pod 2
Backend Pod 3
```

But simply adding Pods does not solve every performance problem.

The application must be able to handle multiple instances.

For our backend:

```text
Spring Boot backend
```

is suitable for running multiple instances, while persistent shared state is kept in PostgreSQL rather than local Pod storage.

---

# 39. Why stateless applications work well with HPA

Suppose we have:

```text
Backend Pod 1
Backend Pod 2
Backend Pod 3
```

All of them run the same application.

Requests can be distributed through the Service.

The application state is stored in external systems such as:

```text
PostgreSQL
```

rather than being stored only inside one Pod.

This makes horizontal scaling easier.

Conceptually:

```text
                Backend Service
                      |
        +-------------+-------------+
        |             |             |
        ↓             ↓             ↓
      Pod 1         Pod 2         Pod 3
        |             |             |
        +-------------+-------------+
                      |
                      ↓
                 PostgreSQL
```

This is one reason our architecture separates:

```text
Backend
Database
```

---

# 40. Example HPA YAML

A basic HPA can look like:

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler

metadata:
  name: backend-hpa

spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: backend-deployment

  minReplicas: 2
  maxReplicas: 5

  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70
```

Let's understand the important parts.

---

# 41. `scaleTargetRef`

```yaml
scaleTargetRef:
  apiVersion: apps/v1
  kind: Deployment
  name: backend-deployment
```

This tells HPA:

> "This is the workload whose replica count you should control."

In our project:

```text
HPA
 ↓
backend-deployment
```

---

# 42. `minReplicas`

```yaml
minReplicas: 2
```

This defines the minimum number of replicas.

```text
Minimum = 2
```

---

# 43. `maxReplicas`

```yaml
maxReplicas: 5
```

This defines the maximum number of replicas.

```text
Maximum = 5
```

---

# 44. CPU target

```yaml
averageUtilization: 70
```

This means:

```text
Target average CPU utilization = 70%
```

HPA uses this target when determining whether more or fewer replicas may be needed.

---

# 45. Complete HPA architecture

The complete flow is:

```text
                    APPLICATION
                         |
                         ↓
                      Pods
                         |
                  CPU / Memory
                     usage
                         |
                         ↓
                  Metrics Server
                         |
                         ↓
                        HPA
                         |
                Compare with target
                         |
                         ↓
                Desired replicas
                         |
                         ↓
                   Deployment
                         |
                         ↓
                    ReplicaSet
                         |
                         ↓
                     New Pods
```

And it continuously repeats.

---

# 46. HPA and Services together

Our complete backend flow becomes:

```text
                    Requests
                        |
                        ↓
                  Backend Service
                        |
             +----------+----------+
             |          |          |
             ↓          ↓          ↓
           Pod 1      Pod 2      Pod 3
             |          |          |
             +----------+----------+
                        |
                        ↓
                  Resource Usage
                        |
                        ↓
                  Metrics Server
                        |
                        ↓
                       HPA
                        |
                        ↓
                Backend Deployment
                        |
                        ↓
                  ReplicaSet
                        |
                        ↓
                More/Fewer Pods
```

This shows how the concepts we learned earlier work together.

---

# 47. What we actually practiced in our project

We already created and observed:

```text
backend-hpa
```

We verified it with:

```bash
kubectl get hpa
```

We observed:

```text
backend-hpa
→ Deployment/backend-deployment
→ CPU target = 70%
→ minReplicas = 2
→ maxReplicas = 5
→ current replicas = 2
```

We also verified that our backend Deployment has CPU requests:

```yaml
resources:
  requests:
    cpu: "100m"
```

This is important for CPU utilization-based HPA.

Our current architecture therefore includes:

```text
backend-hpa
      |
      ↓
backend-deployment
      |
      ↓
ReplicaSet
      |
      ↓
Backend Pods
```

---

# 48. Useful commands for our project

### Check HPA

```bash
kubectl get hpa
```

### Detailed HPA information

```bash
kubectl describe hpa backend-hpa
```

### Check current Pod resource usage

```bash
kubectl top pods
```

### Check node resource usage

```bash
kubectl top nodes
```

### Check backend replicas

```bash
kubectl get deployment backend-deployment
```

### Check backend Pods

```bash
kubectl get pods -l app=backend
```

### Check HPA YAML

```bash
kubectl get hpa backend-hpa -o yaml
```

---

# 49. Common beginner mistakes

## Mistake 1 — Thinking HPA creates containers directly

It doesn't.

The flow is:

```text
HPA
 ↓
Deployment
 ↓
ReplicaSet
 ↓
Pods
 ↓
Containers
```

---

## Mistake 2 — Thinking HPA scales nodes

It doesn't.

```text
HPA
→ Pods

Cluster Autoscaler
→ Nodes
```

---

## Mistake 3 — Thinking HPA always increases replicas when CPU is high

HPA is constrained by:

```text
minReplicas
maxReplicas
```

and its scaling algorithm.

For example:

```text
maxReplicas = 5
```

means HPA cannot scale beyond 5 replicas.

---

## Mistake 4 — Forgetting CPU requests

CPU utilization-based HPA needs an appropriate CPU request to calculate utilization.

For example:

```yaml
requests:
  cpu: "100m"
```

---

## Mistake 5 — Thinking persistent storage automatically scales

Storage and Pod scaling are separate concepts.

```text
HPA
→ Number of Pods

PVC/PV
→ Persistent Storage
```

---

## Mistake 6 — Thinking HPA replaces Deployment

HPA does not replace the Deployment.

They work together:

```text
HPA
 ↓
Deployment
 ↓
ReplicaSet
 ↓
Pods
```

---

# 50. Interview explanations

## What is HPA?

> "Horizontal Pod Autoscaler automatically adjusts the number of Pods of a workload based on metrics such as CPU utilization. It can scale the application between a configured minimum and maximum number of replicas."

---

## Why do we need HPA?

> "Instead of manually changing the number of Pods when traffic changes, HPA can automatically increase or decrease replicas based on application resource usage."

---

## What is horizontal scaling?

> "Horizontal scaling means increasing or decreasing the number of application instances, such as adding or removing Pods."

---

## What is vertical scaling?

> "Vertical scaling means increasing or decreasing the resources allocated to an existing instance, such as CPU or memory."

---

## How does HPA work?

> "HPA gets metrics such as CPU utilization, compares them with the configured target, and adjusts the desired replica count of the target workload. The Deployment and ReplicaSet then create or remove Pods to reach that desired count."

---

## What is the difference between HPA and Cluster Autoscaler?

> "HPA scales the number of Pods, while Cluster Autoscaler scales the number of Worker Nodes when the cluster doesn't have enough capacity."

---

## Why are CPU requests important for HPA?

> "For CPU utilization-based autoscaling, Kubernetes uses the CPU request as the baseline for calculating CPU utilization. Therefore, CPU requests need to be configured appropriately."

---

## Does HPA directly create Pods?

> "No. HPA changes the desired replica count of the target workload, such as a Deployment. The Deployment and ReplicaSet are responsible for creating or removing the Pods."

---

# 51. Questions that naturally arise

These concepts can be studied separately or when we move into more advanced Kubernetes topics.

### Question 1

How exactly does Kubernetes calculate the number of replicas?

→ HPA scaling algorithm and metrics.

### Question 2

How frequently does HPA check metrics?

→ HPA controller timing and behavior.

### Question 3

Why doesn't HPA immediately scale up and down for every small CPU change?

→ Stabilization and scaling behavior.

### Question 4

What happens if the cluster doesn't have enough resources for the new Pods?

→ Pod scheduling and Cluster Autoscaler.

### Question 5

Can HPA scale based on memory?

→ Yes, HPA can use resource metrics such as memory.

### Question 6

Can HPA scale based on application-specific metrics?

→ Yes, custom and external metrics can be used with the appropriate metrics infrastructure.

We don't need to go deeply into these yet.

---

# 52. What we should remember

The most important idea is:

```text
Application load increases
        ↓
Resource usage increases
        ↓
Metrics Server provides metrics
        ↓
HPA compares usage with target
        ↓
HPA increases replicas
        ↓
Deployment / ReplicaSet
        ↓
More Pods
```

When the load decreases:

```text
Application load decreases
        ↓
Resource usage decreases
        ↓
HPA detects lower usage
        ↓
HPA can reduce replicas
        ↓
Deployment / ReplicaSet
        ↓
Fewer Pods
```

With our configuration:

```text
Minimum = 2
Maximum = 5
Target CPU = 70%
```

So:

```text
2 ≤ backend replicas ≤ 5
```

---

# 53. Final mental model

Remember HPA like this:

```text
                    APPLICATION
                         |
                         ↓
                       PODS
                         |
                   CPU / Memory
                         |
                         ↓
                  METRICS SERVER
                         |
                         ↓
                        HPA
                         |
                  "Do we need
                   more Pods?"
                         |
               +---------+---------+
               |                   |
             YES                   NO
               |                   |
               ↓                   ↓
        Increase replicas     Keep/reduce
               |              replicas
               ↓                   |
          Deployment              |
               |                   |
               ↓                   |
           ReplicaSet              |
               |                   |
               +---------+---------+
                         |
                         ↓
                        Pods
```

The most important distinction is:

```text
HPA
→ Automatically adjusts Pod count

Deployment
→ Manages the application rollout

ReplicaSet
→ Maintains the requested number of Pods

Service
→ Provides stable networking to those Pods

Metrics Server
→ Provides current resource usage metrics
```

---

# 54. Our Kubernetes architecture so far

Our project now contains the following major concepts:

```text
                    KUBERNETES CLUSTER
                           |
                           ↓
                      DEPLOYMENT
                           |
                           ↓
                      REPLICASET
                           |
                           ↓
                         PODS
                           |
             +-------------+-------------+
             |                           |
             ↓                           ↓
          SERVICE                      HPA
             |                           |
             ↓                           ↓
        Stable access              Pod scaling
             |                           |
             ↓                           ↓
        Backend Pods              2 → 5 Pods
             |
             ↓
       PostgreSQL Service
             |
             ↓
       PostgreSQL Pod
             |
             ↓
          PVC
             |
             ↓
           PV
             |
             ↓
     Persistent Storage
```

And the HPA-specific flow is:

```text
Pods
 ↓
Resource usage
 ↓
Metrics Server
 ↓
HPA
 ↓
Desired replica count
 ↓
Deployment
 ↓
ReplicaSet
 ↓
Pods
```

This connects HPA to the desired-state and reconciliation model we learned at the beginning.

The next important concept in our planned notes is **Self-Healing and Rollouts**, where we will connect Pod failures, ReplicaSets, Deployments, rolling updates, rollout history, rollback, and the practical behavior we observed in our project.

```
```
