# CardDemo Modernization Blueprint

## 1. Executive Summary

CardDemo is a COBOL/CICS mainframe credit card management system comprising **29 core COBOL programs**, **29 copybooks**, **38 JCL jobs**, and **5 optional-module programs** (IMS/DB2/MQ authorization extension). The system spans online transaction processing (CICS 3270 terminals), batch processing (JCL/Control-M), and integrations with VSAM, DB2, IMS DB, and IBM MQ.

This blueprint evaluates four modernization strategies — **Strangler Fig**, **Replatform**, **Refactor**, and **Rewrite** — for each of the six identified functional areas, then provides a recommended approach per area.

---

## 2. Functional Area Inventory

| # | Functional Area | Online Programs | Batch Programs | Key Data Stores | LOC (approx) |
|---|----------------|-----------------|----------------|-----------------|--------------|
| 1 | **Authentication & User Management** | COSGN00C, COADM01C, COUSR00C, COUSR01C, COUSR02C, COUSR03C | DUSRSECJ (IEBGENER) | USRSEC (VSAM KSDS) | ~2,330 |
| 2 | **Account Management** | COACTVWC, COACTUPC | CBACT01C, CBACT04C (interest calc) | ACCTDATA (VSAM KSDS), DISCGRP, TCATBALF | ~6,260 |
| 3 | **Credit Card Management** | COCRDLIC, COCRDSLC, COCRDUPC | CBACT02C, CBACT03C | CARDDATA (VSAM KSDS), CARDXREF (VSAM KSDS+AIX) | ~4,260 |
| 4 | **Transaction Processing** | COTRN00C, COTRN01C, COTRN02C | CBTRN01C, CBTRN02C (post-tran), CBTRN03C (reports) | TRANSACT (VSAM KSDS+AIX), DALYTRAN, TRANCATG, TRANTYPE | ~4,440 |
| 5 | **Billing, Statements & Reporting** | COBIL00C, CORPT00C | CREASTMT (CBSTM03A), COMBTRAN (SORT), INTCALC | TRANSACT, ACCTDATA | ~1,870 |
| 6 | **Authorization & Fraud (Optional)** | COPAUS0C, COPAUS1C, COPAUS2C, COPAUA0C | CBPAUP0C | AUTHFRDS (DB2), IMS DB (DBPAUTP0/DBPAUTX0), MQ queues | ~2,500+ |

**Cross-cutting concerns:** Branch migration export/import (CBEXPORT, CBIMPORT — 1,069 LOC), date utility (CSUTLDTC — 157 LOC), wait utility (COBSWAIT — 41 LOC), COMMAREA routing (COCOM01Y copybook), menu navigation (COMEN01C, COMEN02Y).

---

## 3. Strategy Evaluation Matrix

### 3.1 Authentication & User Management

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Strangler Fig** | ★★★★☆ | Auth is a natural seam — new REST/JWT auth can run alongside USRSEC VSAM reads. Dual-write to USRSEC during transition preserves existing CICS sign-on. |
| **Replatform** | ★★★☆☆ | COBOL→Java auto-translation preserves USRSEC flat-file model, but the 80-byte fixed-length user record with plaintext passwords needs redesign regardless. |
| **Refactor** | ★★★★☆ | Small codebase (~2,330 LOC). Extract user CRUD into a clean Java service with proper password hashing, RBAC, and a relational user store. |
| **Rewrite** | ★★★★★ | **Recommended.** Auth is the foundation — modern JWT/OAuth2 eliminates RACF-simulated auth. Only 6 programs, simple VSAM I/O, no complex business logic. Plaintext passwords and 8-char limits demand a clean break. |

**Recommendation: Rewrite** — Replace with a Spring Security / JWT-based auth microservice. Migrate USRSEC records to a relational user table with bcrypt-hashed passwords.

---

### 3.2 Account Management

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Strangler Fig** | ★★★★★ | **Recommended.** Account View (COACTVWC, 941 LOC) can be replaced first with a read-only REST API backed by the same VSAM data (via adapter). Account Update (COACTUPC, 4,236 LOC) follows once read path is proven. |
| **Replatform** | ★★★☆☆ | COACTUPC is the largest single program (4,236 LOC) with dense input-validation logic (phone, date, zip, signed numbers). Auto-translation would preserve this complexity without simplifying it. |
| **Refactor** | ★★★★☆ | Extract validation logic into a shared Java validation library. Refactor the monolithic COACTUPC into separate read/validate/update concerns. |
| **Rewrite** | ★★★☆☆ | High risk — COACTUPC contains complex field-by-field edit logic across 40+ screen fields. Rewrite risks missing edge cases. |

