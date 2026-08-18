````markdown
# Kubernetes Self-Healing and Rollouts

## 1. Why do we need self-healing and rollouts?

So far, we have learned how Kubernetes:

- Runs Pods
- Maintains the desired number of Pods
- Provides stable networking through Services
- Performs health checks using probes
- Automatically scales Pods using HPA

Now we need to understand two important Kubernetes behaviors:

1. **Self-healing**
2. **Rollouts**

These solve different problems.

### Self-healing

Self-healing means Kubernetes can automatically take action when the actual state of the application no longer matches the desired state.

For example:

```text
Desired:
2 backend Pods

Actual:
1 backend Pod
````

Kubernetes works to bring the system back to:

```text
Desired:
2 backend Pods

Actual:
2 backend Pods
```

### Rollouts

Rollouts are related to updating an application from one version to another.

For example:

```text
Old version
     ↓
Backend v1
```

needs to become:

```text
New version
     ↓
Backend v2
```

Kubernetes can gradually replace the old Pods with new Pods instead of removing everything at once.

---

# 2. What is self-healing?

Simple definition:

> **Self-healing is Kubernetes automatically taking corrective action when the actual state of a managed workload differs from the desired state.**

For example:

```text
Deployment

replicas: 2
```

means:

```text
Desired = 2 Pods
```

If one Pod disappears:

```text
Desired = 2
Actual  = 1
```

Kubernetes detects the difference and creates a replacement.

Eventually:

```text
Desired = 2
Actual  = 2
```

This is the desired-state reconciliation model we learned earlier.

---

# 3. Self-healing is not magic

It is important to understand what Kubernetes is actually doing.

Kubernetes is continuously observing the state of the cluster.

Conceptually:

```text
Desired State
      |
      ↓
   Observe
      |
      ↓
 Actual State
      |
      ↓
   Compare
      |
      ↓
Different?
   /     \
 Yes      No
  |        |
  ↓        ↓
Act      Nothing
```

If the states differ, Kubernetes takes the necessary action.

This is called:

> **Reconciliation**

---

# 4. Example: Pod deletion

Suppose our backend has:

```text
backend-deployment
        |
        ↓
    ReplicaSet
        |
    +---+---+
    |       |
    ↓       ↓
  Pod 1   Pod 2
```

The Deployment wants:

```text
replicas: 2
```

Now suppose Pod 1 is deleted.

The state becomes:

```text
Desired = 2
Actual  = 1
```

The ReplicaSet notices that only one Pod exists.

It creates another Pod.

Now:

```text
backend-deployment
        |
        ↓
    ReplicaSet
        |
    +---+---+
    |       |
    ↓       ↓
  Pod 2   Pod 3
```

And:

```text
Desired = 2
Actual  = 2
```

The system is back to the desired state.

---

# 5. Why does the ReplicaSet perform this?

We learned earlier:

```text
Deployment
    ↓
ReplicaSet
    ↓
Pods
```

The Deployment manages the application and its rollout configuration.

The ReplicaSet maintains the required number of Pods for a particular revision.

So if:

```yaml
replicas: 2
```

the ReplicaSet works to maintain:

```text
2 Pods
```

Therefore:

```text
Pod disappears
      ↓
ReplicaSet notices
      ↓
Replacement Pod
```

This is one of the mechanisms behind Kubernetes self-healing.

---

# 6. What happens if a container crashes?

A Pod and a container are not exactly the same thing.

Suppose our Pod contains:

```text
Backend Pod
    |
    ↓
Spring Boot container
```

If the application's main process exits, the container may stop.

Depending on the Pod's restart policy and the failure, Kubernetes can restart the container within the Pod.

So we should distinguish:

```text
Container failure
→ Container can be restarted
```

from:

```text
Pod failure/deletion
→ Controller can create a replacement Pod
```

These are different recovery mechanisms.

---

# 7. Container restart vs Pod replacement

This distinction is important.

### Container restart

The Pod still exists.

```text
Pod
 |
 └── Container
       ↓
    crashes
       ↓
  container restarts
