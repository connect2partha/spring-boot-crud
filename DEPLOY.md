# Local Kubernetes Deployment Guide (macOS + Minikube)

This guide walks through the full setup to deploy the Spring Boot CRUD API to a local Kubernetes cluster on a MacBook using Minikube, triggered automatically by the GitHub Actions CI/CD pipeline.

---

## Prerequisites

| Tool | Purpose |
|------|---------|
| [Homebrew](https://brew.sh) | Package manager for macOS |
| Docker Desktop | Container runtime for Minikube |
| Git | Source control |

---

## Step 1 — Install Docker Desktop

Download and install Docker Desktop from https://www.docker.com/products/docker-desktop

After installation, open Docker Desktop and make sure it is running (whale icon in the menu bar).

---

## Step 2 — Install Minikube

```bash
brew install minikube
```

Verify the installation:

```bash
minikube version
```

---

## Step 3 — Install kubectl

```bash
brew install kubectl
```

Verify:

```bash
kubectl version --client
```

---

## Step 4 — Start Minikube

```bash
minikube start --driver=docker
```

This starts a single-node Kubernetes cluster inside a Docker container. It takes about a minute on first run.

Verify the cluster is up:

```bash
kubectl get nodes
```

Expected output:

```
NAME       STATUS   ROLES           AGE   VERSION
minikube   Ready    control-plane   1m    v1.x.x
```

---

## Step 5 — Enable the Kubernetes Dashboard

```bash
minikube addons enable dashboard
minikube addons enable metrics-server
```

Open the dashboard in your browser:

```bash
minikube dashboard
```

This opens a browser tab with the full Kubernetes UI — deployments, pods, services, logs, and metrics.

---

## Step 6 — Set Up the GitHub Actions Self-Hosted Runner

The `deploy` job in the workflow runs on `self-hosted` — meaning it runs on your MacBook where `kubectl` and `minikube` are already configured. You need to register your Mac as a runner once.

1. Go to your GitHub repository.
2. Navigate to **Settings** → **Actions** → **Runners**.
3. Click **New self-hosted runner**.
4. Select **macOS** as the operating system.
5. Follow the displayed commands — they look like this:

```bash
# Create a directory for the runner
mkdir actions-runner && cd actions-runner

# Download the runner package (use the exact URL shown on GitHub)
curl -o actions-runner-osx-x64.tar.gz -L https://github.com/actions/runner/releases/download/...
tar xzf ./actions-runner-osx-x64.tar.gz

# Configure the runner (use your repo URL and token shown on GitHub)
./config.sh --url https://github.com/<your-org>/<your-repo> --token <YOUR_TOKEN>

# Start the runner
./run.sh
```

Keep this terminal open — the runner must be running whenever you want deployments to trigger.

> **Tip:** To run the runner as a background service so it starts automatically on login:
> ```bash
> ./svc.sh install
> ./svc.sh start
> ```

---

## Step 7 — Update the Workflow for Minikube

Open `.github/workflows/ci.yml` and uncomment the Minikube line in the `Load image into cluster` step:

```yaml
- name: Load image into cluster
  run: minikube image load spring-boot-crud:latest
```

Remove the other commented lines and the `echo` placeholder so the step looks like this:

```yaml
- name: Load image into cluster
  run: minikube image load spring-boot-crud:latest
```

---

## Step 8 — Trigger the Deployment

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

## Step 9 — Access the Application

After the deployment succeeds, get the app URL:

```bash
minikube service spring-boot-crud --url
```

This prints something like `http://127.0.0.1:30080`. Open it in a browser or use `curl`:

```bash
curl http://127.0.0.1:30080/api/products
```

---

## Useful Commands

### Check deployment status

```bash
kubectl get deployments
kubectl get pods
kubectl get services
```

### View application logs

```bash
kubectl logs deployment/spring-boot-crud
```

### Follow live logs

```bash
kubectl logs -f deployment/spring-boot-crud
```

### Restart the deployment manually

```bash
kubectl rollout restart deployment/spring-boot-crud
```

### Stop Minikube (frees resources)

```bash
minikube stop
```

### Delete the cluster entirely

```bash
minikube delete
```

---

## Troubleshooting

**Pod is stuck in `Pending` or `ImagePullBackOff`**
The image was not loaded into Minikube. Run:
```bash
minikube image load spring-boot-crud:latest
kubectl rollout restart deployment/spring-boot-crud
```

**`kubectl` points to the wrong cluster**
```bash
kubectl config use-context minikube
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
Edit `k8s/service.yaml` and change `nodePort` to a free port (range: 30000–32767), then re-apply:
```bash
kubectl apply -f k8s/service.yaml
```
