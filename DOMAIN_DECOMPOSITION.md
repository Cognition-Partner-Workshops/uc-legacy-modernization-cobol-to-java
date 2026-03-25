# CardDemo Domain Decomposition

## Overview

This document identifies bounded contexts within the CardDemo COBOL/CICS application and performs extraction seam analysis to determine where the monolithic mainframe system can be cleanly separated into independent, deployable microservices.

---

## Bounded Context Map

```
┌─────────────────────────────────────────────────────────────────────┐
│                        CardDemo System                              │
│                                                                     │
│  ┌──────────────┐   ┌──────────────────┐   ┌───────────────────┐   │
│  │  Identity &   │──▶│  Account &        │──▶│  Transaction      │   │
│  │  Access Mgmt  │   │  Card Mgmt        │   │  Processing       │   │
│  │              │   │                    │   │                   │   │
│  │ COSGN00C    │   │ COACTVWC COACTUPC │   │ COTRN00C COTRN01C│   │
│  │ COADM01C    │   │ COCRDLIC COCRDSLC │   │ COTRN02C          │   │
│  │ COUSR00-03C │   │ COCRDUPC          │   │ CBTRN02C          │   │
│  │              │   │ CBACT01-03C       │   │ CBTRN01C CBTRN03C│   │
│  │ USRSEC      │   │ CBCUS01C          │   │                   │   │
│  │              │   │                    │   │ TRANSACT          │   │
│  │              │   │ ACCTDAT CARDDAT   │   │ DALYTRAN          │   │
│  │              │   │ CUSTDAT CCXREF    │   │ DALYREJS          │   │
│  └──────┬───────┘   └────────┬──────────┘   └─────────┬─────────┘   │
│         │                    │                         │             │
│         │           ┌────────▼──────────┐              │             │
│         │           │  Financial        │◀─────────────┘             │
│         │           │  Operations       │                            │
│         │           │                   │                            │
│         │           │ COBIL00C          │                            │
│         │           │ CBACT04C          │                            │
│         │           │ CBSTM03A/B        │                            │
│         │           │ CORPT00C          │                            │
│         │           │                   │                            │
│         │           │ TCATBAL DISCGRP   │                            │
│         │           └───────────────────┘                            │
│         │                                                            │
│         │           ┌───────────────────┐                            │
│         │           │  Optional:        │                            │
│         │           │  Authorization &  │                            │
│         │           │  Messaging        │                            │
│         │           │                   │                            │
│         │           │ IMS-DB2-MQ module │                            │
│         │           │ DB2 tran types    │                            │
│         │           │ VSAM-MQ module    │                            │
│         │           └───────────────────┘                            │
└─────────────────────────────────────────────────────────────────────┘
```

---

## Bounded Context Definitions

### BC-1: Identity & Access Management

**Domain responsibility:** User authentication, session management, role-based access control, and user CRUD administration.

**Programs:**

| Program | Function | LOC | CICS Trans |
|---|---|---|---|
| `COSGN00C` | Sign-on / authentication | 261 | CC00 |
| `COADM01C` | Admin menu routing | 289 | CA00 |
| `COUSR00C` | User list (paginated) | 696 | CU00 |
| `COUSR01C` | User add | ~400 | CU01 |
| `COUSR02C` | User update | ~400 | CU02 |
| `COUSR03C` | User delete | ~300 | CU03 |

**Data ownership:**

| Entity | VSAM File | Copybook | Record Size |
|---|---|---|---|
| User Security Record | USRSEC | CSUSR01Y | 80 bytes |

**Key fields:** `SEC-USR-ID` (PIC X(8)), `SEC-USR-FNAME`, `SEC-USR-LNAME`, `SEC-USR-PWD` (PIC X(8)), `SEC-USR-TYPE` (A=Admin, U=User)

**Inbound dependencies:** None (this is the entry point)
**Outbound dependencies:** Routes to BC-2 menus (`COMEN01C` for users, `COADM01C` for admins) via COMMAREA

