# Kubernetes Persistent Storage — PV and PVC

## 1. Why do we need persistent storage?

We already learned that Pods are temporary.

For example, our PostgreSQL application is running inside a Pod:

```text
PostgreSQL Deployment
        |
        ↓
PostgreSQL Pod
        |
        ↓
PostgreSQL Container
````

At first, this seems fine.

But PostgreSQL stores important data inside its storage.

For example:

```text
Users
Tasks
Orders
Passwords
Application data
```

Now imagine the PostgreSQL Pod is deleted.

Kubernetes may create a new PostgreSQL Pod:

```text
Old PostgreSQL Pod
        ↓
      Deleted

        ↓

New PostgreSQL Pod
```

The new Pod is a new container environment.

If PostgreSQL's data was stored only inside the container's writable filesystem, the data could be lost when the container/storage is removed.

Therefore, databases need storage that exists independently of the Pod lifecycle.

This is where Kubernetes persistent storage comes in.

---

# 2. The basic problem

Without persistent storage:

```text
PostgreSQL Pod
      |
      ↓
Container filesystem
      |
      ↓
Database data
```

If the Pod/container is removed:

```text
Pod deleted
    ↓
Container storage removed
    ↓
Database data may be lost
```

We need:

```text
PostgreSQL Pod
      |
      ↓
Persistent Storage
      |
      ↓
Database data survives Pod recreation
```

The important idea is:

> **Application Pods are temporary, but important data should survive the lifecycle of those Pods.**

---

# 3. What is persistent storage?

Persistent storage is storage that exists independently of the temporary lifecycle of a Pod or container.

For example:

```text
Pod
 |
 ↓
Persistent Storage
 |
 ↓
Database data
```

If the Pod is recreated:

```text
Old Pod
   ↓
Deleted

New Pod
   ↓
Connects to the same persistent storage
```

The data can remain available.

---

# 4. Why is this especially important for databases?

Stateless applications can often be recreated from their image.

For example:

```text
Backend Pod
    ↓
Spring Boot application
    ↓
Docker image
```

If the backend Pod disappears, Kubernetes can create another one.

The application code comes from the image.

But a database contains state:

```text
PostgreSQL
   ↓
Users
Tasks
Orders
Other data
```

We don't want Kubernetes recreating the PostgreSQL Pod to mean:

> "Create a completely empty database."

We want:

```text
PostgreSQL Pod
      |
      ↓
Persistent Storage
      |
      ↓
Existing database data
```

---

# 5. Kubernetes storage architecture

Kubernetes provides several storage-related objects.

The two most important concepts for us are:

```text
PersistentVolume
        ↑
        |
PersistentVolumeClaim
        ↑
        |
      Pod
```

The basic relationship is:

```text
Pod
 ↓
PVC
 ↓
PV
 ↓
Actual Storage
```

Where:

```text
PVC
→ Request for storage

PV
→ Storage resource provided to Kubernetes

Actual Storage
→ The underlying place where the data is stored
```

---

# 6. What is a PersistentVolume?

A **PersistentVolume**, or **PV**, is a piece of storage made available to the Kubernetes cluster.

Simple definition:

> **A PersistentVolume is a Kubernetes resource representing persistent storage that can be used by workloads.**

Conceptually:

```text
Kubernetes Cluster
       |
       ↓
PersistentVolume
       |
       ↓
Persistent Storage
```

A PV can represent storage from different sources depending on the Kubernetes environment.

For example:

```text
Local storage
Cloud disk
Network storage
Other supported storage systems
```

We don't need to learn every storage backend now.

The important idea is:

```text
PV = storage resource
```

---

# 7. What is a PersistentVolumeClaim?

A **PersistentVolumeClaim**, or **PVC**, is a request for storage.

Simple definition:

> **A PersistentVolumeClaim is a request from a workload for persistent storage with specific requirements.**

For example, we can request:

```yaml
resources:
  requests:
    storage: 5Gi
```

This means:

> "I need 5 GiB of storage."

So:

```text
PV
→ Provides storage

