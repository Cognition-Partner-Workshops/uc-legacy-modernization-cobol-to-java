# CardDemo Domain Decomposition

> **Generated:** 2026-03-25 | **Methodology:** Domain-Driven Design bounded context identification with extraction seam analysis

---

## 1. Overview

The CardDemo monolith is a single CICS region with shared VSAM files and a common COMMAREA. This document identifies natural bounded contexts, maps the current COBOL artifacts to each, analyzes the seams where contexts can be separated, and defines the target microservice (or modular monolith) architecture.

---

## 2. Bounded Context Map

### 2.1 Identified Bounded Contexts

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         CardDemo Application                                │
│                                                                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │
│  │  IDENTITY &  │  │   ACCOUNT    │  │    CARD      │  │ TRANSACTION  │   │
│  │  ACCESS      │  │   MGMT       │  │    MGMT      │  │ PROCESSING   │   │
│  │              │  │              │  │              │  │              │   │
│  │ COSGN00C     │  │ COACTVWC     │  │ COCRDLIC     │  │ COTRN00C     │   │
│  │ COUSR00C     │  │ COACTUPC     │  │ COCRDSLC     │  │ COTRN01C     │   │
│  │ COUSR01C     │  │ CBACT01C     │  │ COCRDUPC     │  │ COTRN02C     │   │
│  │ COUSR02C     │  │              │  │ CBACT02C     │  │ CBTRN01C     │   │
│  │ COUSR03C     │  │              │  │ CBACT03C     │  │ CBTRN02C     │   │
│  │              │  │              │  │              │  │              │   │
│  │ USRSEC VSAM  │  │ ACCTDATA     │  │ CARDDATA     │  │ TRANSACT     │   │
│  │              │  │              │  │ CARDXREF     │  │ DALYTRAN     │   │
│  │              │  │              │  │              │  │ TCATBALF     │   │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘   │
│         │                 │                 │                 │             │
│  ┌──────┴─────────────────┴─────────────────┴─────────────────┴───────┐    │
│  │                        SHARED KERNEL                               │    │
│  │  COCOM01Y (COMMAREA), COTTL01Y, CSDAT01Y, CSMSG01Y, DFHAID/BMSCA │    │
│  │  CSUTLDTC (date utility), CSLKPCDY (lookup codes)                 │    │
│  └────────────────────────────────────────────────────────────────────┘    │
│                                                                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │
│  │  BILLING &   │  │  FINANCIAL   │  │  REPORTING   │  │    DATA      │   │
│  │  PAYMENTS    │  │  PROCESSING  │  │              │  │  MIGRATION   │   │
│  │              │  │              │  │              │  │              │   │
│  │ COBIL00C     │  │ CBACT04C     │  │ CORPT00C     │  │ CBEXPORT     │   │
│  │              │  │              │  │ CBTRN03C     │  │ CBIMPORT     │   │
│  │              │  │              │  │ CBSTM03A/B   │  │              │   │
│  │              │  │              │  │              │  │              │   │
│  │ TRANSACT     │  │ TCATBALF     │  │ (reads all)  │  │ (all files)  │   │
│  │ ACCTDATA     │  │ DISCGRP      │  │              │  │              │   │
│  │              │  │ ACCTDATA     │  │              │  │              │   │
│  └──────────────┘  └──────────────┘  └──────────────┘  └──────────────┘   │
│                                                                             │
│  ┌──────────────────────────────────────────────────────────────────────┐  │
│  │  OPTIONAL CONTEXTS (separate deployment units today)                 │  │
│  │  ┌─────────────────┐  ┌──────────────────┐  ┌────────────────────┐  │  │
│  │  │ AUTHORIZATION   │  │ TRAN TYPE MGMT   │  │ MQ INTEGRATION     │  │  │
│  │  │ (IMS/DB2/MQ)    │  │ (DB2)            │  │ (VSAM-MQ)          │  │  │
│  │  │ 8 programs      │  │ 3 programs       │  │ 2 programs         │  │  │
│  │  └─────────────────┘  └──────────────────┘  └────────────────────┘  │  │
│  └──────────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 3. Bounded Context Details