**Extraction seam:** Clean. The USRSEC file is only read by `COSGN00C` for login and managed by `COUSR00-03C`. No other context writes to it. The only coupling is the COMMAREA's `CDEMO-USER-ID` and `CDEMO-USER-TYPE` fields, which become JWT claims in the target architecture.

---

### BC-2: Account & Card Management

**Domain responsibility:** Account lifecycle (view, update), card lifecycle (list, view, update), customer data management, and the cross-reference relationships between accounts, cards, and customers.

**Programs:**

| Program | Function | LOC | CICS Trans |
|---|---|---|---|
| `COACTVWC` | Account view | 942 | CAVW |
| `COACTUPC` | Account update | 4,237 | CAUP |
| `COCRDLIC` | Card list (paginated) | ~600 | CCLI |
| `COCRDSLC` | Card detail view | ~500 | CCDL |
| `COCRDUPC` | Card update | ~800 | CCUP |
| `COMEN01C` | Main menu (routing) | 309 | CM00 |
| `CBACT01C` | Batch: account data load | ~300 | -- |
| `CBACT02C` | Batch: card data load | ~300 | -- |
| `CBACT03C` | Batch: card xref load | ~300 | -- |
| `CBCUS01C` | Batch: customer data load | ~300 | -- |

**Data ownership:**

| Entity | VSAM File | Copybook | Record Size | Key |
|---|---|---|---|---|
| Account | ACCTDAT | CVACT01Y | 300 bytes | ACCT-ID (9-11) |
| Card | CARDDAT | CVACT02Y | 150 bytes | CARD-NUM (X16) |
| Customer | CUSTDAT | CVCUS01Y | 500 bytes | CUST-ID (9-9) |
| Card Cross-Reference | CCXREF | CVACT03Y | 50 bytes | XREF-CARD-NUM (X16) |
| Card Alt-Index by Account | CARDAIX | -- | -- | via AIX on CARDDAT |
| XRef Alt-Index by Account | CXACAIX | -- | -- | via AIX on CCXREF |

**Key relationships:**
- Card -> Account: `CARD-ACCT-ID` in CVACT02Y
- CrossRef: `XREF-CARD-NUM` <-> `XREF-CUST-ID` <-> `XREF-ACCT-ID` in CVACT03Y
- Account -> Customer: via cross-reference lookup

**Inbound dependencies:**
- BC-1 routes users here after authentication
- BC-3 reads ACCTDAT, CCXREF, CXACAIX for transaction validation
- BC-4 reads ACCTDAT for bill payment and interest calculation

**Outbound dependencies:**
- Navigates to Card Management screens from Account View (`COCRDLIC`, `COCRDSLC`, `COCRDUPC`)

**Extraction seam analysis:**

| Seam | Quality | Details |
|---|---|---|
| ACCTDAT reads by other contexts | **Shared data -- requires anti-corruption layer** | `COBIL00C` (BC-4) reads and updates ACCTDAT; `CBTRN02C` (BC-3 batch) reads ACCTDAT; `CBACT04C` (BC-4) updates ACCTDAT for interest. These cross-context writes are the highest-risk coupling in the system. |
| CCXREF/CXACAIX lookups | **Moderate** | Used by BC-3 (`COTRN02C`) and BC-4 (`COBIL00C`) for card-to-account resolution. Can be replaced by a synchronous API call to the Account Service. |
| CUSTDAT reads | **Clean** | Only read by `COACTVWC` (account view) and `CBSTM03A` (statements). No cross-context writes. |
| CARDDAT/CARDAIX reads | **Clean** | Only accessed within this context and by statement generation. |

**Migration note:** The CCXREF file is a denormalized cross-reference that exists because VSAM cannot perform JOINs. In PostgreSQL, this becomes a foreign key relationship (`cards.account_id -> accounts.id`), eliminating the need for a separate xref entity.

---

### BC-3: Transaction Processing

**Domain responsibility:** Transaction capture (online add), transaction browsing (list/view), daily transaction posting (batch), and transaction reporting (batch).

**Programs:**

