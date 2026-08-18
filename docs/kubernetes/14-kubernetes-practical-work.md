````markdown
# Kubernetes Practical Work

## 1. Why do we have a separate practical-work note?

Throughout this Kubernetes sprint, we did not only study concepts.

We actually created and tested Kubernetes resources in our project.

The purpose of this note is to document:

- What we actually implemented
- Which commands we used
- What we observed
- What problems we faced
- How we fixed them
- How the different Kubernetes concepts work together in our project

The concept notes explain **what Kubernetes is and how it works**.

This note explains:

> "What did we actually do in our Cloud Native Task Platform?"

---

# 2. Our Kubernetes project architecture

Our Kubernetes application currently contains:

```text
                         USER
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
       Frontend Pods              Backend Pods
                                         |
                                         ↓
                                PostgreSQL Service
                                         |
                                         ↓
                                  PostgreSQL Pod
````

We also have Nginx in the cluster.

At a high level:

```text
Kubernetes Cluster
│
├── Backend
│   └── backend-deployment
│       └── Backend Pods
│
├── Frontend
│   └── frontend-deployment
│       └── Frontend Pods
│
├── PostgreSQL
│   └── postgres-deployment
│       └── PostgreSQL Pod
│
├── Nginx
│   └── nginx-deployment
│       └── Nginx Pods
│
├── Services
│
├── Ingress
│
├── ConfigMap
│
├── Secret
│
├── PersistentVolumeClaim
│
└── HorizontalPodAutoscaler
```

---

# 3. Our Kubernetes folder structure

Our project contains a separate Kubernetes directory:

```text
cloud-native-task-platform/
│
├── task-management-backend/
│
├── task-management-frontend/
│
├── kubernetes/
│
└── ...
```

Inside:

```text
kubernetes/
```

we maintain our Kubernetes manifests.

Conceptually:

```text
kubernetes/
│
├── backend-deployment.yaml
├── backend-service.yaml
├── frontend-deployment.yaml
├── frontend-service.yaml
├── postgres-deployment.yaml
├── postgres-service.yaml
├── postgres-pvc.yaml
├── nginx-deployment.yaml
├── nginx-service.yaml
├── ingress.yaml
├── configmap.yaml
├── secret.yaml
└── ...
```

The exact filenames can differ depending on how we organized the repository.

The important idea is that Kubernetes configuration is maintained as YAML manifests.

---

# 4. Applying Kubernetes manifests

Instead of manually creating every resource, we define the desired state in YAML.

For example:

```yaml
apiVersion: apps/v1
kind: Deployment
```

Then we apply it using:

```bash
kubectl apply -f backend-deployment.yaml
```

Kubernetes reads the manifest and creates or updates the requested resource.

The simplified flow is:

```text
YAML file
    |
    ↓
kubectl apply
    |
    ↓
API Server
    |
    ↓