### 3.1 Identity & Access Context

| Attribute | Detail |
|-----------|--------|
| **Ubiquitous Language** | User, Credential, Role (Admin/Regular), Session, Authentication |
| **Programs** | COSGN00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C |
| **Copybooks** | CSUSR01Y, COCOM01Y (user fields only) |
| **BMS Maps** | COSGN00, COUSR00, COUSR01, COUSR02, COUSR03 |
| **VSAM Files** | USRSEC.VSAM.KSDS |
| **Total LOC** | 2,027 |

**Internal Cohesion:** Very high. All 5 programs operate exclusively on USRSEC VSAM. No other context reads or writes user data (except COSGN00C which authenticates).

**External Dependencies:**
- OUTBOUND: Populates `CDEMO-USR-ID` and `CDEMO-USR-TYP` in COMMAREA → consumed by all other contexts for authorization checks
- INBOUND: None — this context is self-contained

**Target Service:** `identity-service` — Spring Security + JPA `UserRepository`

---

### 3.2 Account Management Context

| Attribute | Detail |
|-----------|--------|
| **Ubiquitous Language** | Account, Balance, Credit Limit, Account Status, Cycle Credit/Debit, Disclosure Group |
| **Programs** | COACTVWC, COACTUPC, CBACT01C |
| **Copybooks** | CVACT01Y, COCOM01Y (account fields) |
| **BMS Maps** | COACTVW, COACTUP |
| **VSAM Files** | ACCTDATA.VSAM.KSDS (primary owner) |
| **Total LOC** | 5,607 |

**Internal Cohesion:** High for core account CRUD. COACTUPC (4,236 LOC) dominates this context.

**External Dependencies:**
- OUTBOUND: Account balance read by Billing (COBIL00C), Financial Processing (CBACT04C), Transaction Posting (CBTRN02C), Statement Gen (CBSTM03A)
- INBOUND: COACTUPC reads CUSTDATA (Customer context) and CARDXREF (Card context) for display
- SHARED DATA: `CDEMO-ACCT-ID` in COMMAREA bridges Account and Card contexts

**Coupling Analysis:** ACCTDATA is the most cross-referenced VSAM file — read by 8 programs across 4 contexts. This makes Account the **most coupled context** and the hardest to extract cleanly.

**Target Service:** `account-service` — Spring MVC + JPA `AccountRepository`

---

### 3.3 Card Management Context

| Attribute | Detail |
|-----------|--------|
| **Ubiquitous Language** | Card, Card Number, CVV, Embossed Name, Card Status, Cross-Reference |
| **Programs** | COCRDLIC, COCRDSLC, COCRDUPC, CBACT02C, CBACT03C |
| **Copybooks** | CVACT02Y, CVACT03Y, CVCRD01Y |
| **BMS Maps** | COCRDLI, COCRDSL, COCRDUP |
| **VSAM Files** | CARDDATA.VSAM.KSDS, CARDXREF.VSAM.KSDS (primary owner of both) |
| **Total LOC** | 4,084 |

**Internal Cohesion:** High. Card list/view/update form a natural CRUD group. Cross-reference (CVACT03Y) is the join table linking Card → Account → Customer.

**External Dependencies:**
- OUTBOUND: CARDXREF read by Transaction Posting (CBTRN02C), Transaction Add (COTRN02C), Bill Payment (COBIL00C), Statement Gen (CBSTM03A), Interest Calc (CBACT04C)
- INBOUND: COCRDSLC and COCRDUPC read CUSTDATA for customer name display
- SHARED DATA: `CDEMO-CARD-NUM` in COMMAREA

**Coupling Analysis:** CARDXREF is the second-most referenced file. It's the central lookup table that resolves card numbers to account IDs and customer IDs. Every transaction-related operation needs it.

