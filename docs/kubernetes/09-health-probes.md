# 09 — Kubernetes Health Probes

## 1. Why do we need Health Probes?

We already know that Kubernetes manages our Pods and containers.

But Kubernetes needs to answer an important question:

> **"Is my application actually healthy?"**

Consider our backend container.

The container process might still be running:

```text
Container
    ↓
Spring Boot process
    ↓
Running
```

But the application itself could have a problem.

For example:

* The application may have stopped responding.
* The application may be stuck.
* The application may still be starting.
* The application may be running but not ready to receive user traffic.
* A dependency required by the application may not be available.

So simply checking:

```text
Container process = running
```

is not always enough.

Kubernetes provides **health probes** to check the state of an application.

---

# 2. What is a Health Probe?

A health probe is a check performed by Kubernetes to determine the state of an application running inside a container.

Simple definition:

> **A health probe is a Kubernetes health check used to determine whether an application is alive and/or ready to receive traffic.**

The two most important probes for us are:

```text
Liveness Probe
        ↓
"Is the application still alive?"

Readiness Probe
        ↓
"Is the application ready to receive traffic?"
```

There is also:

```text
Startup Probe
```

which is useful for applications that take a long time to start.

---

# 3. Why isn't `Running` enough?

Suppose Kubernetes shows:

```text
NAME       READY   STATUS
backend    1/1     Running
```

This tells us the container is running and currently considered ready.

But imagine the application becomes stuck while the container process continues to exist.

The container may still technically be running:

```text
Container process
       ↓
     Running
```

while the application is not functioning correctly.

Kubernetes therefore needs a way to ask the application:

```text
"Are you alive?"

"Are you ready?"
```

This is where probes are useful.

---

# 4. The two main questions Kubernetes asks

Think about the probes as two different questions.

### Liveness

```text
"Should this container continue running?"
```

### Readiness

```text
"Should this Pod currently receive traffic?"
```

These are different questions.

That distinction is extremely important.

---

# 5. Liveness Probe

A **liveness probe** determines whether the application is still alive.

Simple explanation:

> **Liveness asks whether the application is functioning enough that Kubernetes should keep the container running.**

If the liveness probe repeatedly fails, Kubernetes can restart the container.

Conceptually:

```text
Liveness Probe
       |
       ↓
Is application alive?
       |
   +---+---+
   |       |
  YES      NO
   |       |
   ↓       ↓
Keep     Restart
running  container
```

---

# 6. Why do we need Liveness?

Imagine our Spring Boot application gets stuck.

The process might still exist:

```text
Java process
    ↓
Running
```

but the application isn't responding properly.

Without a liveness probe, Kubernetes might continue seeing:

```text
Container = Running
```

With a liveness probe:

```text
Kubernetes
     |
     ↓
Health check
     |
     ↓
Application not responding
     |
     ↓
Liveness fails
     |
     ↓
Container restarted
```

This provides a form of automatic recovery.

---

# 7. Readiness Probe

A **readiness probe** determines whether the application is ready to receive traffic.

Simple explanation:

> **Readiness asks whether the Pod should currently receive application traffic.**

If readiness fails:

```text
Pod
 ↓
Not Ready
```

The container is **not automatically restarted** just because the readiness probe failed.

Instead, the Pod is removed from the set of ready endpoints used for normal Service traffic.

Conceptually:

```text
Readiness Probe
       |
       ↓
Is application ready?
       |
   +---+---+
   |       |
  YES      NO
   |       |
   ↓       ↓
Traffic   No normal
allowed   traffic
          to this Pod
```

---

# 8. Liveness vs Readiness

This is one of the most important Kubernetes concepts.

| Probe     | Main question                          | If it fails                                            |
| --------- | -------------------------------------- | ------------------------------------------------------ |
| Liveness  | Is the application alive?              | Container can be restarted                             |
| Readiness | Is the application ready for traffic?  | Pod is removed from ready Service endpoints            |
| Startup   | Has the application finished starting? | Prevents liveness/readiness from interfering too early |

The easiest way to remember:

```text
Liveness
→ Keep it alive

Readiness
→ Send traffic or not

Startup
→ Has it finished starting?
```

---

# 9. Our Spring Boot Health Endpoints

Our backend uses Spring Boot Actuator.

We configured:

```text
/actuator/health/liveness
```

for liveness.

And:

```text
/actuator/health/readiness
```

