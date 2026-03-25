# CardDemo Domain Decomposition

> Bounded contexts, extraction seam analysis, and duplicate logic identification for microservice decomposition.

---

## 1. Identified Bounded Contexts

Based on data ownership, program clustering, and business capability alignment, the CardDemo application decomposes into **7 bounded contexts**.

```
┌──────────────────────────────────────────────────────────────────────┐
│                        CardDemo Application                          │
│                                                                      │
│  ┌─────────────┐  ┌─────────────┐  ┌──────────────────────────────┐ │
│  │  Identity &  │  │   Account   │  │     Card Management          │ │
│  │  Access Mgmt │  │ Management  │  │                              │ │
│  │             │  │             │  │  COCRDLIC, COCRDSLC,         │ │
│  │  COSGN00C   │  │  COACTVWC   │  │  COCRDUPC                   │ │
│  │  COUSR00C   │  │  COACTUPC   │  │                              │ │
│  │  COUSR01C   │  │             │  │  Data: CARDFILE, CARDXREF   │ │
│  │  COUSR02C   │  │  Data:      │  └──────────────────────────────┘ │
│  │  COUSR03C   │  │  ACCTFILE   │                                    │
│  │             │  │  CUSTFILE   │  ┌──────────────────────────────┐ │
│  │  Data:      │  └─────────────┘  │   Transaction Processing     │ │
│  │  USRSEC     │                    │                              │ │
│  └─────────────┘  ┌─────────────┐  │  Online: COTRN00C-02C       │ │
│                    │  Financial   │  │  Batch:  CBTRN01C, CBTRN02C│ │
│  ┌─────────────┐  │  Operations  │  │  Payment: COBIL00C          │ │
│  │  Reporting  │  │             │  │                              │ │
│  │  & Stmts    │  │  CBACT04C   │  │  Data: TRANSACT, DALYTRAN  │ │
│  │             │  │             │  └──────────────────────────────┘ │
│  │  CORPT00C   │  │  Data:      │                                    │
│  │  CBTRN03C   │  │  TCATBALF   │  ┌──────────────────────────────┐ │
│  │  CBSTM03A/B │  │  DISCGRP    │  │   Data Migration             │ │
│  │  TXT2PDF1   │  └─────────────┘  │                              │ │
│  │             │                    │  CBEXPORT, CBIMPORT          │ │
│  │  Data: read │                    │                              │ │
│  │  from all   │                    │  Data: EXPORTFL              │ │
│  └─────────────┘                    └──────────────────────────────┘ │
└──────────────────────────────────────────────────────────────────────┘
```

---

## 2. Bounded Context Details

### BC-1: Identity & Access Management

| Attribute | Value |
|-----------|-------|
| **Programs** | COSGN00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C |
| **Data Owned** | USRSEC VSAM (user credentials and roles) |
| **BMS Maps** | COSGN00, COUSR00–03 |
| **Coupling** | LOW — USRSEC is only accessed by these 5 programs |
| **External Dependencies** | None |
| **Extraction Difficulty** | ★☆☆☆☆ Easy |

**Seam Analysis:**
- Clean data boundary — no other bounded context writes to USRSEC
- COSGN00C routes users to COMEN01C or COADM01C via XCTL — this is the only outbound coupling point
- CSUSR01Y copybook is referenced by other programs, but only for reading the user type from COMMAREA (not accessing USRSEC directly)
- **Extraction seam:** Replace XCTL navigation with REST redirect after JWT authentication

**Duplicate Logic Identified:**
- COUSR01C (Add), COUSR02C (Update), and COUSR03C (Delete) each duplicate BMS SEND/RECEIVE patterns, error message handling, and VSAM access boilerplate
- **Consolidation:** Merge into single User CRUD Service with shared validation and persistence layer

---

### BC-2: Account Management

| Attribute | Value |
|-----------|-------|
| **Programs** | COACTVWC, COACTUPC |
| **Data Owned** | ACCTFILE VSAM (account balances, limits, dates) |
| **Shared Data** | CUSTFILE (read), CARDXREF (read) |
| **BMS Maps** | COACTVW, COACTUP |
| **Coupling** | HIGH — ACCTFILE is written by batch posting and bill payment |
| **Extraction Difficulty** | ★★★★☆ Hard |

