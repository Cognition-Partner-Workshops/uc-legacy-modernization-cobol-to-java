# Domain Decomposition — CardDemo COBOL Estate

> Bounded context identification, microservice candidate mapping, and extraction seam analysis for the CardDemo credit card management system.

---

## Table of Contents

1. [Methodology](#methodology)
2. [Bounded Context Map](#bounded-context-map)
3. [Bounded Context Details](#bounded-context-details)
   - [BC-1: Account Management](#bc-1-account-management)
   - [BC-2: Card Management](#bc-2-card-management)
   - [BC-3: Transaction Processing](#bc-3-transaction-processing)
   - [BC-4: Transaction Reference Data](#bc-4-transaction-reference-data)
   - [BC-5: Statement & Reporting](#bc-5-statement--reporting)
   - [BC-6: User Security & Authentication](#bc-6-user-security--authentication)
   - [BC-7: Payment Authorization](#bc-7-payment-authorization)
   - [BC-8: Data Exchange (Export/Import)](#bc-8-data-exchange-exportimport)
4. [Shared Kernel Analysis](#shared-kernel-analysis)
5. [Data File Ownership Matrix](#data-file-ownership-matrix)
6. [Extraction Seam Analysis](#extraction-seam-analysis)
7. [Candidate Microservice Architecture](#candidate-microservice-architecture)

---

## Methodology

Bounded contexts were identified by analyzing three coupling dimensions:

1. **Copybook sharing** — Programs that share the same copybooks operate on the same data structures and likely belong to the same bounded context. Shared copybooks that cross context boundaries (COCOM01Y, COMEN02Y, COTTL01Y) indicate a shared kernel.
2. **JCL job grouping** — Programs executed within the same JCL job step chain share a processing pipeline and belong to the same operational context.
3. **VSAM data file access** — Programs that read/write the same VSAM files are data-coupled. Files accessed by a single context are isolated; files accessed by multiple contexts are shared and represent coupling seams.

---

## Bounded Context Map

```
┌──────────────────────────────────────────────────────────────────────┐
│                        SHARED KERNEL                                  │
│   COCOM01Y (COMMAREA), COTTL01Y (Title), CSDAT01Y (Date),           │
│   CSMSG01Y/02Y (Messages), DFHAID, DFHBMSCA                        │
└─────────────┬──────────┬──────────┬──────────┬──────────┬───────────┘
              │          │          │          │          │
    ┌─────────▼──┐  ┌────▼─────┐  ┌▼────────┐ ┌▼───────┐ ┌▼──────────┐
    │ BC-1       │  │ BC-2     │  │ BC-3    │ │ BC-6  │ │ BC-7      │
    │ Account    │  │ Card     │  │ Transac │ │ User  │ │ Payment   │
    │ Management │  │ Managmnt │  │ Process │ │ Secur │ │ Auth      │
    │            │  │          │  │         │ │       │ │ (IMS/DB2/ │
    │ ACCTDAT ◄──┼──┼── reads ─┼──┼─ reads ─┤ │USRSEC │ │  MQ)      │
    │ CXACAIX    │  │ CARDDAT  │  │DALYTRAN │ │       │ │ AUTHFRDS  │
    │            │  │          │  │TRANSACT │ │       │ │ IMS DBs   │
    └─────┬──────┘  └────┬─────┘  └────┬────┘ └───────┘ └───────────┘
          │              │             │
          │         ┌────▼─────┐       │
          │         │ CCXREF   │◄──────┘
          └────────►│(cardxref)│
                    │ SHARED   │
                    └────┬─────┘
                         │
              ┌──────────▼───────────┐
              │ BC-4                  │
              │ Transaction Ref Data │
              │ TRANTYPE, TRANCATG   │
              │ TCATBALF, DISCGRP    │
              └──────────────────────┘

    ┌───────────────┐    ┌───────────────┐
    │ BC-5          │    │ BC-8          │
    │ Statement &   │    │ Data Exchange │
    │ Reporting     │    │ Export/Import │
    │ (reads all)   │    │ (reads/writes │
    │               │    │  all)         │
    └───────────────┘    └───────────────┘
```

---

## Bounded Context Details

### BC-1: Account Management

| Attribute | Detail |
|-----------|--------|
| **Programs** | COACTUPC (4,236 LOC), COACTVWC (941 LOC), CBACT01C (430 LOC), CBACT02C (178 LOC), CBACT03C (178 LOC), CBACT04C (652 LOC) |
| **Total LOC** | 6,615 |
| **Owned Data** | `acctdata.txt` → ACCTDAT (VSAM KSDS, key=ACCT-ID) |
| **Shared Data (reads)** | CCXREF / CXACAIX (card cross-reference), CARDDAT (via COACTVWC), TCATBALF, DISCGRP (via CBACT04C) |
| **Shared Data (writes)** | TRANSACT (CBACT04C writes interest transactions) |
| **Copybooks (owned)** | CVACT01Y, CVACT02Y |
| **Copybooks (shared)** | CVACT03Y (cross-ref, shared with BC-2, BC-3), COCOM01Y, COTTL01Y, CSDAT01Y, CSMSG01Y/02Y |
| **JCL Jobs** | ACCTFILE.jcl (setup), INTCALC.jcl (CBACT04C), READACCT.jcl (diagnostic) |
| **Candidate Microservice** | `account-service` |

**Cohesion Analysis:** Programs are strongly cohesive — all operate on account records (CVACT01Y/02Y). CBACT04C (interest calculation) is the most complex batch program and accesses TCATBALF and DISCGRP, creating a dependency on BC-4 (Transaction Reference Data).

**Key Boundary Issue:** COACTUPC reads CCXREF and CXACAIX to resolve card-to-account mappings. This cross-reference lookup is the primary coupling point between Account Management and Card Management.

---

### BC-2: Card Management

| Attribute | Detail |
|-----------|--------|
| **Programs** | COCRDLIC (1,459 LOC), COCRDSLC (887 LOC), COCRDUPC (1,560 LOC), COBIL00C (572 LOC) |
| **Total LOC** | 4,478 |
| **Owned Data** | `carddata.txt` → CARDDAT (VSAM KSDS, key=CARD-NUM) |
| **Shared Data (reads)** | ACCTDAT (via all programs), CCXREF / CXACAIX (cross-reference) |
| **Shared Data (writes)** | TRANSACT (COBIL00C writes payment transactions), ACCTDAT (COBIL00C updates balance) |
| **Copybooks (owned)** | CVCRD01Y |
| **Copybooks (shared)** | CVACT01Y, CVACT02Y, CVACT03Y, COCOM01Y, COTTL01Y, CSDAT01Y, CSMSG01Y/02Y, CSLKPCDY |
| **JCL Jobs** | CARDFILE.jcl (setup), READCARD.jcl (diagnostic) |
| **Candidate Microservice** | `card-service` |

**Cohesion Analysis:** Moderate cohesion. Card list/view/update programs are strongly related. COBIL00C (bill payment) is an outlier — it modifies ACCTDAT and writes TRANSACT, which are operations that belong to the Account and Transaction contexts. COBIL00C is a candidate for extraction into a separate `payment-service` or reassignment to BC-1.

**Key Boundary Issue:** COBIL00C writes to both TRANSACT and ACCTDAT, creating write coupling across three bounded contexts (Card, Account, Transaction). This is the hardest seam in the card domain.

---

### BC-3: Transaction Processing

| Attribute | Detail |
|-----------|--------|
| **Programs** | CBTRN01C (494 LOC), CBTRN02C (731 LOC), COTRN00C (699 LOC), COTRN01C (330 LOC), COTRN02C (783 LOC) |
| **Total LOC** | 3,037 |
| **Owned Data** | `dailytran.txt` → DALYTRAN (PS flat file), TRANSACT (VSAM KSDS, key=TRAN-CARD+ID) |
| **Shared Data (reads)** | CCXREF / CXACAIX (cross-reference), ACCTDAT, TRANTYPE, TRANCATG |
| **Shared Data (writes)** | ACCTDAT (CBTRN02C updates balances), TCATBALF (CBTRN02C updates category balances) |
| **Copybooks (owned)** | CVTRA05Y (transaction record), CVTRA06Y (daily transaction) |
| **Copybooks (shared)** | CVACT01Y, CVACT03Y, CVTRA01Y, CVTRA03Y, CVTRA04Y, COCOM01Y, COTTL01Y, CSDAT01Y, CSMSG01Y |
| **JCL Jobs** | POSTTRAN.jcl (CBTRN01C → CBTRN02C), TRANFILE.jcl (setup), COMBTRAN.jcl, TRANBKP.jcl, TRANIDX.jcl |
| **Candidate Microservice** | `transaction-service` |

**Cohesion Analysis:** Strong pipeline cohesion — CBTRN01C validates and CBTRN02C posts, forming a single batch unit of work. Online programs (COTRN00-02C) provide browse/view/add for the same TRANSACT file. However, CBTRN02C's write to ACCTDAT and TCATBALF creates tight coupling to BC-1 and BC-4.

**Key Boundary Issue:** CBTRN02C is the most data-coupled program in the batch pipeline — it reads DALYTRAN and CCXREF, then writes to TRANSACT, ACCTDAT, and TCATBALF. In a microservice architecture, this requires either a saga pattern or the transaction posting must remain co-located with the account balance update.

---

### BC-4: Transaction Reference Data

| Attribute | Detail |
|-----------|--------|
| **Programs** | COTRTLIC (2,098 LOC), COTRTUPC (1,702 LOC), COBTUPDT (237 LOC) |
| **Total LOC** | 4,037 |
| **Owned Data** | DB2 tables: `TRANSACTION_TYPE`, `TRANSACTION_TYPE_CATEGORY`; VSAM mirrors: `trantype.txt` → TRANTYPE, `trancatg.txt` → TRANCATG; also `tcatbal.txt` → TCATBALF, `discgrp.txt` → DISCGRP |
| **Shared Data (reads)** | None — this context is a data provider, not a consumer |
| **Shared Data (writes)** | TRANTYPE.PS and TRANCATG.PS (flat file exports consumed by VSAM reload jobs) |
| **Copybooks (owned)** | CSDB2RWY, CSDB2RPY (DB2 working storage/procedures), DCLTRTYP (DB2 declarations) |
| **Copybooks (shared)** | COCOM01Y, COTTL01Y, CSDAT01Y, CSMSG01Y/02Y |
| **JCL Jobs** | CREADB21.jcl (DB2 setup), MNTTRDB2.jcl (batch maintenance), TRANEXTR.jcl (DB2→flat file extract), TRANTYPE.jcl, TRANCATG.jcl, TCATBALF.jcl, DISCGRP.jcl (VSAM setup) |
| **Candidate Microservice** | `reference-data-service` |

**Cohesion Analysis:** Very strong cohesion. All three programs manage the same two DB2 tables. The TRANEXTR.jcl → VSAM reload pipeline is an internal data synchronization mechanism that disappears when everything reads from the same database.

**Key Boundary Issue:** TCATBALF and DISCGRP are reference data used by BC-1 (CBACT04C for interest calculation) and BC-3 (CBTRN02C for category balance updates). In the current system, these are separate VSAM files. In the target architecture, they should be tables owned by the reference data service, with read access granted to account and transaction services via API or shared database schema.

---

### BC-5: Statement & Reporting

| Attribute | Detail |
|-----------|--------|
| **Programs** | CBSTM03A (924 LOC), CBSTM03B (230 LOC), CBTRN03C (649 LOC), CORPT00C (649 LOC) |
| **Total LOC** | 2,452 |
| **Owned Data** | Statement output files (STATEMNT.PS, STATEMNT.HTML), Report output (TRANREPT GDG) |
| **Shared Data (reads)** | TRANSACT, CCXREF, CUSTFILE, ACCTDAT (CBSTM03A reads all four); TRANFILE, CARDXREF, TRANTYPE, TRANCATG, DATEPARM (CBTRN03C reads five files) |
| **Shared Data (writes)** | None — this context is read-only for entity data |
| **Copybooks (owned)** | COSTM01, CUSTREC, CVTRA07Y, CSUTLDPY |
| **Copybooks (shared)** | CVACT01Y, CVACT03Y, CVTRA05Y, COCOM01Y, COTTL01Y, CSDAT01Y, CSMSG01Y |
| **JCL Jobs** | CREASTMT.JCL (CBSTM03A), TRANREPT.jcl (CBTRN03C), TXT2PDF1.JCL, REPTFILE.jcl, DALYREJS.jcl |
| **Candidate Microservice** | `reporting-service` |

**Cohesion Analysis:** Moderate cohesion. Statement generation (CBSTM03A/B) and transaction reporting (CBTRN03C) serve different business purposes but share the same read-only access pattern. CORPT00C is the online trigger that submits batch report jobs. All are consumers of other contexts' data — they have no owned entity data.

**Key Boundary Issue:** This context reads from nearly every VSAM file in the system. In a microservice architecture, reporting services typically use a read replica or data warehouse. This context has **no write coupling** to other contexts, making it easy to extract — it can be replaced with a reporting service that reads from a shared database or event stream.

---

### BC-6: User Security & Authentication

| Attribute | Detail |
|-----------|--------|
| **Programs** | COSGN00C (260 LOC), COUSR00C (695 LOC), COUSR01C (299 LOC), COUSR02C (414 LOC), COUSR03C (359 LOC) |
| **Total LOC** | 2,027 |
| **Owned Data** | USRSEC (VSAM KSDS, key=SEC-USR-ID) — **exclusively owned, no other context reads or writes** |
| **Shared Data** | None |
| **Copybooks (owned)** | CSUSR01Y, CSSETATY |
| **Copybooks (shared)** | COCOM01Y, COTTL01Y, CSDAT01Y, CSMSG01Y |
| **JCL Jobs** | DUSRSECJ.jcl (setup) |
| **Candidate Microservice** | `auth-service` / `identity-service` |

**Cohesion Analysis:** Very strong cohesion. All five programs operate exclusively on the USRSEC file. No other program in the estate reads or writes USRSEC except these five. This is the cleanest bounded context in the system.

**Key Boundary Issue:** COCOM01Y carries the user session state (CDEMO-USR-ID, CDEMO-USR-TYP, CDEMO-USR-SEC-LVL) in the CICS COMMAREA. Every online program reads these fields for authorization decisions. In the target architecture, this becomes JWT claims — but every other service must accept and validate JWT tokens, creating an integration dependency.

---

### BC-7: Payment Authorization

| Attribute | Detail |
|-----------|--------|
| **Programs** | CBPAUP0C (386 LOC), COPAUA0C (1,026 LOC), COPAUS0C (1,032 LOC), COPAUS1C (604 LOC), COPAUS2C (244 LOC), PAUDBLOD (369 LOC), PAUDBUNL (317 LOC), DBUNLDGS (366 LOC) |
| **Total LOC** | 4,344 |
| **Owned Data** | DB2: AUTHFRDS (fraud detection); IMS: DBPAUTP0, DBPAUTX0 (payment authorization databases) |
| **Shared Data (reads)** | ACCTDAT, CARDDAT, CCXREF (via COACCT01 in VSAM-MQ sub-app, which handles inquiry on behalf of auth) |
| **Shared Data (writes)** | None to main application files |
| **Copybooks (owned)** | CCPAURQY, CCPAURLY, CCPAUERY, CIPAUDTY, CIPAUSMY, IMSFUNCS, PAUTBPCB, PADFLPCB, PASFLPCB |
| **Copybooks (shared)** | COCOM01Y, COTTL01Y, CSDAT01Y, CSMSG01Y, CSDB2RWY, CSDB2RPY |
| **JCL Jobs** | CBPAUP0J.jcl, DBPAUTP0.jcl, LOADPADB.JCL, UNLDPADB.JCL, UNLDGSAM.JCL |
| **Candidate Microservice** | `authorization-service` |

**Cohesion Analysis:** Strong cohesion within the sub-application. All programs deal with payment authorization workflows. The IMS database programs (PAUDBLOD, PAUDBUNL, DBUNLDGS) are data management utilities for the authorization-specific IMS databases.

**Key Boundary Issue:** The MQ message interface (CCPAURQY request, CCPAURLY reply) provides a natural API boundary. However, the fraud detection queries in COPAUA0C access DB2 AUTHFRDS, which is independent of the IMS databases — suggesting the fraud detection could be a separate sub-context.

---

### BC-8: Data Exchange (Export/Import)

| Attribute | Detail |
|-----------|--------|
| **Programs** | CBEXPORT (582 LOC), CBIMPORT (487 LOC) |
| **Total LOC** | 1,069 |
| **Owned Data** | EXPORT.DATA.PS (consolidated export flat file) |
| **Shared Data (reads)** | CBEXPORT reads: CUSTFILE, ACCTDAT, CARDDAT, CCXREF, TRANSACT (all five entity files) |
| **Shared Data (writes)** | CBIMPORT writes: CUSTFILE, ACCTDAT, CARDDAT, CCXREF, TRANSACT (all five entity files) |
| **Copybooks (used)** | CVCUS01Y, CVACT01Y, CVCRD01Y, CVACT03Y, CVTRA05Y, CVEXPORT |
| **JCL Jobs** | CBEXPORT.jcl, CBIMPORT.jcl |
| **Candidate Microservice** | `data-exchange-service` (or integrated into an ETL pipeline) |

**Cohesion Analysis:** Strong internal cohesion — export and import are symmetric operations on the same data format (CVEXPORT). However, this context crosses every other entity boundary.

**Key Boundary Issue:** CBIMPORT writes to all five entity VSAM files. In a microservice architecture, import must be decomposed into API calls to each entity service. This context should not exist as a standalone service; instead, import/export becomes an orchestration layer that delegates to entity services.

---

## Shared Kernel Analysis

### Universally Shared Copybooks

These copybooks are used by nearly all online CICS programs and form the **shared kernel** — infrastructure-level structures that cross all bounded contexts.

| Copybook | Used By | Purpose | Migration Strategy |
|----------|---------|---------|-------------------|
| **COCOM01Y** | 20 programs | CICS COMMAREA — session state (user ID, account ID, card number, navigation state) | Replace with JWT claims + request context object. Each microservice validates the JWT and extracts user/session info. |
| **COTTL01Y** | 17 programs | Screen title/header constants | Replace with frontend UI component. Eliminated entirely in SPA architecture. |
| **CSDAT01Y** | 17 programs | Date/time working storage (WS-CURDATE, WS-CURTIME) | Replace with `java.time.LocalDateTime.now()`. Injected via Spring's `@Autowired Clock` for testability. |
| **CSMSG01Y** | 17 programs | Short message area (WS-MSG, WS-RETURN-MSG) | Replace with HTTP response body or error DTO. |
| **CSMSG02Y** | 6 programs | Extended message area (WS-LONG-MSG) | Same as CSMSG01Y. |
| **DFHAID** | 17 programs | CICS AID key byte definitions (ENTER, PF keys) | Eliminated. Modern UI handles keyboard events natively. |
| **DFHBMSCA** | 17 programs | BMS screen attribute definitions | Eliminated. Replaced by CSS/HTML in modern frontend. |
| **COMEN02Y** | 1 program (COMEN01C) | Main menu option arrays | Eliminated. Menu items become frontend route configuration. |

### Cross-Context Entity Copybooks

These copybooks define entity structures shared across bounded context boundaries.

| Copybook | Owned By | Also Used By | Coupling Impact |
|----------|----------|-------------|----------------|
| **CVACT03Y** (Card Cross-Reference) | Shared (BC-1 / BC-2) | BC-1 (COACTUPC, COACTVWC, CBACT04C), BC-2 (COCRDLIC, COCRDUPC), BC-3 (CBTRN01C, CBTRN02C, CBTRN03C, COTRN02C), BC-5 (CBSTM03A), BC-8 (CBEXPORT, CBIMPORT) | **High** — 12 programs across 5 contexts. The cross-reference is the central coupling point of the entire system. |
| **CVACT01Y** (Account Master) | BC-1 | BC-2 (COBIL00C, COCRDSLC, COCRDUPC), BC-3 (CBTRN02C, COTRN02C), BC-5 (CBSTM03A), BC-8 (CBEXPORT, CBIMPORT) | **High** — 12 programs across 5 contexts read account data. |
| **CVTRA05Y** (Transaction Record) | BC-3 | BC-1 (via CBACT04C writes), BC-2 (COBIL00C writes), BC-5 (CBSTM03A reads, CBTRN03C reads), BC-8 (CBEXPORT, CBIMPORT) | **High** — 9 programs across 5 contexts. |
| **CVCRD01Y** (Card Record) | BC-2 | BC-1 (COACTUPC), BC-7 (indirectly via COACCT01), BC-8 (CBEXPORT, CBIMPORT) | **Medium** — 6 programs across 3 contexts. |
| **CVCUS01Y** (Customer Record) | Implicit (no dedicated BC) | BC-5 (CBSTM03A), BC-8 (CBEXPORT, CBIMPORT), CBCUS01C | **Low** — 4 programs. Customer data is mostly read-only. |
| **CSUSR01Y** (User Security) | BC-6 | None | **None** — exclusively owned by BC-6. Cleanest boundary. |

---

## Data File Ownership Matrix

| Data File | VSAM Dataset | Owner BC | Read By | Written By | Isolation |
|-----------|-------------|----------|---------|------------|-----------|
| `acctdata.txt` | ACCTDAT | BC-1 (Account) | BC-1, BC-2, BC-3, BC-5, BC-8 | BC-1 (COACTUPC), BC-2 (COBIL00C), BC-3 (CBTRN02C), BC-8 (CBIMPORT) | **Shared** — 4 writers across 4 contexts |
| `carddata.txt` | CARDDAT | BC-2 (Card) | BC-1, BC-2, BC-5, BC-8 | BC-2 (COCRDUPC), BC-8 (CBIMPORT) | **Shared** — 2 writers |
| `cardxref.txt` | CCXREF / CXACAIX | Shared (BC-1/BC-2) | BC-1, BC-2, BC-3, BC-5, BC-7, BC-8 | BC-8 (CBIMPORT) only | **Shared** — most widely read file (6 contexts) |
| `custdata.txt` | CUSTFILE | Implicit (no BC) | BC-5, BC-8, CBCUS01C | BC-8 (CBIMPORT) only | **Mostly isolated** — single writer |
| `dailytran.txt` | DALYTRAN | BC-3 (Transaction) | BC-3 (CBTRN01C, CBTRN02C) | External feed | **Isolated** — single-context consumption |
| `tcatbal.txt` | TCATBALF | BC-4 (Ref Data) | BC-1 (CBACT04C), BC-3 (CBTRN02C) | BC-3 (CBTRN02C) | **Shared** — read by 2 contexts, written by 1 |
| `trancatg.txt` | TRANCATG | BC-4 (Ref Data) | BC-3 (CBTRN01C, CBTRN03C) | TRANEXTR (DB2 extract) | **Isolated** — read-only by consumers |
| `trantype.txt` | TRANTYPE | BC-4 (Ref Data) | BC-3 (CBTRN01C, CBTRN03C) | TRANEXTR (DB2 extract) | **Isolated** — read-only by consumers |
| `discgrp.txt` | DISCGRP | BC-4 (Ref Data) | BC-1 (CBACT04C) | None (static reference) | **Isolated** — single reader |
| USRSEC | USRSEC | BC-6 (Security) | BC-6 only | BC-6 only | **Fully isolated** |
| TRANSACT | TRANSACT | BC-3 (Transaction) | BC-3, BC-5 | BC-1 (CBACT04C), BC-2 (COBIL00C), BC-3 (COTRN02C, CBTRN01C, CBTRN02C) | **Shared** — 3 writers across 3 contexts |
| DB2: TRANSACTION_TYPE | DB2 | BC-4 (Ref Data) | BC-4 (COTRTLIC) | BC-4 (COTRTUPC, COBTUPDT) | **Fully isolated** |
| DB2: TRANSACTION_TYPE_CATEGORY | DB2 | BC-4 (Ref Data) | BC-4 (COTRTLIC) | BC-4 (COTRTUPC) | **Fully isolated** |
| DB2: AUTHFRDS | DB2 | BC-7 (Auth) | BC-7 (COPAUA0C, COPAUS0C, COPAUS1C) | BC-7 (COPAUA0C) | **Fully isolated** |
| IMS: DBPAUTP0 / DBPAUTX0 | IMS | BC-7 (Auth) | BC-7 (PAUDBUNL, DBUNLDGS) | BC-7 (PAUDBLOD) | **Fully isolated** |

---

## Extraction Seam Analysis

A **seam** is a boundary where a bounded context can be separated from the monolith with defined integration points. Each seam is rated for extraction difficulty based on data coupling, cross-context writes, and runtime dependencies.

### Seam 1: User Security (BC-6) extraction from monolith

| Attribute | Detail |
|-----------|--------|
| **Programs to extract** | COSGN00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C |
| **Data to migrate** | USRSEC → `users` table (PostgreSQL) |
| **Integration points needed** | JWT token issuance (replacing CICS COMMAREA session). All other services must validate JWT tokens. |
| **Rollback plan** | Keep USRSEC VSAM file as fallback; sign-on can dual-write to both. |
| **Extraction Difficulty** | **Easy** |
| **Rationale** | Zero shared data. USRSEC is exclusively owned. No other program reads or writes USRSEC. The only integration is the session state in COCOM01Y, which is replaced by JWT. |

### Seam 2: Transaction Reference Data (BC-4) extraction from monolith

| Attribute | Detail |
|-----------|--------|
| **Programs to extract** | COTRTLIC, COTRTUPC, COBTUPDT |
| **Data to migrate** | DB2 TRANSACTION_TYPE / TRANSACTION_TYPE_CATEGORY → JPA entities (same PostgreSQL or dedicated schema). Eliminate VSAM mirrors (TRANTYPE, TRANCATG). |
| **Integration points needed** | REST API for transaction type/category lookup (consumed by BC-3 batch programs). TCATBALF and DISCGRP may need their own API or database view. |
| **Rollback plan** | Keep DB2 tables and VSAM mirrors in parallel during transition. TRANEXTR.jcl continues to sync. |
| **Extraction Difficulty** | **Easy** |
| **Rationale** | Already uses DB2 SQL. Isolated sub-application with its own source directory. No VSAM write dependencies. Consumers (CBTRN01C, CBTRN03C, CBACT04C) only read TRANTYPE/TRANCATG — they can be redirected to a REST API or shared database view. |

### Seam 3: Payment Authorization (BC-7) extraction from monolith

| Attribute | Detail |
|-----------|--------|
| **Programs to extract** | CBPAUP0C, COPAUA0C, COPAUS0C, COPAUS1C, COPAUS2C, PAUDBLOD, PAUDBUNL, DBUNLDGS |
| **Data to migrate** | IMS databases → PostgreSQL tables; DB2 AUTHFRDS → PostgreSQL table |
| **Integration points needed** | MQ message bridge (maintain CCPAURQY/CCPAURLY message format during transition). Account inquiry API (replacing COACCT01's VSAM lookups). |
| **Rollback plan** | Dual-consumer on MQ queue — both COBOL and Java consumers process messages; compare results. |
| **Extraction Difficulty** | **Medium** |
| **Rationale** | The MQ message contract provides a natural seam. However, IMS database migration requires schema reverse-engineering from DL/I segment definitions. The programs are in a separate sub-application directory, indicating intentional isolation. DB2 and IMS dependencies are confined to this context. |

### Seam 4: Reporting (BC-5) extraction from monolith

| Attribute | Detail |
|-----------|--------|
| **Programs to extract** | CBSTM03A, CBSTM03B, CBTRN03C, CORPT00C |
| **Data to migrate** | No owned entity data. Output files (STATEMNT.PS, TRANREPT) become generated artifacts. |
| **Integration points needed** | Read access to Account, Transaction, Customer, and Cross-Reference data (via API calls or database read replica). Job scheduling (replacing JCL + CICS TD queue triggers). |
| **Rollback plan** | Run old COBOL reporting in parallel with new reporting; compare outputs. |
| **Extraction Difficulty** | **Medium** |
| **Rationale** | No write coupling — this context is purely read-only for entity data. However, it reads from 5+ VSAM files across 4 contexts, requiring either a shared database or API aggregation. CBSTM03A's ALTER/GO TO pattern complicates the rewrite itself (not the extraction). |

### Seam 5: Card Management (BC-2) extraction from monolith

| Attribute | Detail |
|-----------|--------|
| **Programs to extract** | COCRDLIC, COCRDSLC, COCRDUPC, COBIL00C |
| **Data to migrate** | CARDDAT → `cards` table. CCXREF → `card_cross_references` table (shared with BC-1). |
| **Integration points needed** | Account lookup API (ACCTDAT reads), Cross-reference resolution API (CCXREF), Transaction write API (COBIL00C creating payment transactions). |
| **Rollback plan** | Dual-write to VSAM and PostgreSQL during transition. Read from PostgreSQL, fall back to VSAM on error. |
| **Extraction Difficulty** | **Medium** |
| **Rationale** | CARDDAT is primarily owned by BC-2, but COBIL00C's writes to ACCTDAT and TRANSACT create cross-context write coupling. CCXREF is the most widely shared file (6 contexts). Extracting card management requires resolving the cross-reference ownership question first. |

### Seam 6: Transaction Processing (BC-3) extraction from monolith

| Attribute | Detail |
|-----------|--------|
| **Programs to extract** | CBTRN01C, CBTRN02C, COTRN00C, COTRN01C, COTRN02C |
| **Data to migrate** | DALYTRAN → message queue or staging table, TRANSACT → `transactions` table |
| **Integration points needed** | Account balance update API or saga (CBTRN02C writes ACCTDAT), Category balance update API (CBTRN02C writes TCATBALF), Cross-reference lookup API, Transaction type/category lookup API. |
| **Rollback plan** | Parallel-run: process daily transactions through both old and new pipelines; compare ACCTDAT and TCATBALF results before cutover. |
| **Extraction Difficulty** | **Hard** |
| **Rationale** | CBTRN02C writes to ACCTDAT and TCATBALF (cross-context writes). The POSTTRAN.jcl pipeline has strict ordering (SORT → CBTRN01C → CBTRN02C). JCL SORT steps must be replaced with in-process sorting. The daily transaction feed format (DALYTRAN) must be preserved or transformed. This is the most operationally critical pipeline. |

### Seam 7: Account Management (BC-1) extraction from monolith

| Attribute | Detail |
|-----------|--------|
| **Programs to extract** | COACTUPC, COACTVWC, CBACT01-04C |
| **Data to migrate** | ACCTDAT → `accounts` table. CXACAIX (alternate index) → database index on account_id in cross-reference table. |
| **Integration points needed** | Cross-reference resolution API (CCXREF reads), Card lookup API (COACTVWC reads CARDDAT), Transaction write API (CBACT04C writes interest transactions to TRANSACT), Reference data API (CBACT04C reads TCATBALF, DISCGRP). |
| **Rollback plan** | Shadow-write to both VSAM and PostgreSQL; compare state nightly. Revert to VSAM reads on failure. |
| **Extraction Difficulty** | **Hard** |
| **Rationale** | ACCTDAT has 4 writers across 4 bounded contexts. COACTUPC at 4,236 LOC with 188 decision points is the single most complex program. CBACT04C depends on TCATBALF and DISCGRP (BC-4 data) and writes to TRANSACT (BC-3 data). Extracting this context requires resolving write conflicts across BC-1, BC-2, BC-3, and BC-8. |

### Seam 8: Data Exchange (BC-8) extraction from monolith

| Attribute | Detail |
|-----------|--------|
| **Programs to extract** | CBEXPORT, CBIMPORT |
| **Data to migrate** | EXPORT.DATA.PS → JSON/CSV format |
| **Integration points needed** | API calls to every entity service (Account, Card, Customer, Transaction, Cross-Reference) for both read (export) and write (import). |
| **Rollback plan** | Keep COBOL export/import as fallback; new version produces compatible format. |
| **Extraction Difficulty** | **Medium** |
| **Rationale** | CBIMPORT writes to all five entity files, but this can be decomposed into sequential API calls to each entity service. The export/import programs are batch utilities, not real-time services, so eventual consistency is acceptable. |

---

## Extraction Seam Summary

| Seam | Bounded Context | Difficulty | Key Blocker |
|------|----------------|------------|-------------|
| 1 | BC-6: User Security | **Easy** | JWT integration with all other services |
| 2 | BC-4: Transaction Reference Data | **Easy** | Eliminate VSAM mirror pipeline |
| 3 | BC-7: Payment Authorization | **Medium** | IMS schema reverse-engineering |
| 4 | BC-5: Reporting | **Medium** | Multi-context read aggregation |
| 5 | BC-2: Card Management | **Medium** | CCXREF ownership, COBIL00C cross-writes |
| 6 | BC-3: Transaction Processing | **Hard** | CBTRN02C cross-context writes to ACCTDAT + TCATBALF |
| 7 | BC-1: Account Management | **Hard** | 4 writers to ACCTDAT, COACTUPC complexity |
| 8 | BC-8: Data Exchange | **Medium** | Writes to all entity files |

---

## Candidate Microservice Architecture

```
                    ┌─────────────────┐
                    │   API Gateway   │
                    │   (JWT Auth)    │
                    └───────┬─────────┘
                            │
        ┌───────────────────┼───────────────────┐
        │                   │                   │
   ┌────▼────┐        ┌────▼────┐        ┌────▼────┐
   │ account │        │  card   │        │  auth   │
   │ service │        │ service │        │ service │
   │         │        │         │        │ (JWT)   │
   │ /accounts│       │ /cards  │        │ /login  │
   │ /accounts│       │ /cards/ │        │ /users  │
   │ /:id    │        │  :num   │        │         │
   └────┬────┘        └────┬────┘        └─────────┘
        │                  │
        │    ┌─────────────┤
        │    │             │
   ┌────▼────▼───┐   ┌────▼──────────┐
   │ transaction │   │ authorization │
   │ service     │   │ service       │
   │             │   │               │
   │ /transactions│  │ /authorize    │
   │ /daily-post │   │ /auth-history │
   └──────┬──────┘   └───────────────┘
          │
   ┌──────▼──────┐   ┌───────────────┐
   │ reference   │   │  reporting    │
   │ data svc    │   │  service      │
   │             │   │               │
   │ /tran-types │   │ /statements   │
   │ /categories │   │ /reports      │
   └─────────────┘   └───────────────┘
```

### Service Boundaries

| Service | Owns | Database Tables | APIs Exposed |
|---------|------|----------------|-------------|
| `auth-service` | User identity, sessions | `users`, `sessions` | POST /login, GET/POST/PUT/DELETE /users |
| `account-service` | Accounts, interest calc | `accounts`, `account_history` | GET/PUT /accounts/:id, POST /accounts/:id/interest |
| `card-service` | Cards, card-account xref | `cards`, `card_cross_references` | GET/PUT /cards/:num, GET /cards?account=:id |
| `transaction-service` | Transactions, daily posting | `transactions`, `daily_transactions`, `tran_cat_balances` | GET /transactions, POST /transactions, POST /daily-post |
| `reference-data-service` | Type/category ref data | `transaction_types`, `transaction_categories`, `disclosure_groups` | GET /tran-types, GET /categories, PUT /tran-types/:code |
| `authorization-service` | Auth decisions, fraud | `authorizations`, `fraud_rules` | POST /authorize, GET /auth-history |
| `reporting-service` | Report generation | (read replica access) | POST /statements/generate, POST /reports/daily |

### Shared Database vs. API Calls

For the transition period, a **shared database** approach is recommended over strict API-per-read:

- All services write to their own tables via JPA.
- Services that need cross-context reads (e.g., reporting, transaction posting) use read-only database views or a shared schema with read permissions.
- As services mature and traffic patterns are understood, introduce API calls for cross-service reads where latency is acceptable.

This avoids the distributed transaction problem that would arise from strict microservice isolation during the initial migration phases.
