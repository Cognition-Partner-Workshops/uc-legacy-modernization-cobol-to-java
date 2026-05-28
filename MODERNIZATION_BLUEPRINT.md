# CardDemo Modernization Blueprint

## 1. Executive Summary

CardDemo is a ~20,650-line COBOL/CICS mainframe credit card management system comprising 30 online programs, 29 copybooks, 40+ JCL jobs, and 3 optional extension modules (IMS/DB2/MQ Authorization, DB2 Transaction Types, VSAM/MQ Account Extraction). This blueprint evaluates four modernization strategies — **Strangler Fig**, **Replatform**, **Refactor**, and **Rewrite** — for each functional area, and recommends a composite approach that minimizes risk while maximizing business value.

---

## 2. System Inventory Summary

| Metric | Count |
|---|---|
| Core COBOL programs (app/cbl) | 30 |
| Copybooks (app/cpy) | 29 |
| BMS screen maps (app/bms) | 17 |
| JCL batch jobs (app/jcl) | 40 |
| CICS online transactions | 20 |
| Optional extension modules | 3 |
| Total COBOL LOC (core) | ~20,650 |
| Control-M scheduler workflows | 4 (Daily, Weekly×2, Monthly) |
| VSAM KSDS data files | 12 |
| Technologies | COBOL, CICS, VSAM, JCL, DB2, IMS DB, MQ, RACF, ASM |

---

## 3. Functional Area Identification

### 3.1 Authentication & User Security
**Programs:** COSGN00C (260 lines)
**Data stores:** USRSEC VSAM (CSUSR01Y copybook — 80-byte records)
**Description:** CICS sign-on screen, RACF-simulated user/password authentication, user type routing (Admin vs Regular).

### 3.2 Menu Navigation & Session Management
**Programs:** COMEN01C (308 lines), COADM01C (288 lines)
**Data stores:** CARDDEMO-COMMAREA (COCOM01Y — shared session state)
**Description:** Main menu routing for regular users (11 options) and admin menu. COMMAREA-based session propagation between programs.

### 3.3 Account Management
**Programs:** COACTVWC (941 lines), COACTUPC (4,236 lines — largest online program)
**Data stores:** ACCTDAT VSAM KSDS (CVACT01Y — 300-byte records), CARDXREF (CVACT03Y — 50 bytes)
**Description:** View and update credit card account details including balances, credit limits, dates, and status. Heavy validation logic in COACTUPC.

### 3.4 Credit Card Management
**Programs:** COCRDLIC (1,459 lines), COCRDSLC (887 lines), COCRDUPC (1,560 lines)
**Data stores:** CARDDAT VSAM KSDS (CVACT02Y — 150 bytes), CARDXREF (CVACT03Y)
**Description:** List, view detail, and update credit card records. Cross-references cards to accounts and customers via XREF file.

### 3.5 Transaction Processing (Online)
**Programs:** COTRN00C (699 lines), COTRN01C (330 lines), COTRN02C (783 lines)
**Data stores:** TRANSACT VSAM KSDS (CVTRA05Y — 350 bytes), DALYTRAN (CVTRA06Y), TRANCATG (CVTRA04Y), TRANTYPE (CVTRA03Y)
**Description:** List, view, and add transactions. Uses transaction type and category reference data.

### 3.6 Transaction Processing (Batch)
**Programs:** CBTRN02C (731 lines), CBTRN01C (494 lines), CBTRN03C (649 lines)
**Data stores:** DALYTRAN, TRANSACT, XREFFILE, ACCTFILE, TCATBALF (CVTRA01Y), DALYREJS
**Description:** Core batch pipeline — post daily transactions, validate against XREF, update account balances, update category balances, generate reject files. CBTRN03C produces transaction reports.

### 3.7 Bill Payment
**Programs:** COBIL00C (572 lines)
**Data stores:** TRANSACT, ACCTDAT, CXACAIX (alternate index)
**Description:** Online bill payment — pay account balance in full, create a payment transaction record.

### 3.8 Reporting & Statements
**Programs:** CORPT00C (649 lines), CBSTM03A (924 lines), CBSTM03B (230 lines)
**Data stores:** TRANSACT, ACCTDAT, CUSTDATA, STMTFILE, HTMLFILE
**Description:** Online report submission (via TDQ to internal reader), batch statement generation in plain text and HTML formats. Uses COMP/COMP-3, ALTER/GO-TO, 2D arrays, subroutine calls.