Kubernetes resources
```

---

# 5. Checking Pods

One of the commands we used frequently was:

```bash
kubectl get pods
```

For backend Pods specifically:

```bash
kubectl get pods -l app=backend
```

We used this to verify:

* Whether Pods were running
* Whether Pods were ready
* Whether containers had restarted
* How many Pods existed

For example:

```text
NAME                                  READY   STATUS    RESTARTS
backend-deployment-...               1/1     Running   0
backend-deployment-...               1/1     Running   0
```

The important fields are:

```text
READY
STATUS
RESTARTS
AGE
```

---

# 6. Understanding `1/1`

When we saw:

```text
1/1
```

it means:

```text
1 container ready
out of
1 container in the Pod
```

For our backend Pods, we currently use one container per Pod.

Therefore:

```text
1/1
```

means the backend container is ready.

If we saw:

```text
0/1
```

it means:

```text
1 container exists
but it is not currently ready
```

This became especially useful when testing readiness probes.

---

# 7. Understanding Pod status

We observed several statuses during our practical work.

### Running

```text
Running
```

The Pod is running.

### Pending

```text
Pending
```

The Pod has not yet successfully started.

There can be several reasons, such as scheduling or resource problems.

### CrashLoopBackOff

```text
CrashLoopBackOff
```

The container is repeatedly failing and Kubernetes is backing off between restart attempts.

We actually observed this when we intentionally broke the liveness probe.

### Terminating

```text
Terminating
```

Kubernetes is in the process of removing the Pod.

We observed this when scaling the backend from:

```text
2 replicas
```

to:

```text
0 replicas
```

---

# 8. Scaling the backend

Our backend Deployment was configured with multiple replicas.

We used:

```bash
kubectl scale deployment backend-deployment --replicas=0
```

This changed the desired state to:

```text
Desired = 0
```

Kubernetes then removed the backend Pods.

We verified:

```bash
kubectl get pods -l app=backend
```

and eventually got:

```text
No resources found
```

---

# 9. Scaling back to two Pods

We then used:

```bash
kubectl scale deployment backend-deployment --replicas=2
```

Kubernetes created two backend Pods.

We verified:

```bash
kubectl get pods -l app=backend
```

and saw:

```text
backend-deployment-...   1/1   Running
backend-deployment-...   1/1   Running
```

This demonstrated desired-state reconciliation.

We said:

```text
Desired = 2
```

and Kubernetes created the required Pods.

---

# 10. Checking the Deployment

We used:

```bash
kubectl get deployments
```

Our output included:

```text
NAME                  READY   UP-TO-DATE   AVAILABLE
backend-deployment    2/2     2            2
frontend-deployment   3/3     3            3
nginx-deployment      5/5     5            5
postgres-deployment   1/1     1            1
```

For the backend:

```text
2/2
```

means two desired backend replicas are ready.

---

# 11. Checking ReplicaSets

We used:

```bash
kubectl get rs
```

or:

```bash
kubectl get rs -l app=backend
```

We observed multiple ReplicaSets.

For example:

```text
backend-deployment-64d6ff7d46
backend-deployment-7fbbcb778b
backend-deployment-6d4479cbd5
...
```

Most old ReplicaSets had:

```text
DESIRED   CURRENT   READY
0         0         0
```

while the current ReplicaSet had:

```text
2         2         2
```

This demonstrated that Deployments maintain ReplicaSet revisions.

---

# 12. Checking Services

We used:

```bash
kubectl get services
```

Our project currently contains Services such as:

```text
backend-nodeport
backend-service
frontend-service
nginx-nodeport
nginx-service
postgres-service
```

For example:

```text
backend-service   ClusterIP   10.111.127.205   8080/TCP
```

The Service provides stable access to the backend Pods.

---

# 13. Checking Service details

We can inspect a Service using:

```bash
kubectl describe service backend-service
```

This is useful for checking:

* Service type
* ClusterIP
* Ports
* Target ports
* Selectors
* Endpoints

The important relationship is:

```text
Service selector
       ↓
Matching Pod labels
       ↓
Service endpoints
       ↓
Backend Pods
```

---

# 14. Checking Ingress

We used:

```bash
kubectl get ingress
```

Our project contains:

```text
application-ingress
```

with the Nginx Ingress class.

We observed:

```text
ADDRESS: 192.168.49.2
PORTS:   80
```

The Ingress provides the external HTTP entry point.

Conceptually:

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

---

# 15. Testing the application through Kubernetes

Instead of directly running Spring Boot using:

```bash
./gradlew bootRun
```

we tested the application running inside Kubernetes.

For example, we used:

```bash
curl -i -X POST http://192.168.49.2/api/auth/register \
-H "Content-Type: application/json" \
-d '{"username":"testUser","email":"testUser@example.com","password":"User@123"}'
```

The request returned:

```text
HTTP/1.1 200
```

with the created user information.

This was an important verification.

It demonstrated that the complete path was working:

```text
curl
  |
  ↓
Minikube IP
  |
  ↓
Ingress
  |
  ↓
Backend Service
  |
  ↓
Backend Pod
  |
  ↓
Spring Boot
  |
  ↓
PostgreSQL Service
  |
  ↓
