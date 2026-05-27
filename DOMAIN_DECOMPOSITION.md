# CardDemo Domain Decomposition

## 1. Overview

This document identifies bounded contexts within the CardDemo mainframe application using Domain-Driven Design (DDD) principles. Each bounded context maps to a candidate microservice in the target architecture. Extraction seam analysis identifies the integration points, shared data, and coupling patterns that must be addressed during decomposition.

---

## 2. Bounded Context Map

```
┌──────────────────────────────────────────────────────────────────────────┐
│                        CardDemo System Boundary                         │
│                                                                         │
│  ┌─────────────┐    ┌──────────────┐    ┌───────────────┐              │
│  │  Identity &  │    │   Customer   │    │   Account     │              │
│  │  Access      │───▶│   Profile    │◀──▶│   Management  │              │
│  │  (BC-1)      │    │   (BC-2)     │    │   (BC-3)      │              │
│  └─────────────┘    └──────┬───────┘    └───────┬───────┘              │
│                            │                    │                       │
│                            │    ┌───────────────┤                       │
│                            │    │               │                       │
│                    ┌───────▼────▼──┐    ┌───────▼───────┐              │
│                    │    Card       │    │  Transaction  │              │
│                    │    Lifecycle  │◀──▶│  Processing   │              │
│                    │    (BC-4)     │    │  (BC-5)       │              │
│                    └──────────────┘    └───────┬───────┘              │
│                                               │                       │
│                    ┌──────────────┐    ┌───────▼───────┐              │
│                    │  Branch      │    │  Financial    │              │
│                    │  Migration   │◀───│  Operations   │              │
│                    │  (BC-7)      │    │  (BC-6)       │              │
│                    └──────────────┘    └───────────────┘              │
│                                                                         │
│  ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┐            │
│  │              Optional Contexts                          │            │
│  │  ┌──────────────┐  ┌──────────┐  ┌───────────────┐    │            │
│  │  │Authorization │  │ Tran Type│  │  Messaging    │    │            │
│  │  │ (BC-8)       │  │ Mgmt     │  │  Gateway      │    │            │
│  │  │IMS/DB2/MQ    │  │ (BC-9)   │  │  (BC-10)      │    │            │
│  │  └──────────────┘  └──────────┘  └───────────────┘    │            │
│  └ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┘            │
└──────────────────────────────────────────────────────────────────────────┘

Legend:  ───▶ Depends on    ◀──▶ Bidirectional dependency
        ─ ─  Optional boundary
```

---

## 3. Bounded Contexts

### BC-1: Identity & Access Context

**Aggregate Root:** `UserCredential`

| Attribute | Detail |
|---|---|
| **Core Entity** | `SEC-USER-DATA` (CSUSR01Y -- 80B record) |
| **Programs** | `COSGN00C` (sign-on), `COUSR00C/01C/02C/03C` (admin CRUD) |
| **Data Store** | USRSEC VSAM KSDS |
| **Ubiquitous Language** | User ID, Password, User Type (Admin/Regular), Sign-on, Session |
| **Invariants** | User ID uniqueness; password required; type must be 'A' or 'U' |

**Domain Events:**
- `UserAuthenticated` -- successful sign-on
- `UserCreated` / `UserUpdated` / `UserDeleted` -- admin operations
- `AuthenticationFailed` -- invalid credentials

**Extraction Notes:** Self-contained. Only external dependency is the commarea (`COCOM01Y`) used to pass user context to downstream programs. In the target architecture, this becomes a JWT/session token.

---

### BC-2: Customer Profile Context

**Aggregate Root:** `Customer`

| Attribute | Detail |
|---|---|
| **Core Entity** | `CUSTOMER-RECORD` (CVCUS01Y -- 500B record) |
| **Fields** | ID, Name (first/middle/last), Address (3 lines + state + country + zip), Phone (2), SSN, Govt ID, DOB, EFT Account, Primary Card Holder Indicator, FICO Score |
| **Programs** | `CBCUS01C`, `CBACT01C`, `CBACT02C`, `CBACT03C` |
| **Data Store** | CUSTDAT VSAM KSDS |
| **Ubiquitous Language** | Customer, Profile, Credit Score, Primary Card Holder |

**Domain Events:**
- `CustomerProfileViewed`
- `CustomerProfileUpdated`

**Extraction Notes:** Customer data is referenced by Transaction Processing (for card-to-customer lookups via XREF), Statement Generation (customer name/address on statements), and Branch Migration (full customer export). The XREF file (`CVACT03Y`) is the primary coupling point -- it links Customer ID to Card Number and Account ID.

---

### BC-3: Account Management Context

