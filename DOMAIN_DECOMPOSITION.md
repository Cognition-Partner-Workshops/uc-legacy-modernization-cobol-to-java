# CardDemo Domain Decomposition

## Overview

This document identifies bounded contexts within the CardDemo mainframe application using Domain-Driven Design (DDD) principles, maps data ownership and coupling, and provides extraction seam analysis for each context. The goal is to decompose the monolithic COBOL/CICS/VSAM system into well-defined service boundaries suitable for a microservices or modular-monolith architecture.

---

## Entity-Relationship Map (Current State)

```
┌─────────────┐       ┌─────────────┐       ┌─────────────┐
│   USRSEC    │       │   CUSTDAT   │       │   ACCTDAT   │
│  (Security) │       │  (Customer) │       │  (Account)  │
│─────────────│       │─────────────│       │─────────────│
│ USR-ID  (PK)│       │ CUST-ID (PK)│◄──┐   │ ACCT-ID (PK)│
│ USR-FNAME   │       │ FIRST-NAME  │   │   │ ACTIVE-STAT │
│ USR-LNAME   │       │ LAST-NAME   │   │   │ CURR-BAL    │
│ USR-PWD     │       │ ADDR-*      │   │   │ CREDIT-LIM  │
│ USR-TYPE    │       │ PHONE-*     │   │   │ OPEN-DATE   │
│ (80 bytes)  │       │ SSN, DOB    │   │   │ GROUP-ID    │
└─────────────┘       │ FICO-SCORE  │   │   │ (300 bytes) │
                      │ (500 bytes) │   │   └──────┬──────┘
                      └─────────────┘   │          │
                                        │          │
                      ┌─────────────────┴──────────┴──────────┐
                      │          CARDXREF (Cross-Reference)    │
                      │─────────────────────────────────────── │
                      │ XREF-CARD-NUM (PK)  ──────────────┐   │
                      │ XREF-CUST-ID (FK → CUSTDAT)       │   │
                      │ XREF-ACCT-ID (FK → ACCTDAT)       │   │
                      │ (50 bytes)                         │   │
                      └────────────────────────────────────┘   │
                                                               │
                      ┌─────────────┐                          │
                      │   CARDDAT   │◄─────────────────────────┘
                      │   (Card)    │
                      │─────────────│
                      │ CARD-NUM(PK)│
                      │ CARD-ACCT-ID│──► ACCTDAT
                      │ CVV-CD      │
                      │ EMBOSS-NAME │
                      │ EXPIRY-DATE │
                      │ STATUS      │
                      │ (150 bytes) │
                      └─────────────┘

┌─────────────┐       ┌─────────────┐       ┌─────────────┐
│  TRANSACT   │       │  TCATBALF   │       │   DISCGRP   │
│(Transaction)│       │ (Cat.Bal.)  │       │ (Disc.Group)│
│─────────────│       │─────────────│       │─────────────│
│ TRAN-ID (PK)│       │ ACCT-ID     │       │ GROUP-ID    │
│ TYPE-CD     │       │ TYPE-CD     │       │ TRAN-TYPE   │
│ CAT-CD      │       │ CAT-CD      │       │ TRAN-CAT    │
│ AMT         │       │ BALANCE     │       │ INT-RATE    │
│ MERCHANT-*  │       │ (50 bytes)  │       │ (50 bytes)  │
│ CARD-NUM    │──►    └─────────────┘       └─────────────┘
│ TIMESTAMPS  │
│ (350 bytes) │       ┌─────────────┐
└─────────────┘       │  DALYTRAN   │
                      │(Daily Input)│
                      │─────────────│
                      │ Same layout │
                      │ as TRANSACT │
                      │ (350 bytes) │
                      └─────────────┘
```

---

## Bounded Contexts

### BC-1: Identity & Access Management (IAM)

**Aggregate Root:** User (SEC-USER-DATA)

**Owned Entities:**
- User account (USRSEC VSAM file)
- User roles/types (Admin 'A', Regular 'U')