for readiness.

Our Deployment contains:

```yaml
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8080
```

and:

```yaml
readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8080
```

So Kubernetes can make HTTP requests to our Spring Boot application.

---

# 10. How an HTTP Probe works

Our backend listens on:

```text
8080
```

Kubernetes can perform:

```text
HTTP GET
    ↓
http://Pod-IP:8080/actuator/health/liveness
```

If the application responds successfully:

```text
HTTP 200
```

the probe is considered successful.

If the endpoint returns an unsuccessful response such as:

```text
HTTP 404
```

the probe fails.

Conceptually:

```text
kubelet
   |
   | HTTP GET
   ↓
Pod:8080
   |
   ↓
/actuator/health/liveness
   |
   ↓
HTTP response
```

---

# 11. Who performs the probe?

The **kubelet** on the Worker Node performs the health checks for containers running on that node.

Conceptually:

```text
Worker Node
    |
  kubelet
    |
    | health check
    ↓
Backend Pod
    |
    ↓
Spring Boot
```

This connects the Health Probe concept to the Kubernetes architecture we learned earlier.

---

# 12. Types of probes

Kubernetes supports different ways of checking an application.

The important ones to know are:

```text
HTTP probe
TCP probe
Command probe
```

And Kubernetes also provides the concept of a startup probe.

---

# 13. HTTP Probe

An HTTP probe sends an HTTP request to an endpoint.

Example:

```yaml
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8080
```

Kubernetes essentially asks:

```text
GET /actuator/health/liveness
```

If the response indicates success, the probe succeeds.

This is what we use in our Spring Boot application.

---

# 14. TCP Probe

A TCP probe checks whether a TCP connection can be established to a port.

For example:

```yaml
livenessProbe:
  tcpSocket:
    port: 8080
```

This checks whether something is accepting TCP connections on port `8080`.

Conceptually:

```text
kubelet
   |
   | TCP connection
   ↓
Pod:8080
```

TCP probing is useful when the application doesn't expose a convenient HTTP health endpoint.

---

# 15. Command Probe

A command probe executes a command inside the container.

For example:

```yaml
livenessProbe:
  exec:
    command:
      - cat
      - /tmp/healthy
```

Kubernetes executes the command.

If the command succeeds, the probe succeeds.

If the command fails, the probe fails.

Conceptually:

```text
kubelet
   |
   ↓
Execute command inside container
   |
   ↓
Success / Failure
```

We don't use this method for our Spring Boot backend.

---

# 16. Startup Probe

Some applications take a long time to start.

For example:

```text
Application starts
       |
       | 60 seconds
       |
       ↓
Application ready
```

If we immediately configure an aggressive liveness probe, Kubernetes might think:

```text
Application is unhealthy
```

and restart it before it finishes starting.

A **startup probe** can solve this problem.

It asks:

> **"Has the application finished starting?"**

Once the startup probe succeeds, Kubernetes can begin using the liveness and readiness probes normally.

Conceptually:

```text
Container starts
      |
      ↓
Startup Probe
      |
      ↓
Application finishes startup
      |
      ↓
Liveness + Readiness become active
```

For our current Spring Boot application, startup is fast enough that we don't currently need a startup probe.

---

# 17. Probe timing

Our Deployment currently has:

```yaml
initialDelaySeconds: 30
periodSeconds: 10
```

For example:

```yaml
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 10
```

These settings control when and how frequently Kubernetes performs the probe.

---

# 18. `initialDelaySeconds`

Example:

```yaml
initialDelaySeconds: 30
```

This means Kubernetes waits approximately 30 seconds after the container starts before beginning the probe.

Conceptually:

```text
Container starts
      |
      |
   30 seconds
      |
      ↓
Probe begins
```

This gives the application time to start.

---

# 19. `periodSeconds`

Example:

```yaml
periodSeconds: 10
```

This means Kubernetes performs the probe approximately every 10 seconds.

Conceptually:

```text
Probe
  ↓
10 seconds
  ↓
Probe
  ↓
10 seconds
  ↓
Probe
```

---

# 20. `failureThreshold`

A probe doesn't necessarily cause immediate action after one failure.

For example:

```yaml
failureThreshold: 3
```

means Kubernetes allows multiple consecutive failures before considering the probe failed according to the threshold.

Conceptually:

```text
Failure 1
   ↓
Failure 2
   ↓
Failure 3
   ↓
Probe considered failed
```

