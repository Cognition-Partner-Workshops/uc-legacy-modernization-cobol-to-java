# CardDemo Domain Decomposition

## Overview

This document identifies bounded contexts within the CardDemo COBOL/CICS application using Domain-Driven Design (DDD) principles, maps program-to-program and program-to-data dependencies, and analyzes extraction seams for incremental migration to Java microservices.

---

## Bounded Context Map

```
+----------------------------------------------------------------------+
|                        CardDemo Application                          |
|                                                                      |
|  +------------------+     +--------------------+                     |
|  | Identity &       |     | Account            |                     |
|  | Access Mgmt      |---->| Management         |                     |
|  |                  |     |                    |                     |
|  | COSGN00C         |     | COACTVWC           |                     |
|  | COUSR00C-03C     |     | COACTUPC           |                     |
|  | COADM01C         |     | CVACT01Y           |                     |
|  | COMEN01C         |     | CVCUS01Y           |                     |
|  | CSUSR01Y         |     +--------+-----------+                     |
|  +------------------+              |                                 |
|                                    | shares ACCTDAT, CUSTDAT         |
|                                    v                                 |
|  +------------------+     +--------+-----------+                     |
|  | Payment          |     | Card               |                     |
|  | Processing       |<--->| Management         |                     |
|  |                  |     |                    |                     |
|  | COBIL00C         |     | COCRDLIC           |                     |
|  | CBTRN02C (batch) |     | COCRDSLC           |                     |
|  | CBACT04C (batch) |     | COCRDUPC           |                     |
|  | CVTRA05Y/06Y     |     | CVACT02Y           |                     |
|  +--------+---------+     | CVACT03Y (xref)    |                     |
|           |               +--------------------+                     |
|           |                                                          |
|           v                                                          |
|  +------------------+     +--------------------+                     |
|  | Transaction      |     | Reporting &        |                     |
|  | Management       |     | Statements         |                     |
|  |                  |     |                    |                     |
|  | COTRN00C         |     | CORPT00C           |                     |
|  | COTRN01C         |     | CBTRN03C           |                     |
|  | COTRN02C         |     | CBSTM03A/B         |                     |
|  | CVTRA05Y         |     | CVTRA05Y (read)    |                     |
|  +------------------+     +--------------------+                     |
|                                                                      |
|  +------------------+     +--------------------+                     |
|  | Authorization    |     | Reference Data     |                     |
|  | (Optional)       |     | (Optional)         |                     |
|  |                  |     |                    |                     |
|  | COPAUA0C         |     | COTRTUPC           |                     |
|  | COPAUS0C/1C/2C   |     | COTRTLIC           |                     |
|  | CBPAUP0C         |     | COBTUPDT           |                     |
|  +------------------+     +--------------------+                     |
+----------------------------------------------------------------------+
```

---

## Bounded Context Definitions

### BC-1: Identity & Access Management

**Purpose**: Authenticate users, manage sessions, enforce role-based access, and provide user CRUD operations for administrators.

| Attribute | Detail |
|-----------|--------|
| **Programs** | COSGN00C, COMEN01C, COADM01C, COUSR00C, COUSR01C, COUSR02C, COUSR03C |
| **Copybooks** | CSUSR01Y (user record), COCOM01Y (commarea), COMEN02Y (menu options), COTTL01Y, CSDAT01Y, CSMSG01Y |
| **BMS Maps** | COSGN00, COMEN01, COADM01, COUSR00-03 (6 maps) |
| **VSAM Files** | USRSEC (user security file) |
| **CICS Transactions** | CC00, CM00, CA01, CU00-CU03 |
| **Upstream Dependencies** | None -- this is the entry point |
| **Downstream Consumers** | All other bounded contexts (receives user identity) |

**Domain Entities**:
- `User` (SEC-USR-ID, SEC-USR-FNAME, SEC-USR-LNAME, SEC-USR-PWD, SEC-USR-TYPE)
- `Session` (derived from COMMAREA: CDEMO-USER-ID, CDEMO-USER-TYPE, navigation state)
- `MenuOption` (CDEMO-MENU-OPT-NUM, CDEMO-MENU-OPT-NAME, CDEMO-MENU-OPT-PGMNAME, CDEMO-MENU-OPT-USRTYPE)

**Aggregate Root**: `User`

---

### BC-2: Account Management

**Purpose**: View and update credit card account information including customer demographics, credit limits, balances, and account status.

