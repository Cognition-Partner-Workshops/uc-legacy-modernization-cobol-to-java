# CardDemo Domain Decomposition

> **Purpose:** Identify bounded contexts, define extraction seams, and map the monolithic COBOL/CICS application to a target microservice architecture
> **Method:** Data affinity analysis (which programs share which VSAM files/copybooks), behavioral cohesion (which programs collaborate on a workflow), and domain-driven design (DDD) bounded context identification

---

## 1. Bounded Context Map

```
┌──────────────────────────────────────────────────────────────────────────┐
│                         CardDemo Application                             │
│                                                                          │
│  ┌──────────────┐  ┌──────────────┐  ┌────────────────┐  ┌───────────┐  │
│  │  Identity &   │  │   Account    │  │     Card       │  │Transaction│  │
│  │  Access (IAM) │  │  Management  │  │  Management    │  │ Processing│  │
│  │              │  │              │  │                │  │           │  │
│  │ COSGN00C    │  │ COACTVWC    │  │ COCRDLIC      │  │ COTRN00C  │  │
│  │ COMEN01C    │  │ COACTUPC    │  │ COCRDSLC      │  │ COTRN01C  │  │
│  │ COADM01C    │  │ CBACT01C    │  │ COCRDUPC      │  │ COTRN02C  │  │
│  │ COUSR00C    │  │              │  │ CBACT02C      │  │ COBIL00C  │  │
│  │ COUSR01C    │  │              │  │                │  │ CBTRN01C  │  │
│  │ COUSR02C    │  │              │  │                │  │ CBTRN02C  │  │
│  │ COUSR03C    │  │              │  │                │  │ CBTRN03C  │  │
│  │              │  │              │  │                │  │ CBACT04C  │  │
│  └──────┬───────┘  └──────┬───────┘  └───────┬────────┘  └─────┬─────┘  │
│         │                 │                  │                 │         │
│         │   ┌─────────────┼──────────────────┼─────────────────┤         │
│         │   │             │                  │                 │         │
│  ┌──────▼───▼──┐  ┌──────▼───────┐  ┌───────▼────────┐  ┌────▼──────┐  │
│  │  Reporting  │  │   Customer   │  │  Reference     │  │  Data     │  │
│  │  & Statements│  │   (embedded) │  │  Data          │  │  Migration│  │
│  │              │  │              │  │                │  │           │  │
│  │ CORPT00C    │  │ (within      │  │ DISCGRP       │  │ CBEXPORT  │  │
│  │ CBTRN03C    │  │  Account &   │  │ TRANTYPE      │  │ CBIMPORT  │  │
│  │ CBSTM03A/B  │  │  Card ctx)   │  │ TRANCATG      │  │           │  │
│  └──────────────┘  └──────────────┘  └────────────────┘  └───────────┘  │
│                                                                          │
└──────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Bounded Context Definitions

### 2.1 Identity & Access Management (IAM)

| Attribute | Value |
|-----------|-------|
| **Core Entity** | User Security Record (CSUSR01Y) |
| **VSAM File** | USRSEC |
| **Programs** | COSGN00C (sign-on), COMEN01C (user menu), COADM01C (admin menu), COUSR00C-03C (user CRUD) |
| **Copybooks** | CSUSR01Y, COCOM01Y (COMMAREA) |
| **BMS Maps** | COSGN00, COMEN01, COADM01, COUSR00-03 (7 maps) |
| **External Coupling** | Writes user context to COMMAREA consumed by all downstream programs |
| **Data Ownership** | Owns USRSEC exclusively; no other context writes to it |

**Context Boundary Analysis:**
- **Inbound:** None (entry point)
- **Outbound:** COMMAREA fields (user ID, user type, program name) consumed by every online program
- **Seam Quality:** **Clean** -- USRSEC is read/written only by IAM programs. The COMMAREA is the sole integration contract. This context can be extracted first with a JWT/session token replacing the COMMAREA user fields.

---

### 2.2 Account Management

| Attribute | Value |
|-----------|-------|
| **Core Entities** | Account Master (CVACT01Y), Customer Master (CVCUS01Y) |
| **VSAM Files** | ACCTFILE (R/W), CUSTFILE (R/W) |
| **Programs** | COACTVWC (view), COACTUPC (update), CBACT01C (batch read) |
| **Copybooks** | CVACT01Y, CVCUS01Y, CVCRD01Y, CVACT03Y, CSLKPCDY, CSUTLDWY |
| **BMS Maps** | COACTVW, COACTUP (2 maps) |
| **External Coupling** | ACCTFILE read by Transaction Processing (balance checks, updates); CUSTFILE read by Card and Reporting contexts |
| **Data Ownership** | Primary owner of ACCTFILE and CUSTFILE; Transaction Processing has write access to ACCTFILE (balance updates) |

**Context Boundary Analysis:**
- **Inbound:** Card context reads CUSTFILE for customer name display; Transaction context reads ACCTFILE for balance validation
- **Outbound:** None (self-contained inquiry/update cycle)
- **Seam Quality:** **Contested** -- ACCTFILE is written by both Account Management (COACTUPC) and Transaction Processing (CBTRN02C, CBACT04C, COBIL00C). This is the most significant data coupling in the entire application.

**Coupling Resolution Strategy:**
The ACCTFILE write contention between Account Management and Transaction Processing is the critical seam to resolve. Options:

| Option | Approach | Trade-off |
|--------|----------|-----------|
| **A. Shared database** | Single `accounts` table, both services write with row-level locking | Simple but increases coupling |
| **B. Event-driven** | Transaction service publishes `BalanceChanged` events; Account service applies them | Decoupled but adds eventual consistency |
| **C. Account owns balance** | Transaction service calls Account API to update balance | Clean ownership but adds synchronous dependency |

**Recommendation:** Option C during initial migration (simplest), evolve to Option B when event infrastructure is mature.

---

### 2.3 Card Management

| Attribute | Value |
|-----------|-------|
| **Core Entities** | Card Master (CVACT02Y), Card Cross-Reference (CVACT03Y) |
| **VSAM Files** | CARDFILE (R/W), CARDXREF (R) |
| **Programs** | COCRDLIC (list), COCRDSLC (view), COCRDUPC (update), CBACT02C (batch read) |
| **Copybooks** | CVACT02Y, CVACT03Y, CVCRD01Y, CVCUS01Y (read-only) |
| **BMS Maps** | COCRDLI, COCRDSL, COCRDUP (3 maps) |
| **External Coupling** | CARDXREF is heavily read by Transaction Processing for card→account lookup; CUSTFILE is read for customer display |
| **Data Ownership** | Owns CARDFILE exclusively; shares read access to CARDXREF with Transaction Processing |

**Context Boundary Analysis:**
- **Inbound:** Account context navigates to card list; Transaction context reads CARDXREF for card→account resolution
- **Outbound:** Reads CUSTFILE (Account context) for customer name display on card screens
- **Seam Quality:** **Moderate** -- CARDFILE writes are isolated to this context. CARDXREF is read-only from Transaction's perspective. The cross-context CUSTFILE read can be replaced with an API call.

---

### 2.4 Transaction Processing

| Attribute | Value |
|-----------|-------|
| **Core Entities** | Transaction Master (CVTRA05Y), Daily Transaction (CVTRA06Y), Category Balance (CVTRA01Y) |
| **VSAM Files** | TRANSACT (R/W), DALYTRAN (R), TCATBALF (R/W), DALYREJS (W) |
| **Programs** | COTRN00C (list), COTRN01C (view), COTRN02C (add), COBIL00C (bill payment), CBTRN01C (batch validation), CBTRN02C (posting), CBACT04C (interest calc) |
| **Copybooks** | CVTRA05Y, CVTRA06Y, CVTRA01Y, CVTRA02Y |
| **BMS Maps** | COTRN00, COTRN01, COTRN02, COBIL00 (4 maps) |
| **External Coupling** | Reads CARDXREF (Card context), reads/writes ACCTFILE (Account context), reads DISCGRP (Reference Data) |
| **Data Ownership** | Owns TRANSACT, DALYTRAN, TCATBALF, DALYREJS exclusively |

**Context Boundary Analysis:**
- **Inbound:** Reporting context reads TRANSACT for report generation and statements
- **Outbound:** Reads CARDXREF for card→account resolution; reads/writes ACCTFILE for balance updates; reads DISCGRP for interest rates
- **Seam Quality:** **Complex** -- This is the most coupled context. It reaches into Account (balance updates), Card (cross-reference lookup), and Reference Data (interest rates). Extraction requires establishing clear API contracts for each dependency.

**Sub-Context Decomposition:**
```
Transaction Processing
├── Online Transactions (COTRN00C, COTRN01C, COTRN02C, COBIL00C)
│   └── Real-time transaction entry and bill payment
├── Batch Posting (CBTRN01C, CBTRN02C)
│   └── Daily transaction validation and posting
└── Interest & Fees (CBACT04C)
    └── Interest calculation and fee assessment
