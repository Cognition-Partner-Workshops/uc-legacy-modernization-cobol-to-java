# CardDemo Domain Decomposition

## Overview

This document identifies bounded contexts within the CardDemo COBOL/CICS application using Domain-Driven Design (DDD) principles. Each bounded context is analyzed for extraction seams — the interfaces, data boundaries, and coupling points that determine where the monolithic application can be split into independent services.

---

## Source System Data Flow

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        CICS Online Region                               │
│                                                                         │
│  COSGN00C ──► COMEN01C ──► COACTVWC / COACTUPC (Account)              │
│     │            │     ──► COCRDLIC / COCRDSLC / COCRDUPC (Card)       │
│     │            │     ──► COTRN00C / COTRN01C / COTRN02C (Txn)       │
│     │            │     ──► COBIL00C (Bill Payment)                      │
│     │            │     ──► CORPT00C (Reports)                           │
│     │            │                                                      │
│     │         COADM01C ──► COUSR00C-03C (User Admin)                   │
│     │                                                                   │
│  [COMMAREA: COCOM01Y - shared navigation/session state]                │
└────────────────────────────┬────────────────────────────────────────────┘
                             │ VSAM Files
                             ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  USRSEC    ACCTDAT    CARDDAT    CUSTDAT    TRANSACT    CARDXREF       │
│  (80B)     (300B)     (150B)     (500B)     (350B)      (50B)          │
│                                              TCATBAL    DALYTRAN       │
│                                              (50B)      (350B)         │
└────────────────────────────┬────────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                        Batch Region (JCL)                               │
│                                                                         │
│  CLOSEFIL → Data Refresh → POSTTRAN → INTCALC → TRANBKP              │
│           → COMBTRAN → CREASTMT → TRANIDX → OPENFIL                   │
│                                                                         │
│  CBTRN02C (Post)  CBACT04C (Interest)  CBSTM03A/B (Statements)        │
│  CBTRN03C (Report)  CBEXPORT/CBIMPORT (ETL)                           │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## Bounded Contexts

### 1. Identity & Access Management (IAM) Context

**Aggregate Root:** `User` (SEC-USER-DATA)

**Programs:**
- COSGN00C — Authentication (login/logout)
- COADM01C — Admin menu routing (role-based)
- COMEN01C — User menu routing (role-based)
- COUSR00C — User list (admin)
- COUSR01C — User add (admin)
- COUSR02C — User update (admin)
- COUSR03C — User delete (admin)

**Data Stores:**
- USRSEC VSAM KSDS (key: SEC-USR-ID, 80 bytes per record)

**Copybooks:**
- CSUSR01Y — User security record layout (ID, first/last name, password, type)
- COCOM01Y — COMMAREA fields: CDEMO-USER-ID, CDEMO-USER-TYPE (Admin 'A' / User 'U')

**Domain Entities:**
| Entity | Source | Key Fields |
|---|---|---|
| User | CSUSR01Y | user_id (8), first_name (20), last_name (20), password (8), type (1) |

**Extraction Seam Analysis:**

| Seam | Description | Coupling |
|---|---|---|
| **Authentication API** | COSGN00C reads USRSEC, compares password, sets COMMAREA user fields. Clean request/response boundary. | **Low** — Only reads USRSEC. No writes except session state (COMMAREA). |
| **User CRUD API** | COUSR00C-03C perform full CRUD on USRSEC via CICS file control. Self-contained. | **Low** — Only touches USRSEC file. No dependencies on other business data. |
| **Session/Role Propagation** | COMEN01C and COADM01C read CDEMO-USER-TYPE from COMMAREA to route to role-appropriate menus. | **Medium** — Every downstream program receives user context via COMMAREA. New services need a shared session/token mechanism. |

**Extraction Difficulty:** **Low**
- USRSEC is only accessed by auth/user-admin programs
- No foreign key relationships to business data (users are referenced by ID in COMMAREA but not stored in ACCTDAT/TRANSACT)
- Clean cut: replace USRSEC with a `users` table + Spring Security

