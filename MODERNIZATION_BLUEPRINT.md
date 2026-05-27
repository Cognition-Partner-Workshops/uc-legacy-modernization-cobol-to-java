# CardDemo Modernization Blueprint

## 1. Executive Summary

This blueprint evaluates four modernization strategies -- **Strangler Fig**, **Replatform**, **Refactor**, and **Rewrite** -- for each functional area of the CardDemo mainframe credit card management system. The system comprises ~30,000 lines of COBOL across 40+ programs, CICS online transactions, VSAM/DB2/IMS data stores, MQ-based integrations, and Control-M batch orchestration.

The recommended approach is a **hybrid strategy** that applies the most appropriate technique to each functional area based on complexity, risk, and business value.

---

## 2. System Inventory Summary

| Metric | Count |
|---|---|
| Core COBOL Programs (Online) | 18 |
| Core COBOL Programs (Batch) | 13 |
| Optional Module Programs | 13 |
| Copybooks (Data Structures) | 30 |
| BMS Screen Maps | 18 |
| JCL Batch Jobs | 37 |
| VSAM KSDS Files | 8 |
| Control-M Scheduled Workflows | 4 |
| Total COBOL Lines (Core) | ~20,650 |
| Total COBOL Lines (Optional Modules) | ~9,525 |

---

## 3. Functional Area Identification

### FA-1: Authentication & User Security
| Attribute | Detail |
|---|---|
| **Programs** | `COSGN00C` (260 LOC) |
| **Data Stores** | USRSEC VSAM KSDS |
| **Copybooks** | `CSUSR01Y` (user record), `COCOM01Y` (commarea) |
| **CICS Transactions** | CC00 (Sign-on) |
| **Interfaces** | None -- standalone entry point |
| **Complexity** | Low |

### FA-2: Menu Navigation & Routing
| Attribute | Detail |
|---|---|
| **Programs** | `COMEN01C` (308 LOC), `COADM01C` (288 LOC) |
| **Data Stores** | None (commarea-driven) |
| **Copybooks** | `COCOM01Y`, `COMEN02Y` (menu options) |
| **CICS Transactions** | CM00 (Main Menu), CA00 (Admin Menu) |
| **Interfaces** | XCTL to all downstream programs |
| **Complexity** | Low |

### FA-3: Account Management
| Attribute | Detail |
|---|---|
| **Programs** | `COACTVWC` (941 LOC), `COACTUPC` (4,236 LOC) |
| **Data Stores** | ACCTDAT VSAM KSDS, CARDXREF VSAM KSDS |
| **Copybooks** | `CVACT01Y` (account - 300B), `CVACT03Y` (xref - 50B) |
| **CICS Transactions** | CAVW (View), CAUP (Update) |
| **Interfaces** | Reads XREF to resolve Card-to-Account linkage |
| **Complexity** | **High** -- `COACTUPC` is the largest program (4,236 LOC) with extensive field-level validation, REDEFINES for phone numbers, and complex update logic |

### FA-4: Credit Card Management
| Attribute | Detail |
|---|---|
| **Programs** | `COCRDLIC` (1,459 LOC), `COCRDSLC` (887 LOC), `COCRDUPC` (1,560 LOC) |
| **Data Stores** | CARDDAT VSAM KSDS, CARDXREF VSAM KSDS, ACCTDAT VSAM KSDS |
| **Copybooks** | `CVACT02Y` (card - 150B), `CVACT03Y`, `CVCRD01Y` |
| **CICS Transactions** | CCLI (List), CCDL (View), CCUP (Update) |
| **Interfaces** | Cross-references Account and Customer via XREF |
| **Complexity** | **High** -- multi-file VSAM reads with cursor-based list pagination |

### FA-5: Transaction Processing (Online)
| Attribute | Detail |
|---|---|
| **Programs** | `COTRN00C` (699 LOC), `COTRN01C` (330 LOC), `COTRN02C` (783 LOC) |
| **Data Stores** | TRANSACT VSAM KSDS (with AIX), CARDXREF, ACCTDAT |
| **Copybooks** | `CVTRA05Y` (transaction - 350B), `CVTRA03Y`, `CVTRA04Y` |
| **CICS Transactions** | CT00 (List), CT01 (View), CT02 (Add) |
| **Interfaces** | Writes to TRANSACT file; reads XREF for card validation |
| **Complexity** | Medium-High |

