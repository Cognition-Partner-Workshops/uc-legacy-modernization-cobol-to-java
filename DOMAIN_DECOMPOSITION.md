# CardDemo Domain Decomposition

## 1. Overview

This document identifies bounded contexts within the CardDemo COBOL/CICS application, maps dependencies between domains, and performs extraction seam analysis to determine where the legacy system can be cleanly separated for modernization. The decomposition is grounded in the actual code structure: 31 COBOL programs, 30 copybooks, 17 BMS maps, 9 VSAM datasets, and 38 JCL jobs.

---

## 2. Bounded Contexts

### BC-1: Identity & Access Management

**Programs**: COSGN00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C, COADM01C
**Copybooks**: CSUSR01Y (SEC-USER-DATA), COCOM01Y (CDEMO-USER-ID, CDEMO-USER-TYPE)
**BMS Maps**: COSGN00, COUSR00, COUSR01, COUSR02, COUSR03, COADM01
**VSAM Files**: USRSEC

**Domain Entities**:
| Entity | Key | Source | Fields |
|--------|-----|--------|--------|
| User | SEC-USR-ID (8 chars) | CSUSR01Y | ID, first name, last name, password, type (A/U) |

**Responsibilities**:
- User authentication (COSGN00C): Validate credentials against USRSEC file
- Session context: Populate CARDDEMO-COMMAREA with user ID and type
- Role-based routing: Admin users → COADM01C; Regular users → COMEN01C
- User CRUD (COUSR00C-03C): List, add, update, delete users (admin only)
- Admin menu (COADM01C): Dynamic menu construction from COADM02Y copybook

**Coupling Points**:
- COCOM01Y COMMAREA carries user identity (CDEMO-USER-ID, CDEMO-USER-TYPE) across ALL programs
- Every online program checks EIBCALEN = 0 and redirects to COSGN00C (unauthenticated)
- Role flag (88 CDEMO-USRTYP-ADMIN / CDEMO-USRTYP-USER) gates admin-only operations

---

### BC-2: Account Management

**Programs**: COACTVWC, COACTUPC, CBACT01C, CBACT02C, CBACT03C
**Copybooks**: CVACT01Y (ACCOUNT-RECORD), CVACT02Y (CARD-RECORD), CVACT03Y (CARD-XREF-RECORD), CVCUS01Y (CUSTOMER-RECORD)
**BMS Maps**: COACTVW, COACTUPC
**VSAM Files**: ACCTDAT, CARDDAT, CUSTDAT, CARDAIX, CXACAIX

**Domain Entities**:
| Entity | Key | Source | Record Length |
|--------|-----|--------|:------------:|
| Account | ACCT-ID (11 digits) | CVACT01Y | 300 bytes |
| Card | CARD-NUM (16 chars) | CVACT02Y | 150 bytes |
| Customer | CUST-ID (9 digits) | CVCUS01Y | 500 bytes |
| Card Cross-Reference | XREF-CARD-NUM (16 chars) | CVACT03Y | 50 bytes |

**Responsibilities**:
- Account view (COACTVWC): Display account details with linked cards and customer info
- Account update (COACTUPC): Modify account fields with extensive validation (SSN format, phone, dates, credit limits, status codes)
- Batch refresh (CBACT01C-03C): Reload account, card, and customer master files from sequential input

**Aggregate Root**: Account
- An Account owns Cards (via CARD-ACCT-ID FK)
- An Account is linked to a Customer (via CARD-XREF-RECORD: card → customer + account)
- The cross-reference file (CARDXREF/CARDAIX/CXACAIX) serves as a join table with alternate index access paths

**Coupling Points**:
- ACCTDAT is read by Bill Payment (BC-4), Transaction Posting (BC-3), Interest Calculation (BC-5)
- CARD-XREF-RECORD is used by Bill Payment, Transaction Posting, Interest Calculation, and Statement Generation
- CUSTDAT is used by Statement Generation
- Account balance (ACCT-CURR-BAL) is modified by Bill Payment, Transaction Posting, and Interest Calculation

