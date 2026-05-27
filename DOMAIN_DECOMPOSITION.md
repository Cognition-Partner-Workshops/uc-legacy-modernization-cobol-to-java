# CardDemo Domain Decomposition

## 1. Overview

This document identifies bounded contexts within the CardDemo mainframe application, maps COBOL programs and data stores to each context, analyzes the coupling seams between them, and defines extraction strategies for migrating to a microservice architecture.

---

## 2. Bounded Context Map

```
┌─────────────────────────────────────────────────────────────────────┐
│                        CardDemo System                              │
│                                                                     │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────────────┐  │
│  │  Identity &   │    │   Account    │    │   Credit Card        │  │
│  │  Access Mgmt  │───▶│   Domain     │◀──▶│   Domain             │  │
│  │              │    │              │    │                      │  │
│  │ COSGN00C     │    │ COACTVWC     │    │ COCRDLIC             │  │
│  │ COADM01C     │    │ COACTUPC     │    │ COCRDSLC             │  │
│  │ COUSR00-03C  │    │ CBACT01C     │    │ COCRDUPC             │  │
│  │              │    │ CBACT04C     │    │ CBACT02C             │  │
│  │ Store:       │    │              │    │ CBACT03C             │  │
│  │  USRSEC      │    │ Store:       │    │                      │  │
│  └──────┬───────┘    │  ACCTDATA    │    │ Store:               │  │
│         │            │  DISCGRP     │    │  CARDDATA            │  │
│         │            │  TCATBALF    │    │  CARDXREF            │  │
│         │            └──────┬───────┘    └──────────┬───────────┘  │
│         │                   │                       │              │
│         │            ┌──────┴───────────────────────┴───────┐      │
│         │            │       Transaction Domain             │      │
│         │            │                                      │      │
│         │            │  COTRN00C, COTRN01C, COTRN02C       │      │
│         │            │  CBTRN01C, CBTRN02C, CBTRN03C       │      │
│         │            │                                      │      │
│         │            │  Store: TRANSACT, DALYTRAN           │      │
│         │            │         TRANCATG, TRANTYPE           │      │
│         │            └──────┬───────────────────────────────┘      │
│         │                   │                                      │
│         │            ┌──────┴──────────┐  ┌────────────────────┐   │
│         │            │ Billing &       │  │ Authorization &    │   │
│         │            │ Reporting       │  │ Fraud (Optional)   │   │
│         │            │                 │  │                    │   │
│         │            │ COBIL00C        │  │ COPAUA0C           │   │
│         │            │ CORPT00C        │  │ COPAUS0C/1C/2C     │   │
│         │            │ CBSTM03A        │  │ CBPAUP0C           │   │
│         │            │                 │  │                    │   │
│         │            │ Store:          │  │ Store:             │   │
│         │            │  (uses TRANSACT │  │  AUTHFRDS (DB2)    │   │
│         │            │   & ACCTDATA)   │  │  IMS DB segments   │   │
│         │            └─────────────────┘  │  MQ queues         │   │
│         │                                 └────────────────────┘   │
│         │                                                          │
│  ┌──────┴──────────────────────────────────────────────────────┐   │
│  │                  Shared Kernel / Utilities                   │   │
│  │  COCOM01Y (COMMAREA), COMEN01C (Menu Router)               │   │
│  │  CSUTLDTC (Date utility), COBSWAIT (Wait), COBDATFT (ASM)  │   │
│  │  CBEXPORT / CBIMPORT (Branch Migration)                     │   │
│  └─────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 3. Bounded Context Details

### 3.1 Identity & Access Management (IAM)

**Domain responsibility:** User authentication, session establishment, user CRUD, role-based access (Admin vs. Regular User).

| Component | Type | Purpose |
|-----------|------|---------|
| COSGN00C | CICS Online | Sign-on screen — validates user ID/password against USRSEC |
| COADM01C | CICS Online | Admin menu — routes to admin functions based on user type |
| COUSR00C | CICS Online | List users — paginated browse of USRSEC file |
| COUSR01C | CICS Online | Add user — writes new record to USRSEC |
| COUSR02C | CICS Online | Update user — modifies existing USRSEC record |
| COUSR03C | CICS Online | Delete user — removes record from USRSEC |
| CSUSR01Y | Copybook | User record layout (80 bytes: ID, name, password, type) |

**Data ownership:** USRSEC VSAM KSDS (keyed by SEC-USR-ID).

**Inbound dependencies:** None — IAM is the entry point.

**Outbound dependencies:**
- Populates `CDEMO-USER-ID` and `CDEMO-USER-TYPE` in COMMAREA, consumed by all downstream programs.
- COADM01C routes to admin functions via XCTL based on user type.

**Extraction seam analysis:**
- **Seam type:** *Clean boundary* — USRSEC is exclusively owned by IAM programs.
- **Coupling mechanism:** COMMAREA fields (`CDEMO-USER-ID`, `CDEMO-USER-TYPE`) propagated via CICS RETURN TRANSID.
- **Extraction difficulty:** ★☆☆☆☆ (Low)
- **Strategy:** Replace VSAM reads with JWT token validation. New auth service issues tokens; downstream services validate tokens instead of reading COMMAREA user fields.

---

### 3.2 Account Domain

**Domain responsibility:** Account lifecycle management — viewing account details (balances, limits, dates) and updating account attributes (credit limits, status, address).

| Component | Type | Purpose |
|-----------|------|---------|
| COACTVWC | CICS Online (941 LOC) | Account view — reads ACCTDATA, CUSTDATA, CARDXREF |
| COACTUPC | CICS Online (4,236 LOC) | Account update — validates and rewrites ACCTDATA |
| CBACT01C | Batch (430 LOC) | Reads and prints account file |
| CBACT04C | Batch (652 LOC) | Interest calculation — reads TCATBALF, DISCGRP, XREF, ACCTDATA |

**Data ownership:** ACCTDATA VSAM KSDS (keyed by ACCT-ID, record layout CVACT01Y: 300 bytes).

**Shared data (reads):**
- CUSTDATA (owned by a latent Customer context, currently embedded)
- CARDXREF (shared with Credit Card domain)
- DISCGRP (disclosure group interest rates — shared with Transaction domain)
- TCATBALF (transaction category balances — shared with Transaction domain)

**Inbound dependencies:**
- COMEN01C routes here via COMMAREA (options 1 & 2)
- COBIL00C reads and rewrites ACCTDATA for bill payment
- CBTRN02C reads and updates ACCTDATA during transaction posting

**Outbound dependencies:**
- Reads CUSTDATA for customer name display
- Reads CARDXREF to resolve card-to-account relationships

**Extraction seam analysis:**
- **Seam type:** *Shared data* — ACCTDATA is written by Account Update, Bill Payment, and Transaction Posting.
- **Coupling mechanism:** Direct VSAM KSDS reads/writes from multiple programs across domains.
- **Extraction difficulty:** ★★★☆☆ (Medium)
- **Key challenge:** ACCTDATA is the most heavily shared file. Three bounded contexts write to it: Account (COACTUPC), Billing (COBIL00C rewrites balance), and Transaction Processing (CBTRN02C updates cycle debits/credits).
- **Strategy:** Introduce an Account Service as the sole writer. Bill Payment and Transaction Posting call the Account Service API to update balances instead of direct VSAM writes. During transition, use a Change Data Capture (CDC) pattern to sync VSAM ↔ relational DB.

---

### 3.3 Credit Card Domain

**Domain responsibility:** Card lifecycle — listing cards by account, viewing card details, updating card attributes (status, embossed name, expiration).

| Component | Type | Purpose |
|-----------|------|---------|
| COCRDLIC | CICS Online (1,459 LOC) | Card list — paginated browse by account, routes to detail/update |
| COCRDSLC | CICS Online (887 LOC) | Card detail view — reads CARDDATA and CARDXREF |
| COCRDUPC | CICS Online (1,560 LOC) | Card update — validates and rewrites CARDDATA |
| CBACT02C | Batch (178 LOC) | Reads and prints card data file |
| CBACT03C | Batch (178 LOC) | Reads and prints cross-reference file |

**Data ownership:**
- CARDDATA VSAM KSDS (keyed by CARD-NUM, layout CVACT02Y: 150 bytes)
- CARDXREF VSAM KSDS (keyed by XREF-CARD-NUM, layout CVACT03Y: 50 bytes) — *shared ownership with Account domain*

**Inbound dependencies:**
- COMEN01C routes here via COMMAREA (options 3, 4, 5)
- COCRDLIC uses XCTL to transfer to COCRDSLC or COCRDUPC

**Outbound dependencies:**
- Reads ACCTDATA for account status validation during card operations
- Reads CUSTDATA for customer name resolution

**Extraction seam analysis:**
- **Seam type:** *Navigational coupling* — COCRDLIC uses CICS XCTL to transfer control to COCRDSLC and COCRDUPC with COMMAREA.
- **Coupling mechanism:** XCTL with COMMAREA carrying `CDEMO-CARD-NUM` and `CDEMO-ACCT-ID`.
- **Extraction difficulty:** ★★☆☆☆ (Low-Medium)
- **Key challenge:** CARDXREF is the pivot table linking cards ↔ accounts ↔ customers. Both Account and Card domains need this data.
- **Strategy:** CARDXREF becomes a relationship table in the Account domain's schema, exposed via API. The Card Service queries it to resolve card-to-account mappings. Alternatively, denormalize the account ID into the Card entity.

---

### 3.4 Transaction Domain

**Domain responsibility:** Transaction lifecycle — listing, viewing, adding transactions online; posting daily transactions in batch; maintaining transaction type and category reference data.

| Component | Type | Purpose |
|-----------|------|---------|
| COTRN00C | CICS Online (699 LOC) | Transaction list — paginated browse with STARTBR/READNEXT/READPREV |
| COTRN01C | CICS Online (330 LOC) | Transaction view — single record read by TRAN-ID |
| COTRN02C | CICS Online (783 LOC) | Transaction add — validates card, writes TRANSACT record |
| CBTRN01C | Batch (494 LOC) | Reads daily transaction file |
| CBTRN02C | Batch (731 LOC) | **Post-tran** — core batch job that posts DALYTRAN records to TRANSACT, updates ACCTDATA balances, writes rejects to DALYREJS |
| CBTRN03C | Batch (649 LOC) | Transaction report — reads TRANSACT, XREF; produces formatted report |

**Data ownership:**
- TRANSACT VSAM KSDS+AIX (keyed by TRAN-ID, layout CVTRA05Y: 350 bytes)
- DALYTRAN sequential file (layout CVTRA06Y: 350 bytes)
- TRANCATG VSAM (layout CVTRA04Y: 60 bytes)
- TRANTYPE VSAM (layout CVTRA03Y: 60 bytes)

**Shared data (writes to other domains):**
- CBTRN02C updates ACCTDATA (Account domain) — current cycle debit/credit, balance
- CBTRN02C updates TCATBALF (Account domain) — transaction category balances
- COTRN02C reads CARDXREF (Card domain) to validate card before adding transaction

**Inbound dependencies:**
- COMEN01C routes here (options 6, 7, 8)
- COBIL00C writes payment transactions to TRANSACT

**Outbound dependencies:**
- Reads CARDXREF for card validation
- Updates ACCTDATA balances during posting
- Updates TCATBALF category balances during posting

**Extraction seam analysis:**
- **Seam type:** *Anti-corruption layer needed* — CBTRN02C is the most tightly coupled batch program, reading from DALYTRAN and writing to 4 different VSAM files across 3 domains.
- **Coupling mechanism:** Direct multi-file I/O across domain boundaries.
- **Extraction difficulty:** ★★★★☆ (High)
- **Key challenge:** CBTRN02C (post-tran) is the central batch pipeline. It reads daily transactions, validates against XREF, writes to TRANSACT, updates ACCTDATA, and updates TCATBALF — all in a single program with compensating error handling (writes rejects to DALYREJS).
- **Strategy:** Decompose CBTRN02C into an orchestrated pipeline:
  1. **Ingestion step:** Read DALYTRAN, validate format → publish transaction events
  2. **Validation step:** Validate card/account via Card Service and Account Service APIs
  3. **Posting step:** Write to Transaction store
  4. **Balance update step:** Call Account Service to update balances
  5. **Reject handling:** Route invalid transactions to a dead-letter queue

---

### 3.5 Billing & Reporting Domain

**Domain responsibility:** Bill payment processing, transaction statement generation, transaction reports.

| Component | Type | Purpose |
|-----------|------|---------|
| COBIL00C | CICS Online (572 LOC) | Bill payment — reads ACCTDATA, rewrites balance, writes payment transaction |
| CORPT00C | CICS Online (649 LOC) | Report request — validates date range, submits report job via TDQ (WRITEQ TD) |
| CBSTM03A | Batch | Statement generation — produces formatted transaction statements |
| CBTRN03C | Batch (649 LOC) | Transaction report — reads TRANSACT/XREF, outputs formatted report |
| CSUTLDTC | Utility (157 LOC) | Date validation/conversion — called by COTRN02C and CORPT00C |

**Data ownership:** None — this domain is a *consumer* of Account and Transaction data.

**Inbound dependencies:**
- COMEN01C routes here (options 9 & 10)

**Outbound dependencies:**
- COBIL00C reads/rewrites ACCTDATA (Account domain)
- COBIL00C writes to TRANSACT (Transaction domain)
- CORPT00C reads TRANSACT via CICS browse
- CBTRN03C reads TRANSACT and XREF

**Extraction seam analysis:**
- **Seam type:** *Consumer context* — depends on Account and Transaction domains but owns no data.
- **Coupling mechanism:** Direct VSAM I/O to ACCTDATA and TRANSACT.
- **Extraction difficulty:** ★★★☆☆ (Medium)
- **Key challenge:** COBIL00C performs a cross-domain write pattern: it reads an account, updates the balance, AND writes a transaction record — a distributed transaction across two future microservices.
- **Strategy:** Bill Payment becomes an API that orchestrates calls to the Account Service (debit balance) and Transaction Service (record payment). Use the Saga pattern with compensating transactions if either step fails. Report generation becomes an async job that queries the Transaction Service's read replica.

---

### 3.6 Authorization & Fraud Domain (Optional Module)

**Domain responsibility:** Credit card authorization processing, fraud flagging, authorization purging.

| Component | Type | Purpose |
|-----------|------|---------|
| COPAUA0C | CICS/MQ | Authorization decision — receives MQ request, queries IMS for customer, writes decision to IMS+DB2 |
| COPAUS0C | CICS/IMS/BMS | Pending authorization summary — reads IMS segments, displays on BMS map |
| COPAUS1C | CICS/IMS/BMS | Authorization detail view — reads IMS, allows update, inserts to DB2 |
| COPAUS2C | CICS/IMS/DB2 | Mark authorization as fraud — updates IMS, inserts fraud record to DB2 |
| CBPAUP0C | Batch/IMS | Purge expired authorizations — deletes aged IMS segments |

**Data ownership:**
- AUTHFRDS DB2 table (card_num + auth_ts composite key, 27 columns)
- IMS DB segments: DBPAUTP0 (primary), DBPAUTX0 (index)
- MQ queues: authorization request/response

**Inbound dependencies:**
- COMEN01C routes here (option 11)
- External systems send MQ authorization requests

**Outbound dependencies:**
- Reads customer data from IMS DB (not VSAM CUSTDATA)
- Writes fraud records to DB2

**Extraction seam analysis:**
- **Seam type:** *Naturally isolated* — already behind an MQ interface with its own data stores (IMS + DB2).
- **Coupling mechanism:** MQ message queues (loosely coupled), IMS DB (tightly coupled internally), DB2 (standard SQL).
- **Extraction difficulty:** ★★★★☆ (High — due to IMS complexity, not coupling)
- **Key challenge:** IMS DB hierarchical data model (DBD/PSB definitions) has no direct relational equivalent. The DBPAUTP0 segments must be redesigned as relational tables.
- **Strategy:** Treat as a standalone microservice. Replace IMS DB with PostgreSQL tables. Replace MQ with an event broker (SQS/Kafka). The existing MQ interface provides a natural anti-corruption layer during the transition — both old and new systems can consume from the same topics.

---

### 3.7 Shared Kernel & Cross-Cutting Utilities

| Component | Type | Purpose | Migration Target |
|-----------|------|---------|-----------------|
| COCOM01Y | Copybook | COMMAREA structure — carries user/customer/account/card context | REST DTOs, JWT claims, request context |
| COMEN01C | CICS Online | Main menu router — dispatches to programs via XCTL | API Gateway routing / SPA navigation |
| COMEN02Y | Copybook | Menu option definitions (11 options + program names) | Frontend route configuration |
| COADM02Y | Copybook | Admin menu options (6 options + program names) | Frontend admin route configuration |
| CSUTLDTC | Utility | Date validation/conversion | Java `java.time` utilities |
| COBSWAIT | Utility | Timer wait (calls MVSWAIT assembler) | `Thread.sleep()` / scheduler delay |
| COBDATFT | Assembler | Date format conversion | `DateTimeFormatter` |
| CBEXPORT | Batch | Multi-record export for branch migration | Spring Batch export job |
| CBIMPORT | Batch | Multi-record import for branch migration | Spring Batch import job |
| CVEXPORT | Copybook | Export record layout (500 bytes, REDEFINES for 5 record types) | Java DTOs per record type |
| COTTL01Y | Copybook | Screen title layout | UI header component |
| CSDAT01Y | Copybook | Date display fields | Shared date DTO |
| CSMSG01Y/02Y | Copybooks | Message constants | i18n message bundles |

---

## 4. Data Flow Analysis

### 4.1 VSAM File Access Matrix

| VSAM File | IAM | Account | Card | Transaction | Billing | Auth |
|-----------|-----|---------|------|-------------|---------|------|
| USRSEC | **RW** | — | — | — | — | — |
| ACCTDATA | — | **RW** | R | RW | RW | — |
| CARDDATA | — | — | **RW** | — | — | — |
| CARDXREF | — | R | **R** | R | R | — |
| CUSTDATA | — | R | R | — | — | — |
| TRANSACT | — | — | — | **RW** | RW | — |
| DALYTRAN | — | — | — | **R** | — | — |
| DISCGRP | — | R | — | — | — | — |
| TCATBALF | — | **RW** | — | RW | — | — |
| TRANCATG | — | — | — | **R** | — | — |
| TRANTYPE | — | — | — | **R** | — | — |

**Legend:** **RW** = Read/Write (owner), R = Read-only, RW = Read/Write (cross-domain)

### 4.2 Critical Cross-Domain Write Paths

These are the most challenging seams to decompose:

1. **CBTRN02C (Post-Tran) → ACCTDATA**: Updates account balances after posting daily transactions. This is the highest-volume cross-domain write.
2. **CBTRN02C (Post-Tran) → TCATBALF**: Updates transaction category balances during posting.
3. **COBIL00C (Bill Payment) → ACCTDATA**: Rewrites account balance after payment.
4. **COBIL00C (Bill Payment) → TRANSACT**: Writes payment transaction record.
5. **CBACT04C (Interest Calc) → ACCTDATA**: Updates account balances with interest charges.

---

## 5. Extraction Seam Priority

| Priority | Seam | Type | Difficulty | Dependencies |
|----------|------|------|------------|--------------|
| 1 | IAM ↔ All Contexts | Session/Auth | ★☆☆☆☆ | None — extract first |
| 2 | Card Domain ↔ Account Domain | Data (CARDXREF) | ★★☆☆☆ | IAM extracted |
| 3 | Account Domain (read path) | Data (ACCTDATA) | ★★☆☆☆ | IAM, Card extracted |
| 4 | Transaction Domain (online) | Navigational (XCTL) | ★★★☆☆ | Account read path |
| 5 | Account Domain (write path) | Data (ACCTDATA writes) | ★★★☆☆ | Transaction online |
| 6 | Billing → Account + Transaction | Distributed write | ★★★★☆ | Account + Transaction services |
| 7 | Transaction Posting (batch) | Multi-file orchestration | ★★★★☆ | All core services |
| 8 | Authorization & Fraud | IMS/MQ integration | ★★★★☆ | Independent — can parallelize |

---

## 6. Anti-Corruption Layer (ACL) Design

During the transition period, anti-corruption layers bridge old and new systems:

### ACL-1: VSAM-to-REST Adapter
**Purpose:** Allow legacy CICS programs to continue reading VSAM files while new services write to PostgreSQL.
**Mechanism:** A CDC (Change Data Capture) process mirrors PostgreSQL changes back to VSAM files. Legacy programs are unaware of the new system.

### ACL-2: COMMAREA-to-JWT Translator
**Purpose:** Bridge the COMMAREA-based session model with JWT token-based auth.
**Mechanism:** A CICS exit intercepts COMMAREA and populates user context from JWT claims during the hybrid phase where some transactions use the new auth and some still use USRSEC.

### ACL-3: MQ-to-Event-Broker Bridge
**Purpose:** Allow the Authorization module to transition from IBM MQ to a cloud event broker.
**Mechanism:** An MQ bridge consumer reads from legacy MQ queues and republishes to Kafka/SQS topics. During transition, both old IMS-based consumers and new microservice consumers process events.

### ACL-4: Batch File-to-API Adapter
**Purpose:** Allow batch programs (CBTRN02C) to call new microservice APIs instead of direct VSAM writes during the hybrid phase.
**Mechanism:** Replace VSAM WRITE/REWRITE statements with CALL to a thin COBOL adapter program that makes HTTP calls to the Account Service REST API.