### 3.9 Interest Calculation
**Programs:** CBACT04C (652 lines)
**Data stores:** TCATBALF, XREFFILE, ACCTFILE, DISCGRP (CVTRA02Y — interest rates)
**Description:** Monthly batch interest calculation. Reads disclosure groups for interest rates by account group/transaction type, applies to category balances, updates account current balance.

### 3.10 User Administration
**Programs:** COUSR00C (695 lines), COUSR01C (299 lines), COUSR02C (414 lines), COUSR03C (359 lines)
**Data stores:** USRSEC VSAM
**Description:** Admin-only CRUD for user accounts — list, add, update, delete security records.

### 3.11 Data Migration & Export/Import
**Programs:** CBEXPORT (582 lines), CBIMPORT (487 lines)
**Data stores:** All VSAM files → EXPORT sequential file (CVEXPORT — 500-byte multi-record layout with REDEFINES, OCCURS, COMP-3)
**Description:** Branch migration subsystem. Multi-record type export with customers, accounts, transactions, cards, XREF. Uses complex copybook structures (REDEFINES, OCCURS, COMP-3).

### 3.12 Authorization Processing (Optional — IMS/DB2/MQ)
**Programs:** COPAUA0C, COPAUS0C, COPAUS1C, COPAUS2C, CBPAUP0C, PAUDBLOD, PAUDBUNL, DBUNLDGS
**Data stores:** IMS HIDAM DB (DBPAUTP0), DB2 AUTHFRDS table, MQ request/reply queues
**Description:** Real-time credit card authorization via MQ, IMS storage, fraud detection in DB2. Two-phase commit across IMS/DB2. Batch purging of expired authorizations.

### 3.13 Transaction Type Management (Optional — DB2)
**Programs:** COTRTLIC, COTRTUPC, COBTUPDT
**Data stores:** DB2 tables (TRNTYPE, TRNTYCAT), VSAM extracts
**Description:** Admin CRUD for transaction type reference data. DB2 cursors, SQL inserts/updates/deletes. Batch extract to VSAM for runtime use.

### 3.14 MQ Account Extraction (Optional — VSAM/MQ)
**Programs:** CODATE01, COACCT01
**Data stores:** VSAM + MQ queues
**Description:** System date and account detail inquiries via MQ request/response pattern. Demonstrates asynchronous integration.

### 3.15 Shared Utilities & Infrastructure
**Programs:** CSUTLDTC (157 lines), COBSWAIT (41 lines), ASM modules (MVSWAIT, COBDATFT)
**Data stores:** CSLKPCDY (1,318-line lookup table copybook), CSSTRPFY, CSSETATY, CODATECN
**Description:** Date utilities, string processing, wait timers, lookup code data. Assembler modules for system-level operations.

---

## 4. Strategy Evaluation per Functional Area

### Evaluation Criteria

| Criterion | Weight | Description |
|---|---|---|
| Risk | 30% | Likelihood of regression, data loss, or outage |
| Cost | 20% | Development effort, licensing, infrastructure |
| Time-to-Value | 20% | Speed to production with business benefit |
| Maintainability | 15% | Long-term developer experience and extensibility |
| Fidelity | 15% | Exactness of behavior preservation |

### Strategy Definitions

- **Strangler Fig**: Incrementally replace functionality by routing traffic to new services while legacy continues to run. Coexistence via anti-corruption layer.
- **Replatform**: Lift COBOL code to a modern runtime (e.g., Micro Focus on Linux, AWS Mainframe Modernization / Blu Age) with minimal code changes. Retains COBOL logic.
- **Refactor**: Automated or semi-automated conversion of COBOL to Java (or similar) preserving program structure. Tools: AWS Blu Age, Heirloom, TSRI.
- **Rewrite**: Clean-room reimplementation in Java/Spring Boot with modern architecture, REST APIs, and relational database.

---

### 4.1 Authentication & User Security

| Strategy | Risk | Cost | Time | Maintain. | Fidelity | Score |
|---|---|---|---|---|---|---|
| Strangler Fig | Low | Low | Fast | High | High | **88** |
| Replatform | Low | Low | Fast | Low | High | 70 |
| Refactor | Low | Med | Med | Med | High | 72 |
| Rewrite | Low | Med | Med | High | Med | 78 |

**Recommendation: Strangler Fig → Rewrite**
Small surface area (260 lines, single VSAM file). Replace with modern JWT/OAuth2 authentication service first. Use an anti-corruption layer to translate legacy COMMAREA session tokens. This is the natural starting point for modernization.