| Attribute | Detail |
|-----------|--------|
| **Programs** | COACTVWC, COACTUPC |
| **Copybooks** | CVACT01Y (account), CVACT02Y (card), CVACT03Y (xref), CVCUS01Y (customer), CVCRD01Y (card work area) |
| **BMS Maps** | COACTVW, COACTUP (2 maps) |
| **VSAM Files** | ACCTDAT, CARDDAT, CUSTDAT, CARDAIX (alternate index), CXACAIX (xref alternate index) |
| **CICS Transactions** | CAVW, CAUP |
| **Upstream Dependencies** | BC-1 (user identity for access control) |
| **Downstream Consumers** | BC-3 Card Management (shares CARDDAT, CXACAIX), BC-4 Payment Processing (reads ACCTDAT) |

**Domain Entities**:
- `Account` (ACCT-ID, ACCT-ACTIVE-STATUS, ACCT-CURR-BAL, ACCT-CREDIT-LIMIT, ACCT-CASH-CREDIT-LIMIT, ACCT-OPEN-DATE, ACCT-EXPIRAION-DATE, ACCT-REISSUE-DATE, ACCT-CURR-CYC-CREDIT, ACCT-CURR-CYC-DEBIT, ACCT-ADDR-ZIP, ACCT-GROUP-ID)
- `Customer` (CUST-ID, names, addresses, phone numbers, SSN, DOB, FICO score, EFT account)
- `CardXref` (XREF-CARD-NUM, XREF-CUST-ID, XREF-ACCT-ID) -- linking entity

**Aggregate Root**: `Account` (with `Customer` as a separate aggregate linked by `CardXref`)

---

### BC-3: Card Management

**Purpose**: List, view, and update credit card details including card number, CVV, embossed name, expiration, and active status.

| Attribute | Detail |
|-----------|--------|
| **Programs** | COCRDLIC, COCRDSLC, COCRDUPC |
| **Copybooks** | CVACT02Y (card), CVACT03Y (xref), CVCRD01Y (card work area) |
| **BMS Maps** | COCRDLI, COCRDSL, COCRDUP (3 maps) |
| **VSAM Files** | CARDDAT, CARDAIX (alternate index by account) |
| **CICS Transactions** | CCLI, CCDL, CCUP |
| **Upstream Dependencies** | BC-1 (user identity), BC-2 (account context for filtered card lists) |
| **Downstream Consumers** | BC-4 Payment Processing (card number for transactions), BC-5 Transaction Management (card number lookups) |

**Domain Entities**:
- `Card` (CARD-NUM, CARD-ACCT-ID, CARD-CVV-CD, CARD-EMBOSSED-NAME, CARD-EXPIRAION-DATE, CARD-ACTIVE-STATUS)

**Aggregate Root**: `Card`

**Relationship to BC-2**: Cards belong to Accounts. The CARDAIX alternate index enables lookup of all cards for a given account. In the Java model, this becomes a `@OneToMany` relationship from Account to Card.

---

### BC-4: Payment Processing

**Purpose**: Handle bill payments (online) and batch transaction posting, interest calculation, and account balance updates.

| Attribute | Detail |
|-----------|--------|
| **Programs** | COBIL00C (online bill pay), CBTRN02C (batch posting), CBACT04C (batch interest calc) |
| **Copybooks** | CVTRA05Y (transaction), CVTRA06Y (daily transaction), CVACT01Y (account), CVACT03Y (xref), CVTRA01Y (category balance), CVTRA02Y (discount group) |
| **BMS Maps** | COBIL00 (1 map) |
| **VSAM Files** | TRANSACT, DALYTRAN, ACCTDAT, CCXREF/CXACAIX, TCATBAL, DISCGRP, DALYREJS |
| **CICS Transactions** | CB00 (online), N/A (batch) |
| **JCL Jobs** | POSTTRAN, INTCALC, COMBTRAN |
| **Upstream Dependencies** | BC-2 (account data), BC-3 (card/xref data) |
| **Downstream Consumers** | BC-5 Transaction Management (creates transaction records), BC-6 Reporting (transaction data) |