---

### 2. Account Context

**Aggregate Root:** `Account` (ACCOUNT-RECORD)

**Programs:**
- COACTVWC — Account view (942 lines)
- COACTUPC — Account update (4237 lines)

**Data Stores:**
- ACCTDAT VSAM KSDS (key: ACCT-ID, 300 bytes)
- CARDXREF VSAM KSDS (key: XREF-CARD-NUM — shared with Card Context)

**Copybooks:**
- CVACT01Y — Account record (ACCT-ID, status, balance, credit limit, cash credit limit, open/expiry/reissue dates, cycle credits/debits, zip, group ID)
- CVACT03Y — Card cross-reference (XREF-CARD-NUM, XREF-CUST-ID, XREF-ACCT-ID)
- COCOM01Y — COMMAREA fields: CDEMO-ACCT-ID, CDEMO-ACCT-STATUS

**Domain Entities:**
| Entity | Source | Key Fields |
|---|---|---|
| Account | CVACT01Y | acct_id (11), status (1), curr_bal, credit_limit, cash_credit_limit, open_date, expiry_date, reissue_date, cycle_credit, cycle_debit, zip (10), group_id (10) |
| CardXRef | CVACT03Y | card_num (16), cust_id (9), acct_id (11) |

**Extraction Seam Analysis:**

| Seam | Description | Coupling |
|---|---|---|
| **Account View API** | COACTVWC reads ACCTDAT by key, formats for display. Pure read operation. | **Low** — Read-only access to ACCTDAT. |
| **Account Update API** | COACTUPC performs READ UPDATE / REWRITE on ACCTDAT with extensive validation. | **Medium** — Writes to ACCTDAT. Batch programs (CBACT04C, CBTRN02C) also update ACCTDAT balances concurrently. Requires coordination or eventual consistency. |
| **Cross-Reference Lookup** | COACTVWC reads CARDXREF (CXACAIX alternate index) to find cards for an account. | **High** — CARDXREF is shared between Account, Card, and Transaction contexts. This is the primary coupling point. |

**Extraction Difficulty:** **Medium**
- ACCTDAT is written by both online (COACTUPC, COBIL00C) and batch (CBTRN02C posting, CBACT04C interest) programs
- CARDXREF is the central cross-reference used by Account, Card, and Transaction contexts
- Seam strategy: Introduce an Account Service that owns ACCTDAT; other contexts interact via API calls or domain events

---

### 3. Card Context

**Aggregate Root:** `Card` (CARD-RECORD)

**Programs:**
- COCRDLIC — Card list with pagination (1460 lines)
- COCRDSLC — Card detail view
- COCRDUPC — Card update

**Data Stores:**
- CARDDAT VSAM KSDS (key: CARD-NUM, 150 bytes)
- CARDAIX alternate index (by CARD-ACCT-ID)
- CARDXREF VSAM KSDS (shared with Account Context)

**Copybooks:**
- CVACT02Y — Card record (card_num, acct_id, CVV, embossed name, expiry, status)
- CVACT03Y — Cross-reference (shared)
- COCOM01Y — COMMAREA fields: CDEMO-CARD-NUM

**Domain Entities:**
| Entity | Source | Key Fields |
|---|---|---|
| Card | CVACT02Y | card_num (16), acct_id (11), cvv (3), embossed_name (50), expiry_date (10), status (1) |

**Extraction Seam Analysis:**

| Seam | Description | Coupling |
|---|---|---|
| **Card List API** | COCRDLIC browses CARDDAT (STARTBR/READNEXT/ENDBR). Can optionally filter by account via CARDAIX. | **Low** — Read-only browse of CARDDAT. |
| **Card View API** | COCRDSLC reads a single card record by CARD-NUM. | **Low** — Single-record read. |
| **Card Update API** | COCRDUPC performs READ UPDATE / REWRITE on CARDDAT. | **Medium** — CARDDAT is also read by transaction processing programs for validation. |
| **Card-Account Relationship** | Cards reference accounts via CARD-ACCT-ID. Cross-reference file CARDXREF links cards, accounts, and customers. | **High** — CARDXREF is the shared coupling point. |

