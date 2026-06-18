# CardDemo Domain Decomposition

## Overview

This document identifies bounded contexts in the CardDemo application by analyzing shared copybooks, JCL job groupings, and data file access patterns. Each bounded context maps to a candidate microservice or module, with extraction seams and coupling ratings.

---

## Bounded Context Identification Method

1. **Copybook sharing analysis** — Programs sharing the same copybooks operate on the same data structures and likely belong to the same domain.
2. **JCL job grouping** — Jobs sequenced together in Control-M folders represent a single workflow.
3. **VSAM file access** — Programs that read/write the same files are coupled; isolated files indicate domain boundaries.

---

## Bounded Context Map

### BC-1: Identity & Access Management

**Programs:**
| Program | Type | LOC | Function |
|---|---|---|---|
| `COSGN00C` | CICS | 260 | Sign-on screen |
| `COADM01C` | CICS | 288 | Admin menu dispatcher |
| `COUSR00C` | CICS | 695 | List users |
| `COUSR01C` | CICS | 299 | Add user |
| `COUSR02C` | CICS | 414 | Update user |
| `COUSR03C` | CICS | 359 | Delete user |

**Shared Copybooks:**
- `CSUSR01Y` — User security record (80 bytes: ID, name, password, type)
- `COCOM01Y` — Application commarea (session context)
- `COADM02Y` — Admin menu option table

**Data Stores (isolated):**
- `USRSEC` — User security VSAM KSDS (**exclusive to this context**)

**JCL Jobs:**
- `DUSRSECJ` — Initial load of user security file (IEBGENER utility)

**Candidate Microservice: `identity-service`**

Responsibilities: Authentication, authorization, user CRUD, session management.

**Extraction Seam:** The commarea field `CDEMO-USER-TYPE` (A/U) is the only outbound contract. All other programs read this flag from the commarea but never access USRSEC directly.

| Seam | Direction | Coupling | Extraction Difficulty |
|---|---|---|---|
| Commarea `CDEMO-USER-TYPE` flag | BC-1 → all other BCs | Data only (single field) | **EASY** |
| Commarea `CDEMO-USER-ID` | BC-1 → all other BCs | Data only (single field) | **EASY** |

---

### BC-2: Account & Customer Management

**Programs:**
| Program | Type | LOC | Function |
|---|---|---|---|
| `COACTVWC` | CICS | 941 | Account view |
| `COACTUPC` | CICS | 4,236 | Account update |

**Shared Copybooks:**
- `CVACT01Y` — Account record (300 bytes)
- `CVCUS01Y` — Customer record (500 bytes)
- `CVACT02Y` — Card record (150 bytes) — *shared with BC-3*
- `CVACT03Y` — Card XREF record (50 bytes) — *shared with BC-3, BC-4*
- `CVCRD01Y` — Card working storage variables
- `CSUTLDWY` — Date validation utility
- `CSUTLDPY` — Display utility

**Data Stores:**
- `ACCTDAT` — Account master VSAM KSDS (**primary owner**)
- `CUSTDAT` — Customer master VSAM KSDS (**primary owner**)
- `CARDDAT` / `CARDAIX` — Card data + alternate index (*shared with BC-3*)
- `CXACAIX` — Cross-reference AIX (*shared with BC-3*)

**JCL Jobs:**
- `ACCTFILE` — Refresh account master (IDCAMS REPRO)
- `CUSTFILE` — Refresh customer master (IDCAMS REPRO)
- `DEFCUST` — Define customer VSAM cluster

**Candidate Microservice: `account-service`**

Responsibilities: Account lifecycle (view/update), customer profile management, credit limit enforcement, account status transitions.

**Extraction Seams:**

| Seam | Direction | Coupling | Extraction Difficulty |
|---|---|---|---|
| `ACCTDAT` file (account balance, status, limits) | BC-2 → BC-4 (Transaction Processing) | Both read & write same file | **HARD** |
| `CUSTDAT` file | BC-2 → BC-5 (Reporting) | Read-only by BC-5 | **MEDIUM** |
| `CARDDAT` / `CVACT02Y` | BC-2 ↔ BC-3 (Credit Card) | Both domains read/write | **HARD** |
| `CVACT03Y` XREF | BC-2 ↔ BC-3 ↔ BC-4 | Central junction table | **HARD** |
| Commarea `CDEMO-ACCT-ID` | BC-2 → BC-3, BC-4 | Data only | **EASY** |

---

### BC-3: Credit Card Management

**Programs:**
| Program | Type | LOC | Function |
|---|---|---|---|
| `COCRDLIC` | CICS | 1,459 | Credit card list (pagination) |
| `COCRDSLC` | CICS | 887 | Credit card detail view |
| `COCRDUPC` | CICS | 1,560 | Credit card update |