**Domain Entities**:
- `Transaction` (TRAN-ID, TRAN-TYPE-CD, TRAN-CAT-CD, TRAN-SOURCE, TRAN-DESC, TRAN-AMT, merchant info, TRAN-CARD-NUM, timestamps)
- `DailyTransaction` (same structure, staging table for batch input)
- `TransactionCategoryBalance` (TRANCAT-ACCT-ID, TRANCAT-TYPE-CD, TRANCAT-CD, balance)
- `DiscountGroup` (DIS-ACCT-GROUP-ID, DIS-TRAN-TYPE-CD, DIS-TRAN-CAT-CD, interest rate)
- `RejectedTransaction` (transaction data + validation trailer with reason code)

**Aggregate Root**: `Transaction`

**Critical Business Logic**:
1. **Bill Payment Flow**: Read account FOR UPDATE -> validate balance > 0 -> lookup card via XREF -> create transaction (type 02, cat 2) -> debit account -> rewrite account
2. **Batch Posting Flow**: Read daily trans -> validate card in XREF -> validate account exists -> write to TRANSACT -> update ACCTDAT balance -> update TCATBAL
3. **Interest Calculation**: Read TCATBAL sequentially -> group by account -> lookup discount rate -> compute monthly interest -> update account balance -> write interest transaction

---

### BC-5: Transaction Management (Online)

**Purpose**: Browse, search, view details, and manually add transaction records.

| Attribute | Detail |
|-----------|--------|
| **Programs** | COTRN00C, COTRN01C, COTRN02C |
| **Copybooks** | CVTRA05Y (transaction), CVACT01Y (account), CVACT03Y (xref) |
| **BMS Maps** | COTRN00, COTRN01, COTRN02 (3 maps) |
| **VSAM Files** | TRANSACT, CCXREF, CXACAIX, ACCTDAT |
| **CICS Transactions** | CT00, CT01, CT02 |
| **Upstream Dependencies** | BC-1 (user identity), BC-2/BC-3 (account/card validation for transaction add) |
| **Downstream Consumers** | BC-6 Reporting (transaction data) |

**Domain Entities**: Shares `Transaction` entity with BC-4.

**Aggregate Root**: `Transaction` (read-only in list/view; write in add)

**Note**: BC-4 and BC-5 both operate on the TRANSACT file. In the target architecture, they share the `transactions` database table but have distinct service responsibilities (BC-4 = processing engine, BC-5 = user-facing CRUD).

---

### BC-6: Reporting & Statements

**Purpose**: Generate transaction reports (monthly, yearly, custom) and produce account statements.

| Attribute | Detail |
|-----------|--------|
| **Programs** | CORPT00C (online report trigger), CBTRN03C (batch report), CBSTM03A, CBSTM03B (statement generation) |
| **Copybooks** | CVTRA05Y (transaction), CVACT01Y (account), CVCUS01Y (customer) |
| **BMS Maps** | CORPT00 (1 map) |
| **VSAM Files** | TRANSACT (read-only) |
| **CICS Transactions** | CR00 |
| **JCL Jobs** | TRANREPT, CREASTMT |
| **Upstream Dependencies** | BC-4/BC-5 (transaction data), BC-2 (account/customer data for statements) |
| **Downstream Consumers** | None (terminal output) |

**Domain Entities**:
- `ReportRequest` (report type, date range)
- `Statement` (account, period, line items)

**Aggregate Root**: `ReportRequest`

---

### BC-7: Authorization Service (Optional Module)

**Purpose**: Process pending transaction authorizations using IMS DB, DB2, and MQ integration. Mark fraudulent transactions.

| Attribute | Detail |
|-----------|--------|
| **Programs** | COPAUA0C (MQ trigger), COPAUS0C (summary), COPAUS1C (details), COPAUS2C (fraud marking), CBPAUP0C (batch purge) |
| **Data Stores** | IMS DB, DB2 tables, MQ queues |
| **Upstream Dependencies** | BC-4 (transaction data) |
| **Downstream Consumers** | BC-2 (account flags) |

---

### BC-8: Reference Data (Optional Module)

**Purpose**: Manage transaction type codes and categories (DB2-backed CRUD).

| Attribute | Detail |
|-----------|--------|
| **Programs** | COTRTUPC, COTRTLIC, COBTUPDT |
| **Data Stores** | DB2 transaction type table |
| **Upstream Dependencies** | None |
| **Downstream Consumers** | BC-4 (transaction type validation), BC-5 (type code display) |

---

## Extraction Seam Analysis

