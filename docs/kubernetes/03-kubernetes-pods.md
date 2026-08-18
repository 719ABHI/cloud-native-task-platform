# Kubernetes Pods

## 1. What is a Pod?

A **Pod** is the smallest deployable unit in Kubernetes.

Kubernetes does not normally deploy our application as a bare container.

Instead, Kubernetes runs containers inside Pods.

The basic relationship is:

Pod
  ↓
Container
  ↓
Application

For our backend:

Backend Pod
  ↓
Spring Boot container
  ↓
Task Management application

In our project, each backend Pod currently contains one container.

---

## 2. Why does Kubernetes use Pods?

At first, this can seem unnecessary.

We already know:

```text
Docker
  ↓
Container
````

So why does Kubernetes introduce another object called a Pod?

The important idea is that Kubernetes needs a unit that it can manage as a workload.

A Pod provides a wrapper around one or more closely related containers.

Kubernetes can therefore manage:

* The container or containers inside the Pod
* Networking for the Pod
* Storage attached to the Pod
* Pod-level configuration
* Health checks
* Pod lifecycle

So instead of thinking:

```text
Kubernetes
    ↓
Container
```

think:

```text
Kubernetes
    ↓
Pod
    ↓
Container
```

---

## 3. Pod vs Container

A common beginner mistake is to think:

> "A Pod is just another name for a container."

It is not.

They are different concepts.

### Container

A container is where our application process actually runs.

For example:

```text
Container
   ↓
Spring Boot application
```

### Pod

A Pod is the Kubernetes unit that contains and manages one or more containers.

```text
Pod
  ↓
Container
  ↓
Spring Boot application
```

Therefore:

```text
Container
→ runs the application

Pod
→ provides the Kubernetes environment around the container
```

---

## 4. A Pod can contain multiple containers

A Pod can contain one or more containers.

For example:

```text
Pod
├── Application container
└── Helper container
```

The containers inside the same Pod are closely related and share certain resources.

However, for most simple applications, using one main container per Pod is common.

Our project follows this approach:

```text
Backend Pod
    ↓
Backend container
    ↓
Spring Boot application
```

We do not need multiple containers inside one Pod for our current backend.

---

## 5. Why don't we put all application containers into one Pod?

Suppose our application contains:

```text
Frontend
Backend
PostgreSQL
```

We could technically put multiple containers into one Pod.

But that would usually be a poor design for our application.

We want Kubernetes to manage them independently.

For example:

```text
Frontend
    ↓
Frontend Pods

Backend
    ↓
Backend Pods

PostgreSQL
    ↓
PostgreSQL Pod
```

This allows us to:

* Scale the backend independently
* Scale the frontend independently
* Update the backend independently
* Restart a backend workload without recreating the frontend
* Manage different components separately

Therefore, we generally use separate Pods for independently managed application components.

---

## 6. Our backend Pod

Our backend Deployment creates Pods containing our Spring Boot application.

Our Pod looks conceptually like:

```text
backend-deployment
        ↓
    ReplicaSet
        ↓
    Backend Pod
        ↓
Backend container
        ↓
Spring Boot application
```

The container uses our Docker image:

```text
cloud-native-task-platform-backend:latest
```

So the image is used to create the container inside the Pod.

The relationship is:

```text
Docker image
      ↓
Container
      ↓
Pod
```

More accurately, Kubernetes creates the Pod and the container runtime uses the specified image to create the container inside that Pod.

---

## 7. Pod IP address

Pods receive their own IP addresses inside the Kubernetes cluster.

For example, when we ran:

```bash
kubectl describe pod
```

we saw information such as:

```text
IP: 10.244.0.254
```

That means the Pod had its own network identity inside the cluster.

Conceptually:

```text
Backend Pod
IP: 10.244.0.254
```

Another Pod might have:

```text
Backend Pod
IP: 10.244.0.255
```

Each Pod can therefore communicate over the Kubernetes cluster network.

---

## 8. Why can't we depend on Pod IPs?

This is very important.

Pod IPs are not permanent.

Suppose we have:

```text
Backend Pod 1
IP: 10.244.0.10
```

Now the Pod is deleted.

Kubernetes creates a replacement:

```text
Backend Pod 2
IP: 10.244.0.25
```

The application has changed from:

```text
10.244.0.10
```

to:

```text
10.244.0.25
```

Therefore, we should not normally hard-code Pod IP addresses.

This is one of the reasons Kubernetes Services exist.

We will study Services separately.

The important idea for now is:

```text
Pod IP
→ temporary