**Programs:**
| Program | Function | VSAM Files | Coupling |
|---|---|---|---|
| `COSGN00C` | Authentication/signon | USRSEC (read) | Writes to COMMAREA: user-id, user-type |
| `COUSR00C` | User list | USRSEC (browse) | Admin-only access check |
| `COUSR01C` | User add | USRSEC (write) | None |
| `COUSR02C` | User update | USRSEC (read/write) | None |
| `COUSR03C` | User delete | USRSEC (delete) | None |

**Data Ownership:** Full ownership of USRSEC file. No other context writes to this file.

**Inbound Dependencies:** None — this is the entry point for all sessions.

**Outbound Dependencies:**
- `COSGN00C` sets `CDEMO-USER-ID` and `CDEMO-USER-TYPE` in COMMAREA, consumed by every downstream program for authorization decisions.
- XCTL to `COMEN01C` (regular users) or `COADM01C` (admin users) after successful login.

**Extraction Seam Analysis:**
- **Seam Type:** Clean boundary. USRSEC is exclusively owned by this context.
- **Seam Location:** The COMMAREA fields `CDEMO-USER-ID` and `CDEMO-USER-TYPE` are the sole integration point. Replace with JWT token claims.
- **Extraction Difficulty:** Low. No shared data writes. The only coupling is the identity propagation via COMMAREA.
- **Anti-Corruption Layer:** New IAM service issues JWT tokens. Legacy COMMAREA population can be bridged by a thin adapter during transition.

---

### BC-2: Customer Management

**Aggregate Root:** Customer (CUSTOMER-RECORD)

**Owned Entities:**
- Customer profile (CUSTDAT VSAM file — 500-byte record)
- Customer demographics (name, address, SSN, DOB, FICO score)

**Programs:**
| Program | Function | VSAM Files | Coupling |
|---|---|---|---|
| `COACTUPC` | Account update (reads customer) | CUSTDAT (read), ACCTDAT, CARDDAT | Tight coupling with Account context |
| `COACTVWC` | Account view (reads customer) | CUSTDAT (read), ACCTDAT | Read-only dependency |
| `CBCUS01C` | Batch customer processing | CUSTDAT | Batch operations |

**Data Ownership:** Owns CUSTDAT file. However, no dedicated online CRUD screens exist for customer data — customer fields are displayed/edited through account screens.

**Inbound Dependencies:**
- Account View (`COACTVWC`) reads customer name and address for display.
- Account Update (`COACTUPC`) reads/validates customer fields (SSN, DOB, phone, FICO) during account update.

**Outbound Dependencies:**
- Card cross-reference links customer to cards and accounts.

**Extraction Seam Analysis:**
- **Seam Type:** Shared data boundary. Customer data is read by Account Management programs but has no dedicated UI.
- **Seam Location:** The `CUSTDAT` file reads in `COACTVWC` (line ~800+, `9000-READ-ACCT` paragraph) and `COACTUPC` (customer field validation sections).
- **Extraction Difficulty:** Medium. Customer data is interleaved with account data on the same BMS screens. Extracting requires either:
  - (a) A Customer API that Account Management calls for customer data, or
  - (b) Keeping Customer as a sub-context of Account Management initially, splitting later.
- **Anti-Corruption Layer:** Expose `CustomerService.getById(custId)` API. Account screens call this instead of direct VSAM reads.
- **Recommendation:** Initially keep Customer as part of a broader Account context, then extract as a separate service when the UI is redesigned.

---

### BC-3: Account & Card Management

**Aggregate Root:** Account (ACCOUNT-RECORD)

**Owned Entities:**
- Account master (ACCTDAT — 300-byte record)
- Card records (CARDDAT — 150-byte record)
- Card cross-reference (CARDXREF — 50-byte record linking card↔customer↔account)

**Programs:**
| Program | Function | VSAM Files | Coupling |
|---|---|---|---|
| `COACTVWC` | Account view | ACCTDAT, CUSTDAT, CARDDAT, CARDXREF | Reads from 4 files |
| `COACTUPC` | Account update | ACCTDAT, CUSTDAT, CARDDAT, CARDXREF | Reads/writes; 4,237 LOC |
| `COCRDLIC` | Card list | CARDDAT, CARDAIX | Browse by card or account path |
| `COCRDSLC` | Card detail view | CARDDAT | Read-only |
| `COCRDUPC` | Card update | CARDDAT | Write |
| `CBACT01C`–`CBACT03C` | Batch account processing | ACCTDAT | Batch |

