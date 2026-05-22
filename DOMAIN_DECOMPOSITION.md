# CardDemo Domain Decomposition

## Overview

This document identifies bounded contexts within the CardDemo mainframe application, maps program-to-context ownership, analyzes data coupling between contexts, and identifies extraction seams for incremental modernization.

---

## Bounded Context Map

```
┌──────────────────────────────────────────────────────────────────────────┐
│                          CardDemo System                                 │
│                                                                          │
│  ┌─────────────┐    ┌──────────────────┐    ┌────────────────────────┐  │
│  │  Identity &  │    │   Account        │    │  Transaction           │  │
│  │  Access      │───>│   Management     │<───│  Processing            │  │
│  │  (BC-1)      │    │   (BC-2)         │    │  (BC-5)                │  │
│  └─────────────┘    └────────┬─────────┘    └───────────┬────────────┘  │
│         │                    │                           │               │
│         │           ┌────────┴─────────┐    ┌───────────┴────────────┐  │
│         │           │   Card           │    │  Financial             │  │
│         │           │   Management     │<───│  Calculations          │  │
│         │           │   (BC-3)         │    │  (BC-6)                │  │
│         │           └────────┬─────────┘    └───────────┬────────────┘  │
│         │                    │                           │               │
│         │           ┌────────┴──────────────────────────┴────────────┐  │
│         │           │                Customer                        │  │
│         │           │                (BC-4)                          │  │
│         │           └────────────────────────────────────────────────┘  │
│         │                                                               │
│  ┌──────┴──────┐    ┌──────────────────┐    ┌────────────────────────┐  │
│  │ User Admin  │    │  Reporting &     │    │  Bill Payment          │  │
│  │ (BC-1)      │    │  Statements      │    │  (BC-7)                │  │
│  │             │    │  (BC-9)          │    │                        │  │
│  └─────────────┘    └──────────────────┘    └────────────────────────┘  │
│                                                                          │
│  ┌─────────────┐    ┌──────────────────┐    ┌────────────────────────┐  │
│  │ Reference   │    │  Data Migration  │    │  Authorization         │  │
│  │ Data (BC-8) │    │  (BC-10)         │    │  (BC-11) [Optional]    │  │
│  └─────────────┘    └──────────────────┘    └────────────────────────┘  │
└──────────────────────────────────────────────────────────────────────────┘
```

---

## Bounded Context Definitions

### BC-1: Identity & Access Management

**Domain Purpose**: Authenticate users, manage user lifecycle, and enforce role-based access control.

| Component | Type | Context Role |
|:----------|:-----|:-------------|
| `COSGN00C` | CICS Online | Authentication entry point |
| `COUSR00C` | CICS Online | User list (admin) |
| `COUSR01C` | CICS Online | User add (admin) |
| `COUSR02C` | CICS Online | User update (admin) |
| `COUSR03C` | CICS Online | User delete (admin) |
| `CSUSR01Y` | Copybook | User entity definition (80 bytes) |
| USRSEC | VSAM KSDS | User credential store |
| DUSRSECJ | JCL | User file initialization |

**Ubiquitous Language**:
- **User**: An operator of the system, identified by `SEC-USR-ID` (8 chars)
- **User Type**: `A` (Admin) or `U` (Regular) — stored in `SEC-USR-TYPE`
- **COMMAREA Identity**: `CDEMO-USER-ID` + `CDEMO-USER-TYPE` in `COCOM01Y`

**Data Ownership**: Owns the `USRSEC` dataset exclusively. No other context writes to it.

**Extraction Seam Analysis**:
- **Seam Type**: Clean API boundary
- **Inbound Contract**: CICS RETURN with COMMAREA populating `CDEMO-USER-ID` and `CDEMO-USER-TYPE`
- **Outbound Dependencies**: None — pure producer
- **Consumers**: Every CICS program reads `CDEMO-USER-ID`/`CDEMO-USER-TYPE` from COMMAREA
- **Extraction Approach**: Replace with JWT/OAuth2 token. The COMMAREA fields become JWT claims. All downstream programs check `CDEMO-USER-TYPE` — this maps to a role claim.
- **Seam Quality**: **Excellent** — The COMMAREA contract is the only integration point. No shared data access.

---

### BC-2: Account Management

**Domain Purpose**: Maintain credit card account lifecycle — balances, limits, status, and dates.

