# CardDemo Modernization Blueprint

## Executive Summary

This document evaluates four modernization strategies — **Strangler Fig**, **Replatform**, **Refactor**, and **Rewrite** — for each functional area of the CardDemo mainframe credit card management system. The assessment considers business risk, technical complexity, data coupling, and organizational readiness to recommend a per-domain strategy that minimizes disruption while maximizing long-term value.

---

## System Overview

| Metric | Value |
|--------|-------|
| Total COBOL LOC (core) | ~20,650 |
| Total COBOL LOC (extensions) | ~9,525 |
| Online programs (CICS) | 22 |
| Batch programs | 12 |
| Copybooks (data structures) | 30 |
| JCL jobs | 35+ |
| VSAM datasets | 12 |
| Optional DB2 tables | 3+ |
| IMS databases | 2 |
| MQ queues | 4 |

---

## Strategy Definitions

| Strategy | Description | Best When |
|----------|-------------|-----------|
| **Strangler Fig** | Incrementally route traffic to new services while legacy remains operational; legacy is decommissioned module-by-module | Low-risk, self-contained modules with clear API boundaries |
| **Replatform** | Move COBOL to a modern runtime (e.g., Micro Focus, UniKix) with minimal code changes; containerize and cloud-host | Time-to-value is critical; business logic is stable and well-tested |
| **Refactor** | Restructure COBOL into cleaner modular code, then transpile or port to Java/Spring; preserve business logic semantics | Complex business rules that must be preserved exactly |
| **Rewrite** | Build greenfield services from domain requirements; old code used only as reference | Legacy code is unmaintainable, poorly structured, or requirements have fundamentally changed |

---

## Functional Area Assessment

### 1. Identity & Access (Sign-on / User Management)

**Programs:** `COSGN00C`, `COUSR00C`, `COUSR01C`, `COUSR02C`, `COUSR03C`  
**Data:** `CSUSR01Y` (USRSEC VSAM KSDS, 80-byte records)  
**LOC:** ~1,800

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler Fig | ⭐⭐⭐⭐⭐ | Authentication is a natural extraction seam — clearly bounded, no shared mutable state with other domains |
| Replatform | ⭐⭐ | Rehosting RACF-simulated auth adds no value; modern identity providers (OAuth2/OIDC) are superior |
| Refactor | ⭐⭐⭐ | Simple enough to refactor, but the domain model (8-byte passwords, no hashing) is architecturally obsolete |
| Rewrite | ⭐⭐⭐⭐ | Modern security standards demand a rewrite regardless; Spring Security + JWT is the clear target |

**Recommendation:** **Strangler Fig → Rewrite**  
Deploy a Spring Security / OAuth2 service behind an API gateway. Route sign-on requests to the new service while the legacy USRSEC file continues to serve downstream programs during transition via a sync adapter.

---

### 2. Account Management

**Programs:** `COACTVWC` (view), `COACTUPC` (update, 4,236 LOC — largest single program)  
**Data:** `CVACT01Y` (Account VSAM KSDS, 300-byte records)  
**LOC:** ~5,100

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler Fig | ⭐⭐⭐⭐ | Read path (view) is easily extracted; write path has coupling to transaction posting and interest calc |
| Replatform | ⭐⭐⭐ | Complex `COACTUPC` (4,236 LOC) is heavily procedural with embedded screen I/O — replatform preserves it but perpetuates tech debt |
| Refactor | ⭐⭐⭐⭐ | Business rules (credit limits, balance updates, status transitions) are well-defined and suitable for extraction into a domain service |
| Rewrite | ⭐⭐⭐ | Risk of regression is high given the program complexity; rewrite requires exhaustive test coverage first |

**Recommendation:** **Refactor**  
Extract business rules from `COACTUPC` into a Java domain model (Account entity with JPA). Separate the CICS screen logic from the domain logic layer-by-layer. Preserve exact balance/limit arithmetic to avoid financial discrepancies.

---

### 3. Card Management

**Programs:** `COCRDLIC` (list, 1,459 LOC), `COCRDSLC` (detail view), `COCRDUPC` (update, 1,560 LOC)  
**Data:** `CVACT02Y` (Card VSAM KSDS, 150-byte records), `CVACT03Y` (XREF, 50-byte)  
**LOC:** ~3,400

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler Fig | ⭐⭐⭐⭐⭐ | Card CRUD is self-contained; XREF lookup is the only cross-domain dependency and can be replicated via a shared lookup service |
| Replatform | ⭐⭐⭐ | Moderate complexity; replatform is viable but doesn't address the VSAM-indexed access pattern |
| Refactor | ⭐⭐⭐⭐ | Well-structured programs with clear field-level validation; good candidate for systematic port |
| Rewrite | ⭐⭐⭐ | Relatively clean domain — rewrite possible but not required |