---

### 4.2 Menu Navigation & Session Management

| Strategy | Risk | Cost | Time | Maintain. | Fidelity | Score |
|---|---|---|---|---|---|---|
| Strangler Fig | Med | Low | Fast | High | Med | **80** |
| Replatform | Low | Low | Fast | Low | High | 68 |
| Refactor | Low | Med | Med | Med | High | 70 |
| Rewrite | Med | Med | Med | High | Med | 74 |

**Recommendation: Strangler Fig (absorb into new UI layer)**
Menu programs are pure routing logic (COMMAREA propagation). They dissolve naturally when a modern web frontend replaces 3270 terminal screens. No standalone migration needed — absorbed as part of the new presentation layer.

---

### 4.3 Account Management

| Strategy | Risk | Cost | Time | Maintain. | Fidelity | Score |
|---|---|---|---|---|---|---|
| Strangler Fig | Med | Med | Med | High | High | **82** |
| Replatform | Low | Low | Fast | Low | High | 70 |
| Refactor | Med | Med | Med | Med | High | 72 |
| Rewrite | High | High | Slow | High | Med | 62 |

**Recommendation: Strangler Fig**
COACTUPC at 4,236 lines is the largest and most complex online program with extensive validation. Strangler Fig allows incremental extraction while the legacy system handles edge cases. Build a new Account Service that reads from the same data (via change data capture or dual-write) and progressively takes over traffic.

---

### 4.4 Credit Card Management

| Strategy | Risk | Cost | Time | Maintain. | Fidelity | Score |
|---|---|---|---|---|---|---|
| Strangler Fig | Med | Med | Med | High | High | **80** |
| Replatform | Low | Low | Fast | Low | High | 70 |
| Refactor | Med | Med | Med | Med | High | 72 |
| Rewrite | Med | High | Slow | High | Med | 64 |

**Recommendation: Strangler Fig**
Three tightly coupled programs (list/view/update) sharing CARDDAT and CARDXREF VSAM files. Extract as a Card Service behind the Account bounded context. The XREF file creates a natural join table that maps cleanly to a relational schema.

---

### 4.5 Transaction Processing (Online)

| Strategy | Risk | Cost | Time | Maintain. | Fidelity | Score |
|---|---|---|---|---|---|---|
| Strangler Fig | Med | Med | Med | High | High | **78** |
| Replatform | Low | Low | Fast | Low | High | 70 |
| Refactor | Med | Med | Med | Med | High | 72 |
| Rewrite | High | High | Slow | High | Med | 58 |

**Recommendation: Strangler Fig**
Online transaction entry (COTRN02C) writes to the TRANSACT VSAM file that is also consumed by batch processing. The coupling to batch makes a full rewrite risky. Use Strangler Fig to add a new transaction API that writes to both legacy VSAM and a new database during the transition period.

---

### 4.6 Transaction Processing (Batch)

| Strategy | Risk | Cost | Time | Maintain. | Fidelity | Score |
|---|---|---|---|---|---|---|
| Strangler Fig | High | High | Slow | High | Med | 60 |
| Replatform | Low | Low | Fast | Low | High | **76** |
| Refactor | Med | Med | Med | Med | High | **76** |
| Rewrite | High | High | Slow | High | Med | 54 |

**Recommendation: Replatform first, then Refactor**
CBTRN02C (daily posting) is the most critical batch program — it updates account balances and category totals. This is the highest-risk area. Replatform to Micro Focus or Blu Age first to de-risk the mainframe exit, then refactor to Java batch (Spring Batch) with comprehensive regression testing.

---

### 4.7 Bill Payment

| Strategy | Risk | Cost | Time | Maintain. | Fidelity | Score |
|---|---|---|---|---|---|---|
| Strangler Fig | Low | Med | Med | High | High | **84** |
| Replatform | Low | Low | Fast | Low | High | 70 |
| Refactor | Low | Med | Med | Med | High | 74 |
| Rewrite | Low | Med | Med | High | High | 80 |

**Recommendation: Strangler Fig → Rewrite**
Moderate complexity (572 lines), clear business function. Extract as a standalone Payment Service. The bill payment flow creates transaction records and updates balances — can be reimplemented as a single atomic operation in a new service once the Account and Transaction services are in place.

---

### 4.8 Reporting & Statements