PostgreSQL Pod
```

---

# 16. Testing the backend registration endpoint

We previously tested registration locally using:

```bash
curl -i -X POST http://localhost:8080/api/auth/register \
-H "Content-Type: application/json" \
-d '{"username":"localuser","email":"local@example.com","password":"Test@123"}'
```

The first request returned:

```text
HTTP/1.1 200
```

and created the user.

When we attempted the same registration again, the application returned:

```text
HTTP/1.1 409
```

with:

```json
{
  "status": 409,
  "message": "Email already exists"
}
```

This confirmed that the duplicate-email validation was working.

We later changed the registration test data for our Kubernetes testing.

---

# 17. Testing login locally

We also tested login:

```bash
curl -i -X POST http://localhost:8080/api/auth/login \
-H "Content-Type: application/json" \
-d '{"email":"local@example.com","password":"Test@123"}'
```

The response returned:

```text
HTTP/1.1 200
```

and included:

```json
{
  "accessToken": "...",
  "tokenType": "Bearer",
  "user": {
    ...
  }
}
```

This confirmed that:

```text
Registration
    ↓
User stored
    ↓
Login
    ↓
JWT access token
```

was working locally.

---

# 18. Why did we test locally first?

Before testing Kubernetes, it is useful to make sure the application itself works.

The layers are:

```text
Application
    ↓
Docker
    ↓
Kubernetes
```

If the application fails locally, debugging Kubernetes at the same time makes the problem harder to identify.

Therefore our approach was:

```text
1. Test Spring Boot locally
2. Build Docker image
3. Run/test container
4. Load image into Minikube
5. Deploy to Kubernetes
6. Test through Kubernetes networking
```

This separates problems between application, container, and Kubernetes.

---

# 19. Rebuilding our backend Docker image

Because we were developing the backend continuously, we needed to rebuild the image whenever we changed the backend code.

From the project root, we built:

```bash
docker build -t cloud-native-task-platform-backend:latest ./task-management-backend
```

This creates:

```text
cloud-native-task-platform-backend:latest
```

---

# 20. Why did we have to load the image into Minikube?

Our Kubernetes Deployment uses:

```yaml
image: cloud-native-task-platform-backend:latest
```

and:

```yaml
imagePullPolicy: Never
```

This means Kubernetes should use an image already available inside the Minikube environment.

Building the image on the host does not automatically mean that Kubernetes can use the newly built image.

Therefore we used:

```bash
minikube image load cloud-native-task-platform-backend:latest
```

This loads the image into Minikube.

---

# 21. Why did we remove the old image?

Because we were using the same tag:

```text
cloud-native-task-platform-backend:latest
```

we wanted to make sure Minikube was using the newly built image.

Our practical workflow was:

```bash
kubectl scale deployment backend-deployment --replicas=0
```

Wait until the Pods disappear:

```bash
kubectl get pods -l app=backend
```

Then:

```bash
minikube image rm cloud-native-task-platform-backend:latest
```

Then load the newly built image:

```bash
minikube image load cloud-native-task-platform-backend:latest
```

Then restore the replicas:

```bash
kubectl scale deployment backend-deployment --replicas=2
```

And verify:

```bash
kubectl get pods -l app=backend
```

---

# 22. Verifying which image the Pods are actually using

We used:

```bash
kubectl get pods -l app=backend \
-o jsonpath='{range .items[*]}{.metadata.name}{" -> "}{.status.containerStatuses[0].imageID}{"\n"}{end}'
```

This produced output similar to:

```text
backend-deployment-... -> docker://sha256:7c7269...
backend-deployment-... -> docker://sha256:7c7269...
```

The important point is that both Pods had the same image ID.

This helped us verify that both backend Pods were running the same image.

---

# 23. Why `latest` can be confusing

We used:

```text
cloud-native-task-platform-backend:latest
```

during our local Kubernetes learning.

However, in professional CI/CD systems, immutable version tags are generally safer.

For example:

```text
backend:v1.0.0
backend:v1.0.1
```

or:

```text
backend:<git-commit-sha>
```

This makes it much easier to know exactly which version is running.

For our current Minikube learning environment, using `latest` with:

```yaml
imagePullPolicy: Never
```

is acceptable for understanding the workflow.

Later, our GitHub Actions/CI/CD stage can introduce proper versioned image tagging.

---

# 24. Kubernetes health probes

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

---

# 25. Testing Actuator health locally

We tested:

```bash
curl -i http://localhost:8080/actuator/health/liveness
```

and received:

```text
HTTP/1.1 200
```

We also tested readiness:

```bash
curl -i http://localhost:8080/actuator/health/readiness
```

and received:

```json
{
  "status": "UP"
}
```

This confirmed that Spring Boot's health endpoints were available.

---

# 26. Testing readiness failure

To understand how Kubernetes behaves, we intentionally configured an invalid readiness endpoint:

```text
/actuator/health/does-not-exist
```

The Pod events showed:

```text
Readiness probe failed
HTTP probe failed with statuscode: 404
```

But the container remained running.

This demonstrated:

```text
Readiness failure
      ↓
