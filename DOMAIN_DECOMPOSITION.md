# Domain Decomposition

## Overview

This document identifies bounded contexts within the CardDemo mainframe application and analyzes extraction seams — the natural boundaries where the monolithic system can be decomposed into independent services.

---

## Bounded Contexts

### BC-1: Identity & Access

**Responsibility:** User authentication, session establishment, role-based access control.

| Artifact | Type | Role |
|----------|------|------|
| `COSGN00C.cbl` | CICS Online | Sign-on validation against USRSEC |
| `COUSR00C.cbl` | CICS Online | List all users (admin) |
| `COUSR01C.cbl` | CICS Online | Add new user |
| `COUSR02C.cbl` | CICS Online | Update user details |
| `COUSR03C.cbl` | CICS Online | Delete user |
| `CSUSR01Y.cpy` | Copybook | SEC-USER-DATA record layout |
| `USRSEC` | VSAM KSDS | User credentials + role store |

**Aggregate Root:** `SEC-USER-DATA` (keyed on `SEC-USR-ID`)

**Domain Events (implicit):**
- UserAuthenticated
- UserCreated / UserUpdated / UserDeleted

**External Dependencies:** None — self-contained context.

---

### BC-2: Customer Management

**Responsibility:** Customer profile lifecycle — creation, update, inquiry.

| Artifact | Type | Role |
|----------|------|------|
| `CBCUS01C.cbl` | Batch | Read customer file |
| `CVCUS01Y.cpy` | Copybook | CUSTOMER-RECORD (500-byte, 23 fields) |
| `CUSTDAT` | VSAM KSDS | Customer master file |

**Aggregate Root:** `CUSTOMER-RECORD` (keyed on `CUST-ID`, PIC 9(09))

**Key Attributes:**
- Personal info (name, address, phone, SSN, DOB)
- Financial identifiers (EFT account, FICO score)
- Primary cardholder indicator

**External Dependencies:**
- Referenced by Account context (via XREF)
- Referenced by Card context (via XREF)

---

### BC-3: Account Management

**Responsibility:** Account lifecycle — open, update, view, status changes, credit limits.

| Artifact | Type | Role |
|----------|------|------|
| `COACTUPC.cbl` | CICS Online (4,236 LOC) | Account update with full validation |
| `COACTVWC.cbl` | CICS Online | Account view |
| `CBACT01C.cbl` | Batch | Read account file, write subsets |
| `CBACT02C.cbl` | Batch | Account data processing |
| `CBACT03C.cbl` | Batch | Account data processing |
| `CBACT04C.cbl` | Batch | Account data processing |
| `CVACT01Y.cpy` | Copybook | ACCOUNT-RECORD (300-byte) |
| `CVACT02Y.cpy` | Copybook | Account detail layout |
| `CVACT03Y.cpy` | Copybook | Account supplementary data |
| `ACCTDAT` | VSAM KSDS | Account master file |

**Aggregate Root:** `ACCOUNT-RECORD` (keyed on `ACCT-ID`, PIC 9(11))

**Key Attributes:**
- Financial state (current balance, credit limit, cash credit limit)
- Lifecycle dates (open, expiration, reissue)
- Cycle accumulators (current cycle credit/debit)
- Group and geographic identifiers

**Invariants:**
- Balance must not exceed credit limit (enforced in `COACTUPC`)
- Status transitions are validated (active/inactive/closed)

**External Dependencies:**
- Owns relationship to Cards (via CCXREF)
- Written to by Transaction Processing (balance updates)
- Read by Billing (for payment processing)

---

### BC-4: Card Management

**Responsibility:** Credit card lifecycle — issuance, listing, detail view, updates.

| Artifact | Type | Role |
|----------|------|------|
| `COCRDLIC.cbl` | CICS Online (1,459 LOC) | List cards (admin: all; user: by account) |
| `COCRDSLC.cbl` | CICS Online | Card detail view |
| `COCRDUPC.cbl` | CICS Online (1,560 LOC) | Card update |
| `CVCRD01Y.cpy` | Copybook | Card work areas and navigation |
| `CARDDAT` | VSAM KSDS | Card master file |
| `CARDAIX` | VSAM AIX Path | Alternate index (card → account) |
| `CCXREF` | VSAM KSDS | Card-to-account cross-reference |
| `CXACAIX` | VSAM AIX Path | XREF alternate index (account → cards) |