**Data Ownership:** Owns ACCTDAT, CARDDAT, and CARDXREF files. ACCTDAT is also written by Bill Payment and Batch Transaction Posting (cross-context writes — a key coupling point).

**Inbound Dependencies:**
- Bill Payment (`COBIL00C`) reads and updates `ACCT-CURR-BAL` in ACCTDAT.
- Batch Transaction Posting (`CBTRN02C`) updates `ACCT-CURR-BAL`, `ACCT-CURR-CYC-CREDIT`, `ACCT-CURR-CYC-DEBIT` in ACCTDAT.
- Interest Calculation (`CBACT04C`) updates `ACCT-CURR-BAL` and resets cycle amounts.
- Transaction view reads card number from CARDXREF for display.

**Outbound Dependencies:**
- Reads Customer data from CUSTDAT (BC-2) for account view/update screens.

**Extraction Seam Analysis:**
- **Seam Type:** Core domain with multiple inbound writers. This is the most coupled context.
- **Seam Location:** The critical coupling point is `ACCT-CURR-BAL` in ACCTDAT, which is written by three different contexts (Account Update, Bill Payment, Batch Posting/Interest). In CICS, these are protected by VSAM record-level locking.
- **Extraction Difficulty:** High. Multiple writers to ACCTDAT create the biggest decomposition challenge. Options:
  1. **Account Balance as internal service:** All balance modifications go through `AccountService.adjustBalance()`, enforcing single-writer semantics.
  2. **Event-driven updates:** Bill Payment and Batch Posting emit events; Account service processes them.
  3. **Saga pattern:** Cross-context transactions use compensating actions.
- **Anti-Corruption Layer:** `AccountService` exposes:
  - `getAccount(acctId)` — replaces CICS READ ACCTDAT
  - `updateAccount(acctId, fields)` — replaces CICS REWRITE ACCTDAT
  - `adjustBalance(acctId, amount, type)` — replaces direct balance writes from other contexts
- **Card Sub-Context:** Cards can be a separate sub-service within this context. CARDDAT has a clean boundary (card CRUD) with the cross-reference providing the link to accounts.

---

### BC-4: Transaction Management

**Aggregate Root:** Transaction (TRAN-RECORD)

**Owned Entities:**
- Transaction master (TRANSACT — 350-byte record)
- Daily transactions (DALYTRAN — 350-byte input file)
- Daily rejects (DALYREJS)
- Transaction category balances (TCATBALF — 50-byte record)

**Programs:**
| Program | Function | VSAM Files | Coupling |
|---|---|---|---|
| `COTRN00C` | Transaction list (online) | TRANSACT (browse) | Read-only |
| `COTRN01C` | Transaction view (online) | TRANSACT (read) | Read-only |
| `COTRN02C` | Transaction add (online) | TRANSACT (write) | Also reads CARDXREF, ACCTDAT |
| `CBTRN02C` | Batch posting | DALYTRAN→TRANSACT, ACCTDAT, XREFFILE, TCATBALF | Heavy cross-file writes |
| `CBTRN01C` | Batch transaction processing | TRANSACT | Batch |
| `CBTRN03C` | Transaction report (batch) | TRANSACT (read) | Read-only |

**Data Ownership:** Owns TRANSACT, DALYTRAN, DALYREJS, and TCATBALF files. However, writes to ACCTDAT during posting (cross-boundary).

**Inbound Dependencies:**
- Bill Payment (`COBIL00C`) writes payment transactions to TRANSACT.
- Interest Calculation (`CBACT04C`) writes interest transactions to TRANSACT.

**Outbound Dependencies:**
- Batch posting (`CBTRN02C`) reads XREFFILE and writes to ACCTDAT (BC-3).
- Online add (`COTRN02C`) reads CARDXREF for card validation.

**Extraction Seam Analysis:**
- **Seam Type:** Core domain with bidirectional coupling to Account Management.
- **Seam Location:**
  - *Online read path:* Clean seam. `COTRN00C` and `COTRN01C` only browse/read TRANSACT — no cross-file dependencies.
  - *Online write path:* `COTRN02C` reads CARDXREF (lookups can become API calls).
  - *Batch posting:* `CBTRN02C` is the tightest coupling point — it reads DALYTRAN, validates against XREFFILE, writes to TRANSACT, and updates ACCTDAT and TCATBALF. This is a transaction saga spanning two bounded contexts.
