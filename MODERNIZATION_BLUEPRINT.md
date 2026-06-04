# Modernization Blueprint

## Executive Summary

This document evaluates four modernization strategies — **Strangler Fig**, **Replatform**, **Refactor**, and **Rewrite** — for each functional area of the CardDemo mainframe credit card management system. The system comprises ~20,650 lines of COBOL across 33 programs, with CICS online transactions, VSAM/DB2/IMS data stores, batch JCL workflows, and Control-M orchestration.

---

## System Inventory Overview

| Layer | Artifact Count | LOC | Technologies |
|-------|---------------|-----|--------------|
| Online (CICS) | 19 programs | ~14,200 | COBOL, CICS, BMS Maps, VSAM |
| Batch | 12 programs | ~5,500 | COBOL, JCL, Sequential files |
| Copybooks | 30 files | ~600 | Shared record layouts |
| BMS Maps | 17 maps | — | 3270 terminal UI |
| JCL | 40 jobs | — | Batch orchestration |
| Scheduler | 2 definitions | — | Control-M, CA-7 |
| Data | VSAM KSDS, AIX, DB2, IMS | — | EBCDIC/ASCII |

---

## Functional Areas

### 1. Authentication & User Management

**Programs:** `COSGN00C` (sign-on), `COUSR00C` (list users), `COUSR01C/02C/03C` (user CRUD)
**Data:** USRSEC VSAM file (SEC-USER-DATA: ID, name, password, type)
**Complexity:** Low-Medium (~1,900 LOC total)

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Strangler Fig** | **Recommended** | Clean API boundary — authentication is an entry-point; can be intercepted with a gateway. New auth service (Spring Security / OAuth2) fronts legacy until all callers migrate. |
| Replatform | Moderate | Could lift VSAM user file to a relational table with minimal logic change, but gains little without also modernizing the auth protocol. |
| Refactor | Low | RACF-simulated security has no modern equivalent worth preserving structurally. |
| Rewrite | High fit | Simple domain (< 700 LOC core logic). A fresh JWT/OAuth2 microservice is straightforward. Risk: must maintain backward compat for COMMAREA callers during transition. |

---

### 2. Account Management

**Programs:** `COACTUPC` (update, 4,236 LOC), `COACTVWC` (view), `CBACT01C–04C` (batch reads/writes)
**Data:** ACCTDAT VSAM KSDS (ACCOUNT-RECORD: 300-byte, 16 fields including balance, limits, dates)
**Complexity:** High — `COACTUPC` alone is the largest program with complex edit validation.

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Strangler Fig** | **Recommended** | Expose account operations as REST/gRPC services behind an anti-corruption layer. Existing VSAM reads continue while new service takes over writes incrementally. |
| Replatform | Moderate | AWS Mainframe Modernization (Blu Age / Micro Focus) can rehost, but the 4,236-LOC update program needs semantic understanding, not just lifting. |
| Refactor | Moderate | Extract validation logic into a domain service; field-level edits map well to Bean Validation in Java. But 88-level conditions and COMP-3 arithmetic need careful translation. |
| Rewrite | High risk | Business rules are embedded in 4K+ lines of procedural code; rewrite risks losing edge-case behavior without exhaustive test coverage first. |

---

### 3. Credit Card Management

**Programs:** `COCRDLIC` (list cards, 1,459 LOC), `COCRDSLC` (card detail), `COCRDUPC` (card update, 1,560 LOC)
**Data:** CARDDAT VSAM KSDS, CARDAIX (alternate index by account), CCXREF (card-to-account cross-reference)
**Complexity:** Medium-High — alternate index browsing, admin vs. user access control.

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Strangler Fig** | **Recommended** | Card operations are accessed via well-defined CICS transactions (CC00 prefix). An API gateway can intercept and route to a new Card Service. |
| Replatform | Low | AIX path browsing and VSAM KSDS semantics don't translate cleanly to rehosted environments without index redesign. |
| Refactor | Moderate | Role-based access logic (admin vs. user) can be extracted into a policy engine. Data access patterns suit JPA/Spring Data. |
| Rewrite | Moderate | Bounded scope (~3K LOC). Card lifecycle is well-understood. Risk is in XREF integrity during cutover. |

---

### 4. Transaction Processing