**Recommendation:** **Strangler Fig**  
Build a Card Service (Spring Boot + JPA) exposing REST APIs. Use the XREF table as the integration seam — the new service maintains its own XREF projection. Route CICS card transactions (CCLI, CCDL, CCUP) to the new service via an API gateway shim.

---

### 4. Customer Management

**Programs:** `CBCUS01C` (batch load)  
**Data:** `CVCUS01Y` (Customer VSAM KSDS, 500-byte records)  
**LOC:** ~450

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler Fig | ⭐⭐⭐⭐⭐ | Customer data is consumed by many modules but written rarely — classic read-heavy domain ideal for extraction |
| Replatform | ⭐⭐⭐ | Simple batch load; replatform adds little value |
| Refactor | ⭐⭐⭐⭐ | Straightforward data structure maps directly to a JPA entity |
| Rewrite | ⭐⭐⭐⭐ | Minimal logic — a rewrite is essentially trivial |

**Recommendation:** **Strangler Fig → Rewrite**  
Build a Customer Service with a PostgreSQL-backed model. Sync from VSAM via CDC during transition. This becomes the system of record; downstream consumers (Account, Statement) query it via API.

---

### 5. Transaction Processing (Online)

**Programs:** `COTRN00C` (list), `COTRN01C` (view), `COTRN02C` (add)  
**Data:** `CVTRA05Y` (Transaction VSAM KSDS, 350-byte records), `CVTRA03Y` (types), `CVTRA04Y` (categories)  
**LOC:** ~2,200

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler Fig | ⭐⭐⭐⭐ | Online transaction entry is isolatable; however, it writes to the same file as the batch posting engine |
| Replatform | ⭐⭐⭐ | Viable for short-term coexistence |
| Refactor | ⭐⭐⭐⭐ | Transaction-entry logic is straightforward CRUD; the real complexity is in posting (batch) |
| Rewrite | ⭐⭐⭐ | Low risk if batch posting is handled separately |

**Recommendation:** **Strangler Fig**  
New Transaction Entry service writes to a modern event store (e.g., Kafka topic or PostgreSQL outbox). A bridge adapter writes records back to the VSAM TRANSACT file for the legacy batch posting engine during coexistence.

---

### 6. Transaction Posting & Interest Calculation (Batch)

**Programs:** `CBTRN02C` (posting, 731 LOC), `CBACT04C` (interest calc, 652 LOC), `CBTRN01C`, `CBTRN03C`  
**Data:** Multiple VSAM files (DALYTRAN, TRANSACT, TCATBALF, DISCGRP, ACCTFILE, XREFFILE)  
**LOC:** ~2,600

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler Fig | ⭐⭐ | Batch posting touches 6+ files atomically — difficult to incrementally replace without a data seam |
| Replatform | ⭐⭐⭐⭐ | Well-contained batch jobs; replatform on UniKix/Micro Focus allows rapid cloud migration while preserving exact arithmetic |
| Refactor | ⭐⭐⭐⭐⭐ | Financial logic (posting, interest, category balances) must be preserved exactly — systematic refactoring into a Spring Batch service with comprehensive parity tests is the safest approach |
| Rewrite | ⭐⭐ | Financial calculation bugs can lead to regulatory issues; rewrite is high-risk |

**Recommendation:** **Refactor**  
Port `CBTRN02C` and `CBACT04C` to Spring Batch jobs. Implement decimal-exact arithmetic (Java `BigDecimal`). Run dual-write parity testing (legacy batch vs. new batch) for a minimum of 3 billing cycles before cutover.

---

### 7. Statement & Reporting