- **Extraction Difficulty:** Medium for online, High for batch.
- **Anti-Corruption Layer:**
  - `TransactionService.list(filters)` / `.getById(tranId)` — replaces TRANSACT browse/read.
  - `TransactionService.addTransaction(tran)` — replaces TRANSACT write + ACCTDAT balance update (calls `AccountService.adjustBalance()` internally).
  - Batch posting becomes a Spring Batch job that calls `TransactionService` and `AccountService` APIs.

---

### BC-5: Billing & Payments

**Aggregate Root:** Payment (payment transaction within TRAN-RECORD)

**Owned Entities:**
- Bill payment logic (no dedicated data store — writes to TRANSACT and ACCTDAT)

**Programs:**
| Program | Function | VSAM Files | Coupling |
|---|---|---|---|
| `COBIL00C` | Bill payment | TRANSACT (write), ACCTDAT (read/update), CXACAIX (read) | Cross-context writes |

**Data Ownership:** None — Bill Payment is a process-centric context that orchestrates writes to Transaction (TRANSACT) and Account (ACCTDAT) data owned by other contexts.

**Extraction Seam Analysis:**
- **Seam Type:** Orchestration process spanning BC-3 (Account) and BC-4 (Transaction).
- **Seam Location:** `COBIL00C` lines 210–243. The payment flow:
  1. Read CXACAIX to get card number for account (→ Account context)
  2. Read ACCTDAT to get current balance (→ Account context)
  3. Generate new TRAN-ID by reading last ID from TRANSACT (→ Transaction context)
  4. Write payment transaction to TRANSACT (→ Transaction context)
  5. Update ACCT-CURR-BAL in ACCTDAT (→ Account context)
- **Extraction Difficulty:** Medium. The five-step flow crosses two bounded contexts. Requires:
  - Saga or choreography pattern for consistency.
  - Or: `PaymentService` calls `AccountService.adjustBalance()` and `TransactionService.addTransaction()` within a single database transaction (if using shared database) or via saga (if separate databases).
- **Anti-Corruption Layer:** `PaymentService.payBill(acctId)` orchestrates the flow, calling Account and Transaction services.

---

### BC-6: Interest & Fee Calculation

**Aggregate Root:** Disclosure Group (DIS-GROUP-RECORD)

**Owned Entities:**
- Disclosure/interest rate groups (DISCGRP — 50-byte record)
- Calculation logic

**Programs:**
| Program | Function | VSAM Files | Coupling |
|---|---|---|---|
| `CBACT04C` | Interest calculation | TCATBALF (read), DISCGRP (read), XREFFILE (read), ACCTDAT (read/write), TRANSACT (write) | Heavy cross-file |

**Data Ownership:** Owns DISCGRP file (interest rate definitions). Reads from Transaction (TCATBALF) and writes to Account (ACCTDAT) and Transaction (TRANSACT).

**Extraction Seam Analysis:**
- **Seam Type:** Batch computation process with cross-context side effects.
- **Seam Location:** `CBACT04C` paragraphs `1200-GET-INTEREST-RATE` (rate lookup), `1300-COMPUTE-INTEREST` (calculation), `1050-UPDATE-ACCOUNT` (account balance update).
- **Extraction Difficulty:** Medium. The rate lookup and computation are pure functions that extract cleanly. The coupling is in the output writes (account balance update, interest transaction generation).
- **Anti-Corruption Layer:**
  - `InterestRateService.getRate(groupId, tranType, tranCat)` — pure lookup.
  - `InterestCalculationService.calculate(acctId)` — reads category balances, computes interest, then calls `AccountService.adjustBalance()` and `TransactionService.addTransaction()`.

---

### BC-7: Reporting & Statements

**Aggregate Root:** Report Request

**Owned Entities:**
- Report definitions and output (no persistent data store — reads from TRANSACT)
- JCL job templates for report submission

