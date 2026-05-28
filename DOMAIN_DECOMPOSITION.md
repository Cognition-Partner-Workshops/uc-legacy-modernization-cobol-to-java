# CardDemo Domain Decomposition

## 1. Overview

This document identifies bounded contexts within the CardDemo COBOL/CICS application using Domain-Driven Design principles, maps existing programs and data stores to each context, and analyzes extraction seams — the natural boundaries where the monolith can be split with minimal disruption.

---

## 2. Bounded Context Map

```
┌─────────────────────────────────────────────────────────────────────┐
│                        CardDemo Monolith                            │
│                                                                     │
│  ┌──────────────┐   ┌──────────────┐   ┌──────────────────────┐    │
│  │  Identity &   │   │   Account    │   │   Transaction        │    │
│  │  Access Mgmt  │──▶│   Domain     │◀──│   Domain             │    │
│  │              │   │              │   │                      │    │
│  │  COSGN00C    │   │  COACTVWC    │   │  COTRN00C/01C/02C   │    │
│  │  COUSR00-03C │   │  COACTUPC    │   │  CBTRN01C/02C/03C   │    │
│  │  USRSEC VSAM │   │  ACCTDAT     │   │  TRANSACT VSAM      │    │
│  └──────────────┘   │  CUSTDATA    │   │  DALYTRAN           │    │
│                     │  CARDXREF    │   │  TRANTYPE/TRANCATG  │    │
│                     └──────┬───────┘   │  TCATBALF            │    │
│                            │           └──────────┬───────────┘    │
│  ┌──────────────┐          │                      │                │
│  │  Card        │──────────┘                      │                │
│  │  Domain      │                                 │                │
│  │              │   ┌──────────────┐   ┌──────────┴───────────┐    │
│  │  COCRDLIC    │   │  Billing &   │   │  Reporting &         │    │
│  │  COCRDSLC    │   │  Payment     │──▶│  Statements          │    │
│  │  COCRDUPC    │   │              │   │                      │    │
│  │  CARDDAT     │   │  COBIL00C    │   │  CORPT00C            │    │
│  └──────────────┘   │  TRANSACT    │   │  CBSTM03A/B          │    │
│                     │  ACCTDAT     │   │  CBTRN03C            │    │
│                     └──────────────┘   └──────────────────────┘    │
│                                                                     │
│  ┌──────────────┐   ┌──────────────┐   ┌──────────────────────┐    │
│  │  Interest &   │   │Authorization │   │  Data Migration      │    │
│  │  Finance     │   │  & Fraud     │   │                      │    │
│  │              │   │              │   │  CBEXPORT            │    │
│  │  CBACT04C    │   │  COPAUA0C    │   │  CBIMPORT            │    │
│  │  DISCGRP     │   │  COPAUS0-2C  │   │  CVEXPORT layout     │    │
│  │  TCATBALF    │   │  CBPAUP0C    │   └──────────────────────┘    │
│  └──────────────┘   │  IMS/DB2/MQ  │                               │
│                     └──────────────┘                               │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 3. Bounded Context Definitions

### 3.1 Identity & Access Management (IAM)

**Responsibility:** User authentication, authorization, session management, and user lifecycle (CRUD).

| Artifact | Type | Description |
|---|---|---|
| COSGN00C.cbl | Online (CICS) | Sign-on screen, credential validation |
| COUSR00C.cbl | Online (CICS) | List users |
| COUSR01C.cbl | Online (CICS) | Add user |
| COUSR02C.cbl | Online (CICS) | Update user |
| COUSR03C.cbl | Online (CICS) | Delete user |
| COMEN01C.cbl | Online (CICS) | Main menu (regular user routing) |
| COADM01C.cbl | Online (CICS) | Admin menu routing |
| CSUSR01Y.cpy | Copybook | User security record (80 bytes) |
| COCOM01Y.cpy | Copybook | COMMAREA — session state carrier |
| USRSEC | VSAM KSDS | User credentials and type flags |

**Ubiquitous Language:**
- *User* — An authenticated operator of the system
- *User Type* — Admin (A) or Regular (U), controlling menu access
- *Session* — COMMAREA-propagated state across CICS transactions
- *Sign-on* — Authentication entry point (transaction CC00)

**Extraction Seam Analysis:**
- **Seam type:** Clean boundary — data coupling only
- **Inbound dependencies:** Every other context reads `CDEMO-USER-ID` and `CDEMO-USER-TYPE` from COMMAREA
- **Outbound dependencies:** None — IAM is a leaf context
- **Shared data:** COMMAREA fields `CDEMO-USER-ID`, `CDEMO-USER-TYPE` used across all programs
- **Extraction approach:** Replace COMMAREA session propagation with JWT tokens. New IAM service issues tokens containing user ID and role. All other contexts consume tokens via HTTP header or API gateway.
- **Difficulty:** Low ⬤⬤○○○

---

### 3.2 Account Domain

**Responsibility:** Account lifecycle — creation, viewing, modification of credit card account master data. Customer data ownership.

| Artifact | Type | Description |
|---|---|---|
| COACTVWC.cbl | Online (CICS) | Account view |
| COACTUPC.cbl | Online (CICS) | Account update (4,236 lines — largest program) |
| CBACT01C.cbl | Batch | Account data file load |
| CBACT02C.cbl | Batch | Card data file load |
| CBACT03C.cbl | Batch | Cross-reference file load |
| CBCUS01C.cbl | Batch | Customer data file load |
| CVACT01Y.cpy | Copybook | Account record (300 bytes) |
| CVACT02Y.cpy | Copybook | Card record (150 bytes) — shared with Card context |
| CVACT03Y.cpy | Copybook | Card cross-reference (50 bytes) |
| CVCUS01Y.cpy | Copybook | Customer record (500 bytes) |
| ACCTDAT | VSAM KSDS | Account master file |
| CUSTDATA | VSAM KSDS | Customer master file |
| CARDXREF | VSAM KSDS | Customer↔Account↔Card cross-reference |

**Ubiquitous Language:**
- *Account* — A credit card account with balance, limits, and status
- *Customer* — The person holding the account (KYC data, SSN, address)
- *Cross-Reference (XREF)* — Mapping table linking Card Number → Customer ID → Account ID
- *Account Group* — Grouping for interest rate disclosure purposes
- *Credit Limit* — Maximum balance, with separate cash credit limit

**Extraction Seam Analysis:**
- **Seam type:** Core domain — high coupling, high value
- **Inbound dependencies:** Transaction Processing reads/writes ACCTDAT (balance updates). Interest Calculation reads/writes ACCTDAT. Bill Payment reads/writes ACCTDAT. Reporting reads ACCTDAT+CUSTDATA. Card context reads CARDXREF.
- **Outbound dependencies:** None — Account is a data provider
- **Shared data:** ACCTDAT is the most widely shared VSAM file (6+ consumers)
- **Coupling hotspot:** `ACCT-CURR-BAL` field is written by Transaction Processing, Interest Calculation, and Bill Payment
- **Extraction approach:** Introduce an Account Service with a well-defined API. During transition, use Change Data Capture (CDC) from VSAM to sync to a new PostgreSQL database. Protect balance updates with optimistic locking. This is the most complex extraction due to shared write access.
- **Difficulty:** High ⬤⬤⬤⬤⬤

---

### 3.3 Card Domain

**Responsibility:** Credit card lifecycle — issuance tracking, card status, CVV, embossed name, expiration.

| Artifact | Type | Description |
|---|---|---|
| COCRDLIC.cbl | Online (CICS) | Card list with pagination |
| COCRDSLC.cbl | Online (CICS) | Card detail view |
| COCRDUPC.cbl | Online (CICS) | Card update |
| CVACT02Y.cpy | Copybook | Card record (150 bytes) |
| CVCRD01Y.cpy | Copybook | Card work areas and AID handling |
| CARDDAT | VSAM KSDS | Card master file |

**Ubiquitous Language:**
- *Card* — A physical credit card linked to an account
- *Card Number* — Primary key, 16-digit PAN
- *Embossed Name* — Cardholder name on the physical card
- *Active Status* — Card operational status flag

**Extraction Seam Analysis:**
- **Seam type:** Clean boundary — reads from Account via XREF lookup
- **Inbound dependencies:** Transaction Processing validates card number against XREF. Authorization module validates card.
- **Outbound dependencies:** CARDXREF (owned by Account domain) for account resolution
- **Shared data:** CVACT02Y copybook used in Card programs and export
- **Extraction approach:** Extract Card Service that owns CARDDAT. Calls Account Service for XREF resolution. Card number validation becomes an API call.
- **Difficulty:** Medium ⬤⬤⬤○○

---

### 3.4 Transaction Domain

**Responsibility:** Transaction lifecycle — creation, posting, validation, categorization, and daily batch processing.

| Artifact | Type | Description |
|---|---|---|
| COTRN00C.cbl | Online (CICS) | Transaction list |
| COTRN01C.cbl | Online (CICS) | Transaction view |
| COTRN02C.cbl | Online (CICS) | Transaction add |
| CBTRN01C.cbl | Batch | Transaction file operations |
| CBTRN02C.cbl | Batch | Daily transaction posting (core batch) |
| CBTRN03C.cbl | Batch | Transaction report generation |
| CVTRA05Y.cpy | Copybook | Transaction record (350 bytes) |
| CVTRA06Y.cpy | Copybook | Daily transaction record (350 bytes) |
| CVTRA03Y.cpy | Copybook | Transaction type (60 bytes) |
| CVTRA04Y.cpy | Copybook | Transaction category (60 bytes) |
| CVTRA01Y.cpy | Copybook | Transaction category balance (50 bytes) |
| CVTRA07Y.cpy | Copybook | Report data structures |
| TRANSACT | VSAM KSDS | Transaction master file |
| DALYTRAN | Sequential | Daily transaction input file |
| TRANTYPE | VSAM KSDS | Transaction type reference |
| TRANCATG | VSAM KSDS | Transaction category reference |
| TCATBALF | VSAM KSDS | Category balance aggregates |
| DALYREJS | Sequential | Rejected transaction output |

**Ubiquitous Language:**
- *Transaction* — A financial event (purchase, payment, refund) against an account
- *Daily Transaction (DALYTRAN)* — Incoming transactions awaiting posting
- *Transaction Posting* — Batch process validating and committing daily transactions
- *Transaction Type* — High-level classification (e.g., purchase, cash advance)
- *Transaction Category* — Sub-classification within a type
- *Category Balance (TCATBALF)* — Running aggregate balance per account/type/category
- *Rejection* — A daily transaction that fails validation (written to DALYREJS)

**Extraction Seam Analysis:**
- **Seam type:** Core domain — highest business complexity, batch+online split
- **Inbound dependencies:** Bill Payment writes to TRANSACT. Interest Calculation reads TCATBALF and writes interest transactions.
- **Outbound dependencies:** CBTRN02C reads XREFFILE (Account domain) to validate card→account mapping. Updates ACCTDAT balances (Account domain). Updates TCATBALF.
- **Shared data:** TRANSACT is read by Reporting/Statements. TCATBALF is shared with Interest Calculation.
- **Coupling hotspot:** CBTRN02C's batch posting is the central integration point — it reads DALYTRAN, validates via XREF, writes TRANSACT, updates ACCTDAT, updates TCATBALF, writes DALYREJS.
- **Extraction approach:** Split into Online Transaction Service (CRUD) and Batch Transaction Processor (Spring Batch). The batch processor calls Account Service and Card Service APIs for validation rather than direct VSAM reads. Use event sourcing for transaction lifecycle tracking.
- **Difficulty:** High ⬤⬤⬤⬤⬤

---

### 3.5 Billing & Payment Domain

**Responsibility:** Bill payment processing — pay account balance, create payment transaction records.

| Artifact | Type | Description |
|---|---|---|
| COBIL00C.cbl | Online (CICS) | Bill payment screen and processing |
| COBIL00 | BMS Map | Bill payment screen layout |
| TRANSACT | VSAM KSDS | Transaction file (writes payment records) |
| ACCTDAT | VSAM KSDS | Account file (reads/updates balance) |
| CXACAIX | VSAM AIX | Alternate index for account lookups |

**Ubiquitous Language:**
- *Bill Payment* — A payment transaction that reduces account balance
- *Confirmation* — Two-step payment flow (enter → confirm → execute)
- *Payment Amount* — Full balance payment (balance zeroed)

**Extraction Seam Analysis:**
- **Seam type:** Clean boundary — well-defined input/output
- **Inbound dependencies:** None — payment is initiated by user action
- **Outbound dependencies:** Writes to TRANSACT (Transaction domain), reads/writes ACCTDAT (Account domain), uses CXACAIX alternate index
- **Shared data:** Shares both TRANSACT and ACCTDAT with other domains
- **Extraction approach:** Extract as Payment Service. Calls Account Service to retrieve balance, calls Transaction Service to create payment record, calls Account Service to update balance. Implements saga pattern for atomic multi-service operation.
- **Difficulty:** Medium ⬤⬤⬤○○

---

### 3.6 Reporting & Statements Domain

**Responsibility:** Generate transaction reports and account statements in text and HTML format.

| Artifact | Type | Description |
|---|---|---|
| CORPT00C.cbl | Online (CICS) | Report submission (submits JCL via TDQ) |
| CBSTM03A.CBL | Batch | Statement generation (text + HTML) |
| CBSTM03B.CBL | Batch | Statement subroutine (file I/O helper) |
| CBTRN03C.cbl | Batch | Transaction report generation |
| COSTM01.CPY | Copybook | Statement working storage |
| CVTRA07Y.cpy | Copybook | Report format structures |
| STMTFILE | Sequential | Statement output (text) |
| HTMLFILE | Sequential | Statement output (HTML) |

**Ubiquitous Language:**
- *Statement* — Periodic account summary showing transactions and balances
- *Transaction Report* — Date-ranged report of transactions with totals
- *Report Submission* — Online trigger that submits batch JCL via extra-partition TDQ
- *Internal Reader* — Mainframe mechanism for dynamic JCL submission

**Extraction Seam Analysis:**
- **Seam type:** Read-only consumer — no write coupling
- **Inbound dependencies:** None (triggered by user request or scheduler)
- **Outbound dependencies:** Reads TRANSACT, ACCTDAT, CUSTDATA, CARDXREF — all from Account and Transaction domains
- **Shared data:** Read-only access to other domain data files
- **Extraction approach:** Clean extraction. Build Reporting Service that queries Account and Transaction APIs/databases. Replace TDQ/internal reader pattern with async job submission (message queue → batch worker). Replace EBCDIC output with PDF/HTML generation.
- **Difficulty:** Low ⬤⬤○○○

---

### 3.7 Interest & Finance Domain

**Responsibility:** Monthly interest calculation based on disclosure group rates applied to transaction category balances.

| Artifact | Type | Description |
|---|---|---|
| CBACT04C.cbl | Batch | Interest calculation engine |
| CVTRA02Y.cpy | Copybook | Disclosure group (interest rates, 50 bytes) |
| CVTRA01Y.cpy | Copybook | Transaction category balance (50 bytes) |
| DISCGRP | VSAM KSDS | Disclosure groups (rate table) |
| TCATBALF | VSAM KSDS | Category balances |
| XREFFILE | VSAM KSDS | Cross-reference (card→account) |
| ACCTDAT | VSAM KSDS | Account master (balance update) |

**Ubiquitous Language:**
- *Disclosure Group* — Interest rate configuration keyed by Account Group + Transaction Type + Category
- *Interest Rate* — Annual percentage rate (S9(04)V99 — packed decimal)
- *Category Balance* — Running balance per account/type/category used as interest calculation base
- *Interest Posting* — Monthly batch that computes interest and updates account balance

**Extraction Seam Analysis:**
- **Seam type:** Batch process with write coupling to Account domain
- **Inbound dependencies:** Depends on Transaction Posting completing first (TCATBALF must be current)
- **Outbound dependencies:** Reads DISCGRP (own data), reads TCATBALF (Transaction domain), reads XREFFILE and updates ACCTDAT (Account domain)
- **Shared data:** TCATBALF is shared with Transaction domain. ACCTDAT balance updates.
- **Coupling hotspot:** Writes to `ACCT-CURR-BAL` — same field updated by Transaction Posting and Bill Payment
- **Extraction approach:** Extract as a Finance Service. Owns DISCGRP rate data. Calls Transaction Service for category balances, calls Account Service for balance updates. Must run after daily posting cycle completes — orchestrate via scheduler dependency.
- **Difficulty:** High ⬤⬤⬤⬤○

---

### 3.8 Authorization & Fraud Domain (Optional Module)

**Responsibility:** Real-time credit card authorization processing, fraud detection, and authorization lifecycle management.

| Artifact | Type | Description |
|---|---|---|
| COPAUA0C.cbl | Online (CICS) | MQ-triggered authorization processor |
| COPAUS0C.cbl | Online (CICS) | Authorization summary view |
| COPAUS1C.cbl | Online (CICS) | Authorization detail view + fraud flagging |
| COPAUS2C.cbl | Online (CICS) | Authorization status update |
| CBPAUP0C.cbl | Batch | Expired authorization purge |
| PAUDBLOD.CBL | Batch | IMS database load utility |
| PAUDBUNL.CBL | Batch | IMS database unload utility |
| DBPAUTP0 | IMS HIDAM | Authorization hierarchical database |
| AUTHFRDS | DB2 Table | Fraud detection records |
| MQ Queues | IBM MQ | Authorization request/response channels |

**Ubiquitous Language:**
- *Authorization* — A hold on available credit pending transaction settlement
- *Authorization Request* — MQ message from POS/merchant system
- *Fraud Flag* — Manual marking of suspicious authorization in DB2
- *Purge* — Batch removal of expired/unmatched authorizations
- *Two-Phase Commit* — Coordinated update across IMS and DB2

**Extraction Seam Analysis:**
- **Seam type:** Isolated module — loosely coupled via MQ and COMMAREA
- **Inbound dependencies:** MQ messages from external POS systems. CICS menu links.
- **Outbound dependencies:** Reads CARDXREF and ACCTDAT (Account domain) for authorization validation
- **Shared data:** Accesses Account domain files for card/account lookup
- **Technology dependencies:** IMS DB (hierarchical), DB2 (relational), MQ (messaging) — three separate middleware technologies
- **Extraction approach:** This is the most technology-complex extraction. Rewrite as an event-driven Authorization Service. Replace IMS with relational tables (PostgreSQL). Replace MQ with Kafka/SQS while maintaining message format compatibility during transition. Fraud detection becomes a separate analytics pipeline.
- **Difficulty:** High ⬤⬤⬤⬤⬤

---

### 3.9 Reference Data Domain (Optional Module — DB2)

**Responsibility:** Master data management for transaction types and categories.

| Artifact | Type | Description |
|---|---|---|
| COTRTLIC.cbl | Online (CICS) | Transaction type list/update/delete |
| COTRTUPC.cbl | Online (CICS) | Transaction type add/edit |
| COBTUPDT.cbl | Batch | Batch transaction type maintenance |
| TRANEXTR.jcl | JCL | DB2 → VSAM extract job |
| TRNTYPE (DB2) | DB2 Table | Transaction type reference table |
| TRNTYCAT (DB2) | DB2 Table | Transaction type/category join table |

**Ubiquitous Language:**
- *Transaction Type* — Reference code (2-char) with description
- *Transaction Category* — Sub-type classification linked to a type
- *Extract* — Batch process copying DB2 reference data to VSAM for online consumption

**Extraction Seam Analysis:**
- **Seam type:** Clean boundary — reference data provider
- **Inbound dependencies:** Transaction domain reads TRANTYPE/TRANCATG VSAM files
- **Outbound dependencies:** None — self-contained data management
- **Shared data:** Produces VSAM extracts consumed by Transaction domain
- **Extraction approach:** Direct migration. DB2 schema maps to PostgreSQL. Eliminate the DB2→VSAM extract pattern by having consumers read from the new database directly via API. Simplest bounded context to extract.
- **Difficulty:** Low ⬤○○○○

---

### 3.10 Data Migration Domain

**Responsibility:** Branch data migration — export and import of consolidated customer, account, card, and transaction data.

| Artifact | Type | Description |
|---|---|---|
| CBEXPORT.cbl | Batch | Multi-record type export |
| CBIMPORT.cbl | Batch | Multi-record type import |
| CVEXPORT.cpy | Copybook | Export record layout (500 bytes, REDEFINES/OCCURS/COMP-3) |

**Ubiquitous Language:**
- *Branch Migration* — Bulk data transfer between CardDemo instances
- *Export Record* — 500-byte multi-type record (Customer/Account/Transaction/Card/XREF) with record type discriminator
- *Sequence Number* — COMP-packed ordering field for indexed access

**Extraction Seam Analysis:**
- **Seam type:** Cross-cutting utility — reads all major data stores
- **Inbound dependencies:** None (triggered on demand)
- **Outbound dependencies:** Reads ALL domain data files (CUSTDATA, ACCTDAT, CARDXREF, TRANSACT, CARDDAT)
- **Shared data:** Read-only consumer of all domains
- **Extraction approach:** After individual domains are extracted, this becomes a data integration job. Reimplment using modern ETL tools or domain API aggregation. The complex REDEFINES/COMP-3 layout makes this a good candidate for automated COBOL-to-Java conversion testing.
- **Difficulty:** Medium ⬤⬤⬤○○

---

## 4. Cross-Cutting Concerns

### 4.1 COMMAREA (Session State)
The `CARDDEMO-COMMAREA` (COCOM01Y.cpy) is the primary coupling mechanism across all online programs. It carries:
- User identity (ID, type)
- Navigation state (from/to program, transaction ID)
- Customer/account/card context (IDs passed between screens)
- UI state (last map/mapset)

**Modernization:** Replace with stateless JWT tokens (identity) + URL/route parameters (navigation) + server-side session cache (context data).

### 4.2 VSAM File Sharing
Six VSAM files are shared across multiple bounded contexts:

| VSAM File | Owner Context | Reader Contexts |
|---|---|---|
| ACCTDAT | Account | Transaction, Billing, Interest, Reporting, Export |
| CARDDAT | Card | Export |
| CARDXREF | Account | Card, Transaction, Interest, Authorization, Export |
| TRANSACT | Transaction | Billing, Reporting, Export |
| TCATBALF | Transaction | Interest |
| USRSEC | IAM | (none) |

**Modernization:** Each owning context exposes APIs. Shared VSAM files become database tables owned by a single service. During transition, use CDC (Change Data Capture) to maintain read replicas for consumers.

### 4.3 Control-M Job Dependencies
The scheduler defines execution ordering that creates implicit coupling:

```
DAILY:     CLOSEFIL → TRANBKP → WAITSTEP → OPENFIL
WEEKLY:    MNTTRDB2 → [CLOSEFIL → DISCGRP → WAITSTEP → OPENFIL]
                     → [TRANEXTR]