Pod not ready
      ↓
Container continues running
```

---

# 27. Testing liveness failure

We then intentionally configured an invalid liveness endpoint.

Kubernetes reported:

```text
Liveness probe failed
HTTP probe failed with statuscode: 404
```

Then:

```text
Container backend failed liveness probe,
will be restarted
```

The Pods eventually showed:

```text
CrashLoopBackOff
```

with increasing restart counts.

This demonstrated the difference between:

```text
Readiness
→ controls traffic eligibility

Liveness
→ controls container restart
```

---

# 28. Restoring the health probes

We restored the correct configuration:

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

Then we verified:

```bash
kubectl rollout status deployment backend-deployment
```

and:

```bash
kubectl get pods -l app=backend
```

The backend Pods returned to:

```text
1/1   Running
```

---

# 29. ConfigMap and Secret

Our backend Deployment uses:

```yaml
envFrom:
  - configMapRef:
      name: backend-config

  - secretRef:
      name: backend-secret
```

This means configuration is provided to the container through Kubernetes resources rather than hard-coding configuration directly into the Deployment.

Conceptually:

```text
ConfigMap
     |
     ↓
Backend Pod
     |
     ↓
Environment variables
```

and:

```text
Secret
     |
     ↓
Backend Pod
     |
     ↓
Sensitive environment variables
```

---

# 30. Why did we create `application-local.yaml`?

During local development, our Spring Boot application needs local configuration.

We created:

```text
src/main/resources/application-local.yaml
```

and run the application with:

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

This activates the:

```text
local
```

Spring profile.

This keeps local development configuration separate from the normal configuration used in other environments.

---

# 31. Why is `application-local.yaml` in `.gitignore`?

Our `.gitignore` contains:

```text
src/main/resources/application-local.yaml
```

This means Git should not commit that local configuration file.

This is useful when the file contains environment-specific or sensitive local configuration.

The general principle is:

```text
Local configuration
        ↓
Keep outside Git when appropriate
```

For our project, Kubernetes configuration is handled separately through ConfigMaps and Secrets.

---

# 32. Important security distinction

A Kubernetes Secret is intended for sensitive configuration, but we should not think that putting a value in a Kubernetes Secret automatically makes it cryptographically safe in every context.

For our learning project:

```text
ConfigMap
→ Non-sensitive configuration

Secret
→ Sensitive configuration
```

Later, when we move to AWS and professional CI/CD, we can use more appropriate secret-management solutions.

---

# 33. PostgreSQL persistent storage

Our PostgreSQL setup includes a PersistentVolumeClaim:

```text
postgres-pvc
```

We verified it using:

```bash
kubectl get pvc
```

and saw:

```text
postgres-pvc
STATUS: Bound
CAPACITY: 5Gi
ACCESS MODES: RWO
STORAGECLASS: standard
```

This means the PostgreSQL storage claim is bound to persistent storage.

The important idea is:

```text
PostgreSQL Pod
      |
      ↓
Persistent Volume
      |
      ↓
Data survives Pod recreation
```

The Pod itself is temporary, but persistent storage is designed to survive Pod recreation.

---

# 34. Checking the PVC

We used:

```bash
kubectl get pvc
```

For more details:

```bash
kubectl describe pvc postgres-pvc
```

This helps us understand:

* Requested storage
* Current status
* Storage class
* Bound volume
* Access mode

---

# 35. HPA in our project

Our project also contains a HorizontalPodAutoscaler.

We verified it using:

```bash
kubectl get hpa
```

and saw:

```text
horizontalpodautoscaler.autoscaling/backend-hpa
```

with information such as:

```text
TARGETS
cpu: 3%/70%

MINPODS
2

MAXPODS
5

REPLICAS
2
```

This means the backend can automatically scale between:

```text
2 Pods
```

and:

```text
5 Pods
```

based on the configured CPU target.

---

# 36. How HPA fits with our Deployment

The HPA targets:

```text
Deployment/backend-deployment
```

So the relationship is:

```text
HPA
  |
  ↓