**Programs:**
| Program | Function | VSAM Files | Coupling |
|---|---|---|---|
| `CORPT00C` | Report request/submission | TRANSACT (read via batch) | JCL submission via TDQ |
| `CBSTM03A` | Statement generation (batch) | TRANSACT, ACCTDAT | Read-only |
| `CBSTM03B` | Statement generation (batch) | TRANSACT | Read-only |
| `CBTRN03C` | Transaction report (batch) | TRANSACT | Read-only |

**Data Ownership:** No owned data files. Pure consumers of Transaction and Account data.

**Extraction Seam Analysis:**
- **Seam Type:** Clean read-only boundary. Reports only consume data, never modify it.
- **Seam Location:** The only coupling is the TRANSACT file reads and the mainframe-specific JCL submission mechanism (TDQ write in `CORPT00C`).
- **Extraction Difficulty:** Low. Reports can query the new transaction database directly or via read-replicas/views. No write coupling.
- **Anti-Corruption Layer:** Reporting service queries transaction and account APIs/database views. Replace TDQ-based job submission with REST API triggers or scheduled jobs.

---

## Cross-Context Coupling Matrix

This matrix shows which VSAM files each bounded context reads (R) or writes (W):

| VSAM File | BC-1 IAM | BC-2 Customer | BC-3 Account | BC-4 Transaction | BC-5 Payment | BC-6 Interest | BC-7 Reporting |
|---|---|---|---|---|---|---|---|
| USRSEC | R/W | | | | | | |
| CUSTDAT | | R/W | R | | | | |
| ACCTDAT | | | R/W | **W** | **W** | **W** | R |
| CARDDAT | | | R/W | | | | |
| CARDXREF | | | R/W | R | R | R | |
| TRANSACT | | | | R/W | **W** | **W** | R |
| DALYTRAN | | | | R | | | |
| DALYREJS | | | | W | | | |
| TCATBALF | | | | R/W | | R | |
| DISCGRP | | | | | | R | |

**Bold W** = Cross-context write (coupling hotspot requiring careful decomposition)

---

## Coupling Hotspot Analysis

### Hotspot 1: ACCTDAT (Account Balance)

**Writers:** BC-3 (Account Update), BC-4 (Batch Posting), BC-5 (Bill Payment), BC-6 (Interest Calculation)

**Field:** `ACCT-CURR-BAL` (S9(10)V99) — the account current balance

**Resolution Strategy:**
- All balance modifications must flow through `AccountService.adjustBalance(acctId, amount, adjustmentType)`.
- In the interim (dual-running period), use a database-backed balance with optimistic locking.
- Long-term: consider event sourcing for balance changes (every adjustment is an immutable event).

### Hotspot 2: TRANSACT (Transaction Master)

**Writers:** BC-4 (Online Add, Batch Posting), BC-5 (Bill Payment), BC-6 (Interest — generates interest transactions)

**Resolution Strategy:**
- `TransactionService.create(transaction)` is the single write entry point.
- Transaction ID generation (currently MAX+1 from VSAM browse) must be centralized — use database sequences.
- Read path (list/view/report) can use read replicas or CQRS pattern.

### Hotspot 3: CARDXREF (Cross-Reference)

**Readers:** BC-3, BC-4, BC-5, BC-6 (all contexts needing card↔account↔customer mappings)

**Resolution Strategy:**
- CARDXREF is a reference data lookup. Expose as `CardXrefService.lookupByCard(cardNum)` / `.lookupByAccount(acctId)`.
- High read frequency, very low write frequency — good candidate for caching.
- Ownership stays with BC-3 (Account & Card Management).

---

## Recommended Service Boundaries (Target Architecture)