**Aggregate Root:** `Account`

| Attribute | Detail |
|---|---|
| **Core Entity** | `ACCOUNT-RECORD` (CVACT01Y -- 300B record) |
| **Fields** | Account ID, Active Status, Current Balance, Credit Limit, Cash Credit Limit, Open/Expiration/Reissue Dates, Cycle Credit/Debit, ZIP, Group ID |
| **Programs** | `COACTVWC` (view), `COACTUPC` (update -- 4,236 LOC) |
| **Data Store** | ACCTDAT VSAM KSDS |
| **Ubiquitous Language** | Account, Balance, Credit Limit, Cycle, Group, Status |
| **Invariants** | Balance must not exceed credit limit (soft); account status transitions (Active/Closed) |

**Domain Events:**
- `AccountViewed`
- `AccountUpdated` (status, limits, address changes)
- `BalanceAdjusted` (from transaction posting)

**Extraction Notes:** `COACTUPC` is the single most complex program (4,236 LOC) with extensive field-level validation including US phone number format checking, alpha-only/alphanumeric validation, signed number parsing, and yes/no flag validation. This validation logic is a key extraction candidate.

**Shared Data Coupling:**
- Reads XREF to resolve Card → Account mappings
- Account balance updated by Batch Transaction Processing (`CBTRN02C`)
- Account balance updated by Interest Calculation (`CBACT04C`)
- Account balance read by Bill Payment (`COBIL00C`)

---

### BC-4: Card Lifecycle Context

**Aggregate Root:** `CreditCard`

| Attribute | Detail |
|---|---|
| **Core Entity** | `CARD-RECORD` (CVACT02Y -- 150B record) |
| **Fields** | Card Number, Account ID, CVV, Embossed Name, Expiration Date, Active Status |
| **Cross-Reference** | `CARD-XREF-RECORD` (CVACT03Y -- 50B: Card Num → Customer ID + Account ID) |
| **Programs** | `COCRDLIC` (list -- 1,459 LOC), `COCRDSLC` (view -- 887 LOC), `COCRDUPC` (update -- 1,560 LOC) |
| **Data Store** | CARDDAT VSAM KSDS, CARDXREF VSAM KSDS |
| **Ubiquitous Language** | Card, Embossed Name, CVV, Expiration, Cross-Reference |

**Domain Events:**
- `CardListed` / `CardViewed` / `CardUpdated`
- `CardActivated` / `CardDeactivated`

**Extraction Notes:** The XREF file is the critical shared data structure. It serves as the denormalized join table linking Cards, Customers, and Accounts. In the target architecture, this becomes either:
1. A shared database table with foreign keys, or
2. An event-driven synchronization pattern where each service maintains its own projections

---

### BC-5: Transaction Processing Context

**Aggregate Root:** `Transaction`

| Attribute | Detail |
|---|---|
| **Core Entity** | `TRAN-RECORD` (CVTRA05Y -- 350B) |
| **Supporting Entities** | `DALYTRAN-RECORD` (CVTRA06Y), `TRAN-CAT-BAL-RECORD` (CVTRA01Y), `TRAN-TYPE-RECORD` (CVTRA03Y), `TRAN-CAT-RECORD` (CVTRA04Y) |
| **Fields** | Transaction ID, Type Code, Category Code, Source, Description, Amount, Merchant (ID/Name/City/ZIP), Card Number, Timestamps (Original/Processed) |
| **Online Programs** | `COTRN00C` (list), `COTRN01C` (view), `COTRN02C` (add) |
| **Batch Programs** | `CBTRN02C` (POSTTRAN -- 731 LOC), `CBTRN01C`, `CBTRN03C` (report) |
| **Data Stores** | TRANSACT VSAM KSDS (with AIX), DALYTRAN (sequential) |
| **JCL** | POSTTRAN, COMBTRAN, TRANBKP, TRANFILE, TRANIDX |
| **Ubiquitous Language** | Transaction, Posting, Daily Transaction, Reject, Category Balance |

**Domain Events:**
- `TransactionCreated` (online add)
- `DailyTransactionsPosted` (batch POSTTRAN)
- `TransactionRejected` (validation failure in batch)
- `CategoryBalanceUpdated`

**Extraction Notes:** This is the highest-coupling context. It reads/writes:
- TRANSACT (own data)
- DALYTRAN (input for batch posting)
- XREF (card validation)
- ACCTDAT (balance updates during posting)
- TCATBALF (category balance updates)
- DALYREJS (reject output)

The batch posting job (`CBTRN02C`) is the most critical extraction challenge because it atomically updates multiple VSAM files. In the target architecture, this requires a saga pattern or distributed transaction.

