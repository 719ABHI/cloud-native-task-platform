# 08 — ConfigMaps and Secrets

## 1. Why do we need ConfigMaps and Secrets?

Our application needs configuration to run.

For example, our Spring Boot backend may need information such as:

```text
Database host
Database port
Database name
Database username
Database password
JWT configuration
Other application settings
```

One way would be to put these values directly inside the Docker image or Kubernetes Deployment:

```yaml
env:
  - name: DB_HOST
    value: "postgres-service"

  - name: DB_PASSWORD
    value: "mypassword"
```

But this creates problems.

For example:

* Configuration becomes tightly coupled to the application image.
* We may need different configuration for development, testing, and production.
* Sensitive information such as passwords should not be written directly into normal configuration files.
* Changing configuration should not require rebuilding the Docker image.

Kubernetes provides two important objects for this:

```text
ConfigMap
    ↓
Non-sensitive configuration

Secret
    ↓
Sensitive configuration
```

---

# 2. What is a ConfigMap?

A **ConfigMap** is a Kubernetes object used to store non-sensitive configuration data.

Simple definition:

> A ConfigMap stores configuration separately from the application so that the application can use that configuration without putting it directly inside the container image.

For example:

```text
ConfigMap
   |
   +── DB_HOST
   +── DB_PORT
   +── DB_NAME
```

The application can then receive these values as environment variables.

---

# 3. Why was ConfigMap introduced?

Imagine our backend Docker image contains:

```text
Spring Boot application
+
Database configuration
```

Now suppose we move from:

```text
Development database
```

to:

```text
Production database
```

We shouldn't have to rebuild the application image just because the database address changed.

Instead:

```text
Application Image
       |
       | same image
       ↓
Different configuration
```

For example:

```text
Development

DB_HOST=localhost
```

and:

```text
Kubernetes

DB_HOST=postgres-service
```

The application image can remain the same.

This is one of the main benefits of externalizing configuration.

---

# 4. Configuration should be separated from application code

Think about our architecture:

```text
Docker Image
     |
     ↓
Spring Boot Application
```

Configuration is kept separately:

```text
ConfigMap
     |
     ↓
Environment Variables
     |
     ↓
Spring Boot Application
```

So:

```text
Application
    +
Configuration
```

are separated.

This makes the application easier to deploy in different environments.

---

# 5. Example ConfigMap

A simple ConfigMap can look like:

```yaml
apiVersion: v1
kind: ConfigMap

metadata:
  name: backend-config

data:
  DB_HOST: postgres-service
  DB_PORT: "5432"
  DB_NAME: cloud_native_task_platform
```

The important section is:

```yaml
data:
```

This contains the configuration values.

---

# 6. Understanding the ConfigMap fields

### `apiVersion`

```yaml
apiVersion: v1
```

Specifies the Kubernetes API version for the object.

---

### `kind`

```yaml
kind: ConfigMap
```

Tells Kubernetes that this object is a ConfigMap.

---

### `metadata.name`

```yaml
metadata:
  name: backend-config
```

This gives the ConfigMap its name.

Our backend Deployment can refer to:

```text
backend-config
```

---

### `data`

```yaml
data:
  DB_HOST: postgres-service
  DB_PORT: "5432"
```

This contains the actual configuration.

---

# 7. How does our backend use the ConfigMap?

Our Deployment contains:

```yaml
envFrom:
  - configMapRef:
      name: backend-config
```

This means:

> "Load the configuration values from the `backend-config` ConfigMap into the container's environment."

Conceptually:

```text
backend-config
      |
      ↓
ConfigMap values
      |
      ↓
Environment variables
      |
      ↓
Backend container
      |
      ↓
Spring Boot
```

---

# 8. What is `envFrom`?

We used:

```yaml
envFrom:
  - configMapRef:
      name: backend-config
```

`envFrom` means:

> Load environment variables from the referenced configuration source.

For example, if the ConfigMap contains:

```yaml
data:
  DB_HOST: postgres-service
  DB_PORT: "5432"
```

the container receives:

```text
DB_HOST=postgres-service
DB_PORT=5432
```

---

# 9. `envFrom` vs individual environment variables

There are two common ways to load configuration.

### Using `envFrom`

```yaml
envFrom:
  - configMapRef:
      name: backend-config
```

This loads all the keys from the ConfigMap.

---

### Referencing one specific value

We can also do:

```yaml
env:
  - name: DB_HOST
    valueFrom:
      configMapKeyRef:
        name: backend-config
        key: DB_HOST
```

This loads only the specified key.

For our project, we are using:

```yaml
envFrom
```

because we want to load the configuration values from the ConfigMap into the backend container.

---

# 10. ConfigMap does not store application code

A ConfigMap is not for storing:

```text
Java source code
Docker images
JAR files
Application binaries
```

It is for configuration.

Think:

```text
Application
→ Docker image

Configuration
→ ConfigMap
```

---

# 11. What is a Secret?

A **Secret** is a Kubernetes object designed to store sensitive configuration data.

Examples include:

```text
Database password
JWT secret
API key
Token
Credentials
```

Simple definition:

> A Secret stores sensitive configuration separately from the application.

Conceptually:

```text
Secret
   |
   +── DB_USERNAME
   +── DB_PASSWORD
   +── JWT_SECRET
```

---

# 12. Why do we need Secrets?

Some configuration values should not be treated like normal configuration.

For example:

```text
DB_HOST=postgres-service
```

is generally not sensitive.

But:

```text
DB_PASSWORD=MyVerySecretPassword
```

is sensitive.

We therefore separate them:

```text
ConfigMap
   ↓
Non-sensitive configuration


Secret
   ↓
Sensitive configuration
```

---

# 13. Example Secret

A Secret can look like:

```yaml
apiVersion: v1
kind: Secret

metadata:
  name: backend-secret

type: Opaque

stringData:
  DB_USERNAME: cloud_native_user
  DB_PASSWORD: mypassword
  JWT_SECRET: my-secret
```

For learning purposes, `stringData` is convenient because we can provide the values as normal strings.

---

# 14. `stringData` vs `data`

Kubernetes Secrets commonly support:

```yaml
data:
```

and:

```yaml
stringData:
```

### `stringData`

Allows us to provide plain text values in the manifest.

Example:

```yaml
stringData:
  DB_PASSWORD: mypassword
```

Kubernetes handles the conversion for the Secret representation.

### `data`

Values under `data` are expected to be base64-encoded.

For example:

```yaml
data:
  DB_PASSWORD: <base64-value>
```

Important:

> Base64 is encoding, not encryption.

This is an important security concept.

---

# 15. Why does Kubernetes use base64 for Secret data?

Base64 allows binary or arbitrary data to be represented safely as text.

But base64 does **not** make a password secret.

For example:

```text
mypassword
```

can be encoded into:

```text
bXlwYXNzd29yZA==
```

Anyone can decode it.

Therefore:

```text
Base64 ≠ Encryption
```

A Kubernetes Secret should not be considered completely secure simply because its value is base64 encoded.

---

# 16. How does our backend use the Secret?

Our Deployment contains:

```yaml
envFrom:
  - configMapRef:
      name: backend-config

  - secretRef:
      name: backend-secret
```

This means the backend container receives configuration from both sources.

Conceptually:

```text
                Backend Deployment
                       |
              +--------+--------+
              |                 |
              ↓                 ↓
         ConfigMap           Secret
              |                 |
              ↓                 ↓
       Non-sensitive        Sensitive
       configuration        configuration
              |                 |
              +--------+--------+
                       |
                       ↓
               Backend Container
                       |
                       ↓
                 Spring Boot
```

---

# 17. Our actual backend configuration architecture

Our Deployment contains:

```yaml
envFrom:
  - configMapRef:
      name: backend-config

  - secretRef:
      name: backend-secret
```

So our application receives:

```text
backend-config
      +
backend-secret
      ↓
Environment variables
      ↓
Spring Boot application
```

This allows our application image to remain separate from environment-specific configuration.

---

# 18. Why not put configuration directly in the Dockerfile?

Suppose we did:

```dockerfile
ENV DB_HOST=postgres-service
ENV DB_PASSWORD=mypassword
```

Now the configuration is built into the image.

This creates problems.

If the password changes:

```text
New password
     ↓
Docker image needs rebuilding
```

With Kubernetes configuration objects:

```text
Secret changes
     ↓
Configuration changes
```

The Docker image doesn't need to contain the environment-specific configuration.

---

# 19. Why not put the password directly in Deployment YAML?

We could technically write:

```yaml
env:
  - name: DB_PASSWORD
    value: "mypassword"
```

But this is a bad practice because sensitive information becomes directly visible in the Deployment manifest.

Instead:

```yaml
envFrom:
  - secretRef:
      name: backend-secret
```