| Program | Function | LOC | CICS Trans |
|---|---|---|---|
| `COTRN00C` | Transaction list (paginated) | 700 | CT00 |
| `COTRN01C` | Transaction view | ~400 | CT01 |
| `COTRN02C` | Transaction add | 784 | CT02 |
| `CBTRN02C` | Batch: daily transaction posting | 732 | -- |
| `CBTRN01C` | Batch: transaction combine | ~400 | -- |
| `CBTRN03C` | Batch: transaction report | ~500 | -- |

**Data ownership:**

| Entity | VSAM File | Copybook | Record Size | Key |
|---|---|---|---|---|
| Transaction | TRANSACT | CVTRA05Y | 350 bytes | TRAN-ID (X16) |
| Daily Transaction | DALYTRAN | CVTRA06Y | 350 bytes | sequential |
| Daily Rejects | DALYREJS | -- | 430 bytes | sequential |
| Tran Category Balance | TCATBAL | CVTRA01Y | 50 bytes | ACCT+TYPE+CAT |

**Key fields in CVTRA05Y:** `TRAN-ID`, `TRAN-TYPE-CD`, `TRAN-CAT-CD`, `TRAN-SOURCE`, `TRAN-DESC`, `TRAN-AMT` (S9(9)V99), `TRAN-MERCHANT-*`, `TRAN-CARD-NUM`, `TRAN-ORIG-TS`, `TRAN-PROC-TS`

**Inbound dependencies:**
- BC-4 (Bill Payment) writes transaction records to TRANSACT
- BC-4 (Interest Calculation) writes interest transaction records

**Outbound dependencies:**
- BC-2: Reads CCXREF/CXACAIX for card-to-account validation in `COTRN02C`
- BC-2: Reads ACCTDAT for account validation in `CBTRN02C`

**Extraction seam analysis:**

| Seam | Quality | Details |
|---|---|---|
| TRANSACT writes from other contexts | **Shared data -- highest coupling risk** | `COBIL00C` (BC-4) generates a transaction ID by reading the last TRANSACT record and incrementing. `CBACT04C` (BC-4) writes interest transactions. This sequential ID generation pattern creates contention and must be replaced with a centralized ID generator (UUID or database sequence). |
| TRANSACT reads by batch | **Clean within context** | All batch transaction programs (`CBTRN01-03C`) belong to this context. |
| CCXREF/CXACAIX lookups | **API boundary** | `COTRN02C` reads CCXREF to resolve card-to-account. Replace with API call to Account Service. |
| Date validation subroutine | **Utility seam** | `COTRN02C` calls `CSUTLDTC` for date validation. Extract as a shared utility library. |

---

### BC-4: Financial Operations

**Domain responsibility:** Bill payment, interest calculation, statement generation, and financial reporting.

**Programs:**

| Program | Function | LOC | CICS Trans |
|---|---|---|---|
| `COBIL00C` | Bill payment (online) | 573 | CB00 |
| `CBACT04C` | Interest calculation (batch) | 653 | -- |
| `CBSTM03A` | Statement generation (batch, orchestrator) | 924 | -- |
| `CBSTM03B` | Statement generation (batch, file I/O subroutine) | ~400 | -- |
| `CORPT00C` | Report submission (online) | 650 | CR00 |

**Data ownership:**

| Entity | VSAM File | Copybook | Record Size | Key |
|---|---|---|---|---|
| Tran Category Balance | TCATBAL | CVTRA01Y | 50 bytes | ACCT+TYPE+CAT |
| Discount Group | DISCGRP | CVTRA02Y | 50 bytes | GROUP+TYPE+CAT |

**Note:** TCATBAL is shared with BC-3 (updated by `CBTRN02C` during posting). DISCGRP is exclusively owned by BC-4.

**Inbound dependencies:**
- BC-3: TCATBAL records are created/updated by the posting process

**Outbound dependencies:**
- BC-2: Reads ACCTDAT (account balance for bill payment, interest update)
- BC-2: Reads CCXREF/CXACAIX (card lookup for bill payment)
- BC-2: Reads CUSTDAT (customer name/address for statements)
- BC-3: Reads TRANSACT (transaction details for statements/reports)
- BC-3: Writes to TRANSACT (bill payment transaction, interest transaction)

