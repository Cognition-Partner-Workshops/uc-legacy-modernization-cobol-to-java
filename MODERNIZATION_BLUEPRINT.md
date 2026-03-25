# CardDemo Modernization Blueprint

## Executive Summary

This blueprint evaluates four modernization strategies -- **Strangler Fig**, **Replatform**, **Refactor**, and **Rewrite** -- for each functional area of the AWS CardDemo mainframe application. CardDemo is a COBOL/CICS/VSAM credit card management system comprising 31 online and batch programs, 30 copybooks, 17 BMS screen maps, and 38 JCL batch jobs. The target platform is Java (Spring Boot / Spring Batch) with a relational database backend.

---

## Codebase Inventory

| Category | Count | Technology |
|---|---|---|
| Online CICS programs | 16 | COBOL/CICS |
| Batch programs | 15 | COBOL/JCL |
| Copybooks (data structures) | 30 | COBOL |
| BMS screen maps | 17 | BMS/3270 |
| JCL batch jobs | 38 | JCL |
| VSAM data files | 7+ | KSDS/ESDS/RRDS |
| Optional modules | 3 | IMS-DB2-MQ, DB2, VSAM-MQ |

## Functional Areas Identified

1. **Authentication & Session Management** -- Sign-on, user identity, role routing
2. **Account Management** -- Account view, account update, customer data
3. **Card Management** -- Card list, card view, card update, cross-reference lookup
4. **Transaction Processing (Online)** -- Transaction list, view, add
5. **Bill Payment** -- Online bill payment with balance adjustment
6. **Reporting** -- Online report submission (monthly/yearly/custom)
7. **User Administration** -- User list, add, update, delete (admin-only)
8. **Batch Processing** -- Transaction posting, interest calculation, statement generation, data export/import
9. **Optional Integrations** -- IMS/DB2/MQ authorization, DB2 transaction types, VSAM-MQ messaging

---

## Strategy Evaluation by Functional Area

### 1. Authentication & Session Management

**Programs:** `COSGN00C` (261 LOC)
**Data stores:** USRSEC VSAM file
**Complexity:** Low (single program, simple password check, role routing)

| Strategy | Fit | Rationale |
|---|---|---|
| **Strangler Fig** | Poor | Too small to justify incremental wrapping |
| **Replatform** | Poor | CICS COMMAREA session model has no direct Java equivalent |
| **Refactor** | Fair | Could adapt logic, but security model needs modernization |
| **Rewrite** | **Recommended** | Replace with Spring Security + JWT/OAuth2. Plaintext password storage and 8-char limits must be eliminated. Modern auth patterns are well-established and low risk. |

**Recommendation:** **Rewrite** using Spring Security. This is the foundation for all other modules and should be migrated first.

---

### 2. Account Management

**Programs:** `COACTVWC` (942 LOC), `COACTUPC` (4,237 LOC)
**Data stores:** ACCTDAT, CARDDAT, CUSTDAT, CARDAIX, CXACAIX VSAM files
**Copybooks:** CVACT01Y (account, 300 bytes), CVACT02Y (card, 150 bytes), CVCUS01Y (customer, 500 bytes), CVACT03Y (xref, 50 bytes)
**Complexity:** High -- `COACTUPC` is the largest program with extensive field-level validation (SSN, phone, dates, credit limits), multi-file reads/updates, and complex state management.

| Strategy | Fit | Rationale |
|---|---|---|
| **Strangler Fig** | **Recommended** | High coupling to shared data stores. Wrap VSAM reads behind a new Account Service API; route new consumers to Java while COBOL continues operating. Allows incremental field-by-field migration of the 4,237-line update program. |
| **Replatform** | Fair | Auto-conversion tools can handle the data structures but will produce brittle Java from the complex validation logic |
| **Refactor** | Fair | Validation logic is procedural and deeply nested; refactoring in COBOL offers limited value before migration |
| **Rewrite** | Poor | Too large and too central to rewrite in a single pass without high risk |

**Recommendation:** **Strangler Fig**. Stand up Account Service (REST/JPA) over PostgreSQL; migrate read path (view) first, then write path (update) in a second phase.

