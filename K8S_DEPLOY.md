# Local Kubernetes Deployment Guide (macOS + Docker Desktop)

This guide walks through the full setup to deploy the Spring Boot CRUD API to the Kubernetes cluster included with Docker Desktop on a MacBook, triggered automatically by the GitHub Actions CI/CD pipeline.

---

## Prerequisites

| Tool | Purpose |
|------|---------|
| [Homebrew](https://brew.sh) | Package manager for macOS |
| Docker Desktop | Container runtime and local Kubernetes cluster |
| Helm | Kubernetes package manager |
| Git | Source control |

---

## Step 1 — Install Docker Desktop

Download and install Docker Desktop from https://www.docker.com/products/docker-desktop

After installation, open Docker Desktop and make sure it is running (whale icon in the menu bar).

---

## Step 2 — Enable Kubernetes in Docker Desktop

1. Open Docker Desktop.
2. Go to **Settings** → **Kubernetes**.
3. Select **Enable Kubernetes** and click **Apply & restart**.
4. Wait until Docker Desktop reports that Kubernetes is running.

Docker Desktop configures the `docker-desktop` Kubernetes context automatically.

---

## Step 3 — Install kubectl

```bash
brew install kubectl
```

Verify:

```bash
kubectl version --client
```

Install Helm:

```bash
brew install helm
```

Verify:

```bash
helm version
```

---

## Step 4 — Verify the Kubernetes cluster

```bash
kubectl config use-context docker-desktop
kubectl get nodes
```

Expected output:

```
NAME             STATUS   ROLES    AGE   VERSION
docker-desktop   Ready    <none>   ...    v1.x.x
```

---

## Step 5 — Install Headlamp

Headlamp is a Kubernetes UI that can be used to inspect deployments, pods, services, logs, and metrics.

Install the Headlamp desktop application with Homebrew:

```bash
brew install --cask headlamp
```

Open Headlamp, select the `docker-desktop` context, and connect to the cluster. If metrics are not displayed, install the Metrics Server from Headlamp or follow the [Headlamp Metrics documentation](https://headlamp.dev/docs/latest/installation/).

---

## Step 6 — Set Up the GitHub Actions Self-Hosted Runner

The `deploy` job runs on `self-hosted` and performs the Docker image build and Helm deployment on your MacBook. Before registering the runner, make sure Docker Desktop Kubernetes is running and the runner machine has Docker, Helm, and `kubectl` installed. The runner must use the `docker-desktop` context and have permission to access Docker Desktop.

Verify the required tools and context:

```bash
docker version
helm version
kubectl config use-context docker-desktop
kubectl get nodes
```

The deployment is installed by Helm in the `spring-boot-crud` namespace. The workflow creates that namespace automatically with `--create-namespace`; no manual `kubectl apply` step or Minikube setup is required.

1. Go to your GitHub repository.
2. Navigate to **Settings** → **Actions** → **Runners**.
3. Click **New self-hosted runner**.
4. Select **macOS** as the operating system.
5. Follow the displayed commands — they look like this:

```bash
# Create a folder
$ mkdir actions-runner && cd actions-runner
# Download the latest runner package
$ curl -o actions-runner-osx-x64-2.337.0.tar.gz -L https://github.com/actions/runner/releases/download/v2.337.0/actions-runner-osx-x64-2.337.0.tar.gz
# Optional: Validate the hash
$ echo "d383f505d7ed041b1873ab68c35dd766fc093f2252330f95bb427be8f2c6dcfc  actions-runner-osx-x64-2.337.0.tar.gz" | shasum -a 256 -c
# Extract the installer
$ tar xzf ./actions-runner-osx-x64-2.337.0.tar.gz

# Create the runner and start the configuration experience
$ ./config.sh --url https://github.com/connect2partha/spring-boot-crud --token AUHVK4GLH6BLPJ2TAIWCTPDKV6NY6
# Last step, run it!
$ ./run.sh
```

Keep this terminal open — the runner must be running whenever you want deployments to trigger.

> **Tip:** To run the runner as a background service so it starts automatically on login:
> ```bash
> ./svc.sh install
> ./svc.sh start
> ```

---

## Step 7 — Deploy with Helm

Build the application and its local Docker image:

```bash
mvn clean package
docker build -t spring-boot-crud:latest .
```

Docker Desktop Kubernetes uses a separate containerd image store. If the cluster cannot see the local image, import it into the worker runtime before installing the chart. For a registry-backed setup, set `image.repository` to an image address reachable from the cluster.

Create the dedicated namespace:

```bash
kubectl create namespace spring-boot-crud --dry-run=client -o yaml | kubectl apply -f -
```

Install or upgrade the release in the dedicated namespace. Always include `--namespace spring-boot-crud`; omitting it attempts to install into `default` and can cause a NodePort allocation error:

```bash
helm upgrade --install spring-boot-crud helm/spring-boot-crud \
  --namespace spring-boot-crud \
  --create-namespace \
  --set image.repository=spring-boot-crud \
  --set image.pullPolicy=Never \
  --wait \
  --timeout 180s
```

Verify the release and rollout:

```bash
helm status spring-boot-crud --namespace spring-boot-crud
kubectl get pods,services --namespace spring-boot-crud
kubectl rollout status deployment/spring-boot-crud --namespace spring-boot-crud
```

The chart is in [`helm/spring-boot-crud`](helm/spring-boot-crud).

---

## Step 8 — Configure the Workflow for Docker Desktop Kubernetes

The `deploy` job in `.github/workflows/ci.yml` runs after the build and Robot Framework jobs. It checks out the repository, downloads the JAR artifact, builds the Docker image, and deploys the Helm release into the dedicated namespace:

```yaml
- name: Download JAR artifact
  uses: actions/download-artifact@v5
  with:
    name: app-jar
    path: target

- name: Build Docker image
  run: docker build -t spring-boot-crud:latest .

- name: Deploy to Kubernetes with Helm
  run: |
    helm upgrade --install spring-boot-crud helm/spring-boot-crud \
      --namespace spring-boot-crud \
      --create-namespace \
      --set image.repository=spring-boot-crud \
      --set image.pullPolicy=Never \
      --wait \
      --timeout 180s
```

---

## Step 9 — Trigger the Deployment

Push or merge a change to `main`:

```bash
git push origin main
```

The pipeline will run in this order:

```
build → robot-tests → deploy
```

The `deploy` job only starts after all Robot Framework tests pass.

---

## Step 10 — Access the Application

After the deployment succeeds, the NodePort service is available directly through Docker Desktop:

```bash
open http://localhost:30080
```

You can also use `curl`:

```bash
curl http://localhost:30080/api/products
```

If the NodePort is not reachable at `localhost:30080`, forward the Kubernetes service port to your machine:

```bash
kubectl port-forward --namespace spring-boot-crud service/spring-boot-crud 8080:8080
```

Keep the port-forward command running, then access the application at:

```text
http://localhost:8080
```

For example:

```bash
curl http://localhost:8080/api/products
```

Press `Ctrl+C` to stop port forwarding.

---

## Useful Commands

### Check deployment status

```bash
helm status spring-boot-crud --namespace spring-boot-crud
kubectl get deployments --namespace spring-boot-crud
kubectl get pods --namespace spring-boot-crud
kubectl get services --namespace spring-boot-crud
```

### View application logs

```bash
kubectl logs --namespace spring-boot-crud deployment/spring-boot-crud
```

### Follow live logs

```bash
kubectl logs --follow --namespace spring-boot-crud deployment/spring-boot-crud
```

### Restart the deployment manually

```bash
helm upgrade --install spring-boot-crud helm/spring-boot-crud \
  --create-namespace \
  --namespace spring-boot-crud \
  --set image.repository=spring-boot-crud \
  --set image.pullPolicy=Never \
  --wait
```

### Reset the Kubernetes cluster

Use **Docker Desktop** → **Settings** → **Kubernetes** → **Reset Kubernetes cluster** only when you want to remove the local cluster and its resources.

---

## Troubleshooting

**Pod is stuck in `Pending` or `ImagePullBackOff`**
Confirm that Docker Desktop is running and rebuild the image:
```bash
docker build -t spring-boot-crud:latest .
kubectl rollout restart --namespace spring-boot-crud deployment/spring-boot-crud
```

**`kubectl` points to the wrong cluster**
```bash
kubectl config use-context docker-desktop
```

**Self-hosted runner is offline**
Navigate to the `actions-runner` directory and run:
```bash
./run.sh
```
Or if installed as a service:
```bash
./svc.sh start
```

**Port 30080 is already in use**
Set a free NodePort (range: 30000–32767) during the Helm upgrade:
```bash
helm upgrade --install spring-boot-crud helm/spring-boot-crud \
  --namespace spring-boot-crud \
  --set service.nodePort=30081
```

To remove the application and its namespace:

```bash
helm uninstall spring-boot-crud --namespace spring-boot-crud
kubectl delete namespace spring-boot-crud
```