The exact behavior depends on the probe type and configuration, but the important beginner concept is:

> Kubernetes can require multiple consecutive failures before taking action.

---

# 21. `successThreshold`

Example:

```yaml
successThreshold: 1
```

This controls how many consecutive successful checks are required for the probe to be considered successful.

For many common HTTP liveness/readiness configurations, the default is:

```text
1
```

So one successful check is enough.

---

# 22. `timeoutSeconds`

A probe also has a timeout.

For example:

```yaml
timeoutSeconds: 1
```

This means Kubernetes waits for the probe response for approximately one second before considering that individual check unsuccessful.

Conceptually:

```text
Send probe
    |
    ↓
Wait for response
    |
    +---- Response in time → Success
    |
    +---- Too slow → Failure
```

---

# 23. Our complete probe configuration

Our backend currently has:

```yaml
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 10

readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 10
```

When we ran:

```bash
kubectl describe pod -l app=backend
```

we saw:

```text
Liveness:
http-get http://:8080/actuator/health/liveness

Readiness:
http-get http://:8080/actuator/health/readiness
```

This confirmed that Kubernetes was using the correct endpoints.

---

# 24. What happens when Readiness fails?

We actually tested this in our project.

We deliberately changed the readiness endpoint to:

```text
/actuator/health/does-not-exist
```

This endpoint does not exist.

Kubernetes produced events such as:

```text
Readiness probe failed:
HTTP probe failed with statuscode: 404
```

The Pod became:

```text
0/1
```

but its status remained:

```text
Running
```

This was an important practical observation.

---

# 25. Why was the Pod `Running` but `0/1`?

This confused us initially, but it demonstrates the difference between container state and readiness.

We saw:

```text
READY   STATUS
0/1     Running
```

This means:

```text
Container is running
        +
Pod is not ready
```

So:

```text
Running ≠ Ready
```

A container can be running while the application is not considered ready for traffic.

---

# 26. Readiness failure does not restart the container

When our readiness endpoint returned:

```text
404
```

Kubernetes did not restart the container simply because of that readiness failure.

Instead:

```text
Readiness failure
       ↓
Pod becomes Not Ready
       ↓
Container continues running
```

This is exactly what we wanted to demonstrate.

---

# 27. Why does Readiness affect Services?

Suppose we have:

```text
Backend Pod 1 → Ready
Backend Pod 2 → Not Ready
```

The backend Service should send normal traffic to the ready Pod rather than the Pod that is currently not ready.

Conceptually:

```text
Backend Service
       |
       +------> Pod 1 ✓
       |
       +------> Pod 2 ✗
```

This is why readiness is important for applications behind Services.

---

# 28. What happens when Liveness fails?

We also deliberately changed the liveness endpoint to an invalid endpoint.

For example:

```text
/actuator/health/does-not-exist
```

Kubernetes reported:

```text
Liveness probe failed:
HTTP probe failed with statuscode: 404
```

Then we saw:

```text
Killing
Container backend failed liveness probe, will be restarted
```

This is the practical behavior of a failed liveness probe.

---

# 29. Liveness failure and restart

The flow was:

```text
Liveness Probe
      |
      ↓
HTTP 404
      |
      ↓
Repeated failures
      |
      ↓
Kubernetes kills container
      |
      ↓
Container restarted
```

We then observed the restart count increase.

For example:

```text
RESTARTS
5
```

This showed us that the container was actually being restarted.

---

# 30. CrashLoopBackOff

When we deliberately kept the incorrect liveness probe, our Pods eventually showed:

```text
CrashLoopBackOff
```

For example:

```text
backend-deployment-...   0/1   CrashLoopBackOff
```

This means Kubernetes is repeatedly trying to restart a container that keeps failing.

Conceptually:

```text
Container starts
      ↓
Liveness fails
      ↓
Container restarted
      ↓
Liveness fails again
      ↓
Container restarted
      ↓
Repeated failures
      ↓
CrashLoopBackOff
```

Kubernetes uses increasing delays between restart attempts to avoid continuously restarting the container as fast as possible.

---

# 31. We fixed the liveness probe

After restoring:

```yaml
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8080
```

the Pods returned to:

```text
1/1 Running
```

We verified:

```bash
kubectl get pods -l app=backend
```

and saw:

```text
backend-deployment-...   1/1   Running
backend-deployment-...   1/1   Running
```