**Extraction Difficulty:** **Medium**
- CARDDAT is relatively self-contained (owned by card programs)
- The CARDXREF cross-reference is the main coupling: it links Card ↔ Account ↔ Customer
- Seam strategy: Card Service owns CARDDAT. CARDXREF becomes a relationship table owned by either Account or a shared reference-data service

---

### 4. Transaction Context

**Aggregate Root:** `Transaction` (TRAN-RECORD)

**Programs (Online):**
- COTRN00C — Transaction list with pagination (700 lines)
- COTRN01C — Transaction detail view
- COTRN02C — Transaction add (manual entry)

**Programs (Batch):**
- CBTRN02C — Transaction posting from daily file (732 lines)
- CBTRN03C — Transaction reporting
- CBACT04C — Interest calculation (reads transaction category balances)

**Data Stores:**
- TRANSACT VSAM KSDS (key: FD-TRANS-ID, 350 bytes)
- DALYTRAN sequential file (daily transactions pending posting)
- TCATBAL VSAM KSDS (key: TRAN-CAT-KEY = acct_id + type + category, 50 bytes)
- DALYREJS sequential file (rejected transactions)

**Copybooks:**
- CVTRA05Y — Transaction record (tran_id, type, category, source, desc, amount, merchant info, card_num, timestamps)
- CVTRA06Y — Daily transaction record (same layout + filler)
- CVTRA01Y — Transaction category balance (acct_id + type + category → balance)
- CVTRA02Y–CVTRA07Y — Various transaction views

**Domain Entities:**
| Entity | Source | Key Fields |
|---|---|---|
| Transaction | CVTRA05Y | tran_id (16), type_cd (2), cat_cd (4), source (10), desc (100), amount, merchant_id (9), merchant_name (50), merchant_city (50), merchant_zip (10), card_num (16), orig_ts (26), proc_ts (26) |
| DailyTransaction | CVTRA06Y | Same as Transaction + 20-byte filler |
| TranCategoryBalance | CVTRA01Y | acct_id (11), type_cd (2), cat_cd (4), balance |

**Extraction Seam Analysis:**

| Seam | Description | Coupling |
|---|---|---|
| **Transaction Browse API** | COTRN00C/01C browse and view TRANSACT records. Read-only. | **Low** — Pure read operations on TRANSACT. |
| **Transaction Add API** | COTRN02C creates new transactions. Generates ID by reading the last key and incrementing. | **Medium** — Writes to TRANSACT. ID generation depends on VSAM key ordering. |
| **Transaction Posting** | CBTRN02C reads DALYTRAN, validates against CARDXREF and ACCTDAT, posts to TRANSACT, writes rejects to DALYREJS, updates TCATBAL. | **High** — Reads CARDXREF (Card context), reads/updates ACCTDAT (Account context), writes TRANSACT and TCATBAL. This is the most cross-cutting batch operation. |
| **Interest Calculation** | CBACT04C reads TCATBAL sequentially, looks up discount rates (DISCGRP), computes interest, updates ACCTDAT balances, writes interest transactions to TRANSACT. | **High** — Reads TCATBAL and DISCGRP, writes TRANSACT, updates ACCTDAT. Cross-context writes. |

**Extraction Difficulty:** **High**
- TRANSACT is the most widely accessed file (online browse, online add, batch posting, batch interest, batch statements, bill payment, reporting)
- Batch posting (CBTRN02C) is the most coupled operation: it spans Transaction, Account, and Card contexts
- Seam strategy: Transaction Service owns TRANSACT and TCATBAL. Batch posting becomes a saga or orchestrated workflow that calls Account Service and Card Service APIs for validation

---

### 5. Customer Context

**Aggregate Root:** `Customer` (CUSTOMER-RECORD)