**Shared Copybooks:**
- `CVACT02Y` — Card record (150 bytes) — *shared with BC-2*
- `CVACT03Y` — Card XREF — *shared with BC-2, BC-4*
- `CVCRD01Y` — Card working storage
- `CVCUS01Y` — Customer record — *shared with BC-2*

**Data Stores:**
- `CARDDAT` — Card master VSAM KSDS (**primary owner**)
- `CARDAIX` — Alternate index by account (**primary owner**)
- `CXACAIX` — XREF alternate index (*shared*)
- `CUSTDAT` — Customer master (*read-only access*)

**JCL Jobs:**
- `CARDFILE` — Refresh card master (IDCAMS REPRO)
- `XREFFILE` — Refresh cross-reference (IDCAMS REPRO)
- `TRANIDX` — Define alternate index on transaction file

**Candidate Microservice: `card-service`**

Responsibilities: Card issuance, card detail view/update, card-account-customer cross-reference lookups, card pagination/browse.

**Extraction Seams:**

| Seam | Direction | Coupling | Extraction Difficulty |
|---|---|---|---|
| `CARDDAT` / `CARDAIX` | BC-3 ↔ BC-2 | Account view reads cards | **HARD** |
| `CVACT03Y` XREF file | BC-3 ↔ BC-2 ↔ BC-4 | Three-way shared data | **HARD** |
| `CUSTDAT` read | BC-3 → BC-2 | Read-only | **MEDIUM** |
| Card number in commarea | BC-3 → BC-4 | Data field only | **EASY** |

---

### BC-4: Transaction Processing

**Programs:**
| Program | Type | LOC | Function |
|---|---|---|---|
| `COTRN00C` | CICS | 699 | Transaction list |
| `COTRN01C` | CICS | 330 | Transaction view |
| `COTRN02C` | CICS | 783 | Transaction add |
| `COBIL00C` | CICS | 572 | Bill payment |
| `CBTRN01C` | Batch | 494 | Daily transaction posting (alternate) |
| `CBTRN02C` | Batch | 731 | Daily transaction posting (primary/POSTTRAN) |
| `CBACT04C` | Batch | 652 | Interest calculation |

**Shared Copybooks:**
- `CVTRA05Y` — Transaction record (350 bytes)
- `CVTRA06Y` — Daily transaction record (350 bytes)
- `CVTRA01Y` — Transaction category balance (50 bytes)
- `CVTRA02Y` — Disclosure group / interest rate (50 bytes)
- `CVTRA03Y` — Transaction type (60 bytes)
- `CVTRA04Y` — Transaction category (60 bytes)
- `CVACT01Y` — Account record — *shared with BC-2*
- `CVACT03Y` — Card XREF — *shared with BC-2, BC-3*

**Data Stores:**
- `TRANSACT` — Transaction master VSAM KSDS (**primary owner**)
- `DALYTRAN` — Daily transaction input (sequential) (**exclusive**)
- `DALYREJS` — Daily rejects output (sequential) (**exclusive**)
- `TCATBALF` — Transaction category balance VSAM KSDS (**primary owner**)
- `DISCGRP` — Disclosure groups VSAM KSDS (**primary owner**)
- `TRANTYPE` — Transaction type lookup VSAM (**primary owner**)
- `TRANCATG` — Transaction category lookup VSAM (**primary owner**)
- `ACCTDAT` — Account master (*read + update balance during posting*)
- `CARDXREF` / `CXACAIX` — Card cross-reference (*read-only for validation*)
- GDG datasets for transaction backup

**JCL Jobs (Control-M DAILY-TransactionBackup folder):**
1. `CLOSEFIL` → 2. `TRANBKP` → 3. `WAITSTEP` → 4. `OPENFIL`

**JCL Jobs (Control-M MONTHLY-InterestCalculation folder):**
1. `CLOSEFIL` → 2. `INTCALC` → 3. `COMBTRAN` → 4. `WAITSTEP` → 5. `OPENFIL`

**Additional JCL:** `POSTTRAN`, `TRANFILE`, `TCATBALF`, `DISCGRP`, `TRANCATG`, `TRANTYPE`, `COMBTRAN`, `DALYREJS`

**Candidate Microservice: `transaction-service`**

Responsibilities: Transaction CRUD, daily posting pipeline, interest calculation, bill payment, category balance tracking, transaction validation against account/card data.

**Extraction Seams:**

| Seam | Direction | Coupling | Extraction Difficulty |
|---|---|---|---|
| `ACCTDAT` balance update during posting | BC-4 → BC-2 | Write to shared file | **HARD** |
| `CARDXREF` validation lookup | BC-4 → BC-3 | Read-only | **MEDIUM** |
| `TRANSACT` file | BC-4 → BC-5 (Reporting) | BC-5 reads only | **MEDIUM** |
| CLOSEFIL/OPENFIL (CICS file control) | BC-4 ↔ CICS region | Infrastructure coupling | **HARD** |
| Bill payment transaction creation | BC-4 internal | Self-contained | **EASY** |
| DALYTRAN sequential input | External → BC-4 | Inbound interface | **EASY** |