An **extraction seam** is a boundary in the existing system where a bounded context can be separated with minimal disruption. Seams are identified by analyzing coupling points: shared data stores, COMMAREA fields, XCTL/LINK calls, and VSAM file dependencies.

### Seam 1: Identity & Access Management (BC-1)

**Seam Type**: **Clean Seam** (lowest coupling)

**Coupling Points**:
| Coupling | Direction | Mechanism | Mitigation |
|----------|-----------|-----------|------------|
| Session initiation | BC-1 -> all | COMMAREA fields (CDEMO-USER-ID, CDEMO-USER-TYPE) | Replace with JWT token in HTTP header |
| Menu routing | BC-1 -> all | EXEC CICS XCTL to target programs | Replace with HTTP redirect / SPA routing |
| USRSEC file | BC-1 only | VSAM KSDS read/write | No shared access -- clean extraction |

**Extraction Approach**:
1. Deploy Spring Security service with JWT issuance
2. Create anti-corruption layer (ACL) that translates JWT claims back to COMMAREA format for remaining COBOL programs
3. BMS signon screen replaced by web login page
4. USRSEC VSAM migrated to `users` table in PostgreSQL

**Seam Quality**: **Excellent** -- USRSEC file is only accessed by BC-1 programs. The only downstream coupling is the COMMAREA user identity fields, which are easily mapped to JWT claims.

---

### Seam 2: Card Management (BC-3)

**Seam Type**: **Shared Data Seam** (moderate coupling via CARDDAT)

**Coupling Points**:
| Coupling | Direction | Mechanism | Mitigation |
|----------|-----------|-----------|------------|
| CARDDAT file | BC-3 read/write, BC-2 read | Shared VSAM KSDS | Dual-write during transition or shared database table |
| CARDAIX alternate index | BC-3 read, BC-2 read | VSAM AIX by account | SQL JOIN replaces AIX |
| Card context | BC-2 -> BC-3 | COMMAREA (CDEMO-ACCT-ID) | REST API parameter |
| XCTL navigation | BC-2 <-> BC-3 | COCRDLIC called from COACTVWC | REST API call or frontend routing |

**Extraction Approach**:
1. Create Card Service with REST API (GET /cards, GET /cards/{num}, PUT /cards/{num})
2. Migrate CARDDAT to `cards` table
3. During transition: Card Service writes to both database and VSAM (dual-write pattern)
4. Account View program modified to call Card Service API instead of direct VSAM read

**Seam Quality**: **Good** -- CARDDAT is primarily owned by BC-3. BC-2 reads are limited to account-filtered card lists, which can be served by the Card Service API.

---

### Seam 3: Account Management (BC-2)

**Seam Type**: **High-Coupling Seam** (ACCTDAT is read by many contexts)

**Coupling Points**:
| Coupling | Direction | Mechanism | Mitigation |
|----------|-----------|-----------|------------|
| ACCTDAT file | BC-2 write, BC-4 read/write, BC-5 read | Shared VSAM KSDS | Account Service becomes system of record; other services call its API |
| CUSTDAT file | BC-2 read, BC-6 read | Shared VSAM KSDS | Customer data served via Account Service API |
| CXACAIX xref | BC-2/3/4/5 read | Shared VSAM AIX | Database foreign keys eliminate need for separate xref file |
| Account context | BC-2 -> BC-3/4/5 | COMMAREA (CDEMO-ACCT-ID) | REST API path parameter |

**Extraction Approach**:
1. Create Account Service as the **system of record** for account and customer data
2. Expose REST APIs: GET/PUT /accounts/{id}, GET /accounts/{id}/customer
3. All other services call Account Service instead of reading ACCTDAT directly
4. During transition: Account Service dual-writes to database + VSAM
5. Longest transition period due to wide read access

**Seam Quality**: **Challenging** -- ACCTDAT is the most widely-shared file. The Account Service must be extracted carefully with a facade that supports both CICS and REST consumers during the transition.

---

### Seam 4: Transaction Management (BC-5) and Payment Processing (BC-4)

**Seam Type**: **Shared Data Seam** (TRANSACT file shared between online and batch)

**Coupling Points**:
| Coupling | Direction | Mechanism | Mitigation |
|----------|-----------|-----------|------------|
| TRANSACT file | BC-4 write, BC-5 read/write, BC-6 read | Shared VSAM KSDS | Single `transactions` table; services share via database |
| ACCTDAT file | BC-4 read/write | Cross-context | Call Account Service API |
| CCXREF/CXACAIX | BC-4/5 read | Cross-context | Database foreign keys |
| Batch cycle coordination | BC-4 batch -> BC-6 batch | JCL job dependencies (POSTTRAN before CREASTMT) | Spring Batch job orchestration |