```

The Pod itself may remain the same.

### Pod replacement

The Pod disappears or is terminated.

```text
Pod 1
  ↓
Deleted
  ↓
ReplicaSet
  ↓
Creates Pod 2
```

The replacement Pod will normally have a different name and IP.

So:

```text
Container restart
→ Same Pod

Pod replacement
→ New Pod
```

---

# 8. How liveness probes connect to self-healing

We previously tested this in our project.

Our backend has:

```yaml
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8080
```

The kubelet periodically checks this endpoint.

If the application is unhealthy and the liveness probe repeatedly fails:

```text
Liveness probe
      ↓
Failure
      ↓
Container considered unhealthy
      ↓
Container restarted
```

We actually observed this behavior.

When we intentionally used an invalid liveness endpoint, we saw:

```text
Liveness probe failed
```

and then:

```text
Container backend failed liveness probe, will be restarted
```

The Pod showed:

```text
RESTARTS
```

in:

```bash
kubectl get pods
```

This was an important practical demonstration of Kubernetes self-healing.

---

# 9. Readiness failure is different

We also tested an invalid readiness endpoint.

For example:

```text
/actuator/health/does-not-exist
```

The events showed:

```text
Readiness probe failed
HTTP probe failed with statuscode: 404
```

But the container continued running.

This demonstrated:

```text
Readiness failure
      ↓
Pod becomes Not Ready
      ↓
Pod is removed from normal Service traffic
      ↓
Container is NOT restarted just because readiness failed
```

This is different from liveness.

---

# 10. Liveness vs readiness

Remember:

```text
Liveness
→ "Should this container be restarted?"

Readiness
→ "Is this Pod ready to receive traffic?"
```

So:

```text
Liveness failure
        ↓
Container restart
```

while:

```text
Readiness failure
        ↓
Pod not ready
        ↓
No normal traffic
```

This distinction is extremely important in Kubernetes.

---

# 11. What is CrashLoopBackOff?

We also observed:

```text
CrashLoopBackOff
```

when we intentionally broke the liveness probe.

For example:

```text
Pod starts
   ↓
Liveness probe fails
   ↓
Container restarted
   ↓
Container starts again
   ↓
Liveness probe fails
   ↓
Container restarted
   ↓
Repeated failures
```

Kubernetes eventually applies a backoff delay between restarts.

The Pod can therefore show:

```text
CrashLoopBackOff
```

Simple meaning:

> Kubernetes keeps trying to restart the container, but it keeps failing, so Kubernetes waits longer between restart attempts.

---

# 12. CrashLoopBackOff does not mean Kubernetes stopped trying

This is important.

When we see:

```text
CrashLoopBackOff
```

it does not mean:

> "Kubernetes has permanently given up."

It means the container is repeatedly failing and Kubernetes is backing off before trying again.

Useful commands are:

```bash
kubectl get pods
```

and:

```bash
kubectl describe pod <pod-name>
```

and:

```bash
kubectl logs <pod-name>
```

These help us understand why the container keeps failing.

---

# 13. What happened in our project?

We intentionally configured an invalid liveness endpoint.

The events showed:

```text
Warning  Unhealthy
Liveness probe failed
HTTP probe failed with statuscode: 404
```

Then:

```text
Normal  Killing
Container backend failed liveness probe, will be restarted
```

Then:

```text
Warning  BackOff
Back-off restarting failed container
```

And:

```bash
kubectl get pods
```

showed:

```text
READY   STATUS             RESTARTS
0/1     CrashLoopBackOff   5
```

This was not a theoretical example.

We directly observed how Kubernetes reacts to a failed liveness probe.

---

# 14. How did we fix it?

We restored the correct endpoints:

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

Then the Deployment created healthy Pods.

We verified:

```bash
kubectl get pods -l app=backend
```

and got:

```text
backend-deployment-...   1/1   Running   0
backend-deployment-...   1/1   Running   0
```

We also verified:

```bash
kubectl describe pod -l app=backend
```

showed:

```text
Liveness:
http-get http://:8080/actuator/health/liveness