**Programs:** `CBSTM03A` (statement generation, 924 LOC), `CBSTM03B` (subroutine), `CORPT00C` (online report), `CBTRN03C`  
**Data:** Reads from TRANSACT, ACCTFILE, CUSTFILE, XREFFILE  
**LOC:** ~1,500

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler Fig | ⭐⭐⭐⭐⭐ | Pure read-only downstream consumer; zero write-back to source files |
| Replatform | ⭐⭐⭐ | Works but perpetuates COMP/COMP-3 byte manipulation patterns |
| Refactor | ⭐⭐⭐ | Statement template generation is straightforward but the code uses ALTER/GO TO and mainframe control-block addressing (intentionally complex) |
| Rewrite | ⭐⭐⭐⭐⭐ | Reporting is a solved problem (JasperReports, Apache PDFBox); rewrite delivers modern HTML/PDF output |

**Recommendation:** **Strangler Fig → Rewrite**  
Build a Statement Service that queries the modernized Transaction and Account services via APIs. Generate statements in HTML/PDF using modern templating. Can run in parallel with legacy statements for validation.

---

### 8. Bill Payment

**Programs:** `COBIL00C`  
**Data:** Writes to TRANSACT and updates ACCTFILE  
**LOC:** ~600

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler Fig | ⭐⭐⭐⭐ | Single-program module; writes must be coordinated with account and transaction domains |
| Replatform | ⭐⭐⭐ | Simple enough to rehost |
| Refactor | ⭐⭐⭐⭐ | Clean business logic that maps to a Payment domain command |
| Rewrite | ⭐⭐⭐⭐ | Straightforward domain; modern payment rails often require a fresh design anyway |

**Recommendation:** **Strangler Fig**  
Implement as a Payment Service that orchestrates Account debit + Transaction creation via the modernized Account and Transaction services. Ensures transactional consistency via Saga pattern or outbox.

---

### 9. Authorization Processing (IMS/DB2/MQ Extension)

**Programs:** `COPAUA0C` (MQ processor, 1,026 LOC), `COPAUS0C`/`COPAUS1C` (CICS screens), `CBPAUP0C` (batch purge)  
**Data:** IMS HIDAM database, DB2 fraud table, MQ queues, VSAM  
**LOC:** ~4,500

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler Fig | ⭐⭐⭐⭐⭐ | MQ-driven architecture already provides a natural service boundary; the MQ interface IS the API contract |
| Replatform | ⭐⭐ | IMS DB is the primary complexity — replatforming IMS adds no modernization value |
| Refactor | ⭐⭐⭐ | Complex multi-resource transactions (IMS + DB2 + MQ) make systematic refactoring challenging |
| Rewrite | ⭐⭐⭐⭐ | Modern event-driven authorization (Kafka + PostgreSQL) is well-understood; IMS hierarchical model maps to document store or relational joins |

**Recommendation:** **Strangler Fig → Rewrite**  
Build an Authorization Service consuming from the same MQ queues (or Kafka). Replace IMS hierarchical storage with PostgreSQL (or DynamoDB for the tree structure). Replace DB2 fraud table with a dedicated Fraud Analytics service. The existing MQ message format becomes the API contract during migration.

---

### 10. Reference Data Management (DB2 Extension)

**Programs:** `COTRTLIC` (list, 2,098 LOC), `COTRTUPC` (add/edit, 1,702 LOC), `COBTUPDT` (batch maintain)  
**Data:** DB2 tables (transaction types/categories)  
**LOC:** ~4,200

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler Fig | ⭐⭐⭐⭐⭐ | Reference data is consumed by many modules but owned by this one — classic strangler candidate |
| Replatform | ⭐⭐⭐ | DB2 → PostgreSQL is a straightforward port |
| Refactor | ⭐⭐⭐⭐ | DB2 SQL already maps cleanly to JPA/JDBC |
| Rewrite | ⭐⭐⭐⭐ | Simple CRUD domain; modern admin UIs are trivial to build |

**Recommendation:** **Strangler Fig → Rewrite**  
Build a Reference Data Service (Spring Boot + JPA) exposing REST CRUD. Migrate DB2 data to PostgreSQL. Publish change events for downstream consumers. Legacy programs read from a sync'd VSAM extract during transition.

---

### 11. Branch Migration (Export/Import)

**Programs:** `CBEXPORT` (export), `CBIMPORT` (import)  
**Data:** Multi-record export file format, reads/writes all master files  
**LOC:** ~1,300

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler Fig | ⭐⭐⭐ | Can be replaced once individual domain services expose APIs |
| Replatform | ⭐⭐⭐⭐ | If branch migration is still needed, replatform maintains compatibility |
| Refactor | ⭐⭐⭐ | Batch file I/O logic is straightforward but the multi-record format is fragile |
| Rewrite | ⭐⭐⭐⭐⭐ | Modern ETL tools (Spring Batch, Spark) handle this better; REST APIs eliminate the need for file-based transfer |