**Extraction Approach**:
1. Extract BC-5 (Transaction CRUD) first as it's simpler (read + add)
2. Extract BC-4 (Payment Processing) second, calling Account Service for balance updates
3. Both share the `transactions` database table but have separate service boundaries
4. Batch jobs (CBTRN02C, CBACT04C) converted to Spring Batch jobs that use the same database

**Seam Quality**: **Moderate** -- The TRANSACT file is the central data artifact. The key insight is that BC-5 is primarily a UI layer over the transaction table, while BC-4 is the processing engine. They naturally share a database table without tight service coupling.

---

### Seam 5: Reporting & Statements (BC-6)

**Seam Type**: **Read-Only Seam** (only reads TRANSACT, ACCTDAT, CUSTDAT)

**Coupling Points**:
| Coupling | Direction | Mechanism | Mitigation |
|----------|-----------|-----------|------------|
| TRANSACT file | BC-6 read | VSAM KSDS sequential read | Read replica or direct database query |
| JCL job submission | BC-6 online -> batch | CICS TDQ to internal reader | Replace with async job queue (Spring Batch launcher) |
| Batch orchestration | BC-4 -> BC-6 | JCL dependency chain | Spring Batch flow dependency |

**Extraction Approach**:
1. Rewrite report generation as Spring Batch jobs reading from `transactions` table
2. Online report trigger replaced by REST API that launches async report job
3. Statement generation reads from database and produces PDF output

**Seam Quality**: **Excellent** -- BC-6 only reads data produced by other contexts. No write coupling. Can be extracted independently after the transaction data is in a database.

---

## Dependency Matrix

This matrix shows which programs access which VSAM files and how (R=Read, W=Write, U=Update/Rewrite, B=Browse):

| Program | USRSEC | ACCTDAT | CARDDAT | CUSTDAT | CCXREF | TRANSACT | DALYTRAN | TCATBAL | DISCGRP |
|---------|--------|---------|---------|---------|--------|----------|----------|---------|---------|
| COSGN00C | R | | | | | | | | |
| COMEN01C | | | | | | | | | |
| COADM01C | | | | | | | | | |
| COACTVWC | | R | R | R | R | | | | |
| COACTUPC | | R,U | R | R,U | R | | | | |
| COCRDLIC | | | R,B | | | | | | |
| COCRDSLC | | | R | | | | | | |
| COCRDUPC | | | R,U | | | | | | |
| COTRN00C | | | | | | R,B | | | |
| COTRN01C | | | | | | R | | | |
| COTRN02C | | R | | | R | R,W | | | |
| COBIL00C | | R,U | | | R | R,W | | | |
| CORPT00C | | | | | | | | | |
| COUSR00C | R,B | | | | | | | | |
| COUSR01C | R,W | | | | | | | | |
| COUSR02C | R,U | | | | | | | | |
| COUSR03C | R,U* | | | | | | | | |
| CBTRN02C | | R,U | | | R | W | R | | R,U |
| CBACT04C | | R,U | | | R | W | | R | R |
| CBSTM03A/B | | R | | R | | R | | | |

*\*COUSR03C performs a delete, implemented as a VSAM DELETE operation*

---

## Program Call Graph

```
COSGN00C (Signon CC00)
  |
  +--[Admin]--> COADM01C (Admin Menu CA01)
  |               |
  |               +---> COUSR00C (User List CU00)
  |               |       +---> COUSR02C (User Update CU02)
  |               |       +---> COUSR03C (User Delete CU03)
  |               +---> COUSR01C (User Add CU01)
  |
  +--[User]---> COMEN01C (Main Menu CM00)
                  |
                  +---> COACTVWC (Account View CAVW)
                  |       +---> COCRDLIC (Card List, drill-down)
                  |       +---> COCRDUPC (Card Update, drill-down)
                  +---> COACTUPC (Account Update CAUP)
                  +---> COCRDLIC (Card List CCLI)
                  |       +---> COCRDSLC (Card Detail CCDL)
                  |       +---> COCRDUPC (Card Update CCUP)
                  +---> COCRDSLC (Card Detail CCDL)
                  +---> COCRDUPC (Card Update CCUP)
                  +---> COTRN00C (Transaction List CT00)
                  |       +---> COTRN01C (Transaction View CT01)
                  +---> COTRN01C (Transaction View CT01)
                  +---> COTRN02C (Transaction Add CT02)
                  +---> CORPT00C (Reports CR00)
                  +---> COBIL00C (Bill Payment CB00)
                  +---> COPAUS0C (Pending Auth, optional)

Batch Job Chain:
  CLOSEFIL --> ACCTFILE/CARDFILE/CUSTFILE/XREFFILE/TRANFILE (data refresh)
           --> POSTTRAN (CBTRN02C)
           --> INTCALC  (CBACT04C)
           --> TRANBKP  (backup)
           --> COMBTRAN (combine)
           --> CREASTMT (CBSTM03A/B)
           --> TRANIDX  (rebuild index)
           --> OPENFIL
```