**Programs:** `COTRN00C` (list, 699 LOC), `COTRN01C` (detail view), `COTRN02C` (add transaction), `CBTRN01C` (post daily, 494 LOC), `CBTRN02C` (post with categorization, 731 LOC), `CBTRN03C` (report, 649 LOC)
**Data:** TRANSACT VSAM KSDS (TRAN-RECORD: 350-byte), DALYTRAN sequential file, TCATBALF (category balances)
**Complexity:** High — this is the core financial processing pipeline with daily batch posting.

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Strangler Fig** | **Recommended** | Introduce event-driven transaction capture (Kafka/SQS) alongside legacy VSAM writes. New consumers process in parallel; once validated, legacy posting is retired. |
| Replatform | Low | Batch posting semantics (file close → process → reopen) are tightly coupled to CICS file control; rehosting preserves but doesn't improve. |
| Refactor | Moderate | Category balance accumulation and daily posting logic are candidates for a domain event model. Requires careful state reconciliation. |
| Rewrite | High risk | Financial calculations must be bit-for-bit accurate (COMP-3 packed decimal). Rewrite requires parallel-run validation infrastructure. |

---

### 5. Billing & Payments

**Programs:** `COBIL00C` (bill payment, 572 LOC)
**Data:** Writes to TRANSACT, updates ACCTDAT balance
**Complexity:** Medium — creates a payment transaction and updates account balance atomically.

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Strangler Fig** | Moderate | Payment is a single entry point but has cross-aggregate side effects (transaction + account). Need saga/choreography. |
| Replatform | Low | Simple enough that rehosting adds no value. |
| **Refactor** | **Recommended** | Extract payment as a command in the Transaction domain. Reuse the new Account and Transaction services with a orchestrated workflow. |
| Rewrite | Low risk | Only 572 LOC; straightforward to rewrite once Account and Transaction services exist. |

---

### 6. Reporting & Statements

**Programs:** `CORPT00C` (submit report job, 649 LOC), `CBTRN03C` (transaction detail report), `CBSTM03A` (account statements, 924 LOC), `CBSTM03B` (statement variant, 230 LOC)
**Data:** Reads TRANSACT, writes to print/HTML output via TDQ/spool
**Complexity:** Medium — statement generation uses mainframe control block addressing and complex formatting.

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler Fig | Moderate | Reports can be generated from new services once data is migrated; legacy reporting can run in parallel. |
| Replatform | Low | Print spool and TDQ submission patterns are mainframe-specific; little value in lifting them. |
| Refactor | Low | Statement formatting logic (HTML generation in COBOL) is better discarded than preserved. |
| **Rewrite** | **Recommended** | Replace with modern reporting (JasperReports, or a reporting microservice). Statement generation is output-only with no upstream dependencies. Easiest to validate via output comparison. |

---

### 7. Batch Operations & Data Maintenance

**Programs:** `CBACT01C–04C` (account file ops), `CBCUS01C` (customer read), batch JCL for file management (CLOSEFIL, OPENFIL, TRANBKP, POSTTRAN, INTCALC, COMBTRAN, DISCGRP)
**Data:** All VSAM files, sequential intermediaries
**Complexity:** Medium — file open/close/backup patterns, interest calculation, transaction consolidation.

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Strangler Fig** | **Recommended** | Replace batch jobs incrementally with scheduled microservices or cloud-native batch (Spring Batch / AWS Step Functions). Each job is independent enough to migrate individually. |
| Replatform | Moderate | Control-M → cloud scheduler (AWS EventBridge / Airflow) is a natural replatform target for orchestration. |
| Refactor | Low | Batch file patterns (sequential reads, IDCAMS operations) have no modern structural equivalent worth preserving. |
| Rewrite | Moderate | Interest calculation and transaction consolidation have clear business rules suitable for rewrite, but need parallel-run validation. |

---

### 8. Branch Migration (Export/Import)

**Programs:** `CBEXPORT` (582 LOC), `CBIMPORT` (487 LOC)
**Data:** Multi-record export file (Customer, Account, Card-XREF, Transaction)
**Complexity:** Medium — ETL-style multi-record layout parsing and generation.

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler Fig | Low | These are utility programs, not ongoing services. |
| Replatform | Low | No value in lifting ETL utilities. |
| Refactor | Low | Record layout parsing is not worth preserving. |
| **Rewrite** | **Recommended** | Replace with a modern ETL/data pipeline (Spring Batch, Apache Spark, or simple file-based microservice). The multi-record format can be replaced with JSON/CSV. Low risk — utility function with clear I/O contract. |