---

### BC-3: Transaction Processing

**Programs**: COTRN00C, COTRN01C, COTRN02C, CBTRN02C, CBTRN03C
**Copybooks**: CVTRA05Y (TRAN-RECORD), CVTRA06Y (DALYTRAN-RECORD), CVTRA01Y (TRAN-CAT-BAL-RECORD), CVTRA02Y (DISCGRP-RECORD)
**BMS Maps**: COTRN00, COTRN01, COTRN02
**VSAM Files**: TRANSACT, DALYTRAN, TCATBALF, DALYREJS

**Domain Entities**:
| Entity | Key | Source | Record Length |
|--------|-----|--------|:------------:|
| Transaction | TRAN-ID (16 chars) | CVTRA05Y | 350 bytes |
| Daily Transaction | Sequential | CVTRA06Y | 350 bytes |
| Transaction Category Balance | ACCT-ID+TYPE-CD+CAT-CD | CVTRA01Y | 50 bytes |
| Rejected Transaction | Sequential | — | 430 bytes |

**Responsibilities**:
- Transaction list (COTRN00C): Browse transactions with pagination (10 rows/page, PF7/PF8)
- Transaction view (COTRN01C): Display single transaction detail
- Transaction add (COTRN02C): Create new transaction with auto-generated ID
- Batch posting (CBTRN02C): Read daily transactions → validate (xref lookup, account verification) → post to TRANSACT + update TCATBALF + update ACCTDAT → write rejects to DALYREJS
- Transaction reporting (CBTRN03C): Generate filtered transaction reports

**Aggregate Root**: Transaction
- Transactions reference cards (TRAN-CARD-NUM) and merchants
- Category balances aggregate transactions by account + type + category
- Daily transactions are staging records consumed by the batch posting process

**Coupling Points**:
- TRANSACT file is written by online (COTRN02C, COBIL00C) and batch (CBTRN02C, CBACT04C)
- CBTRN02C reads CARDXREF (BC-2) and ACCTDAT (BC-2), updates TCATBALF and ACCTDAT
- TCATBALF is read by Interest Calculation (BC-5)
- Transaction ID generation: MAX(TRAN-ID) + 1 — shared sequence across all writers

---

### BC-4: Bill Payment

**Programs**: COBIL00C
**Copybooks**: CVACT01Y, CVACT03Y, CVTRA05Y (shared with BC-2 and BC-3)
**BMS Maps**: COBIL00
**VSAM Files**: ACCTDAT (shared), CXACAIX (shared), TRANSACT (shared)

**Domain Entities**: None unique — operates on Account and Transaction entities.

**Responsibilities**:
- Accept account ID and payment confirmation
- Validate account exists and has positive balance
- Look up card number via cross-reference (CXACAIX)
- Generate transaction ID (MAX+1 from TRANSACT)
- Create bill payment transaction record (type '02', category 2, source 'POS TERM')
- Deduct payment amount from account balance (ACCT-CURR-BAL)

**Coupling Points**:
- Reads/writes ACCTDAT (shared with BC-2, BC-3, BC-5)
- Reads CXACAIX (shared with BC-2)
- Writes TRANSACT (shared with BC-3)
- Uses same TRAN-ID generation as BC-3 (contention risk)

**Note**: Bill Payment is a strong candidate for merging into BC-3 (Transaction Processing) since it is essentially a specialized transaction type. It is called out separately because it has its own UI flow and business rules.

---

### BC-5: Financial Calculations

**Programs**: CBACT04C
**Copybooks**: CVTRA01Y, CVTRA02Y, CVACT01Y, CVACT03Y, CVTRA05Y
**VSAM Files**: TCATBALF, DISCGRP, ACCTDAT, CARDXREF, TRANSACT

**Domain Entities**:
| Entity | Key | Source | Purpose |
|--------|-----|--------|---------|
| Discount Group | ACCT-GROUP-ID+TYPE-CD+CAT-CD | CVTRA02Y | Interest rate lookup |