The Deployment refers to the Secret rather than directly containing the password.

---

# 20. ConfigMap vs Secret

The distinction is simple:

| Object    | Purpose                     |
| --------- | --------------------------- |
| ConfigMap | Non-sensitive configuration |
| Secret    | Sensitive configuration     |

Example:

```text
ConfigMap

DB_HOST
DB_PORT
DB_NAME
```

```text
Secret

DB_USERNAME
DB_PASSWORD
JWT_SECRET
```

The important rule is:

> Don't put passwords, tokens, or other sensitive credentials in a ConfigMap.

---

# 21. ConfigMap and Secret are not automatically injected into every Pod

Creating:

```text
backend-config
```

does not automatically make its values available to every container.

The Pod/Deployment must explicitly reference it.

For example:

```yaml
envFrom:
  - configMapRef:
      name: backend-config
```

Similarly:

```yaml
envFrom:
  - secretRef:
      name: backend-secret
```

So the relationship is:

```text
ConfigMap
      |
      | referenced by
      ↓
Pod/Deployment
      |
      ↓
Container
```

---

# 22. What happens if the ConfigMap does not exist?

Our Deployment contains:

```yaml
configMapRef:
  name: backend-config
```

By default, the reference is required.

If the ConfigMap doesn't exist, Kubernetes cannot properly start the Pod with that required configuration.

We can see this behavior through Pod events/status.

This is why we should create the configuration objects before starting the application.

---

# 23. What happens if the Secret does not exist?

The same idea applies.

Our Deployment references:

```yaml
secretRef:
  name: backend-secret
```

If the Secret is required but doesn't exist, the container cannot receive those required environment variables.

Therefore:

```text
Secret must exist
      ↓
Pod can start correctly
```

---

# 24. Environment variables inside the container

Once Kubernetes injects the configuration, the container sees environment variables.

Conceptually:

```text
Container
|
├── DB_HOST=postgres-service
├── DB_PORT=5432
├── DB_NAME=cloud_native_task_platform
├── DB_USERNAME=...
├── DB_PASSWORD=...
└── JWT_SECRET=...
```

Spring Boot can read these values as environment-based configuration.

---

# 25. ConfigMap can also be mounted as a volume

We are currently using:

```yaml
envFrom
```

But ConfigMaps can also be mounted into Pods as files.

For example:

```text
ConfigMap
    |
    ↓
Volume
    |
    ↓
File inside container
```

Conceptually:

```text
/etc/config/
    |
    ├── DB_HOST
    ├── DB_PORT
    └── DB_NAME
```

We don't need to use this method for our current backend.

The important thing is to know that ConfigMaps can provide configuration through:

```text
Environment variables
or
Mounted files
```

---

# 26. Secrets can also be mounted as files

Secrets can similarly be exposed to a container as files.

Conceptually:

```text
Secret
   |
   ↓
Volume
   |
   ↓
/etc/secrets/
```

For example:

```text
/etc/secrets/
    |
    ├── username
    └── password
```

Again, our current project uses environment variables through:

```yaml
envFrom
```

---

# 27. ConfigMap is not a replacement for a database

A ConfigMap is designed for configuration.

It should not be used as:

```text
Application database
```

For example, don't use a ConfigMap to store:

```text
Thousands of users
Tasks
Orders
Application records
```

Our PostgreSQL database handles application data.

So:

```text
ConfigMap
→ Configuration

PostgreSQL
→ Application data
```

---

# 28. Secret is not a full security system

This is an important beginner-level security point.

Kubernetes Secrets are intended for sensitive configuration, but simply using a Secret does not mean that the data is automatically protected from every possible access path.

For example, access to Secrets should be controlled using Kubernetes permissions such as RBAC.

In production, organizations may also use dedicated secret-management systems.

Examples include:

```text
AWS Secrets Manager
HashiCorp Vault
Cloud provider secret-management systems
```

We don't need to implement those yet.

For our project, understand:

```text
Secret
→ better separation for sensitive configuration

RBAC / secret-management systems
→ additional security controls
```

---

# 29. ConfigMap and Secret are Kubernetes objects

Just like:

```text
Deployment
Service
Pod
```

ConfigMaps and Secrets are Kubernetes resources.

We can inspect them using:

```bash
kubectl get configmaps
```

and:

```bash
kubectl get secrets
```

For our project, we have:

```text
backend-config
backend-secret
```