Readiness:
http-get http://:8080/actuator/health/readiness
```

This confirmed that our health probes were correctly configured.

---

# 15. What is a rollout?

Now we move to the second major concept.

A **rollout** is the process of updating an application managed by a Deployment from one version to another.

For example:

```text
Version 1
Backend image A
```

needs to become:

```text
Version 2
Backend image B
```

A Deployment can gradually replace the old Pods with new Pods.

This is called a:

> **Rolling update**

---

# 16. Why do we need rolling updates?

Imagine we have:

```text
Backend Pod 1 → Version A
Backend Pod 2 → Version A
```

We build:

```text
Version B
```

One simple approach would be:

```text
Delete Pod 1
Delete Pod 2
Create Pod 1
Create Pod 2
```

During the transition, there may be no running backend Pods.

That could cause downtime.

Instead, Kubernetes can gradually replace them.

For example:

```text
Version A
Pod 1
Pod 2

        ↓

Version A
Pod 2

Version B
Pod 3

        ↓

Version B
Pod 3
Pod 4
```

This is a rolling update.

---

# 17. Deployment and ReplicaSets during a rollout

This is where the relationship we learned earlier becomes very useful.

Before the update:

```text
Deployment
    |
    ↓
Old ReplicaSet
    |
    +---- Pod A
    |
    +---- Pod B
```

After changing the Deployment:

```text
Deployment
    |
    +-------------------+
    |                   |
    ↓                   ↓
Old ReplicaSet      New ReplicaSet
    |                   |
 Old Pods             New Pods
```

The Deployment manages the transition between the old and new ReplicaSets.

Eventually:

```text
Deployment
    |
    ↓
New ReplicaSet
    |
    +---- New Pod
    |
    +---- New Pod
```

The old ReplicaSet is scaled down.

---

# 18. Why do old ReplicaSets remain?

We previously saw many ReplicaSets:

```text
backend-deployment-59b5c58f4b
backend-deployment-5b95999878
backend-deployment-5d57cd8986
backend-deployment-745598db8c
backend-deployment-86d59d7679
backend-deployment-f79957f9c
backend-deployment-64d6ff7d46
```

Most had:

```text
DESIRED   CURRENT   READY
0         0         0
```

while the current ReplicaSet had:

```text
2   2   2
```

The old ReplicaSets represent previous Deployment revisions.

They can be kept as rollout history.

So:

```text
Old ReplicaSet
→ Previous version

Current ReplicaSet
→ Current version
```

---

# 19. What is a rolling update?

A rolling update gradually replaces old Pods with new Pods.

Conceptually:

```text
Before:

Old Pod 1
Old Pod 2
```

Then:

```text
Old Pod 1
Old Pod 2
New Pod 1
```

Then:

```text
Old Pod 2
New Pod 1
New Pod 2
```

Eventually:

```text
New Pod 1
New Pod 2
```

The exact number of Pods temporarily running depends on the Deployment's rollout strategy and settings.

---

# 20. Deployment strategy

Deployments support rollout strategies.

The commonly used strategy is:

```yaml
strategy:
  type: RollingUpdate
```

This is the default strategy for Deployments.

Another strategy is:

```yaml
strategy:
  type: Recreate
```

We should understand the difference at a basic level.

### RollingUpdate

Gradually replace old Pods.

```text
Old → New
```

This can reduce downtime.

### Recreate

Terminate the old Pods before creating the new ones.

```text
Old Pods
   ↓
Deleted
   ↓