Backend Deployment
  |
  ↓
ReplicaSet
  |
  ↓
Backend Pods
```

The HPA changes the desired replica count of the Deployment based on resource utilization.

The Deployment/ReplicaSet then creates or removes Pods accordingly.

---

# 37. Checking all Kubernetes resources

We used:

```bash
kubectl get all
```

This gives a useful high-level view of many Kubernetes resources.

Our output included:

```text
Pods
Services
Deployments
ReplicaSets
```

However, `kubectl get all` does not literally mean every Kubernetes resource.

For example, resources such as:

```text
Ingress
PVC
ConfigMaps
Secrets
HPA
```

may need to be checked separately.

Useful commands include:

```bash
kubectl get ingress
kubectl get pvc
kubectl get configmap
kubectl get secrets
kubectl get hpa
```

---

# 38. Inspecting a Pod

We used:

```bash
kubectl describe pod <pod-name>
```

This gives detailed information about a Pod.

We observed:

```text
Node
IP
Container
Image
Image ID
Resources
Liveness probe
Readiness probe
Environment sources
Events
```

This is one of the most useful debugging commands in Kubernetes.

---

# 39. Checking Pod events

We also used:

```bash
kubectl get events --sort-by=.lastTimestamp
```

This helps us understand what Kubernetes has been doing.

For example, we observed events such as:

```text
Scheduled
Pulled
Created
Started
Unhealthy
Killing
BackOff
```

These events helped us diagnose our broken health probes.

---

# 40. Reading container logs

Another important debugging command is:

```bash
kubectl logs <pod-name>
```

For a specific container:

```bash
kubectl logs <pod-name> -c backend
```

Logs help us understand application-level failures.

A useful debugging approach is:

```text
Pod problem
   ↓
kubectl describe pod
   ↓
Check Events
   ↓
kubectl logs
   ↓
Understand application failure
```

---

# 41. Executing commands inside a Pod

We also used:

```bash
kubectl exec
```

For example:

```bash
kubectl exec -it <pod-name> -- curl ...
```

This allows us to execute a command inside the running container.

We attempted to test:

```text
/actuator/health/liveness
```

from inside the Pod.

At one point we received:

```text
HTTP/1.1 401
```

because the endpoint was protected by Spring Security.

This was useful because it demonstrated an important practical issue:

> Kubernetes may be configured correctly, but the application's security configuration can still prevent the probe from reaching the endpoint successfully.

We then verified the external health endpoint behavior separately and corrected the configuration.

---

# 42. Why health endpoints should be accessible to Kubernetes probes

Kubernetes probes need to check application health.

If Spring Security requires authentication for:

```text
/actuator/health/liveness
```

then Kubernetes cannot provide a username/password unless the probe is configured for it.

Therefore, health endpoints used by Kubernetes should generally be configured so that the probes can access them appropriately.

Our final setup successfully used:

```text
/actuator/health/liveness
/actuator/health/readiness
```

for the Kubernetes probes.

---

# 43. Verifying the running image

After rebuilding and loading the image, we verified:

```bash
kubectl get pods -l app=backend \
-o jsonpath='{range .items[*]}{.metadata.name}{" -> "}{.status.containerStatuses[0].imageID}{"\n"}{end}'
```

Both Pods returned the same SHA256 image ID.

This gave us confidence that the Pods were running the newly loaded image.

---

# 44. Why we scaled to zero before replacing the image

Our local development workflow was:

```text
1. Stop backend Pods
2. Remove old image from Minikube
3. Load newly built image
4. Start backend Pods again
5. Verify Pods
6. Verify image ID
7. Test application
```

Commands:

```bash
kubectl scale deployment backend-deployment --replicas=0
```

Wait:

```bash
kubectl get pods -l app=backend
```

Then:

```bash
minikube image rm cloud-native-task-platform-backend:latest
```

Load:

```bash
minikube image load cloud-native-task-platform-backend:latest
```

Start:

```bash
kubectl scale deployment backend-deployment --replicas=2
```

Verify:

```bash
kubectl get pods -l app=backend
```

Then:

```bash
kubectl rollout status deployment backend-deployment
```

And finally:

```bash
kubectl get pods -l app=backend
```

---

# 45. Final end-to-end test

After our Kubernetes configuration was working, we tested the actual backend API through the Minikube address:

```bash
curl -i -X POST http://192.168.49.2/api/auth/register \
-H "Content-Type: application/json" \
-d '{"username":"testUser","email":"testUser@example.com","password":"User@123"}'
```

The response was:

```text
HTTP/1.1 200
```

and returned:

```json
{
  "createdAt": "2026-08-18T10:03:24.568191313",
  "email": "testUser@example.com",
  "id": 12,
  "role": "USER",
  "username": "testUser"
}
```

This confirmed that the Kubernetes-deployed backend was successfully processing the request.

---

# 46. What this test actually proves

The successful request was more important than simply seeing:

```text
Pod → Running
```

because it tested the complete application path.

The request went approximately through:

```text
curl
  |
  ↓