**Target Service:** `card-service` — owns cards and cross-references. Exposes lookup API consumed by other services.

---

### 3.4 Transaction Processing Context

| Attribute | Detail |
|-----------|--------|
| **Ubiquitous Language** | Transaction, Transaction Type, Transaction Category, Daily Transaction, Posting, Rejection |
| **Programs** | COTRN00C, COTRN01C, COTRN02C, CBTRN01C, CBTRN02C |
| **Copybooks** | CVTRA05Y, CVTRA06Y, CVTRA01Y, CVTRA03Y, CVTRA04Y |
| **BMS Maps** | COTRN00, COTRN01, COTRN02 |
| **VSAM Files** | TRANSACT.VSAM.KSDS, DALYTRAN.VSAM.KSDS, TCATBALF.VSAM.KSDS (primary owner) |
| **JCL** | POSTTRAN, TRANFILE, TRANIDX, TRANTYPE, TRANCATG |
| **Total LOC** | 3,543 |

**Internal Cohesion:** Very high. Online transaction CRUD + batch posting form a cohesive domain. The daily-to-master posting pipeline is entirely within this context.

**External Dependencies:**
- OUTBOUND: TRANSACT read by Reporting (CBTRN03C, CBSTM03A), Financial Processing (CBACT04C — reads for interest calc). TCATBALF read by Financial Processing.
- INBOUND: COTRN02C reads ACCTDATA (Account) and CARDXREF (Card) for validation. CBTRN02C reads ACCTDATA and CARDXREF for posting validation.
- SHARED DATA: Transaction records created here flow to Billing, Reporting, and Statements.

**Coupling Analysis:** TRANSACT is the highest-volume VSAM file and feeds into multiple downstream contexts. The daily posting pipeline (DALYTRAN → TRANSACT) is the core batch workflow.

**Target Service:** `transaction-service` — owns transaction lifecycle from creation through posting. Publishes transaction events consumed by downstream services.

---

### 3.5 Billing & Payments Context

| Attribute | Detail |
|-----------|--------|
| **Ubiquitous Language** | Payment, Bill Pay, Balance Update |
| **Programs** | COBIL00C |
| **Copybooks** | CVACT01Y (account), CVACT03Y (xref), CVTRA05Y (transaction) |
| **BMS Maps** | COBIL00 |
| **VSAM Files** | Writes to TRANSACT and ACCTDATA (dual-write) |
| **Total LOC** | 572 |

**Internal Cohesion:** Medium. Single program, but it spans two VSAM files owned by other contexts (TRANSACT owned by Transaction Processing, ACCTDATA owned by Account Management).

**External Dependencies:**
- OUTBOUND: Creates transactions in TRANSACT (Transaction context), updates balance in ACCTDATA (Account context)
- INBOUND: Reads CARDXREF (Card context) to resolve card → account

**Coupling Analysis:** This is the **most cross-cutting operation** in the system. A single bill payment touches three contexts' data. This is where domain boundaries are weakest.

**Extraction Decision:** In a microservice architecture, Bill Payment becomes an **orchestration service** that calls Transaction Service (create payment) and Account Service (update balance) within a saga or distributed transaction.

**Target Service:** `billing-service` — thin orchestrator. OR: fold into `transaction-service` if you prefer a modular monolith to avoid distributed transaction complexity.

---

### 3.6 Financial Processing Context

| Attribute | Detail |
|-----------|--------|
| **Ubiquitous Language** | Interest Rate, Disclosure Group, Category Balance, Interest Transaction |
| **Programs** | CBACT04C |
| **Copybooks** | CVTRA01Y (cat balance), CVTRA02Y (disclosure group) |
| **VSAM Files** | DISCGRP.VSAM.KSDS (primary owner), reads TCATBALF, ACCTDATA, CARDXREF |
| **JCL** | INTCALC |
| **Total LOC** | 652 |

**Internal Cohesion:** High. Single-purpose batch program with its own reference data (DISCGRP).

