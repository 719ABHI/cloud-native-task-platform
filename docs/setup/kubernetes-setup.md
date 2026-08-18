# Kubernetes Setup Guide

This guide explains how to set up the local Kubernetes environment used by the **Cloud Native Task Management Platform**.

The project currently uses:

- Docker
- kubectl
- Minikube
- Kubernetes manifests stored in the `kubernetes/` directory

The Kubernetes environment is used for local development and testing.

---

## 1. Prerequisites

Make sure the following tools are installed:

- Docker
- kubectl
- Minikube

Verify the installations:

```bash
docker --version
````

```bash
kubectl version --client
```

```bash
minikube version
```

---

## 2. Start Minikube

The project uses Minikube with Docker as the driver.

Start the Kubernetes cluster:

```bash
minikube start --driver=docker
```

Verify that Minikube is running:

```bash
minikube status
```

A healthy cluster should show the main components as running.

---

## 3. Verify the Kubernetes Cluster

Check the available nodes:

```bash
kubectl get nodes
```

Our local Minikube environment uses a node named:

```text
minikube
```

You should see the node in a `Ready` state.

Example:

```text
NAME       STATUS   ROLES           AGE   VERSION
minikube   Ready    control-plane   ...   ...
```

---

## 4. Verify kubectl Communication

Check the Kubernetes cluster information:

```bash
kubectl cluster-info
```

This verifies that `kubectl` can communicate with the Kubernetes API Server.

---

## 5. Project Kubernetes Manifests

The Kubernetes configuration files are stored in:

```text
kubernetes/
```

The directory contains resources for:

```text
Backend
Frontend
PostgreSQL
Services
Configuration
Secrets
Persistent Storage
Autoscaling
Ingress
```

The current structure is:

```text
kubernetes/
├── backend-deployment.yaml
├── backend-service.yaml
├── backend-nodeport.yaml
├── backend-configmap.yaml
├── backend-secret.yaml
├── backend-hpa.yaml
├── frontend-deployment.yaml
├── frontend-service.yaml
├── postgres-deployment.yaml
├── postgres-service.yaml
├── postgres-configmap.yaml
├── postgres-secret.yaml
├── postgres-pvc.yaml
└── ingress.yaml
```

> The exact filenames may evolve as the project continues.

---

## 6. Build the Docker Images

Before deploying the application, the required Docker images must be available to the Kubernetes environment.

Build the backend image:

```bash
docker build -t cloud-native-task-platform-backend:latest ./task-management-backend
```

Build the frontend image:

```bash
docker build -t cloud-native-task-platform-frontend:latest ./task-management-frontend
```

Verify the images:

```bash
docker images
```

---

## 7. Make Images Available to Minikube

When using Minikube, the Kubernetes nodes need access to the images used by the Pods.

One approach is to load the locally built images into Minikube:

```bash
minikube image load cloud-native-task-platform-backend:latest
```

```bash
minikube image load cloud-native-task-platform-frontend:latest
```

Verify the images available to Minikube if required:

```bash
minikube image ls
```

The Kubernetes Deployments use the corresponding image names.

---

## 8. Apply Kubernetes Resources

From the project root:

```bash
kubectl apply -f kubernetes/
```

Kubernetes will create or update the resources defined by the manifests.

---

## 9. Verify Pods

Check the Pods:

```bash
kubectl get pods
```

The project should contain Pods for the main application components.

For example:

```text
Backend Pods
Frontend Pods
PostgreSQL Pod
NGINX Pods
```

Wait until the required Pods reach:

```text
STATUS: Running
```

and:

```text
READY: 1/1
```

---

## 10. Verify Deployments

Check the Deployments:

```bash
kubectl get deployments
```

The current project includes:

```text
backend-deployment
frontend-deployment
postgres-deployment
nginx-deployment
```

Example:

```text
NAME                  READY
backend-deployment    2/2
frontend-deployment   3/3
postgres-deployment   1/1
nginx-deployment      5/5
```

The exact replica count can change because the backend uses HPA.

---

## 11. Verify ReplicaSets

Check the ReplicaSets:

```bash
kubectl get replicasets
```

Deployments create and manage ReplicaSets.

The current ReplicaSet maintains the running Pods for each Deployment.

Older ReplicaSets may also be present because Kubernetes keeps previous revisions for Deployment rollouts.

---

## 12. Verify Services

Check the Services:

```bash
kubectl get services
```

The project currently contains Services such as:

```text
backend-service
backend-nodeport
frontend-service
nginx-service
nginx-nodeport
postgres-service
```

The important internal Services include:

```text
backend-service
postgres-service
```

These provide stable communication between application components.

---

## 13. Verify Kubernetes DNS

Services can be accessed inside the Kubernetes cluster using their Service names.

For example:

```text
backend-service
```

and:

```text
postgres-service
```

The full DNS format is:

```text
<service-name>.<namespace>.svc.cluster.local
```

For example:

```text
backend-service.default.svc.cluster.local
```

The project uses Kubernetes DNS for service discovery rather than depending on changing Pod IP addresses.

---

## 14. Verify ConfigMaps

Check ConfigMaps:

```bash
kubectl get configmaps
```

The project uses ConfigMaps for non-sensitive application configuration.

For example:

```text
backend-config
```

You can inspect a ConfigMap using:

```bash
kubectl describe configmap backend-config
```

---

## 15. Verify Secrets

Check Secrets:

```bash
kubectl get secrets
```

The project uses Kubernetes Secrets for sensitive configuration such as database credentials.

For example:

```text
backend-secret
postgres-secret
```

Inspect Secret metadata:

```bash
kubectl describe secret backend-secret
```

> Avoid exposing secret values in logs, documentation, screenshots, or Git commits.

---

## 16. Verify Persistent Storage

Check PersistentVolumeClaims:

```bash
kubectl get pvc
```

The project currently uses:

```text
postgres-pvc
```

The expected status is:

```text
Bound
```

Example:

```text
NAME           STATUS   VOLUME   CAPACITY   ACCESS MODES
postgres-pvc   Bound    ...      5Gi        RWO
```

A bound PVC means that Kubernetes has successfully associated the claim with a PersistentVolume.

---

## 17. Verify Health Probes

The backend uses:

* Liveness Probe
* Readiness Probe

Check the probes:

```bash
kubectl describe pod -l app=backend
```

Look for:

```text
Liveness:
Readiness:
```

The backend probes use Spring Boot Actuator endpoints:

```text
/actuator/health/liveness
/actuator/health/readiness
```

---

## 18. Verify Metrics Server

The HPA requires resource metrics.

Check the Metrics Server:

```bash
kubectl get pods -n kube-system
```

Look for the Metrics Server Pod.

You can also test resource metrics using:

```bash
kubectl top pods
```

If metrics are available, Pods should display CPU and memory usage.

Example:

```text
NAME                       CPU(cores)   MEMORY(bytes)
backend-deployment-...     ...          ...
```

---

## 19. Verify Horizontal Pod Autoscaler

Check the HPA:

```bash
kubectl get hpa
```

The backend HPA currently uses:

```text
Minimum replicas : 2
Maximum replicas : 5
CPU target       : 70%
```

Example:

```text
NAME          REFERENCE                       TARGETS   MINPODS   MAXPODS   REPLICAS
backend-hpa   Deployment/backend-deployment   .../70%   2         5         2
```

The exact CPU utilization and replica count can change depending on current workload.

---

## 20. Verify Ingress

Check the Ingress:

```bash
kubectl get ingress
```

The current application uses:

```text
application-ingress
```

The Minikube environment provides the local address used to access the application.

Get the Minikube IP:

```bash
minikube ip
```

For the current setup, the Ingress address was:

```text
192.168.49.2
```

The exact IP can differ between environments.

---

## 21. Verify the Complete Application

After all Kubernetes resources are running, test the application through the Ingress.

For example:

```bash
curl -i -X POST http://<minikube-ip>/api/auth/register \
-H "Content-Type: application/json" \
-d '{"username":"testUser","email":"testUser@example.com","password":"User@123"}'
```

Example:

```bash
curl -i -X POST http://192.168.49.2/api/auth/register \
-H "Content-Type: application/json" \
-d '{"username":"testUser","email":"testUser@example.com","password":"User@123"}'
```

A successful response confirms that the request can travel through the Kubernetes networking path to the backend application.

Conceptually:

```text
Browser / curl
      |
      ▼
