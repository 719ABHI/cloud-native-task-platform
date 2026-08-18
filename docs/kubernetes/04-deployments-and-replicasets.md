# Kubernetes Deployments and ReplicaSets

## 1. Why do we need Deployments?

We already learned about Pods.

A Pod runs our application:

```text
Pod
 ↓
Container
 ↓
Application
````

But we normally don't want to manually create and manage individual Pods.

Imagine our backend needs:

```text
Backend Pod 1
Backend Pod 2
Backend Pod 3
Backend Pod 4
Backend Pod 5
```

Now imagine:

* Pod 2 crashes
* We need 5 replicas instead of 2
* We release a new version of the application
* We want to roll back to the previous version
* We want Kubernetes to maintain the correct number of Pods

Managing all of this manually would be difficult.

This is where **Deployments and ReplicaSets** come in.

---

# 2. Simple idea

The basic relationship is:

```text
Deployment
     ↓
ReplicaSet
     ↓
Pods
     ↓
Containers
```

Each object has a different responsibility.

### Deployment

Manages the desired state and updates of our application.

### ReplicaSet

Maintains the required number of Pods.

### Pod

Runs our application containers.

A simple way to remember it:

```text
Deployment
→ "What version and how many replicas should I have?"

ReplicaSet
→ "Make sure that number of Pods exists."

Pod
→ "Run the application."
```

---

# 3. What is a Deployment?

A **Deployment** is a Kubernetes object used to manage a set of application Pods.

For example, our backend Deployment contains:

```yaml
spec:
  replicas: 2
```

This means:

> "I want two backend Pods."

It also specifies things such as:

```yaml
image: cloud-native-task-platform-backend:latest
```

and:

```yaml
ports:
  - containerPort: 8080
```

and our health probes:

```yaml
livenessProbe:
  ...

readinessProbe:
  ...
```

So the Deployment describes the desired configuration of our application.

---

# 4. Our backend Deployment

Our project contains:

```text
backend-deployment
```

We can see it using:

```bash
kubectl get deployments
```

We previously got output similar to:

```text
NAME                  READY   UP-TO-DATE   AVAILABLE
backend-deployment    2/2     2            2
```

This tells us that the Deployment currently has two desired and available replicas.

Our Deployment YAML contains:

```yaml
apiVersion: apps/v1
kind: Deployment

metadata:
  name: backend-deployment

spec:
  replicas: 2

  selector:
    matchLabels:
      app: backend

  template:
    metadata:
      labels:
        app: backend

    spec:
      containers:
        - name: backend
          image: cloud-native-task-platform-backend:latest
```

There are several important parts here.

---

# 5. `kind: Deployment`

This tells Kubernetes what type of resource we are defining.

```yaml
kind: Deployment
```

We are saying:

> "This YAML describes a Deployment."

Kubernetes has many resource types, such as:

```text
Pod
Deployment
Service
ConfigMap
Secret
Ingress
PersistentVolumeClaim
```

For this file:

```yaml
kind: Deployment
```

means Kubernetes should create/manage a Deployment.

---

# 6. `metadata.name`

Our Deployment has:

```yaml
metadata:
  name: backend-deployment
```

This gives the Deployment its name.

We can use this name with commands such as:

```bash
kubectl get deployment backend-deployment
```

or:

```bash
kubectl describe deployment backend-deployment
```

or:

```bash
kubectl scale deployment backend-deployment --replicas=2
```

---

# 7. `replicas`

One of the most important fields is:

```yaml
spec:
  replicas: 2
```

This represents the desired number of Pods.

We are telling Kubernetes:

```text
I want:
2 backend Pods
```

Kubernetes then continuously works to maintain that state.

So:

```text
Desired State
    ↓
2 Pods
```

If there are only:

```text
Actual State
    ↓