---

### BC-6: Financial Operations Context

**Aggregate Root:** `BillingCycle`

| Attribute | Detail |
|---|---|
| **Core Entities** | `DIS-GROUP-RECORD` (CVTRA02Y -- disclosure groups with interest rates), `TRAN-CAT-BAL-RECORD` (CVTRA01Y) |
| **Programs** | `CBACT04C` (interest calc -- 652 LOC), `COBIL00C` (bill payment -- 572 LOC) |
| **Data Stores** | TCATBALF, DISCGRP, ACCTDAT, XREF, TRANSACT |
| **JCL** | INTCALC |
| **Control-M** | MONTHLY-InterestCalculation (CLOSEFIL → INTCALC → COMBTRAN → WAITSTEP → OPENFIL) |
| **Ubiquitous Language** | Interest Rate, Disclosure Group, Category Balance, Bill Payment, Cycle |

**Domain Events:**
- `InterestCalculated`
- `BillPaymentProcessed`
- `StatementGenerated`

**Sub-contexts:**
1. **Interest Calculation** (`CBACT04C`): Reads TCATBALF sequentially, looks up disclosure group interest rates, calculates interest per account per category, writes to TRANSACT
2. **Bill Payment** (`COBIL00C`): Online CICS program that creates a payment transaction and adjusts account balance
3. **Statement Generation** (`CBSTM03A`, `CBSTM03B`): Reads all transactions for an account, formats plain-text and HTML statements

**Extraction Notes:** Interest calculation has complex multi-file traversal: TCATBALF → DISCGRP (rate lookup by account group + tran type + tran category) → XREF (account to card mapping) → ACCTDAT (balance update). This chain must be replicated exactly to preserve financial accuracy.

---

### BC-7: Branch Migration Context

**Aggregate Root:** `MigrationPackage`

| Attribute | Detail |
|---|---|
| **Core Entity** | `EXPORT-RECORD` (CVEXPORT -- 500B multi-type record with REDEFINES) |
| **Record Types** | Customer (type C), Account (type A), Transaction (type T), Card XREF (type X), Card (type D) |
| **Programs** | `CBEXPORT` (582 LOC), `CBIMPORT` (487 LOC) |
| **Data Stores** | All master VSAM files (read for export, write for import) |
| **JCL** | CBEXPORT, CBIMPORT |
| **Ubiquitous Language** | Export, Import, Branch, Region, Sequence, Checksum |

**Domain Events:**
- `DataExportStarted` / `DataExportCompleted`
- `DataImportStarted` / `DataImportCompleted`
- `ImportValidationFailed`

**Extraction Notes:** This context is a natural fit for a standalone data migration service. It already encapsulates cross-cutting data access patterns. The COMP/COMP-3 packed decimal fields in `CVEXPORT` require careful numeric conversion during migration.

---

### BC-8: Authorization Processing Context (Optional)

**Aggregate Root:** `AuthorizationRequest`

| Attribute | Detail |
|---|---|
| **Programs** | `COPAUA0C` (process -- 1,026 LOC), `COPAUS0C` (summary -- 1,032 LOC), `COPAUS1C` (details -- 604 LOC), `COPAUS2C`, `CBPAUP0C` (purge -- 386 LOC), plus IMS utilities |
| **Data Stores** | IMS DB (DBPAUTP0, DBPAUTX0), DB2 (AUTHFRDS table), VSAM |
| **Messaging** | IBM MQ (trigger-based authorization) |
| **CICS Transactions** | CPVS, CPVD, CP00 |
| **Ubiquitous Language** | Authorization Request, Pending Authorization, Approval, Purge, Fraud Detection |

**Extraction Notes:** Most complex integration surface. Spans 3 data technologies (IMS, DB2, VSAM) plus MQ messaging. IMS hierarchical database has no direct modern equivalent -- requires schema redesign. The MQ trigger pattern maps to event-driven processing (Kafka consumer groups or SQS Lambda triggers).

---

### BC-9: Transaction Type Management Context (Optional)

**Aggregate Root:** `TransactionType`

| Attribute | Detail |
|---|---|
| **Programs** | `COTRTLIC` (list -- 2,098 LOC), `COTRTUPC` (add/edit -- 1,702 LOC), `COBTUPDT` (batch -- 237 LOC) |
| **Data Stores** | DB2 tables |
| **JCL** | MNTTRDB2, TRANEXTR |
| **Control-M** | WEEKLY-TransactionTypesDBRefresh, WEEKLY-DisclosureGroupsRefresh |
| **Ubiquitous Language** | Transaction Type, Category, Reference Data |