**Seam Analysis:**
- ACCTFILE is the most contended resource: written by COACTUPC (online), COBIL00C (online), CBTRN01C/02C (batch), CBACT04C (batch)
- CUSTFILE is read by Account and Card contexts — candidate for shared read-only view
- **Extraction seam:** Expose Account Service API. Other contexts call API instead of reading VSAM directly. Requires CLOSEFIL/OPENFIL elimination.
- COACTUPC's COPY REPLACING pattern (30+ instances of CSSETATY) is a maintenance burden that will be eliminated by modern UI frameworks

**Duplicate Logic Identified:**
- COACTVWC and COACTUPC both perform the same ACCTFILE + CARDXREF + CUSTFILE read/join pattern (estimated ~200 lines of duplicated read logic)
- Both programs duplicate BMS screen initialization, error handling, and COMMAREA management
- **Consolidation:** Extract shared Account Read Service; COACTUPC extends it with write capability

---

### BC-3: Card Management

| Attribute | Value |
|-----------|-------|
| **Programs** | COCRDLIC, COCRDSLC, COCRDUPC |
| **Data Owned** | CARDFILE VSAM, CARDXREF VSAM |
| **Shared Data** | CUSTFILE (read), ACCTFILE (read via CARDXREF) |
| **BMS Maps** | COCRDLI, COCRDSL, COCRDUP |
| **Coupling** | MEDIUM — CARDXREF is the system-wide join table read by 12 programs |
| **Extraction Difficulty** | ★★★☆☆ Medium |

**Seam Analysis:**
- CARDXREF is the tightest coupling point in the system — it bridges Card and Account contexts
- COCRDLIC has role-based filtering logic that duplicates the auth check pattern from COMMAREA
- **Extraction seam:** Card Management owns CARDFILE + CARDXREF. Expose `GET /cards?accountId=X` API. Other contexts query this API instead of reading CARDXREF VSAM.
- The STARTBR/READNEXT/READPREV browse pattern in COCRDLIC (1,459 lines) replaces with SQL pagination

**Duplicate Logic Identified:**
- COCRDSLC (Detail) and COCRDUPC (Update) duplicate CARDFILE + CUSTFILE read logic (~150 lines each)
- All three programs duplicate BMS attribute control, COMMAREA navigation, and error display patterns
- CVCRD01Y card constant definitions are included by 5 programs across Card and Account contexts
- **Consolidation:** Single Card Service with list/detail/update endpoints sharing data access layer

---

### BC-4: Transaction Processing

| Attribute | Value |
|-----------|-------|
| **Programs** | COTRN00C, COTRN01C, COTRN02C (online), CBTRN01C, CBTRN02C (batch), COBIL00C (payment) |
| **Data Owned** | TRANSACT VSAM, DALYTRAN (daily input) |
| **Shared Data** | ACCTFILE (read/write), CARDXREF (read), TCATBALF (write) |
| **BMS Maps** | COTRN00, COTRN01, COTRN02, COBIL00 |
| **Coupling** | VERY HIGH — writes to TRANSACT, ACCTFILE, and TCATBALF |
| **Extraction Difficulty** | ★★★★★ Very Hard |

**Seam Analysis:**
- TRANSACT is written by both online (COTRN02C, COBIL00C) and batch (CBTRN01C/02C) — requires careful coordination
- Transaction ID generation in COTRN02C uses STARTBR/READPREV anti-pattern — must be replaced with UUID or database sequence
- COBIL00C (bill payment) creates transactions AND updates account balances — it straddles Transaction and Account contexts
- **Extraction seam:** 
  1. First extract read-only Transaction List/View (COTRN00C/01C)
  2. Then extract Transaction Add (COTRN02C) with new ID generation
  3. COBIL00C becomes a Payment Service that publishes events consumed by both Transaction and Account services
  4. Batch posting (CBTRN01C/02C) becomes a Spring Batch job reading from event queue

**Duplicate Logic Identified:**
- **CBTRN01C and CBTRN02C are near-duplicates** — both post daily transactions. CBTRN02C adds category balance tracking but shares ~60% of the posting logic with CBTRN01C
- COTRN00C and COTRN01C duplicate TRANSACT read patterns and BMS display logic
- COTRN02C and COBIL00C both write to TRANSACT with similar validation patterns
- **Consolidation:** 
  - Merge CBTRN01C + CBTRN02C into single Transaction Posting Service (eliminates ~300 lines of duplicate logic)
  - Merge COTRN00C + COTRN01C into Transaction Query Service
  - COBIL00C becomes standalone Payment Service calling Transaction Service API