We also checked:

```bash
kubectl describe pod -l app=backend
```

and confirmed:

```text
Liveness:
http-get http://:8080/actuator/health/liveness

Readiness:
http-get http://:8080/actuator/health/readiness
```

---

# 32. Health probes and self-healing

Health probes connect directly to Kubernetes self-healing.

For liveness:

```text
Application unhealthy
       ↓
Liveness fails
       ↓
Container restarted
       ↓
Application starts again
```

This is one mechanism through which Kubernetes can recover from application failures.

Readiness works differently:

```text
Application temporarily unavailable
       ↓
Readiness fails
       ↓
Pod stops receiving normal traffic
       ↓
Application can continue running
```

This prevents traffic from being sent to an application that isn't ready.

---

# 33. Health probes and Services

These two concepts work together.

```text
                    Backend Service
                          |
                +---------+---------+
                |                   |
                ↓                   ↓
            Backend Pod 1       Backend Pod 2
                |                   |
          Readiness ✓          Readiness ✗
                |                   |
             Traffic            No normal
             allowed            traffic
```

The Service provides the networking.

The readiness probe tells Kubernetes whether the Pod should currently participate as a ready endpoint.

---

# 34. Health probes and Deployments

Our Deployment defines the probes in the Pod template:

```yaml
spec:
  template:
    spec:
      containers:
        - name: backend

          livenessProbe:
            ...

          readinessProbe:
            ...
```

This means every Pod created by the Deployment gets the same health-check configuration.

Conceptually:

```text
Deployment
     |
     ↓
ReplicaSet
     |
     +---------+---------+
     |                   |
     ↓                   ↓
 Backend Pod 1       Backend Pod 2
     |                   |
 Liveness             Liveness
 Readiness             Readiness
```

---

# 35. Why probes belong to the container configuration

The health check is checking the application running inside the container.

Therefore the probe is defined under:

```yaml
containers:
```

For example:

```yaml
containers:
  - name: backend

    image: cloud-native-task-platform-backend:latest

    livenessProbe:
      ...
```

The probe is associated with that container.

---

# 36. What does HTTP 200 mean?

For an HTTP probe, a successful HTTP response generally means the probe succeeded.

Our Spring Boot endpoint should respond successfully when healthy:

```text
GET /actuator/health/liveness
       ↓
HTTP 200
```

Similarly:

```text
GET /actuator/health/readiness
       ↓
HTTP 200
```

means the corresponding health check is successful.

A response such as:

```text
404
```

means the endpoint was not found and therefore the HTTP probe fails.

---

# 37. Health endpoint vs application endpoint

A health endpoint is designed specifically for health checking.

For example:

```text
/actuator/health/liveness
```

is not the same as:

```text
/api/tasks
```

The health endpoint provides Kubernetes with a simple way to determine application health.

This is why frameworks such as Spring Boot Actuator are useful.

---

# 38. Why not use any normal API endpoint?

Suppose we use:

```text
/api/tasks
```

as the liveness endpoint.

That endpoint might depend on:

```text
Database
Authentication
Other services
```

A temporary database problem could then make Kubernetes think:

```text
"The entire application is dead."
```

and restart the container.

Health endpoints let us define more appropriate health semantics.

For our current project, Spring Boot Actuator provides dedicated:

```text
liveness
readiness
```

health groups.

---

# 39. Liveness should not be too aggressive

A common mistake is to make liveness checks too strict.

Suppose the application takes:

```text
60 seconds
```

to start.

But our liveness probe begins after:

```text
5 seconds
```

and fails repeatedly.

Kubernetes may restart the application before it has even finished starting.

This can produce:

```text
Start
 ↓
Probe fails
 ↓
Restart
 ↓
Start
 ↓
Probe fails
 ↓
Restart
```

A startup probe or appropriate initial delay can prevent this.

This is why probe configuration must consider the application's startup time.

---

# 40. Liveness should represent "restart me"

This is a useful mental model.

Ask:

> "If this check fails, do I really want Kubernetes to restart the application?"

If the answer is yes:

```text
Liveness
```

may be appropriate.

If the application is simply temporarily unable to handle traffic:

```text
Readiness
```

is usually more appropriate.

---

# 41. Readiness should represent "don't send traffic yet"

Ask:

> "If this check fails, should the Pod temporarily stop receiving traffic?"

If yes:

```text
Readiness
```