Minikube Node IP
  |
  ↓
Ingress
  |
  ↓
Backend Service
  |
  ↓
Backend Pod
  |
  ↓
Spring Boot application
  |
  ↓
PostgreSQL Service
  |
  ↓
PostgreSQL Pod
```

Therefore, the successful API response demonstrated that multiple Kubernetes concepts were working together.

---

# 47. Our current Kubernetes resource state

We verified:

```bash
kubectl get deployments
```

and had:

```text
backend-deployment
frontend-deployment
nginx-deployment
postgres-deployment
```

We verified:

```bash
kubectl get services
```

and had:

```text
backend-nodeport
backend-service
frontend-service
nginx-nodeport
nginx-service
postgres-service
```

We verified:

```bash
kubectl get ingress
```

and had:

```text
application-ingress
```

We verified:

```bash
kubectl get pvc
```

and had:

```text
postgres-pvc
```

We verified:

```bash
kubectl get hpa
```

and had:

```text
backend-hpa
```

This gives us a practical overview of the current Kubernetes environment.

---

# 48. Debugging workflow we learned

When something goes wrong in Kubernetes, we should not randomly run commands.

We can follow a systematic approach.

## Step 1 — Check Pods

```bash
kubectl get pods
```

Look for:

```text
Pending
CrashLoopBackOff
Error
0/1
```

---

## Step 2 — Describe the Pod

```bash
kubectl describe pod <pod-name>
```

Check:

```text
Events
Probes
Image
Environment
Resources
```

---

## Step 3 — Check logs

```bash
kubectl logs <pod-name>
```

---

## Step 4 — Check Deployment

```bash
kubectl get deployment
```

and:

```bash
kubectl describe deployment backend-deployment
```

---

## Step 5 — Check ReplicaSets

```bash
kubectl get rs
```

---

## Step 6 — Check Services

```bash
kubectl get services
```

and:

```bash
kubectl describe service backend-service
```

---

## Step 7 — Check Ingress

```bash
kubectl get ingress
```

and:

```bash
kubectl describe ingress application-ingress
```

---

## Step 8 — Check Events

```bash
kubectl get events --sort-by=.lastTimestamp
```

This workflow is much better than guessing.

---

# 49. Kubernetes practical workflow we followed

Our overall development workflow became:

```text
Change application code
        |
        ↓
Test locally
        |
        ↓
Build Docker image
        |
        ↓
Load image into Minikube
        |
        ↓
Update/restart Kubernetes workload
        |
        ↓
Check Pods
        |
        ↓
Check probes
        |
        ↓
Check Services
        |
        ↓
Check Ingress
        |
        ↓
Test API
```

This is the practical development loop we used while learning Kubernetes.

---

# 50. What we learned from the practical work

The most important lesson is that Kubernetes concepts are connected.

We did not learn these as isolated commands.

For example:

```text
Deployment
    ↓
ReplicaSet
    ↓
Pods
    ↓
Service
    ↓
Ingress
```

and:

```text
Pod
    ↓
kubelet
    ↓
Health Probes
    ↓
Self-healing
```

and:

```text
Deployment
    ↓
HPA
    ↓
Replica count
    ↓
More/Fewer Pods
```

and:

```text
PostgreSQL Pod
    ↓
PVC
    ↓