New Pods
```

This can cause downtime during the transition.

For our backend, RollingUpdate is the important strategy to understand.

---

# 21. What happens when we change the image?

Suppose our Deployment currently uses:

```yaml
image: cloud-native-task-platform-backend:v1
```

We build:

```text
cloud-native-task-platform-backend:v2
```

and update the Deployment.

Kubernetes notices that the Pod template has changed.

The Deployment creates a new ReplicaSet for the new Pod template.

Conceptually:

```text
Deployment
     |
     +----------------+
     |                |
     ↓                ↓
Old ReplicaSet    New ReplicaSet
v1                v2
```

The rollout then gradually replaces the old Pods.

---

# 22. Why does changing the Pod template create a new ReplicaSet?

A Deployment uses ReplicaSets to represent different versions of its Pod template.

For example:

```text
ReplicaSet A
→ image v1

ReplicaSet B
→ image v2
```

The Deployment can then manage the transition between them.

This is why changing things such as:

```text
Container image
Environment variables
Labels in the Pod template
Resource configuration
Probe configuration
```

can result in a new ReplicaSet.

The exact behavior depends on whether the Pod template actually changes.

---

# 23. What is rollout status?

We used:

```bash
kubectl rollout status deployment backend-deployment
```

This command allows us to observe whether a Deployment rollout has completed.

For example:

```text
deployment "backend-deployment" successfully rolled out
```

means Kubernetes considers the new Deployment revision successfully rolled out.

This is useful during deployments because we don't have to manually inspect every Pod to determine whether the rollout has completed.

---

# 24. What is rollout history?

Kubernetes keeps Deployment revision information.

We can inspect it using:

```bash
kubectl rollout history deployment backend-deployment
```

This can show previous revisions of the Deployment.

Conceptually:

```text
Revision 1
→ Backend v1

Revision 2
→ Backend v2

Revision 3
→ Backend v3
```

This history is useful when we need to understand or revert a Deployment.

---

# 25. What is rollback?

Suppose we deploy:

```text
Version 2
```

but something is wrong.

For example:

```text
Application starts
but requests fail
```

We may want to return to the previous version.

This is called a:

> **Rollback**

We can use:

```bash
kubectl rollout undo deployment backend-deployment
```

Kubernetes can then move the Deployment back toward the previous revision.

Conceptually:

```text
Version 1
     ↓
Version 2
     ↓
Problem discovered
     ↓
Rollback
     ↓
Version 1
```

---

# 26. Why is rollback useful?

Without rollout history and rollback, recovering from a bad deployment may require manually rebuilding and redeploying the previous version.

With a Deployment:

```text
Old version
     ↓
New version
     ↓
Problem
     ↓
Rollback
```

Kubernetes can restore the previous Deployment revision.

This makes deployments safer.

---

# 27. Rollout vs rollback

These are opposite directions.

### Rollout

Move forward to a new version.

```text
v1 → v2
```

### Rollback

Move back to a previous version.

```text
v2 → v1
```

Remember:

```text
Rollout
→ Deploy a new revision

Rollback
→ Return to a previous revision
```

---

# 28. Rollout commands we should know

### Check rollout status

```bash
kubectl rollout status deployment backend-deployment
```

### View rollout history

```bash
kubectl rollout history deployment backend-deployment
```

### Roll back

```bash
kubectl rollout undo deployment backend-deployment
```

### Restart a Deployment

```bash
kubectl rollout restart deployment backend-deployment
```

These commands are useful when working with Deployments.

---

# 29. What does `rollout restart` do?

This command:

```bash
kubectl rollout restart deployment backend-deployment
```

does not necessarily mean:

> "Deploy a new application version."

Instead, it causes the Deployment's Pods to be recreated through a new rollout.

This can be useful when we want to restart all Pods managed by a Deployment.

For example:

```text
Current Pods
   ↓
Rolling restart
   ↓
New Pods
```

It is different from:

```text
rollout undo
```

which is specifically used to move back to a previous revision.

---

# 30. Rollout and readiness probes

Readiness probes are very important during rolling updates.

Suppose a new Pod starts:

```text
New Pod
   ↓