---

### BC-5: Financial Operations

| Attribute | Value |
|-----------|-------|
| **Programs** | CBACT04C |
| **Data Owned** | TCATBALF VSAM (category balances), DISCGRP VSAM (interest rates) |
| **Shared Data** | ACCTFILE (read), CARDXREF (read), TRANSACT (read) |
| **BMS Maps** | None (batch only) |
| **Coupling** | MEDIUM — reads from multiple contexts but owns its own calculation data |
| **Extraction Difficulty** | ★★★☆☆ Medium |

**Seam Analysis:**
- Interest calculation is a pure computation — reads rates and balances, produces updated balances
- TCATBALF is also written by CBTRN02C during posting — shared write creates coupling with Transaction Processing
- **Extraction seam:** Interest Calculation Service reads account/transaction data via APIs from BC-2 and BC-4. Owns TCATBALF and DISCGRP tables.

**Duplicate Logic Identified:**
- CBACT04C reads CARDXREF to map cards to accounts — same pattern as 11 other programs
- **Consolidation:** Use Card Management API for card-to-account resolution instead of direct VSAM access

---

### BC-6: Reporting & Statements

| Attribute | Value |
|-----------|-------|
| **Programs** | CORPT00C (online trigger), CBTRN03C (batch report), CBSTM03A/B (statement gen), TXT2PDF1 (PDF conversion) |
| **Data Owned** | Report/statement output files (STMTFILE, HTMLFILE) |
| **Shared Data** | Reads from TRANSACT, CARDXREF, CUSTFILE, ACCTFILE, TRANTYPE, TRANCATG |
| **BMS Maps** | CORPT00 |
| **Coupling** | LOW — purely read-only against other contexts' data |
| **Extraction Difficulty** | ★★☆☆☆ Easy-Medium |

**Seam Analysis:**
- Entirely read-only — no writes to master data files
- CORPT00C uses TDQ to submit batch JCL via internal reader — a mainframe-specific pattern
- CBSTM03A calls CBSTM03B 13 times for file I/O — tightly coupled subroutine pair
- **Extraction seam:** Reporting Service queries other contexts via APIs or read replicas. Replace TDQ with REST trigger or message queue.

**Duplicate Logic Identified:**
- CBTRN03C and CBSTM03A both join TRANSACT + CARDXREF + account data — different output formats but same data retrieval logic (~200 lines duplicated)
- CVTRA07Y report layout definitions are only used by CBTRN03C but could serve as a shared report template definition
- **Consolidation:** Shared Transaction Data Retrieval Service feeds both report and statement generators

---

### BC-7: Data Migration

| Attribute | Value |
|-----------|-------|
| **Programs** | CBEXPORT, CBIMPORT |
| **Data Owned** | EXPORTFL (multi-record export file) |
| **Shared Data** | Reads/writes all master files |
| **Coupling** | LOW — utility programs used on-demand, not in regular batch cycle |
| **Extraction Difficulty** | ★☆☆☆☆ Easy |

**Seam Analysis:**
- Standalone utility — only used for data migration scenarios
- CVEXPORT copybook defines a multi-record format that's specific to mainframe data movement
- **Extraction seam:** Replace with REST API endpoints for data export (JSON/CSV) and import (file upload + validation)

**Duplicate Logic Identified:**
- CBEXPORT and CBIMPORT share the same CVEXPORT record layout and 5 copybook includes — they're mirror images of each other
- Both duplicate VSAM open/read/write/close patterns for each file type
- **Consolidation:** Single Data Migration Service with export and import endpoints sharing data access layer

---

## 3. Extraction Seam Priority Map