**Extraction Notes:** Already DB2-based. SQL can be ported almost directly to JPA/Hibernate. The cursor-based pagination in `COTRTLIC` maps to Spring Data's `Pageable`. Weekly batch refresh from DB2 to VSAM flat files would be replaced by direct database queries.

---

### BC-10: Messaging Gateway Context (Optional)

**Aggregate Root:** `MessageExchange`

| Attribute | Detail |
|---|---|
| **Programs** | `CODATE01` (system date inquiry -- 524 LOC), `COACCT01` (account inquiry -- 620 LOC) |
| **Data Stores** | VSAM files, MQ queues |
| **CICS Transactions** | CDRD, CDRA |
| **Ubiquitous Language** | Request, Response, Queue, Inquiry |

**Extraction Notes:** Demonstrates the MQ request/response pattern. In the target architecture, these become REST API endpoints or async event consumers, eliminating the need for a separate messaging gateway context.

---

## 4. Extraction Seam Analysis

### 4.1 Data Seams (Shared VSAM Files)

The primary coupling mechanism in CardDemo is shared VSAM file access. Multiple programs read/write the same files:

| VSAM File | Read By | Written By | Seam Type |
|---|---|---|---|
| **ACCTDAT** (Account) | BC-3, BC-4, BC-5, BC-6, BC-7, BC-8 | BC-3, BC-5, BC-6 | **Critical** -- highest fan-out |
| **CARDXREF** (Cross-Reference) | BC-3, BC-4, BC-5, BC-6, BC-7, BC-8 | BC-4 | **Critical** -- join table |
| **TRANSACT** (Transaction) | BC-5, BC-6, BC-7, BC-8 | BC-5, BC-6 | **High** -- core financial data |
| **CUSTDAT** (Customer) | BC-2, BC-7, BC-8 | BC-2 | **Medium** -- mostly read |
| **CARDDAT** (Card) | BC-4, BC-7 | BC-4 | **Medium** -- scoped to card context |
| **USRSEC** (Security) | BC-1 | BC-1 | **Low** -- fully self-contained |
| **TCATBALF** (Cat Balance) | BC-5, BC-6 | BC-5, BC-6 | **High** -- financial aggregation |
| **DISCGRP** (Disclosure) | BC-6 | (batch load only) | **Low** -- reference data |
| **DALYTRAN** (Daily Trans) | BC-5 | (external feed) | **Low** -- inbound only |
| **TRANTYPE/TRANCATG** | BC-5, BC-6, BC-8, BC-9 | BC-9 | **Medium** -- reference data |

### 4.2 Extraction Seam Strategies

#### Seam S-1: Account Data (ACCTDAT) -- Anti-Corruption Layer

**Problem:** 6 bounded contexts access account data. Direct VSAM sharing cannot continue post-migration.

**Strategy:** Create an Account Service as the single source of truth. Other services access account data via:
- **Synchronous:** REST API for real-time lookups (view, validation)
- **Asynchronous:** Domain events (`BalanceAdjusted`, `AccountUpdated`) published to Kafka for eventual consistency

**Transition Pattern:**
1. Introduce a database-backed Account Service
2. Create a Change Data Capture (CDC) bridge from VSAM to the new database
3. Gradually redirect consumers from VSAM to API/events
4. Decommission VSAM access when all consumers migrated

---

#### Seam S-2: Cross-Reference (CARDXREF) -- Shared Kernel

**Problem:** XREF is the denormalized join table linking Cards, Customers, and Accounts. It's read by 6 contexts.

**Strategy:** Decompose the XREF into domain-owned relationships:
- **Card Service** owns Card → Account mapping
- **Customer Service** owns Customer → Account mapping
- Each service maintains its own projection of the relationships it needs

**Transition Pattern:**
1. Replicate XREF data into each consuming service's database
2. Publish `CardLinked`, `CustomerLinked` events when relationships change
3. Each service builds local read models from events
4. Remove XREF VSAM when all services have local projections

---

#### Seam S-3: Transaction Data (TRANSACT) -- Event Sourcing Candidate

**Problem:** Transaction data is written by online and batch processes, read by reporting, billing, and migration.

**Strategy:** Transaction Processing Service owns the write path. Consumers subscribe to events:
- `TransactionCreated` → consumed by Reporting, Billing, Migration
- `TransactionPosted` → consumed by Account Service (balance update)

**Transition Pattern:**
1. Dual-write: new Transaction Service writes to both new DB and VSAM
2. Consumers gradually shift from VSAM reads to event consumption
3. Implement materialized views for read-heavy consumers (reporting)

---

#### Seam S-4: Batch Processing -- Saga Pattern