**Recommendation: Strangler Fig** — Wrap VSAM account data with a REST API. Migrate Account View first (read-only, low risk), then Account Update with thorough test coverage of the 40+ validation rules.

---

### 3.3 Credit Card Management

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Strangler Fig** | ★★★★★ | **Recommended.** Card List → Card View → Card Update is a natural read-before-write migration sequence. CARDXREF cross-reference provides a clean join point between Card and Account domains. |
| **Replatform** | ★★★☆☆ | VSAM KSDS + AIX indexed access patterns translate cleanly to relational DB indexes, but BMS map handling and CICS browse commands need manual intervention. |
| **Refactor** | ★★★★☆ | Card data structures (CVACT02Y: 150 bytes, CVACT03Y: 50 bytes) are simple. Refactor into JPA entities with proper relationships. |
| **Rewrite** | ★★★☆☆ | Card management has tight coupling to Account domain via XREF. A rewrite needs the Account service in place first. |

**Recommendation: Strangler Fig** — Expose card operations as REST endpoints behind an API gateway. The CARDXREF cross-reference becomes a foreign-key relationship in the new relational model.

---

### 3.4 Transaction Processing

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Strangler Fig** | ★★★☆☆ | Online transaction entry (COTRN02C) creates records that batch posting (CBTRN02C) later processes. Splitting this cross-channel flow requires careful synchronization. |
| **Replatform** | ★★★★☆ | **Recommended for batch.** CBTRN02C (731 LOC) follows a sequential file-read → validate → update pattern that translates well to a Spring Batch job with minimal logic changes. |
| **Refactor** | ★★★★☆ | Online transaction screens (COTRN00C/01C/02C) benefit from refactoring the CICS browse/pagination into a paginated REST API with standard query parameters. |
| **Rewrite** | ★★☆☆☆ | The daily transaction posting (CBTRN02C) touches 6 files simultaneously (DALYTRAN, TRANSACT, XREF, ACCTDATA, TCATBALF, DALYREJS). A rewrite risks breaking the multi-file consistency model. |

**Recommendation: Hybrid — Strangler Fig (online) + Replatform (batch).** Replace the CICS transaction screens with REST/UI while converting batch posting to Spring Batch. The DALYTRAN → TRANSACT posting pipeline becomes an event-driven flow.

---

### 3.5 Billing, Statements & Reporting

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Strangler Fig** | ★★★☆☆ | Bill payment (COBIL00C) does online VSAM writes (REWRITE account, WRITE transaction). Hard to strangle without replacing the underlying data store. |
| **Replatform** | ★★★★★ | **Recommended.** COBIL00C (572 LOC) and CORPT00C (649 LOC) are moderate-size programs with straightforward logic. Statement generation (CREASTMT) and report submission (CORPT00C → CSUTLDTC) map to standard batch/reporting patterns. |
| **Refactor** | ★★★★☆ | Report generation via TDQ (WRITEQ TD) can be replaced with an async report generation service. Date validation (CSUTLDTC) becomes a shared utility. |
| **Rewrite** | ★★★☆☆ | Unnecessary — the logic is not complex enough to justify a full rewrite, and statement formatting requirements are well-defined. |

**Recommendation: Replatform** — Convert COBIL00C to a Java service that performs the same read-validate-rewrite-write sequence against a relational DB. Convert report generation to a scheduled Spring Batch job.

---

### 3.6 Authorization & Fraud (Optional Module)

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Strangler Fig** | ★★★★☆ | The MQ-based request/response pattern (COPAUA0C) is already loosely coupled. A new authorization service can consume from the same MQ queues. |
| **Replatform** | ★★☆☆☆ | IMS DB hierarchical data model (DBD/PSB) has no direct relational equivalent. Auto-translation would produce awkward relational schemas. |
| **Refactor** | ★★★☆☆ | DB2 AUTHFRDS table can be migrated directly, but IMS DB segments need manual schema redesign. |
| **Rewrite** | ★★★★★ | **Recommended.** This is the most complex integration (CICS + IMS DB + DB2 + MQ). The IMS hierarchical model should be redesigned as a relational schema. MQ can be replaced with a modern event broker (Kafka/SQS). Fraud detection benefits from a purpose-built rules engine. |

**Recommendation: Rewrite** — Design a new authorization microservice with a relational data model, event-driven architecture (replacing MQ), and a pluggable fraud-detection rules engine. This module is already optional and loosely coupled — ideal for a clean-slate approach.

---

## 4. Technology Target State