---

### 3. Card Management

**Programs:** `COCRDLIC` (card list), `COCRDSLC` (card view), `COCRDUPC` (card update)
**Data stores:** CARDDAT, CARDAIX (alternate index by account), CCXREF
**Complexity:** Medium -- paginated list with selection routing, detail view, update with card number validation

| Strategy | Fit | Rationale |
|---|---|---|
| **Strangler Fig** | **Recommended** | Shares data with Account Management. Co-migrate with Account Service, exposing card endpoints on the same service or a dedicated Card Service. |
| **Replatform** | Fair | Card data structures are straightforward; auto-conversion is feasible |
| **Refactor** | Poor | Limited value in COBOL refactoring |
| **Rewrite** | Fair | Smaller scope than account update, but still coupled to shared VSAM files |

**Recommendation:** **Strangler Fig**, co-migrated with Account Management (same bounded context).

---

### 4. Transaction Processing (Online)

**Programs:** `COTRN00C` (700 LOC, list), `COTRN01C` (view), `COTRN02C` (784 LOC, add)
**Data stores:** TRANSACT, ACCTDAT, CCXREF, CXACAIX VSAM files
**Copybooks:** CVTRA05Y (transaction record, 350 bytes)
**Complexity:** Medium-High -- Transaction add (`COTRN02C`) performs cross-reference validation, date validation via subroutine call (`CSUTLDTC`), amount formatting, and writes to TRANSACT file.

| Strategy | Fit | Rationale |
|---|---|---|
| **Strangler Fig** | **Recommended** | Transaction data is read by multiple consumers (batch posting, reports, statements). Wrap write path behind a Transaction Service API. Read-only list/view can be migrated first. |
| **Replatform** | Fair | Logic is moderately complex but procedural |
| **Refactor** | Poor | Date validation subroutine already well-factored |
| **Rewrite** | Fair | Could rewrite if decoupled from batch, but strangler is safer |

**Recommendation:** **Strangler Fig**. Create Transaction Service; migrate list/view (read-only) first, then add (write) once Account and Card services are stable.

---

### 5. Bill Payment

**Programs:** `COBIL00C` (573 LOC)
**Data stores:** TRANSACT, ACCTDAT, CXACAIX VSAM files
**Complexity:** Medium -- Reads account balance, creates a payment transaction record, updates account balance. Generates a new sequential transaction ID.

| Strategy | Fit | Rationale |
|---|---|---|
| **Strangler Fig** | Fair | Depends on Account and Transaction services |
| **Replatform** | Poor | Business logic needs modernization (e.g., idempotency, concurrency) |
| **Refactor** | Poor | Limited value |
| **Rewrite** | **Recommended** | Self-contained business flow (573 LOC). Once Account and Transaction services exist, bill payment becomes a thin orchestration layer. Modern payment patterns (idempotency keys, saga pattern) are required regardless. |

**Recommendation:** **Rewrite** as a Payment Service that orchestrates calls to Account and Transaction services.

---

### 6. Reporting

**Programs:** `CORPT00C` (650 LOC), `CBTRN03C` (batch report generation)
**Data stores:** TRANSACT VSAM file; JCL internal reader for job submission
**Complexity:** Medium -- Online program builds JCL dynamically and submits via CICS TDQ to the internal reader. Batch program reads TRANSACT file and produces reports.

| Strategy | Fit | Rationale |
|---|---|---|
| **Strangler Fig** | Poor | JCL submission via internal reader has no Java equivalent |
| **Replatform** | Poor | TDQ/INTRDR mechanism is mainframe-specific |
| **Refactor** | Poor | Report generation logic is straightforward but platform-tied |
| **Rewrite** | **Recommended** | Replace with Spring Batch report generation + a REST API for report requests. Use a job scheduler (Spring `@Scheduled` or Quartz) instead of JCL submission. Report output moves from print files to PDF/HTML generation. |

**Recommendation:** **Rewrite**. Replace JCL submission with Spring Batch jobs triggered via REST API.

---

### 7. User Administration