1 Pod
```

Kubernetes works to create another Pod.

Eventually:

```text
Desired = 2
Actual  = 2
```

---

# 8. What happens when we change replicas?

Suppose we currently have:

```yaml
replicas: 2
```

Kubernetes maintains:

```text
Pod 1
Pod 2
```

If we change it to:

```yaml
replicas: 5
```

Kubernetes creates additional Pods until:

```text
Pod 1
Pod 2
Pod 3
Pod 4
Pod 5
```

If we change it to:

```yaml
replicas: 0
```

Kubernetes removes the running Pods managed by that Deployment.

We actually tested this in our project.

We ran:

```bash
kubectl scale deployment backend-deployment --replicas=0
```

Then:

```bash
kubectl get pods -l app=backend
```

and received:

```text
No resources found in default namespace.
```

Then we scaled it back:

```bash
kubectl scale deployment backend-deployment --replicas=2
```

Kubernetes created two new Pods.

This was a practical demonstration of the desired-state model.

---

# 9. Declarative configuration

Deployments are part of Kubernetes' declarative approach.

Instead of saying:

```text
Create Pod 1
Create Pod 2
Restart Pod 1 if it crashes
Create another Pod if Pod 2 disappears
```

we say:

```yaml
replicas: 2
```

We describe what we want.

Kubernetes determines what actions are required.

This is:

```text
Desired State
      ↓
Kubernetes
      ↓
Actual State
```

and Kubernetes continuously tries to make:

```text
Desired State = Actual State
```

---

# 10. What is a ReplicaSet?

A **ReplicaSet** is responsible for maintaining the desired number of Pods.

For example:

```text
ReplicaSet
    |
    +---- Pod
    |
    +---- Pod
```

If the ReplicaSet requires:

```text
2 Pods
```

but only one exists:

```text
Desired = 2
Actual  = 1
```

the ReplicaSet works to create another Pod.

Eventually:

```text
Desired = 2
Actual  = 2
```

---

# 11. Deployment → ReplicaSet → Pods

This hierarchy is extremely important.

When we create:

```text
Deployment
```

the Deployment manages a:

```text
ReplicaSet
```

and the ReplicaSet manages:

```text
Pods
```

So:

```text
Deployment
     ↓
ReplicaSet
     ↓
Pods
     ↓
Containers
```

For our backend:

```text
backend-deployment
        ↓
ReplicaSet
        ↓
+-------------------+
|                   |
↓                   ↓
Backend Pod       Backend Pod
|                   |
↓                   ↓
Container         Container
|                   |
↓                   ↓
Spring Boot       Spring Boot
```

---

# 12. Why don't we directly create a ReplicaSet?

Technically, we can create a ReplicaSet directly.

But normally we use a Deployment for application workloads.

Why?

Because a Deployment gives us additional capabilities such as:

* Managing ReplicaSets
* Rolling updates
* Rollbacks
* Version history
* Updating application configuration
* Managing application revisions

So our normal architecture is:

```text
Deployment
     ↓
ReplicaSet
     ↓
Pods
```

rather than manually managing ReplicaSets.

---

# 13. ReplicaSets we saw in our project

We ran:

```bash
kubectl get all
```

and saw ReplicaSets such as:

```text
backend-deployment-64d6ff7d46
backend-deployment-7fbbcb778b
backend-deployment-6d4479cbd5
backend-deployment-f79957f9c
```

Some of them showed:

```text
DESIRED   CURRENT   READY
0         0         0
```

while the current ReplicaSet showed:

```text
DESIRED   CURRENT   READY
2         2         2
```

This initially looks confusing.

Why do old ReplicaSets still exist?

---

# 14. Why are old ReplicaSets kept?

A Deployment can create different ReplicaSets for different revisions of the application.

For example:

```text
Deployment
    |
    +---- Old ReplicaSet
    |       |
    |       +---- Old Pods
    |
    +---- New ReplicaSet
            |
            +---- New Pods
```

During an update, the Deployment can gradually move from the old ReplicaSet to the new ReplicaSet.

Eventually:

```text
Old ReplicaSet
DESIRED = 0