```

---

### 2.5 Reporting & Statements

| Attribute | Value |
|-----------|-------|
| **Core Entity** | Report output files (no persistent entity -- pure consumers) |
| **VSAM Files** | TRANSACT (R), CARDXREF (R), CUSTFILE (R), ACCTFILE (R), TRANTYPE (R), TRANCATG (R) |
| **Programs** | CORPT00C (report submission), CBTRN03C (daily report), CBSTM03A/CBSTM03B (statements) |
| **Copybooks** | CVTRA05Y, CVACT03Y, CVTRA03Y, CVTRA04Y, CVTRA07Y, COSTM01, CUSTREC |
| **BMS Maps** | CORPT00 (1 map) |
| **External Coupling** | Pure read-only consumer of Transaction, Account, Card, Customer, and Reference Data |
| **Data Ownership** | Owns only output files (report files, statement files) |

**Context Boundary Analysis:**
- **Inbound:** CORPT00C submits JCL via CICS internal reader (triggers CBTRN03C batch)
- **Outbound:** Reads from 6 VSAM files across 3 other contexts
- **Seam Quality:** **Clean** -- purely read-only. Once other contexts expose APIs or share a database, reporting becomes a simple query consumer. No write contention.

---

### 2.6 Reference Data

| Attribute | Value |
|-----------|-------|
| **Core Entities** | Disclosure Group (CVTRA02Y), Transaction Type (CVTRA03Y), Transaction Category (CVTRA04Y) |
| **VSAM Files** | DISCGRP (R), TRANTYPE (R), TRANCATG (R) |
| **Programs** | None (loaded by JCL, consumed by batch programs) |
| **Copybooks** | CVTRA02Y, CVTRA03Y, CVTRA04Y |
| **External Coupling** | Read by Transaction Processing (CBACT04C, CBTRN03C) |
| **Data Ownership** | Static reference data loaded from JCL; no online programs modify it |

**Context Boundary Analysis:**
- **Seam Quality:** **Clean** -- immutable reference data. Extract as a shared reference data service or database lookup tables. No write contention.

---

### 2.7 Data Migration

| Attribute | Value |
|-----------|-------|
| **Programs** | CBEXPORT, CBIMPORT |
| **VSAM Files** | All 5 master files (R for export, W for import) |
| **External Coupling** | Reads/writes every entity across all contexts |

**Context Boundary Analysis:**
- **Seam Quality:** **Cross-cutting** -- by design, export/import touches all contexts. In the target architecture, this becomes a Spring Batch utility that reads/writes from the shared database. Not a domain service; treat as infrastructure.

---

## 3. Extraction Seam Analysis

### 3.1 Data Coupling Matrix

Each cell shows the access pattern: **R** = Read, **W** = Write, **RW** = Read/Write, **-** = No access

| VSAM File | IAM | Account | Card | Transaction | Reporting | Ref Data | Migration |
|-----------|:---:|:-------:|:----:|:-----------:|:---------:|:--------:|:---------:|
| USRSEC | **RW** | - | - | - | - | - | - |
| ACCTFILE | - | **RW** | R | **RW** ⚠️ | R | - | RW |
| CUSTFILE | - | **RW** | R | R | R | - | RW |
| CARDFILE | - | R | **RW** | - | - | - | RW |
| CARDXREF | - | R | **R** | R | R | - | RW |
| TRANSACT | - | - | - | **RW** | R | - | RW |
| DALYTRAN | - | - | - | **R** | - | - | - |
| TCATBALF | - | - | - | **RW** | - | - | - |
| DALYREJS | - | - | - | **W** | - | - | - |
| DISCGRP | - | - | - | R | - | **R** | - |
| TRANTYPE | - | - | - | - | R | **R** | - |
| TRANCATG | - | - | - | - | R | **R** | - |

⚠️ = Cross-context write contention (critical seam)

### 3.2 Seam Classification

| Seam | Type | Contexts Involved | Difficulty | Notes |
|------|------|-------------------|:----------:|-------|
| **USRSEC boundary** | Data | IAM (exclusive) | Easy | No contention; clean extraction |
| **COMMAREA contract** | Behavioral | IAM → All Online | Easy | Replace with JWT claims |
| **CARDXREF lookups** | Data | Card → Transaction | Moderate | Read-only cross-context; API or shared table |
| **CUSTFILE reads** | Data | Account → Card, Reporting | Moderate | Read-only cross-context; API or shared table |
| **ACCTFILE writes** | Data | Account ↔ Transaction | **Hard** | Write contention; requires ownership decision |
| **Report JCL submission** | Behavioral | Reporting → Batch | Moderate | CICS internal reader → Spring Batch trigger |
| **Batch cycle ordering** | Temporal | All batch contexts | **Hard** | CLOSEFIL→POSTTRAN→INTCALC→CREASTMT sequence |
| **CBSTM03A→CBSTM03B CALL** | Structural | Reporting (internal) | Easy | Merge into single service |

### 3.3 Critical Seam: ACCTFILE Write Contention

This is the single most important seam to resolve for successful decomposition.

**Current State:**
```
ACCTFILE (Account Master)
    ├── COACTUPC  writes: credit limit, account status, customer info changes
    ├── COBIL00C  writes: balance update after bill payment
    ├── CBTRN02C  writes: balance update after transaction posting
    └── CBACT04C  writes: balance update after interest/fee calculation