| Strategy | Risk | Cost | Time | Maintain. | Fidelity | Score |
|---|---|---|---|---|---|---|
| Strangler Fig | Low | Med | Med | High | Med | 76 |
| Replatform | Low | Low | Fast | Low | High | 72 |
| Refactor | Med | Med | Med | Med | Med | 66 |
| Rewrite | Low | Med | Med | High | High | **82** |

**Recommendation: Rewrite**
CBSTM03A uses legacy coding patterns (ALTER, GO-TO, mainframe control block addressing) deliberately to test modernization tooling. These patterns make automated refactoring unreliable. A clean rewrite using modern reporting tools (JasperReports, Apache POI) with the same business logic is lower risk than trying to preserve these constructs.

---

### 4.9 Interest Calculation

| Strategy | Risk | Cost | Time | Maintain. | Fidelity | Score |
|---|---|---|---|---|---|---|
| Strangler Fig | High | Med | Slow | High | Med | 62 |
| Replatform | Low | Low | Fast | Low | High | **76** |
| Refactor | Med | Med | Med | Med | High | 74 |
| Rewrite | High | High | Slow | High | Med | 56 |

**Recommendation: Replatform, then Refactor**
Financial calculation logic (652 lines) with COMP-3 packed decimal arithmetic. Exact numeric fidelity is critical. Replatform preserves COBOL decimal semantics. After validating parity, refactor to Java using BigDecimal with explicit rounding rules. Requires exhaustive parallel-run testing.

---

### 4.10 User Administration

| Strategy | Risk | Cost | Time | Maintain. | Fidelity | Score |
|---|---|---|---|---|---|---|
| Strangler Fig | Low | Low | Fast | High | High | **86** |
| Replatform | Low | Low | Fast | Low | High | 70 |
| Refactor | Low | Med | Med | Med | High | 72 |
| Rewrite | Low | Med | Med | High | High | 82 |

**Recommendation: Strangler Fig → Rewrite**
Simple CRUD on USRSEC (4 programs, ~1,767 lines total). Directly replace with a User Management microservice. Can be done early alongside Authentication as part of the Identity domain.

---

### 4.11 Data Migration & Export/Import

| Strategy | Risk | Cost | Time | Maintain. | Fidelity | Score |
|---|---|---|---|---|---|---|
| Strangler Fig | Med | Med | Med | Med | Med | 68 |
| Replatform | Low | Low | Fast | Low | High | 72 |
| Refactor | Med | Med | Med | Med | High | **74** |
| Rewrite | Med | Med | Med | High | Med | 72 |

**Recommendation: Refactor**
CBEXPORT/CBIMPORT use complex copybook structures (REDEFINES, OCCURS, COMP-3) that are a good test for automated conversion tools. Refactor to Java with byte-level data handling, then evolve the export format to JSON/Parquet as downstream consumers are modernized.

---

### 4.12 Authorization Processing (IMS/DB2/MQ)

| Strategy | Risk | Cost | Time | Maintain. | Fidelity | Score |
|---|---|---|---|---|---|---|
| Strangler Fig | High | High | Slow | High | Med | 58 |
| Replatform | Med | Med | Med | Low | High | 66 |
| Refactor | Med | Med | Med | Med | Med | 66 |
| Rewrite | Med | High | Med | High | High | **72** |

**Recommendation: Rewrite**
This module spans three middleware technologies (IMS, DB2, MQ) with two-phase commit semantics. The IMS dependency is the hardest to replatform. Rewrite as an event-driven authorization service (Kafka/SQS) with a relational database, eliminating the IMS dependency entirely. Design the new service to implement the same MQ message contract initially for backward compatibility.

---

### 4.13 Transaction Type Management (DB2)

| Strategy | Risk | Cost | Time | Maintain. | Fidelity | Score |
|---|---|---|---|---|---|---|
| Strangler Fig | Low | Low | Fast | High | High | **84** |
| Replatform | Low | Low | Fast | Low | High | 70 |
| Refactor | Low | Med | Med | Med | High | 74 |
| Rewrite | Low | Med | Med | High | High | 80 |

**Recommendation: Strangler Fig → Rewrite**
Small DB2 CRUD module (3 programs). Already uses SQL, making the mapping to JPA/JDBC trivial. Extract as a reference data management service early. The existing DB2 schema can be migrated directly to PostgreSQL/MySQL.

---

### 4.14 MQ Account Extraction