**Recommendation:** **Rewrite (deferred)**  
Once domain services are available with APIs, branch migration becomes API-to-API data sync or a standard ETL job. Deprioritize this module — keep it running on the legacy platform until the domains it depends on are modernized.

---

### 12. MQ Integration (VSAM-MQ Extension)

**Programs:** `CODATE01` (system date inquiry), `COACCT01` (account inquiry)  
**Data:** MQ request/response queues  
**LOC:** ~450

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler Fig | ⭐⭐⭐⭐⭐ | MQ adapters are natural integration points; replace with REST/gRPC as services come online |
| Replatform | ⭐⭐⭐ | Viable short-term |
| Refactor | ⭐⭐⭐ | Minimal logic to refactor |
| Rewrite | ⭐⭐⭐⭐⭐ | Replace MQ request/response with synchronous REST calls to Account and Platform services |

**Recommendation:** **Strangler Fig → Rewrite**  
Once the Account Service is live, the MQ adapter becomes a thin translation layer (MQ message → REST call → MQ response). Eventually retire the MQ interface entirely as consumers migrate to direct REST/gRPC.

---

## Strategy Summary Matrix

| Functional Area | Primary Strategy | Target Architecture | Risk | Effort |
|----------------|-----------------|--------------------:|:----:|:------:|
| Identity & Access | Strangler → Rewrite | Spring Security + OAuth2 | Low | S |
| Account Management | Refactor | Spring Boot + JPA | Med | L |
| Card Management | Strangler Fig | Spring Boot REST | Low | M |
| Customer Management | Strangler → Rewrite | Spring Boot + PostgreSQL | Low | S |
| Transaction (Online) | Strangler Fig | Event-driven service | Med | M |
| Transaction (Batch) | Refactor | Spring Batch + BigDecimal | High | XL |
| Statement & Reporting | Strangler → Rewrite | Templating engine | Low | M |
| Bill Payment | Strangler Fig | Payment orchestration | Med | M |
| Authorization (IMS/MQ) | Strangler → Rewrite | Event-driven + PostgreSQL | Med | L |
| Reference Data (DB2) | Strangler → Rewrite | Spring Boot CRUD | Low | S |
| Branch Migration | Rewrite (deferred) | ETL/API sync | Low | S |
| MQ Integration | Strangler → Rewrite | REST adapter | Low | S |

**Effort Key:** S = < 2 weeks, M = 2-6 weeks, L = 6-12 weeks, XL = 12+ weeks (per team)

---

## Target Architecture Vision

```
┌─────────────────────────────────────────────────────────────────┐
│                        API Gateway                              │
│               (Authentication, Rate Limiting, Routing)          │
├────────┬──────────┬──────────┬──────────┬──────────┬───────────┤
│ Auth   │ Customer │ Account  │ Card     │ Txn      │ Payment   │
│Service │ Service  │ Service  │ Service  │ Service  │ Service   │
│(OAuth2)│ (CRUD)   │ (Domain) │ (CRUD)   │ (Event)  │ (Saga)    │
├────────┴──────────┴──────────┴──────────┴──────────┴───────────┤
│                     Event Bus (Kafka)                           │
├────────┬──────────┬──────────┬──────────┬──────────┬───────────┤
│ Auth   │Statement │ Interest │ Posting  │ RefData  │ Fraud     │
│ Proc   │ Service  │ Calc Job │ Batch    │ Service  │ Analytics │
└────────┴──────────┴──────────┴──────────┴──────────┴───────────┘
                              │
                    ┌─────────┴─────────┐
                    │   PostgreSQL /    │
                    │   Document Store  │
                    └───────────────────┘
```

---

## Key Principles

1. **Data Ownership First**: Each bounded context owns its data store. Cross-domain queries go through APIs, never shared databases.
2. **Decimal Exactness**: All financial calculations use `BigDecimal` or equivalent fixed-point arithmetic. No floating-point for monetary values.
3. **Dual-Run Validation**: Critical batch processes (posting, interest) run in parallel with legacy for at least 3 billing cycles.
4. **Event-Driven Integration**: Replace VSAM file coupling with domain events. Services publish state changes; consumers subscribe.
5. **Incremental Delivery**: Each functional area delivers independently. No "big bang" cutover.