Service
→ stable way to access Pods
```

---

## 9. How Pods get created in our project

We normally don't manually create backend Pods.

We created a Deployment:

```text
backend-deployment
```

The Deployment manages the desired number of replicas.

For example:

```yaml
spec:
  replicas: 2
```

This means:

> "I want two backend Pods."

The simplified relationship is:

```text
Deployment
    ↓
ReplicaSet
    ↓
Pods
```

So when we say:

```text
replicas: 2
```

Kubernetes eventually creates:

```text
Backend Pod 1
Backend Pod 2
```

We will study the Deployment and ReplicaSet relationship in detail in:

```text
04-deployments-and-replicasets.md
```

---

## 10. What happens when a Pod is deleted?

Suppose our Deployment wants:

```text
2 Pods
```

Initially:

```text
Pod 1
Pod 2
```

Now suppose Pod 1 is deleted.

The actual state becomes:

```text
Desired = 2
Actual  = 1
```

Kubernetes detects that the desired state is no longer satisfied.

The controller works to create a replacement Pod.

Eventually:

```text
Pod 2
Pod 3
```

and:

```text
Desired = 2
Actual  = 2
```

This is part of Kubernetes' self-healing behavior.

The important point is:

> We normally don't manually recreate the replacement Pod when it is managed by a Deployment.

Kubernetes does that for us.

---

## 11. What happens when a Pod's container crashes?

A Pod can contain a container running our application.

Suppose our Spring Boot process crashes.

Kubernetes can detect the container failure and the kubelet/container runtime can restart the container depending on the Pod's restart behavior.

This is different from the case where the entire Pod is replaced by a Deployment/ReplicaSet.

So there are two related ideas:

```text
Container problem
    ↓
Container may be restarted
```

and:

```text
Pod disappears
    ↓
Deployment/ReplicaSet can create a replacement Pod
```

This distinction becomes important when learning health probes and self-healing.

---

## 12. Pod lifecycle — beginner level

A Pod goes through different lifecycle phases.

The important phases for beginners are:

```text
Pending
Running
Succeeded
Failed
Unknown
```

### Pending

The Pod has been accepted by Kubernetes but is not yet fully running.

For example, Kubernetes may still need to:

* Schedule the Pod
* Pull an image
* Create the container

Conceptually:

```text
Pod created
    ↓
Pending
```

### Running

The Pod has been scheduled and at least one of its containers is running.

For our healthy backend:

```text
READY   STATUS
1/1     Running
```

### Succeeded

The containers in the Pod completed successfully and will not be restarted.

This is more common for short-lived workloads such as Jobs.

### Failed

The containers in the Pod terminated and at least one container failed.

### Unknown

Kubernetes cannot determine the current state of the Pod, usually because it cannot communicate properly with the node.

For our continuously running backend application, the state we normally want to see is:

```text
Running
```

with:

```text
READY
1/1
```

---

## 13. What does `1/1` mean?

We repeatedly saw:

```text
READY   STATUS
1/1     Running
```

The `1/1` refers to containers.

It means:

```text
1 container is ready
out of
1 container in the Pod
```

For our backend:

```text
Pod
 └── Backend container
```

So:

```text
1/1
```

means the backend container is ready.

If we had a Pod containing two containers, we might see:

```text
2/2
```

if both containers were ready.

---

## 14. `Running` does not always mean `Ready`

This is a very important concept we observed practically.

We intentionally changed a readiness probe to an invalid endpoint.

The Pod showed:

```text
READY   STATUS
0/1     Running
```

This initially looks confusing.

How can it be:

```text
Running
```

but:

```text
0/1
```

?

Because these represent different things.

### Running

The container process is running.

### Ready

The container has passed the readiness requirements and is considered ready to receive normal traffic.

So:

```text
0/1 Running
```

can mean:

```text
Container is running
        ↓
Readiness check is failing
        ↓
Pod is not ready for traffic
```

This is why readiness probes are important.

We will document probes separately.

---

## 15. Pod readiness and traffic

Suppose we have:

```text
Backend Pod 1 → Ready
Backend Pod 2 → Not Ready
```

A Service should normally send traffic only to Pods that are considered ready.

Conceptually:

```text
Backend Service
       |
       +------------------+
       |                  |
       ↓                  ↓
   Pod 1               Pod 2
   Ready              Not Ready
     ↑
 Traffic