NGINX Ingress
      |
      ▼
Backend Service
      |
      ▼
Backend Pod
      |
      ▼
Spring Boot
```

---

## 22. Verify the Backend Image

To check which image is actually running inside backend Pods:

```bash
kubectl get pods -l app=backend \
-o jsonpath='{range .items[*]}{.metadata.name}{" -> "}{.status.containerStatuses[0].imageID}{"\n"}{end}'
```

This can be useful when verifying that the expected Docker image is running.

---

## 23. Check the Complete Kubernetes Environment

A quick overview of the deployed resources:

```bash
kubectl get all
```

Additional resources:

```bash
kubectl get pvc
```

```bash
kubectl get ingress
```

```bash
kubectl get hpa
```

```bash
kubectl get configmaps
```

```bash
kubectl get secrets
```

---

## 24. Useful Troubleshooting Commands

### Pod is not running

```bash
kubectl get pods
```

Then:

```bash
kubectl describe pod <pod-name>
```

Check the Events section at the bottom.

---

### Check application logs

```bash
kubectl logs <pod-name>
```

For the backend:

```bash
kubectl logs <backend-pod-name>
```

---

### Check Deployment status

```bash
kubectl rollout status deployment backend-deployment
```

---

### Check Deployment history

```bash
kubectl rollout history deployment backend-deployment
```

---

### Check Service details

```bash
kubectl describe service backend-service
```

---

### Check Ingress details

```bash
kubectl describe ingress application-ingress
```

---

### Check HPA details

```bash
kubectl describe hpa backend-hpa
```

---

### Check PVC details

```bash
kubectl describe pvc postgres-pvc
```

---

## 25. Scaling the Backend Manually

For testing Kubernetes scaling behavior:

```bash
kubectl scale deployment backend-deployment --replicas=2
```

Check:

```bash
kubectl get pods -l app=backend
```

To scale down:

```bash
kubectl scale deployment backend-deployment --replicas=0
```

Then restore:

```bash
kubectl scale deployment backend-deployment --replicas=2
```

> When HPA is configured, the HPA can also adjust the replica count based on resource utilization.

---

## 26. Restarting the Backend Deployment

To restart the backend Pods:

```bash
kubectl rollout restart deployment backend-deployment
```

Check the rollout:

```bash
kubectl rollout status deployment backend-deployment
```

Then verify the Pods:

```bash
kubectl get pods -l app=backend
```

---

## 27. Stopping Minikube

To stop the local Kubernetes cluster:

```bash
minikube stop
```

This stops the cluster without deleting its configuration.

---

## 28. Starting the Existing Cluster Again

If Minikube was previously stopped:

```bash
minikube start
```

Verify:

```bash
kubectl get nodes
```

---

## 29. Deleting the Minikube Cluster

If the local cluster needs to be completely removed:

```bash
minikube delete
```

This deletes the Minikube cluster and its resources.

After deletion, a new cluster can be created with:

```bash
minikube start --driver=docker
```

---

## 30. Kubernetes Setup Verification Checklist

Before considering the local Kubernetes environment ready, verify:

```text
[ ] Docker is installed and running
[ ] kubectl is installed
[ ] Minikube is installed
[ ] Minikube cluster is running
[ ] Kubernetes node is Ready
[ ] Kubernetes manifests are available
[ ] Backend image is available to Minikube
[ ] Frontend image is available to Minikube
[ ] Backend Pods are Running
[ ] Frontend Pods are Running
[ ] PostgreSQL Pod is Running
[ ] NGINX Pods are Running
[ ] Deployments are Ready
[ ] Services are available
[ ] ConfigMaps are created
[ ] Secrets are created
[ ] PostgreSQL PVC is Bound
[ ] Metrics Server is running
[ ] HPA is available
[ ] Ingress is available
[ ] Application request succeeds
```

---

## 31. Current Project Kubernetes Environment

The Kubernetes environment used during Sprint 07 was:

```text
Local Machine
     |
     ▼