**External Dependencies:**
- OUTBOUND: Writes interest transactions to TRANSACT (Transaction context)
- INBOUND: Reads ACCTDATA (Account), CARDXREF (Card), TCATBALF (Transaction)

**Target Service:** `financial-processing-service` — batch-only. Consumes account/card data via API, produces interest transactions published to Transaction Service.

---

### 3.7 Reporting Context

| Attribute | Detail |
|-----------|--------|
| **Ubiquitous Language** | Report, Statement, Transaction Report, Page Total, Grand Total |
| **Programs** | CORPT00C, CBTRN03C, CBSTM03A, CBSTM03B |
| **Copybooks** | CVTRA07Y (report layouts), COSTM01 (statement layout), CUSTREC |
| **BMS Maps** | CORPT00 |
| **VSAM Files** | Reads all major files (TRANSACT, CARDXREF, ACCTDATA, CUSTDATA, TRANTYPE, TRANCATG) |
| **JCL** | CREASTMT, TRANREPT, PRTCATBL, TXT2PDF1, DALYREJS, REPTFILE |
| **Total LOC** | 2,452 |

**Internal Cohesion:** High. All programs produce reports/statements. Read-only access to other contexts' data.

**External Dependencies:**
- OUTBOUND: None — produces report files only
- INBOUND: Reads from Account, Card, Transaction, and Customer data. This context is a **pure consumer** of other contexts' data.

**Coupling Analysis:** Reads the most files of any context (6 VSAM files), but all access is read-only. This makes Reporting an ideal candidate for a **CQRS read model** — a separate denormalized view optimized for reporting queries.

**Target Service:** `reporting-service` — Spring Batch jobs consuming a denormalized read model (materialized views or event-sourced projections).

---

### 3.8 Data Migration Context

| Attribute | Detail |
|-----------|--------|
| **Programs** | CBEXPORT, CBIMPORT |
| **VSAM Files** | Reads/writes all 5 core VSAM files |
| **Total LOC** | 1,069 |

**Internal Cohesion:** High — dedicated import/export pair.

**Target Service:** Temporary migration tooling, not a permanent service. Replace with database dump/restore or ETL pipeline.

---

## 4. Extraction Seam Analysis

A **seam** is a point where two bounded contexts can be separated with minimal disruption. Seams are identified by analyzing shared data, shared copybooks, and cross-context EXEC CICS XCTL/LINK calls.

### 4.1 Seam Inventory

| Seam ID | Between Contexts | Coupling Type | Current Mechanism | Separation Difficulty |
|---------|-----------------|---------------|------------------|-----------------------|
| S1 | Identity → All | Auth state | COMMAREA (USR-ID, USR-TYP) | **Easy** — Replace with JWT claims |
| S2 | Account ↔ Card | Data lookup | CARDXREF VSAM + COMMAREA (ACCT-ID) | **Medium** — XREF is the join. Service-to-service API call |
| S3 | Card → Transaction | Card validation | COTRN02C reads CARDXREF via STARTBR | **Medium** — API call for card lookup |
| S4 | Transaction → Account | Balance query | COTRN02C/CBTRN02C reads ACCTDATA | **Medium** — API call for account validation |
| S5 | Billing → Transaction + Account | Dual-write | COBIL00C writes TRANSACT + rewrites ACCTDATA | **Hard** — Requires saga/distributed transaction |
| S6 | Financial → Transaction + Account | Multi-read + write | CBACT04C reads 4 files, writes TRANSACT | **Hard** — Batch cross-context data access |
| S7 | Reporting → All | Read-only fan-out | CBTRN03C/CBSTM03A reads 5-6 files | **Easy** — CQRS read model solves this cleanly |
| S8 | Navigation → All | Screen routing | COMEN01C XCTL to functional programs | **Easy** — Web UI routing replaces XCTL |
| S9 | Transaction Online ↔ Batch | Data pipeline | DALYTRAN → TRANSACT posting | **Easy** — Same context, internal pipeline |