**Problem:** `CBTRN02C` (POSTTRAN) atomically updates TRANSACT, ACCTDAT, and TCATBALF in a single batch run.

**Strategy:** Replace with a saga orchestrator:
1. Read daily transactions (ItemReader)
2. Validate against XREF (service call)
3. Post to Transaction Service (command)
4. Update Account balance (command)
5. Update Category Balance (command)
6. On failure: compensate (write to reject queue)

**Transition Pattern:**
1. Implement Spring Batch job with same file I/O patterns
2. Replace VSAM reads with service calls
3. Implement compensation logic for partial failures
4. Use batch job metadata table for restart/recovery

---

### 4.3 Communication Seams (COMMAREA/XCTL)

| Seam | Current Mechanism | Target Mechanism |
|---|---|---|
| Sign-on → Menu | COMMAREA with user context | JWT token in HTTP header |
| Menu → Any Screen | CICS XCTL with COMMAREA | SPA client-side routing |
| Online → Batch Submit | TDQ (Transient Data Queue) | REST API trigger + async job queue |
| MQ Request/Response | IBM MQ put/get | Kafka request-reply or REST |
| Batch Job Chaining | JCL COND codes + Control-M | Spring Batch step flow + K8s CronJob |

### 4.4 Temporal Seams (Batch Windows)

| Schedule | Current Jobs | Extraction Impact |
|---|---|---|
| **Daily** | CLOSEFIL → TRANBKP → WAITSTEP → OPENFIL | Eliminated in target (no file-level locking needed with RDBMS) |
| **Weekly** | MNTTRDB2 → TRANEXTR, DISCGRP refresh | Replaced by direct DB queries; VSAM refresh jobs eliminated |
| **Monthly** | CLOSEFIL → INTCALC → COMBTRAN → WAITSTEP → OPENFIL | Converted to Spring Batch scheduled job |

---

## 5. Dependency Graph

```
BC-1 (Identity)
  └──▶ [produces: UserAuthenticated event]
       └──▶ BC-2, BC-3, BC-4, BC-5 [consume: user context via token]

BC-2 (Customer)
  └──▶ [produces: CustomerProfile API]
       └──▶ BC-7 (Migration) [consumes: customer data for export]
       └──▶ BC-6 (Financial) [consumes: customer name for statements]

BC-3 (Account)
  └──▶ [produces: AccountData API, BalanceAdjusted event]
       └──▶ BC-4 (Card) [consumes: account status for card validation]
       └──▶ BC-5 (Transaction) [consumes: account for posting]
       └──▶ BC-6 (Financial) [consumes: balance for interest calc]
       └──▶ BC-7 (Migration) [consumes: account data for export]

BC-4 (Card)
  └──▶ [produces: CardData API, XREF resolution]
       └──▶ BC-5 (Transaction) [consumes: card-to-account mapping]
       └──▶ BC-7 (Migration) [consumes: card data for export]

BC-5 (Transaction)
  └──▶ [produces: TransactionCreated event, TransactionPosted event]
       └──▶ BC-3 (Account) [triggers: balance adjustment]
       └──▶ BC-6 (Financial) [triggers: category balance update]
       └──▶ BC-7 (Migration) [consumes: transaction data for export]

BC-6 (Financial)
  └──▶ [produces: InterestCalculated event, StatementGenerated event]
       └──▶ BC-3 (Account) [triggers: interest balance adjustment]

BC-7 (Migration)
  └──▶ [consumes from: BC-2, BC-3, BC-4, BC-5]
       (Read-only aggregator -- no upstream dependencies)
```

---

## 6. Recommended Service Boundaries

| Service | Bounded Contexts | Database | API Style |
|---|---|---|---|
| **identity-service** | BC-1 | PostgreSQL (users schema) | REST + OAuth2 |
| **customer-service** | BC-2 | PostgreSQL (customers schema) | REST |
| **account-service** | BC-3 | PostgreSQL (accounts schema) | REST + Events |
| **card-service** | BC-4 | PostgreSQL (cards schema) | REST + Events |
| **transaction-service** | BC-5 | PostgreSQL (transactions schema) | REST + Events |
| **billing-service** | BC-6 | PostgreSQL (billing schema) | REST + Spring Batch |
| **migration-service** | BC-7 | (reads from other services) | REST + Batch |
| **authorization-service** | BC-8 | PostgreSQL (authorizations schema) | REST + Events |
| **reference-data-service** | BC-9, BC-10 | PostgreSQL (reference schema) | REST |

**Database-per-Service Pattern:** Each service owns its schema. No cross-service direct database access. All inter-service communication via REST APIs or domain events on Kafka topics.