**Aggregate Root:** Card entity (keyed on card number, PIC 9(16))

**Cross-Reference Entity:** CCXREF links Card ↔ Account ↔ Customer (the central relationship nexus of the system).

**External Dependencies:**
- Reads Account data for context display
- XREF is shared with Account context (ownership boundary is contested — see Seam Analysis)

---

### BC-5: Transaction Processing

**Responsibility:** Capture, categorize, post, and accumulate financial transactions.

| Artifact | Type | Role |
|----------|------|------|
| `COTRN00C.cbl` | CICS Online (699 LOC) | List transactions |
| `COTRN01C.cbl` | CICS Online | Transaction detail view |
| `COTRN02C.cbl` | CICS Online | Add new transaction |
| `CBTRN01C.cbl` | Batch (494 LOC) | Post daily transactions (simple) |
| `CBTRN02C.cbl` | Batch (731 LOC) | Post daily transactions (with categorization) |
| `CVTRA05Y.cpy` | Copybook | TRAN-RECORD (350-byte) |
| `CVTRA01Y.cpy` | Copybook | TRAN-CAT-BAL-RECORD (category balance) |
| `TRANSACT` | VSAM KSDS | Transaction master file |
| `DALYTRAN` | Sequential | Daily transaction input file |
| `TCATBALF` | VSAM KSDS | Transaction category balance file |

**Aggregate Root:** `TRAN-RECORD` (keyed on `TRAN-ID`, PIC X(16))

**Value Objects:**
- Transaction Category Balance (`TRAN-CAT-BAL-RECORD`)
- Daily Transaction (sequential input record)

**Invariants:**
- Posted transactions update both TRANSACT and TCATBALF atomically
- Daily posting must be idempotent (processed flag in DALYTRAN)

**External Dependencies:**
- Reads CCXREF to resolve card → account mapping
- Updates Account balance (cross-context write — critical coupling)
- Source data for Reporting context

---

### BC-6: Billing & Payments

**Responsibility:** Process bill payments — pay account balance and record payment transaction.

| Artifact | Type | Role |
|----------|------|------|
| `COBIL00C.cbl` | CICS Online (572 LOC) | Bill payment processing |
| Uses TRANSACT, ACCTDAT | VSAM | Cross-aggregate writes |

**Aggregate Root:** None — this is a domain service (orchestrates Transaction + Account).

**External Dependencies:**
- Writes to Transaction context (creates payment transaction)
- Writes to Account context (updates balance)
- This is a **coordination point**, not an independent aggregate.

---

### BC-7: Reporting & Statements

**Responsibility:** Generate transaction reports and account statements.

| Artifact | Type | Role |
|----------|------|------|
| `CORPT00C.cbl` | CICS Online (649 LOC) | Submit batch report job via TDQ |
| `CBTRN03C.cbl` | Batch (649 LOC) | Transaction detail report |
| `CBSTM03A.CBL` | Batch (924 LOC) | Account statements (text + HTML) |
| `CBSTM03B.CBL` | Batch (230 LOC) | Statement variant |

**Aggregate Root:** None — read-only projection of Transaction and Account data.

**External Dependencies:**
- Reads TRANSACT, ACCTDAT (read-only)
- Writes to print spool / HTML files (output-only)

---

### BC-8: Batch Orchestration & Infrastructure

**Responsibility:** File management, scheduling, system utilities.

| Artifact | Type | Role |
|----------|------|------|
| `CLOSEFIL.jcl` | JCL | Close CICS files for batch processing |
| `OPENFIL.jcl` | JCL | Reopen CICS files after batch |
| `TRANBKP.jcl` | JCL | Transaction file backup |
| `WAITSTEP.jcl` | JCL | Timer wait between batch steps |
| `INTCALC.jcl` | JCL | Monthly interest calculation |
| `COMBTRAN.jcl` | JCL | Combine/consolidate transactions |
| `DISCGRP.jcl` | JCL | Disclosure group refresh |
| `COBSWAIT.cbl` | COBOL | Assembler wait utility |
| `CSUTLDTC.cbl` | COBOL | Date conversion utility |
| `CardDemo.controlm` | Control-M XML | Schedule definitions |