```
┌──────────────────────────────────────────────────────────────────┐
│                        API Gateway                               │
│  (Authentication, Rate Limiting, Routing)                        │
└─────┬──────────┬──────────┬──────────┬──────────┬───────────────┘
      │          │          │          │          │
      ▼          ▼          ▼          ▼          ▼
┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐
│   IAM    │ │ Account  │ │  Trans-  │ │ Payment  │ │Reporting │
│ Service  │ │ Service  │ │ action   │ │ Service  │ │ Service  │
│          │ │          │ │ Service  │ │          │ │          │
│ - Auth   │ │ - Acct   │ │ - Online │ │ - Bill   │ │ - Report │
│ - Users  │ │ - Card   │ │ - Batch  │ │   Pay    │ │ - Stmt   │
│ - Roles  │ │ - Cust.  │ │ - Cat.   │ │          │ │          │
│          │ │ - XRef   │ │   Bal.   │ │          │ │          │
└────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘
     │            │            │            │            │
     ▼            ▼            ▼            ▼            ▼
┌──────────┐ ┌──────────┐ ┌──────────┐    (calls      (reads
│ User DB  │ │Account DB│ │ Trans DB │   Acct+Trans   Trans+
│ (users,  │ │(accounts,│ │(transact,│    services)   Acct DB)
│  roles)  │ │ cards,   │ │ daily,   │
│          │ │ customers│ │ rejects, │
│          │ │ xref)    │ │ catbal,  │
│          │ │          │ │ discgrp) │
└──────────┘ └──────────┘ └──────────┘

                    ┌──────────────────────────┐
                    │   Interest Calculation    │
                    │   (Scheduled Batch Job)   │
                    │                          │
                    │ Reads: Trans DB (catbal) │
                    │        Account DB (disc) │
                    │ Writes: via Account +    │
                    │         Transaction APIs │
                    └──────────────────────────┘
```

---

## Extraction Seam Summary

| Bounded Context | Seam Cleanliness | Extraction Order | Key Challenge |
|---|---|---|---|
| BC-1: IAM | Clean | 1st | Replace COMMAREA-based identity with JWT |
| BC-7: Reporting | Clean (read-only) | 2nd | Replace TDQ/JCL submission with job API |
| BC-2: Customer | Embedded in Account screens | Defer (extract with BC-3) | No dedicated UI; interleaved with Account |
| BC-3: Account & Card | Multiple inbound writers | 3rd | Four contexts write to ACCTDAT |
| BC-5: Billing & Payments | Orchestration process | 4th (with BC-3 and BC-4) | Saga across Account and Transaction |
| BC-4: Transaction | Bidirectional with Account | 5th | Batch posting spans two contexts |
| BC-6: Interest Calculation | Batch with cross-writes | 6th | Depends on Account + Transaction services |

---

## COMMAREA as Integration Point

The `CARDDEMO-COMMAREA` (defined in `COCOM01Y.cpy`) is the shared state mechanism across all CICS programs:

```
CARDDEMO-COMMAREA (approx. 200 bytes)
├── CDEMO-GENERAL-INFO
│   ├── FROM-TRANID (4)      — Source transaction
│   ├── FROM-PROGRAM (8)     — Source program
│   ├── TO-TRANID (4)        — Target transaction
│   ├── TO-PROGRAM (8)       — Target program
│   ├── USER-ID (8)          — Logged-in user
│   ├── USER-TYPE (1)        — 'A'dmin or 'U'ser
│   └── PGM-CONTEXT (1)      — 0=enter, 1=reenter
├── CDEMO-CUSTOMER-INFO
│   ├── CUST-ID (9)
│   ├── CUST-FNAME (25)
│   ├── CUST-MNAME (25)
│   └── CUST-LNAME (25)
├── CDEMO-ACCOUNT-INFO
│   ├── ACCT-ID (11)
│   └── ACCT-STATUS (1)
├── CDEMO-CARD-INFO
│   └── CARD-NUM (16)
└── CDEMO-MORE-INFO
    ├── LAST-MAP (7)
    └── LAST-MAPSET (7)
```

**Decomposition Impact:** The COMMAREA is the monolith's "shared session state." In the target architecture:
- `USER-ID` / `USER-TYPE` → JWT token claims
- `CUST-ID` / `ACCT-ID` / `CARD-NUM` → URL path parameters or query parameters in REST APIs
- `FROM-PROGRAM` / `TO-PROGRAM` → Frontend routing (React Router / Angular Router)
- `PGM-CONTEXT` (enter/reenter) → HTTP request state (GET for initial load, POST for form submission)
- `LAST-MAP` / `LAST-MAPSET` → Browser history / frontend state management

Each bounded context should receive only the data it needs via API parameters, not a monolithic shared structure.