Docker
     |
     ▼
Minikube
     |
     ▼
Kubernetes Cluster
     |
     +-------------------+
     |                   |
     ▼                   ▼
Control Plane        Worker Node
                         |
          +--------------+--------------+
          |              |              |
       Backend         Frontend      PostgreSQL
          |              |              |
       Service         Service        Service
          |              |              |
          +--------------+--------------+
                         |
                       Ingress
                         |
                         ▼
                       User
```

The project was successfully tested by sending an API request through the local Kubernetes Ingress and receiving a successful response from the Spring Boot backend.

---

## Related Documentation

Detailed Kubernetes concepts are documented separately:

```text
docs/kubernetes/
├── 01-kubernetes-basics.md
├── 02-kubernetes-architecture.md
├── 03-pods.md
├── 04-deployments-and-replicasets.md
├── 05-services.md
├── 06-kubernetes-dns-and-coredns.md
├── 07-kubernetes-networking.md
├── 08-configmaps-and-secrets.md
├── 09-health-probes.md
├── 10-ingress.md
├── 11-persistent-storage-pv-pvc.md
├── 12-horizontal-pod-autoscaling.md
├── 13-self-healing-and-rollouts.md
└── 14-kubernetes-practical-work.md
```

This setup guide focuses on **how to prepare, deploy, verify, test, troubleshoot, and manage the local Kubernetes environment**. It does not replace the detailed Kubernetes concept notes.

```
```
