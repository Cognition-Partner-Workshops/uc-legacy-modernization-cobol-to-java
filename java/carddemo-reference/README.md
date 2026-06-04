# CardDemo Reference Data Service (Phase 2)

Spring Boot 3 modernization of the legacy **DB2 "Transaction Type Management"
optional module** of CardDemo. It exposes a REST CRUD API over the transaction
type and transaction category reference data that the mainframe maintained in
DB2 (and mirrored to VSAM).

## Legacy → Java mapping

| Legacy artifact | Location | Replaced by |
|-----------------|----------|-------------|
| `COTRTLIC.cbl` (CICS list/update/delete) | `app/app-transaction-type-db2/cbl/` | `GET`/`PUT`/`DELETE` on `/api/transaction-types` |
| `COTRTUPC.cbl` (CICS add/edit) | `app/app-transaction-type-db2/cbl/` | `POST`/`PUT` on `/api/transaction-types` |
| `COBTUPDT.cbl` (batch maintenance) | `app/app-transaction-type-db2/cbl/` | `POST /api/transaction-types/batch` |
| `TRNTYPE.ddl`, `TRNTYCAT.ddl` (DB2 DDL) | `app/app-transaction-type-db2/ddl/` | `db/migration/V1__create_transaction_type_tables.sql` |
| `CVTRA03Y.cpy`, `CVTRA04Y.cpy` (VSAM copybooks) | `app/cpy/` | `TransactionType`, `TransactionTypeCategory` entities |
| `trantype.txt`, `trancatg.txt` (data) | `app/data/ASCII/` | `db/migration/V2__seed_reference_data.sql` |

## Data model

- **TRANSACTION_TYPE** — `TR_TYPE CHAR(2)` (PK), `TR_DESCRIPTION VARCHAR(50)`.
- **TRANSACTION_TYPE_CATEGORY** — composite PK `(TRC_TYPE_CODE CHAR(2),
  TRC_TYPE_CATEGORY CHAR(4))`, `TRC_CAT_DATA VARCHAR(50)`, FK
  `TRC_TYPE_CODE → TRANSACTION_TYPE.TR_TYPE` with **ON DELETE RESTRICT**
  (a type that owns categories cannot be deleted → HTTP 409).

## REST API

### Transaction types
- `GET    /api/transaction-types?search=&page=&size=` — paginated list / search
- `GET    /api/transaction-types/{code}` — single type with its categories
- `POST   /api/transaction-types` — create (code = 2 chars, description 1–50)
- `PUT    /api/transaction-types/{code}` — update description
- `DELETE /api/transaction-types/{code}` — delete (409 if categories exist)
- `POST   /api/transaction-types/batch` — transactional INSERT/UPDATE/DELETE list

### Transaction categories
- `GET    /api/transaction-types/{code}/categories`
- `POST   /api/transaction-types/{code}/categories`
- `PUT    /api/transaction-types/{code}/categories/{catCode}`
- `DELETE /api/transaction-types/{code}/categories/{catCode}`

Errors: `400` validation, `404` not found, `409` duplicate key / referential
integrity violation.

## Tech stack

Java 17, Spring Boot 3.2, Spring Data JPA, H2 (in-memory), Flyway, Bean
Validation, JUnit 5 + MockMvc.

## Build & run

```bash
# requires JAVA_HOME pointing at a JDK 17
mvn test          # run the test suite
mvn spring-boot:run
# H2 console at http://localhost:8080/h2-console (jdbc:h2:mem:carddemo)
```