Spring Boot application starting
   ↓
Not Ready
```

The readiness probe fails while the application is not ready.

Kubernetes should not treat that Pod as ready for normal Service traffic.

Once:

```text
/actuator/health/readiness
```

returns successfully:

```text
New Pod
   ↓
Ready
   ↓
Can receive Service traffic
```

This helps make rolling updates safer.

The connection is:

```text
Deployment rollout
       ↓
New Pod starts
       ↓
Readiness probe
       ↓
Pod becomes Ready
       ↓
Service can use it
       ↓
Old Pod can be replaced
```

---

# 31. What happens if the new version never becomes ready?

Suppose we deploy a broken version.

The new Pod starts, but:

```text
Readiness probe
      ↓
Fails
```

Then the new Pod remains:

```text
Not Ready
```

The rollout may stop progressing because Kubernetes cannot establish the required number of available new Pods.

This is one reason readiness probes are important during deployments.

They help prevent Kubernetes from considering an unhealthy new Pod ready for traffic.

---

# 32. Liveness and rollout safety

Liveness also plays a role.

Suppose a new application version starts but is fundamentally broken.

The liveness probe may repeatedly fail:

```text
New Pod
   ↓
Liveness failure
   ↓
Container restart
```

If it continues failing:

```text
CrashLoopBackOff
```

This indicates a problem with the new version.

So health probes help Kubernetes understand whether a newly deployed application instance is functioning correctly.

---

# 33. Self-healing vs rollout

These two concepts should not be confused.

### Self-healing

The goal is:

```text
Keep the current desired state running
```

Example:

```text
Desired = 2
One Pod dies
     ↓
Create replacement
```

### Rollout

The goal is:

```text
Move from one application revision to another
```

Example:

```text
v1
 ↓
v2
```

So:

```text
Self-healing
→ Recover from failure

Rollout
→ Move to a new version
```

---

# 34. HPA vs self-healing

HPA also has a different purpose.

### Self-healing

```text
Pod failure
    ↓
Replacement Pod
```

### HPA

```text
High workload
    ↓
More Pods
```

So:

```text
Self-healing
→ Maintains availability

HPA
→ Adjusts capacity based on demand
```

Both can cause Pods to be created, but for different reasons.

---

# 35. Service vs self-healing

A Service does not replace failed Pods.

For example:

```text
Service
   |
   +----> Pod 1
   |
   +----> Pod 2
```

If Pod 1 disappears:

```text
Service
   |
   +----> Pod 2
```

The Service itself does not create a replacement.

The Deployment/ReplicaSet does:

```text
Pod 1 disappears
       ↓
ReplicaSet
       ↓
Creates Pod 3
       ↓
Pod becomes Ready
       ↓
Service can use Pod 3
```

So:

```text
ReplicaSet
→ Maintains Pods

Service
→ Routes traffic to available Pods
```

---

# 36. Complete self-healing flow

Suppose one backend Pod crashes.

The complete simplified flow is:

```text
Backend Pod
     |
     ↓
Container crashes
     |
     ↓
kubelet detects container failure
     |
     ↓
Container restart
```

If the Pod itself is lost:

```text
Backend Pod disappears
     |
     ↓
ReplicaSet detects:
Desired = 2
Actual = 1
     |
     ↓
Replacement Pod created
     |
     ↓
New Pod starts
     |
     ↓
Readiness probe succeeds
     |
     ↓
Service can send traffic to it
```

This combines many of the concepts we have learned.

---

# 37. Complete rolling update flow

Now consider an application update.

```text
Current Deployment
        |
        ↓
Old ReplicaSet
        |
        ↓
Old Pods
        |
        ↓
Update image/configuration
        |
        ↓
Deployment creates new ReplicaSet
        |
        ↓
New Pods created
        |
        ↓
Readiness probes
        |
        ↓
New Pods become Ready
        |
        ↓