---

# 30. Useful commands

### List ConfigMaps

```bash
kubectl get configmaps
```

or:

```bash
kubectl get cm
```

---

### Describe a ConfigMap

```bash
kubectl describe configmap backend-config
```

---

### View a ConfigMap

```bash
kubectl get configmap backend-config -o yaml
```

---

### List Secrets

```bash
kubectl get secrets
```

---

### Describe a Secret

```bash
kubectl describe secret backend-secret
```

Be careful when displaying sensitive information.

---

### View Secret metadata

```bash
kubectl get secret backend-secret
```

This normally shows metadata such as the Secret name and type without directly printing the secret values.

---

# 31. Verify environment variables inside the backend Pod

We can inspect the environment inside a running Pod.

First find a backend Pod:

```bash
kubectl get pods -l app=backend
```

Then:

```bash
kubectl exec <backend-pod-name> -- env
```

This displays the environment variables available inside the container.

You should be careful when doing this because it may display sensitive values.

For learning purposes, you can verify that the variables exist, but avoid exposing passwords or secrets unnecessarily.

---

# 32. Better way to verify configuration

Instead of printing all environment variables, we can inspect a specific non-sensitive value.

For example:

```bash
kubectl exec <backend-pod-name> -- printenv DB_HOST
```

This might return:

```text
postgres-service
```

For a Secret, avoid printing the actual password unless absolutely necessary.

This is a good security habit even during learning.

---

# 33. ConfigMap and application portability

One of the biggest advantages is portability.

Suppose we use the same Docker image:

```text
cloud-native-task-platform-backend
```

We can run it with different configuration.

### Development

```text
DB_HOST=localhost
```

### Kubernetes

```text
DB_HOST=postgres-service
```

### Production

```text
DB_HOST=production-database
```

The application image can remain the same.

Only the configuration changes.

This is a very important DevOps principle:

> **Build the application once and configure it differently for each environment.**

---

# 34. Configuration vs code

Think about the separation like this:

```text
                 Application
                     |
          +----------+----------+
          |                     |
       Code                 Configuration
          |                     |
          ↓                     ↓
      Docker Image       ConfigMap / Secret
```

The Docker image contains:

```text
Spring Boot application
```

Kubernetes provides:

```text
Environment-specific configuration
```

This separation is useful for CI/CD as well.

---

# 35. Our Deployment configuration

Our backend Deployment contains:

```yaml
containers:
  - name: backend

    image: cloud-native-task-platform-backend:latest

    envFrom:
      - configMapRef:
          name: backend-config

      - secretRef:
          name: backend-secret
```

The important relationship is:

```text
Deployment
    |
    ↓
Pod Template
    |
    ↓
Container
    |
    +---- ConfigMap
    |
    +---- Secret
```

---

# 36. What happens when a Pod is recreated?

Suppose:

```text
Backend Pod 1
```

is deleted.

The Deployment/ReplicaSet creates:

```text
Backend Pod 2
```

The new Pod uses the same Pod template.

Therefore it receives the same referenced configuration:

```text
backend-config
+
backend-secret
```

So:

```text
Pod 1
  ↓
ConfigMap + Secret

Pod 2
  ↓
ConfigMap + Secret
```

This is another advantage of declaring configuration in Kubernetes manifests.

---

# 37. What happens when configuration changes?

This is an important practical consideration.

Suppose we change:

```text
DB_HOST
```

in the ConfigMap.

Kubernetes stores the updated ConfigMap.

However, when configuration is injected through environment variables using `envFrom`, an already-running container does not automatically receive a new environment variable value.

The Pod generally needs to be recreated/restarted to pick up the updated environment.

For example:

```bash
kubectl rollout restart deployment backend-deployment
```

Then new Pods receive the updated environment.

This is important to understand.

---

# 38. ConfigMap and Secret lifecycle

The relationship is:

```text
ConfigMap/Secret
       |
       ↓
Pod configuration
       |
       ↓
Container environment
       |
       ↓
Application
```

Changing the ConfigMap or Secret does not mean every running process immediately reloads the value.

How configuration changes are consumed depends on how we provide the configuration.

For environment variables:

```text
Update ConfigMap/Secret
        ↓
Restart/recreate Pod
        ↓
New environment values
```

For mounted configuration files, Kubernetes can update mounted content, although the application must still be capable of reloading the changed configuration.

We don't need dynamic configuration reload for our current project.

---

# 39. ConfigMap vs Docker image