```

Therefore, readiness is closely connected to Services.

We will study the complete behavior in the Services and Health Probes notes.

---

## 16. Pod networking

Every Pod receives its own IP address.

For example:

```text
Pod A
10.244.0.10

Pod B
10.244.0.11
```

Kubernetes networking is designed so that Pods can communicate with other Pods across the cluster.

At a high level:

```text
Pod A
  |
  | network request
  ↓
Pod B
```

However, applications should generally not depend directly on Pod IPs for stable communication.

Instead:

```text
Application
    ↓
Service
    ↓
Pods
```

The detailed networking model will be covered separately.

---

## 17. Containers inside the same Pod

Containers in the same Pod share the Pod's network namespace.

For beginner-level understanding, the important consequence is that containers in the same Pod can communicate with each other through the Pod's network.

They also share certain resources, such as volumes attached to the Pod.

Conceptually:

```text
             Pod
              |
       +------+------+
       |             |
 Container A     Container B
       |             |
       +------+------+
              |
       Shared Pod network
```

This is one reason multiple containers can be placed in the same Pod when they are tightly coupled.

For our project, we use one main application container per Pod.

---

## 18. Pod storage

Pods can use volumes for storage.

For example:

```text
Pod
 |
 +── Container
 |
 └── Volume
```

A volume can provide storage that is shared between containers in the same Pod or mounted into a container.

However, Pod-local storage and persistent storage are different concepts.

For example, our PostgreSQL data uses a PersistentVolumeClaim:

```text
PostgreSQL Pod
      ↓
PersistentVolumeClaim
      ↓
Persistent Storage
```

We will study Persistent Volumes and PVCs separately.

For now, remember:

> A Pod can have volumes attached to it, but a Pod itself should not be treated as permanent storage.

---

## 19. Pod labels

Pods can have labels.

Our backend Pods use:

```yaml
labels:
  app: backend
```

Labels are key-value pairs used to identify and organize Kubernetes resources.

For example:

```text
app=backend
```

We can then query Pods using:

```bash
kubectl get pods -l app=backend
```

We used exactly this command throughout the project:

```bash
kubectl get pods -l app=backend
```

It means:

> "Show me Pods whose label matches `app=backend`."

---

## 20. Why are labels important?

Labels allow Kubernetes objects to identify related resources.

For example:

```text
Backend Pods
labels:
  app=backend
```

A Service can use:

```yaml
selector:
  app: backend
```

to find those Pods.

A Deployment also uses labels to identify the Pods it manages.

So labels form an important connection between Kubernetes objects.

Conceptually:

```text
Deployment
     |
     | manages
     ↓
Pods
     |
     | label
     ↓
app=backend
     ↑
     |
Service selector
```

We will explore this relationship more deeply when studying Deployments and Services.

---

## 21. Pod names

Pods created by a Deployment have generated names.

For example, we saw:

```text
backend-deployment-64d6ff7d46-bgfjs
backend-deployment-64d6ff7d46-mpxdb
```

The names contain generated parts.

We should not build our application configuration around these names.

Why?

Because Pods are replaceable.

If a Pod is recreated, its name can change:

```text
Old:
backend-deployment-64d6ff7d46-bgfjs

New:
backend-deployment-64d6ff7d46-x7abc
```

The application should rely on stable Kubernetes abstractions such as Services rather than individual Pod names.

---

## 22. Pod IP vs Service IP

This distinction is extremely important.

### Pod IP

```text
Backend Pod
    ↓
10.244.x.x
```

Characteristics:

* Belongs to an individual Pod
* Can change when the Pod is recreated
* Used for Pod-level networking

### Service ClusterIP

```text
backend-service
    ↓
10.111.127.205
```

Characteristics:

* Belongs to the Service
* Provides a stable endpoint
* Represents a group of Pods
* Used for application-to-application communication

So:

```text
Pod IP
→ individual and temporary

