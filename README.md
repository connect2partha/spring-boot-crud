# Spring Boot CRUD REST API

A simple product catalog REST API built with Spring Boot 4, Spring Data JPA, and an H2 in-memory database.

---

## Prerequisites

| Tool | Version |
|------|---------|
| Java | 21+ |
| Maven | 3.9+ |
| Docker Desktop | Current |
| kubectl | Current |
| Helm | 3+ |

---

## Build

```bash
mvn clean package
```

The compiled JAR is written to `target/spring-boot-crud-1.0.0.jar`.

---

## Run

**With Maven (development)**
```bash
mvn spring-boot:run
```

**With the JAR**
```bash
java -jar target/spring-boot-crud-1.0.0.jar
```

The server starts on **http://localhost:8080**.

---

## Test

### Unit & Integration Tests (Maven)

Run all tests:
```bash
mvn test
```

Run only the controller integration tests:
```bash
mvn test -Dtest=ProductControllerTest
```

### Robot Framework API Tests

**Prerequisites**

| Tool | Version |
|------|---------|
| Python | 3.8+ |

Install dependencies:
```bash
pip install -r tests/requirements.txt
```

**Steps**

1. Start the application (see [Run](#run) above).
2. Run the test suite:

```bash
robot --outputdir tests/results tests/product_api.robot
```

> **Windows note:** If you see a `LookupError: unknown encoding` error, prefix the command with `PYTHONUTF8=1` (Linux/macOS) or set `$env:PYTHONUTF8 = "1"` in PowerShell before running.

**Results**

After the run, open the HTML report in your browser:

```
tests/results/report.html   # pass/fail summary
tests/results/log.html      # full execution log with request/response detail
```

**Test cases**

| Test | Description |
|------|-------------|
| Create Product | `POST /api/products` returns 201 |
| Get All Products | `GET /api/products` returns 200 with list |
| Get Product By ID | `GET /api/products/{id}` returns 200 |
| Update Product | `PUT /api/products/{id}` returns 200 |
| Get Product Not Found | `GET /api/products/99999` returns 404 |
| Create Product Validation Failure | `POST` with missing fields returns 400 |
| Delete Product | `DELETE /api/products/{id}` returns 204 |
| Verify Product Deleted | `GET` after delete returns 404 |

---

## API Documentation

Once the app is running, open the interactive Swagger UI in your browser:

```
http://localhost:8080/swagger-ui.html
```

The raw OpenAPI spec (JSON) is available at:

```
http://localhost:8080/v3/api-docs
```

---

## Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/products` | List all products (optional `?name=` filter) |
| `GET` | `/api/products/{id}` | Get a product by ID |
| `POST` | `/api/products` | Create a new product |
| `PUT` | `/api/products/{id}` | Update an existing product |
| `DELETE` | `/api/products/{id}` | Delete a product |

### Example — create a product

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Laptop","description":"15-inch laptop","price":1299.99,"quantity":10}'
```

---

## H2 Console

An in-browser SQL console is available while the app is running:

```
http://localhost:8080/h2-console
```

| Field | Value |
|-------|-------|
| JDBC URL | `jdbc:h2:mem:cruddb` |
| Username | `sa` |
| Password | *(leave blank)* |

---

## Local Kubernetes Deployment

This project can be deployed to the Kubernetes cluster included with Docker Desktop using Helm. The deployment uses the `spring-boot-crud` namespace and a NodePort service on port `30080`.

### 1. Enable Kubernetes in Docker Desktop

1. Open Docker Desktop.
2. Go to **Settings** → **Kubernetes**.
3. Select **Enable Kubernetes** and click **Apply & restart**.
4. Wait until Docker Desktop reports that Kubernetes is running.

Docker Desktop creates the `docker-desktop` Kubernetes context automatically.

### 2. Install and verify Kubernetes tools

Install the tools with Homebrew:

```bash
brew install kubectl helm
```

Select the Docker Desktop context and verify the cluster:

```bash
kubectl config use-context docker-desktop
kubectl get nodes
helm version
```

The node should report `Ready`.

### 3. Install Headlamp (optional)

Headlamp provides a graphical view of deployments, pods, services, logs, and metrics:

```bash
brew install --cask headlamp
```

Open Headlamp, select the `docker-desktop` context, and connect to the cluster.

### 4. Build the application image

Build the JAR and Docker image locally:

```bash
mvn clean package
docker build -t spring-boot-crud:latest .
```

Docker Desktop Kubernetes uses a separate containerd image store. If the pod reports `ErrImageNeverPull`, import the image into the Kubernetes worker runtime or configure a registry reachable from the cluster before deploying.

### 5. Deploy with Helm

Create the dedicated namespace and install or upgrade the release:

```bash
kubectl create namespace spring-boot-crud --dry-run=client -o yaml | kubectl apply -f -

helm upgrade --install spring-boot-crud helm/spring-boot-crud \
  --namespace spring-boot-crud \
  --create-namespace \
  --set image.repository=spring-boot-crud \
  --set image.pullPolicy=Never \
  --wait \
  --timeout 180s
```

Always include `--namespace spring-boot-crud`. Omitting it attempts to install into `default` and can cause a NodePort allocation error.

Verify the release:

```bash
helm status spring-boot-crud --namespace spring-boot-crud
kubectl get pods,services --namespace spring-boot-crud
kubectl rollout status deployment/spring-boot-crud --namespace spring-boot-crud
```

The Helm chart is in [`helm/spring-boot-crud`](helm/spring-boot-crud).

### 6. Access the application

When Docker Desktop exposes NodePorts on localhost, open:

```bash
open http://localhost:30080
```

API example:

```bash
curl http://localhost:30080/api/products
```

If the NodePort is not reachable, forward the service port:

```bash
kubectl port-forward --namespace spring-boot-crud service/spring-boot-crud 8080:8080
```

Keep the command running and access the app at `http://localhost:8080`. Press `Ctrl+C` to stop forwarding.

```bash
curl http://localhost:8080/api/products
```

Swagger UI is available at:

```text
http://localhost:8080/swagger-ui.html
```

### 7. GitHub Actions deployment

CI and deployment run as separate workflows:

```text
CI (build → Robot Framework tests) → Deploy (Helm)
```

The [CI workflow](.github/workflows/ci.yml) runs on pushes to any branch and on pull requests targeting `main`. It builds and tests the application, then uploads the JAR as the `app-jar` artifact. The [CD workflow](.github/workflows/cd.yml) listens for completed CI runs and proceeds only when the `main` workflow succeeds. It checks out the exact commit from that CI run, downloads its artifact, builds the Docker image, and deploys it with Helm.

The self-hosted runner used by the Deploy workflow must have Docker Desktop Kubernetes enabled and access to Docker, Helm, `kubectl`, and GitHub CLI:

```bash
docker version
helm version
gh --version
kubectl config use-context docker-desktop
kubectl get nodes
```

Authenticate GitHub CLI for the runner if needed:

```bash
gh auth status
gh auth login
```

Register the Mac as a self-hosted runner from the repository's **Settings** → **Actions** → **Runners** page. No Minikube setup or raw Kubernetes manifest deployment is required.

The deployment workflow's key steps are:

```yaml
- uses: actions/checkout@v5
  with:
    ref: ${{ github.event.workflow_run.head_sha }}

- name: Download JAR artifact
  env:
    GH_TOKEN: ${{ github.token }}
  run: gh run download "${{ github.event.workflow_run.id }}" --name app-jar --dir target

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

### Useful Kubernetes commands

```bash
helm status spring-boot-crud --namespace spring-boot-crud
kubectl get deployments --namespace spring-boot-crud
kubectl get pods --namespace spring-boot-crud
kubectl get services --namespace spring-boot-crud
kubectl logs --namespace spring-boot-crud deployment/spring-boot-crud
kubectl logs --follow --namespace spring-boot-crud deployment/spring-boot-crud
```

Restart the release:

```bash
helm upgrade --install spring-boot-crud helm/spring-boot-crud \
  --namespace spring-boot-crud \
  --create-namespace \
  --set image.repository=spring-boot-crud \
  --set image.pullPolicy=Never \
  --wait \
  --timeout 180s
```

Remove the application and its namespace:

```bash
helm uninstall spring-boot-crud --namespace spring-boot-crud
kubectl delete namespace spring-boot-crud
```
