# Spring Boot CRUD REST API

A simple product catalog REST API built with Spring Boot 4, Spring Data JPA, and an H2 in-memory database.

---

## Prerequisites

| Tool | Version |
|------|---------|
| Java | 21+ |
| Maven | 3.9+ |

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
