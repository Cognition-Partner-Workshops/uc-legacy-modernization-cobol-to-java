# CardDemo Domain Decomposition & Bounded Context Analysis

> **Generated**: 2026-03-25 | **Methodology**: Domain-Driven Design bounded context identification with extraction seam analysis

## Overview

This document decomposes the CardDemo monolithic COBOL application into bounded contexts suitable for microservice or modular-monolith extraction. Each context is analyzed for data ownership, integration seams, and coupling strength to guide the migration sequence.

---

## Bounded Context Map

```
┌─────────────────────────────────────────────────────────────────┐
│                    CardDemo Application                         │
│                                                                 │
│  ┌──────────────┐    ┌───────────────┐    ┌──────────────────┐  │
│  │  Identity &   │    │   Account     │    │  Credit Card     │  │
│  │  Access Mgmt  │───>│   Management  │<───│  Management      │  │
│  │              │    │               │    │                  │  │
│  │  COSGN00C    │    │  COACTVWC     │    │  COCRDLIC        │  │
│  │  COUSR00-03C │    │  COACTUPC     │    │  COCRDSLC        │  │
│  │  CSUSR01Y    │    │  CBACT01C     │    │  COCRDUPC        │  │
│  │  USRSEC      │    │  CBACT04C     │    │  CBACT02C/03C    │  │
│  │              │    │  CVACT01Y     │    │  CVACT02Y        │  │
│  │              │    │  ACCTDATA     │    │  CARDDATA         │  │
│  └──────────────┘    └───────┬───────┘    └────────┬─────────┘  │
│                              │                     │            │
│                    ┌─────────▼─────────────────────▼──────────┐  │
│                    │        Cross-Reference                   │  │
│                    │        CVACT03Y / CARDXREF               │  │
│                    └─────────┬─────────────────────┬──────────┘  │
│                              │                     │            │
│  ┌──────────────┐    ┌───────▼───────┐    ┌───────▼──────────┐  │
│  │  Bill         │    │  Transaction  │    │  Batch           │  │
│  │  Payment      │───>│  Management   │<───│  Processing      │  │
│  │              │    │               │    │                  │  │
│  │  COBIL00C    │    │  COTRN00-02C  │    │  CBTRN01-03C     │  │
│  │              │    │  CVTRA05Y     │    │  CBSTM03A/B      │  │
│  │              │    │  TRANSACT     │    │  CVTRA06Y         │  │
│  │              │    │               │    │  DALYTRAN          │  │
│  └──────────────┘    └───────────────┘    └──────────────────┘  │
│                                                                 │
│  ┌──────────────┐    ┌───────────────┐    ┌──────────────────┐  │
│  │  Reporting    │    │  Reference     │    │  Data Migration  │  │
│  │              │    │  Data          │    │                  │  │
│  │  CORPT00C    │    │  CVTRA01-04Y  │    │  CBEXPORT        │  │
│  │  CBTRN03C    │    │  TCATBALF     │    │  CBIMPORT        │  │
│  │  CBSTM03A/B  │    │  DISCGRP      │    │  CVEXPORT        │  │
│  │  CVTRA07Y    │    │  TRANTYPE     │    │                  │  │
│  │              │    │  TRANCATG     │    │                  │  │
│  └──────────────┘    └───────────────┘    └──────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

---

## Bounded Contexts

### BC-1: Identity & Access Management

**Domain Purpose:** Authenticate users, manage credentials, and control role-based access to application functions.

#### Assets

| Type | Asset | Lines | Role |
|---|---|---|---|
| Online Program | COSGN00C | 260 | Signon / authentication gateway |
| Online Program | COUSR00C | 695 | User list |
| Online Program | COUSR01C | 299 | User add |
| Online Program | COUSR02C | 414 | User update |
| Online Program | COUSR03C | 359 | User delete |
| Copybook | CSUSR01Y | 26 | User security record (80 bytes) |
| BMS Map | COSGN00 | -- | Signon screen |
| BMS Map | COUSR00-03 | -- | User CRUD screens |
| VSAM File | USRSEC | KSDS | User security master |
| JCL | DUSRSECJ | -- | Load user security VSAM |

#### Data Ownership
- **Owns:** USRSEC (user credentials, user type, user profile)
- **Publishes:** Authenticated user identity (user ID, user type) via COCOM01Y COMMAREA
- **Consumes:** Nothing from other contexts

#### Extraction Seam Analysis

| Seam Type | Description | Coupling Strength |
|---|---|---|
| **Outbound Data** | Sets CDEMO-USER-ID and CDEMO-USER-TYPE in COMMAREA, consumed by all downstream programs | **Weak** -- COMMAREA is a pass-through; replace with JWT claims |
| **Navigation** | XCTL to COMEN01C (regular) or COADM01C (admin) after successful login | **Weak** -- one-way handoff, maps to HTTP redirect |
| **Data Store** | USRSEC VSAM is exclusively owned -- no other program reads/writes it | **None** -- clean extraction |

**Extraction Difficulty:** **Low**
- USRSEC is exclusively owned by this context
- Outbound coupling is limited to COMMAREA fields (user ID, user type)
- No inbound dependencies from other contexts
- Can be extracted first as the authentication gateway

**Target Service:** `identity-service` (Spring Security + Spring Data JPA)
- REST endpoints: `POST /auth/login`, `GET/POST/PUT/DELETE /users`
- JWT token issuance replaces COMMAREA propagation
- BCrypt password hashing replaces plaintext storage

---

### BC-2: Account Management

**Domain Purpose:** Manage credit card account lifecycle including balances, credit limits, cycle tracking, and interest calculations.

#### Assets

| Type | Asset | Lines | Role |
|---|---|---|---|
| Online Program | COACTVWC | 941 | Account view |
| Online Program | COACTUPC | 4,236 | Account update (largest program) |
| Batch Program | CBACT01C | 430 | Read/print accounts |
| Batch Program | CBACT04C | 652 | Interest calculation |
| Copybook | CVACT01Y | 20 | Account record (300 bytes) |
| Copybook | CVCUS01Y | 26 | Customer record (500 bytes) |
| Copybook | CVTRA02Y | 13 | Disclosure group (interest rates) |
| BMS Map | COACTVW | -- | Account view screen |
| BMS Map | COACTUP | -- | Account update screen |
| VSAM File | ACCTDATA | KSDS | Account master |
| VSAM File | CUSTDATA | KSDS | Customer master |
| VSAM File | DISCGRP | KSDS | Disclosure groups (interest rates) |
| JCL | ACCTFILE | -- | Refresh account VSAM |
| JCL | CUSTFILE | -- | Refresh customer VSAM |
| JCL | READACCT | -- | Read account file |
| JCL | INTCALC | -- | Interest calculation batch |

#### Data Ownership
- **Owns:** ACCTDATA (account master), CUSTDATA (customer master), DISCGRP (interest rate configuration)
- **Publishes:** Account ID, account status, customer info via COMMAREA and cross-reference
- **Consumes:** Card cross-reference (CARDXREF) for card-to-account lookup; Transaction category balances (TCATBALF) for interest calculation

#### Extraction Seam Analysis

| Seam Type | Description | Coupling Strength |
|---|---|---|
| **Shared Data (CARDXREF)** | COACTUPC and COACTVWC read CARDXREF to resolve card-to-account relationships | **Medium** -- cross-reference is shared with Card context; need API or shared table |
| **Shared Data (CUSTDATA)** | Customer data is read by Card view/update programs and Account programs | **Medium** -- customer could be its own context, but it's tightly coupled to account |
| **Batch Coupling** | CBACT04C (interest calc) reads TCATBALF and CARDXREF, writes SYSTRAN | **Strong** -- batch pipeline crosses context boundaries |
| **COMMAREA** | Account ID and status passed between programs | **Weak** -- maps to API parameters |

**Extraction Difficulty:** **High**
- COACTUPC is the largest program (4,236 lines) requiring decomposition
- Customer data (CVCUS01Y/CUSTDATA) is shared with Card context
- Interest calculation (CBACT04C) crosses into Transaction/Reference contexts
- CARDXREF is a shared cross-cutting concern

**Target Service:** `account-service` (Spring Boot + Spring Data JPA)
- REST endpoints: `GET/PUT /accounts/{id}`, `GET /accounts/{id}/customer`
- Customer embedded within account context (not separate service -- too coupled)
- Interest calculation as a scheduled Spring Batch job within this service
- Publishes account events (balance changed, status changed) for downstream consumers

---

### BC-3: Credit Card Management

**Domain Purpose:** Manage credit card lifecycle including issuance, status, validation, and card-to-account linking.

#### Assets

| Type | Asset | Lines | Role |
|---|---|---|---|
| Online Program | COCRDLIC | 1,459 | Card list with browse pagination |
| Online Program | COCRDSLC | 887 | Card detail view |
| Online Program | COCRDUPC | 1,560 | Card update |
| Batch Program | CBACT02C | 178 | Read/print cards |
| Batch Program | CBACT03C | 178 | Read/print cross-reference |
| Copybook | CVACT02Y | 14 | Card record (150 bytes) |
| Copybook | CVCRD01Y | 46 | Card detail (extended) |
| BMS Map | COCRDLI | -- | Card list screen |
| BMS Map | COCRDSL | -- | Card detail screen |
| BMS Map | COCRDUP | -- | Card update screen |
| VSAM File | CARDDATA | KSDS | Card master |
| JCL | CARDFILE | -- | Refresh card VSAM |
| JCL | READCARD | -- | Read card file |
| JCL | READXREF | -- | Read cross-reference |

#### Data Ownership
- **Owns:** CARDDATA (card master)
- **Shared:** CARDXREF (card-account cross-reference) -- shared with Account context
- **Consumes:** ACCTDATA (account status), CUSTDATA (customer name for display)

#### Extraction Seam Analysis

| Seam Type | Description | Coupling Strength |
|---|---|---|
| **Shared Data (CARDXREF)** | Cross-reference links cards to accounts and customers -- both contexts read it | **Strong** -- this is the primary coupling point between Card and Account |
| **Read-only Account/Customer** | Card view/update reads ACCTDATA and CUSTDATA for display | **Medium** -- can be replaced with API call to account-service |
| **Navigation** | COCRDLIC XCTL to COCRDSLC (detail) and COCRDUPC (update) | **Weak** -- internal to this context |

**Extraction Difficulty:** **Medium**
- CARDXREF ownership must be decided (assign to Card context, expose via API)
- Read-only dependencies on Account/Customer data map to API calls
- Card programs form a cohesive CRUD group with clear internal navigation
- PCI compliance requirements add architectural constraints

**Target Service:** `card-service` (Spring Boot + Spring Data JPA)
- REST endpoints: `GET /cards`, `GET /cards/{number}`, `PUT /cards/{number}`
- Owns CARDXREF table (source of truth for card-account-customer links)
- Calls `account-service` API for account/customer display data
- PCI-compliant field encryption for card numbers, CVV

---

### BC-4: Transaction Management (Online)

**Domain Purpose:** Capture, browse, and view individual financial transactions through the online interface.

#### Assets

| Type | Asset | Lines | Role |
|---|---|---|---|
| Online Program | COTRN00C | 699 | Transaction list with browse |
| Online Program | COTRN01C | 330 | Transaction detail view |
| Online Program | COTRN02C | 783 | Transaction add |
| Copybook | CVTRA05Y | 21 | Transaction record (350 bytes) |
| BMS Map | COTRN00 | -- | Transaction list screen |
| BMS Map | COTRN01 | -- | Transaction detail screen |
| BMS Map | COTRN02 | -- | Transaction add screen |
| VSAM File | TRANSACT | KSDS | Transaction master |

#### Data Ownership
- **Owns:** TRANSACT (transaction master -- online write path)
- **Consumes:** CARDXREF (card lookup for transaction creation), ACCTDATA (account validation)

#### Extraction Seam Analysis

| Seam Type | Description | Coupling Strength |
|---|---|---|
| **Write Path** | COTRN02C writes to TRANSACT, which is also read/written by batch programs | **Strong** -- shared write target with Batch Processing context |
| **Read Path** | COTRN00C/01C read TRANSACT for browsing | **Weak** -- read-only, maps to database query |
| **Cross-Reference** | COTRN02C reads CARDXREF and ACCTDATA for validation | **Medium** -- API calls to card-service and account-service |

**Extraction Difficulty:** **Medium**
- TRANSACT file is shared between online (this context) and batch (BC-6) contexts
- Read operations are cleanly separable
- Write operations need coordination with batch posting (CBTRN02C)
- Transaction ID generation must migrate from VSAM sequential key to database sequence

**Target Service:** `transaction-service` (Spring Boot + Spring Data JPA)
- REST endpoints: `GET /transactions`, `GET /transactions/{id}`, `POST /transactions`
- Owns TRANSACT table (shared with batch processing via same database)
- Calls card-service and account-service for validation during creation
- Publishes transaction-created events for downstream consumers

---

### BC-5: Bill Payment

**Domain Purpose:** Process customer bill payments, updating account balances and creating payment transaction records.

#### Assets

| Type | Asset | Lines | Role |
|---|---|---|---|
| Online Program | COBIL00C | 572 | Bill payment processing |
| BMS Map | COBIL00 | -- | Bill payment screen |

#### Data Ownership
- **Owns:** Payment business rules (validation, amount calculation)
- **Consumes:** ACCTDATA (balance lookup), CARDXREF (card-account link), TRANSACT (payment record creation)

#### Extraction Seam Analysis

| Seam Type | Description | Coupling Strength |
|---|---|---|
| **Account Balance** | Reads and rewrites ACCTDATA to reduce balance | **Strong** -- direct mutation of Account context data |
| **Transaction Creation** | Writes payment record to TRANSACT | **Medium** -- could publish event instead of direct write |
| **Cross-Reference** | Reads CARDXREF for card-account lookup | **Weak** -- API call to card-service |

**Extraction Difficulty:** **Medium**
- Cross-context write (updates account balance + creates transaction) requires distributed transaction or saga pattern
- Could be modeled as an operation within account-service rather than a standalone service
- Clear business boundary (payment processing) justifies separation

**Target Service:** `payment-service` or embedded within `account-service`
- REST endpoint: `POST /payments`
- Orchestrates: account balance update (account-service) + transaction creation (transaction-service)
- Saga pattern: reserve balance → create transaction → confirm balance update
- Idempotency keys for payment deduplication

---

### BC-6: Batch Processing Pipeline

**Domain Purpose:** Execute the nightly batch cycle: post daily transactions, calculate interest, backup data, combine transactions, and generate statements.

#### Assets

| Type | Asset | Lines | Role |
|---|---|---|---|
| Batch Program | CBTRN01C | 494 | Daily transaction file load |
| Batch Program | CBTRN02C | 731 | Transaction posting |
| Batch Program | CBACT04C | 652 | Interest calculation |
| Batch Program | CBSTM03A | 924 | Statement generation (main) |
| Batch Program | CBSTM03B | 230 | Statement generation (I/O subroutine) |
| Batch Program | CBTRN03C | 649 | Transaction detail report |
| Copybook | CVTRA06Y | 21 | Daily transaction record |
| Copybook | CVTRA01Y | 13 | Category balance |
| Copybook | CVTRA07Y | 73 | Report layout |
| Copybook | COSTM01 | -- | Statement layout |
| VSAM File | DALYTRAN | Sequential | Daily transactions (input) |
| VSAM File | DALYREJS | Sequential | Rejected transactions |
| VSAM File | SYSTRAN | Sequential | System-generated transactions |
| VSAM File | TCATBALF | KSDS | Transaction category balances |
| JCL | POSTTRAN, INTCALC, TRANBKP, COMBTRAN, CREASTMT, TRANREPT, TRANIDX | -- | Batch cycle jobs |
| JCL | CLOSEFIL, OPENFIL | -- | CICS file control |

#### Data Ownership
- **Owns:** DALYTRAN (daily input), DALYREJS (rejects), SYSTRAN (system transactions), TCATBALF (category balances), batch cycle orchestration
- **Shared Read/Write:** TRANSACT (shared with online Transaction context), ACCTDATA (shared with Account context)

#### Extraction Seam Analysis

| Seam Type | Description | Coupling Strength |
|---|---|---|
| **CICS File Control** | CLOSEFIL/OPENFIL jobs control CICS file access during batch | **Very Strong** -- direct coupling to CICS runtime |
| **Shared TRANSACT** | Batch reads/writes the same TRANSACT file as online programs | **Strong** -- must coordinate online/batch access |
| **Shared ACCTDATA** | Interest calculation updates account balances | **Strong** -- cross-context write |
| **Pipeline Ordering** | 12-step sequence with strict dependencies | **Strong** -- internal coupling within batch context |
| **SORT/IDCAMS** | JCL utilities for data manipulation | **Medium** -- maps to SQL or Java sort operations |

**Extraction Difficulty:** **Very High**
- Tightest coupling in the entire application
- CLOSEFIL/OPENFIL coordinate with CICS runtime (disappears in modern architecture)
- Shared file mutations across multiple contexts
- Strict job ordering with no parallelism opportunities
- Financial precision requirements for interest calculation

**Target Service:** `batch-processing-service` (Spring Batch)
- Spring Batch jobs replacing each JCL step
- Database transactions replace VSAM file locking
- CLOSEFIL/OPENFIL become unnecessary (database handles concurrent access)
- Job orchestration via Spring Batch job dependencies or AWS Step Functions

---

### BC-7: Reference Data

**Domain Purpose:** Manage lookup tables for transaction types, categories, disclosure groups, and validation data.

#### Assets

| Type | Asset | Lines | Role |
|---|---|---|---|
| Copybook | CVTRA03Y | 10 | Transaction type record |
| Copybook | CVTRA04Y | 12 | Transaction category record |
| Copybook | CVTRA02Y | 13 | Disclosure group (interest rates) |
| Copybook | CSLKPCDY | 1,318 | Lookup/validation data (area codes, states, ZIPs) |
| VSAM File | TRANTYPE | KSDS | Transaction type reference |
| VSAM File | TRANCATG | KSDS | Transaction category reference |
| VSAM File | DISCGRP | KSDS | Disclosure groups |
| JCL | TRANTYPE, TRANCATG, DISCGRP | -- | Reference data VSAM definitions |

#### Data Ownership
- **Owns:** All reference/lookup tables
- **Publishes:** Type codes, category codes, interest rates, validation data
- **Consumed by:** Transaction Management, Batch Processing, Account Management, Reporting

#### Extraction Seam Analysis

| Seam Type | Description | Coupling Strength |
|---|---|---|
| **Read-only consumers** | All other contexts only read reference data | **Weak** -- clean read-only API boundary |
| **Batch lookup** | Batch programs join against reference tables | **Weak** -- database JOIN or cached lookup |

**Extraction Difficulty:** **Very Low**
- Pure read-only data consumed by other contexts
- No write coupling -- reference data is loaded via JCL/IDCAMS
- Perfect candidate for a shared database table or lightweight API

**Target:** Shared database tables accessed by all services, or a lightweight `reference-data-service` with caching.

---

### BC-8: Reporting

**Domain Purpose:** Generate on-demand and batch reports for transaction activity, account statements, and category balances.

#### Assets

| Type | Asset | Lines | Role |
|---|---|---|---|
| Online Program | CORPT00C | 649 | Report request (online trigger) |
| Batch Program | CBTRN03C | 649 | Transaction detail report |
| Batch Program | CBSTM03A/B | 1,154 | Statement generation |
| BMS Map | CORPT00 | -- | Report request screen |
| JCL | TRANREPT, CREASTMT, PRTCATBL | -- | Report/statement jobs |

#### Data Ownership
- **Owns:** Report output files, report formatting rules
- **Consumes:** TRANSACT, ACCTDATA, CUSTDATA, CARDXREF, reference tables (read-only)

#### Extraction Seam Analysis

| Seam Type | Description | Coupling Strength |
|---|---|---|
| **Read-only data access** | Reports only read from master files | **Weak** -- database queries |
| **Statement overlap** | CBSTM03A/B is in both Batch Processing and Reporting contexts | **Medium** -- assign to Reporting, trigger from batch orchestrator |

**Extraction Difficulty:** **Low**
- Pure read-only operations against other contexts' data
- Report formatting is self-contained
- Can be extracted as a standalone reporting service

**Target Service:** `reporting-service` (Spring Batch + JasperReports)
- REST endpoint: `POST /reports/transaction-detail`, `POST /reports/statements`
- Reads from shared database (no owned tables except report output storage)
- Triggered by batch orchestrator or on-demand via API

---

## Cross-Cutting Concerns

### CC-1: Card-Account Cross-Reference (CARDXREF / CVACT03Y)

The cross-reference file linking cards to accounts and customers is the **highest-coupling data structure** in the application.

| Context | Access Pattern |
|---|---|
| Account Management | Read (card-to-account lookup) |
| Card Management | Read/Write (card-account linking) |
| Transaction Management | Read (card validation during transaction add) |
| Bill Payment | Read (card-to-account for payment) |
| Batch Processing | Read (transaction posting, interest calc, statements) |

**Resolution:** Assign ownership to **Card Management** (BC-3). Other contexts access via `card-service` API or read-replica of the cross-reference table.

### CC-2: Communication Area (COCOM01Y)

The COMMAREA is passed between all online CICS programs carrying session state (user ID, user type, current account/card/customer context).

**Resolution:** Replace with **JWT token** (user identity) + **API request parameters** (account/card/customer IDs). No shared state between services.

### CC-3: Date/Time Utilities (CSUTLDTC, CSDAT01Y, CODATECN)

Date validation and formatting utility used by CORPT00C and COTRN02C (calls CSUTLDTC which uses Language Environment CEEDAYS).

**Resolution:** Replace with `java.time` API. No shared service needed -- each service includes its own date handling.

### CC-4: Common UI Components (COTTL01Y, CSMSG01Y, CSMSG02Y, CSSETATY)

Title bars, messages, and attribute settings shared across all BMS maps.

**Resolution:** Frontend component library (shared React/Angular components). Not a backend concern.

---

## Coupling Matrix

Coupling strength between bounded contexts (0 = none, 1 = weak, 2 = medium, 3 = strong):

| | BC-1 Identity | BC-2 Account | BC-3 Card | BC-4 Txn Online | BC-5 Payment | BC-6 Batch | BC-7 Ref Data | BC-8 Reporting |
|---|---|---|---|---|---|---|---|---|
| **BC-1 Identity** | -- | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
| **BC-2 Account** | 0 | -- | 2 | 1 | 3 | 3 | 1 | 1 |
| **BC-3 Card** | 0 | 2 | -- | 2 | 1 | 2 | 0 | 1 |
| **BC-4 Txn Online** | 0 | 1 | 2 | -- | 0 | 3 | 1 | 1 |
| **BC-5 Payment** | 0 | 3 | 1 | 0 | -- | 0 | 0 | 0 |
| **BC-6 Batch** | 0 | 3 | 2 | 3 | 0 | -- | 2 | 2 |
| **BC-7 Ref Data** | 0 | 1 | 0 | 1 | 0 | 2 | -- | 1 |
| **BC-8 Reporting** | 0 | 1 | 1 | 1 | 0 | 2 | 1 | -- |

### Key Observations

1. **BC-1 (Identity)** has **zero coupling** to all other contexts -- ideal first extraction
2. **BC-7 (Reference Data)** has only **inbound weak coupling** -- ideal second extraction
3. **BC-6 (Batch Processing)** has **strong coupling** to Account (3), Transaction (3), and Card (2) -- extract last
4. **BC-2 (Account)** and **BC-5 (Payment)** have **strong bidirectional coupling** (3) -- consider merging or extracting together
5. **BC-3 (Card)** and **BC-4 (Transaction Online)** have **medium coupling** (2) via CARDXREF -- manageable with API

---

## Extraction Order Recommendation

Based on coupling analysis, extract in this order (lowest coupling first):

| Order | Context | Extraction Difficulty | Rationale |
|---|---|---|---|
| 1 | BC-1: Identity & Access | Low | Zero coupling to other contexts. Gateway for all services. |
| 2 | BC-7: Reference Data | Very Low | Read-only data. No write coupling. |
| 3 | BC-8: Reporting | Low | Read-only consumer of other contexts' data. |
| 4 | BC-3: Credit Card | Medium | Owns CARDXREF. Medium coupling to Account. |
| 5 | BC-4: Transaction (Online) | Medium | Read path clean; write path shared with Batch. |
| 6 | BC-5: Bill Payment | Medium | Strong coupling to Account; small scope (1 program). |
| 7 | BC-2: Account Management | High | Core entity with many dependents. Extract after Card (CARDXREF resolved). |
| 8 | BC-6: Batch Processing | Very High | Strongest coupling. Extract last when all other contexts are stable. |

---

## Target Microservice Architecture

```
                    ┌──────────────┐
                    │  API Gateway │
                    │  (Spring     │
                    │   Cloud GW)  │
                    └──────┬───────┘
                           │
          ┌────────────────┼────────────────┐
          │                │                │
 ┌────────▼──────┐  ┌─────▼──────┐  ┌──────▼───────┐
 │  identity-    │  │  account-  │  │  card-       │
 │  service      │  │  service   │  │  service     │
 │               │  │            │  │              │
 │  - Auth/JWT   │  │  - Account │  │  - Card CRUD │
 │  - User CRUD  │  │  - Customer│  │  - CARDXREF  │
 │  - RBAC       │  │  - Interest│  │  - PCI vault │
 └───────────────┘  └─────┬──────┘  └──────┬───────┘
                          │                │
          ┌───────────────┼────────────────┤
          │               │                │
 ┌────────▼──────┐  ┌─────▼──────┐  ┌──────▼───────┐
 │  transaction- │  │  payment-  │  │  reporting-  │
 │  service      │  │  service   │  │  service     │
 │               │  │            │  │              │
 │  - Txn CRUD   │  │  - Payment │  │  - Statements│
 │  - Txn browse │  │  - Saga    │  │  - Reports   │
 └───────────────┘  └────────────┘  └──────────────┘
                                           │
                    ┌──────────────┐  ┌─────▼──────┐
                    │  reference-  │  │  batch-    │
                    │  data        │  │  processing│
                    │  (shared DB) │  │  service   │
                    │              │  │            │
                    │  - Types     │  │  - Posting │
                    │  - Categories│  │  - Interest│
                    │  - Disc grps │  │  - Stmts   │
                    └──────────────┘  └────────────┘
```

### Service Communication Patterns

| Pattern | Use Case |
|---|---|
| **Synchronous REST** | Online CRUD operations (card lookup, account view, transaction browse) |
| **Saga (choreography)** | Bill payment (reserve balance → create transaction → confirm) |
| **Event-driven** | Transaction created → triggers batch pickup; Account updated → audit log |
| **Batch job orchestration** | Spring Batch job dependencies or AWS Step Functions for nightly cycle |
| **Shared database** | Reference data tables shared across services (read-only) |