PVC
→ Requests storage
```

---

# 8. Why do we need PVC?

A common beginner question is:

> "Why doesn't the Pod directly use the PV?"

Kubernetes separates the application from the underlying storage.

Instead of the Pod saying:

```text
"Give me this exact disk."
```

the application says:

```text
"I need 5Gi of persistent storage."
```

through a PVC.

Conceptually:

```text
Pod
 |
 | "I need persistent storage"
 ↓
PVC
 |
 | "Find suitable storage"
 ↓
PV
 |
 ↓
Actual Storage
```

This provides an abstraction between the application and the storage implementation.

---

# 9. PV vs PVC

This distinction is very important.

### PersistentVolume

```text
PV
→ Storage available to the cluster
```

### PersistentVolumeClaim

```text
PVC
→ Request for storage
```

Think of it like this:

```text
Storage Provider
       ↓
      PV
       ↑
       |
      PVC
       ↑
       |
      Pod
```

Or using a real-world analogy:

```text
PV
→ Available apartment

PVC
→ Application to rent an apartment

Pod
→ Person who needs the apartment
```

The analogy is not exact, but it helps remember the relationship.

---

# 10. Our PostgreSQL storage

Our project has:

```text
postgres-pvc
```

We verified it using:

```bash
kubectl get pvc
```

and saw:

```text
NAME           STATUS   VOLUME                                     CAPACITY   ACCESS MODES   STORAGECLASS
postgres-pvc   Bound    pvc-c39701de-c454-4033-bf8e-a54026d11120   5Gi        RWO            standard
```

This tells us that our PostgreSQL workload has a PVC requesting persistent storage.

The important information is:

```text
PVC:
postgres-pvc

Status:
Bound

Capacity:
5Gi

Access Mode:
RWO

StorageClass:
standard
```

---

# 11. What does `Bound` mean?

Our PVC shows:

```text
STATUS:
Bound
```

This is important.

It means the PVC has successfully been associated with a suitable PersistentVolume.

Conceptually:

```text
PVC
 |
 | storage request
 ↓
PV
 |
 | matched
 ↓
Bound
```

So:

```text
Pending
→ Storage has not been successfully assigned yet

Bound
→ PVC has been successfully associated with a PV
```

For our project:

```text
postgres-pvc
      |
      ↓
Bound
      |
      ↓
PersistentVolume
```

---

# 12. What is the StorageClass?

Our PVC shows:

```text
STORAGECLASS:
standard
```

A **StorageClass** describes a class or type of storage that Kubernetes can provision.

Simple idea:

> **A StorageClass tells Kubernetes what kind of storage should be created when a workload requests storage.**

Instead of manually creating every PV, Kubernetes can use a StorageClass to dynamically provision storage.

Conceptually:

```text
PVC
 |
 | "I need 5Gi"
 ↓
StorageClass
 |
 ↓
Provision storage
 |
 ↓
PV
```

---

# 13. Why was StorageClass introduced?

Imagine an organization has many applications:

```text
Application A → needs 5Gi
Application B → needs 20Gi
Application C → needs 100Gi
```

Manually creating storage for every application would be inconvenient.

A StorageClass allows Kubernetes to automate storage provisioning.

So instead of manually saying:

```text
Create this exact PV.
```

we can say:

```text
I need 5Gi using this StorageClass.
```

Kubernetes can then provision suitable storage.

---

# 14. Dynamic provisioning

When a PVC requests storage using a StorageClass, Kubernetes can dynamically create the required storage.

Conceptually:

```text
Pod
 ↓
PVC
 ↓
StorageClass
 ↓
Dynamic Provisioning
 ↓
PV
 ↓
Actual Storage
```

This is called:

> **Dynamic provisioning**

It is commonly used in cloud environments.

---

# 15. Static vs dynamic provisioning

There are two basic approaches.

## Static provisioning

An administrator creates a PV first:

```text
Administrator
      |
      ↓
PersistentVolume
      |
      ↓
