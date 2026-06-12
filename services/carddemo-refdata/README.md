# CardDemo Reference Data Service (`carddemo-refdata`)

Spring Boot 3.x microservice that replaces the COBOL/DB2 reference-data subsystem of the CardDemo mainframe application with a modern REST API backed by PostgreSQL.

## Legacy COBOL → Java Mapping

| COBOL Artifact | Purpose | Java Equivalent |
|---|---|---|
| **CVTRA03Y.cpy** — `TRAN-TYPE-RECORD` (RECLN=60) | Transaction type master | `TransactionType` entity |
| **CVTRA04Y.cpy** — `TRAN-CAT-RECORD` (RECLN=60) | Transaction category (composite key: type + cat) | `TransactionCategory` entity |
| **CVTRA02Y.cpy** — `DIS-GROUP-RECORD` (RECLN=50) | Disclosure group with interest rate | `DisclosureGroup` entity |
| **COTRTLIC.cbl** (2 098 LOC) | CICS list/delete transaction types & categories | `TransactionTypeController`, `TransactionCategoryController` (GET / DELETE) |
| **COTRTUPC.cbl** (1 702 LOC) | CICS add/edit transaction types & categories | Same controllers (POST / PUT) |
| **COBTUPDT.cbl** | Batch maintenance of transaction types | Superseded by REST CRUD |

### Field-Level Mapping

#### TransactionType — `CVTRA03Y.cpy`

| COBOL Field | PIC | Java Field | Type |
|---|---|---|---|
| `TRAN-TYPE` | `X(02)` | `typeCode` | `String(2)` — Primary Key |
| `TRAN-TYPE-DESC` | `X(50)` | `description` | `String(50)` |
| `FILLER` | `X(08)` | — | not mapped |

#### TransactionCategory — `CVTRA04Y.cpy`

| COBOL Field | PIC | Java Field | Type |
|---|---|---|---|
| `TRAN-TYPE-CD` | `X(02)` | `typeCode` | `String(2)` — Composite PK part 1 |
| `TRAN-CAT-CD` | `9(04)` | `categoryCode` | `int` (0–9999) — Composite PK part 2 |
| `TRAN-CAT-TYPE-DESC` | `X(50)` | `description` | `String(50)` |
| `FILLER` | `X(04)` | — | not mapped |

#### DisclosureGroup — `CVTRA02Y.cpy`

| COBOL Field | PIC | Java Field | Type |
|---|---|---|---|
| `DIS-ACCT-GROUP-ID` | `X(10)` | `accountGroupId` | `String(10)` — Composite PK part 1 |
| `DIS-TRAN-TYPE-CD` | `X(02)` | `transactionTypeCode` | `String(2)` — Composite PK part 2 |
| `DIS-TRAN-CAT-CD` | `9(04)` | `transactionCategoryCode` | `int` (0–9999) — Composite PK part 3 |
| `DIS-INT-RATE` | `S9(04)V99` | `interestRate` | `BigDecimal(6,2)` |
| `FILLER` | `X(28)` | — | not mapped |

## REST API

| Method | Path | Description |
|---|---|---|
| `GET` | `/reference/types` | List all transaction types |
| `POST` | `/reference/types` | Create a transaction type |
| `GET` | `/reference/types/{code}` | Get a single type |
| `PUT` | `/reference/types/{code}` | Update a type |
| `DELETE` | `/reference/types/{code}` | Delete a type |
| `GET` | `/reference/categories` | List all categories |
| `POST` | `/reference/categories` | Create a category |
| `GET` | `/reference/categories/{typeCode}/{catCode}` | Get a single category |
| `PUT` | `/reference/categories/{typeCode}/{catCode}` | Update a category |
| `DELETE` | `/reference/categories/{typeCode}/{catCode}` | Delete a category |
| `GET` | `/reference/disclosure-groups` | List all disclosure groups |
| `POST` | `/reference/disclosure-groups` | Create a disclosure group |
| `GET` | `/reference/disclosure-groups/{groupId}/{typeCode}/{catCode}` | Get a single group |
| `PUT` | `/reference/disclosure-groups/{groupId}/{typeCode}/{catCode}` | Update a group |
| `DELETE` | `/reference/disclosure-groups/{groupId}/{typeCode}/{catCode}` | Delete a group |

## Validation Rules

- **Type code**: 1–2 characters (`@Size(min=1, max=2)`, `@NotBlank`)
- **Category code**: 0–9999 (`@Min(0)`, `@Max(9999)`)
- **Account group ID**: 1–10 characters
- **Interest rate**: `BigDecimal` with up to 4 integer digits and 2 fractional digits (`@Digits(integer=4, fraction=2)`)

## Running Locally

### Prerequisites
- Java 17+
- Maven 3.6+
- PostgreSQL 14+ (or use the H2 test profile)

### Build & Test

```bash
cd services/carddemo-refdata
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64  # adjust as needed
mvn clean test                         # runs all 63 tests against H2
mvn spring-boot:run                    # starts on port 8082 (requires PostgreSQL)
```

### Configuration

| Property | Default | Description |
|---|---|---|
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/carddemo` | JDBC URL |
| `spring.datasource.username` | `carddemo` | DB user |
| `spring.datasource.password` | `carddemo` | DB password |
| `server.port` | `8082` | HTTP port |

## Project Structure

```
services/carddemo-refdata/
├── pom.xml
├── README.md
└── src/
    ├── main/java/com/carddemo/refdata/
    │   ├── RefDataApplication.java
    │   ├── controller/
    │   │   ├── DisclosureGroupController.java
    │   │   ├── GlobalExceptionHandler.java
    │   │   ├── TransactionCategoryController.java
    │   │   └── TransactionTypeController.java
    │   ├── entity/
    │   │   ├── DisclosureGroup.java
    │   │   ├── DisclosureGroupId.java
    │   │   ├── TransactionCategory.java
    │   │   ├── TransactionCategoryId.java
    │   │   └── TransactionType.java
    │   ├── repository/
    │   │   ├── DisclosureGroupRepository.java
    │   │   ├── TransactionCategoryRepository.java
    │   │   └── TransactionTypeRepository.java
    │   └── service/
    │       ├── DisclosureGroupService.java
    │       ├── TransactionCategoryService.java
    │       └── TransactionTypeService.java
    ├── main/resources/
    │   └── application.yml
    └── test/
        ├── java/com/carddemo/refdata/
        │   ├── RefDataApplicationTest.java
        │   ├── controller/
        │   │   ├── DisclosureGroupControllerTest.java
        │   │   ├── TransactionCategoryControllerTest.java
        │   │   └── TransactionTypeControllerTest.java
        │   ├── repository/
        │   │   └── TransactionTypeRepositoryTest.java
        │   └── service/
        │       ├── DisclosureGroupServiceTest.java
        │       ├── TransactionCategoryServiceTest.java
        │       └── TransactionTypeServiceTest.java
        └── resources/
            └── application-test.yml
```