New ReplicaSet
DESIRED = 2
```

The old ReplicaSet may still remain as revision history.

This helps Kubernetes support:

* Rollouts
* Rollbacks
* Revision history

Therefore, seeing old ReplicaSets with:

```text
0/0/0
```

does not mean those old Pods are running.

---

# 15. ReplicaSet names

Our ReplicaSets had names such as:

```text
backend-deployment-64d6ff7d46
```

The generated suffix helps identify the ReplicaSet revision.

The important thing is not to memorize the generated name.

Instead understand:

```text
backend-deployment
       ↓
ReplicaSet
       ↓
Pods
```

---

# 16. How does a ReplicaSet know which Pods belong to it?

This is where **labels and selectors** become important.

Our backend Pods have:

```yaml
labels:
  app: backend
```

The Deployment contains:

```yaml
selector:
  matchLabels:
    app: backend
```

The Pod template also contains:

```yaml
template:
  metadata:
    labels:
      app: backend
```

This allows Kubernetes to identify the Pods associated with the workload.

Conceptually:

```text
Deployment / ReplicaSet
        |
        | selector:
        | app=backend
        ↓
+----------------------+
| Pods with label      |
| app=backend          |
+----------------------+
```

Labels and selectors form an important connection between Kubernetes objects.

---

# 17. What is the Pod template?

Inside our Deployment we have:

```yaml
template:
  metadata:
    labels:
      app: backend

  spec:
    containers:
      - name: backend
        image: cloud-native-task-platform-backend:latest
```

This is called the **Pod template**.

The template describes what the Pods created by the Deployment should look like.

For example, it defines:

```text
Pod labels
Container name
Container image
Container port
Environment variables
Resources
Liveness probe
Readiness probe
```

Conceptually:

```text
Deployment
     |
     | Pod template
     ↓
"Create Pods with this configuration"
     |
     ↓
Pods
```

---

# 18. Why is the Pod template important?

Suppose our Deployment says:

```yaml
replicas: 2
```

and the template says:

```yaml
image: cloud-native-task-platform-backend:latest
```

Kubernetes creates two Pods based on that template.

So:

```text
Deployment
replicas: 2
image: backend:latest
        |
        ↓
   Pod Template
        |
   +----+----+
   |         |
   ↓         ↓
 Pod 1     Pod 2
```

Both Pods are created according to the same template.

---

# 19. Updating the Pod template

Suppose our current application uses:

```text
backend:v1
```

Then we build:

```text
backend:v2
```

and change the Deployment's Pod template to use the new image.

For example:

```yaml
image: backend:v2
```

This changes the desired configuration of the Deployment.

The Deployment creates a new ReplicaSet representing the new version.

Conceptually:

```text
Deployment
      |
      +---------------------+
      |                     |
      ↓                     ↓
Old ReplicaSet         New ReplicaSet
backend:v1             backend:v2
      |                     |
   Old Pods              New Pods
```

Kubernetes can then gradually replace the old Pods with the new Pods.

This is called a **rolling update**.

---

# 20. Rolling updates

A rolling update means Kubernetes gradually replaces old application instances with new ones instead of stopping everything at once.

For example:

```text
Before:

Old Pod
Old Pod
Old Pod
```

During the update:

```text
Old Pod
Old Pod
New Pod
```

Then:

```text
Old Pod
New Pod
New Pod
```

Eventually:

```text
New Pod
New Pod
New Pod
```

The exact behavior depends on the Deployment's rollout configuration.

The important beginner concept is:

> Kubernetes can update application Pods gradually to reduce downtime.

---

# 21. Why are rolling updates useful?

Imagine our application has:

```text
5 backend Pods
```

If we completely stop all 5 before starting the new version:

```text
5 old Pods
    ↓
0 Pods
    ↓
5 new Pods
```

there could be a period where the application cannot serve traffic.

A rolling update instead gradually replaces Pods.

Conceptually:

```text
Old version
     ↓
Gradually replaced
     ↓