| Strategy | Risk | Cost | Time | Maintain. | Fidelity | Score |
|---|---|---|---|---|---|---|
| Strangler Fig | Low | Low | Fast | High | High | **86** |
| Replatform | Low | Low | Fast | Low | High | 72 |
| Refactor | Low | Med | Med | Med | High | 74 |
| Rewrite | Low | Med | Med | High | High | 80 |

**Recommendation: Strangler Fig → Rewrite**
Two small programs demonstrating async patterns. Replace MQ with modern messaging (Kafka, SQS, or retained MQ with JMS) and expose account queries via REST API.

---

### 4.15 Shared Utilities & Infrastructure

| Strategy | Risk | Cost | Time | Maintain. | Fidelity | Score |
|---|---|---|---|---|---|---|
| Strangler Fig | N/A | N/A | N/A | N/A | N/A | N/A |
| Replatform | Low | Low | Fast | Low | High | **74** |
| Refactor | Med | Med | Med | Med | High | 72 |
| Rewrite | Low | Low | Fast | High | High | **74** |

**Recommendation: Rewrite (absorb into platform libraries)**
CSUTLDTC, date conversion utilities, and lookup tables become standard Java library functions (java.time, HashMap). The ASM modules (MVSWAIT, COBDATFT) have direct JDK equivalents. No separate migration — absorbed into the Java platform layer.

---

## 5. Recommended Composite Strategy

```
Phase 1 (Strangler Fig):  Auth, User Admin, Menus, MQ Extraction, Tran Type Mgmt
Phase 2 (Replatform):     Batch Transaction Processing, Interest Calculation
Phase 3 (Strangler Fig):  Account Mgmt, Card Mgmt, Online Transactions, Bill Payment
Phase 4 (Rewrite):        Reporting/Statements, Authorization (IMS/DB2/MQ)
Phase 5 (Refactor):       Data Migration/Export, remaining utilities
Phase 6 (Decommission):   Retire mainframe, remove anti-corruption layers
```

### Technology Target State

| Layer | Technology |
|---|---|
| Language | Java 21 (LTS) |
| Framework | Spring Boot 3.x / Spring Batch |
| API | REST (OpenAPI 3.0) + event-driven (Kafka/SQS) |
| Database | PostgreSQL (from VSAM/DB2) |
| Authentication | Spring Security + OAuth2/OIDC |
| UI | React or Angular SPA (replacing BMS/3270) |
| Scheduling | Spring Batch + Kubernetes CronJobs (replacing Control-M/JCL) |
| Messaging | Apache Kafka or Amazon SQS (replacing IBM MQ) |
| CI/CD | GitHub Actions / Jenkins |
| Observability | OpenTelemetry, Prometheus, Grafana |

---

## 6. Decision Matrix Summary

| Functional Area | LOC | Strategy | Phase | Risk | Rationale |
|---|---|---|---|---|---|
| Authentication | 260 | Strangler → Rewrite | 1 | Low | Small, self-contained, natural starting point |
| Menu/Navigation | 596 | Strangler (absorb) | 1 | Low | Dissolves into new UI layer |
| User Admin | 1,767 | Strangler → Rewrite | 1 | Low | Simple CRUD, pairs with Auth |
| MQ Extraction | ~400 | Strangler → Rewrite | 1 | Low | Small async module |
| Tran Type Mgmt | ~700 | Strangler → Rewrite | 1 | Low | Already SQL-based |
| Batch Tran Processing | 1,874 | Replatform → Refactor | 2 | High | Critical financial logic |
| Interest Calculation | 652 | Replatform → Refactor | 2 | High | COMP-3 decimal fidelity |
| Account Management | 5,177 | Strangler | 3 | Medium | Largest program, complex validation |
| Card Management | 3,906 | Strangler | 3 | Medium | Tightly coupled to accounts |
| Online Transactions | 1,812 | Strangler | 3 | Medium | Coupled to batch pipeline |
| Bill Payment | 572 | Strangler → Rewrite | 3 | Low | Clean business function |
| Reporting/Statements | 1,803 | Rewrite | 4 | Medium | Legacy coding patterns (ALTER/GO-TO) |
| Authorization (IMS) | ~2,000 | Rewrite | 4 | Medium | Multi-middleware, IMS dependency |
| Data Export/Import | 1,069 | Refactor | 5 | Medium | Complex copybook structures |
| Utilities | ~1,500 | Rewrite (absorb) | 5 | Low | Standard library equivalents |