---

## Recommended Service Decomposition (Target Architecture)

| Java Service | Bounded Context(s) | Spring Module | Database Tables |
|-------------|-------------------|---------------|-----------------|
| `identity-service` | BC-1 | Spring Security + Web | `users` |
| `account-service` | BC-2 | Spring Web + Data JPA | `accounts`, `customers`, `card_xref` |
| `card-service` | BC-3 | Spring Web + Data JPA | `cards` |
| `transaction-service` | BC-4 + BC-5 | Spring Web + Data JPA + Batch | `transactions`, `daily_transactions`, `transaction_category_balances`, `discount_groups`, `rejected_transactions` |
| `payment-service` | BC-4 (online bill pay) | Spring Web + Data JPA | Calls `account-service` and `transaction-service` |
| `reporting-service` | BC-6 | Spring Batch + Web | Read-only access to `transactions`, `accounts`, `customers` |
| `authorization-service` | BC-7 (optional) | Spring Web + Kafka | `pending_authorizations` |
| `reference-data-service` | BC-8 (optional) | Spring Web + Data JPA | `transaction_types` |

---

## Anti-Corruption Layer (ACL) Design

During the strangler fig transition, an ACL mediates between the legacy CICS system and new Java services.

```
                 +------------------+
                 |   API Gateway    |
                 |  (Kong / Spring  |
                 |   Cloud Gateway) |
                 +--------+---------+
                          |
            +-------------+-------------+
            |                           |
   +--------v---------+    +-----------v-----------+
   | New Java Services |    | Anti-Corruption Layer |
   | (REST APIs)       |    | (CICS-to-REST Bridge) |
   +-------------------+    +-----------+-----------+
                                        |
                            +-----------v-----------+
                            | Legacy CICS Region    |
                            | (Remaining COBOL pgms)|
                            +-----------------------+
```

**ACL Responsibilities**:
1. **COMMAREA Translation**: Convert JWT claims to COMMAREA fields for COBOL programs still running on CICS
2. **VSAM Proxy**: For VSAM files that have been migrated to database, provide a CICS-accessible proxy that reads/writes to the database but presents a VSAM-like interface
3. **Event Bridge**: Publish domain events when legacy programs modify data, so new services can react
4. **Data Sync**: Bidirectional sync between VSAM files and database tables during transition (conflict resolution favors the system of record)

---

## Shared Kernel

The following artifacts are used across multiple bounded contexts and form a **shared kernel** that should be extracted as a common library:

| Artifact | Type | Used By | Target |
|----------|------|---------|--------|
| COCOM01Y | Copybook (COMMAREA) | All online programs | `CardDemoContext` DTO / JWT claims |
| COTTL01Y | Copybook (screen titles) | All online programs | Application constants |
| CSDAT01Y | Copybook (date formatting) | All online programs | `java.time.format.DateTimeFormatter` |
| CSMSG01Y | Copybook (common messages) | All online programs | Message resource bundle (i18n) |
| CSMSG02Y | Copybook (abend messages) | Most online programs | Exception hierarchy |
| CSUTLDTC | Program (date validation) | CORPT00C, COTRN02C | `java.time.LocalDate.parse()` |
| DFHAID | IBM copybook (AID bytes) | All online programs | Not needed (no 3270 terminal) |
| DFHBMSCA | IBM copybook (BMS attrs) | All online programs | Not needed (no 3270 terminal) |

**Target**: A `carddemo-common` Maven module containing shared DTOs, validation utilities, exception classes, and message constants.