```

**Resolution Strategy (phased):**

**Phase 1: Shared Table**
```
accounts table (PostgreSQL)
    ├── AccountService.updateAccount()       -- admin changes (from COACTUPC)
    ├── TransactionService.postPayment()     -- balance update (from COBIL00C)
    ├── PostingJob.updateBalance()           -- batch posting (from CBTRN02C)
    └── InterestJob.applyInterest()          -- interest calc (from CBACT04C)
    
    All use row-level locking (SELECT FOR UPDATE) to prevent conflicts
```

**Phase 2: Event-Driven**
```
TransactionService publishes:
    BalanceChangedEvent { accountId, oldBalance, newBalance, transactionId }
    
AccountService subscribes:
    Updates materialized balance view for display
    
PostingJob publishes:
    BatchPostingCompletedEvent { batchId, affectedAccounts[] }
```

### 3.4 Critical Seam: Batch Cycle Temporal Coupling

**Current State:**
```
CLOSEFIL → [data refresh] → POSTTRAN → INTCALC → TRANBKP → COMBTRAN → CREASTMT → TRANIDX → OPENFIL
```

All jobs must run in sequence because VSAM files require exclusive access.

**Target State:**
```
With RDBMS (row-level locking), the temporal coupling is eliminated:
- Online and batch can run concurrently
- CLOSEFIL / OPENFIL are eliminated
- Data refresh becomes database seed/sync
- Jobs can run independently (with DB transaction isolation)

