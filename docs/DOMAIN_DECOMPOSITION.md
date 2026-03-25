# CardDemo Domain Decomposition

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo
> **Purpose:** Identify bounded contexts, define service boundaries, and analyze extraction seams for microservice decomposition.

---

## Table of Contents

1. [Bounded Context Map](#1-bounded-context-map)
2. [Bounded Context Definitions](#2-bounded-context-definitions)
3. [Extraction Seam Analysis](#3-extraction-seam-analysis)
4. [Data Ownership Matrix](#4-data-ownership-matrix)
5. [Anti-Corruption Layer Design](#5-anti-corruption-layer-design)
6. [Recommended Extraction Order](#6-recommended-extraction-order)

---

## 1. Bounded Context Map

```
┌─────────────────────────────────────────────────────────────────────┐
│                        CardDemo Application                         │
│                                                                     │
│  ┌───────────────┐    ┌───────────────┐    ┌───────────────┐       │
│  │   IDENTITY    │    │   ACCOUNT     │    │    CARD       │       │
│  │   CONTEXT     │───▶│   CONTEXT     │◀──▶│   CONTEXT     │       │
│  │               │    │               │    │               │       │
│  │ COSGN00C      │    │ COACTVWC      │    │ COCRDLIC      │       │
│  │ COUSR00C-03C  │    │ COACTUPC      │    │ COCRDSLC      │       │
│  │ CSUSR01Y      │    │ CVACT01Y      │    │ COCRDUPC      │       │
│  │               │    │ CVCUS01Y      │    │ CVACT02Y      │       │
│  └───────────────┘    └───────┬───────┘    │ CVACT03Y      │       │
│                               │            │ CVCRD01Y      │       │
│                               │            └───────────────┘       │
│                               │                                     │
│  ┌───────────────┐    ┌───────▼───────┐    ┌───────────────┐       │
│  │   PAYMENT     │    │ TRANSACTION   │    │  REPORTING    │       │
│  │   CONTEXT     │───▶│   CONTEXT     │───▶│   CONTEXT     │       │
│  │               │    │               │    │               │       │
│  │ COBIL00C      │    │ COTRN00C-02C  │    │ CORPT00C      │       │
│  │               │    │ CBTRN01C-02C  │    │ CBTRN03C      │       │
│  │               │    │ CBACT04C      │    │ CBSTM03A/B    │       │
│  │               │    │ CVTRA05Y-06Y  │    │ CVTRA07Y      │       │
│  │               │    │ CVTRA01Y-04Y  │    │ COSTM01       │       │
│  └───────────────┘    └───────────────┘    └───────────────┘       │
│                                                                     │
│  ┌───────────────┐    ┌───────────────┐                             │
│  │  NAVIGATION   │    │ DATA MIGRATION│                             │
│  │  CONTEXT      │    │   CONTEXT     │                             │
│  │               │    │               │                             │
│  │ COMEN01C      │    │ CBEXPORT      │                             │
│  │ COADM01C      │    │ CBIMPORT      │                             │
│  │ COCOM01Y      │    │ CVEXPORT      │                             │
│  └───────────────┘    └───────────────┘                             │
└─────────────────────────────────────────────────────────────────────┘

Arrows: ──▶ = depends on / calls into
```

---

## 2. Bounded Context Definitions

### 2.1 Identity Context

**Responsibility:** User authentication, authorization, and user lifecycle management.

| Attribute           | Value                                                              |
|--------------------|--------------------------------------------------------------------|
| **Programs**        | COSGN00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C                  |
| **Data Owned**      | USRSEC (CSUSR01Y — 80-byte user record)                           |
| **BMS Maps**        | COSGN00, COUSR00, COUSR01, COUSR02, COUSR03                       |
| **Upstream Deps**   | None — this is a root context                                      |
| **Downstream Deps** | Every other context depends on Identity for authentication         |
| **LOC**             | 2,027                                                              |
| **Coupling**        | Low — only USRSEC file, no shared writes with other contexts       |

**Seam Quality:** Excellent. USRSEC is only accessed by Identity programs. Clean extraction boundary.

---

### 2.2 Account Context

**Responsibility:** Account master data management — view, update, and maintain credit card account records and associated customer data.

| Attribute           | Value                                                              |
|--------------------|--------------------------------------------------------------------|
| **Programs**        | COACTVWC, COACTUPC                                                 |
| **Data Owned**      | ACCTDATA (CVACT01Y — 300 bytes), CUSTDATA (CVCUS01Y — 500 bytes)  |
| **BMS Maps**        | COACTVW, COACTUP                                                   |
| **Upstream Deps**   | Identity (user auth), Card (cross-reference lookup)                |
| **Downstream Deps** | Transaction, Payment, Reporting (all read ACCTDATA)                |
| **LOC**             | 5,177                                                              |
| **Coupling**        | High — ACCTDATA is read by 9+ programs across 4 contexts          |

**Seam Quality:** Moderate. ACCTDATA is heavily shared. Customer data (CUSTDATA) is also read by Card and Reporting contexts. The Account Context must expose read APIs for other contexts.

**Shared Data Challenge:** COACTUPC writes to BOTH ACCTDATA and CUSTDATA in a single transaction. In the decomposed model, Customer may need to be a separate aggregate or Account must own the Customer lifecycle.

---

### 2.3 Card Context

**Responsibility:** Credit card lifecycle — listing, viewing, and updating card details.

| Attribute           | Value                                                              |
|--------------------|--------------------------------------------------------------------|
| **Programs**        | COCRDLIC, COCRDSLC, COCRDUPC                                      |
| **Data Owned**      | CARDDATA (CVACT02Y — 150 bytes), CARDXREF (CVACT03Y — 50 bytes)  |
| **BMS Maps**        | COCRDLI, COCRDSL, COCRDUP                                          |
| **Upstream Deps**   | Identity (user auth), Account (account lookup for card)            |
| **Downstream Deps** | Transaction (CARDXREF used for card-to-account resolution)         |
| **LOC**             | 3,906                                                              |
| **Coupling**        | Medium — CARDXREF is read by Transaction, Reporting, Statement     |

**Seam Quality:** Moderate. CARDXREF is a critical junction table used widely. The Card Context must expose a lookup API: `getAccountForCard(cardNumber)`.

---

### 2.4 Transaction Context

**Responsibility:** Transaction lifecycle — online inquiry/creation, batch posting, interest calculation, category balance tracking.

| Attribute           | Value                                                              |
|--------------------|--------------------------------------------------------------------|
| **Programs**        | COTRN00C, COTRN01C, COTRN02C (online); CBTRN01C, CBTRN02C, CBACT04C (batch) |
| **Data Owned**      | TRANSACT (CVTRA05Y — 350 bytes), DALYTRAN (CVTRA06Y — 350 bytes), TCATBAL (CVTRA01Y — 50 bytes), DISCGRP (CVTRA02Y — 50 bytes), TRANTYPE (CVTRA03Y — 60 bytes), TRANCATG (CVTRA04Y — 60 bytes) |
| **BMS Maps**        | COTRN00, COTRN01, COTRN02                                          |
| **Upstream Deps**   | Identity, Account (balance validation), Card (card-to-account resolution) |
| **Downstream Deps** | Reporting (reads TRANSACT), Payment (creates transactions)         |
| **LOC**             | 3,736 (online) + 1,877 (batch) = 5,613                            |
| **Coupling**        | Very High — central data hub; TRANSACT written by 3 programs, read by 6+ |

**Seam Quality:** Poor for extraction. This is the most coupled context. CBTRN02C writes to TRANSACT + TCATBAL + ACCTDATA (cross-context write to Account). CBACT04C similarly updates ACCTDATA from the Transaction context.

**Cross-Context Write Problem:** Batch programs CBTRN02C and CBACT04C update ACCTDATA (owned by Account Context) as a side effect of transaction posting and interest calculation. In the decomposed model, these must use the Account Context's API to update balances rather than direct database writes.

---

### 2.5 Payment Context

**Responsibility:** Bill payment processing — validates and creates payment transactions.

| Attribute           | Value                                                              |
|--------------------|--------------------------------------------------------------------|
| **Programs**        | COBIL00C                                                           |
| **Data Owned**      | None (creates transactions in TRANSACT, reads ACCTDATA + CARDXREF)|
| **BMS Maps**        | COBIL00                                                            |
| **Upstream Deps**   | Identity, Account, Card, Transaction (payment = special transaction)|
| **Downstream Deps** | None                                                               |
| **LOC**             | 572                                                                |
| **Coupling**        | Medium — reads 3 datasets, writes to 1                             |

**Seam Quality:** Good. Payment is a thin orchestration layer that coordinates Account and Transaction. It can be extracted as a service that calls Account and Transaction APIs.

**Design Decision:** Payment could be merged INTO the Transaction Context as a specialized transaction type, OR kept separate as a distinct business capability. Recommendation: **Keep separate** — payment processing has distinct compliance, audit, and idempotency requirements.

---

### 2.6 Reporting Context

**Responsibility:** Report generation — online report triggering, batch daily reports, account statement production.

| Attribute           | Value                                                              |
|--------------------|--------------------------------------------------------------------|
| **Programs**        | CORPT00C (online trigger); CBTRN03C (batch report); CBSTM03A, CBSTM03B (statements) |
| **Data Owned**      | Report output files (STATEMNT.PS, STATEMNT.HTML), CVTRA07Y layout  |
| **BMS Maps**        | CORPT00                                                            |
| **Upstream Deps**   | Transaction (reads TRANSACT), Account, Card, Customer              |
| **Downstream Deps** | None — terminal output context                                     |
| **LOC**             | 2,452                                                              |
| **Coupling**        | Low (read-only consumer of all other contexts)                     |

**Seam Quality:** Excellent. Reporting is purely read-only and produces output artifacts. It can be extracted independently as long as it has read access to Transaction, Account, Card, and Customer data.

---

### 2.7 Navigation Context

**Responsibility:** Menu routing and screen navigation flow.

| Attribute           | Value                                                              |
|--------------------|--------------------------------------------------------------------|
| **Programs**        | COMEN01C, COADM01C                                                 |
| **Data Owned**      | COCOM01Y (COMMAREA), COMEN02Y, COADM02Y (menu definitions)       |
| **BMS Maps**        | COMEN01, COADM01                                                   |
| **Upstream Deps**   | Identity (determines Regular vs. Admin menu)                       |
| **Downstream Deps** | Routes to all other contexts via XCTL                              |
| **LOC**             | 596                                                                |
| **Coupling**        | High coupling to COMMAREA structure, but logic is simple routing   |

**Seam Quality:** N/A — Navigation is eliminated in modernization. Web UI routing replaces CICS XCTL navigation entirely.

---

### 2.8 Data Migration Context

**Responsibility:** Bulk data export and import between VSAM and sequential files.

| Attribute           | Value                                                              |
|--------------------|--------------------------------------------------------------------|
| **Programs**        | CBEXPORT, CBIMPORT                                                 |
| **Data Owned**      | None (operates on all VSAM files)                                  |
| **LOC**             | 1,069                                                              |
| **Coupling**        | Reads/writes ALL core datasets — maximum breadth but utility only  |

**Seam Quality:** Good for extraction. Utility programs that can become Spring Batch import/export jobs independent of business logic.

---

## 3. Extraction Seam Analysis

A "seam" is a boundary where one context can be separated from another with minimal cross-boundary data flow. Quality depends on data coupling, call coupling, and transaction boundaries.

### Seam Quality Rankings

| Context Pair                      | Seam Quality | Barrier                                                       | Mitigation                                           |
|----------------------------------|-------------|---------------------------------------------------------------|------------------------------------------------------|
| Identity ↔ Everything            | **Clean**   | None — USRSEC is exclusively owned                            | Extract first as foundation service                  |
| Reporting ↔ Everything           | **Clean**   | Read-only consumer; no shared writes                          | Extract with read replicas or API queries            |
| Data Migration ↔ Everything      | **Clean**   | Utility only; no runtime coupling                             | Extract as standalone batch utility                  |
| Payment ↔ Transaction            | **Good**    | Payment creates transactions (write coupling)                 | Payment calls Transaction API to create entries      |
| Card ↔ Account                   | **Moderate**| CARDXREF links cards to accounts; shared reads                | Card exposes `getAccountForCard()` API               |
| Card ↔ Transaction               | **Moderate**| Transaction uses CARDXREF for card-to-account resolution      | Transaction calls Card API for resolution            |
| Account ↔ Transaction            | **Poor**    | CBTRN02C and CBACT04C **write** to ACCTDATA from Transaction  | Introduce Account Balance Update API; eventual consistency or saga pattern |
| Account ↔ Customer               | **Poor**    | COACTUPC writes to BOTH in a single operation                 | Keep Customer as sub-entity of Account, or use two-phase update |

### Cross-Context Write Violations

These are the most critical seam issues — programs that write to data owned by another context:

| Program      | Source Context  | Writes To          | Owning Context | Severity |
|-------------|----------------|--------------------|--------------  |----------|
| CBTRN02C    | Transaction    | ACCTDATA           | Account        | **Critical** — balance updates during posting |
| CBACT04C    | Transaction    | ACCTDATA           | Account        | **Critical** — interest charges applied to balances |
| COACTUPC    | Account        | CUSTDATA           | Account*       | **Medium** — customer is co-owned with account |
| COBIL00C    | Payment        | TRANSACT           | Transaction    | **Medium** — payment creates transaction records |
| COTRN02C    | Transaction    | TRANSACT (write)   | Transaction    | None — same context |

*Customer data could be split into its own context but is currently tightly coupled with Account.

---

## 4. Data Ownership Matrix

| Dataset        | Owner Context  | Read By Contexts                          | Written By Contexts             |
|---------------|---------------|-------------------------------------------|----------------------------------|
| USRSEC        | Identity       | Identity                                  | Identity                         |
| ACCTDATA      | Account        | Account, Transaction, Payment, Reporting  | Account, Transaction (violation) |
| CUSTDATA      | Account        | Account, Card, Reporting                  | Account                          |
| CARDDATA      | Card           | Card                                      | Card                             |
| CARDXREF      | Card           | Card, Account, Transaction, Reporting     | Card (+ initial load)            |
| TRANSACT      | Transaction    | Transaction, Reporting                    | Transaction, Payment             |
| DALYTRAN      | Transaction    | Transaction (batch)                       | External feed                    |
| TCATBAL       | Transaction    | Transaction (batch)                       | Transaction (batch)              |
| DISCGRP       | Transaction    | Transaction (batch)                       | Reference data load              |
| TRANTYPE      | Transaction    | Transaction, Reporting                    | Reference data load              |
| TRANCATG      | Transaction    | Transaction, Reporting                    | Reference data load              |

---

## 5. Anti-Corruption Layer Design

During the strangler migration, an Anti-Corruption Layer (ACL) translates between legacy VSAM data formats and modern API contracts.

```
┌──────────────┐         ┌──────────────────┐         ┌──────────────┐
│ Legacy COBOL │  VSAM   │  Anti-Corruption  │  REST   │ Modern Java  │
│  Programs    │◀───────▶│     Layer (ACL)   │◀───────▶│  Services    │
│              │  files  │                    │  APIs   │              │
└──────────────┘         │ • VSAM↔DB sync    │         └──────────────┘
                         │ • Format transform│
                         │ • Event bridge    │
                         └──────────────────┘
```

### ACL Components

| Component                | Function                                                         |
|-------------------------|------------------------------------------------------------------|
| **VSAM-DB Sync**        | Bidirectional sync between VSAM files and PostgreSQL tables during coexistence period |
| **Format Transformer**  | Converts EBCDIC packed decimal / COMP fields to Java types       |
| **Event Bridge**        | Publishes domain events when VSAM files are updated by legacy programs (via CICS exit or file trigger) |
| **API Gateway**         | Routes requests to legacy or modern implementation based on feature flags |

---

## 6. Recommended Extraction Order

Based on seam quality, coupling analysis, and dependency direction:

```
Phase 1: Foundation (clean seams, no cross-context writes)
├── 1a. Identity Context          ← clean seam, all contexts depend on it
├── 1b. Navigation Context        ← eliminated (replaced by web UI routing)
└── 1c. Data Migration Context    ← utility, clean seam

Phase 2: Read-Only Consumers (clean seams, no writes)
├── 2a. Reporting Context         ← read-only, clean extraction
└── 2b. Card Context (list/view)  ← read-only operations first

Phase 3: Write Operations (moderate seams)
├── 3a. Card Context (update)     ← moderate coupling via CARDXREF
├── 3b. Payment Context           ← calls Transaction API for writes
└── 3c. Transaction Context (online) ← list/view/add

Phase 4: Batch Core (poor seams, cross-context writes)
├── 4a. Transaction Context (batch posting)    ← must resolve ACCTDATA write violation
├── 4b. Transaction Context (interest calc)    ← must resolve ACCTDATA write violation
└── 4c. Account Context (update)               ← depends on Transaction batch resolution

Phase 5: Final Cutover
├── 5a. Account Context (view)    ← simple, but wait for batch resolution
├── 5b. Decommission ACL          ← remove VSAM-DB sync
└── 5c. Decommission legacy       ← shut down CICS region
```

### Why This Order?

1. **Identity first** — Every context needs authentication. Building this first enables all subsequent services to use modern auth.
2. **Read-only before write** — Read operations can be strangled with zero risk to data integrity. They prove the data access layer works.
3. **Online before batch** — Online programs have simpler transaction boundaries (single request/response). Batch programs have complex multi-file update patterns.
4. **Account last** — ACCTDATA is the most widely shared dataset. Extracting Account requires all cross-context write violations to be resolved first via API boundaries.