---

### 9. Administration & Navigation

**Programs:** `COADM01C` (admin menu, 288 LOC), `COMEN01C` (user menu, 308 LOC)
**Data:** None (navigation/routing only via COMMAREA)
**Complexity:** Low — menu dispatch based on user type.

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler Fig | N/A | Menus disappear when the UI is replaced. |
| Replatform | N/A | BMS maps have no target equivalent. |
| Refactor | N/A | No business logic to preserve. |
| **Rewrite** | **Recommended** | Replace with a modern SPA/web frontend (React/Angular). Navigation logic is trivial. Should be built last, after backend services are available. |

---

### 10. MQ Integration & Authorization (Optional Module)

**Programs:** Authorization via IMS/DB2/MQ (`app/app-authorization-ims-db2-mq/`)
**Data:** IMS hierarchical DB, DB2 tables, MQ queues
**Complexity:** High — asynchronous authorization flow with multiple middleware dependencies.

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Strangler Fig** | **Recommended** | MQ-based authorization is already asynchronous — ideal for event-driven replacement. New auth service subscribes to same queue initially, then migrates to cloud-native messaging (SQS/Kafka). |
| Replatform | Moderate | MQ → Amazon MQ/MSK is a direct replatform path for the messaging layer. |
| Refactor | Low | IMS DB access patterns don't refactor cleanly. |
| Rewrite | Moderate | Authorization rules are well-defined; risk is in the IMS data model migration. |

---

## Strategy Summary Matrix

| Functional Area | Recommended Strategy | Complexity | Priority |
|----------------|---------------------|------------|----------|
| Authentication & User Mgmt | Strangler Fig | Low | Phase 1 |
| Reporting & Statements | Rewrite | Medium | Phase 1 |
| Branch Migration (ETL) | Rewrite | Medium | Phase 1 |
| Administration & Navigation | Rewrite (new UI) | Low | Phase 2 |
| Billing & Payments | Refactor | Medium | Phase 2 |
| Credit Card Management | Strangler Fig | Medium-High | Phase 3 |
| Account Management | Strangler Fig | High | Phase 3 |
| Batch Operations | Strangler Fig | Medium | Phase 3 |
| Transaction Processing | Strangler Fig | High | Phase 4 |
| MQ/Authorization Module | Strangler Fig | High | Phase 4 |

---

## Target Architecture Vision

```
┌─────────────────────────────────────────────────────────────┐
│                    API Gateway / BFF                         │
├─────────┬──────────┬───────────┬──────────┬─────────────────┤
│  Auth   │ Account  │   Card    │  Txn     │  Reporting      │
│ Service │ Service  │  Service  │ Service  │  Service        │
├─────────┴──────────┴───────────┴──────────┴─────────────────┤
│              Event Bus (Kafka / SQS)                         │
├─────────────────────────────────────────────────────────────┤
│         Relational DB (PostgreSQL / Aurora)                  │
│         + Object Storage (S3 for statements)                │
├─────────────────────────────────────────────────────────────┤
│    Cloud Batch Scheduler (Step Functions / Spring Batch)     │
└─────────────────────────────────────────────────────────────┘
          ↕ Anti-Corruption Layer ↕
┌─────────────────────────────────────────────────────────────┐
│              Legacy Mainframe (CICS/VSAM/DB2)               │
└─────────────────────────────────────────────────────────────┘
```

---

## Key Principles

1. **Strangler Fig as default** — most functional areas have clean CICS transaction boundaries that serve as natural interception points.
2. **Rewrite only for leaf nodes** — reporting, ETL, and UI have no upstream consumers and clear output contracts.
3. **Parallel-run validation mandatory** — for any financial calculation (interest, balances, posting), run old and new in parallel and compare results before cutover.
4. **Data migration drives timeline** — VSAM → relational migration is the critical path; all service rewrites depend on it.
5. **Preserve COMMAREA contracts** — during transition, new services must speak the legacy protocol via an anti-corruption layer until all consumers are migrated.