We should clearly understand the difference.

### Docker image

Contains:

```text
Application
Dependencies
Runtime
JAR
```

### ConfigMap

Contains:

```text
Environment-specific configuration
```

So:

```text
Docker image
→ What the application is

ConfigMap
→ How the application is configured
```

---

# 40. Secret vs ConfigMap

A simple way to remember:

```text
Can everyone normally see this configuration?
        |
        +---- Yes → ConfigMap
        |
        +---- No / sensitive → Secret
```

Examples:

```text
DB_HOST
DB_PORT
DB_NAME
```

→ ConfigMap

```text
DB_PASSWORD
JWT_SECRET
API_KEY
```

→ Secret

---

# 41. Our project's configuration architecture

Our backend currently follows:

```text
                    Backend Deployment
                            |
                   +--------+--------+
                   |                 |
                   ↓                 ↓
             backend-config    backend-secret
             ConfigMap          Secret
                   |                 |
                   |                 |
                   +--------+--------+
                            |
                            ↓
                     Backend Pod
                            |
                            ↓
                    Spring Boot App
```

The backend then uses those environment variables to connect to:

```text
PostgreSQL
```

through:

```text
postgres-service
```

So the overall flow is:

```text
ConfigMap
   ↓
DB configuration

Secret
   ↓
Credentials / sensitive configuration

        ↓

Backend Pod
   ↓
Spring Boot
   ↓
postgres-service
   ↓
PostgreSQL Pod
```

---

# 42. Why this is important for DevOps

Configuration management is a major part of DevOps.

Without configuration separation:

```text
Code change
    ↓
Rebuild image
    ↓
Redeploy
```

even when only a configuration value changed.

With externalized configuration:

```text
Application image
       +
Environment configuration
```

we can reuse the same application image across environments.

This works very well with CI/CD.

For example:

```text
GitHub Actions
      ↓
Build Docker Image
      ↓
Push Image
      ↓
Deploy to Kubernetes
      ↓
ConfigMap + Secret
      ↓
Application
```

---

# 43. Security practices we should remember

Even though this is a beginner note, a few security principles are important.

### Do not commit real passwords to Git

Avoid:

```yaml
DB_PASSWORD: RealProductionPassword
```

inside a Git repository.

---

### Do not put secrets in ConfigMaps

Use:

```text
Secret
```

for sensitive values.

---

### Base64 is not encryption

Remember:

```text
Base64 ≠ encryption
```

---

### Don't unnecessarily print secrets

Avoid commands that expose:

```text
DB_PASSWORD
JWT_SECRET
API keys
```

in terminal logs or CI/CD logs.

---

### Production systems need stronger secret management

For production, we may use:

```text
AWS Secrets Manager
Vault
Cloud-native secret management
```

along with Kubernetes permissions.

We will consider this later when we move toward AWS.

---

# 44. Common beginner mistakes

## Mistake 1 — Putting passwords in ConfigMap

Wrong:

```yaml
kind: ConfigMap

data:
  DB_PASSWORD: mypassword
```

Use a Secret instead.

---

## Mistake 2 — Thinking base64 means encrypted

Wrong idea:

```text
base64 → secure
```

Correct:

```text
base64 → encoded
```

---

## Mistake 3 — Putting everything inside the Docker image

This makes configuration tightly coupled to the image.

Prefer:

```text
Image
+
ConfigMap
+
Secret
```

---

## Mistake 4 — Forgetting to reference the ConfigMap/Secret

Creating:

```text
backend-config
```

does not automatically inject it into the backend.

The Pod must reference it.

---

## Mistake 5 — Printing secrets unnecessarily

Avoid:

```bash
kubectl get secret backend-secret -o yaml
```

when working around sensitive values, because encoded Secret data can be exposed.

---

# 45. What we actually practiced in our project

In our Kubernetes project, we:

* Created a `backend-config` ConfigMap.
* Created a `backend-secret` Secret.
* Referenced the ConfigMap from the backend Deployment.
* Referenced the Secret from the backend Deployment.
* Used `envFrom`.
* Injected configuration into backend Pods.
* Used PostgreSQL configuration through Kubernetes.
* Kept application configuration separate from the Docker image.
* Verified that the backend Pods received the configuration.
* Used the configuration while running the backend inside Kubernetes.

Our backend Deployment contains:

```yaml
envFrom:
  - configMapRef:
      name: backend-config

  - secretRef:
      name: backend-secret
```