| Component | Type | Context Role |
|:----------|:-----|:-------------|
| `COACTVWC` | CICS Online | Account view |
| `COACTUPC` | CICS Online | Account update (4,236 LOC) |
| `CVACT01Y` | Copybook | Account entity (300 bytes) |
| ACCTDATA | VSAM KSDS | Account master file (keyed by `ACCT-ID`) |
| ACCTFILE | JCL | Account file refresh |

**Ubiquitous Language**:
- **Account**: Identified by `ACCT-ID` (11-digit). Contains balance, credit limit, cash credit limit, cycle credits/debits
- **Account Status**: `ACCT-ACTIVE-STATUS` — active/inactive flag
- **Group**: `ACCT-GROUP-ID` — links account to a disclosure/rate group

**Data Ownership**: Owns the `ACCTDATA` VSAM dataset. However, other contexts **update** account balances:
- `CBTRN02C` (Transaction Processing) updates `ACCT-CURR-BAL`, `ACCT-CURR-CYC-CREDIT`, `ACCT-CURR-CYC-DEBIT`
- `CBACT04C` (Interest Calculation) updates `ACCT-CURR-BAL`
- `COBIL00C` (Bill Payment) updates `ACCT-CURR-BAL`

**Extraction Seam Analysis**:
- **Seam Type**: Shared data with write contention
- **Inbound Contract**: Direct VSAM READ by `ACCT-ID`
- **Outbound Dependencies**: `CVACT03Y` cross-reference for card-to-account lookups
- **Cross-Context Writers**: Transaction Processing, Interest Calculation, Bill Payment
- **Extraction Approach**: Create an Account Service API with dedicated balance-update endpoints. Other contexts call `POST /accounts/{id}/adjustments` instead of directly writing VSAM. Use optimistic locking (version field) to replace CICS ENQUEUE.
- **Seam Quality**: **Moderate** — The balance-update coupling with BC-5, BC-6, and BC-7 (Bill Payment) must be resolved via an API contract. The cross-reference (`CVACT03Y`) is a shared lookup that spans BC-2, BC-3, and BC-4.

---

### BC-3: Card Management

**Domain Purpose**: Manage credit card issuance, status, and the card-account-customer cross-reference.

| Component | Type | Context Role |
|:----------|:-----|:-------------|
| `COCRDLIC` | CICS Online | Card list with pagination |
| `COCRDSLC` | CICS Online | Card detail view |
| `COCRDUPC` | CICS Online | Card update |
| `CVACT02Y` | Copybook | Card entity (150 bytes) |
| `CVACT03Y` | Copybook | Card cross-reference (50 bytes) |
| CARDDATA | VSAM KSDS | Card master (keyed by `CARD-NUM`) |
| CARDXREF | VSAM KSDS | Cross-reference (keyed by `XREF-CARD-NUM`, AIX on `XREF-ACCT-ID`) |
| CARDFILE, XREFFILE | JCL | Card/XREF file refresh |

**Ubiquitous Language**:
- **Card**: 16-digit number (`CARD-NUM`) with CVV, embossed name, expiration, status
- **Cross-Reference (XREF)**: Links `CARD-NUM` → `CUST-ID` → `ACCT-ID` (the central join entity)

**Data Ownership**: Owns `CARDDATA` and `CARDXREF`. The XREF is read by many contexts:
- `CBTRN02C` reads XREF to validate card numbers on transactions
- `CBACT04C` reads XREF (via AIX) for account-to-card lookups during interest calculation
- `COBIL00C` reads XREF (via AIX) for account lookups during bill payment
- `CBSTM03A` reads XREF for statement generation
- `CBTRN03C` reads XREF for transaction reports

**Extraction Seam Analysis**:
- **Seam Type**: Shared read-heavy lookup table (XREF)
- **Inbound Contract**: Direct VSAM READ by `CARD-NUM` or VSAM AIX READ by `ACCT-ID`
- **Consumers of XREF**: 6+ programs across 4 bounded contexts
- **Extraction Approach**: The XREF is a **Shared Kernel** — it cannot belong exclusively to one context. Options:
  1. **Preferred**: Make XREF a read-only view/API owned by Card Management. Other contexts query `GET /cards/xref?cardNum=X` or `GET /cards/xref?acctId=Y`. Card Management is the single writer.
  2. **Alternative**: Replicate XREF data into each context's local store (eventual consistency).
