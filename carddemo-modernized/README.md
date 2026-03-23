# CardDemo Modernized - Reactive Java

> Legacy Modernization: COBOL/CICS/VSAM/DB2 to Reactive Java with Spring Boot, Kubernetes, Reactive Cassandra, JDBC & ODBC

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│                        Kubernetes Cluster                          │
│                                                                     │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │              CardDemo Spring Boot WebFlux App                │   │
│  │                                                              │   │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐   │   │
│  │  │  REST API     │  │  Service     │  │  Security        │   │   │
│  │  │  Controllers  │──│  Layer       │──│  (Spring Sec)    │   │   │
│  │  │  (WebFlux)    │  │  (Reactive)  │  │  replaces RACF   │   │   │
│  │  └──────────────┘  └──────┬───────┘  └──────────────────┘   │   │
│  │                           │                                  │   │
│  │              ┌────────────┴────────────┐                     │   │
│  │              │                         │                     │   │
│  │  ┌───────────▼──────────┐  ┌──────────▼──────────┐          │   │
│  │  │  Reactive Cassandra  │  │  JDBC / ODBC        │          │   │
│  │  │  Repository Layer    │  │  Repository Layer   │          │   │
│  │  │  (replaces VSAM)     │  │  (replaces DB2)     │          │   │
│  │  └───────────┬──────────┘  └──────────┬──────────┘          │   │
│  └──────────────┼────────────────────────┼──────────────────────┘   │
│                 │                        │                          │
│  ┌──────────────▼──────────┐  ┌──────────▼──────────┐              │
│  │  Apache Cassandra       │  │  PostgreSQL          │              │
│  │  (NoSQL Data Store)     │  │  (Relational DB)     │              │
│  │  StatefulSet            │  │  StatefulSet          │              │
│  └─────────────────────────┘  └──────────────────────┘              │
│                                                                     │
│  ┌────────────────────────────────────────────────────────────────┐ │
│  │  K8S Resources: HPA, Ingress, ConfigMap, Secrets, Services    │ │
│  └────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────┘
```

## Technology Mapping

| Legacy (Mainframe)           | Modernized (Cloud-Native)                     |
|:-----------------------------|:----------------------------------------------|
| COBOL programs               | Java 21 + Spring Boot 3.2                     |
| CICS Transaction Processing  | Spring WebFlux (Reactive REST APIs)           |
| VSAM KSDS files              | Apache Cassandra (Reactive)                   |
| DB2 Relational Database      | PostgreSQL via JDBC/ODBC                      |
| BMS Maps (3270 Screens)      | REST API + OpenAPI/Swagger UI                 |
| JCL Batch Jobs               | Spring Batch / Scheduled Tasks                |
| RACF Security                | Spring Security                               |
| CICS COMMAREA                | DTOs / Request/Response objects                |
| Mainframe WLM                | Kubernetes HPA (Horizontal Pod Autoscaler)    |
| VTAM/TN3270                  | Kubernetes Ingress (HTTP/HTTPS)               |
| DASD Storage                 | Kubernetes PersistentVolumes                  |

## COBOL Program to Java Mapping

### Authentication & Menu (Legacy CICS Transactions)

| COBOL Program | Transaction | Java Component              | Endpoint                   |
|:-------------|:------------|:----------------------------|:---------------------------|
| COSGN00C     | CC00        | AuthController              | POST /api/auth/login       |
| COMEN01C     | CM00        | (Menu replaced by API)      | -                          |
| COADM01C     | CA00        | (Admin menu replaced by API)| -                          |

### Account Management

| COBOL Program | Transaction | Java Component              | Endpoint                          |
|:-------------|:------------|:----------------------------|:----------------------------------|
| COACTVWC     | CAVW        | AccountController           | GET /api/accounts/{id}            |
| COACTUPC     | CAUP        | AccountController           | PUT /api/accounts/{id}            |

### Card Management

| COBOL Program | Transaction | Java Component              | Endpoint                          |
|:-------------|:------------|:----------------------------|:----------------------------------|
| COCRDLIC     | CCLI        | CardController              | GET /api/cards                    |
| COCRDSLC     | CCDL        | CardController              | GET /api/cards/{num}              |
| COCRDUPC     | CCUP        | CardController              | PUT /api/cards/{num}              |

### Transaction Processing

| COBOL Program | Transaction | Java Component              | Endpoint                          |
|:-------------|:------------|:----------------------------|:----------------------------------|
| COTRN00C     | CT00        | TransactionController       | GET /api/transactions             |
| COTRN01C     | CT01        | TransactionController       | GET /api/transactions/{id}        |
| COTRN02C     | CT02        | TransactionController       | POST /api/transactions            |
| COBIL00C     | CB00        | TransactionController       | POST /api/transactions/bill-payment|
| CORPT00C     | CR00        | TransactionController       | GET /api/transactions/report      |

### Admin Functions

| COBOL Program | Transaction | Java Component              | Endpoint                              |
|:-------------|:------------|:----------------------------|:--------------------------------------|
| COUSR00C     | CU00        | UserController              | GET /api/admin/users                  |
| COUSR01C     | CU01        | UserController              | POST /api/admin/users                 |
| COUSR02C     | CU02        | UserController              | PUT /api/admin/users/{id}             |
| COUSR03C     | CU03        | UserController              | DELETE /api/admin/users/{id}          |
| COTRTLIC     | CTLI        | TransactionTypeController   | GET/DELETE /api/admin/transaction-types|
| COTRTUPC     | CTTU        | TransactionTypeController   | POST/PUT /api/admin/transaction-types |

## Data Store Mapping

### Cassandra Tables (replacing VSAM KSDS files)

| VSAM File  | Cassandra Table          | Entity              | Key                |
|:-----------|:-------------------------|:---------------------|:-------------------|
| ACCTDAT    | accounts                 | Account             | account_id         |
| CARDDAT    | cards                    | Card                | card_number        |
| CUSTDAT    | customers                | Customer            | customer_id        |
| TRANSACT   | transactions             | Transaction         | transaction_id     |
| CARDXREF   | card_cross_references    | CardCrossReference  | card_number        |
| USRSEC     | users                    | UserSecurity        | user_id            |

### JDBC/ODBC Tables (replacing DB2)

| DB2 Table          | JDBC Table                  | Entity              |
|:-------------------|:----------------------------|:---------------------|
| TRAN_TYPE_TABLE    | transaction_types           | TransactionType     |
| TRAN_CAT_TABLE     | transaction_categories      | TransactionCategory |

## Project Structure

```
carddemo-modernized/
├── pom.xml                          # Maven build with Spring Boot 3.2
├── Dockerfile                       # Multi-stage container build
├── docker-compose.yml               # Full stack (App + Cassandra + PostgreSQL)
├── src/main/java/com/carddemo/
│   ├── CardDemoApplication.java     # Spring Boot entry point
│   ├── config/
│   │   ├── CassandraConfig.java     # Reactive Cassandra (replaces VSAM)
│   │   ├── JdbcConfig.java          # JDBC/ODBC (replaces DB2)
│   │   └── SecurityConfig.java      # Spring Security (replaces RACF)
│   ├── controller/
│   │   ├── AuthController.java      # CC00 Sign-on
│   │   ├── AccountController.java   # CAVW/CAUP Account View/Update
│   │   ├── CardController.java      # CCLI/CCDL/CCUP Card operations
│   │   ├── TransactionController.java # CT00/CT01/CT02/CB00/CR00
│   │   ├── CustomerController.java  # Customer operations
│   │   ├── UserController.java      # CU00-CU03 Admin User Management
│   │   └── TransactionTypeController.java # CTLI/CTTU Admin Tran Types
│   ├── dto/                         # Request/Response DTOs (replace BMS maps)
│   ├── exception/                   # Error handling (replaces ABEND routines)
│   ├── model/                       # Domain entities (from COBOL copybooks)
│   ├── repository/
│   │   ├── cassandra/               # Reactive Cassandra repos (VSAM replacement)
│   │   └── jdbc/                    # JDBC repos (DB2 replacement, ODBC-compatible)
│   ├── service/                     # Business logic (from COBOL PROCEDURE DIVISION)
│   └── security/                    # Security utilities
├── src/main/resources/
│   ├── application.yml              # Configuration (replaces JCL PARM/CICS SIT)
│   └── schema.sql                   # Relational schema (replaces DB2 DDL)
└── src/test/java/                   # Tests