### FA-6: Transaction Processing (Batch)
| Attribute | Detail |
|---|---|
| **Programs** | `CBTRN02C` (731 LOC -- POSTTRAN), `CBTRN01C` (494 LOC) |
| **Data Stores** | DALYTRAN (sequential), TRANSACT VSAM, XREF, ACCTDAT, TCATBALF |
| **Copybooks** | `CVTRA06Y` (daily tran - 350B), `CVTRA01Y` (cat balance - 50B) |
| **JCL Jobs** | POSTTRAN, COMBTRAN, TRANBKP, TRANFILE, TRANIDX |
| **Control-M** | DAILY-TransactionBackup folder |
| **Interfaces** | Reads daily transaction file, posts to master, updates account balances and category totals; produces reject file |
| **Complexity** | **High** -- core financial posting logic, multi-file updates, error handling with reject file |

### FA-7: Interest Calculation & Billing
| Attribute | Detail |
|---|---|
| **Programs** | `CBACT04C` (652 LOC), `COBIL00C` (572 LOC) |
| **Data Stores** | TCATBALF, XREF, ACCTDAT, DISCGRP, TRANSACT |
| **Copybooks** | `CVTRA01Y`, `CVTRA02Y` (disclosure - 50B), `CVACT01Y` |
| **JCL Jobs** | INTCALC |
| **Control-M** | MONTHLY-InterestCalculation folder |
| **Interfaces** | Reads disclosure groups for interest rates, calculates interest per category balance, updates account |
| **Complexity** | **High** -- financial calculation logic with disclosure group lookups |

### FA-8: Statements & Reporting
| Attribute | Detail |
|---|---|
| **Programs** | `CBSTM03A` (924 LOC), `CBSTM03B` (230 LOC), `CORPT00C` (649 LOC), `CBTRN03C` (649 LOC) |
| **Data Stores** | TRANSACT, XREF, ACCTDAT, CUSTDAT, TRANTYPE, TRANCATG |
| **Copybooks** | `COSTM01`, `CUSTREC`, multiple transaction copybooks |
| **JCL Jobs** | CREASTMT, TRANREPT, REPTFILE |
| **Control-M** | MONTHLY-InterestCalculation (CREASTMT step) |
| **Interfaces** | Produces plain-text and HTML statement output; CORPT00C submits batch via TDQ |
| **Complexity** | Medium-High -- ALTER/GO TO statements, COMP/COMP-3 variables, 2D arrays, subroutine calls |

### FA-9: User Administration
| Attribute | Detail |
|---|---|
| **Programs** | `COUSR00C` (695 LOC), `COUSR01C` (299 LOC), `COUSR02C` (414 LOC), `COUSR03C` (359 LOC) |
| **Data Stores** | USRSEC VSAM KSDS |
| **Copybooks** | `CSUSR01Y`, `COADM02Y` |
| **CICS Transactions** | CU00 (List), CU01 (Add), CU02 (Update), CU03 (Delete) |
| **Interfaces** | Admin-only; CRUD on security file |
| **Complexity** | Medium |

### FA-10: Customer Management
| Attribute | Detail |
|---|---|
| **Programs** | `CBCUS01C` (178 LOC), `CBACT01C` (430 LOC), `CBACT02C` (178 LOC), `CBACT03C` (178 LOC) |
| **Data Stores** | CUSTDAT VSAM KSDS |
| **Copybooks** | `CVCUS01Y` (customer - 500B) |
| **Interfaces** | Read operations for customer data enrichment |
| **Complexity** | Low-Medium |

### FA-11: Branch Migration (Export/Import)
| Attribute | Detail |
|---|---|
| **Programs** | `CBEXPORT` (582 LOC), `CBIMPORT` (487 LOC) |
| **Data Stores** | All master files (CUSTDAT, ACCTDAT, CARDXREF, TRANSACT, CARDDAT) |
| **Copybooks** | `CVEXPORT` (multi-record layout - 500B with REDEFINES, OCCURS, COMP/COMP-3) |
| **JCL Jobs** | CBEXPORT, CBIMPORT |
| **Interfaces** | Multi-record sequential export file with 5 record types |
| **Complexity** | Medium-High -- complex copybook with REDEFINES, OCCURS, COMP/COMP-3 fields |