**Scheduling Flows (from Control-M):**
1. **DAILY-TransactionBackup:** CLOSEFIL → TRANBKP → WAITSTEP → OPENFIL
2. **WEEKLY-TransactionTypesDBRefresh:** MNTTRDB2 → (CLOSEFIL → DISCGRP → WAITSTEP → OPENFIL)
3. **WEEKLY-TransactionTypesDBRefresh (extract):** TRANEXTR
4. **MONTHLY-InterestCalculation:** CLOSEFIL → INTCALC → COMBTRAN → WAITSTEP → OPENFIL

---

### BC-9: Data Migration (Branch Export/Import)

**Responsibility:** Bulk data export/import for branch consolidation.

| Artifact | Type | Role |
|----------|------|------|
| `CBEXPORT.cbl` | Batch (582 LOC) | Export customer profiles to multi-record file |
| `CBIMPORT.cbl` | Batch (487 LOC) | Import and split multi-record file |
| `CVEXPORT.cpy` | Copybook | Export record layout |

**External Dependencies:**
- Reads/writes all master files (Customer, Account, Card-XREF, Transaction)
- Self-contained utility — no runtime coupling to online system.

---

### BC-10: Authorization (Optional — IMS/DB2/MQ)

**Responsibility:** Asynchronous credit card authorization via message queuing.

| Artifact | Type | Role |
|----------|------|------|
| `app/app-authorization-ims-db2-mq/cbl/` | COBOL | Authorization request/response processing |
| `app/app-authorization-ims-db2-mq/ims/` | IMS DBD/PSB | Hierarchical data definitions |
| `app/app-authorization-ims-db2-mq/ddl/` | DB2 DDL | Authorization tables |
| MQ Queues | IBM MQ | Request/response channels |

**External Dependencies:**
- Reads Customer data from IMS DB
- Logs to DB2 tables
- Communicates via MQ (asynchronous)

---

## Extraction Seam Analysis

### Seam 1: CICS Transaction Boundary (Primary Seam)

**Location:** Each CICS program is invoked via a unique transaction ID (CC00, CA00, CT00, etc.)
**Mechanism:** Programs communicate exclusively through COMMAREA (`COCOM01Y.cpy`)
**Why it's a seam:**
- COMMAREA is a fixed, well-documented contract (47 lines, ~120 bytes)
- Each transaction maps to exactly one program
- No shared state except COMMAREA + VSAM files

**Extraction Pattern:**
```
3270 Terminal → CICS TranID → COBOL Program → VSAM
         ↓ (replace with)
Browser/API → API Gateway → Java Service → RDBMS
```

**Risk:** COMMAREA carries cross-context data (customer + account + card info in one struct). Services must agree on a shared DTO during transition.

---

### Seam 2: VSAM File Boundaries (Data Seam)

**Location:** Each VSAM KSDS file is defined separately in CSD with independent access.

| File | Primary Owner | Secondary Readers | Secondary Writers |
|------|--------------|-------------------|-------------------|
| USRSEC | Identity & Access | — | — |
| CUSTDAT | Customer Mgmt | Card, Export | Import |
| ACCTDAT | Account Mgmt | Billing, Reporting, Export | Txn Processing, Billing |
| CARDDAT | Card Mgmt | — | — |
| CCXREF | Card Mgmt | Txn Processing, Export | Import |
| TRANSACT | Txn Processing | Reporting, Export | Billing |
| TCATBALF | Txn Processing | Reporting | — |
| DALYTRAN | Txn Processing (input) | — | External feed |

**Why it's a seam:**
- Each file has a clear primary owner
- Cross-context access is read-heavy (except Billing → Account, Txn → Account)
- VSAM → RDBMS table is a natural 1:1 mapping

**Contested Boundaries:**
- **CCXREF** is the most coupled artifact — it's the join table between Customer, Account, and Card. Ownership should move to a shared "Relationship" service or be denormalized into each context.
- **ACCTDAT** is written by three contexts (Account, Txn, Billing). Needs an owning service with command interface.

---

### Seam 3: Online/Batch Boundary

**Location:** The daily CLOSEFIL/OPENFIL cycle creates a temporal seam.
**Mechanism:** CICS files are closed for batch exclusive access, then reopened.

**Why it's a seam:**
- Batch programs never run concurrently with online programs on the same files
- Batch operations are naturally idempotent (rerunnable after failure)
- Each batch job has a clear JCL-defined input/output contract