k8s/                                 # Kubernetes manifests
├── namespace.yml
├── configmap.yml
├── secret.yml
├── deployment.yml                   # App deployment (replaces CICS region)
├── service.yml                      # Service (replaces VTAM listener)
├── hpa.yml                          # Autoscaler (replaces WLM)
├── ingress.yml                      # External access (replaces TN3270)
├── cassandra-statefulset.yml        # Cassandra (replaces VSAM DASD)
└── postgres-statefulset.yml         # PostgreSQL (replaces DB2 subsystem)
```

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.9+
- Docker & Docker Compose (for local development)
- kubectl (for Kubernetes deployment)

### Local Development

```bash
# Start infrastructure (Cassandra + PostgreSQL)
cd carddemo-modernized
docker-compose up -d cassandra postgres

# Build and run the application
mvn clean package -DskipTests
mvn spring-boot:run

# Or run with Docker Compose (full stack)
docker-compose up --build
```

### API Access

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/actuator/health
- **Metrics**: http://localhost:8080/actuator/prometheus

### Kubernetes Deployment

```bash
# Create namespace and resources
kubectl apply -f k8s/namespace.yml
kubectl apply -f k8s/secret.yml
kubectl apply -f k8s/configmap.yml
kubectl apply -f k8s/cassandra-statefulset.yml
kubectl apply -f k8s/postgres-statefulset.yml