MONTHLY:   CLOSEFIL → INTCALC → COMBTRAN → WAITSTEP → OPENFIL
```

**Modernization:** Replace with event-driven orchestration. Each batch service publishes completion events. Downstream jobs subscribe and trigger automatically. Eliminates temporal coupling from CLOSEFIL/OPENFIL bookend pattern.

### 4.4 BMS Screen Maps (3270 UI)
17 BMS maps define the terminal UI. Each map is tightly bound to one COBOL program.

**Modernization:** Replace entirely with a modern SPA (React/Angular). No map-by-map migration — build a new UI that calls modernized service APIs. The map → program 1:1 mapping suggests a natural page-per-feature structure in the new UI.

---

## 5. Dependency Matrix

This matrix shows data coupling between bounded contexts. R = reads, W = writes, RW = reads and writes.

| Data Store | IAM | Account | Card | Transaction | Billing | Reporting | Interest | Auth | RefData | Migration |
|---|---|---|---|---|---|---|---|---|---|---|
| USRSEC | RW | | | | | | | | | |
| ACCTDAT | | RW | | W | RW | R | RW | R | | R |
| CUSTDATA | | RW | | | | R | | | | R |
| CARDDAT | | | RW | | | | | | | R |
| CARDXREF | | RW | R | R | | R | R | R | | R |
| TRANSACT | | | | RW | W | R | | | | R |
| DALYTRAN | | | | RW | | | | | | |
| TCATBALF | | | | RW | | | RW | | | |
| DISCGRP | | | | | | | R | | | |
| TRANTYPE | | | | R | | | | | RW | |
| TRANCATG | | | | R | | | | | RW | |
| IMS HIDAM | | | | | | | | RW | | |
| DB2 AUTHFRDS | | | | | | | | RW | | |
| DB2 TRNTYPE | | | | | | | | | RW | |

---

## 6. Extraction Priority and Sequencing

Based on coupling analysis, extract in this order:

| Priority | Context | Rationale |
|---|---|---|
| 1 | IAM | Zero inbound write coupling. Leaf node. Enables auth for all new services. |
| 2 | Reference Data | Zero inbound write coupling. Small, SQL-based. Quick win. |
| 3 | Card | Single outbound dependency (XREF). Clean boundary. |
| 4 | Reporting | Read-only consumer. No write coupling to any shared store. |
| 5 | Billing & Payment | Well-defined I/O boundary. Depends on Account + Transaction APIs. |
| 6 | Account | Core domain. Must be extracted before Transaction batch can be fully decoupled. |
| 7 | Transaction | Highest complexity. Batch posting is the linchpin. Extract after Account. |
| 8 | Interest & Finance | Depends on Transaction (TCATBALF) and Account (balance updates). |
| 9 | Authorization & Fraud | Multi-middleware (IMS/DB2/MQ). Isolated but complex. Rewrite last. |
| 10 | Data Migration | Cross-cutting. Becomes trivial once individual domains are extracted. |

---

## 7. Anti-Corruption Layer Design

During the transition period, an Anti-Corruption Layer (ACL) translates between legacy and modern contexts:

```
┌──────────────┐     ┌─────────────────┐     ┌──────────────┐
│  Modern UI   │────▶│  API Gateway    │────▶│  New Service  │
│  (React SPA) │     │  + ACL          │     │  (Java/Spring)│
└──────────────┘     │                 │     └──────┬───────┘
                     │  COMMAREA ↔ JWT │            │
                     │  EBCDIC ↔ UTF-8 │            │ CDC sync
                     │  VSAM ↔ SQL     │            │
                     └────────┬────────┘     ┌──────┴───────┐
                              │              │  VSAM Files   │
                              ▼              │  (Legacy)     │
                     ┌──────────────┐        └──────────────┘
                     │  CICS Region  │
                     │  (Legacy)     │
                     └──────────────┘
```

**Key translations:**
1. **COMMAREA ↔ JWT**: Session state mapped to token claims
2. **EBCDIC ↔ UTF-8**: Character encoding conversion at data boundary
3. **Packed Decimal ↔ BigDecimal**: Numeric format conversion with explicit rounding
4. **VSAM Key ↔ Primary Key**: Index mapping between VSAM KSDS keys and database PKs
5. **BMS Map ↔ REST JSON**: Screen data structure to/from API response mapping