**Responsibilities**:
- Read transaction category balances sequentially
- Detect account boundaries (break logic on TRANCAT-ACCT-ID change)
- Look up interest rates from discount group table
- Compute monthly interest on category balances
- Compute fees
- Write interest transactions to TRANSACT
- Update account balances (ACCT-CURR-BAL += total interest)
- Reset cycle credits/debits

**Coupling Points**:
- Reads TCATBALF (written by BC-3 batch posting)
- Reads DISCGRP (reference data)
- Reads/writes ACCTDAT (shared with BC-2, BC-3, BC-4)
- Writes TRANSACT (shared with BC-3, BC-4)
- Reads CARDXREF (shared with BC-2)

---

### BC-6: Reporting & Statements

**Programs**: CORPT00C, CBSTM03A, CBSTM03B
**Copybooks**: CVTRA05Y, CVACT01Y, CVACT03Y, CVCUS01Y, CUSTREC, COSTM01
**BMS Maps**: CORPT00
**VSAM Files**: TRANSACT, ACCTDAT, CUSTDAT, CARDXREF
**JCL**: TRANREPT procedure

**Responsibilities**:
- Online report submission (CORPT00C): Accept report parameters (monthly/yearly/custom date range), validate dates, submit JCL batch job via CICS TDQ (Internal Reader)
- Statement generation (CBSTM03A/B): Generate per-account statements in plain text and HTML from transaction data, using customer and account master files
- Subroutine (CBSTM03B): File I/O operations called by CBSTM03A

**Coupling Points**:
- Read-only access to TRANSACT, ACCTDAT, CUSTDAT, CARDXREF
- No write coupling to other bounded contexts
- JCL job submission requires CICS TDQ infrastructure
- CBSTM03A uses ALTER/GO TO, mainframe control block addressing (PSA/TCB/TIOT) — deeply platform-specific

---

### BC-7: Data Administration

**Programs**: CBACT01C, CBACT02C, CBACT03C, CBCUS01C, CBEXPORT, CBIMPORT
**VSAM Files**: All master files

**Responsibilities**:
- Refresh/seed master data files from sequential input
- Export VSAM data to flat files
- Import flat files to VSAM datasets

**Coupling Points**:
- Touches all VSAM files but operates independently (typically run during maintenance windows)
- No runtime coupling with online programs

---

## 3. Dependency Matrix

This matrix shows which bounded contexts depend on which data stores. Shared data access is the primary source of coupling.

| VSAM File | BC-1 IAM | BC-2 Account | BC-3 Txn | BC-4 BillPay | BC-5 Financial | BC-6 Reporting | BC-7 DataAdmin |
|-----------|:--------:|:------------:|:--------:|:------------:|:--------------:|:--------------:|:--------------:|
| USRSEC | RW | | | | | | RW |
| ACCTDAT | | RW | RW | RW | RW | R | RW |
| CARDDAT | | R | | | | | RW |
| CUSTDAT | | R | | | | R | RW |
| CARDXREF/AIX | | R | R | R | R | R | RW |
| TRANSACT | | | RW | W | W | R | |
| DALYTRAN | | | R | | | | |
| TCATBALF | | | RW | | R | | |
| DISCGRP | | | | | R | | |
| DALYREJS | | | W | | | | |

**R** = Read, **W** = Write, **RW** = Read + Write

---

## 4. Extraction Seam Analysis

An extraction seam is a natural boundary where a bounded context can be separated from the legacy system with minimal disruption. The analysis below identifies seams, ranks them by extractability, and notes the coupling that must be addressed.

### Seam 1: Identity & Access Management (BC-1)
**Extractability: HIGH**