**Extraction Pattern:**
- Replace CLOSEFIL/OPENFIL with database transaction isolation
- Replace sequential file I/O with database queries or event streams
- Replace Control-M scheduling with cloud-native orchestration

---

### Seam 4: Menu/Navigation Layer (UI Seam)

**Location:** `COADM01C` and `COMEN01C` are pure routing programs.
**Mechanism:** They set `CCARD-NEXT-PROG` in COMMAREA and XCTL to the target program.

**Why it's a seam:**
- Zero business logic — only PF-key → program mapping
- Removing BMS maps removes the entire presentation layer in one cut
- Modern UI consumes APIs directly; no menu program equivalent needed

---

### Seam 5: Asynchronous Messaging (MQ Seam)

**Location:** `app/app-authorization-ims-db2-mq/` module.
**Mechanism:** Authorization requests flow through IBM MQ queues.

**Why it's a seam:**
- Already decoupled via async messaging — natural microservice boundary
- Queue interface is the contract; internal implementation can be replaced transparently
- Can migrate queue-by-queue (request queue first, then response queue)

---

## Context Map

```
┌───────────────┐       ┌──────────────────┐
│  Identity &   │       │    Customer      │
│    Access     │       │   Management     │
└───────┬───────┘       └────────┬─────────┘
        │ authenticates           │ owns
        ▼                         ▼
┌───────────────┐       ┌──────────────────┐
│  Navigation   │       │   Card Mgmt      │◄──── CCXREF ────┐
│   (UI Shell)  │       └────────┬─────────┘                  │
└───────────────┘                │ linked via                  │
                                 ▼                             │
                        ┌──────────────────┐                  │
                        │    Account       │◄─────────────────┘
                        │   Management     │
                        └────────┬─────────┘
                                 │ balance updated by
                                 ▼
┌───────────────┐       ┌──────────────────┐
│   Billing &   │──────►│  Transaction     │
│   Payments    │       │  Processing      │
└───────────────┘       └────────┬─────────┘
                                 │ feeds
                                 ▼
                        ┌──────────────────┐
                        │   Reporting &    │
                        │   Statements     │
                        └──────────────────┘

┌───────────────┐       ┌──────────────────┐
│ Batch Orch.   │       │ Data Migration   │
│ (Infra)       │       │ (Export/Import)  │
└───────────────┘       └──────────────────┘

┌──────────────────────────────────┐
│ Authorization (MQ/IMS/DB2)       │
│ [Optional - already decoupled]   │
└──────────────────────────────────┘
```

---

## Coupling Analysis Summary

| From → To | Coupling Type | Strength | Decoupling Strategy |
|-----------|--------------|----------|---------------------|
| Txn Processing → Account | Write (balance update) | **Strong** | Domain events + eventual consistency |
| Billing → Account | Write (balance update) | **Strong** | Command via Account Service API |
| Billing → Transaction | Write (payment record) | **Strong** | Saga pattern (orchestrated) |
| Card Mgmt → CCXREF | Shared data | **Medium** | Relationship microservice or denormalize |
| Txn Processing → CCXREF | Read (card→account lookup) | **Medium** | Read replica / cache in Txn service |
| Reporting → Txn + Account | Read | **Weak** | CQRS read model / materialized view |
| Export/Import → All files | Read/Write | **Medium** | Bulk API endpoints on each service |
| Navigation → All programs | Control flow | **Weak** | Eliminated by new UI |
| All Online → COMMAREA | Shared contract | **Medium** | Versioned API contracts per service |

---

## Recommended Extraction Order

1. **Identity & Access** — zero external writers, self-contained
2. **Reporting & Statements** — read-only, no writers depend on it
3. **Data Migration (Export/Import)** — utility, no runtime coupling
4. **Customer Management** — few direct mutations from other contexts
5. **Card Management** (with CCXREF ownership) — moderately coupled
6. **Account Management** — heavily written to; requires event infrastructure first
7. **Billing & Payments** — coordination service; depends on Account + Txn being available
8. **Transaction Processing** — core financial engine; extract last due to write coupling
9. **Authorization (MQ module)** — parallel track, already decoupled
10. **Batch Orchestration** — dissolves into scheduled triggers for each service