Service IP
→ stable endpoint for a group of Pods
```

---

## 23. What happens when we scale Pods?

Suppose we have:

```text
replicas: 2
```

Then:

```text
Backend Pod 1
Backend Pod 2
```

If we change it to:

```text
replicas: 5
```

Kubernetes creates additional Pods.

Eventually:

```text
Backend Pod 1
Backend Pod 2
Backend Pod 3
Backend Pod 4
Backend Pod 5
```

Each Pod has its own IP.

The Service can continue providing one stable endpoint for all of them.

So:

```text
                 backend-service
                       |
        +--------------+--------------+
        |       |       |       |      |
        ↓       ↓       ↓       ↓      ↓
      Pod 1   Pod 2   Pod 3   Pod 4  Pod 5
```

This is one of the important reasons Pods and Services are used together.

---

## 24. Our practical Pod scaling experiment

We tested scaling our backend using:

```bash
kubectl scale deployment backend-deployment --replicas=0
```

Then:

```bash
kubectl get pods -l app=backend
```

showed:

```text
No resources found in default namespace.
```

This demonstrated that the Deployment's desired replica count had become:

```text
0
```

Then we scaled it back:

```bash
kubectl scale deployment backend-deployment --replicas=2
```

Kubernetes created two new backend Pods.

We verified them using:

```bash
kubectl get pods -l app=backend
```

and saw:

```text
backend-deployment-...   1/1   Running
backend-deployment-...   1/1   Running
```

This was a practical demonstration of:

```text
Desired replicas
        ↓
Kubernetes
        ↓
Pods
```

---

## 25. Our practical image experiment

We also rebuilt our backend Docker image.

We used:

```text
cloud-native-task-platform-backend:latest
```

We then removed the old image from Minikube and loaded the newly built image:

```bash
minikube image rm cloud-native-task-platform-backend:latest
```

and:

```bash
minikube image load cloud-native-task-platform-backend:latest
```

Then we started the backend Pods again.

We verified which image was actually running inside the Pods using:

```bash
kubectl get pods -l app=backend \
-o jsonpath='{range .items[*]}{.metadata.name}{" -> "}{.status.containerStatuses[0].imageID}{"\n"}{end}'
```

We saw the image ID:

```text
docker://sha256:...
```

This confirmed which image the running containers were using.

This is important because the image tag:

```text
latest
```

is only a tag.

The actual image identity is represented by its image digest/ID.

---

## 26. Pod health probes

Our backend Pods have health probes.

We configured:

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

The kubelet performs these checks.

We verified the configuration using:

```bash
kubectl describe pod -l app=backend
```

and saw:

```text
Liveness:
http-get http://:8080/actuator/health/liveness

Readiness:
http-get http://:8080/actuator/health/readiness
```

The detailed behavior of these probes belongs in our separate:

```text
09-health-probes.md
```

For this Pod note, the important thing is:

```text
Pod
 ↓
Container
 ↓
Health probes
 ↓
Kubelet observes application health
```

---

## 27. What we learned from deliberately breaking readiness

We changed the readiness endpoint to an invalid path:

```text
/actuator/health/does-not-exist
```

The Kubernetes event showed:

```text
Readiness probe failed
HTTP probe failed with statuscode: 404
```

The Pod remained:

```text
0/1 Running
```

This taught us an important distinction:

```text
Running
→ container process is running

Ready
→ application is considered ready to receive traffic
```

A readiness failure does not automatically mean:

```text
restart container
```

It means the Pod should not be treated as ready for normal traffic.

---

## 28. What we learned from deliberately breaking liveness

We also tested an invalid liveness endpoint.

The Kubernetes events showed:

```text
Liveness probe failed
HTTP probe failed with statuscode: 404
```

Then Kubernetes reported:

```text
Killing
Container backend failed liveness probe, will be restarted
```

The Pod eventually showed restarts and, during repeated failures, we observed:

```text
CrashLoopBackOff
```

This demonstrated:

```text
Liveness failure
      ↓
Container restarted
```

while:

```text
Readiness failure
      ↓
Pod becomes Not Ready
```

This practical experiment will be documented fully in the health-probes note.

---

## 29. Pod vs Deployment

Another important distinction:

### Pod

Represents the running workload unit.

```text
Pod
 ↓
Container
```

### Deployment

Manages the desired state of the application.

```text
Deployment
 ↓
ReplicaSet
 ↓