| Priority | Seam | Difficulty | Risk | Dependencies to Break |
|----------|------|-----------|------|----------------------|
| **1** | Identity & Access → Auth Service | ★☆☆☆☆ | Low | XCTL to menu programs → REST redirect |
| **2** | Reporting → Reporting Service | ★★☆☆☆ | Low | TDQ trigger → REST/MQ trigger; VSAM reads → API calls |
| **3** | Data Migration → Migration Service | ★☆☆☆☆ | Low | VSAM I/O → database queries; EXPORTFL format → JSON/CSV |
| **4** | Card List/Detail → Card Query API | ★★☆☆☆ | Low | CICS browse → SQL pagination; VSAM READ → SELECT |
| **5** | Transaction List/View → Txn Query API | ★★☆☆☆ | Low | CICS browse → SQL pagination |
| **6** | Account View → Account Query API | ★★☆☆☆ | Low | Multi-file JOIN → SQL JOIN |
| **7** | Card Update → Card Mutation API | ★★★☆☆ | Med | VSAM REWRITE → JPA save |
| **8** | User CRUD consolidation | ★★☆☆☆ | Low | 4 programs → 1 service |
| **9** | Transaction Add → Txn Creation API | ★★★☆☆ | Med | ID generation fix; VSAM WRITE → INSERT |
| **10** | Bill Payment → Payment Service | ★★★★☆ | High | Dual-write TRANSACT+ACCTFILE → DB transaction |
| **11** | Account Update → Account Mutation API | ★★★★☆ | High | 4,236 lines of validation; VSAM REWRITE → JPA |
| **12** | Batch Posting → Posting Service | ★★★★★ | High | CLOSEFIL elimination; multi-file writes → DB transactions |
| **13** | Interest Calc → Finance Service | ★★★★★ | High | Financial precision; TCATBALF shared writes |
| **14** | Statement Gen → Statement Service | ★★★☆☆ | Med | CBSTM03A/B coupling; HTML gen → template engine |

---

## 4. Duplicate Logic Summary

| Duplicate Pattern | Instances | Lines Saved | Consolidation Target |
|-------------------|-----------|-------------|---------------------|
| VSAM browse (STARTBR/READNEXT/READPREV/ENDBR) | COCRDLIC, COTRN00C, COUSR00C, COBIL00C | ~400 | Shared pagination utility → SQL OFFSET/LIMIT |
| CARDXREF card→account lookup | 12 programs | ~200 | Card Management API endpoint |
| BMS SEND/RECEIVE + error display | All 17 online programs | ~600 | REST controller + error handling middleware |
| COMMAREA management | All 17 online programs | ~300 | Spring Security context + session management |
| ACCTFILE + CUSTFILE + CARDXREF join | COACTVWC, COACTUPC, COCRDSLC, COCRDUPC, CBSTM03A | ~300 | Account Query Service with JOIN |
| CBTRN01C / CBTRN02C posting logic | 2 programs | ~300 | Single Transaction Posting Service |
| CBEXPORT / CBIMPORT mirror logic | 2 programs | ~200 | Single Data Migration Service |
| CBTRN03C / CBSTM03A data retrieval | 2 programs | ~200 | Shared Transaction Data Service |
| CEE3ABD error termination pattern | All 12 batch programs | ~100 | Exception handling framework |
| COUSR01C/02C/03C user CRUD patterns | 3 programs | ~200 | Single User Service |
| **Total estimated duplicate elimination** | | **~2,800 lines** | |

---

## 5. Context Mapping (Inter-Context Communication)

### Current State (COBOL/CICS)

| From Context | To Context | Mechanism | Data Exchanged |
|-------------|-----------|-----------|----------------|
| Identity | Account Mgmt, Card Mgmt | XCTL + COMMAREA | User ID, user type, selected account |
| Account Mgmt | Card Mgmt | COMMAREA | Account ID for card filtering |
| Card Mgmt | Account Mgmt | CARDXREF VSAM read | Card → Account resolution |
| Transaction | Account Mgmt | VSAM REWRITE | Balance updates |
| Transaction | Card Mgmt | CARDXREF VSAM read | Card validation |
| Financial Ops | Account Mgmt | VSAM REWRITE | Interest-adjusted balances |
| Financial Ops | Transaction | TCATBALF VSAM | Category balance updates |
| Reporting | All contexts | VSAM READ | Read-only data access |

### Target State (Microservices)

| From Context | To Context | Mechanism | Data Exchanged |
|-------------|-----------|-----------|----------------|
| Identity | All | JWT token | User claims (ID, role, permissions) |
| Account Mgmt | Card Mgmt | REST API call | `GET /cards?accountId=X` |
| Card Mgmt | Account Mgmt | REST API call | `GET /accounts/{id}` |
| Transaction | Account Mgmt | Domain event | `TransactionPosted` → balance update |
| Transaction | Card Mgmt | REST API call | `GET /cards/{num}` for validation |
| Financial Ops | Account Mgmt | Domain event | `InterestCalculated` → balance update |
| Reporting | All contexts | Read replica / CQRS query | Materialized views |
| Data Migration | All contexts | Bulk API / ETL | Batch import/export |
