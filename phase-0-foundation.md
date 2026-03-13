# Phase 0: Foundation & Preparation

**Risk Level:** None  
**Objective:** Set up the Java project structure (Spring Boot multi-module), CI/CD pipeline, relational database schema, data migration scripts, and test framework before any COBOL conversion begins.

> This phase has no direct COBOL conversion. It establishes the infrastructure that all subsequent phases depend on.

---

## Wave 0.1: Java Project Scaffolding & CI/CD Pipeline

### Objective
Create the Spring Boot multi-module Maven/Gradle project and establish the CI/CD pipeline.

### Deliverables

- **Spring Boot multi-module project** with the following modules:
  - `carddemo-common` - Shared entities, DTOs, utilities
  - `carddemo-web` - REST API controllers, Spring MVC / web UI
  - `carddemo-batch` - Spring Batch jobs
  - `carddemo-security` - Spring Security configuration
  - `carddemo-service` - Business logic services
  - `carddemo-repository` - JPA repositories, data access layer

- **Technology stack:**
  - Java 17+
  - Spring Boot 3.x
  - Spring Data JPA / Hibernate
  - Spring Batch
  - Spring Security
  - PostgreSQL (or MySQL) as the target RDBMS
  - Maven or Gradle build system

- **CI/CD pipeline:**
  - Build and unit test on every push
  - Integration test stage
  - Code quality gates (SonarQube / Checkstyle)
  - Artifact publishing
  - Deployment stages (dev, staging, production)

### Acceptance Criteria
- [ ] Project builds successfully with `mvn clean install` / `gradle build`
- [ ] CI/CD pipeline runs on every commit
- [ ] All modules resolve dependencies correctly

---

## Wave 0.2: Relational Database Schema Design

### Objective
Design and implement the relational DB schema by mapping VSAM file layouts (defined in copybooks) to normalized relational tables.

### VSAM-to-Table Mapping

| VSAM File (CICS Name) | Copybook Layout | Target Table | Primary Key |
|---|---|---|---|
| `ACCTDAT` | `CVACT01Y.cpy`, `CVACT02Y.cpy`, `CVACT03Y.cpy` | `accounts` | `account_id` |
| `CARDDAT` | `CVCRD01Y.cpy` | `cards` | `card_number` |
| `CCXREF` | (cross-reference layout) | `card_xref` | `card_number` + `account_id` |
| `CUSTDAT` | `CVCUS01Y.cpy` | `customers` | `customer_id` |
| `TRANSACT` | `CVTRA01Y.cpy` - `CVTRA07Y.cpy` | `transactions` | `transaction_id` |
| `USRSEC` | `CSUSR01Y.cpy` | `users` | `user_id` |
| `DISCGRP` | (disclosure group layout) | `disclosure_groups` | `group_id` |

### Source Copybooks for Schema Design

Located in `app/cpy/`:

| Copybook | Content | Maps To |
|---|---|---|
| `CVACT01Y.cpy` | Account record layout | `accounts` table columns |
| `CVACT02Y.cpy` | Account detail layout | `accounts` table (extended) |
| `CVACT03Y.cpy` | Account summary layout | `accounts` table (summary fields) |
| `CVCRD01Y.cpy` | Card record layout | `cards` table columns |
| `CVCUS01Y.cpy` | Customer record layout | `customers` table columns |
| `CVTRA01Y.cpy` | Transaction record layout | `transactions` table columns |
| `CVTRA02Y.cpy` | Transaction detail layout | `transactions` (detail fields) |
| `CVTRA03Y.cpy` | Transaction summary layout | `transactions` (summary) |
| `CVTRA04Y.cpy` | Transaction type layout | `transaction_types` table |
| `CVTRA05Y.cpy` | Transaction extended layout | `transactions` (extended) |
| `CVTRA06Y.cpy` | Transaction category layout | `transaction_categories` |
| `CVTRA07Y.cpy` | Transaction balance layout | `transaction_balances` |
| `CSUSR01Y.cpy` | User security record layout | `users` table columns |
| `CUSTREC.cpy` | Customer record (alt layout) | `customers` (validation) |

### Deliverables
- Flyway or Liquibase migration scripts for schema creation
- Entity-relationship diagram (ERD)
- Index definitions matching VSAM key structures and alternate index paths

### Acceptance Criteria
- [ ] All 7+ tables created with proper constraints and indexes
- [ ] Foreign key relationships established (e.g., `cards.account_id` -> `accounts.account_id`)
- [ ] Migration scripts are idempotent and version-controlled

---

## Wave 0.3: Data Migration ETL Scripts

### Objective
Create ETL (Extract, Transform, Load) scripts to migrate data from the legacy flat files into the new relational database.

### Source Data Files

**ASCII Files** (`app/data/ASCII/`):