**Programs:** `COUSR00C` (list, 696 LOC), `COUSR01C` (add), `COUSR02C` (update), `COUSR03C` (delete)
**Data stores:** USRSEC VSAM file
**Complexity:** Low-Medium -- Standard CRUD with paginated list. Admin-only access.

| Strategy | Fit | Rationale |
|---|---|---|
| **Strangler Fig** | Fair | Small scope reduces strangler benefits |
| **Replatform** | Fair | CRUD logic is simple enough for auto-conversion |
| **Refactor** | Poor | Limited value |
| **Rewrite** | **Recommended** | Tightly coupled to Authentication rewrite. User entity needs modern fields (email, hashed password, roles). CRUD is trivial to implement with Spring Data JPA. |

**Recommendation:** **Rewrite** alongside Authentication. Implement as part of the User/Auth Service.

---

### 8. Batch Processing

**Programs:** `CBTRN02C` (transaction posting, 732 LOC), `CBACT04C` (interest calculation, 653 LOC), `CBSTM03A/B` (statement generation, 924 LOC), `CBTRN01C/03C` (transaction report), `CBACT01C-03C` (account/card/customer data load), `CBCUS01C` (customer load), `CBEXPORT/CBIMPORT` (data export/import)
**JCL jobs:** POSTTRAN, INTCALC, COMBTRAN, CREASTMT, TRANBKP, TRANIDX, CLOSEFIL, OPENFIL, and data refresh jobs
**Batch cycle:** `CLOSEFIL -> data refresh -> POSTTRAN -> INTCALC -> TRANBKP -> COMBTRAN -> CREASTMT -> TRANIDX -> OPENFIL`
**Complexity:** High -- Multi-step orchestrated batch cycle with file open/close coordination, GDG management, alternate index rebuilds, and cross-program data dependencies.

| Strategy | Fit | Rationale |
|---|---|---|
| **Strangler Fig** | Fair | Batch programs can be replaced one at a time if data layer is shared |
| **Replatform** | **Recommended for posting/interest** | `CBTRN02C` (posting) and `CBACT04C` (interest) are pure file-processing logic with clear input/output contracts. Replatform to Spring Batch with JPA readers/writers while preserving the calculation logic. |
| **Refactor** | Poor | JCL orchestration cannot be refactored incrementally |
| **Rewrite** | **Recommended for statements/reports** | `CBSTM03A` uses mainframe-specific patterns (PSA/TCB/TIOT addressing, ALTER/GO TO, CALL to subroutine `CBSTM03B`). These have no portable equivalent and must be rewritten. Statement generation should produce PDF/HTML via modern templating. |

**Recommendation:** **Mixed** -- Replatform posting and interest calculation to Spring Batch; Rewrite statement generation and report jobs. Replace JCL orchestration with Spring Batch job orchestration or a workflow engine.

---

### 9. Optional Integrations

**Modules:** `app-authorization-ims-db2-mq/` (8 programs), `app-transaction-type-db2/` (3 programs), `app-vsam-mq/` (2 programs)
**Technologies:** IMS DB, DB2, IBM MQ
**Complexity:** Medium-High -- These modules add IMS database access, DB2 embedded SQL, and MQ messaging.

| Strategy | Fit | Rationale |
|---|---|---|
| **Strangler Fig** | Fair | MQ integration can be replaced with a modern message broker |
| **Replatform** | Fair | DB2 SQL can be migrated to PostgreSQL/MySQL relatively directly |
| **Refactor** | Poor | Platform-specific (IMS, MQ) |
| **Rewrite** | **Recommended** | IMS DB access has no Java equivalent. MQ can be replaced with RabbitMQ/Kafka. DB2 CRUD becomes JPA repositories. Authorization logic should be reimplemented with modern fraud detection patterns. |

**Recommendation:** **Rewrite**. These are optional add-on modules and can be deferred to later phases.

---

## Strategy Summary Matrix

