# CardDemo Modernized — Spring Boot Microservices

Modernized version of the CardDemo mainframe credit card application, migrated from COBOL/CICS/VSAM to Spring Boot 3.x microservices with PostgreSQL.

## Architecture

| Service | Port | Schema | Description |
|---|---|---|---|
| auth-service | 8081 | auth | User authentication, JWT, user CRUD |
| customer-service | 8082 | customer | Customer management, SSN masking |
| account-service | 8083 | account | Account management (skeleton) |
| card-service | 8084 | card | Card management (skeleton) |
| transaction-service | 8085 | transaction | Transaction processing (skeleton) |
| statement-service | 8086 | statement | Statement generation (skeleton) |
| api-gateway | 8080 | — | API gateway (skeleton) |
| data-loader | CLI | all | Spring Batch data loader from ASCII files |

## Prerequisites

- Java 17+
- Maven 3.8+
- Docker & Docker Compose (for PostgreSQL)

## Quick Start

### 1. Start PostgreSQL

```bash
docker-compose up -d
```

### 2. Build All Modules

```bash
mvn clean install
```

### 3. Load Sample Data

```bash
java -jar data-loader/target/data-loader-1.0.0-SNAPSHOT.jar --data.dir=../app/data/ASCII
```

### 4. Run Services

```bash
# Auth Service
cd auth-service && mvn spring-boot:run

# Customer Service (in another terminal)
cd customer-service && mvn spring-boot:run
```

## Running Tests

```bash
mvn test
```

Integration tests use Testcontainers (requires Docker).

## API Endpoints

### Auth Service (port 8081)

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | /api/auth/login | None | Login, returns JWT |
| GET | /api/users | ADMIN | List all users |
| GET | /api/users/{id} | Any | Get user by ID |
| POST | /api/users | ADMIN | Create user |
| PUT | /api/users/{id} | ADMIN | Update user |
| DELETE | /api/users/{id} | ADMIN | Delete user |

### Customer Service (port 8082)

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | /api/customers | JWT | List customers (paginated) |
| GET | /api/customers/{id} | JWT | Get customer by ID |
| GET | /api/customers/search?ssn= | JWT | Search by SSN |
| GET | /api/customers/search?lastName= | JWT | Search by last name |
| POST | /api/customers | JWT | Create customer |
| PUT | /api/customers/{id} | JWT | Update customer |

### Example Login

```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"userId":"ADMIN001","password":"PASSWORD"}'
```

## Default Users

| User ID | Password | Type |
|---|---|---|
| ADMIN001 | PASSWORD | Admin |
| USER0001 | PASSWORD | User |

## COBOL Source Mapping

| COBOL Program | Java Service | Endpoint |
|---|---|---|
| COSGN00C | auth-service | POST /api/auth/login |
| COUSR00C | auth-service | GET /api/users |
| COUSR01C | auth-service | POST /api/users |
| COUSR02C | auth-service | PUT /api/users/{id} |
| COUSR03C | auth-service | DELETE /api/users/{id} |
| COCRDSLC | customer-service | GET /api/customers/search |
| COCRDLIC | customer-service | GET /api/customers |