### FA-12: Authorization Processing (Optional -- IMS/DB2/MQ)
| Attribute | Detail |
|---|---|
| **Programs** | `COPAUA0C` (1,026 LOC), `COPAUS0C` (1,032 LOC), `COPAUS1C` (604 LOC), `COPAUS2C` (244 LOC), `CBPAUP0C` (386 LOC), `PAUDBLOD` (369 LOC), `PAUDBUNL` (317 LOC), `DBUNLDGS` (366 LOC) |
| **Data Stores** | IMS DB (DBPAUTP0, DBPAUTX0), DB2 (AUTHFRDS), VSAM |
| **Copybooks** | Module-specific copybooks, DCL files, IMS DBD/PSB |
| **CICS Transactions** | CPVS (Summary), CPVD (Details), CP00 (Process) |
| **Interfaces** | MQ trigger-based authorization; IMS/DB2 cross-database reads/writes |
| **Complexity** | **Very High** -- spans MQ, IMS, DB2, and VSAM; MQ trigger processing |

### FA-13: Transaction Type Management (Optional -- DB2)
| Attribute | Detail |
|---|---|
| **Programs** | `COTRTLIC` (2,098 LOC), `COTRTUPC` (1,702 LOC), `COBTUPDT` (237 LOC) |
| **Data Stores** | DB2 tables (transaction types/categories) |
| **Copybooks** | Module-specific, DCL files |
| **CICS Transactions** | CTLI (List/Delete), CTTU (Add/Edit) |
| **JCL Jobs** | MNTTRDB2, TRANEXTR |
| **Control-M** | WEEKLY-TransactionTypesDBRefresh, WEEKLY-DisclosureGroupsRefresh |
| **Interfaces** | DB2 cursors, SQL INSERT/UPDATE/DELETE |
| **Complexity** | High -- full DB2 CRUD with cursor-based pagination |

### FA-14: MQ Integration (Optional -- VSAM/MQ)
| Attribute | Detail |
|---|---|
| **Programs** | `CODATE01` (524 LOC), `COACCT01` (620 LOC) |
| **Data Stores** | VSAM files, MQ queues |
| **CICS Transactions** | CDRD (Date Inquiry), CDRA (Account Inquiry) |
| **Interfaces** | MQ request/response pattern |
| **Complexity** | Medium -- demonstrates asynchronous messaging patterns |

---

## 4. Strategy Evaluation per Functional Area

### Evaluation Criteria

| Criterion | Weight | Description |
|---|---|---|
| **Risk** | 30% | Probability of data loss, business disruption, or regression |
| **Effort** | 25% | Development time, testing effort, skill requirements |
| **Business Value** | 25% | Revenue impact, compliance, competitive advantage |
| **Technical Debt Reduction** | 20% | Elimination of mainframe dependencies, maintainability improvement |

### Strategy Definitions

| Strategy | Description | Best For |
|---|---|---|
| **Strangler Fig** | Incrementally replace by routing traffic to new services while legacy runs in parallel | High-risk modules needing zero-downtime migration |
| **Replatform** | Move COBOL to a modern runtime (e.g., Micro Focus, AWS M2) with minimal code changes | Low-complexity modules where COBOL logic is acceptable |
| **Refactor** | Restructure COBOL into cleaner modules, then convert to Java/Spring | Medium-complexity modules with salvageable business logic |
| **Rewrite** | Build from scratch in Java/Spring Boot | Modules where COBOL is too tangled or where modern patterns provide clear advantages |

---

### FA-1: Authentication & User Security

| Strategy | Risk | Effort | Value | Debt Reduction | Score | Verdict |
|---|---|---|---|---|---|---|
| Strangler Fig | Low | Medium | High | High | **82** | |
| Replatform | Low | Low | Low | Low | 45 | |
| Refactor | Low | Medium | Medium | Medium | 62 | |
| **Rewrite** | Low | Medium | **High** | **High** | **85** | **RECOMMENDED** |