PVC
      |
      ↓
Pod
```

The PVC can then bind to the suitable PV.

---

## Dynamic provisioning

The application creates a PVC:

```text
PVC
 |
 ↓
StorageClass
 |
 ↓
Kubernetes provisions storage
 |
 ↓
PV
 |
 ↓
Pod
```

Dynamic provisioning is more convenient for many modern Kubernetes environments.

For our project, we are using a StorageClass-based setup.

---

# 16. What is an Access Mode?

Our PVC shows:

```text
RWO
```

This means:

> **ReadWriteOnce**

The storage can be mounted for read/write use by a single node at a time.

Important:

```text
RWO
→ Read/write from one node
```

It does not simply mean:

> "Only one Pod can ever use this storage."

The exact behavior depends on the storage implementation and node placement.

For our PostgreSQL setup, RWO is a normal choice because PostgreSQL is running as a single database Pod.

---

# 17. Common access modes

The important access modes to know are:

### ReadWriteOnce — RWO

```text
Read + Write
from one node
```

### ReadOnlyMany — ROX

```text
Read-only
from many nodes
```

### ReadWriteMany — RWX

```text
Read + Write
from many nodes
```

The availability of a particular access mode depends on the underlying storage system.

We don't need to memorize every storage backend now.

For our project:

```text
postgres-pvc
→ RWO
```

---

# 18. Pod → PVC → PV

Now let's understand how our PostgreSQL Pod uses storage.

Conceptually:

```text
PostgreSQL Pod
      |
      ↓
PersistentVolumeClaim
postgres-pvc
      |
      ↓
PersistentVolume
      |
      ↓
Actual Storage
```

The Pod does not need to know the low-level details of the storage.

It simply uses the PVC.

---

# 19. How does a Pod use a PVC?

The Pod specification can contain a volume:

```yaml
volumes:
  - name: postgres-storage
    persistentVolumeClaim:
      claimName: postgres-pvc
```

Then the container mounts that volume:

```yaml
volumeMounts:
  - name: postgres-storage
    mountPath: /var/lib/postgresql/data
```

Conceptually:

```text
PostgreSQL Container
        |
        | mount
        ↓
/var/lib/postgresql/data
        |
        ↓
postgres-pvc
        |
        ↓
PV
        |
        ↓
Persistent Storage
```

This allows PostgreSQL's data directory to use persistent storage.

---

# 20. Why is the mount path important?

PostgreSQL stores its database files in a data directory.

If that directory is backed by persistent storage:

```text
/var/lib/postgresql/data
        ↓
Persistent Volume
```

then database data can survive the recreation of the PostgreSQL Pod.

The exact PostgreSQL data directory can depend on the PostgreSQL image/configuration, so we should always verify the actual mount path used by our deployment.

---

# 21. What happens if PostgreSQL Pod is deleted?

Suppose:

```text
PostgreSQL Pod
      |
      ↓
postgres-pvc
      |
      ↓
Persistent Storage
```

Now the Pod is deleted.

Kubernetes creates another PostgreSQL Pod.

The new Pod can mount the same PVC:

```text
Old Pod
   ↓
Deleted

New Pod
   |
   ↓
postgres-pvc
   |
   ↓
Same persistent storage
```

The important idea is:

```text
Pod lifecycle
      ≠
Data lifecycle
```

This is one of the most important reasons persistent storage exists.

---

# 22. What happens if a container restarts?

A container restart is different from deleting the storage.

For example:

```text
PostgreSQL container
      ↓
Crash
      ↓
Container restarted
```

If the database data is stored on the persistent volume, the data remains.

Similarly, if the Pod is recreated and the same PVC is mounted, the persistent data can remain.

---

# 23. Persistent storage does not mean automatic backup

This is a very important distinction.

A persistent volume helps data survive things such as:

```text
Pod recreation
Container recreation
Application restarts
```

But it does **not automatically mean**:

```text
Backup
Disaster recovery
Replication
Point-in-time recovery
```

Persistent storage and backup are different concepts.

For a production database, we would still need an appropriate backup and recovery strategy.

---

# 24. PersistentVolume lifecycle

A simplified lifecycle is:

```text
PV
 |
 +---- Available
 |
 +---- Bound
 |
 +---- Released
 |
 +---- Failed