New version
```

This helps maintain application availability during deployments.

---

# 22. Deployment rollout

When we change a Deployment, Kubernetes creates a new revision.

We can check rollout status using:

```bash
kubectl rollout status deployment backend-deployment
```

We used this command several times in our project.

For a successful deployment we saw:

```text
deployment "backend-deployment" successfully rolled out
```

This tells us that Kubernetes completed the rollout successfully.

---

# 23. Rollout history

Deployments maintain revision history.

We can inspect it using:

```bash
kubectl rollout history deployment backend-deployment
```

This can show different revisions of the Deployment.

The purpose is to allow us to understand previous versions and support rollback.

---

# 24. Rollback

Suppose we deploy a new version:

```text
Version 2
```

but the application has a problem.

A Deployment can be rolled back to a previous revision.

Conceptually:

```text
Version 1
     ↓
Version 2
     ↓
Problem
     ↓
Rollback
     ↓
Version 1
```

The command is:

```bash
kubectl rollout undo deployment backend-deployment
```

We don't need to perform a rollback in our project right now unless we specifically want to practice it.

The important concept is:

> Deployments provide a mechanism for rolling back to an earlier revision.

---

# 25. Scaling vs rolling update

These are different concepts.

### Scaling

Changes the number of Pods.

For example:

```text
2 Pods
  ↓
5 Pods
```

using:

```bash
kubectl scale deployment backend-deployment --replicas=5
```

### Rolling update

Changes the application version/configuration while gradually replacing Pods.

For example:

```text
backend:v1
     ↓
backend:v2
```

So:

```text
Scaling
→ changes quantity

Rolling update
→ changes version/configuration
```

Both are managed through the Deployment.

---

# 26. What happens when a Pod fails?

Suppose our Deployment says:

```yaml
replicas: 2
```

Initially:

```text
Pod A
Pod B
```

Now Pod A disappears.

The ReplicaSet sees:

```text
Desired = 2
Actual = 1
```

It creates a replacement:

```text
Pod B
Pod C
```

Eventually:

```text
Desired = 2
Actual = 2
```

This is the reconciliation process we learned earlier.

---

# 27. What happens when we scale to zero?

We tested:

```bash
kubectl scale deployment backend-deployment --replicas=0
```

The desired state becomes:

```text
Desired = 0
```

The ReplicaSet therefore does not need any Pods.

The Pods are terminated.

So:

```text
Deployment
replicas: 0
      ↓
ReplicaSet
desired Pods = 0
      ↓
No backend Pods
```

When we later run:

```bash
kubectl scale deployment backend-deployment --replicas=2
```

the desired state becomes:

```text
Desired = 2
```

and Kubernetes creates two Pods again.

---

# 28. Deployment and Service are different

This is an important distinction.

A Deployment manages:

```text
Pods
```

A Service provides:

```text
Stable network access to Pods
```

So:

```text
Deployment
    ↓
Pods

Service
    ↓
Provides network access to those Pods
```

For our backend we have:

```text
backend-deployment
        ↓
Backend Pods
        ↑
        |
backend-service
```

The Deployment does not replace the Service.

They solve different problems.

---

# 29. Our backend architecture

Our current backend structure is:

```text
                    backend-deployment
                           |
                           ↓
                      ReplicaSet
                           |
              +------------+------------+
              |                         |
              ↓                         ↓
         Backend Pod                Backend Pod
              |                         |
              ↓                         ↓
      Spring Boot container     Spring Boot container
              |                         |
              +------------+------------+
                           |
                     backend-service
```

The Service gives clients a stable way to reach the backend Pods.

We will study Services separately.

---

# 30. Deployment and HPA

Our project also has a Horizontal Pod Autoscaler.

We saw:

```text
horizontalpodautoscaler.autoscaling/backend-hpa
```

with information such as:

```text
MINPODS   MAXPODS
2         5
```

This means the HPA can automatically adjust the number of Pods between:

```text
Minimum = 2
Maximum = 5
```

based on its configured metrics.

So there are two ways we can change replicas:

### Manual scaling

```bash
kubectl scale deployment backend-deployment --replicas=5
```

### Automatic scaling

```text
HPA
 ↓