**Rationale:** Simple program (260 LOC). Modern authentication (OAuth2/JWT) would replace the USRSEC flat-file model entirely. Rewriting delivers modern security standards with minimal risk.

---

### FA-2: Menu Navigation & Routing

| Strategy | Risk | Effort | Value | Debt Reduction | Score | Verdict |
|---|---|---|---|---|---|---|
| Strangler Fig | Low | Low | Medium | High | 72 | |
| Replatform | Low | Low | Low | Low | 40 | |
| Refactor | Low | Low | Medium | Medium | 60 | |
| **Rewrite** | Low | Low | **High** | **High** | **88** | **RECOMMENDED** |

**Rationale:** BMS-map-driven menu navigation is fundamentally incompatible with modern UIs. Replace with a web-based SPA routing layer (React/Angular). No business logic to preserve.

---

### FA-3: Account Management

| Strategy | Risk | Effort | Value | Debt Reduction | Score | Verdict |
|---|---|---|---|---|---|---|
| **Strangler Fig** | **Medium** | **Medium** | **High** | **High** | **80** | **RECOMMENDED** |
| Replatform | Medium | Low | Low | Low | 42 | |
| Refactor | Medium | High | Medium | Medium | 58 | |
| Rewrite | High | High | High | High | 65 | |

**Rationale:** `COACTUPC` (4,236 LOC) contains critical validation logic and is the largest single program. A strangler approach allows the new Account microservice to run alongside the legacy system, with gradual traffic cutover. The extensive field-level validation logic should be extracted and tested before full replacement.

---

### FA-4: Credit Card Management

| Strategy | Risk | Effort | Value | Debt Reduction | Score | Verdict |
|---|---|---|---|---|---|---|
| **Strangler Fig** | **Medium** | **Medium** | **High** | **High** | **78** | **RECOMMENDED** |
| Replatform | Medium | Low | Low | Low | 42 | |
| Refactor | Medium | High | Medium | Medium | 58 | |
| Rewrite | High | High | High | High | 63 | |

**Rationale:** Three tightly coupled programs (~3,900 LOC total) with multi-file VSAM access patterns. Strangler Fig with an API facade allows incremental extraction while maintaining cross-reference integrity.

---

### FA-5: Transaction Processing (Online)

| Strategy | Risk | Effort | Value | Debt Reduction | Score | Verdict |
|---|---|---|---|---|---|---|
| **Strangler Fig** | **Medium** | **Medium** | **High** | **High** | **80** | **RECOMMENDED** |
| Replatform | Medium | Low | Low | Low | 40 | |
| Refactor | Medium | Medium | Medium | Medium | 60 | |
| Rewrite | High | High | High | High | 62 | |

**Rationale:** Core revenue-generating function. Must maintain zero-downtime during migration. Strangler enables parallel running with transaction-level routing.

---

### FA-6: Transaction Processing (Batch)

| Strategy | Risk | Effort | Value | Debt Reduction | Score | Verdict |
|---|---|---|---|---|---|---|
| Strangler Fig | High | High | High | High | 68 | |
| Replatform | Medium | Low | Medium | Low | 50 | |
| **Refactor** | **Medium** | **Medium** | **High** | **High** | **78** | **RECOMMENDED** |
| Rewrite | High | High | High | High | 65 | |

**Rationale:** POSTTRAN is the financial heart of the system -- posting daily transactions, updating balances, and generating rejects. Refactor preserves the proven business logic while restructuring for Spring Batch. The sequential file processing pattern maps cleanly to Spring Batch's ItemReader/ItemProcessor/ItemWriter model.

---

### FA-7: Interest Calculation & Billing

| Strategy | Risk | Effort | Value | Debt Reduction | Score | Verdict |
|---|---|---|---|---|---|---|
| Strangler Fig | Medium | Medium | High | Medium | 68 | |
| Replatform | Medium | Low | Medium | Low | 48 | |
| **Refactor** | **Medium** | **Medium** | **High** | **High** | **78** | **RECOMMENDED** |
| Rewrite | High | High | High | High | 62 | |

**Rationale:** Complex financial calculations that must produce identical results. Refactor allows line-by-line conversion with decimal-precision parity testing. Monthly batch cadence provides natural testing windows.