| Aspect | Detail |
|--------|--------|
| Data coupling | Single file: USRSEC. No other BC writes to it. |
| Interface seam | Authentication result flows through COCOM01Y COMMAREA (CDEMO-USER-ID, CDEMO-USER-TYPE). This becomes a JWT token claim. |
| Extraction approach | 1. Deploy Spring Security service with same user data. 2. API gateway validates JWT and injects user context. 3. Legacy programs continue reading COMMAREA (populated by gateway adapter). |
| Residual coupling | Every legacy program checks EIBCALEN for authentication redirect. Until all programs are migrated, the gateway adapter must populate COMMAREA for remaining CICS programs. |
| Risk | Low. Clear boundary, simple data model. |

### Seam 2: User Administration (subset of BC-1)
**Extractability: HIGH**

| Aspect | Detail |
|--------|--------|
| Data coupling | USRSEC file only. |
| Interface seam | Four CRUD programs with standard list/select/edit pattern. No complex business logic. |
| Extraction approach | Standard REST CRUD API. During dual-run, sync USRSEC file from the new database using a change-data-capture mechanism or scheduled sync. |
| Residual coupling | Authentication (COSGN00C) must see users created via the new admin UI. Solved by bidirectional sync or by extracting authentication simultaneously. |
| Risk | Low. Should be extracted together with Seam 1. |

### Seam 3: Card Management (subset of BC-2)
**Extractability: MEDIUM-HIGH**

| Aspect | Detail |
|--------|--------|
| Data coupling | Reads CARDDAT and CARDAIX. Card data is read-only from the online programs' perspective (no card creation/update in the current online CICS programs — only CBACT02C batch refreshes card data). |
| Interface seam | List/view/update screens with pagination. Well-defined VSAM browse pattern. |
| Extraction approach | Migrate CARDDAT to `cards` table. Strangler facade routes card queries to new service. Card updates flow to new database; batch refresh becomes a database import. |
| Residual coupling | CARD-XREF-RECORD is used by Transaction Processing and Bill Payment for card-to-account lookup. The new Card service must expose a card-xref API or the xref data must be accessible to other services. |
| Risk | Medium. Cross-reference dependency requires coordinated migration. |

### Seam 4: Transaction List/View (read-only subset of BC-3)
**Extractability: MEDIUM-HIGH**

| Aspect | Detail |
|--------|--------|
| Data coupling | Read-only access to TRANSACT file. |
| Interface seam | COTRN00C (list with pagination) and COTRN01C (view detail) are pure query operations. |
| Extraction approach | Migrate TRANSACT to `transactions` table. New service provides paginated list and detail endpoints. Legacy programs continue writing to VSAM; a CDC or periodic sync populates the database. |
| Residual coupling | Transaction writes (COTRN02C, COBIL00C, batch programs) must eventually target the new database. During transition, reads come from the database while writes go to VSAM with CDC sync. |
| Risk | Medium. Read-path extraction is safe; the risk is in eventual write-path migration. |

### Seam 5: Bill Payment (BC-4)
**Extractability: MEDIUM**

| Aspect | Detail |
|--------|--------|
| Data coupling | Reads ACCTDAT (with UPDATE lock), CXACAIX; writes TRANSACT; rewrites ACCTDAT. Three files across two bounded contexts. |
| Interface seam | Single program (COBIL00C) with a two-step flow (lookup → confirm → execute). Maps cleanly to a REST API with preview and confirm endpoints. |
| Extraction approach | New Bill Payment service reads account data from Account service API, creates transaction via Transaction service API, updates account balance via Account service API. Requires Account and Transaction services to be at least partially migrated. |
| Residual coupling | Depends on Account and Transaction services being available. Cannot be extracted before Seams 3/4 (or must use VSAM adapters). |
| Risk | Medium. Multiple cross-context writes create coordination complexity. |

### Seam 6: Account View/Update (core of BC-2)
**Extractability: MEDIUM**