Deployment
 ↓
ReplicaSet
 ↓
Pods
```

We will study HPA separately.

For now, remember that the Deployment is still the object whose replica count is ultimately being managed.

---

# 31. Deployment vs ReplicaSet vs Pod

This is one of the most important interview concepts.

| Object     | Main responsibility                                         |
| ---------- | ----------------------------------------------------------- |
| Deployment | Manages application versions, desired state and ReplicaSets |
| ReplicaSet | Maintains the required number of Pods                       |
| Pod        | Runs one or more application containers                     |
| Container  | Runs the actual application process                         |

The hierarchy is:

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

# 32. A simple real-world analogy

Think about a restaurant.

### Deployment

The restaurant manager says:

> "I need 5 chefs working."

### ReplicaSet

The supervisor makes sure:

> "There are always 5 chefs available."

If one chef leaves:

```text
5 required
4 available
```

The supervisor gets another chef.

### Pod

A Pod is like the workstation assigned to a chef.

### Container

The container is where the actual application work happens.

This is only an analogy, but it helps remember the responsibilities.

---

# 33. What happens when we run `kubectl apply`?

Suppose we run:

```bash
kubectl apply -f backend-deployment.yaml
```

A simplified flow is:

```text
kubectl
   ↓
API Server
   ↓
Deployment
   ↓
ReplicaSet
   ↓
Pods
   ↓
Containers
```

The Deployment describes the desired state.

The controllers work to create the necessary ReplicaSet and Pods.

The Scheduler selects appropriate nodes for the Pods.

The kubelet on the selected Worker Node works with the container runtime to run the containers.

The simplified architecture is:

```text
kubectl
   ↓
API Server
   ↓
Deployment
   ↓
ReplicaSet
   ↓
Scheduler
   ↓
Worker Node
   ↓
kubelet
   ↓
Container Runtime
   ↓
Container
```

There are more internal steps, but this is the beginner mental model we should use.

---

# 34. What happens when we update our Deployment?

Suppose the current Deployment uses:

```text
backend:v1
```

We update it to:

```text
backend:v2
```

The process is conceptually:

```text
Deployment
     |
     +---------------------+
     |                     |
     ↓                     ↓
Old ReplicaSet         New ReplicaSet
backend:v1             backend:v2
     |                     |
 Old Pods                New Pods
```

The Deployment controls the rollout.

The old ReplicaSet is gradually scaled down.

The new ReplicaSet is gradually scaled up.

Eventually:

```text
Old ReplicaSet
DESIRED = 0

New ReplicaSet
DESIRED = 2
```

This is why we saw multiple ReplicaSets in our project.

---

# 35. Why does the ReplicaSet hash change?

We saw names such as:

```text
backend-deployment-64d6ff7d46
backend-deployment-7fbbcb778b
backend-deployment-6d4479cbd5
```

The suffix identifies a particular Pod template revision.

When the Pod template changes, Kubernetes creates a new ReplicaSet.

For example:

```text
Old template
    ↓
ReplicaSet A

New template
    ↓
ReplicaSet B
```

Therefore, changing things such as:

* Container image
* Environment configuration
* Pod configuration
* Probe configuration
* Resource configuration

can result in a new ReplicaSet revision.

---

# 36. Why didn't simply restarting the Deployment always create a new ReplicaSet?

This is an important practical distinction.

If we simply restart Pods without changing the Deployment's Pod template, Kubernetes may recreate Pods under the same ReplicaSet.

For example:

```bash
kubectl rollout restart deployment backend-deployment
```

This restarts the workload.

It does not necessarily mean we changed the application version.

A Deployment revision is associated with changes to the Pod template.

So:

```text
Restart
→ restart Pods