---

### BC-5: Reporting & Statement Generation

**Programs:**
| Program | Type | LOC | Function |
|---|---|---|---|
| `CORPT00C` | CICS | 649 | Report request (submits batch via TDQ) |
| `CBTRN03C` | Batch | 649 | Transaction detail report |
| `CBSTM03A` | Batch | 924 | Statement generator (text + HTML) |
| `CBSTM03B` | Batch | 230 | File I/O subroutine for statements |

**Shared Copybooks:**
- `CVTRA05Y` — Transaction record — *shared with BC-4*
- `CVACT03Y` — Card XREF — *shared with BC-2, BC-3, BC-4*
- `CVTRA03Y` — Transaction type — *shared with BC-4*
- `CVTRA04Y` — Transaction category — *shared with BC-4*
- `COSTM01` — Statement format layout
- `CUSTREC` — Customer record for statement headers
- `CVACT01Y` — Account record — *shared with BC-2*

**Data Stores (all read-only):**
- `TRANSACT` — Transaction master (*read-only*)
- `CARDXREF` — Card cross-reference (*read-only*)
- `TRANTYPE` — Transaction type lookup (*read-only*)
- `TRANCATG` — Transaction category lookup (*read-only*)
- `STMTFILE` / `HTMLFILE` — Statement output (**exclusive**)
- `TRANREPT` — Report output (**exclusive**)

**JCL Jobs:** `CREASTMT`, `TRANREPT`, `REPTFILE`

**Candidate Microservice: `reporting-service`**

Responsibilities: Transaction reports, account statements (text/HTML/PDF), report scheduling.

**Extraction Seams:**

| Seam | Direction | Coupling | Extraction Difficulty |
|---|---|---|---|
| `TRANSACT` file read | BC-5 → BC-4 | Read-only | **EASY** |
| `CARDXREF` / reference data reads | BC-5 → BC-3, BC-4 | Read-only | **EASY** |
| Internal reader (TDQ) job submission | BC-5 → JES2 | Mainframe-specific | **HARD** |
| Customer/Account data for headers | BC-5 → BC-2 | Read-only | **EASY** |
| CALL to `CBSTM03B` subroutine | BC-5 internal | Co-located dependency | **EASY** |

---

### BC-6: Data Migration & Branch Operations

**Programs:**
| Program | Type | LOC | Function |
|---|---|---|---|
| `CBEXPORT` | Batch | 582 | Multi-record export for branch migration |
| `CBIMPORT` | Batch | 487 | Multi-record import with validation |

**Shared Copybooks:**
- `CVEXPORT` — Multi-record export layout (500 bytes, REDEFINES with COMP/COMP-3)
- `CVACT01Y`, `CVACT02Y`, `CVACT03Y`, `CVCUS01Y`, `CVTRA05Y` — all entity records

**Data Stores:** All VSAM files (read for export, write for import) + sequential export file

**JCL Jobs:** `CBEXPORT`, `CBIMPORT`

**Candidate Module: `data-migration-toolkit`** (utility, not a permanent service)

**Extraction Seams:**

| Seam | Direction | Coupling | Extraction Difficulty |
|---|---|---|---|
| All VSAM files | BC-6 → all BCs | Reads/writes everything | **HARD** |
| EBCDIC/COMP-3 encoding | BC-6 internal | Platform-specific | **MEDIUM** |

---

### BC-7: Authorization Processing (Optional — IMS/DB2/MQ)

**Programs:**
| Program | Type | LOC | Function |
|---|---|---|---|
| `COPAUA0C` | CICS | 1,026 | Process authorization requests (MQ trigger) |
| `COPAUS0C` | CICS | 1,032 | Pending authorization summary |
| `COPAUS1C` | CICS | 604 | Pending authorization details |
| `COPAUS2C` | CICS | 244 | Authorization helper |
| `CBPAUP0C` | Batch | 386 | Purge expired authorizations |

**Shared Copybooks:** `CCPAURQY`, `CCPAURLY`, `CCPAUERY`, `CIPAUDTY`, `CIPAUSMY`, `IMSFUNCS`, IMS PCBs

**Data Stores:** IMS database (DBPAUTP0), DB2 table (AUTHFRDS), MQ queue, VSAM files

**JCL Jobs:** `CBPAUP0J`, `LOADPADB`, `UNLDPADB`, `DBPAUTP0`

**Candidate Microservice: `authorization-service`**

**Extraction Seams:**