| Aspect | Detail |
|--------|--------|
| Data coupling | ACCTDAT (RW), CARDDAT (R), CUSTDAT (R), CARDAIX (R), CXACAIX (R). Five VSAM files. |
| Interface seam | COACTVWC (view) is read-only and lower risk. COACTUPC (update) has 4,237 LOC of validation logic. |
| Extraction approach | Phase 1: Extract account view (COACTVWC) with read-only access to migrated tables. Phase 2: Extract account update (COACTUPC) with all validation rules reimplemented as a validation service. |
| Residual coupling | ACCTDAT is the most shared file in the system. Bill Payment, Transaction Posting, and Interest Calculation all modify ACCT-CURR-BAL. During transition, the Account service becomes the system of record and must expose balance-update APIs to other contexts. |
| Risk | Medium-High. The account balance is the most contested shared state in the system. |

### Seam 7: Reporting & Statements (BC-6)
**Extractability: MEDIUM**

| Aspect | Detail |
|--------|--------|
| Data coupling | Read-only access to TRANSACT, ACCTDAT, CUSTDAT, CARDXREF. |
| Interface seam | CORPT00C submits JCL via TDQ — no modern equivalent. CBSTM03A uses ALTER/GO TO and PSA/TCB control block addressing. |
| Extraction approach | Rewrite as Spring Batch jobs. The report submission UI becomes a React form that triggers jobs via REST. Statement generation queries the database directly. |
| Residual coupling | Must wait for transaction and account data to be in the database (or use VSAM adapters). No write coupling simplifies extraction. |
| Risk | Medium. The complexity is in reimplementing the statement format and report logic, not in coupling. |

### Seam 8: Batch Transaction Posting (BC-3 batch)
**Extractability: LOW-MEDIUM**

| Aspect | Detail |
|--------|--------|
| Data coupling | Reads DALYTRAN; writes TRANSACT, TCATBALF, DALYREJS; reads CARDXREF, ACCTDAT; updates ACCTDAT. Six files. |
| Interface seam | Batch program invoked by JCL. No online interface. |
| Extraction approach | Rewrite as Spring Batch job. Requires all referenced data (accounts, cards, xref, transactions, category balances) to be in the database. |
| Residual coupling | TCATBALF is consumed by Interest Calculation (Seam 9). TRANSACT is shared with all transaction readers/writers. ACCTDAT balance updates must be coordinated with Bill Payment and Interest Calculation. |
| Risk | Medium-High. Highest cross-context data coupling in the system. Must be one of the last batch programs migrated. |

### Seam 9: Interest Calculation (BC-5)
**Extractability: LOW-MEDIUM**

| Aspect | Detail |
|--------|--------|
| Data coupling | Reads TCATBALF, DISCGRP, ACCTDAT, CARDXREF; writes TRANSACT, ACCTDAT. |
| Interface seam | Batch program with break logic on account boundaries. |
| Extraction approach | Rewrite as Spring Batch job after TCATBALF and DISCGRP are migrated. The interest computation formula must be precisely preserved through equivalence testing. |
| Residual coupling | Depends on TCATBALF being populated by the migrated batch posting job. DISCGRP is reference data that can be migrated early. |
| Risk | High. Financial calculation precision must be guaranteed. Requires end-of-cycle coordination with Transaction Posting. |

### Seam 10: Data Administration (BC-7)
**Extractability: HIGH (but low priority)**

| Aspect | Detail |
|--------|--------|
| Data coupling | Touches all files but runs independently. |
| Interface seam | Batch-only, no online coupling. |
| Extraction approach | Replace with Flyway migrations + Spring Batch import jobs. Much of this functionality becomes unnecessary once data lives in a database. |
| Residual coupling | None at runtime. Only relevant during initial data migration. |
| Risk | Low. These programs become obsolete post-migration. |

---

## 5. Extraction Order (by Seam Readiness)