- **Seam Quality**: **Moderate** — XREF is the most heavily shared data structure in the system. Its extraction defines the integration architecture for the entire modernized system.

---

### BC-4: Customer Management

**Domain Purpose**: Maintain customer personal information and demographics.

| Component | Type | Context Role |
|:----------|:-----|:-------------|
| `CBCUS01C` | Batch | Customer file processing |
| `CVCUS01Y` | Copybook | Customer entity (500 bytes) |
| CUSTDATA | VSAM KSDS | Customer master (keyed by `CUST-ID`) |
| CUSTFILE | JCL | Customer file refresh |

**Ubiquitous Language**:
- **Customer**: Identified by `CUST-ID` (9-digit). Contains name, address (3 lines + state + country + ZIP), phone numbers, SSN, government ID, date of birth, EFT account, FICO score

**Data Ownership**: Owns `CUSTDATA` exclusively. Read by:
- `COACTVWC` (Account View) displays customer info alongside account
- `CBSTM03A` (Statements) includes customer name/address on statements
- `CBEXPORT` (Export) includes customer data in migration file

**Extraction Seam Analysis**:
- **Seam Type**: Clean data boundary with read-only consumers
- **Inbound Contract**: Direct VSAM READ by `CUST-ID`
- **Extraction Approach**: Customer Service API with `GET /customers/{id}`. The 500-byte record maps to a rich JPA entity. PII fields (SSN, DOB, government ID) require encryption-at-rest in the new system.
- **Seam Quality**: **Good** — Read-only access from other contexts. The XREF provides the `CUST-ID` lookup key, so Customer Service depends on Card Management's XREF API.

---

### BC-5: Transaction Processing

**Domain Purpose**: Record, validate, post, and manage financial transactions.

| Component | Type | Context Role |
|:----------|:-----|:-------------|
| `COTRN00C` | CICS Online | Transaction list |
| `COTRN01C` | CICS Online | Transaction view |
| `COTRN02C` | CICS Online | Transaction add |
| `CBTRN02C` | Batch | Daily transaction posting (731 LOC) |
| `CBTRN01C` | Batch | Transaction backup |
| `CVTRA05Y` | Copybook | Transaction entity (350 bytes) |
| `CVTRA06Y` | Copybook | Daily transaction entity (350 bytes) |
| TRANSACT | VSAM KSDS | Transaction master |
| DALYTRAN | Sequential | Daily transaction input |
| DALYREJS | GDG | Rejected transactions |
| TCATBALF | VSAM KSDS | Transaction category balances |
| POSTTRAN, TRANBKP, COMBTRAN, TRANIDX | JCL | Batch processing chain |

**Ubiquitous Language**:
- **Transaction**: 350-byte record with ID, type, category, merchant info, card number, timestamps
- **Daily Transaction**: Input file for batch posting (same layout as master)
- **Rejection**: Transaction that fails card-number validation against XREF
- **Category Balance**: Running totals per account + transaction category
- **Post**: The act of validating and recording a daily transaction into the master file

**Data Ownership**: Owns `TRANSACT`, `DALYTRAN`, `DALYREJS`, `TCATBALF`. Updates `ACCTDATA` (owned by BC-2) during posting.

**Extraction Seam Analysis**:
- **Seam Type**: Complex boundary with cross-context writes
- **Inbound Contract**: Online — CICS READ/WRITE to TRANSACT. Batch — Sequential READ of DALYTRAN, RANDOM READ/WRITE to TRANSACT, ACCTDATA, TCATBALF
- **Cross-Context Writes**: Updates `ACCT-CURR-BAL` in ACCTDATA (BC-2) during posting
- **Extraction Approach**:
  1. Online transaction CRUD → Transaction Service API
  2. Batch posting → Spring Batch job calling Account Service API for balance updates
  3. Daily transaction input → File upload or message queue ingestion
  4. Rejected transactions → Error table or dead-letter queue
- **Seam Quality**: **Challenging** — The batch posting job (`CBTRN02C`) is a cross-cutting concern that reads XREF (BC-3), updates accounts (BC-2), and manages its own data. This is the **most complex extraction** in the system.

---

### BC-6: Financial Calculations

**Domain Purpose**: Compute interest, fees, and maintain disclosure group rate tables.