Pod template change
→ new Deployment revision / ReplicaSet
```

This distinction helps explain why our project accumulated multiple ReplicaSets during our experiments.

---

# 37. Checking our Deployment

Useful commands we used:

### List Deployments

```bash
kubectl get deployments
```

### Describe Deployment

```bash
kubectl describe deployment backend-deployment
```

### List ReplicaSets

```bash
kubectl get replicasets
```

or:

```bash
kubectl get rs
```

### List backend ReplicaSets

```bash
kubectl get rs -l app=backend
```

### List backend Pods

```bash
kubectl get pods -l app=backend
```

### Check rollout

```bash
kubectl rollout status deployment backend-deployment
```

### View rollout history

```bash
kubectl rollout history deployment backend-deployment
```

### Restart a Deployment

```bash
kubectl rollout restart deployment backend-deployment
```

### Roll back

```bash
kubectl rollout undo deployment backend-deployment
```

---

# 38. Understanding `kubectl get deployments`

When we ran:

```bash
kubectl get deployments
```

we saw something like:

```text
NAME                  READY   UP-TO-DATE   AVAILABLE
backend-deployment    2/2     2            2
```

These columns provide useful information.

### READY

How many replicas are ready compared with the desired number.

```text
2/2
```

means:

```text
2 ready
out of
2 desired
```

### UP-TO-DATE

How many replicas are using the latest Deployment configuration.

### AVAILABLE

How many replicas are currently available to serve the application.

For a healthy Deployment, we generally want these values to match the desired replica count.

---

# 39. Understanding our ReplicaSet output

We saw output similar to:

```text
NAME                            DESIRED   CURRENT   READY
backend-deployment-64d6ff7d46   2         2         2
backend-deployment-7fbbcb778b   0         0         0
backend-deployment-6d4479cbd5   0         0         0
```

The current ReplicaSet is:

```text
2 / 2 / 2
```

The old ReplicaSets are:

```text
0 / 0 / 0
```

This means:

```text
Current revision
→ maintaining the running Pods

Old revisions
→ retained as Deployment history
```

The old ReplicaSets are not necessarily a problem.

---

# 40. Important beginner mental model

Don't think:

```text
Deployment = Pods
```

Instead:

```text
Deployment
    ↓
Manages ReplicaSet
    ↓
ReplicaSet manages Pods
    ↓
Pods contain Containers
```

And remember:

```text
Deployment
→ application management

ReplicaSet
→ replica count

Pod
→ workload unit

Container
→ application process
```

---

# 41. Interview explanations

## What is a Deployment?

> "A Deployment is a Kubernetes object used to manage application Pods. It maintains the desired state and provides features such as scaling, rolling updates and rollbacks."

## What is a ReplicaSet?

> "A ReplicaSet ensures that the desired number of Pods are running. If a Pod disappears, it creates a replacement to maintain the required replica count."

## What is the relationship between Deployment and ReplicaSet?

> "A Deployment manages ReplicaSets, and ReplicaSets maintain the desired number of Pods. So the hierarchy is Deployment → ReplicaSet → Pods."

## Why don't we directly manage Pods?

> "Pods are replaceable and can disappear or be recreated. A Deployment provides a higher-level abstraction that manages the desired number of Pods and handles updates and rollbacks."

## What is scaling?

> "Scaling means changing the number of application replicas. For example, changing the backend from two Pods to five Pods."

## What is a rolling update?

> "A rolling update gradually replaces Pods running the old application version with Pods running the new version, helping reduce downtime during deployment."

## What is a rollback?

> "A rollback means returning the Deployment to a previous application revision when the current version has a problem."

## Why do we see multiple ReplicaSets?

> "Deployments create different ReplicaSets for different Pod template revisions. Older ReplicaSets can remain with zero replicas so Kubernetes can maintain revision history and support rollbacks."

---

# 42. What we should understand instead of memorizing

Don't try to memorize every command.

Understand this story:

```text
I have an application.
        ↓
I want 2 copies of it.
        ↓
I create a Deployment.
        ↓
Deployment manages a ReplicaSet.
        ↓