Pods
```

We should not normally manually manage individual Pods when they are created by a Deployment.

For example, instead of repeatedly creating Pods ourselves, we define:

```yaml
replicas: 2
```

and let Kubernetes maintain that desired state.

---

## 30. Pod vs ReplicaSet

### Pod

Runs the actual application container.

### ReplicaSet

Maintains the required number of Pods.

For example:

```text
ReplicaSet
   |
   +---- Pod
   |
   +---- Pod
```

If one Pod disappears:

```text
ReplicaSet
   |
   +---- Pod
   |
   +---- Missing
```

The ReplicaSet works to create a replacement.

The Deployment normally manages the ReplicaSet.

The complete hierarchy is:

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

## 31. Pod lifecycle in our application

For our continuously running backend, the typical lifecycle looks roughly like:

```text
Deployment
    ↓
Pod created
    ↓
Pending
    ↓
Container starts
    ↓
Running
    ↓
Readiness checks pass
    ↓
Ready
```

If the container becomes unhealthy:

```text
Running
    ↓
Liveness failure
    ↓
Container restart
```

If the Pod itself is removed:

```text
Pod removed
    ↓
ReplicaSet notices
    ↓
Replacement Pod created
```

This gives us a useful mental model of how Pods behave in a Deployment.

---

## 32. How a Pod fits into our complete architecture

We learned earlier about the Kubernetes architecture:

```text
Kubernetes Cluster
       |
       +----------------------+
       |                      |
Control Plane             Worker Node
                              |
                             Pod
                              |
                          Container
```

The Pod is therefore part of the Worker Node's workload.

For our backend:

```text
Kubernetes Cluster
       |
       ↓
Worker Node
       |
       ↓
Backend Pod
       |
       ↓
Spring Boot Container
       |
       ↓
Task Management Backend
```

The Control Plane manages the desired state, while the Worker Node runs the Pod.

---

## 33. Our actual backend Pods

At different points during our project, we saw Pods such as:

```text
backend-deployment-64d6ff7d46-bgfjs
backend-deployment-64d6ff7d46-mpxdb
```

We verified that they were:

```text
READY   STATUS
1/1     Running
```

We also checked their image IDs:

```bash
kubectl get pods -l app=backend \
-o jsonpath='{range .items[*]}{.metadata.name}{" -> "}{.status.containerStatuses[0].imageID}{"\n"}{end}'
```

This allowed us to confirm that both running backend Pods were using the same image.

---

## 34. Important beginner mental model

Do not think:

```text
Pod = permanent server
```

Instead, think:

```text
Pod = replaceable application workload
```

A Pod can be:

* Created
* Started
* Stopped
* Restarted
* Deleted
* Recreated

Its:

* Name can change
* IP can change
* Instance can disappear

Therefore, Kubernetes applications should be designed around higher-level abstractions such as:

```text
Deployment
Service
Ingress
Persistent Storage
```

rather than depending on individual Pods.

---

## 35. Common beginner misunderstandings

### "Pod is the same as a Docker container."

Not exactly.

```text
Pod
 ↓
Container
```

The Pod is the Kubernetes workload unit that contains one or more containers.

---

### "A Pod always contains only one container."

No.

A Pod can contain multiple containers.

However, our project uses one main container per Pod.

---

### "Pod IP is permanent."

No.

Pod IPs can change when Pods are recreated.

---

### "If a Pod is deleted, the application is permanently gone."

Not when the Pod is managed by a Deployment/ReplicaSet.

Kubernetes can create a replacement Pod to satisfy the desired state.

---

### "`Running` means the application is ready."

Not necessarily.

A Pod can be:

```text
0/1 Running
```

when its container is running but its readiness probe is failing.

---

### "We should manually create backend Pods."

Normally no.

We use a Deployment:

```text
Deployment
 ↓
ReplicaSet
 ↓