**Extraction seam analysis:**

| Seam | Quality | Details |
|---|---|---|
| ACCTDAT read/update for bill payment | **Tight coupling** | `COBIL00C` opens ACCTDAT with READ UPDATE and REWRITE in the same unit of work. Must be replaced with a transactional API call to Account Service. |
| ACCTDAT update for interest | **Tight coupling** | `CBACT04C` reads account, computes interest, and rewrites the account record. Must use Account Service API. |
| TRANSACT write for bill payment | **Cross-context write** | `COBIL00C` generates a new TRAN-ID and writes to TRANSACT. Must use Transaction Service API. |
| Statement generation data reads | **Read-only, clean** | `CBSTM03A` reads XREFFILE, CUSTFILE, ACCTFILE, TRNXFILE. These are all read-only and can be replaced with API calls or a read replica. |
| PSA/TCB/TIOT addressing in CBSTM03A | **Hard platform dependency** | `CBSTM03A` accesses z/OS control blocks to enumerate DD names. This has no Java equivalent and must be removed. |
| ALTER/GO TO in CBSTM03A | **Legacy control flow** | Uses COBOL ALTER to dynamically change GO TO targets. This is the most complex control flow pattern in the codebase. |

---

### BC-5: Optional Integrations (Authorization, DB2 Transaction Types, Messaging)

**Domain responsibility:** Extended authorization checks via IMS/DB2/MQ, DB2-based transaction type management, and MQ-based account inquiry.

**Programs:**

| Module | Programs | Technology |
|---|---|---|
| Authorization (IMS-DB2-MQ) | `COPAUA0C`, `COPAUS0C`, `COPAUS1C`, `COPAUS2C`, `CBPAUP0C` + 3 more | IMS DB, DB2, MQ |
| Transaction Type (DB2) | `COTRTUPC`, `COTRTLIC`, `COBTUPDT` | DB2 embedded SQL |
| Account Inquiry (VSAM-MQ) | `CODATE01`, `COACCT01` | VSAM, MQ request/reply |

**Extraction seam:** Clean boundary. These are add-on modules with their own subdirectories and READMEs. They extend the core system but are not required for basic operation. They can be migrated independently in a later phase.

---

## Shared Data Problem Analysis

The most critical coupling in the system is the shared VSAM data pattern. Multiple bounded contexts read and write the same files:

### ACCTDAT (Account Master) -- Shared by 3 Contexts

| Context | Access | Programs |
|---|---|---|
| BC-2 (Account Mgmt) | Read + Write | `COACTVWC`, `COACTUPC` |
| BC-3 (Transaction) | Read | `CBTRN02C` (posting validation) |
| BC-4 (Financial Ops) | Read + Write | `COBIL00C` (bill pay), `CBACT04C` (interest) |

**Resolution:** BC-2 owns the `accounts` table. BC-3 and BC-4 access via Account Service API. For batch, use a database view or read replica.

### TRANSACT (Transaction Master) -- Shared by 2 Contexts

| Context | Access | Programs |
|---|---|---|
| BC-3 (Transaction) | Read + Write | `COTRN00-02C`, `CBTRN02C` |
| BC-4 (Financial Ops) | Read + Write | `COBIL00C` (bill pay creates txn), `CBACT04C` (interest creates txn), `CBSTM03A` (reads for statements) |

**Resolution:** BC-3 owns the `transactions` table. BC-4 writes via Transaction Service API. Statement generation reads via API or CQRS read model.

### CCXREF (Cross-Reference) -- Shared by 3 Contexts

| Context | Access | Programs |
|---|---|---|
| BC-2 (Account Mgmt) | Read + Write | `COACTVWC` |
| BC-3 (Transaction) | Read | `COTRN02C`, `CBTRN02C` |
| BC-4 (Financial Ops) | Read | `COBIL00C` |