ReplicaSet creates/maintains 2 Pods.
        ↓
Each Pod runs my application container.
```

If one Pod disappears:

```text
Pod disappears
      ↓
ReplicaSet notices
      ↓
Replacement Pod
      ↓
Back to desired number
```

If I change the application version:

```text
Deployment changes
      ↓
New ReplicaSet
      ↓
New Pods
      ↓
Old ReplicaSet gradually reduced
      ↓
New version becomes active
```

If I want more capacity:

```text
replicas: 2
      ↓
replicas: 5
      ↓
More Pods
```

This is the core idea.

---

# 43. Questions that naturally lead to the next concepts

Now that we understand Deployments and ReplicaSets, several questions naturally arise.

### Question 1

We have multiple backend Pods. How does another application communicate with them without knowing their individual IP addresses?

→ **Kubernetes Services**

### Question 2

How does a Service know which Pods belong to the backend?

→ **Labels and Selectors**

### Question 3

How does Kubernetes give a Service a stable IP address?

→ **ClusterIP and Service networking**

### Question 4

How does a Service distribute requests among multiple Pods?

→ **Service traffic routing**

### Question 5

How does `backend-service` resolve to an IP address?

→ **Kubernetes DNS / CoreDNS**

These will be covered in the Services and DNS notes.

---

# 44. What we actually practiced in our project

We didn't learn Deployments and ReplicaSets only theoretically.

We actually:

* Created `backend-deployment`
* Set the backend replica count
* Changed replicas from 2 to 0
* Observed the backend Pods disappear
* Changed replicas from 0 back to 2
* Observed Kubernetes create new Pods
* Used `kubectl rollout status`
* Observed successful rollouts
* Inspected Deployments using `kubectl get deployments`
* Inspected ReplicaSets using `kubectl get rs`
* Observed old ReplicaSets with `0/0/0`
* Observed the active ReplicaSet maintaining the running Pods
* Updated the Deployment configuration
* Observed new ReplicaSet revisions
* Tested liveness and readiness configuration
* Observed Pods being restarted after liveness failures
* Observed Pods remaining running but not ready after readiness failures
* Rebuilt the backend image
* Loaded the image into Minikube
* Started new Pods using the updated image
* Verified the actual image IDs running inside the Pods

These practical experiments connect the Deployment/ReplicaSet architecture with the Kubernetes behavior we observed.

---

# 45. Final mental model

Remember this:

```text
                 DEPLOYMENT
                     |
          "I want this application
           in this desired state"
                     |
                     ↓
                REPLICASET
                     |
          "I need the required
           number of Pods"
                     |
              +------+------+
              |             |
              ↓             ↓
            POD 1         POD 2
              |             |
              ↓             ↓
          Container      Container
              |             |
              ↓             ↓
          Application    Application
```

For updates:

```text
                 DEPLOYMENT
                      |
          +-----------+-----------+
          |                       |
          ↓                       ↓
    Old ReplicaSet          New ReplicaSet
          |                       |
       Old Pods                New Pods
          |                       |
          +-----------+-----------+
                      |
                 Rolling Update
```

For failure:

```text
Desired:
2 Pods

Actual:
1 Pod

      ↓

ReplicaSet notices difference

      ↓

Creates replacement Pod

      ↓

Desired:
2 Pods

Actual:
2 Pods
```

For scaling:

```text
replicas: 2
     ↓
2 Pods

replicas: 5
     ↓
5 Pods

replicas: 0
     ↓
0 Pods
```

The most important relationship is:

```text
Deployment
    ↓
ReplicaSet
    ↓
Pods
    ↓
Containers
```

And the most important principle remains:

```text
Desired State
      ↓
Deployment
      ↓
ReplicaSet
      ↓
Actual Pods
      ↓
Reconciliation
      ↓
Keep actual state matching desired state
```

That is the foundation for understanding Kubernetes scaling, rolling updates, rollbacks, self-healing, HPA, and the rest of our application deployment architecture.

```
```