is the appropriate concept.

For example:

```text
Application starts
       ↓
Not ready
       ↓
Readiness fails
       ↓
No normal traffic
       ↓
Application becomes ready
       ↓
Readiness succeeds
       ↓
Traffic allowed
```

---

# 42. Startup probe mental model

Startup probe is mainly useful for slow-starting applications.

Think:

```text
Container starts
       |
       ↓
"Give me time to start"
       |
       ↓
Startup Probe
       |
       ↓
Startup succeeds
       |
       +------------------+
       |                  |
       ↓                  ↓
Liveness             Readiness
```

We don't currently need this in our project, but understanding its purpose is important.

---

# 43. Common beginner mistakes

## Mistake 1 — Thinking readiness failure restarts the container

Wrong:

```text
Readiness fails
     ↓
Container restarted
```

Correct:

```text
Readiness fails
     ↓
Pod becomes Not Ready
     ↓
Normal Service traffic is avoided
```

---

## Mistake 2 — Thinking liveness only checks whether the process exists

Liveness can check application behavior using an HTTP endpoint, TCP connection, or command.

It is not simply:

```text
"Does the process exist?"
```

---

## Mistake 3 — Using an aggressive liveness probe during startup

This can cause unnecessary restarts.

---

## Mistake 4 — Using readiness as liveness

If the application is temporarily unavailable but can recover by itself, restarting it may be unnecessary.

---

## Mistake 5 — Thinking `Running` means healthy

Remember:

```text
Running
≠
Ready
```

A Pod can be:

```text
0/1 Running
```

because the container is running but the readiness probe is failing.

---

# 44. Useful commands

### View Pods

```bash
kubectl get pods
```

---

### View backend Pods

```bash
kubectl get pods -l app=backend
```

---

### Inspect probes

```bash
kubectl describe pod -l app=backend
```

---

### Filter probe information

```bash
kubectl describe pod -l app=backend | grep -A8 -E "Liveness|Readiness"
```

---

### View events

```bash
kubectl get events --sort-by=.lastTimestamp
```

This is particularly useful when debugging probe failures.

---

### Inspect Deployment configuration

```bash
kubectl get deployment backend-deployment -o yaml
```

---

# 45. How we debugged our probe problem

Our debugging process was important.

First, we saw:

```text
READY
0/1
```

Then we inspected the Pod:

```bash
kubectl describe pod ...
```

We found:

```text
Readiness probe failed
HTTP probe failed with statuscode: 404
```

This told us that the readiness endpoint was wrong.

We then checked the Deployment:

```bash
kubectl get deployment backend-deployment -o yaml
```

and found the incorrect endpoint.

After correcting it, the Pods became:

```text
1/1 Running
```

Later, we deliberately broke the liveness endpoint to understand its behavior.

We observed:

```text
Liveness probe failed
        ↓
Container restarted
        ↓
CrashLoopBackOff
```

Then we restored the correct endpoint.

This practical debugging experience is more valuable than simply memorizing the definitions.

---

# 46. Our actual project configuration

The backend Deployment currently uses:

```yaml
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 10

readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 10
```

The backend application listens on:

```text
8080
```

and Spring Boot Actuator provides:

```text
/actuator/health/liveness
/actuator/health/readiness
```

---

# 47. Complete architecture connection

Now connect everything we have learned so far:

```text
                         Kubernetes
                              |
                         Deployment
                              |
                         ReplicaSet
                              |
                    +---------+---------+
                    |                   |
                    ↓                   ↓
               Backend Pod 1       Backend Pod 2
                    |                   |
              Liveness ✓          Liveness ✓
              Readiness ✓         Readiness ✓
                    |                   |
                    +---------+---------+
                              |
                         Backend Service
                              |
                              ↓
                           Ingress
                              |
                              ↓
                              User
```

Health probes determine whether the Pods are healthy and ready.

The Service provides stable networking.

The Deployment and ReplicaSet manage the Pods.

The Ingress provides external HTTP routing.

---

# 48. Questions that naturally arise

We now understand:

```text
Liveness
Readiness
Startup
HTTP/TCP/Exec probes
Probe timing
Probe failures
Service interaction
```

Naturally, we may ask:

### Question 1

How does external traffic enter the cluster?

→ **Ingress**

### Question 2

Where does PostgreSQL data live if its Pod is deleted?

→ **Persistent Volumes and PVCs**