### 4.2 Seam Difficulty Assessment

```
Difficulty:  EASY ──────────────── MEDIUM ──────────────── HARD
             │                      │                       │
             S1 (Identity)          S2 (Account↔Card)       S5 (Billing dual-write)
             S7 (Reporting CQRS)    S3 (Card→Transaction)   S6 (Financial batch)
             S8 (Navigation)        S4 (Transaction→Acct)
             S9 (Tran pipeline)
```

### 4.3 Detailed Seam Analysis

#### S1 — Identity Seam (Easy)

**Current coupling:** Every online program reads `CDEMO-USR-ID` and `CDEMO-USR-TYP` from the COMMAREA to check authorization. COSGN00C populates these fields after authentication.

**Extraction approach:**
1. Replace COMMAREA user fields with JWT token claims (`sub`, `role`)
2. Each service validates the JWT independently (stateless auth)
3. No data migration needed — user records move entirely to the Identity service

**Seam cut:** Remove CSUSR01Y copybook dependency from non-Identity programs. Replace COMMAREA user fields with request-scoped security context.

---

#### S2 — Account ↔ Card Seam (Medium)

**Current coupling:** CARDXREF.VSAM.KSDS is the join table. Programs in both contexts read it:
- Card context: COCRDLIC browses cards by account
- Account context: COACTUPC reads customer info via card → xref → customer