Old Pods gradually removed
        |
        ↓
New ReplicaSet becomes active
```

This is the basic rolling-update mental model.

---

# 38. Deployment revision history

A Deployment may maintain several ReplicaSets.

For example:

```text
Deployment
   |
   +---- ReplicaSet v1
   |
   +---- ReplicaSet v2
   |
   +---- ReplicaSet v3
```

Usually only the current revision has active Pods:

```text
ReplicaSet v1
0 Pods

ReplicaSet v2
0 Pods

ReplicaSet v3
2 Pods
```

The old ReplicaSets can be retained for rollout history.

This is why our `kubectl get rs` output contained many old ReplicaSets.

---

# 39. Why do ReplicaSet names change?

ReplicaSet names generated by a Deployment contain a hash.

For example:

```text
backend-deployment-64d6ff7d46
```

The hash is related to the Pod template.

When the Pod template changes, Kubernetes can generate a different ReplicaSet.

For example:

```text
backend-deployment-64d6ff7d46
```

may represent one version, while:

```text
backend-deployment-7fbbcb778b
```

represents another revision.

We don't need to calculate or memorize the hash.

Just understand:

```text
Different Pod template
       ↓
Different ReplicaSet revision
```

---

# 40. Our practical observations

During our project, we experienced several important Kubernetes behaviors.

### We intentionally broke readiness

We changed the readiness path to:

```text
/actuator/health/does-not-exist
```

and observed:

```text
Readiness probe failed
404
```

The container stayed running.

---

### We intentionally broke liveness

We configured an invalid liveness endpoint.

We observed:

```text
Liveness probe failed
```

then:

```text
Container backend failed liveness probe, will be restarted
```

and eventually:

```text
CrashLoopBackOff
```

---

### We restored the probes

We restored:

```text
/actuator/health/liveness
/actuator/health/readiness
```

Then we verified:

```text
1/1 Running
```

for the backend Pods.

---

### We observed old ReplicaSets

We saw multiple ReplicaSets with:

```text
DESIRED = 0
CURRENT = 0
READY = 0
```

while the active ReplicaSet maintained the current backend Pods.

This demonstrated Deployment revision history.

---

# 41. How to inspect rollout history

We can use:

```bash
kubectl rollout history deployment backend-deployment
```

To inspect a specific revision:

```bash
kubectl rollout history deployment backend-deployment --revision=2
```

The exact revision numbers depend on the Deployment's history.

---

# 42. How to watch a rollout

We can use:

```bash
kubectl rollout status deployment backend-deployment
```

We can also watch Pods:

```bash
kubectl get pods -l app=backend -w
```

The `-w` option watches for changes.

During a rollout, we may see Pods being:

```text
Created
Ready
Terminated
```

This is useful for understanding what Kubernetes is doing in real time.

---

# 43. Deployment rollout and Services

Remember our architecture:

```text
Ingress
   ↓
Backend Service
   ↓
Backend Pods
```

During a rolling update:

```text
Backend Service
      |
      +--------> Old Pod
      |
      +--------> New Pod
```

As new Pods become Ready and old Pods are removed:

```text
Backend Service
      |
      +--------> New Pod
      |
      +--------> New Pod
```

The Service provides the stable endpoint while the Deployment changes the underlying Pods.

This is a major reason why Services and Deployments work so well together.

---

# 44. Why rolling updates are important in real systems

Imagine an application with thousands of users.

If we completely stop the application:

```text
Old version
    ↓
Everything stopped
    ↓
New version
```

users may experience downtime.

With rolling updates:

```text
Old Pods
    ↓
New Pods gradually introduced
    ↓