| Component | Type | Context Role |
|:----------|:-----|:-------------|
| `CBACT04C` | Batch | Interest calculator (652 LOC) |
| `CVTRA01Y` | Copybook | Category balance entity |
| `CVTRA02Y` | Copybook | Disclosure group entity |
| TCATBALF | VSAM KSDS | Transaction category balances |
| DISCGRP | VSAM KSDS | Disclosure group rates |
| INTCALC | JCL | Interest calculation job |

**Ubiquitous Language**:
- **Disclosure Group**: Rate table linking account groups to interest rates
- **Category Balance**: Accumulated transaction amounts per account per category
- **Interest Computation**: Monthly calculation based on disclosure group rates applied to category balances

**Data Ownership**: Reads TCATBALF (shared with BC-5), DISCGRP (shared with BC-8). Updates ACCTDATA (BC-2).

**Extraction Seam Analysis**:
- **Seam Type**: Read-heavy computation with cross-context output
- **Inbound Contract**: Reads TCATBALF sequentially, DISCGRP by random key, XREF via AIX
- **Cross-Context Writes**: Updates `ACCT-CURR-BAL` in ACCTDATA (BC-2); generates system transactions
- **Extraction Approach**: Spring Batch job that reads from the Transaction and Reference Data services, computes interest using `BigDecimal`, and calls the Account Service API to post adjustments. The COMP-3 to `BigDecimal` mapping is critical.
- **Seam Quality**: **Moderate** — Computation is self-contained but requires data from 3 other contexts and writes to 1.

---

### BC-7: Bill Payment

**Domain Purpose**: Process online bill payments against account balances.

| Component | Type | Context Role |
|:----------|:-----|:-------------|
| `COBIL00C` | CICS Online | Bill payment with confirmation |
| COBIL00 | BMS Map | Payment screen |

**Data Ownership**: None — reads and writes to data owned by BC-2 (ACCTDATA) and BC-5 (TRANSACT). Uses XREF (BC-3) for lookups.

**Extraction Seam Analysis**:
- **Seam Type**: Orchestrating service (no owned data)
- **Inbound Contract**: CICS MAP input → XREF lookup → Account balance read → Transaction write → Account balance update
- **Cross-Context Dependencies**: BC-2 (Account), BC-3 (XREF), BC-5 (Transaction)
- **Extraction Approach**: Payment Service that orchestrates calls to Account Service and Transaction Service. The two-step confirmation pattern maps to a REST saga (initiate → confirm) or a Process Manager pattern.
- **Seam Quality**: **Good** — As an orchestrator with no owned data, this context is the **easiest to extract** once its dependencies (Account, Card, Transaction services) exist.

---

### BC-8: Reference Data

**Domain Purpose**: Maintain lookup tables for transaction types, categories, and disclosure groups.

| Component | Type | Context Role |
|:----------|:-----|:-------------|
| `CVTRA01Y`–`CVTRA04Y` | Copybooks | Reference data entities |
| TRANTYPE | VSAM KSDS | Transaction type codes |
| TRANCATG | VSAM KSDS | Transaction category codes |
| DISCGRP | VSAM KSDS | Disclosure groups / rates |
| TCATBALF | VSAM KSDS | Category balance file |
| TRANTYPE, TRANCATG, DISCGRP, TCATBALF | JCL | VSAM refresh jobs |
| MNTTRDB2 | JCL/DB2 | Optional DB2 maintenance |

**Data Ownership**: Owns all reference VSAM files. Read by Transaction Processing (BC-5) and Financial Calculations (BC-6).

**Extraction Seam Analysis**:
- **Seam Type**: Clean producer of read-only reference data
- **Extraction Approach**: Reference Data Service with `GET /reference/transaction-types`, `/categories`, `/disclosure-groups`. Cacheable with long TTLs. Weekly refresh cycle can be replaced by direct CRUD API.
- **Seam Quality**: **Excellent** — Pure lookup data with no write contention.

---

### BC-9: Reporting & Statements

**Domain Purpose**: Generate transaction reports and account statements.

| Component | Type | Context Role |
|:----------|:-----|:-------------|
| `CORPT00C` | CICS Online | Report parameter entry |
| `CBTRN03C` | Batch | Transaction detail report |
| `CBSTM03A` | Batch | Statement generation (text + HTML) |
| `CBSTM03B` | Batch | I/O subroutine for statements |
| TRANREPT, CREASTMT | JCL | Report generation jobs |