```

At our level, the most important states are:

### Available

The PV is available to be claimed.

### Bound

The PV is associated with a PVC.

### Released

The PVC was deleted, but the PV may still contain information/data depending on its reclaim policy and storage implementation.

We don't need to deeply study all lifecycle states yet.

---

# 25. What happens when we delete a PVC?

This is an important practical warning.

A PVC is not simply a temporary object that we can delete without consequences.

Deleting a PVC can cause the associated storage to be released, and whether the underlying data is deleted depends on the storage and its reclaim policy.

Therefore:

> **Never casually delete a production database PVC.**

For our Minikube learning environment, we can experiment, but we should understand that storage deletion can result in data loss.

---

# 26. Reclaim policy

A PV can have a reclaim policy.

Common concepts include:

```text
Retain
Delete
```

### Retain

The storage is retained after the PVC is deleted.

This is useful when we want manual control over the data.

### Delete

The underlying dynamically provisioned storage may be deleted when the claim is released, depending on the provisioner.

The exact behavior depends on the storage implementation.

For production systems, reclaim policy should be chosen carefully.

---

# 27. Kubernetes storage architecture

Now combine the pieces:

```text
                    POD
                     |
                     ↓
                    PVC
                     |
                     ↓
                     PV
                     |
                     ↓
               StorageClass
                     |
                     ↓
          Storage Provisioner
                     |
                     ↓
              Actual Storage
```

The ordering is conceptual.

More accurately:

```text
PVC
  |
  +---- requests storage
  |
  ↓
StorageClass
  |
  ↓
Provisioner creates/provides storage
  |
  ↓
PV
  |
  ↓
Pod mounts PVC
```

The important relationship is:

```text
Pod → PVC → PV → Storage
```

---

# 28. What is the difference between a volume and a PersistentVolume?

We already encountered the word:

```text
volume
```

inside a Pod specification.

A Kubernetes volume is a storage mechanism that can be mounted into a Pod.

A PersistentVolume is a Kubernetes resource representing persistent storage that exists beyond a particular Pod.

For persistent application data, we commonly use:

```text
Pod
 ↓
PVC
 ↓
PV
```

So don't treat every Kubernetes `volume` and every `PersistentVolume` as exactly the same concept.

---

# 29. `emptyDir` vs persistent storage

A common Kubernetes volume is:

```yaml
emptyDir: {}
```

This creates temporary storage associated with the Pod.

Conceptually:

```text
Pod
 |
 ↓
emptyDir
```

If the Pod is removed, that storage is removed with the Pod.

This is different from:

```text
Pod
 |
 ↓
PVC
 |
 ↓
Persistent Storage
```

For important database data, we want persistent storage rather than an `emptyDir` volume.

---

# 30. Why shouldn't PostgreSQL use `emptyDir`?

Imagine:

```text
PostgreSQL Pod
     |
     ↓
emptyDir
     |
     ↓
Database data
```

If the Pod is deleted:

```text
Pod deleted
     ↓
emptyDir removed
     ↓
Database data lost
```

That would be unacceptable for our database.

Instead:

```text
PostgreSQL Pod
     |
     ↓
postgres-pvc
     |
     ↓
Persistent Storage
```

Now the storage is separate from the Pod lifecycle.

---

# 31. Our PostgreSQL architecture

Our current PostgreSQL architecture is:

```text
                 Backend Pod
                     |
                     ↓
             postgres-service
                     |
                     ↓
              PostgreSQL Pod
                     |
                     ↓
                postgres-pvc
                     |
                     ↓
             PersistentVolume
                     |
                     ↓
              Persistent Storage
```

The backend reaches PostgreSQL through the Service.

PostgreSQL stores its data through the PVC.

This gives us two separate abstractions:

```text
Service
→ Networking