Old Pods gradually removed
```

the application can remain available during the deployment, assuming the application and rollout configuration support that behavior.

This is one of the core ideas behind zero/minimal-downtime deployments.

---

# 45. Important limitation

Rolling updates do not automatically guarantee zero downtime.

The application and Kubernetes configuration still matter.

For example:

* Readiness probes must be correct.
* The application must start successfully.
* Enough replicas should exist.
* Resource capacity must be available.
* The rollout strategy must be appropriate.
* The application should be capable of running multiple versions during the transition when necessary.

So we should say:

> Rolling updates help reduce downtime by gradually replacing old Pods with new ones.

rather than:

> Rolling updates always guarantee zero downtime.

---

# 46. Self-healing does not mean fixing application bugs

Another important distinction.

Suppose our application contains a bug:

```text
GET /api/tasks
```

always throws an exception.

Kubernetes cannot automatically fix the Java code.

It may restart the container if liveness fails, but if the application continues to fail:

```text
Restart
 ↓
Failure
 ↓
Restart
 ↓
Failure
```

we may end up with:

```text
CrashLoopBackOff
```

Kubernetes can recover infrastructure/workload state, but it does not automatically fix application logic.

So:

```text
Kubernetes self-healing
→ Restores desired workload state

Application bug
→ Developer must fix it
```

---

# 47. Important mental model

Think of Kubernetes self-healing as:

```text
"I told Kubernetes what I want."

        ↓

Something changes unexpectedly.

        ↓

Kubernetes notices.

        ↓

Kubernetes takes corrective action.

        ↓

The system moves back toward
the desired state.
```

This is much more important than memorizing the word "self-healing."

---

# 48. Simple interview explanations

## What is Kubernetes self-healing?

> "Kubernetes continuously compares the actual state with the desired state. If a managed Pod fails or disappears, controllers such as ReplicaSets can create replacement Pods to restore the desired number of replicas."

---

## What happens if a Pod managed by a Deployment is deleted?

> "The ReplicaSet associated with the Deployment detects that the actual number of Pods is lower than the desired number and creates a replacement Pod."

---

## What happens if a container fails?

> "Depending on the failure and Pod configuration, Kubernetes can restart the container. If the Pod itself is lost, the controller can create a replacement Pod."

---

## What is CrashLoopBackOff?

> "CrashLoopBackOff means a container is repeatedly failing and Kubernetes is restarting it with increasing delays between restart attempts."

---

## What is a rollout?

> "A rollout is the process of updating a Deployment from one application revision to another, commonly using a rolling update."

---

## What is a rolling update?

> "A rolling update gradually replaces old Pods with new Pods so that the application can continue serving traffic during the update, assuming the application and configuration support it."

---

## What is rollback?

> "Rollback means reverting a Deployment to a previous revision when the new version has a problem."

---

## What is the difference between rollout and rollback?

> "A rollout moves the application forward to a new revision, while a rollback moves it back to a previous revision."

---

## Why are readiness probes important during rollouts?

> "They allow Kubernetes to determine whether a new Pod is actually ready to receive traffic before treating it as available."

---

# 49. Common beginner mistakes

### Mistake 1

Thinking Kubernetes always restarts a failed Pod.

Not necessarily.

A container can be restarted within an existing Pod, while a controller can create a replacement Pod when the Pod itself is lost.

---

### Mistake 2

Thinking readiness failure restarts the container.

It does not.

```text
Readiness failure
→ Pod not ready
→ No normal traffic

Liveness failure
→ Container restart
```

---

### Mistake 3

Thinking CrashLoopBackOff means the Pod was permanently deleted.

It means the container is repeatedly failing and Kubernetes is backing off between restart attempts.

---

### Mistake 4

Thinking rollout means scaling.

They are different.

```text
Rollout
→ Version change

HPA
→ Replica count change based on metrics
```

---

### Mistake 5

Thinking old ReplicaSets mean old Pods are still running.

Not necessarily.

An old ReplicaSet can exist with:

```text
0 Pods
```

and simply represent an older Deployment revision.

---

### Mistake 6

Thinking rollback fixes application bugs.

Rollback restores an earlier known Deployment revision.

It does not fix the buggy code itself.

---

# 50. What we should remember

The key ideas are:

```text
Self-healing
→ Kubernetes tries to restore desired state.