| File | Description | Target Table |
|---|---|---|
| `acctdata.txt` | Account records | `accounts` |
| `carddata.txt` | Card records | `cards` |
| `cardxref.txt` | Card-account cross-references | `card_xref` |
| `custdata.txt` | Customer records | `customers` |
| `dailytran.txt` | Daily transactions | `transactions` |
| `discgrp.txt` | Disclosure groups | `disclosure_groups` |
| `tcatbal.txt` | Transaction category balances | `transaction_balances` |
| `trancatg.txt` | Transaction categories | `transaction_categories` |
| `trantype.txt` | Transaction types | `transaction_types` |

**EBCDIC Files** (`app/data/EBCDIC/`):

| File | Description | Notes |
|---|---|---|
| `AWS.M2.CARDDEMO.USRSEC.PS` | User security data | EBCDIC-encoded, requires codepage conversion |
| `AWS.M2.CARDDEMO.ACCTDATA.PS` | Account data (EBCDIC) | Alternative to ASCII `acctdata.txt` |
| `AWS.M2.CARDDEMO.CARDDATA.PS` | Card data (EBCDIC) | Alternative to ASCII `carddata.txt` |
| `AWS.M2.CARDDEMO.CUSTDATA.PS` | Customer data (EBCDIC) | Alternative to ASCII `custdata.txt` |
| `AWS.M2.CARDDEMO.DALYTRAN.PS` | Daily transactions (EBCDIC) | Includes `.INIT` variant |
| `AWS.M2.CARDDEMO.DISCGRP.PS` | Disclosure groups (EBCDIC) | |
| `AWS.M2.CARDDEMO.EXPORT.DATA.PS` | Export data (EBCDIC) | Multi-record format per `CVEXPORT.cpy` |
| `AWS.M2.CARDDEMO.CARDXREF.PS` | Card cross-reference (EBCDIC) | |
| `AWS.M2.CARDDEMO.TCATBALF.PS` | Transaction category balances | |
| `AWS.M2.CARDDEMO.TRANCATG.PS` | Transaction categories | |
| `AWS.M2.CARDDEMO.TRANTYPE.PS` | Transaction types | |

### ETL Approach
1. Parse fixed-width ASCII files using copybook layouts as format definitions
2. Handle EBCDIC-to-ASCII conversion for the `USRSEC.PS` file (and other EBCDIC files if ASCII versions are unavailable)
3. Apply data type transformations (COBOL `PIC 9` -> Java `BigDecimal`, packed decimal -> numeric, etc.)
4. Load into target relational tables with validation and error reporting

### Deliverables
- Spring Batch ETL jobs or standalone migration scripts
- EBCDIC-to-ASCII conversion utility
- Data validation report template
- Rollback / re-run capability

### Acceptance Criteria
- [ ] All 9 ASCII data files parsed and loaded successfully
- [ ] EBCDIC user security data converted and loaded
- [ ] Row counts match between source and target
- [ ] Data type conversions verified (especially packed decimal and date fields)

---

## Wave 0.4: Test Framework Establishment

### Objective
Set up the testing infrastructure that will be used to validate every subsequent migration wave.

### Test Framework Components

| Component | Technology | Purpose |
|---|---|---|
| Unit Tests | JUnit 5 + Mockito | Test individual service methods and utilities |
| Integration Tests | Spring Boot Test + Testcontainers | Test with real database (PostgreSQL in Docker) |
| API Tests | REST Assured / MockMvc | Test REST endpoints |
| Parallel-Run Harness | Custom comparator framework | Compare COBOL vs Java outputs side-by-side |
| Test Data Management | DbUnit / SQL scripts | Seed test databases with known data sets |

### Parallel-Run Validation Harness
A key deliverable is a comparison framework that can:
1. Accept identical inputs for both COBOL and Java systems
2. Capture outputs from both systems
3. Compare results field-by-field
4. Generate difference reports highlighting discrepancies
5. Support both online (request/response) and batch (file-based) comparisons

### Deliverables
- JUnit 5 test configuration with Spring Boot Test
- Testcontainers setup for PostgreSQL
- Parallel-run validation framework (skeleton)
- Test data seed scripts derived from `app/data/ASCII/` files
- CI integration for test execution

### Acceptance Criteria
- [ ] Unit test framework runs in CI pipeline
- [ ] Integration tests connect to Testcontainers PostgreSQL
- [ ] Parallel-run harness skeleton can compare two result sets
- [ ] Test data loads successfully from seed scripts

---

## Key Deliverables Summary

| Deliverable | Wave |
|---|---|
| Spring Boot multi-module project | 0.1 |
| CI/CD pipeline | 0.1 |
| Relational database schema + migrations | 0.2 |
| Data migration ETL scripts | 0.3 |
| EBCDIC conversion utility | 0.3 |
| JUnit + Integration test framework | 0.4 |
| Parallel-run validation harness | 0.4 |

---

## Dependencies
- None (this is the foundation phase)

## Next Phase
Proceed to [Phase 1: Shared Services & Utilities](phase-1-shared-services-utilities.md) once all Wave 0.x deliverables are accepted.