**Programs:** No dedicated online programs (customer data is read by account/card views and batch statements)

**Data Stores:**
- CUSTDAT VSAM KSDS (key: CUST-ID, 500 bytes)

**Copybooks:**
- CVCUS01Y — Customer record (cust_id, names, address lines, state/country/zip, phone numbers, SSN, govt ID, DOB, EFT account, primary cardholder indicator, FICO score)

**Domain Entities:**
| Entity | Source | Key Fields |
|---|---|---|
| Customer | CVCUS01Y | cust_id (9), first_name (25), middle_name (25), last_name (25), addr_line_1-3 (50 each), state (2), country (3), zip (10), phone_1/2 (15), ssn (9), govt_id (20), dob (10), eft_acct (10), pri_cardholder (1), fico_score (3) |

**Extraction Seam Analysis:**

| Seam | Description | Coupling |
|---|---|---|
| **Customer Read** | CUSTDAT is read by COACTVWC (account view shows customer name), CBSTM03A (statements include customer address). No online CRUD programs exist for customers. | **Low** — Read-only access from other contexts. |
| **Customer-Account Relationship** | Linked via CARDXREF (XREF-CUST-ID ↔ XREF-ACCT-ID). | **Medium** — No direct foreign key in CUSTDAT; the relationship exists only in CARDXREF. |

**Extraction Difficulty:** **Low**
- CUSTDAT is a reference data store with no dedicated CRUD programs
- Only read by other contexts (account view, statement generation)
- Seam strategy: Customer Service provides a read API. Initially can be a thin wrapper around the `customers` table. CRUD can be added post-migration

---

### 6. Reporting & Statements Context

**Aggregate Root:** `Statement` (generated output, no persistent aggregate)

**Programs:**
- CORPT00C — Online report submission (submits JCL via CICS TDQ)
- CBSTM03A — Statement generation (924 lines, reads multiple files, produces text + HTML)
- CBSTM03B — File I/O subroutine for CBSTM03A (230 lines)
- CBTRN03C — Transaction report generation

**Data Stores (Read-Only):**
- TRANSACT (transactions), XREFFILE (cross-reference), CUSTDAT (customer addresses), ACCTDAT (account summaries)
- Output: STMT-FILE (text statements), HTML-FILE (HTML statements)

**Copybooks:**
- All entity copybooks (read-only consumers)
- CBSTM03A defines internal 2D array: WS-TRNX-TABLE (51 cards × 10 transactions)

**Extraction Seam Analysis:**

| Seam | Description | Coupling |
|---|---|---|
| **Report Submission API** | CORPT00C accepts date range, submits JCL to internal reader. | **High** — Tightly coupled to CICS TDQ and JES job submission. No clean API boundary. |
| **Statement Generation** | CBSTM03A reads TRANSACT, XREFFILE, CUSTDAT, ACCTDAT and produces formatted output. CBSTM03B handles all file I/O as a subroutine. | **High** — Reads from 4 different VSAM files spanning 3 other contexts. Uses non-portable mainframe constructs (PSA/TCB/TIOT addressing, ALTER/GO TO). |
| **Report Output** | Output is plain text + HTML files. | **Low** — Output format is independent of input sources. |

**Extraction Difficulty:** **High**
- CBSTM03A is the most technically complex program (ALTER/GO TO, control block addressing, COMP-3, 2D arrays)
- Reads from every other context's data stores
- Seam strategy: Reporting Service reads from all other services' APIs (or a read replica / data warehouse). Complete rewrite of CBSTM03A is necessary due to non-portable constructs

---

### 7. Batch Operations Context

**Aggregate Root:** `BatchJobExecution` (orchestration, no persistent entity in current system)

**Programs:**
- CBEXPORT — Data export utility
- CBIMPORT — Data import utility
- COBSWAIT — Wait/delay utility