ReplicaSet
→ Maintains the required number of Pods.

Liveness
→ Can trigger container restart.

Readiness
→ Controls whether a Pod is considered ready for traffic.

CrashLoopBackOff
→ Container repeatedly fails and Kubernetes backs off restarts.

Rollout
→ Move to a new application revision.

RollingUpdate
→ Gradually replace old Pods with new Pods.

Rollback
→ Return to a previous revision.
```

---

# 51. Final self-healing mental model

```text
                  DESIRED STATE
                       |
                       ↓
                  Deployment
                       |
                       ↓
                  ReplicaSet
                       |
                       ↓
                      Pods
                       |
                       ↓
                 Actual State
                       |
                       ↓
                    Compare
                       |
              +--------+--------+
              |                 |
           Same              Different
              |                 |
              ↓                 ↓
          Continue          Reconcile
                                |
                                ↓
                         Corrective action
                                |
                                ↓
                       Desired state restored
```

---

# 52. Final rollout mental model

```text
                  DEPLOYMENT
                      |
             Current revision
                      |
                      ↓
                 Old ReplicaSet
                      |
                    Old Pods
                      |
                 New version
                      |
                      ↓
                 New ReplicaSet
                      |
                   New Pods
                      |
                      ↓
               Readiness checks
                      |
                      ↓
                Pods become Ready
                      |
                      ↓
             Old Pods removed
                      |
                      ↓
             New version active
```

---

# 53. How our Kubernetes concepts connect

At this point, the major concepts we have learned connect together:

```text
                    Deployment
                         |
                         ↓
                    ReplicaSet
                         |
                         ↓
                       Pods
                         |
              +----------+----------+
              |                     |
              ↓                     ↓
        Health Probes            Service
              |                     |
       +------+-------+             ↓
       |              |        Stable networking
       ↓              ↓
   Liveness       Readiness
       |              |
       ↓              ↓
 Container       Traffic eligibility
 restart              |
                       |
                       ↓
                      HPA
                       |
                       ↓
                 More/Fewer Pods
```

During a deployment update:

```text
Deployment
    |
    +--------------------+
    |                    |
Old ReplicaSet       New ReplicaSet
    |                    |
Old Pods              New Pods
                         |
                    Readiness
                         |
                         ↓
                    Ready Pods
                         |
                         ↓
                    Service
                         |
                         ↓
                    Traffic
```

This is the overall mental model we should carry forward.

---

# 54. Summary

We have now learned two important Kubernetes behaviors.

## Self-healing

Kubernetes continuously works to keep:

```text
Actual State = Desired State
```

If a managed Pod disappears:

```text
ReplicaSet
    ↓
Replacement Pod
```

If a container fails its liveness probe:

```text
Liveness failure
    ↓
Container restart
```

If a readiness probe fails:

```text
Readiness failure
    ↓
Pod not ready for normal traffic
```

If the container repeatedly fails:

```text
CrashLoopBackOff
```

---

## Rollouts

When we update an application:

```text
Version 1
    ↓
Version 2
```

the Deployment can use a rolling update:

```text
Old ReplicaSet
      ↓
New ReplicaSet
      ↓
New Pods become Ready
      ↓
Old Pods gradually removed
```

If the new version has a problem:

```text
Rollback
    ↓
Previous revision
```

The important commands are:

```bash
kubectl rollout status deployment backend-deployment

kubectl rollout history deployment backend-deployment

kubectl rollout undo deployment backend-deployment

kubectl rollout restart deployment backend-deployment
```

The central idea is:

```text
Self-healing
→ Recover from unexpected failures

Rollout
→ Safely move to a new application version

Rollback
→ Return to a previous application version
```

These concepts complete an important part of our Kubernetes fundamentals and connect directly to the practical behavior we observed in our project.

```
```