**Data Ownership**: None — reads from TRANSACT (BC-5), CARDXREF (BC-3), CUSTDATA (BC-4), ACCTDATA (BC-2), TRANTYPE/TRANCATG (BC-8).

**Extraction Seam Analysis**:
- **Seam Type**: Read-only consumer (no data mutations)
- **Extraction Approach**: Reporting Service that queries Transaction, Account, Customer, and Reference Data APIs. Statement generation uses templating engine (Thymeleaf/JasperReports). Can be extracted **independently** of other contexts since it has no write dependencies.
- **Seam Quality**: **Excellent** — Pure read path. No data ownership conflicts.

---

### BC-10: Data Migration

**Domain Purpose**: Export and import consolidated customer data for branch migration.

| Component | Type | Context Role |
|:----------|:-----|:-------------|
| `CBEXPORT` | Batch | Multi-file export |
| `CBIMPORT` | Batch | Multi-file import with validation |
| `CVEXPORT` | Copybook | Multi-record export layout |
| CBEXPORT, CBIMPORT | JCL | Migration jobs |

**Data Ownership**: Owns the export file format. Reads all master files for export; writes all master files for import.

**Extraction Seam Analysis**:
- **Seam Type**: Cross-cutting data access (reads/writes everything)
- **Extraction Approach**: Replace with ETL pipeline (Spring Batch or AWS Glue) that calls each service API. The REDEFINES-based polymorphic record layout maps to JSON with a `recordType` discriminator.
- **Seam Quality**: **Good** — This is a utility, not a core business process. Extract last after all service APIs exist.

---

### BC-11: Authorization Processing (Optional)

**Domain Purpose**: Process credit card authorization requests via messaging, with IMS/DB2 integration.

| Component | Type | Context Role |
|:----------|:-----|:-------------|
| `COPAUA0C` | CICS/MQ | Authorization decision engine |
| `COPAUS0C` | CICS Online | Pending authorization summary |
| `COPAUS1C` | CICS Online | Pending authorization details |
| `COPAUS2C` | CICS Online | Authorization status update |
| `CBPAUP0C` | Batch | Expired authorization purge |
| IMS DBDs/PSBs | IMS | Hierarchical data definitions |
| AUTHFRDS | DB2 DCL | Authorization fraud detection table |
| MQ Queues | MQ | Authorization request/response |

**Data Ownership**: Owns IMS databases and DB2 authorization tables. Interacts with core card/account data via XREF.

**Extraction Seam Analysis**:
- **Seam Type**: Loosely coupled via MQ (natural async seam)
- **Extraction Approach**: The MQ interface is a **natural extraction seam**. Replace with:
  1. Kafka/SQS topic for authorization requests
  2. Authorization microservice with its own PostgreSQL schema
  3. REST API for pending authorization screens
  4. Scheduled job for expiry purge
- **Seam Quality**: **Good** — MQ already provides async decoupling. IMS/DB2 are internal to this context and can be replaced without affecting other contexts.

---

## Cross-Context Data Flow Map

```
                    XREF (BC-3)
                   ┌─────────┐
                   │CARD-NUM │
                   │CUST-ID  │──────────────> Customer (BC-4)
                   │ACCT-ID  │──────────────> Account (BC-2)
                   └────┬────┘
                        │
         ┌──────────────┼──────────────┬──────────────────┐
         │              │              │                    │
    Transaction    Bill Payment   Interest Calc      Reporting
    Posting(BC-5)  (BC-7)        (BC-6)              (BC-9)
         │              │              │
         │         ┌────┴────┐         │
         └────────>│ACCTDATA │<────────┘
                   │(BC-2)   │
                   │ACCT-BAL │
                   └─────────┘
                    ^^^^^^^
              WRITE CONTENTION ZONE
```

### Critical Shared Data Structures