---

### FA-8: Statements & Reporting

| Strategy | Risk | Effort | Value | Debt Reduction | Score | Verdict |
|---|---|---|---|---|---|---|
| Strangler Fig | Low | Medium | Medium | Medium | 62 | |
| Replatform | Low | Low | Low | Low | 42 | |
| Refactor | Low | Medium | Medium | Medium | 60 | |
| **Rewrite** | Low | Medium | **High** | **High** | **82** | **RECOMMENDED** |

**Rationale:** Statement generation uses ALTER/GO TO spaghetti code and mainframe-specific control block addressing. Modern reporting frameworks (JasperReports, PDF libraries) would be significantly more maintainable. Low risk because output can be diff-tested against legacy.

---

### FA-9: User Administration

| Strategy | Risk | Effort | Value | Debt Reduction | Score | Verdict |
|---|---|---|---|---|---|---|
| Strangler Fig | Low | Medium | Medium | Medium | 65 | |
| Replatform | Low | Low | Low | Low | 42 | |
| Refactor | Low | Medium | Medium | Medium | 60 | |
| **Rewrite** | Low | Low | **High** | **High** | **85** | **RECOMMENDED** |

**Rationale:** Standard CRUD operations on a simple security file. Modern identity management (RBAC, directory services) would replace entirely. Low complexity (4 programs, ~1,767 LOC total).

---

### FA-10: Customer Management

| Strategy | Risk | Effort | Value | Debt Reduction | Score | Verdict |
|---|---|---|---|---|---|---|
| **Strangler Fig** | Low | Low | **High** | **High** | **82** | **RECOMMENDED** |
| Replatform | Low | Low | Low | Low | 45 | |
| Refactor | Low | Medium | Medium | Medium | 62 | |
| Rewrite | Low | Medium | High | High | 75 | |

**Rationale:** Mostly read-only operations for data enrichment. Low risk for strangler extraction. Customer data is referenced by multiple other domains, making it a natural first candidate for a shared service.

---

### FA-11: Branch Migration (Export/Import)

| Strategy | Risk | Effort | Value | Debt Reduction | Score | Verdict |
|---|---|---|---|---|---|---|
| Strangler Fig | Low | Medium | Medium | Medium | 60 | |
| Replatform | Low | Low | Low | Low | 42 | |
| **Refactor** | Low | **Medium** | **High** | **High** | **78** | **RECOMMENDED** |
| Rewrite | Low | High | Medium | High | 65 | |

**Rationale:** The complex multi-record export layout (CVEXPORT.cpy with REDEFINES, OCCURS, COMP/COMP-3) contains important data transformation logic worth preserving. Refactor into a modern ETL pipeline with the same record mappings.

---

### FA-12: Authorization Processing (Optional)

| Strategy | Risk | Effort | Value | Debt Reduction | Score | Verdict |
|---|---|---|---|---|---|---|
| Strangler Fig | High | High | High | High | 68 | |
| Replatform | Medium | Medium | Medium | Low | 50 | |
| Refactor | High | High | High | Medium | 60 | |
| **Rewrite** | Medium | High | **High** | **High** | **75** | **RECOMMENDED** |

**Rationale:** Spans IMS, DB2, MQ, and VSAM -- the most complex integration surface. IMS hierarchical database has no direct modern equivalent. Rewriting with an event-driven architecture (Kafka/SQS replacing MQ, PostgreSQL replacing IMS/DB2) provides a clean break.

---

### FA-13: Transaction Type Management (Optional)

| Strategy | Risk | Effort | Value | Debt Reduction | Score | Verdict |
|---|---|---|---|---|---|---|
| Strangler Fig | Low | Medium | Medium | Medium | 62 | |
| Replatform | Low | Low | Low | Low | 42 | |
| **Refactor** | Low | **Medium** | **High** | **High** | **80** | **RECOMMENDED** |
| Rewrite | Low | Medium | Medium | High | 68 | |

**Rationale:** Already DB2-based, so SQL logic can be directly ported to JPA/Hibernate. Cursor-based pagination maps to Spring Data's pageable queries. Refactor is the most efficient path.

---