**Extraction approach:**
1. Card Service owns CARDXREF data (it's a card-centric lookup)
2. Card Service exposes: `GET /cards?accountId={id}` and `GET /cards/{cardNum}/account`
3. Account Service calls Card Service API instead of directly reading CARDXREF VSAM

**Seam cut:** Replace all CARDXREF VSAM reads in Account programs with Card Service API calls. Keep CARDXREF as a Card Service internal table.

**Risk:** Added latency for cross-service calls. Mitigate with caching (card-to-account mappings change rarely).

---

#### S3 — Card → Transaction Seam (Medium)

**Current coupling:** COTRN02C (transaction add) browses CARDXREF to validate that the card exists and is active before writing the transaction.

**Extraction approach:**
1. Transaction Service calls Card Service: `GET /cards/{cardNum}/validate`
2. Card Service returns card status + associated account ID
3. Transaction Service proceeds with write if valid

**Seam cut:** Replace EXEC CICS STARTBR/READPREV on CARDXREF in COTRN02C with a Card Service API call.

---

#### S4 — Transaction → Account Seam (Medium)

**Current coupling:** COTRN02C reads ACCTDATA to display account info. CBTRN02C (batch posting) reads ACCTDATA to validate accounts during posting.

**Extraction approach:**
1. Online: Transaction Service calls Account Service `GET /accounts/{id}` for display
2. Batch: Transaction Posting Service calls Account Service for validation. Consider bulk API: `POST /accounts/validate` with batch of account IDs

**Seam cut:** Replace ACCTDATA VSAM reads in Transaction programs with Account Service API calls.

---

#### S5 — Billing Dual-Write Seam (Hard)

**Current coupling:** COBIL00C performs an atomic dual-write:
1. `EXEC CICS WRITE` → creates payment transaction in TRANSACT
2. `EXEC CICS REWRITE` → updates account balance in ACCTDATA

In CICS, both operations are within a single logical unit of work (LUW). If either fails, CICS rolls back both.

**Extraction approach — Option A (Saga):**
1. Billing Service calls Transaction Service: "Create payment transaction"
2. On success, calls Account Service: "Deduct balance"
3. On failure of step 2, calls Transaction Service: "Reverse payment transaction" (compensating action)

**Extraction approach — Option B (Modular Monolith):**
1. Keep Billing and Transaction in the same deployment unit sharing a database
2. Use a single `@Transactional` method spanning both tables
3. Defer service separation until the team is comfortable with distributed transactions

**Recommendation:** Start with Option B (modular monolith). Graduate to Option A when the team adopts event-driven architecture.

---

#### S6 — Financial Processing Batch Seam (Hard)

**Current coupling:** CBACT04C reads 4 VSAM files across 3 contexts:
- TCATBALF (Transaction context) — category balances
- DISCGRP (Financial context — owns this) — interest rates
- ACCTDATA (Account context) — account details
- CARDXREF (Card context) — card-to-account mapping

**Extraction approach:**
1. Financial Processing Service reads a **snapshot** of required data at batch start
2. Account Service and Card Service expose bulk export APIs: `GET /accounts/snapshot`, `GET /cards/xref/snapshot`
3. Financial Service processes locally against the snapshot
4. Resulting interest transactions published to Transaction Service via async message

**Seam cut:** Replace VSAM file DD references in INTCALC.jcl with API-sourced data feeds. The batch job fetches snapshots, processes, and publishes results.

---

#### S7 — Reporting Seam (Easy)

**Current coupling:** Reporting programs read 5-6 VSAM files but never write to them. Pure read-only consumer.

**Extraction approach (CQRS):**
1. Each service publishes domain events (account updated, transaction posted, etc.)
2. Reporting Service consumes events and builds a denormalized read model
3. Reports query the read model — no cross-service API calls at report time

**Seam cut:** Reporting programs stop reading production VSAM files. Instead, they query a reporting database populated by event consumers.

---

## 5. Cross-Cutting Concerns

### 5.1 COMMAREA Decomposition

The COMMAREA (`COCOM01Y`) is the monolith's shared state bus. It must be decomposed:

| COMMAREA Field | Target Mechanism | Owner Context |
|---------------|-----------------|---------------|
| CDEMO-USR-ID, CDEMO-USR-TYP | JWT claims / Security Context | Identity |
| CDEMO-FROM-TRANID, CDEMO-TO-TRANID | URL routing / session state | Navigation (eliminated) |
| CDEMO-FROM-PROGRAM, CDEMO-TO-PROGRAM | URL routing / session state | Navigation (eliminated) |
| CDEMO-ACCT-ID | Request parameter / path variable | Account |
| CDEMO-CARD-NUM | Request parameter / path variable | Card |
| CDEMO-CUST-ID | Request parameter / path variable | Card (via XREF) |
| CDEMO-PGM-REENTER | Session attribute (form resubmit flag) | Each service locally |
| CDEMO-LAST-MAP, CDEMO-LAST-MAPSET | Eliminated (web routing handles this) | None |

### 5.2 Shared Copybook Resolution

| Copybook | Current Usage | Resolution |
|----------|--------------|------------|
| COCOM01Y | All 18 online programs | Decompose per field (see above) |
| COTTL01Y | All screen programs | Replaced by web UI header component |
| CSDAT01Y | All screen programs | Each service uses `java.time` |
| CSMSG01Y/02Y | All screen programs | Each service uses its own error/message DTOs |
| DFHAID/DFHBMSCA | All screen programs | Eliminated — BMS not used in web UI |
| CSLKPCDY | Lookup codes | Shared library JAR or database reference tables |
| CSSTRPFY | String utility | Apache Commons StringUtils or shared utility class |

### 5.3 Data Ownership Matrix

| VSAM File | Owner Context | Read-Only Consumers | Write Consumers (non-owner) |
|-----------|--------------|--------------------|-----------------------------|
| USRSEC | Identity | — | — |
| ACCTDATA | Account | Card (display), Reporting, Financial, Transaction (validation) | Billing (balance update) |
| CARDDATA | Card | Reporting | — |
| CARDXREF | Card | Account (display), Transaction (validation), Reporting, Financial | — |
| TRANSACT | Transaction | Reporting | Billing (payment creation), Financial (interest txn) |
| DALYTRAN | Transaction | — | — |
| TCATBALF | Transaction | Financial | — |
| DISCGRP | Financial | — | — |
| TRANTYPE | Transaction | Reporting | — |
| TRANCATG | Transaction | Reporting | — |

---

## 6. Target Architecture

### 6.1 Option A — Microservices (Full Decomposition)

```
                    ┌─────────────┐
                    │  API Gateway │
                    │  (Spring    │
                    │   Cloud)    │
                    └──────┬──────┘
           ┌───────┬───────┼───────┬───────┬───────┐
           ▼       ▼       ▼       ▼       ▼       ▼
      ┌────────┐┌──────┐┌──────┐┌──────┐┌──────┐┌──────┐
      │Identity││Acct  ││Card  ││Trans ││Billing││Report│
      │Service ││Svc   ││Svc   ││Svc   ││Svc   ││Svc   │
      └───┬────┘└──┬───┘└──┬───┘└──┬───┘└──┬───┘└──┬───┘
          │        │       │       │       │       │
      ┌───┴──┐ ┌───┴──┐┌──┴───┐┌──┴───┐   │   ┌──┴───┐
      │users │ │accts │|cards ||trans |   │   │report│
      │ DB   │ │ DB   ││xref  ││daily │   │   │  DB  │
      └──────┘ └──────┘│ DB   ││catbal│   │   │(CQRS)│
                       └──────┘│ DB   │   │   └──────┘
                               └──────┘   │
                          ┌───────────────┘
                          ▼
                    ┌──────────┐
                    │ Message  │
                    │ Broker   │
                    │(RabbitMQ)│
                    └──────────┘
```

**Pros:** Independent deployment, scaling, and technology choices per service.
**Cons:** Distributed transaction complexity (especially Billing). Operational overhead.

### 6.2 Option B — Modular Monolith (Recommended Starting Point)

```
      ┌─────────────────────────────────────────────────────────┐
      │                Spring Boot Application                   │
      │                                                         │
      │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐  │
      │  │ identity │ │ account  │ │  card    │ │  trans   │  │
      │  │ module   │ │ module   │ │  module  │ │  module  │  │
      │  └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘  │
      │       │            │            │            │         │
      │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐  │
      │  │ billing  │ │financial │ │reporting │ │ shared   │  │
      │  │ module   │ │ module   │ │ module   │ │ kernel   │  │
      │  └────┬─────┘ └────┬─────┘ └────┬─────┘ └──────────┘  │
      │       │            │            │                      │
      │  ┌────┴────────────┴────────────┴──────────────────┐   │
      │  │            Shared PostgreSQL Database            │   │
      │  │  (schemas: identity, account, card, transaction, │   │
      │  │   billing, financial, reporting)                  │   │
      │  └─────────────────────────────────────────────────┘   │
      └─────────────────────────────────────────────────────────┘
```

**Pros:** Single deployment, shared database transaction support (solves Billing dual-write), simpler operations.
**Cons:** Modules can drift into coupling if boundaries aren't enforced.

**Recommendation:** Start with Option B. Use Java package boundaries and `ArchUnit` tests to enforce module separation. Graduate to Option A when the team needs independent scaling or deployment.

---

## 7. Context Interaction Patterns

| From → To | Current Pattern | Target Pattern |
|-----------|----------------|----------------|
| Identity → All | COMMAREA fields | JWT token / Spring Security Context |
| Account → Card | VSAM XREF read | Module API call or shared DB read (modular monolith) |
| Transaction → Card | VSAM XREF STARTBR | Card module API: `findByAccountId()` |
| Transaction → Account | VSAM ACCTDATA read | Account module API: `findById()` |
| Billing → Transaction | VSAM TRANSACT write | Transaction module API: `createPayment()` |
| Billing → Account | VSAM ACCTDATA rewrite | Account module API: `updateBalance()` |
| Financial → multiple | VSAM multi-file read | Snapshot APIs + async event publishing |
| Reporting → All | VSAM multi-file read | CQRS read model (denormalized views) |
| Navigation → All | CICS XCTL | Web UI routing (React Router / Spring MVC) |