This is the practical implementation of the concept.

---

# 46. Questions that naturally arise

We don't need to answer all of these inside this note.

They lead to other Kubernetes concepts.

### Question 1

How does Kubernetes make sure the application is healthy?

→ `09-health-probes.md`

### Question 2

How does external traffic reach the backend?

→ `10-ingress.md`

### Question 3

How do we store PostgreSQL data when the Pod is recreated?

→ `11-persistent-storage-pv-pvc.md`

### Question 4

How does Kubernetes automatically increase the number of Pods?

→ `12-horizontal-pod-autoscaling.md`

### Question 5

How does Kubernetes recover from failures and perform updates?

→ `13-self-healing-and-rollouts.md`

---

# 47. Simple interview explanations

## What is a ConfigMap?

> "A ConfigMap is a Kubernetes object used to store non-sensitive configuration separately from the application. We can inject its values into Pods as environment variables or mount them as files."

---

## Why do we use ConfigMaps?

> "We use ConfigMaps to externalize configuration so that the same application image can be used in different environments without rebuilding the image for every configuration change."

---

## What is a Secret?

> "A Secret is a Kubernetes object intended for storing sensitive configuration such as passwords, tokens, and credentials, and it can be provided to Pods as environment variables or mounted files."

---

## What is the difference between ConfigMap and Secret?

> "ConfigMaps are intended for non-sensitive configuration, while Secrets are intended for sensitive values such as passwords and tokens."

---

## Is Kubernetes Secret encrypted because it uses base64?

> "No. Base64 is only an encoding format, not encryption. Secrets still need proper access control and security practices."

---

## How does a Pod use a ConfigMap?

> "The Pod or Deployment references the ConfigMap, for example through `envFrom`, and Kubernetes makes the configuration values available to the container as environment variables."

---

## How does a Pod use a Secret?

> "The Pod references the Secret using `secretRef`, and Kubernetes provides the secret values to the container as environment variables or mounted files."

---

# 48. What we should understand instead of memorizing

The most important idea is:

```text
Application code
        |
        ↓
Docker Image
```

Configuration is separate:

```text
ConfigMap
        |
        ↓
Non-sensitive configuration
```

Sensitive configuration is separate:

```text
Secret
        |
        ↓
Passwords / tokens / credentials
```

Then:

```text
ConfigMap
     +
Secret
     ↓
Backend Pod
     ↓
Environment variables
     ↓
Spring Boot application
```

This means:

> **The application image contains the application, while Kubernetes provides environment-specific configuration.**

---

# 49. Final mental model

Remember the complete concept like this:

```text
                     KUBERNETES

                 Application Image
                        |
                        ↓
                 Spring Boot App
                        |
             +----------+----------+
             |                     |
             ↓                     ↓
        ConfigMap              Secret
             |                     |
       Non-sensitive          Sensitive data
       configuration          credentials
             |                     |
             +----------+----------+
                        |
                        ↓
                   Backend Pod
                        |
                        ↓
                 Environment
                        |
                        ↓
                 Running App
```

For our project:

```text
backend-deployment
        |
        ↓
Backend Pods
        |
        +-------------------+
        |                   |
        ↓                   ↓
backend-config       backend-secret
 ConfigMap                Secret
        |                   |
        +---------+---------+
                  |
                  ↓
          Spring Boot Backend
```

And the key DevOps principle is:

```text
Build application once
        ↓
Use the same image
        ↓
Provide different configuration
        ↓
Run in different environments
```

---

# 50. Summary

The concepts we should remember are:

```text
ConfigMap
→ Stores non-sensitive configuration


Secret
→ Stores sensitive configuration


envFrom
→ Injects configuration into the container environment


configMapRef
→ References a ConfigMap


secretRef
→ References a Secret


stringData
→ Convenient way to provide Secret values as strings


data
→ Secret data represented using base64 encoding


Base64
→ Encoding, NOT encryption
```

The most important architectural distinction is:

```text
Docker Image
→ Application

ConfigMap
→ Non-sensitive configuration

Secret
→ Sensitive configuration
```

And our backend uses:

```text
backend-deployment
        |
        ↓
Backend Pod
        |
        +---- backend-config
        |
        +---- backend-secret
        |
        ↓
Spring Boot Application
```

The main idea to remember is:

> **Don't tightly couple configuration with the application image. Keep configuration outside the image and let Kubernetes provide it to the application.**