| Data Structure | Owner | Readers | Writers | Contention Risk |
|:---------------|:------|:--------|:--------|:----------------|
| CARDXREF | BC-3 (Card) | BC-5, BC-6, BC-7, BC-9, BC-10 | BC-3 only | Low (single writer) |
| ACCTDATA | BC-2 (Account) | BC-5, BC-6, BC-7, BC-9, BC-10 | BC-2, BC-5, BC-6, BC-7 | **High** (4 writers) |
| TRANSACT | BC-5 (Transaction) | BC-7, BC-9, BC-10 | BC-5, BC-7 | Medium (2 writers) |
| CUSTDATA | BC-4 (Customer) | BC-9, BC-10 | BC-4 only | Low (single writer) |
| USRSEC | BC-1 (Identity) | None external | BC-1 only | None |
| TCATBALF | BC-5/BC-8 (shared) | BC-6 | BC-5 | Low |
| DISCGRP | BC-8 (Reference) | BC-6 | BC-8 only | None |

---

## Extraction Priority & Dependency Graph

```
Phase 1 (No dependencies):
  BC-1  Identity ─────────────────────────────────────────> [Extract first]
  BC-8  Reference Data ───────────────────────────────────> [Extract first]

Phase 2 (Depends on Phase 1):
  BC-4  Customer ─────────────────────────────────────────> [Depends on XREF]
  BC-3  Card Management ──────────────────────────────────> [Owns XREF]

Phase 3 (Depends on Phase 2):
  BC-2  Account Management ───────────────────────────────> [Depends on XREF]
  BC-5  Transaction Processing (Online only) ─────────────> [Depends on XREF, Account]

Phase 4 (Depends on Phase 3):
  BC-7  Bill Payment ─────────────────────────────────────> [Depends on Account, Transaction, XREF]
  BC-9  Reporting ────────────────────────────────────────> [Depends on all read APIs]

Phase 5 (Depends on Phase 3):
  BC-5  Transaction Processing (Batch) ───────────────────> [Depends on Account API]
  BC-6  Financial Calculations ───────────────────────────> [Depends on Account, Ref Data, XREF]

Phase 6 (Independent or last):
  BC-10 Data Migration ───────────────────────────────────> [Depends on all service APIs]
  BC-11 Authorization ────────────────────────────────────> [MQ seam; independent timeline]
```

---

## Anti-Corruption Layer Strategy

During migration, an **Anti-Corruption Layer (ACL)** bridges old VSAM data and new service APIs:

| Integration Point | ACL Pattern | Direction |
|:-------------------|:-----------|:----------|
| COMMAREA → JWT | Token Translator | Old → New |
| VSAM READ → REST GET | Data Proxy | Old → New |
| REST POST → VSAM WRITE | Write-Behind Proxy | New → Old |
| BMS MAP → SPA Component | UI Adapter | Parallel operation |
| JCL SUBMIT → REST call | Batch Bridge | Old → New |
| VSAM AIX → SQL JOIN | Query Translator | Old → New |
| Control-M → Airflow | Scheduler Adapter | Old → New |

### Dual-Write Reconciliation

During the transition period when both VSAM and RDBMS are active:

1. **Primary**: New service writes to RDBMS
2. **Secondary**: ACL replicates to VSAM for unconverted programs
3. **Reconciliation**: Nightly batch compares VSAM and RDBMS records, reports discrepancies
4. **Cutover**: Once all consumers migrated, decommission VSAM writes

---

## Microservice Target Architecture

| Service | Source Contexts | API Surface |
|:--------|:---------------|:------------|
| **Identity Service** | BC-1 | `POST /auth/login`, `GET /users`, `POST /users` |
| **Account Service** | BC-2 | `GET /accounts/{id}`, `PUT /accounts/{id}`, `POST /accounts/{id}/adjustments` |
| **Card Service** | BC-3 | `GET /cards`, `GET /cards/{num}`, `PUT /cards/{num}`, `GET /cards/xref` |
| **Customer Service** | BC-4 | `GET /customers/{id}`, `PUT /customers/{id}` |
| **Transaction Service** | BC-5 | `GET /transactions`, `POST /transactions`, `POST /transactions/batch-post` |
| **Financial Calc Service** | BC-6 | `POST /calculations/interest` (scheduled) |
| **Payment Service** | BC-7 | `POST /payments/initiate`, `POST /payments/confirm` |
| **Reference Data Service** | BC-8 | `GET /reference/transaction-types`, `/categories`, `/disclosure-groups` |
| **Reporting Service** | BC-9 | `POST /reports/transactions`, `POST /statements/generate` |
| **Migration Service** | BC-10 | `POST /migration/export`, `POST /migration/import` |
| **Authorization Service** | BC-11 | Event-driven: consumes from `auth-requests` topic |