**Resolution:** Eliminated in target architecture. Replace with foreign key joins (`cards.account_id`, `cards.customer_id`). Account Service exposes a `GET /accounts/{id}/cards` endpoint.

---

## Extraction Seam Summary

| Seam ID | Between | Seam Type | Quality | Resolution |
|---|---|---|---|---|
| S-1 | BC-1 -> BC-2 | Navigation (XCTL) | **Clean** | Replace COMMAREA with JWT claims |
| S-2 | BC-2 -> BC-3 | Shared data (CCXREF) | **Moderate** | API call to Account Service |
| S-3 | BC-2 -> BC-4 | Shared data (ACCTDAT r/w) | **Tight** | Account Service API with transactional guarantees |
| S-4 | BC-3 -> BC-4 | Shared data (TRANSACT r/w) | **Tight** | Transaction Service API |
| S-5 | BC-3 -> BC-4 | Shared data (TCATBAL) | **Moderate** | Co-locate or use event-driven update |
| S-6 | BC-4 -> BC-2 | Read-only data (CUSTDAT) | **Clean** | Customer API endpoint |
| S-7 | All -> Utility | Subroutine (CSUTLDTC) | **Clean** | Shared Java utility library |
| S-8 | Online -> Batch | File open/close (CLOSEFIL/OPENFIL) | **Platform** | Eliminated -- database doesn't need file close |
| S-9 | BC-4 Statement | PSA/TCB/TIOT addressing | **Hard platform** | Remove -- use config/environment instead |

---

## Target Microservice Architecture

Based on the bounded context analysis, the following microservices are recommended:

| Service | Owns | Source Contexts | API Style |
|---|---|---|---|
| **auth-service** | `users` table | BC-1 | REST + JWT issuance |
| **account-service** | `accounts`, `cards`, `customers` tables | BC-2 | REST/CRUD |
| **transaction-service** | `transactions`, `transaction_category_balances` tables | BC-3 | REST/CRUD + event publishing |
| **payment-service** | (stateless orchestrator) | BC-4 (bill pay) | REST, calls account-service + transaction-service |
| **batch-service** | `discount_groups` table, job state | BC-4 (batch) | Spring Batch jobs, reads via service APIs |
| **report-service** | Report templates, generated outputs | BC-4 (reports) | REST trigger, async generation |

### Inter-Service Communication

```
                    ┌──────────────┐
                    │  API Gateway │
                    └──────┬───────┘
                           │
         ┌─────────────────┼─────────────────┐
         │                 │                  │
    ┌────▼────┐      ┌─────▼──────┐     ┌────▼─────┐
    │ auth-   │      │ account-   │     │ trans-   │
    │ service │      │ service    │     │ action-  │
    │         │      │            │     │ service  │
    └─────────┘      └─────┬──────┘     └────┬─────┘
                           │                  │
                    ┌──────▼──────────────────▼──────┐
                    │                                │
               ┌────▼─────┐    ┌─────────┐    ┌─────▼─────┐
               │ payment- │    │ report- │    │ batch-    │
               │ service  │    │ service │    │ service   │
               └──────────┘    └─────────┘    └───────────┘
```

**Sync calls (REST):** payment-service -> account-service, payment-service -> transaction-service
**Async events:** transaction-service publishes `TransactionCreated` events consumed by batch-service for category balance updates
**Batch reads:** batch-service reads account and transaction data via service APIs (or direct DB read replica for performance)

---

## Aggregate Roots and Entity Relationships

### Account Aggregate (account-service)
```
Account (root)
  ├── Card[] (child entities)
  ├── Customer (associated entity)
  └── AccountStatus, CreditLimit, Balance (value objects)
```

### Transaction Aggregate (transaction-service)
```
Transaction (root)
  ├── MerchantInfo (value object)
  ├── TransactionType + Category (value objects)
  └── Timestamps: origination, processing (value objects)
```

### User Aggregate (auth-service)
```
User (root)
  ├── Credentials (value object - hashed password)
  └── Role (value object - ADMIN | USER)
```