### Question 3

How does Kubernetes automatically increase replicas when CPU usage increases?

→ **Horizontal Pod Autoscaler**

### Question 4

How does Kubernetes replace failed Pods and perform updates?

→ **Self-healing and Rollouts**

These are covered in the next concept notes.

---

# 49. Simple interview explanations

## What is a liveness probe?

> "A liveness probe checks whether an application is still functioning. If it repeatedly fails, Kubernetes can restart the container."

---

## What is a readiness probe?

> "A readiness probe checks whether an application is ready to receive traffic. If it fails, Kubernetes considers the Pod not ready and prevents normal Service traffic from being sent to it."

---

## What is the difference between liveness and readiness?

> "Liveness determines whether the container should continue running, while readiness determines whether the application should currently receive traffic."

---

## What is a startup probe?

> "A startup probe is used for applications that take a long time to start. It gives the application time to initialize before liveness and readiness checks become effective."

---

## How does Kubernetes perform an HTTP health check?

> "The kubelet sends an HTTP request to the configured health endpoint on the container. A successful response means the probe succeeds; an unsuccessful response causes the probe to fail."

---

## What happens when a readiness probe fails?

> "The Pod becomes not ready and is removed from the set of ready Service endpoints. The container is not restarted just because of the readiness failure."

---

## What happens when a liveness probe fails?

> "After the configured failure threshold is reached, Kubernetes restarts the affected container."

---

## Who performs the probes?

> "The kubelet on the Worker Node performs the health checks for the containers running on that node."

---

# 50. What we should understand instead of memorizing

Remember the two questions:

```text
Liveness:
"Should Kubernetes restart this container?"

Readiness:
"Should this Pod receive traffic?"
```

And:

```text
Startup:
"Has this application finished starting?"
```

The core flow is:

```text
                    Container
                        |
                        ↓
                     kubelet
                        |
             +----------+----------+
             |                     |
             ↓                     ↓
         Liveness              Readiness
             |                     |
       "Alive?"              "Ready?"
             |                     |
             ↓                     ↓
       Restart if            Allow/remove
       repeatedly fails      from traffic
```

---

# 51. Final mental model

For our project, remember:

```text
                         Backend Pod
                              |
                    +---------+---------+
                    |                   |
                    ↓                   ↓
               Liveness Probe     Readiness Probe
                    |                   |
             "Is it alive?"      "Can it receive
                                  traffic?"
                    |                   |
                    ↓                   ↓
              Restart if          Service uses
              repeatedly fails     only ready Pods
```

And when the application starts slowly:

```text
Container starts
       ↓
Startup Probe
       ↓
Application ready
       ↓
Liveness + Readiness
```

The most important distinction is:

```text
Liveness failure
      ↓
Container restart


Readiness failure
      ↓
Pod not ready
      ↓
No normal Service traffic


Startup probe
      ↓
Protects slow-starting applications
```

---

# 52. Summary

The important concepts we learned are:

```text
Health Probe
→ Kubernetes check used to determine application health


Liveness Probe
→ Determines whether the container should continue running


Readiness Probe
→ Determines whether the Pod should receive traffic


Startup Probe
→ Gives slow-starting applications time to initialize


HTTP Probe
→ Checks an HTTP endpoint


TCP Probe
→ Checks whether a TCP connection can be established


Exec Probe
→ Executes a command inside the container


initialDelaySeconds
→ How long to wait before starting probes


periodSeconds
→ How frequently to perform the probe


timeoutSeconds
→ How long an individual probe can wait


failureThreshold
→ Number of consecutive failures before the probe is considered failed


successThreshold
→ Number of consecutive successes required for the probe to succeed
```

For our application:

```text
Spring Boot
    |
    +── /actuator/health/liveness
    |
    +── /actuator/health/readiness
```

Kubernetes checks them through the kubelet:

```text
kubelet
    |
    +── Liveness → application alive?
    |
    +── Readiness → application ready?
```

And our practical experiment proved the difference:

```text
Readiness 404
    ↓
0/1 Running
    ↓
Container NOT restarted


Liveness 404
    ↓
Container killed
    ↓
Container restarted
    ↓
Repeated failures
    ↓
CrashLoopBackOff
```

The main idea to remember is:

> **Liveness protects the application by allowing Kubernetes to restart an unhealthy container, while readiness protects traffic by preventing requests from being sent to a Pod that is not ready.**