| Functional Area | Recommended Strategy | LOC (est.) | Risk | Priority |
|---|---|---|---|---|
| Authentication & Session | Rewrite | ~260 | Low | P0 (first) |
| User Administration | Rewrite | ~1,800 | Low | P0 (with auth) |
| Account Management | Strangler Fig | ~5,200 | High | P1 |
| Card Management | Strangler Fig | ~2,500 | Medium | P1 (with accounts) |
| Transaction Processing | Strangler Fig | ~2,100 | Medium-High | P2 |
| Bill Payment | Rewrite | ~570 | Medium | P3 |
| Reporting | Rewrite | ~1,300 | Medium | P3 |
| Batch -- Posting/Interest | Replatform | ~1,400 | High | P4 |
| Batch -- Statements | Rewrite | ~1,900 | Medium | P4 |
| Optional Integrations | Rewrite | ~2,500 | Low | P5 (deferred) |

---

## Technology Target Stack

| Current | Target |
|---|---|
| COBOL | Java 17+ |
| CICS | Spring Boot (REST APIs) |
| BMS 3270 screens | React / Angular SPA |
| VSAM KSDS/ESDS | PostgreSQL (relational) |
| JCL batch orchestration | Spring Batch + job scheduler |
| COMMAREA | REST request/response DTOs |
| Copybooks | Java POJOs / JPA entities |
| CICS file I/O | Spring Data JPA repositories |
| IBM MQ (optional) | RabbitMQ / Apache Kafka |
| IMS DB (optional) | PostgreSQL |
| DB2 (optional) | PostgreSQL |

---

## Key Mapping Patterns

| COBOL/Mainframe Pattern | Java Target Pattern |
|---|---|
| CICS transaction (e.g., CC00) | REST controller endpoint |
| BMS MAP SEND/RECEIVE | SPA component with REST call |
| VSAM READ/WRITE/REWRITE | JPA repository `findById` / `save` |
| VSAM STARTBR/READNEXT/ENDBR | JPA `findAll` with pagination (`Pageable`) |
| COPY copybook | Java DTO / Entity class |
| Working-Storage Section | Java class instance fields |
| PERFORM paragraph | Java method call |
| EVALUATE TRUE | Java `switch` or `if-else` chain |
| COMMAREA | Request/Response DTO |
| JCL batch job | Spring Batch `Job` + `Step` |
| CICS XCTL | Spring controller redirect / service call |
| 88-level condition names | Java `enum` or boolean methods |

---

## Data Migration Strategy

### VSAM to PostgreSQL Table Mapping

| VSAM File | Key | Record Size | Target Table | Notes |
|---|---|---|---|---|
| USRSEC | USR-ID (X8) | 80 bytes | `users` | Add password hashing, email, roles |
| ACCTDAT | ACCT-ID (9-11) | 300 bytes | `accounts` | Direct field mapping |
| CARDDAT | CARD-NUM (X16) | 150 bytes | `cards` | Add FK to accounts |
| CUSTDAT | CUST-ID (9-9) | 500 bytes | `customers` | Direct field mapping |
| CCXREF | CARD-NUM (X16) | 50 bytes | Eliminated -- use FKs on `cards` table | Denormalized xref becomes JOIN |
| TRANSACT | TRAN-ID (X16) | 350 bytes | `transactions` | Direct field mapping |
| TCATBAL | ACCT+TYPE+CAT | 50 bytes | `transaction_category_balances` | Composite key |
| DISCGRP | GROUP+TYPE+CAT | 50 bytes | `discount_groups` | Interest rate configuration |

### Migration Approach
1. Extract VSAM data using existing `CBEXPORT` program to ASCII flat files
2. Transform using ETL scripts (field mapping, data type conversion, EBCDIC handling)
3. Load into PostgreSQL tables via bulk INSERT / `COPY` command
4. Validate record counts and field checksums

---

## Success Criteria

- All CICS online transactions have equivalent REST API endpoints
- All BMS screens have equivalent SPA UI pages
- All batch processing (posting, interest, statements) runs in Spring Batch
- All VSAM data successfully migrated to PostgreSQL with zero data loss
- Response times within 200ms for online operations (vs. sub-second 3270)
- Batch cycle completes within existing SLA windows
- Authentication upgraded to modern standards (hashed passwords, RBAC)
- Automated test coverage >= 80% for migrated services