**JCL Jobs (Infrastructure):**
- CLOSEFIL / OPENFIL — CICS file quiesce/resume
- ACCTFILE, CARDFILE, CUSTFILE, XREFFILE, TRANFILE, DUSRSECJ — Data file refresh
- DEFGDGB, DEFGDGD — GDG base definitions
- TRANBKP — Transaction backup
- TRANIDX — Alternate index creation

**Extraction Seam Analysis:**

| Seam | Description | Coupling |
|---|---|---|
| **File Quiesce** | CLOSEFIL/OPENFIL manage CICS file availability. Required because batch and online cannot share VSAM files concurrently. | **Eliminated** — With a relational database, concurrent access is handled by the DBMS. This entire pattern disappears. |
| **Data Refresh** | JCL jobs load data from sequential files into VSAM. | **Replaced** — Becomes database seed/migration scripts or ETL pipelines. |
| **GDG Management** | DEFGDGB/DEFGDGD manage generation data groups for versioned files. | **Replaced** — Database snapshots or temporal tables. |

**Extraction Difficulty:** **Low** (infrastructure, not business logic)
- Most batch infrastructure operations become unnecessary with a relational database
- Export/import become standard ETL jobs
- The CLOSEFIL/OPENFIL pattern is eliminated entirely

---

## Cross-Context Coupling Analysis

### Shared Data Stores

| VSAM File | Owning Context | Other Contexts Accessing | Access Pattern |
|---|---|---|---|
| USRSEC | IAM | None | Exclusive |
| ACCTDAT | Account | Transaction (batch updates balance), Bill Payment (updates balance), Reporting (reads) | Read-Write shared |
| CARDDAT | Card | None (only card programs write) | Exclusive write, read by validation |
| CUSTDAT | Customer | Reporting (reads for statements) | Exclusive write, read shared |
| TRANSACT | Transaction | Bill Payment (creates transactions), Reporting (reads), Online browse (reads) | Write shared |
| CARDXREF | **Shared** | Account, Card, Transaction (validation), Reporting | Read shared |
| TCATBAL | Transaction | Interest calculation batch | Exclusive to batch |
| DALYTRAN | Transaction | Transaction posting batch input | Exclusive |

### The CARDXREF Problem

The `CARDXREF` (card cross-reference) file is the highest-coupling shared data store. It maps:
- **Card Number** (16 chars) → **Customer ID** (9 digits) → **Account ID** (11 digits)

This three-way relationship is used by:
1. **Account View** (COACTVWC) — find cards for an account via CXACAIX alternate index
2. **Card List** (COCRDLIC) — filter cards by account
3. **Transaction Posting** (CBTRN02C) — validate card number against account
4. **Bill Payment** (COBIL00C) — look up account from card number
5. **Statement Generation** (CBSTM03A) — cross-reference all entities

**Resolution:** In the target architecture, CARDXREF becomes a `card_account_xref` table owned by the **Account Context** (since the primary relationship is account → cards → customer). Other contexts access it via the Account Service API or a materialized view.

### COMMAREA Coupling

All online programs share the COMMAREA (COCOM01Y, 300+ bytes) for:
- Program navigation (FROM/TO TRANID/PROGRAM)
- Session state (USER-ID, USER-TYPE)
- Selected entity context (ACCT-ID, CARD-NUM, CUST-ID)

**Resolution:** Replace COMMAREA with:
1. **JWT tokens** carrying user identity and role
2. **API request parameters** for entity context (account ID, card number)
3. **Browser session/URL state** for navigation context

---