# Wait for databases to be ready
kubectl -n carddemo wait --for=condition=ready pod -l app.kubernetes.io/component=cassandra --timeout=120s
kubectl -n carddemo wait --for=condition=ready pod -l app.kubernetes.io/component=postgres --timeout=60s

# Deploy the application
kubectl apply -f k8s/deployment.yml
kubectl apply -f k8s/service.yml
kubectl apply -f k8s/hpa.yml
kubectl apply -f k8s/ingress.yml
```

## JDBC/ODBC Connectivity

The application supports multiple relational database backends via JDBC:

| Database     | Driver Class                    | URL Format                                    |
|:------------|:-------------------------------|:----------------------------------------------|
| H2 (dev)    | org.h2.Driver                  | jdbc:h2:mem:carddemo                         |
| PostgreSQL  | org.postgresql.Driver          | jdbc:postgresql://host:5432/carddemo         |
| MySQL       | com.mysql.cj.jdbc.Driver       | jdbc:mysql://host:3306/carddemo              |
| Oracle      | oracle.jdbc.OracleDriver       | jdbc:oracle:thin:@host:1521:carddemo         |
| DB2         | com.ibm.db2.jcc.DB2Driver      | jdbc:db2://host:50000/carddemo               |
| ODBC Bridge | sun.jdbc.odbc.JdbcOdbcDriver   | jdbc:odbc:CardDemoDSN                        |

Configure via environment variables:
```bash
export JDBC_URL=jdbc:postgresql://localhost:5432/carddemo
export JDBC_USERNAME=carddemo
export JDBC_PASSWORD=secret
export JDBC_DRIVER=org.postgresql.Driver
```

## Default Credentials

| User ID  | Password | Role  | Legacy Equivalent               |
|:---------|:---------|:------|:--------------------------------|
| ADMIN001 | PASSWORD | Admin | CDEMO-USRTYP-ADMIN (value 'A') |
| USER0001 | PASSWORD | User  | CDEMO-USRTYP-USER (value 'U')  |

## License

Apache License 2.0 - See [LICENSE](../LICENSE)