Persistent storage
```

Understanding these relationships is more important than memorizing commands.

---

# 51. Commands we should remember

## Cluster and resources

```bash
kubectl get nodes
kubectl get pods
kubectl get deployments
kubectl get rs
kubectl get services
kubectl get ingress
kubectl get pvc
kubectl get hpa
kubectl get all
```

## Detailed information

```bash
kubectl describe pod <pod-name>
kubectl describe deployment <deployment-name>
kubectl describe service <service-name>
kubectl describe ingress <ingress-name>
kubectl describe pvc <pvc-name>
```

## Logs and debugging

```bash
kubectl logs <pod-name>
kubectl exec -it <pod-name> -- <command>
kubectl get events --sort-by=.lastTimestamp
```

## Scaling

```bash
kubectl scale deployment backend-deployment --replicas=0
kubectl scale deployment backend-deployment --replicas=2
```

## Rollouts

```bash
kubectl rollout status deployment backend-deployment
kubectl rollout history deployment backend-deployment
kubectl rollout undo deployment backend-deployment
kubectl rollout restart deployment backend-deployment
```

## Apply configuration

```bash
kubectl apply -f <file>.yaml
```

## Minikube image workflow

```bash
docker build -t cloud-native-task-platform-backend:latest ./task-management-backend

minikube image rm cloud-native-task-platform-backend:latest

minikube image load cloud-native-task-platform-backend:latest
```

---

# 52. Final project mental model

Our practical Kubernetes architecture can be remembered as:

```text
                         USER
                           |
                           ↓
                        INGRESS
                           |
              +------------+------------+
              |                         |
              ↓                         ↓
      Frontend Service          Backend Service
              |                         |
              ↓                         ↓
       Frontend Pods             Backend Pods
                                        |
                                        ↓
                               PostgreSQL Service
                                        |
                                        ↓
                                PostgreSQL Pod
                                        |
                                        ↓
                                      PVC
```

The workloads are managed through:

```text
Deployment
    ↓
ReplicaSet
    ↓
Pods
```

The Pods are monitored through:

```text
Liveness Probe
Readiness Probe
```

The backend can scale through:

```text
HPA
```

The application is exposed externally through:

```text
Ingress
```

Configuration is provided through:

```text
ConfigMap
Secret
```

Database data is protected through:

```text
PersistentVolumeClaim
```

---

# 53. What we have successfully demonstrated

By the end of our practical Kubernetes work, we demonstrated that we can:

* Create Kubernetes Deployments
* Run multiple backend replicas
* Scale Deployments
* Understand ReplicaSets
* Use Services
* Use ClusterIP
* Use NodePort
* Use Ingress
* Use ConfigMaps
* Use Secrets
* Use PersistentVolumeClaims
* Configure liveness probes
* Configure readiness probes
* Observe readiness failures
* Observe liveness failures
* Observe container restarts
* Diagnose CrashLoopBackOff
* Rebuild Docker images
* Load images into Minikube
* Verify the running image ID
* Perform rollout operations
* Understand self-healing
* Understand rolling updates
* Use HPA
* Test the backend through the Kubernetes network
* Debug Kubernetes resources using `describe`, `logs`, and `events`

---

# 54. Final understanding

The most important thing is not the commands.

The important thing is understanding what happens underneath them.

When we run:

```bash
kubectl apply -f backend-deployment.yaml
```

we are declaring:

```text
"I want this backend workload
to exist in this configuration."
```

Kubernetes then works toward that desired state.

The Deployment manages the application revision.

The ReplicaSet maintains the required Pods.

The Pods run the containers.

The kubelet manages the Pods on the Worker Node.

The health probes tell Kubernetes whether the application is alive and ready.

The Service provides stable networking to the Pods.

Ingress provides external HTTP routing.

The HPA can change the number of replicas based on resource usage.

The PVC provides persistent storage for PostgreSQL.

The overall model is:

```text
                    DESIRED STATE
                         |
                         ↓
                    Kubernetes
                         |
          +--------------+--------------+
          |              |              |
          ↓              ↓              ↓
     Deployment       Services         HPA
          |              |              |
          ↓              ↓              |
     ReplicaSet       Networking        |
          |              |              |
          ↓              ↓              ↓
         Pods ←─────── Traffic ────── Scaling
          |
          ↓
      Containers
          |
          ↓
     Application
```

The most important principle remains:

```text
Desired State
      ↓
Kubernetes
      ↓
Actual State
      ↓
Reconciliation
      ↓
Keep the system working toward the desired state
```

That is the foundation of everything we have implemented in our Kubernetes sprint.

```
```