PVC/PV
→ Storage
```

---

# 32. Our actual PVC

We verified:

```bash
kubectl get pvc
```

and got:

```text
NAME           STATUS   VOLUME                                     CAPACITY   ACCESS MODES   STORAGECLASS
postgres-pvc   Bound    pvc-c39701de-c454-4033-bf8e-a54026d11120   5Gi        RWO            standard
```

Let's interpret it:

```text
postgres-pvc
```

This is the claim created for PostgreSQL.

```text
Bound
```

The claim has been associated with storage.

```text
5Gi
```

The available/requested capacity shown is 5 GiB.

```text
RWO
```

ReadWriteOnce.

```text
standard
```

The StorageClass used for provisioning.

---

# 33. Useful commands

### List PVCs

```bash
kubectl get pvc
```

---

### Describe a PVC

```bash
kubectl describe pvc postgres-pvc
```

This can show:

```text
StorageClass
Capacity
Access Modes
Volume
Events
```

---

### List PersistentVolumes

```bash
kubectl get pv
```

---

### Describe a PV

```bash
kubectl describe pv
```

---

### List StorageClasses

```bash
kubectl get storageclass
```

---

### View a StorageClass

```bash
kubectl describe storageclass standard
```

---

### View PostgreSQL Pod

```bash
kubectl get pods -l app=postgres
```

---

### Describe PostgreSQL Pod

```bash
kubectl describe pod -l app=postgres
```

This is useful for checking:

```text
Volumes
Mounts
PVC
```

---

# 34. How to verify that a Pod uses a PVC

We can inspect the PostgreSQL Pod:

```bash
kubectl describe pod -l app=postgres
```

Look for:

```text
Volumes:
```

and:

```text
Mounts:
```

We should see a relationship similar to:

```text
Volumes:
  postgres-storage:
    PersistentVolumeClaim:
      ClaimName: postgres-pvc
```

and inside the container:

```text
Mounts:
  /var/lib/postgresql/data
```

The exact mount path depends on our deployment.

This confirms:

```text
Pod
 ↓
PVC
```

---

# 35. How to verify the PV/PVC relationship

Run:

```bash
kubectl get pvc
```

We see:

```text
postgres-pvc
    |
    ↓
VOLUME
pvc-c39701de-...
```

Then:

```bash
kubectl get pv
```

We can find the corresponding PV.

Conceptually:

```text
postgres-pvc
      |
      ↓
pvc-c39701de-...
      |
      ↓
PersistentVolume
```

The PVC's `VOLUME` column shows which PV it is bound to.

---

# 36. Storage and Pod recreation

Suppose our PostgreSQL Pod is:

```text
postgres-pod-A
```

and it uses:

```text
postgres-pvc
```

Now the Pod is recreated:

```text
postgres-pod-A
      ↓
Deleted

postgres-pod-B
```

The new Pod can mount:

```text
postgres-pvc
```

So:

```text
postgres-pod-A
      |
      ↓
postgres-pvc
      |
      ↓
Persistent Storage


postgres-pod-B
      |
      ↓
postgres-pvc
      |
      ↓
Same Persistent Storage
```

This is the core benefit.

---

# 37. Storage is independent from the application image

Our PostgreSQL container image contains the PostgreSQL software.

For example:

```text
PostgreSQL Image
      |
      ↓
PostgreSQL application
```

The persistent storage contains:

```text
Database data
```

So conceptually:

```text
Container Image
→ Software

Persistent Storage
→ Data
```

This separation is extremely important.

---

# 38. Why container images shouldn't be used as database storage

A Docker image is intended to package application software and its dependencies.

Database data changes constantly.

For example:

```text
User created
Task created
Task updated
User deleted
```

We don't want to rebuild the PostgreSQL image every time data changes.

Instead:

```text
PostgreSQL Image
→ PostgreSQL software