```
Phase 1 (Foundation)         Phase 2 (Core Services)       Phase 3 (Complex Batch)
━━━━━━━━━━━━━━━━━━          ━━━━━━━━━━━━━━━━━━━━━         ━━━━━━━━━━━━━━━━━━━━━

Seam 1: IAM ────────────►   Seam 5: Bill Payment ──────►  Seam 8: Batch Posting
  (USRSEC only)                (cross-context writes)        (6-file coupling)
                                                            
Seam 2: User Admin ─────►   Seam 6: Account Mgmt ──────►  Seam 9: Interest Calc
  (extract with IAM)           (ACCTDAT ownership)           (financial precision)
                                                            
Seam 10: Data Admin ────►   Seam 7: Reporting ─────────►  
  (seed database)              (read-only, rewrite)         
                                                            
Seam 3: Card Mgmt ──────►                                  
  (read-heavy, low risk)                                    
                                                            
Seam 4: Txn List/View ──►                                  
  (read-only extraction)                                    
```

---

## 6. Shared Kernel: COMMAREA and Cross-Cutting Concerns

The COCOM01Y COMMAREA is the shared kernel that spans all bounded contexts. It carries:

```
CARDDEMO-COMMAREA
├── CDEMO-GENERAL-INFO
│   ├── CDEMO-FROM-TRANID      (navigation: source transaction)
│   ├── CDEMO-FROM-PROGRAM     (navigation: source program)
│   ├── CDEMO-TO-TRANID        (navigation: target transaction)
│   ├── CDEMO-TO-PROGRAM       (navigation: target program)
│   ├── CDEMO-USER-ID          (identity: current user)
│   ├── CDEMO-USER-TYPE        (identity: admin or user)
│   └── CDEMO-PGM-CONTEXT      (state: first entry or re-entry)
├── CDEMO-CUSTOMER-INFO
│   ├── CDEMO-CUST-ID          (context: selected customer)
│   ├── CDEMO-CUST-FNAME/MNAME/LNAME
├── CDEMO-ACCOUNT-INFO
│   ├── CDEMO-ACCT-ID          (context: selected account)
│   └── CDEMO-ACCT-STATUS
├── CDEMO-CARD-INFO
│   └── CDEMO-CARD-NUM         (context: selected card)
└── CDEMO-MORE-INFO
    ├── CDEMO-LAST-MAP          (UI state)
    └── CDEMO-LAST-MAPSET       (UI state)
```

**Modernization**: The COMMAREA becomes:
- **JWT claims**: User ID, user type
- **API request context**: Account ID, customer ID, card number passed as path/query parameters
- **Session state**: Eliminated — modern services are stateless
- **Navigation state**: Handled by frontend router
- **Program context (re-entry flag)**: Eliminated — REST APIs are stateless

---

## 7. Anti-Corruption Layer Design

During the transition period, an Anti-Corruption Layer (ACL) translates between legacy CICS/VSAM patterns and modern REST/RDBMS patterns:

```
┌─────────────────────────────────────────────────┐
│                Modern Services                    │
│  (REST APIs, Spring Boot, PostgreSQL)            │
└────────────┬────────────────────┬────────────────┘
             │                    │
    ┌────────v────────┐  ┌───────v────────┐
    │ VSAM Adapter    │  │ CICS Adapter   │
    │ (Read/Write     │  │ (COMMAREA      │
    │  VSAM files     │  │  translation,  │
    │  via Java API)  │  │  XCTL routing) │
    └────────┬────────┘  └───────┬────────┘
             │                    │
┌────────────v────────────────────v────────────────┐
│              Legacy CICS/VSAM System              │
│  (COBOL programs, BMS maps, VSAM files)          │
└──────────────────────────────────────────────────┘
```

The ACL handles:
1. **Data format translation**: COBOL PIC clauses (packed decimal, zoned decimal, alphanumeric) ↔ Java types
2. **Key format translation**: VSAM composite keys ↔ database composite/surrogate keys
3. **Transaction coordination**: CICS SYNCPOINT ↔ database COMMIT
4. **Record locking**: CICS READ UPDATE ↔ JPA optimistic/pessimistic locking
5. **Bidirectional sync**: Changes in the new system propagated to VSAM (and vice versa) during dual-run period