## Context Map

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│     IAM      │     │   Account    │     │     Card     │
│   Context    │────►│   Context    │◄───►│   Context    │
│              │     │              │     │              │
│ USRSEC       │     │ ACCTDAT      │     │ CARDDAT      │
│ COSGN00C     │     │ COACTVWC     │     │ COCRDLIC     │
│ COUSR00-03C  │     │ COACTUPC     │     │ COCRDSLC     │
│ COADM01C     │     │              │     │ COCRDUPC     │
│ COMEN01C     │     │ Owns:XREF   │     │              │
└──────┬───────┘     └──────┬───────┘     └──────────────┘
       │                    │                     ▲
       │ auth token         │ balance updates     │ card validation
       ▼                    ▼                     │
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│  All Other   │     │ Transaction  │─────┤ Bill Payment │
│  Contexts    │     │   Context    │     │   Context    │
│              │     │              │     │              │
│ (consume     │     │ TRANSACT     │     │ COBIL00C     │
│  auth tokens)│     │ DALYTRAN     │     │ (Creates txn,│
│              │     │ TCATBAL      │     │  updates bal)│
│              │     │ COTRN00-02C  │     │              │
│              │     │ CBTRN02/03C  │     └──────────────┘
│              │     │ CBACT04C     │
│              │     └──────┬───────┘
│              │            │
│              │            │ reads all entities
│              │            ▼
│              │     ┌──────────────┐     ┌──────────────┐
│              │     │  Reporting   │     │   Customer   │
│              │     │   Context    │     │   Context    │
│              │     │              │     │              │
│              │     │ CORPT00C     │     │ CUSTDAT      │
│              │     │ CBSTM03A/B   │     │ (No online   │
│              │     │ CBTRN03C     │     │  CRUD pgms)  │
│              │     └──────────────┘     └──────────────┘
└──────────────┘
```

### Relationship Types

| Upstream Context | Downstream Context | Relationship | Pattern |
|---|---|---|---|
| IAM | All | **Auth Provider** | Published Language (JWT tokens) |
| Account | Card | **Shared Kernel** | Card references Account via acct_id |
| Account | Transaction | **Customer-Supplier** | Transaction posting updates account balances |
| Account | Bill Payment | **Customer-Supplier** | Bill payment updates account balance |
| Card | Transaction | **Conformist** | Transaction references card_num for validation |
| Customer | Account | **Published Language** | Account view reads customer data |
| All Data Contexts | Reporting | **Open Host Service** | Reporting reads from all contexts |

---

## Recommended Service Boundaries

| Service | Database Tables | Source Programs | API Endpoints |
|---|---|---|---|
| **iam-service** | users, roles, sessions | COSGN00C, COUSR00-03C, COADM01C, COMEN01C | POST /auth/login, GET/POST/PUT/DELETE /users |
| **account-service** | accounts, card_xref | COACTVWC, COACTUPC | GET/PUT /accounts/{id}, GET /accounts/{id}/cards |
| **card-service** | cards | COCRDLIC, COCRDSLC, COCRDUPC | GET /cards (paginated), GET/PUT /cards/{num} |
| **transaction-service** | transactions, daily_transactions, tran_cat_balances, rejected_transactions | COTRN00-02C, CBTRN02C, CBACT04C, CBTRN03C | GET /transactions (paginated), POST /transactions, POST /batch/post, POST /batch/interest |
| **billing-service** | (uses transactions + accounts) | COBIL00C | POST /payments |
| **customer-service** | customers | (no dedicated COBOL programs) | GET /customers/{id} |
| **reporting-service** | statements (output), report_jobs | CORPT00C, CBSTM03A/B, CBTRN03C | POST /reports, GET /statements/{id} |
| **batch-scheduler** | job_executions, job_steps | JCL orchestration | POST /jobs/{type}/run, GET /jobs/{id}/status |

---

## Extraction Priority & Sequencing

| Priority | Context | Rationale |
|---|---|---|
| **P0** | IAM | Prerequisite for all other services — provides authentication |
| **P1** | Customer | Lowest coupling, read-only consumers, simple data model |
| **P1** | Account | Core entity, but well-defined boundaries. Extract with XREF ownership |
| **P2** | Card | Depends on Account for cross-reference relationship |
| **P2** | Transaction (Online) | Depends on Card for validation, Account for balance context |
| **P3** | Bill Payment | Depends on Account and Transaction services being available |
| **P3** | Transaction (Batch) | Highest coupling — needs Account, Card, and Transaction services |
| **P4** | Reporting | Depends on all other services — extract last |