PVC
→ PostgreSQL data
```

This is a fundamental containerization principle.

---

# 39. Stateful vs stateless applications

This is an important concept connected to persistent storage.

### Stateless application

A stateless application does not depend on local Pod storage for important persistent data.

For example:

```text
Spring Boot Backend
```

If one backend Pod disappears:

```text
Pod deleted
   ↓
New Pod
   ↓
Application starts again
```

The important data is stored elsewhere, such as PostgreSQL.

### Stateful application

A stateful application depends on persistent data.

For example:

```text
PostgreSQL
```

Its database contents must survive Pod recreation.

Therefore:

```text
Backend
→ Mostly stateless

PostgreSQL
→ Stateful
```

This is why persistent storage is especially important for databases.

---

# 40. Stateful applications in Kubernetes

Kubernetes has a workload object specifically designed for stateful applications:

```text
StatefulSet
```

We are currently using:

```text
Deployment
```

for PostgreSQL in our learning project.

For a production-grade Kubernetes architecture, databases often require more careful stateful management and operational considerations, and StatefulSet is an important Kubernetes concept to learn.

We do not need to replace our current PostgreSQL Deployment immediately.

For now, understand:

```text
Deployment
→ Commonly used for stateless workloads

StatefulSet
→ Designed for workloads requiring stable identity and stateful behavior
```

We will study StatefulSets only if they become necessary for our project architecture.

---

# 41. Persistent storage does not automatically make a database highly available

This is another important distinction.

Suppose:

```text
PostgreSQL Pod
      |
      ↓
Persistent Storage
```

If the Pod crashes, Kubernetes can recreate it and reconnect it to the storage.

That provides persistence.

But it does not automatically provide:

```text
Multiple database replicas
Automatic database failover
Zero-downtime database operations
Database replication
Automatic backups
```

Those are separate concerns.

So:

```text
Persistent Storage
≠
Database High Availability
```

---

# 42. Storage and Kubernetes self-healing

Now connect this with our earlier learning.

Suppose:

```text
PostgreSQL Pod
```

fails.

Kubernetes can recreate the Pod.

The storage remains associated through the PVC.

So:

```text
Pod failure
    ↓
Kubernetes recreates Pod
    ↓
New Pod mounts PVC
    ↓
Existing persistent data is available
```

This combines two Kubernetes concepts:

```text
Self-healing
+
Persistent Storage
```

---

# 43. Storage and Services work together

Our PostgreSQL architecture uses both networking and storage.

```text
Backend
   |
   ↓
postgres-service
   |
   ↓
PostgreSQL Pod
   |
   ↓
postgres-pvc
   |
   ↓
Persistent Storage
```

The responsibilities are:

```text
postgres-service
→ How do we reach PostgreSQL?

postgres-pvc
→ Where does PostgreSQL's persistent data live?
```

This is a very useful way to understand Kubernetes architecture.

---

# 44. Common beginner mistakes

## Mistake 1 — Thinking PVC is the actual disk

PVC is a **claim/request** for storage.

```text
PVC
→ "I need storage"
```

PV represents the storage resource:

```text
PV
→ "Here is storage"
```

---

## Mistake 2 — Thinking deleting a Pod deletes the PVC

Normally:

```text
Pod deleted
    ↓
PVC remains
```

The Pod and PVC have separate lifecycles.

---

## Mistake 3 — Thinking persistent storage means backup

It doesn't.

```text
Persistence
≠
Backup
```

---

## Mistake 4 — Thinking a Service stores data

It doesn't.

```text
Service
→ Networking

PVC/PV
→ Storage
```

---

## Mistake 5 — Thinking the database container image stores the database permanently

The image provides PostgreSQL software.

Persistent storage provides the database data.

```text
Image
→ Software