| Mainframe Component | Target Technology | Rationale |
|--------------------|-------------------|-----------|
| COBOL programs | Java 17+ / Spring Boot 3 | Industry standard for mainframe modernization; strong typing, JPA, Spring Batch |
| CICS transaction processing | Spring MVC REST APIs + API Gateway | Replaces 3270 terminal interaction with REST/JSON |
| BMS maps (3270 screens) | React / Angular SPA | Modern UI replacing green-screen forms |
| VSAM KSDS/AIX | PostgreSQL / Amazon Aurora | Relational DB with proper indexing replaces VSAM keyed access |
| DB2 tables | PostgreSQL / Amazon Aurora | Direct migration path for existing DB2 schemas (AUTHFRDS) |
| IMS DB (hierarchical) | PostgreSQL with redesigned schema | Flatten IMS segments into relational tables |
| IBM MQ | Amazon SQS/SNS or Apache Kafka | Event-driven async messaging |
| JCL batch jobs | Spring Batch + scheduler (Quartz/CloudWatch) | Preserves batch semantics with modern monitoring |
| Control-M | AWS Step Functions / Airflow | Cloud-native workflow orchestration |
| VSAM file I/O (batch) | JDBC / Spring Data JPA | Standard relational persistence |
| RACF (simulated) | Spring Security + OAuth2/JWT | Industry-standard auth with proper password handling |
| EBCDIC data files | UTF-8 / JSON / Parquet | Modern encoding and data formats |
| GDG (versioned datasets) | S3 versioning / database audit tables | Cloud-native data versioning |
| Copybooks (data layouts) | Java POJOs / JPA entities | Type-safe data structures with annotations |
| COMMAREA | REST request/response DTOs | Stateless inter-service communication |

---

## 5. Recommended Strategy Summary

| Functional Area | Primary Strategy | Secondary | Risk Level | Est. Effort |
|----------------|-----------------|-----------|------------|-------------|
| Authentication & User Mgmt | **Rewrite** | — | Low | 3–4 weeks |
| Account Management | **Strangler Fig** | Refactor | Medium | 6–8 weeks |
| Credit Card Management | **Strangler Fig** | Refactor | Medium | 5–7 weeks |
| Transaction Processing | **Strangler Fig** (online) + **Replatform** (batch) | — | High | 8–12 weeks |
| Billing, Statements & Reporting | **Replatform** | Refactor | Medium | 4–6 weeks |
| Authorization & Fraud | **Rewrite** | — | High | 8–10 weeks |
| Branch Migration (Export/Import) | **Rewrite** | — | Low | 2–3 weeks |
| Cross-cutting utilities | **Refactor** | — | Low | 1–2 weeks |

**Total estimated duration:** 37–52 weeks (sequential), reducible to 20–30 weeks with parallel workstreams.

---

## 6. Key Architectural Decisions

### ADR-1: Database Strategy
**Decision:** Single PostgreSQL instance with schema-per-domain during migration; split to separate databases post-migration if scale demands.
**Rationale:** VSAM files are already logically separated (ACCTDATA, CARDDATA, CUSTDATA, etc.), mapping naturally to separate schemas. A single instance simplifies the dual-write phase.

### ADR-2: API-First Design
**Decision:** All new services expose REST APIs conforming to OpenAPI 3.0 specifications before UI development begins.
**Rationale:** Decouples frontend modernization from backend migration. Enables parallel UI and API development.

### ADR-3: Event-Driven Batch Replacement
**Decision:** Replace the daily CLOSEFIL → POSTTRAN → OPENFIL batch window with event-driven processing where feasible, retaining Spring Batch for end-of-day aggregations.
**Rationale:** Eliminates the CICS file close/open window that causes application downtime. Real-time transaction posting improves user experience.

### ADR-4: COMMAREA Replacement
**Decision:** Replace the CARDDEMO-COMMAREA (shared 01-level structure across all programs) with stateless REST DTOs and server-side session state where required.
**Rationale:** COMMAREA is a 200+ byte shared structure carrying user context, customer info, account info, and card info across CICS pseudo-conversational returns. This tight coupling must be broken for microservice decomposition.

### ADR-5: Data Migration Approach
**Decision:** Use a dedicated ETL pipeline (AWS DMS or custom Spring Batch jobs) to migrate VSAM/EBCDIC data to PostgreSQL, leveraging the existing ASCII data samples as validation baselines.
**Rationale:** ASCII sample data in `app/data/ASCII/` provides known-good test fixtures for validating the EBCDIC → UTF-8 → relational transformation pipeline.