Pods
```

---

### "The Pod stores our permanent database data."

Not by itself.

Persistent database data should use persistent storage.

Our PostgreSQL setup uses a PVC.

---

## 36. Interview explanations

### What is a Pod?

> "A Pod is the smallest deployable unit in Kubernetes. It contains one or more containers that are managed together. In our project, each backend Pod contains one Spring Boot container."

### Why does Kubernetes use Pods instead of directly managing containers?

> "A Pod provides a Kubernetes-level unit for managing one or more closely related containers, including their networking, storage and lifecycle."

### Can a Pod contain multiple containers?

> "Yes. A Pod can contain multiple closely related containers that share certain resources. In our project, we use one main container per Pod."

### Can Pod IPs change?

> "Yes. Pods are replaceable, so their IP addresses can change when they are recreated. That's why applications normally communicate through Services instead of depending on Pod IPs."

### What happens when a Pod fails?

> "If the Pod is managed by a Deployment or ReplicaSet, Kubernetes detects that the actual number of Pods is lower than the desired number and creates a replacement."

### What is the difference between a Pod and a container?

> "A container runs the application process, while a Pod is the Kubernetes unit that contains and manages one or more containers."

### What does `1/1 Running` mean?

> "It means the Pod has one container and that one container is currently ready, while the Pod itself is running."

### Can a Pod be Running but not Ready?

> "Yes. For example, if the container is running but its readiness probe fails, the Pod can show `0/1 Running`. The container is running, but Kubernetes does not consider it ready to receive traffic."

---

## 37. What we should remember

The most important concepts are:

```text
Pod
→ Smallest deployable Kubernetes unit


Container
→ Runs our application


Pod IP
→ Network address of a Pod, but not permanent


Labels
→ Identify Pods and other resources


Pod lifecycle
→ Pending → Running → Succeeded/Failed


Deployment
→ Manages Pods through ReplicaSets


ReplicaSet
→ Maintains the desired number of Pods


Readiness
→ Determines whether the Pod is ready for traffic


Liveness
→ Helps Kubernetes detect when a container needs restarting
```

The most important relationship is:

```text
Deployment
    ↓
ReplicaSet
    ↓
Pod
    ↓
Container
    ↓
Application
```

---

## 38. What we actually practiced

In our Cloud Native Task Platform, we:

* Created backend Pods through a Deployment
* Ran multiple backend replicas
* Used labels such as `app=backend`
* Queried Pods using label selectors
* Observed Pod IP addresses
* Scaled the backend to zero replicas
* Observed the backend Pods disappear
* Scaled the backend back to two replicas
* Observed Kubernetes create new Pods
* Rebuilt the backend Docker image
* Loaded the image into Minikube
* Used `imagePullPolicy: Never`
* Verified the actual image ID running inside Pods
* Added liveness probes
* Added readiness probes
* Intentionally broke the readiness endpoint
* Observed `0/1 Running`
* Confirmed that readiness failure did not restart the container
* Intentionally broke the liveness endpoint
* Observed the container being restarted
* Observed `CrashLoopBackOff` during repeated liveness failures
* Restored the correct health endpoints
* Verified the Pods returned to `1/1 Running`

These practical experiments helped us understand that a Pod is not simply a container.

It is a Kubernetes-managed workload unit whose lifecycle is controlled by Kubernetes.

---

## 39. Questions that naturally lead to the next concept

Now that we understand Pods, several questions arise naturally.

### Question 1

If Pods are replaceable, who makes sure the correct number of Pods exists?

→ **Deployments and ReplicaSets**

### Question 2

If Pod IPs can change, how does another application reliably communicate with them?

→ **Services**

### Question 3

How can an application use `backend-service` instead of an IP address?

→ **Kubernetes DNS and CoreDNS**

### Question 4

How does traffic reach a specific Pod behind a Service?

→ **Service selectors and Kubernetes networking**

### Question 5

How does Kubernetes know whether a Pod should receive traffic?

→ **Readiness probes**

### Question 6

How does Kubernetes know whether a container should be restarted?

→ **Liveness probes**

These concepts will be covered separately rather than putting everything into this Pod note.

---

# Final Mental Model

Think of a Pod as:

```text
                KUBERNETES
                    |
                    ↓
                   POD
                    |
              +-----+-----+
              |           |
          Container    Container
          (optional)   (optional)
              |
              ↓
         Application
```

For our project:

```text
             Backend Pod
                  |
                  ↓
       Spring Boot Container
                  |
                  ↓
       Task Management Backend
```

And when managed by a Deployment:

```text
Deployment
    |
    ↓
ReplicaSet
    |
    +-------------+
    |             |
    ↓             ↓
 Backend Pod   Backend Pod
    |             |
    ↓             ↓
Container      Container
    |             |
    +------ + ----+
           |
      Spring Boot
```

The key idea to remember is:

> **A Pod is the Kubernetes unit used to run and manage one or more closely related containers. Pods are replaceable, so we should not depend on their names or IP addresses as permanent identities.**

```
```