PVC/PV
→ Data
```

---

# 45. Interview explanations

## What is a PersistentVolume?

> "A PersistentVolume is a Kubernetes resource that represents persistent storage available to the cluster."

---

## What is a PersistentVolumeClaim?

> "A PersistentVolumeClaim is a request for persistent storage made by a workload. It specifies requirements such as storage capacity and access mode."

---

## What is the difference between PV and PVC?

> "A PV represents the storage resource, while a PVC is a request for that storage. A Pod normally uses the PVC, which is then bound to a suitable PV."

---

## Why do we need persistent storage?

> "Pods are temporary, so data stored only inside a Pod or container can be lost when the Pod is recreated. Persistent storage allows important data, such as database data, to survive Pod recreation."

---

## What is StorageClass?

> "A StorageClass defines a class of storage and allows Kubernetes to dynamically provision storage when a PVC requests it."

---

## What is dynamic provisioning?

> "Dynamic provisioning allows Kubernetes to automatically provision a suitable PersistentVolume when a PersistentVolumeClaim requests storage through a StorageClass."

---

## What is RWO?

> "ReadWriteOnce means the volume can be mounted for read/write access by a single node at a time. The exact behavior depends on the underlying storage implementation."

---

## Does persistent storage provide backups?

> "No. Persistent storage helps data survive workload recreation, but backups and disaster recovery are separate concerns."

---

# 46. What we should remember

Don't memorize every storage implementation.

Remember this:

```text
Pod is temporary
      ↓
Important data should not depend on Pod lifetime
      ↓
PVC requests persistent storage
      ↓
PVC binds to PV
      ↓
PV represents persistent storage
      ↓
Pod mounts the PVC
      ↓
Data survives Pod recreation
```

For our PostgreSQL:

```text
Backend
   |
   ↓
postgres-service
   |
   ↓
PostgreSQL Pod
   |
   ↓
postgres-pvc
   |
   ↓
PersistentVolume
   |
   ↓
Persistent Storage
```

---

# 47. Final mental model

The easiest way to remember Kubernetes persistent storage is:

```text
                    APPLICATION POD
                           |
                           |
                     needs storage
                           |
                           ↓
                         PVC
                  "I need 5Gi storage"
                           |
                           ↓
                          PV
                 "Here is the storage"
                           |
                           ↓
                  ACTUAL STORAGE
                           |
                           ↓
                    DATABASE DATA
```

And the most important principle is:

```text
Pod lifecycle
      ≠
Data lifecycle
```

For our project:

```text
PostgreSQL Pod
      |
      ↓
postgres-pvc
      |
      ↓
PersistentVolume
      |
      ↓
Persistent Storage
```

If PostgreSQL's Pod is recreated:

```text
Old PostgreSQL Pod
       ↓
     Deleted

New PostgreSQL Pod
       ↓
Mounts postgres-pvc
       ↓
Existing data remains available
```

---

# 48. What we have learned so far

Our Kubernetes concepts now connect like this:

```text
Deployment
    ↓
ReplicaSet
    ↓
Pods
    |
    +--------------------+
    |                    |
    ↓                    ↓
 Service             PVC
    |                    |
    ↓                    ↓
Networking           PV
                         |
                         ↓
                    Persistent
                     Storage
```

And for the complete application:

```text
                         USER
                           |
                           ↓
                        INGRESS
                           |
              +------------+------------+
              |                         |
              ↓                         ↓
      Backend Service            Frontend Service
              |                         |
              ↓                         ↓
        Backend Pods              Frontend Pods
              |
              ↓
      PostgreSQL Service
              |
              ↓
       PostgreSQL Pod
              |
              ↓
        postgres-pvc
              |
              ↓
             PV
              |
              ↓
      Persistent Storage
```

The key responsibilities are:

```text
Deployment
→ Manages application rollout and desired replicas

ReplicaSet
→ Maintains the required number of Pods

Pod
→ Runs the application container(s)

Service
→ Provides stable networking to Pods

Ingress
→ Routes external HTTP/HTTPS traffic to Services

PVC
→ Requests persistent storage

PV
→ Represents persistent storage

StorageClass
→ Defines/provisions a class of storage
```

This gives us the foundation needed to understand the next Kubernetes concept: **Horizontal Pod Autoscaling (HPA)**.

```
```