### FA-14: MQ Integration (Optional)

| Strategy | Risk | Effort | Value | Debt Reduction | Score | Verdict |
|---|---|---|---|---|---|---|
| Strangler Fig | Low | Medium | Medium | Medium | 62 | |
| Replatform | Low | Low | Low | Low | 42 | |
| Refactor | Low | Medium | Medium | Medium | 60 | |
| **Rewrite** | Low | Medium | **High** | **High** | **82** | **RECOMMENDED** |

**Rationale:** MQ request/response patterns map naturally to modern messaging (Spring Cloud Stream, Kafka, or Amazon SQS). Rewriting enables modern async patterns (event sourcing, CQRS) that the original MQ integration was approximating.

---

## 5. Strategy Summary Matrix

| Functional Area | LOC | Recommended Strategy | Risk Level | Priority |
|---|---|---|---|---|
| FA-1: Authentication | 260 | **Rewrite** | Low | P1 |
| FA-2: Menu/Routing | 596 | **Rewrite** | Low | P1 |
| FA-9: User Admin | 1,767 | **Rewrite** | Low | P2 |
| FA-10: Customer Mgmt | 964 | **Strangler Fig** | Low | P2 |
| FA-8: Statements/Reports | 2,452 | **Rewrite** | Low | P3 |
| FA-14: MQ Integration | 1,144 | **Rewrite** | Low | P3 |
| FA-11: Branch Migration | 1,069 | **Refactor** | Low | P3 |
| FA-5: Online Transactions | 1,812 | **Strangler Fig** | Medium | P4 |
| FA-4: Credit Card Mgmt | 3,906 | **Strangler Fig** | Medium | P4 |
| FA-3: Account Management | 5,177 | **Strangler Fig** | Medium | P4 |
| FA-13: Tran Type Mgmt | 4,037 | **Refactor** | Medium | P5 |
| FA-7: Interest/Billing | 1,224 | **Refactor** | High | P5 |
| FA-6: Batch Processing | 1,225 | **Refactor** | High | P5 |
| FA-12: Auth Processing | 4,344 | **Rewrite** | High | P6 |

---

## 6. Target Architecture Vision

```
                    ┌─────────────────────────────────┐
                    │         API Gateway / BFF        │
                    │    (Spring Cloud Gateway)        │
                    └──────────┬──────────────────────┘
                               │
          ┌────────────────────┼────────────────────┐
          │                    │                    │
    ┌─────▼─────┐      ┌──────▼──────┐     ┌──────▼──────┐
    │  Auth      │      │  Account    │     │ Transaction │
    │  Service   │      │  Service    │     │ Service     │
    │ (OAuth2)   │      │ (REST API)  │     │ (REST API)  │
    └────────────┘      └─────────────┘     └─────────────┘
                               │                    │
    ┌────────────┐      ┌──────▼──────┐     ┌──────▼──────┐
    │  Customer  │      │  Card       │     │ Billing     │
    │  Service   │      │  Service    │     │ Service     │
    │ (REST API) │      │ (REST API)  │     │ (Spring     │
    └────────────┘      └─────────────┘     │  Batch)     │
                                            └─────────────┘
    ┌────────────┐      ┌─────────────┐     ┌─────────────┐
    │  Report    │      │  Migration  │     │ Event Bus   │
    │  Service   │      │  Service    │     │ (Kafka/SQS) │
    └────────────┘      └─────────────┘     └─────────────┘
```

### Technology Stack Recommendation

| Layer | Technology |
|---|---|
| Language | Java 21 (LTS) |
| Framework | Spring Boot 3.x, Spring Batch |
| API | REST + OpenAPI 3.0 |
| Database | PostgreSQL (replacing VSAM/DB2) |
| Messaging | Apache Kafka or Amazon SQS (replacing IBM MQ) |
| Batch Orchestration | Spring Batch + Kubernetes CronJob (replacing Control-M) |
| Authentication | Spring Security + OAuth2/OIDC |
| Frontend | React or Angular SPA (replacing BMS/3270) |
| Monitoring | Prometheus + Grafana, ELK Stack |
| CI/CD | GitHub Actions, ArgoCD |
| Infrastructure | Kubernetes on AWS EKS |