Remaining ordering requirements (business logic):
  POSTTRAN → INTCALC  (interest calculated on posted transactions)
  POSTTRAN → CREASTMT (statements include posted transactions)
  POSTTRAN → TRANREPT (report covers posted transactions)
```

---

## 4. Target Service Architecture

### 4.1 Service Definitions

| Service | Source Programs | Entities Owned | API Surface |
|---------|----------------|----------------|-------------|
| **iam-service** | COSGN00C, COMEN01C, COADM01C, COUSR00C-03C | `users` | `POST /auth/login`, `GET/POST/PUT/DELETE /users` |
| **account-service** | COACTVWC, COACTUPC, CBACT01C | `accounts`, `customers` | `GET/PUT /accounts/{id}`, `GET /customers/{id}` |
| **card-service** | COCRDLIC, COCRDSLC, COCRDUPC, CBACT02C | `cards`, `card_xref` | `GET /cards`, `GET/PUT /cards/{id}` |
| **transaction-service** | COTRN00C-02C, COBIL00C | `transactions` | `GET /transactions`, `POST /transactions`, `POST /payments` |
| **batch-posting-job** | CBTRN01C, CBTRN02C | `daily_transactions`, `rejects` | Spring Batch job (no REST API) |
| **interest-calc-job** | CBACT04C | `category_balances` | Spring Batch job (no REST API) |
| **reporting-service** | CORPT00C, CBTRN03C, CBSTM03A/B | (output only) | `POST /reports/generate`, `GET /statements/{id}` |
| **reference-data-service** | (JCL-loaded) | `disclosure_groups`, `tran_types`, `tran_categories` | `GET /reference/{type}` |
| **data-migration-job** | CBEXPORT, CBIMPORT | (cross-cutting) | CLI / Spring Batch job |

### 4.2 Inter-Service Communication

```
                    ┌──────────────────┐
                    │   API Gateway    │
                    │  (Spring Cloud)  │
                    └────────┬─────────┘
                             │
        ┌────────────────────┼────────────────────┐
        │                    │                    │
   ┌────▼─────┐      ┌──────▼──────┐      ┌─────▼──────┐
   │iam-service│      │account-svc  │      │card-service │
   │           │      │             │◄─────│  (reads     │
   │ JWT issuer│      │ Balance API │      │  customer)  │
   └───────────┘      └──────▲──────┘      └─────────────┘
                             │
                    ┌────────┴────────┐
                    │transaction-svc  │
                    │ (calls Account  │
                    │  Balance API    │
                    │  for payments)  │
                    └────────┬────────┘
                             │
              ┌──────────────┼──────────────┐
              │              │              │
       ┌──────▼──────┐ ┌────▼─────┐ ┌──────▼──────┐
       │batch-posting│ │interest  │ │reporting-svc│
       │  job        │ │calc-job  │ │             │
       │(writes txns,│ │(reads cat│ │(reads all   │
       │ calls Acct  │ │ bals,    │ │ via DB or   │
       │ Balance API)│ │ calls    │ │ service APIs│
       └─────────────┘ │ Acct API)│ └─────────────┘
                        └──────────┘
```

### 4.3 Database Schema (Target)

| Table | Source VSAM | Primary Key | Owner Service |
|-------|------------|-------------|---------------|
| `users` | USRSEC | `user_id VARCHAR(8)` | iam-service |
| `accounts` | ACCTFILE | `account_id BIGINT` | account-service |
| `customers` | CUSTFILE | `customer_id BIGINT` | account-service |
| `cards` | CARDFILE | `card_number VARCHAR(16)` | card-service |
| `card_xref` | CARDXREF | `card_number VARCHAR(16)` | card-service |
| `transactions` | TRANSACT | `transaction_id VARCHAR(16)` | transaction-service |
| `daily_transactions` | DALYTRAN | `transaction_id VARCHAR(16)` | batch-posting-job |
| `category_balances` | TCATBALF | `(account_id, type_cd, cat_cd)` | interest-calc-job |
| `rejected_transactions` | DALYREJS | `reject_id BIGSERIAL` | batch-posting-job |
| `disclosure_groups` | DISCGRP | `(group_id, tran_type, cat_cd)` | reference-data-service |
| `transaction_types` | TRANTYPE | `tran_type VARCHAR(2)` | reference-data-service |
| `transaction_categories` | TRANCATG | `(type_cd, cat_cd)` | reference-data-service |

---

## 5. Extraction Order

Based on seam quality, coupling analysis, and risk:

| Order | Context | Seam Quality | Rationale |
|------:|---------|:------------:|-----------|
| 1 | **IAM** | Clean | Zero write contention; establishes auth foundation; all other services need it |
| 2 | **Reference Data** | Clean | Static, read-only; provide as shared lookup tables or API; no migration risk |
| 3 | **Reporting** | Clean | Read-only consumer; once data is in RDBMS, reports become SQL queries |
| 4 | **Card Management** | Moderate | CARDFILE writes are isolated; CARDXREF is read-only externally |
| 5 | **Account Management** | Contested | Must resolve ACCTFILE write contention before or during this step |
| 6 | **Transaction (Online)** | Complex | Depends on Account API for balance updates; extract after Account |
| 7 | **Transaction (Batch)** | Complex | Highest risk; extract last with extensive parallel-run validation |
| 8 | **Data Migration** | Cross-cutting | Infrastructure utility; migrate last or replace with DB-native tools |