| Seam | Direction | Coupling | Extraction Difficulty |
|---|---|---|---|
| MQ queue integration | External → BC-7 | Async message | **MEDIUM** |
| IMS hierarchical DB | BC-7 internal | Platform-specific | **HARD** |
| DB2 audit log | BC-7 → DB2 | SQL writes | **EASY** |

---

### BC-8: Transaction Type Administration (Optional — DB2)

**Programs:**
| Program | Type | LOC | Function |
|---|---|---|---|
| `COTRTLIC` | CICS | 2,098 | Transaction type list with DB2 cursor |
| `COTRTUPC` | CICS | 1,702 | Transaction type add/edit via DB2 |
| `COBTUPDT` | Batch | 237 | Batch transaction type maintenance |

**Shared Copybooks:** `CSDB2RPY`, `CSDB2RWY`, `DCLTRTYP`, `DCLTRCAT`

**Data Stores:** DB2 tables (TRNTYPE, TRNTYCAT), VSAM files (TRANTYPE, TRANCATG — synced via TRANEXTR job)

**JCL Jobs (Control-M WEEKLY folder):** `MNTTRDB2` → `TRANEXTR` (DB2 extract to VSAM)

**Candidate Microservice: `reference-data-service`**

**Extraction Seams:**

| Seam | Direction | Coupling | Extraction Difficulty |
|---|---|---|---|
| DB2-to-VSAM sync (TRANEXTR) | BC-8 → BC-4 | One-way data push | **MEDIUM** |
| TRANTYPE/TRANCATG VSAM files | BC-8 → BC-4, BC-5 | Read by other BCs | **MEDIUM** |

---

## Extraction Difficulty Summary

### EASY Seams (data-only coupling, can be replaced with API calls)
- User type flag in commarea → JWT claim
- Account/Card IDs in commarea → URL path parameters
- Read-only reference data lookups → API calls or cached data
- Sequential file input/output → message queue or API endpoints
- `CBSTM03A` → `CBSTM03B` CALL → co-deploy or inline

### MEDIUM Seams (require data synchronization strategy)
- Customer data read by multiple BCs → replicate via events or shared read API
- CARDXREF read by BC-4 for validation → API call to card-service
- DB2-to-VSAM sync job → replace with event-driven reference data updates
- MQ integration → replace with Kafka topics
- EBCDIC/COMP-3 encoding → conversion library at boundary

### HARD Seams (tight data coupling, require database decomposition)
- **ACCTDAT** written by both BC-2 (account update) and BC-4 (transaction posting, interest calc) → requires distributed transaction or saga pattern
- **CARDDAT/CARDAIX** shared between BC-2 and BC-3 → co-extract or use shared database initially
- **CVACT03Y XREF** — three-way junction between customers, accounts, and cards — used by BC-2, BC-3, BC-4 → centralize in account-service with API for other BCs
- **CLOSEFIL/OPENFIL** — CICS file control coupling — batch jobs must close VSAM files before processing → eliminated when migrating off VSAM
- **Internal reader (TDQ)** — CORPT00C submits JCL via CICS TDQ → replace with REST call to trigger batch job
- **BC-6 (Export/Import)** touches all files → temporary; goes away post-migration
- **IMS hierarchical database** — requires schema redesign to relational model

---

## Candidate Microservice Architecture

```
                    +-------------------+
                    |  API Gateway      |
                    +-------------------+
                           |
         +---------+-------+-------+---------+
         |         |       |       |         |
   +-----v---+ +--v------+ +---v-----+ +---v--------+
   | identity | | account | | card    | | transaction|
   | service  | | service | | service | | service    |
   +----------+ +---------+ +---------+ +------------+
                     |           |            |
                     +-----+-----+            |
                           |                  |
                    +------v------+    +------v------+
                    | XREF (owned | <--| reporting   |
                    | by account) |    | service     |
                    +-------------+    +-------------+
                                             |
                                    +--------v--------+
                                    | reference-data  |
                                    | service         |
                                    +-----------------+
```

### Service Ownership of Data

| Service | Owns | Reads From |
|---|---|---|
| `identity-service` | Users (ex-USRSEC) | — |
| `account-service` | Accounts (ex-ACCTDAT), Customers (ex-CUSTDAT), XREF (ex-CARDXREF) | — |
| `card-service` | Cards (ex-CARDDAT) | account-service API (XREF, customer) |
| `transaction-service` | Transactions (ex-TRANSACT), Category Balances (ex-TCATBALF), Disclosure Groups (ex-DISCGRP), Daily transactions | account-service API (balance updates), card-service API (card validation) |
| `reporting-service` | Report output files | transaction-service API, account-service API |
| `reference-data-service` | Transaction Types, Transaction Categories | — |
| `authorization-service` | Authorization records | account-service API, card-service API |
| `data-migration-toolkit` | Export files (temporary) | All services (read-only) |
